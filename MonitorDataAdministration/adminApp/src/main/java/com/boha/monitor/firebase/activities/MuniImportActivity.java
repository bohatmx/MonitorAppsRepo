package com.boha.monitor.firebase.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.boha.monitor.firebase.R;
import com.boha.monitor.firebase.data.MonitorCompanyDTO;
import com.boha.monitor.firebase.data.MunicipalityDTO;
import com.boha.monitor.firebase.data.ProvinceDTO;
import com.boha.monitor.firebase.util.DataUtil;
import com.boha.monitor.firebase.util.ImportUtil;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

public class MuniImportActivity extends AppCompatActivity {

    static final String TAG = MuniImportActivity.class.getSimpleName();

    private FirebaseDatabase db;
    private FirebaseAnalytics analytics;
    private Snackbar snackbar;
    private TextView txtTitle, txtCount;
    private Spinner fileSpinner;
    private Button btnImport;
    private ImageView image;
    private RecyclerView recyclerView;
    private List<MunicipalityDTO> municipalities = new ArrayList<>();
    private List<File> files = new ArrayList<File>();
    private ProvinceDTO province;
    private MonitorCompanyDTO company;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.import_users);
//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);

        company = (MonitorCompanyDTO)getIntent().getSerializableExtra("company");
        province = (ProvinceDTO) getIntent().getSerializableExtra("province");
        db = FirebaseDatabase.getInstance();
        analytics = FirebaseAnalytics.getInstance(getApplicationContext());
        setFields();

        setTitle("Municipalities");
        sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        new Mtask().execute();
    }


    private void setFields() {
        fileSpinner = (Spinner) findViewById(R.id.IMP_fileSpinner);
        btnImport = (Button) findViewById(R.id.IMP_btnImport);
        txtTitle = (TextView) findViewById(R.id.IMP_title);
        txtCount = (TextView) findViewById(R.id.IMP_count);
        recyclerView = (RecyclerView) findViewById(R.id.IMP_list);
        LinearLayoutManager llm = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL,false);
        recyclerView.setLayoutManager(llm);
        recyclerView.setHasFixedSize(true);

        txtTitle.setText(province.getProvinceName());

        btnImport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDialog();
            }
        });


//        setSpinner();
//        Log.w(TAG, "Import files found: " + files.size());
    }

    private void showDialog() {

        AlertDialog.Builder dg = new AlertDialog.Builder(this);
        StringBuilder sb = new StringBuilder();
        sb.append("Do you want to import these municipalities?\n\n");
        for (MunicipalityDTO u: municipalities) {
            sb.append(u.getMunicipalityName()).append("\n");
        }
        dg.setTitle(province.getProvinceName())
                .setMessage(sb.toString())
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        index = 0;
                        importMunis();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .show();
    }
    int index;
    private void importMunis() {
        if (index < municipalities.size()) {
            importOneMuni(municipalities.get(index));
            return;
        }
        Log.i(TAG, "importMunis:  imported: " + municipalities.size());
        snackbar = Snackbar.make(fileSpinner,"" + municipalities.size() + " municipalities have been created on MPS",
                Snackbar.LENGTH_INDEFINITE);
        snackbar.setActionTextColor(ContextCompat.getColor(getApplicationContext(), R.color.green_200));
        snackbar.setAction("OK", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                snackbar.dismiss();
            }
        });
        snackbar.show();
        
    }
    private void importOneMuni(final MunicipalityDTO m) {
        m.setProvinceID(province.getProvinceID());
        m.setProvinceName(province.getProvinceName());
        Log.w(TAG, "importOneMuni " + m.getMunicipalityName());
        DataUtil.addMunicipality(m, new DataUtil.DataAddedListener() {
            @Override
            public void onResponse(String key) {
                Log.e(TAG, "++++++++++++++++ onResponse: Muni created on MPS:"
                        + m.getMunicipalityName() + " key: " + key);
                index++;
                importMunis();
            }

            @Override
            public void onError(String message) {
                snackbar = Snackbar.make(fileSpinner,message,
                        Snackbar.LENGTH_INDEFINITE);
                snackbar.setActionTextColor(ContextCompat.getColor(getApplicationContext(), R.color.red_500));
                snackbar.setAction("OK", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        snackbar.dismiss();
                    }
                });
                snackbar.show();
            }
        });
    }
    private void setSpinner() {

        List<String> list = new ArrayList<String>();
        list.add("Select import file");
        for (File p : files) {
            list.add(p.getName() + " - " + sdf.format(new Date(p.lastModified())));
        }
        ArrayAdapter a = new ArrayAdapter(getApplicationContext(), R.layout.simple_list_item_small, list);
        fileSpinner.setAdapter(a);
        fileSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int index, long l) {
                if (index == 0) {
                    return;
                }
                try {
                    parseFile(files.get(index - 1));
                } catch (IOException e) {
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }
    private void parseFile(File file) throws IOException {
        municipalities = new ArrayList<>();

        HashMap<String, MunicipalityDTO> map = new HashMap<>();
        BufferedReader brReadMe = new BufferedReader(new InputStreamReader(
                new FileInputStream(file), "UTF-8"));
        String strLine = brReadMe.readLine();
        while (strLine != null) {
            MunicipalityDTO dto = null;
            try {
                dto = parseLine(strLine);
                map.put(dto.getMunicipalityName(), dto);
                strLine = brReadMe.readLine();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        brReadMe.close();

        for (MunicipalityDTO m: map.values()) {
            municipalities.add(m);
            Log.e(TAG, "####### Muni added to list from import file: " + m.getMunicipalityName());
        }

        showDialog();
    }

    public static MunicipalityDTO parseLine(String line) throws Exception {
        Pattern patt = Pattern.compile(";");

        if (line.indexOf(",") > -1) {
            patt = Pattern.compile(",");
        }
        String[] result = patt.split(line);
        MunicipalityDTO dto = new MunicipalityDTO();
        try {
            if (result[0] != null) {
                String m = result[0];
                m = m.replaceAll("\"", "");
                dto.setMunicipalityName(m);
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            //ignore
        }
        Log.d(TAG,
                "Found muni: " + dto.getMunicipalityName() + " to import into MPS");
        return dto;
    }

    SharedPreferences sp;

    FirebaseRecyclerAdapter<MunicipalityDTO, MuniViewHolder> adapter;
    @Override
    public void onStart() {
        super.onStart();
        Log.i(TAG, "+++++++++++++++++++ onStart: FirebaseAuth.getInstance(");
        if (province == null) {
            return;
        }
        final DatabaseReference userRef = db.getReference(DataUtil.MONITOR_DB)
                .child(DataUtil.PROVINCES)
                .child(province.getProvinceID())
                .child(DataUtil.MUNICIPALITIES);
                
        Query query = userRef.orderByChild("municipalityName");
         adapter  =
                new FirebaseRecyclerAdapter<MunicipalityDTO, MuniViewHolder>(
                        MunicipalityDTO.class,
                        R.layout.place_item,
                        MuniViewHolder.class,
                        query
                ) {
                    @Override
                    protected void populateViewHolder(MuniViewHolder h, final MunicipalityDTO model, int position) {
                        Log.d(TAG, "####### populateViewHolder: " + model.getMunicipalityName());
                        h.name.setText(model.getMunicipalityName());
                        h.addCities.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                addCities(model);
                            }
                        });
                        h.name.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                addCities(model);
                            }
                        });
                        h.label.setText("Cities");
                        txtCount.setText("" + adapter.getItemCount());
                        h.count.setText("" + (position + 1));
                        h.count.setBackground(ContextCompat.getDrawable(
                                getApplicationContext(), R.drawable.xblack_oval_small));
                    }
                };

        recyclerView.setAdapter(adapter);

    }

    private void addCities(MunicipalityDTO muni) {
        Log.d(TAG, "addMonitors requested: " + muni.getMunicipalityName());
        Intent m = new Intent(getApplicationContext(),CityImportActivity.class);
        m.putExtra("muni",muni);
        if (company != null) {
            m.putExtra("company",company);
        }
        startActivity(m);

    }

    static final Locale loc = Locale.getDefault();
    static final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", loc);

    @Override
    public void onStop() {
        super.onStop();
        Log.w(TAG, "---------------- onStop: ");

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        return super.onOptionsItemSelected(item);
    }
    public static class MuniViewHolder extends RecyclerView.ViewHolder {
        protected TextView name, label, count;
        protected ImageView addCities;


        public MuniViewHolder(View itemView) {
            super(itemView);
            name = (TextView) itemView.findViewById(R.id.name);
            addCities = (ImageView) itemView.findViewById(R.id.addIcon);
            label = (TextView) itemView.findViewById(R.id.label);
            count = (TextView) itemView.findViewById(R.id.count);


        }

    }

    class Mtask extends AsyncTask<Void,Void,Integer> {

        @Override
        protected Integer doInBackground(Void... params) {
            files.clear();
            files = ImportUtil.getImportFiles();
            files.addAll(ImportUtil.getImportFilesOnSD());

            return 0;
        }
        @Override
        protected void onPostExecute(Integer result) {
            if (files.isEmpty()) {
                snackbar = Snackbar.make(btnImport,"No import files found", Snackbar.LENGTH_INDEFINITE);
                snackbar.setActionTextColor(ContextCompat.getColor(getApplicationContext(), R.color.red_500));
                snackbar.setAction("OK", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        snackbar.dismiss();
                        return;
                    }
                });
                snackbar.show();
            } else {
                setSpinner();
            }
        }
    }
}

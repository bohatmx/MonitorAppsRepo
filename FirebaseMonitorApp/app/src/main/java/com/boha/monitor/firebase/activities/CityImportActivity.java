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
import com.boha.monitor.library.data.CityDTO;
import com.boha.monitor.library.data.MonitorCompanyDTO;
import com.boha.monitor.library.data.MunicipalityDTO;
import com.boha.monitor.library.util.DataUtil;
import com.boha.monitor.library.util.ImportUtil;
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
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

public class CityImportActivity extends AppCompatActivity {

    static final String TAG = CityImportActivity.class.getSimpleName();

    private FirebaseDatabase db;
    private FirebaseAnalytics analytics;
    private Snackbar snackbar;
    private TextView txtTitle, txtCount;
    private Spinner fileSpinner;
    private Button btnImport;
    private ImageView image;
    private RecyclerView recyclerView;
    private List<CityDTO> cities = new ArrayList<>();
    private List<File> files = new ArrayList<File>();
    private MunicipalityDTO municipality;
    private MonitorCompanyDTO company;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.import_users);
//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);

        municipality = (MunicipalityDTO) getIntent().getSerializableExtra("muni");
        company = (MonitorCompanyDTO)getIntent().getSerializableExtra("company");
        db = FirebaseDatabase.getInstance();
        analytics = FirebaseAnalytics.getInstance(getApplicationContext());
        setFields();

        setTitle("Municipality Cities");
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

        txtTitle.setText(municipality.getMunicipalityName());

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
        sb.append("Do you want to import these cities?\n\n");
        for (CityDTO u: cities) {
            sb.append(u.getCityName()).append("\n");
        }
        dg.setTitle(municipality.getMunicipalityName())
                .setMessage(sb.toString())
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        index = 0;
                        importCities();
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
    private void importCities() {
        if (index < cities.size()) {
            importOneCity(cities.get(index));
            return;
        }
        Log.i(TAG, "importCities: " + cities.size());
        snackbar = Snackbar.make(fileSpinner,"" + cities.size() + " cities have been created on MPS",
                Snackbar.LENGTH_INDEFINITE);
        snackbar.setActionTextColor(ContextCompat.getColor(getApplicationContext(),R.color.green_200));
        snackbar.setAction("OK", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                snackbar.dismiss();
            }
        });
        snackbar.show();

    }
    private void importOneCity(final CityDTO m) {
        m.setProvinceID(municipality.getProvinceID());
        m.setMunicipalityID(municipality.getMunicipalityID());
        m.setMunicipalityName(municipality.getMunicipalityName());
        Log.w(TAG, "importOneCity: " + m.getMunicipalityName());
        DataUtil.addCity(m, new DataUtil.DataAddedListener() {
            @Override
            public void onResponse(String key) {
                Log.e(TAG, "++++++++++++++++ onResponse: City created on MPS:"
                        + m.getCityName() + " key: " + key);
                index++;
                importCities();
            }

            @Override
            public void onError(String message) {
                snackbar = Snackbar.make(fileSpinner,message,
                        Snackbar.LENGTH_INDEFINITE);
                snackbar.setActionTextColor(ContextCompat.getColor(getApplicationContext(),R.color.red_500));
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
        cities = new ArrayList<>();
        BufferedReader brReadMe = new BufferedReader(new InputStreamReader(
                new FileInputStream(file), "UTF-8"));
        String strLine = brReadMe.readLine();
        while (strLine != null) {
            CityDTO dto = null;
            try {
                dto = parseLine(strLine);
                if (dto.getMunicipalityName().equalsIgnoreCase(municipality.getMunicipalityName())) {
                    cities.add(dto);
                    Log.i(TAG, "####### city added to list from import file: " + dto.getCityName());
                } else {
                    Log.d(TAG, "parseFile: ignored: " + dto.getCityName());
                }
                strLine = brReadMe.readLine();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        brReadMe.close();

        Log.i(TAG, "parseFile: cities for import: " + cities.size());
        if (!cities.isEmpty()) {
            showDialog();
        } else {
            Snackbar.make(btnImport,"No cities found for import",Snackbar.LENGTH_SHORT).show();
        }
    }

    public static CityDTO parseLine(String line) throws Exception {
        Pattern patt = Pattern.compile(";");

        if (line.indexOf(",") > -1) {
            patt = Pattern.compile(",");
        }
        String[] result = patt.split(line);
        CityDTO dto = new CityDTO();
        try {
            if (result[0] != null) {
                String m = result[0];
                m = m.replaceAll("\"", "");
                dto.setCityName(m);
            }
            if (result[1] != null) {
                String m = result[1];
                m = m.replaceAll("\"", "");
                dto.setMunicipalityName(m);
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            //ignore
        }
        Log.d(TAG,
                "Found city: " + dto.getCityName() + " to attempt import into MPS");
        return dto;
    }

    SharedPreferences sp;

    FirebaseRecyclerAdapter<CityDTO, CityViewHolder> adapter;
    @Override
    public void onStart() {
        super.onStart();
        Log.i(TAG, "+++++++++++++++++++ onStart: FirebaseAuth.getInstance(");
        if (municipality == null) {
            return;
        }
        final DatabaseReference cityRef = db.getReference(DataUtil.MONITOR_DB)
                .child(DataUtil.PROVINCES)
                .child(municipality.getProvinceID())
                .child(DataUtil.MUNICIPALITIES)
                .child(municipality.getMunicipalityID())
                .child(DataUtil.CITIES);


        Query query = cityRef.orderByChild("cityName");
         adapter  =
                new FirebaseRecyclerAdapter<CityDTO, CityViewHolder>(
                        CityDTO.class,
                        R.layout.place_item,
                        CityViewHolder.class,
                        cityRef
                ) {
                    @Override
                    protected void populateViewHolder(CityViewHolder h, final CityDTO model, int position) {
                        Log.d(TAG, "####### populateViewHolder: " + model.getCityName());
                        h.name.setText(model.getCityName());
                        h.addProjects.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                addProjects(model);
                            }
                        });
                        h.name.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                addProjects(model);
                            }
                        });
                        txtCount.setText("" + adapter.getItemCount());
                        h.count.setText("" + (position + 1));
                        h.count.setBackground(ContextCompat.getDrawable(
                                getApplicationContext(),R.drawable.xgreen_oval_small));
                    }
                };

        recyclerView.setAdapter(adapter);

    }

    private void addProjects(CityDTO c) {
        Log.d(TAG, "addMonitors requested: " + c.getCityName());
        Intent m = new Intent(getApplicationContext(),ProjectImportActivity.class);
        m.putExtra("city",c);
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
    public static class CityViewHolder extends RecyclerView.ViewHolder {
        protected TextView name, label, count;
        protected ImageView addProjects;


        public CityViewHolder(View itemView) {
            super(itemView);
            name = (TextView) itemView.findViewById(R.id.name);
            addProjects = (ImageView) itemView.findViewById(R.id.addIcon);
            label = (TextView) itemView.findViewById(R.id.label);
            count = (TextView) itemView.findViewById(R.id.count);
            label.setText("Projects");
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
                snackbar.setActionTextColor(ContextCompat.getColor(getApplicationContext(),R.color.red_500));
                snackbar.setAction("OK", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        snackbar.dismiss();
                        finish();
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

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
import com.boha.monitor.firebase.data.ProvinceDTO;
import com.boha.monitor.firebase.util.Constants;
import com.boha.monitor.firebase.util.DataUtil;
import com.boha.monitor.firebase.util.ImportUtil;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
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

public class ProvinceImportActivity extends AppCompatActivity {

    static final String TAG = ProvinceImportActivity.class.getSimpleName();

    private FirebaseDatabase db;
    private FirebaseAnalytics analytics;
    private Snackbar snackbar;
    private TextView txtTitle, txtCount;
    private Spinner fileSpinner;
    private Button btnImport;
    private ImageView image;
    private RecyclerView recyclerView;
    private List<ProvinceDTO> provinces = new ArrayList<>();
    private List<File> files = new ArrayList<File>();
    private MonitorCompanyDTO company;
    private InterstitialAd mInterstitialAd;
    private boolean showAd = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.import_users);
//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);

        company = (MonitorCompanyDTO)getIntent().getSerializableExtra("company");
        db = FirebaseDatabase.getInstance();
        analytics = FirebaseAnalytics.getInstance(getApplicationContext());
        setFields();

        setTitle("Provinces");
        sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        new Mtask().execute();
        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId(Constants.INTERSTITIAL_AD);
        requestNewAd();
    }

    private void requestNewAd() {
        AdRequest request = new AdRequest.Builder()
                .addTestDevice("3E:F3:AA:D7:B9:B4:71:22:A7:F0:CD:4F:89:74:84:6A:92:8C:99:E0")
                .build();
        mInterstitialAd.loadAd(request);
        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                showAd = false;
                onBackPressed();
            }
        });
    }
    @Override
    public void onBackPressed() {
        if (showAd) {
            if (mInterstitialAd.isLoaded()) {
                mInterstitialAd.show();
                return;
            }
        }

        finish();

    }


    private void setFields() {
        fileSpinner = (Spinner) findViewById(R.id.IMP_fileSpinner);
        btnImport = (Button) findViewById(R.id.IMP_btnImport);
        txtTitle = (TextView) findViewById(R.id.IMP_title);
        txtCount = (TextView) findViewById(R.id.IMP_count);
        recyclerView = (RecyclerView) findViewById(R.id.IMP_list);
        LinearLayoutManager llm = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(llm);
        recyclerView.setHasFixedSize(true);

        txtTitle.setText("Provinces");

        btnImport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDialog();
            }
        });
    }

    private void showDialog() {

        AlertDialog.Builder dg = new AlertDialog.Builder(this);
        StringBuilder sb = new StringBuilder();
        sb.append("Do you want to import these provinces?\n\n");
        for (ProvinceDTO u : provinces) {
            sb.append(u.getProvinceName()).append("\n");
        }
        dg.setTitle("Province Import")
                .setMessage(sb.toString())
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        index = 0;
                        importProvinces();
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

    private void importProvinces() {
        if (index < provinces.size()) {
            importOneProvince(provinces.get(index));
            return;
        }
        Log.i(TAG, "importUsers: Provinces imported: " + provinces.size());
        snackbar = Snackbar.make(fileSpinner, "" + provinces.size() + " provinces have been created on MPS",
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

    private void importOneProvince(final ProvinceDTO p) {
        Log.w(TAG, "importOneProvince " + p.getProvinceName());
        DataUtil.addProvince(p, new DataUtil.DataAddedListener() {
            @Override
            public void onResponse(String key) {
                Log.e(TAG, "++++++++++++++++ onResponse: User created on MPS:"
                        + p.getProvinceName() + " key: " + key);
                index++;
                importProvinces();
            }

            @Override
            public void onError(String message) {
                snackbar = Snackbar.make(fileSpinner, message,
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
        provinces = new ArrayList<ProvinceDTO>();

        BufferedReader brReadMe = new BufferedReader(new InputStreamReader(
                new FileInputStream(file), "UTF-8"));
        String strLine = brReadMe.readLine();
        while (strLine != null) {
            ProvinceDTO dto = null;
            try {
                dto = parseLine(strLine);
                provinces.add(dto);
                Log.e(TAG, "####### User added to list from import file: " + dto.getProvinceName());
                strLine = brReadMe.readLine();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        brReadMe.close();

        showDialog();
    }

    public static ProvinceDTO parseLine(String line) throws Exception {
        Pattern patt = Pattern.compile(";");

        if (line.indexOf(",") > -1) {
            patt = Pattern.compile(",");
        }
        String[] result = patt.split(line);
        ProvinceDTO dto = new ProvinceDTO();
        try {
            if (result[0] != null) {
                dto.setProvinceName(result[0]);
            }
        } catch (Exception e) {
            // ignore
        }


        Log.e(TAG,
                "Found province: " + dto.getProvinceName() + " to import into MPS");
        return dto;
    }

    SharedPreferences sp;



    FirebaseRecyclerAdapter<ProvinceDTO, ProvinceViewHolder> adapter;

    @Override
    public void onStart() {
        super.onStart();
        Log.i(TAG, "+++++++++++++++++++ onStart: FirebaseAuth.getInstance(");

        final DatabaseReference provRef = db.getReference(DataUtil.MONITOR_DB)
                .child(DataUtil.PROVINCES);

        Query query = provRef.orderByChild("provinceName");
        adapter =
                new FirebaseRecyclerAdapter<ProvinceDTO, ProvinceViewHolder>(
                        ProvinceDTO.class,
                        R.layout.place_item,
                        ProvinceViewHolder.class,
                        query
                ) {
                    @Override
                    protected void populateViewHolder(ProvinceViewHolder h, final ProvinceDTO model, int position) {
                        Log.d(TAG, "####### populateViewHolder: " + model.getProvinceName());
                        h.name.setText(model.getProvinceName());
                        h.addMuni.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                addMunicipalities(model);
                            }
                        });
                        h.name.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                addMunicipalities(model);
                            }
                        });
                        txtCount.setText("" + adapter.getItemCount());
                        h.count.setText("" + (position + 1));
                    }
                };

        recyclerView.setAdapter(adapter);

    }

    private void addMunicipalities(ProvinceDTO p) {
        Log.d(TAG, "addMunicipalities requested: " + p.getProvinceName());
        Intent m = new Intent(getApplicationContext(), MuniImportActivity.class);
        m.putExtra("province", p);
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

    public static class ProvinceViewHolder extends RecyclerView.ViewHolder {
        protected TextView name, label, count;
        protected ImageView addMuni;


        public ProvinceViewHolder(View itemView) {
            super(itemView);
            name = (TextView) itemView.findViewById(R.id.name);
            label = (TextView) itemView.findViewById(R.id.label);
            addMuni = (ImageView) itemView.findViewById(R.id.addIcon);
            count = (TextView) itemView.findViewById(R.id.count);
            label.setText("Municipalities");
        }

    }

    class Mtask extends AsyncTask<Void, Void, Integer> {

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
                snackbar = Snackbar.make(btnImport, "No import files found", Snackbar.LENGTH_INDEFINITE);
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

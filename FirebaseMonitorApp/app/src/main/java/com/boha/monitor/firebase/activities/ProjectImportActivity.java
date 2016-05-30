package com.boha.monitor.firebase.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
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
import com.boha.monitor.firebase.dto.CityDTO;
import com.boha.monitor.firebase.dto.MonitorCompanyDTO;
import com.boha.monitor.firebase.dto.ProjectDTO;
import com.boha.monitor.firebase.util.DataUtil;
import com.boha.monitor.firebase.util.ImportUtil;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.squareup.picasso.Picasso;

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

public class ProjectImportActivity extends AppCompatActivity {

    static final String TAG = ProjectImportActivity.class.getSimpleName();

    private FirebaseDatabase db;
    private FirebaseAnalytics analytics;
    private Snackbar snackbar;
    private TextView txtTitle, txtCount;
    private Spinner fileSpinner;
    private Button btnImport;
    private ImageView image, camera;
    private RecyclerView recyclerView;
    private List<ProjectDTO> projects = new ArrayList<>();
    private List<File> files = new ArrayList<File>();
    private MonitorCompanyDTO company;
    private CityDTO city;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.import_users);
//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);

        city = (CityDTO) getIntent().getSerializableExtra("city");
        company = (MonitorCompanyDTO) getIntent().getSerializableExtra("company");

        db = FirebaseDatabase.getInstance();
        analytics = FirebaseAnalytics.getInstance(getApplicationContext());
        setFields();

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
        LinearLayoutManager llm = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(llm);
        recyclerView.setHasFixedSize(true);

        txtTitle.setText(city.getCityName() + " Projects");

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
        sb.append("Do you want to import these projects?\n\n");
        for (ProjectDTO u : projects) {
            sb.append(u.getProjectName()).append("\n");
        }
        dg.setTitle(city.getMunicipalityName())
                .setMessage(sb.toString())
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        index = 0;
                        importProjects();
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

    private void importProjects() {
        if (index < projects.size()) {
            importOneProject(projects.get(index));
            return;
        }
        Log.i(TAG, "importProjects: " + projects.size());
        snackbar = Snackbar.make(fileSpinner, "" + projects.size() + " projects have been created on MPS",
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

    private void importOneProject(final ProjectDTO m) {
        m.setCityID(city.getCityID());
        m.setCityName(city.getCityName());
        m.setMunicipalityName(city.getMunicipalityName());
        m.setCompanyID(company.getCompanyID());
        m.setMunicipalityName(city.getMunicipalityName());

        Log.w(TAG, "importOneProject: " + m.getProjectName());
        DataUtil.addProject(m, new DataUtil.DataAddedListener() {
            @Override
            public void onResponse(String key) {
                Log.e(TAG, "++++++++++++++++ onResponse: Project created on MPS:"
                        + m.getCityName() + " key: " + key);
                index++;
                importProjects();
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
        ArrayAdapter a = new ArrayAdapter(getApplicationContext(), R.layout.simple_list_item, list);
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
        projects = new ArrayList<>();
        BufferedReader brReadMe = new BufferedReader(new InputStreamReader(
                new FileInputStream(file), "UTF-8"));
        String strLine = brReadMe.readLine();
        while (strLine != null) {
            ProjectDTO dto = null;
            try {
                dto = parseLine(strLine);
                if (dto.getCityName().equalsIgnoreCase(city.getCityName())) {
                    projects.add(dto);
                    Log.i(TAG, "####### project added to list from import file: " + dto.getCityName());
                } else {
                    Log.d(TAG, "parseFile: ignored: " + dto.getCityName());
                }
                strLine = brReadMe.readLine();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        brReadMe.close();

        Log.i(TAG, "parseFile: projects for import: " + projects.size());
        if (!projects.isEmpty()) {
            showDialog();
        } else {
            Snackbar.make(btnImport, "No projects found for import", Snackbar.LENGTH_SHORT).show();
        }
    }

    public static ProjectDTO parseLine(String line) throws Exception {
        Pattern patt = Pattern.compile(";");

        if (line.indexOf(",") > -1) {
            patt = Pattern.compile(",");
        }
        String[] result = patt.split(line);
        ProjectDTO dto = new ProjectDTO();
        try {
            if (result[0] != null) {
                String m = result[0];
                m = m.replaceAll("\"", "");
                dto.setProjectName(m);
            }
            if (result[1] != null) {
                String m = result[1];
                m = m.replaceAll("\"", "");
                dto.setCityName(m);
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            //ignore
        }
        Log.d(TAG,
                "Found city: " + dto.getCityName() + " to attempt import into MPS");
        return dto;
    }

    SharedPreferences sp;


    FirebaseRecyclerAdapter<ProjectDTO, ProjectViewHolder> adapter;

    @Override
    public void onStart() {
        super.onStart();
        Log.i(TAG, "+++++++++++++++++++ onStart: FirebaseAuth.getInstance(");
        if (city == null) {
            return;
        }
        final DatabaseReference userRef = db.getReference(DataUtil.MONITOR_DB)
                .child(DataUtil.COMPANIES)
                .child(company.getCompanyID())
                .child(DataUtil.PROJECTS);

        Query q = userRef.orderByChild("cityID").equalTo(city.getCityID());

        adapter =
                new FirebaseRecyclerAdapter<ProjectDTO, ProjectViewHolder>(
                        ProjectDTO.class,
                        R.layout.place_item,
                        ProjectViewHolder.class,
                        q
                ) {
                    @Override
                    protected void populateViewHolder(ProjectViewHolder h, final ProjectDTO model, int position) {
                        Log.d(TAG, "####### populateViewHolder: " + model.getProjectName());
                        h.name.setText(model.getProjectName());
                        h.addMonitors.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                addMonitors(model);
                            }
                        });
                        h.name.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                addMonitors(model);
                            }
                        });
                        h.camera.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent m = new Intent(getApplicationContext(), PictureActivity.class);
                                m.putExtra("project", model);
                                startActivity(m);
                            }
                        });
                        txtCount.setText("" + adapter.getItemCount());
                        if (model.getPhotoList() == null
                                || model.getPhotoList().isEmpty()) {
                            h.image.setVisibility(View.GONE);
                            return;
                        }
                        h.image.setVisibility(View.VISIBLE);
                        String url = model.getPhotoList().get(
                                model.getPhotoList().size() - 1).getUrl();
                        Picasso.with(getApplicationContext())
                                .load(Uri.parse(url))
                                .into(h.image);

                    }
                };

        recyclerView.setAdapter(adapter);

    }

    final static int GET_CAMERA = 667;

    private void addMonitors(ProjectDTO c) {
        Log.d(TAG, "addMonitors requested: " + c.getProjectName());
        Intent m = new Intent(getApplicationContext(), ProjectActivity.class);
        m.putExtra("project", c);
        if (company != null) {
            m.putExtra("company", company);
        }
        //startActivity(m);

    }

    static final Locale loc = Locale.getDefault();
    static final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", loc);

    @Override
    public void onStop() {
        super.onStop();
        Log.w(TAG, "---------------- onStop: ");
    }

    private void signIn() {
        Log.w(TAG, "signIn: ================ Firebase signin");
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        String email = "aubrey@mlab.co.za";
        String password = "kktiger3";

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.i(TAG, "####### signIn: onComplete: " + task.isSuccessful());

                        if (task.isSuccessful()) {
                            FirebaseUser user = task.getResult().getUser();
                            Log.i(TAG, "####### onComplete: we cool, name: "
                                    + user.getDisplayName() + " email: " + user.getEmail()
                                    + " uid: " + user.getUid() + " \ntoken: " + user.getToken(true));
                        } else {
                            Log.e(TAG, "------------ sign in FAILED");
                        }
                    }
                });
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

    public static class ProjectViewHolder extends RecyclerView.ViewHolder {
        protected TextView name, label;
        protected ImageView addMonitors, camera, image;


        public ProjectViewHolder(View itemView) {
            super(itemView);
            name = (TextView) itemView.findViewById(R.id.name);
            addMonitors = (ImageView) itemView.findViewById(R.id.addIcon);
            label = (TextView) itemView.findViewById(R.id.label);
            label.setText("Add Monitors");
            camera = (ImageView) itemView.findViewById(R.id.camera);
            image = (ImageView) itemView.findViewById(R.id.image);
            camera.setVisibility(View.VISIBLE);

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

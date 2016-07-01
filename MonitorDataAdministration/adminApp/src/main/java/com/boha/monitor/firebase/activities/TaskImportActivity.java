package com.boha.monitor.firebase.activities;

import android.content.DialogInterface;
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
import com.boha.monitor.firebase.data.ProjectDTO;
import com.boha.monitor.firebase.data.ProjectTaskDTO;
import com.boha.monitor.firebase.util.DataUtil;
import com.boha.monitor.firebase.util.ImportUtil;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

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

public class TaskImportActivity extends AppCompatActivity {

    static final String TAG = TaskImportActivity.class.getSimpleName();

    private FirebaseDatabase db;
    private FirebaseAnalytics analytics;
    private Snackbar snackbar;
    private TextView txtTitle, txtCount;
    private Spinner fileSpinner;
    private Button btnImport;
    private RecyclerView recyclerView;
    private List<ProjectTaskDTO> projectTasks = new ArrayList<>();
    private List<File> files = new ArrayList<File>();
    private ProjectDTO project;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.import_users);
//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);

        project = (ProjectDTO) getIntent().getSerializableExtra("project");

        db = FirebaseDatabase.getInstance();
        analytics = FirebaseAnalytics.getInstance(getApplicationContext());
        setFields();

        sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        setTitle("Project Task Administration");
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

        txtTitle.setText(project.getProjectName() + " Tasks");

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
        sb.append("Do you want to import these projectTasks?\n\n");
        for (ProjectTaskDTO u : projectTasks) {
            sb.append(u.getTaskName()).append("\n");
        }
        dg.setTitle(project.getProjectName())
                .setMessage(sb.toString())
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        index = 0;
                        importTasks();
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

    private void importTasks() {
        if (index < projectTasks.size()) {
            importOneProjectTask(projectTasks.get(index));
            return;
        }
        Log.i(TAG, "importTasks: " + projectTasks.size());
        snackbar = Snackbar.make(fileSpinner, "" + projectTasks.size() + " projectTasks have been created on MPS",
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

    private void importOneProjectTask(final ProjectTaskDTO m) {

        Log.w(TAG, "importOneProjectTask: " + m.getProjectName() + " " + m.getTaskName());
        DataUtil.addProjectTask(m, new DataUtil.DataAddedListener() {
            @Override
            public void onResponse(String key) {
                Log.e(TAG, "++++++++++++++++ onResponse: ProjectTask created on MPS:"
                        + m.getTaskName() + " key: " + key);
                index++;
                importTasks();
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
        ArrayAdapter a = new ArrayAdapter(getApplicationContext(),
                R.layout.simple_list_item_small, list);
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
        projectTasks = new ArrayList<>();
        BufferedReader brReadMe = new BufferedReader(new InputStreamReader(
                new FileInputStream(file), "UTF-8"));
        projectTasks = new ArrayList<>();
        String strLine = brReadMe.readLine();
        while (strLine != null) {
            ProjectTaskDTO dto;
            try {
                dto = parseLine(strLine);
                projectTasks.add(dto);
                strLine = brReadMe.readLine();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        brReadMe.close();

        Log.i(TAG, "parseFile: projectTasks for import: " + projectTasks.size());
        if (!projectTasks.isEmpty()) {
            showDialog();
        } else {
            Snackbar.make(btnImport, "No projectTasks found for import", Snackbar.LENGTH_SHORT).show();
        }
    }

    public  ProjectTaskDTO parseLine(String line) throws Exception {

        ProjectTaskDTO m = new ProjectTaskDTO();
        m.setCompanyID(project.getCompanyID());
        m.setProjectName(project.getProjectName());
        m.setDateRegistered(new Date().getTime());
        m.setProjectID(project.getProjectID());
        m.setTaskName(line);
        m.setPhotoCount(0);
        m.setStatusCount(0);
        Log.d(TAG,
                "Found projectTask: " + line + " to attempt import into MPS");
        return m;
    }

    SharedPreferences sp;


    FirebaseRecyclerAdapter<ProjectTaskDTO, ProjectTaskViewHolder> adapter;

    @Override
    public void onStart() {
        super.onStart();
        Log.i(TAG, "+++++++++++++++++++ onStart: FirebaseAuth.getInstance(");
        if (project == null) {
            return;
        }
        final DatabaseReference userRef = db.getReference(DataUtil.MONITOR_DB)
                .child(DataUtil.COMPANIES)
                .child(project.getCompanyID())
                .child(DataUtil.PROJECTS)
                .child(project.getProjectID())
                .child(DataUtil.TASKS);


        adapter =
                new FirebaseRecyclerAdapter<ProjectTaskDTO, ProjectTaskViewHolder>(
                        ProjectTaskDTO.class,
                        R.layout.task_item,
                        ProjectTaskViewHolder.class,
                        userRef
                ) {
                    @Override
                    protected void populateViewHolder(ProjectTaskViewHolder h, final ProjectTaskDTO model, int position) {
                        Log.d(TAG, "####### populateViewHolder: " + model.getProjectName() + " " + model.getTaskName());
                        h.name.setText(model.getTaskName());
                        h.delete.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                            }
                        });
                        h.edit.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                            }
                        });

                    }
                };

        recyclerView.setAdapter(adapter);

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

    public static class ProjectTaskViewHolder extends RecyclerView.ViewHolder {
        protected TextView name;
        protected ImageView delete, edit;


        public ProjectTaskViewHolder(View itemView) {
            super(itemView);
            name = (TextView) itemView.findViewById(R.id.taskName);
            delete = (ImageView) itemView.findViewById(R.id.delete);
            edit = (ImageView) itemView.findViewById(R.id.edit);


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

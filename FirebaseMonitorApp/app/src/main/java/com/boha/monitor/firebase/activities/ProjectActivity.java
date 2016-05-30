package com.boha.monitor.firebase.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.boha.monitor.firebase.R;
import com.boha.monitor.firebase.dto.CityDTO;
import com.boha.monitor.firebase.dto.MonitorCompanyDTO;
import com.boha.monitor.firebase.dto.ProjectDTO;
import com.boha.monitor.firebase.util.DataUtil;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ProjectActivity extends AppCompatActivity {

    static final String TAG = ProjectActivity.class.getSimpleName();
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseAuth mAuth;
    private FirebaseDatabase db;
    private FirebaseAnalytics analytics;
    private Snackbar snackbar;
    private EditText companyName;
    private ImageView tick;
    private TextView count;
    private RecyclerView recyclerView;
    private MonitorCompanyDTO company;
    private CityDTO city;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_proj);
//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);

        city = (CityDTO) getIntent().getSerializableExtra("city");
        company = (MonitorCompanyDTO) getIntent().getSerializableExtra("company");
        TextView title = (TextView) findViewById(R.id.title);
        title.setText(company.getCompanyName());

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance();
        analytics = FirebaseAnalytics.getInstance(getApplicationContext());
        //
        companyName = (EditText) findViewById(R.id.companyName);
        count = (TextView) findViewById(R.id.count);
        recyclerView = (RecyclerView) findViewById(R.id.recycler);
        LinearLayoutManager llm = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(llm);
        recyclerView.setHasFixedSize(true);
        tick = (ImageView) findViewById(R.id.addCompany);

        //
        tick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addProject();
            }
        });

        sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addProject();
            }
        });
    }

    private void addProject() {
        final ProjectDTO c = new ProjectDTO();
        c.setProjectName(companyName.getText().toString());
        c.setCompanyID(company.getCompanyID());
        c.setCityName(city.getCityName());
        c.setCityID(city.getCityID());
        c.setMunicipalityName(city.getMunicipalityName());
        c.setLocationConfirmed(false);
        c.setDateRegistered(new Date().getTime());

        DataUtil.addProject(c, new DataUtil.DataAddedListener() {
            @Override
            public void onResponse(String key) {
                Log.i(TAG, "onResponse: " + key);
                Bundle params = new Bundle();
                params.putString("projectName", c.getProjectName());
                analytics.logEvent("Project registered on MPS", params);
            }

            @Override
            public void onError(String message) {
                Log.e(TAG, "onError: " + message);
            }
        });
    }

    SharedPreferences sp;

    FirebaseRecyclerAdapter<ProjectDTO, ProjectViewHolder> adapter;

    @Override
    public void onStart() {
        super.onStart();
        Log.i(TAG, "+++++++++++++++++++ onStart: FirebaseAuth.getInstance(");
        final DatabaseReference companiesRef = db.getReference(DataUtil.MONITOR_DB)
                .child(DataUtil.COMPANIES)
                .child(company.getCompanyID())
                .child(DataUtil.PROJECTS);

        adapter =
                new FirebaseRecyclerAdapter<ProjectDTO, ProjectViewHolder>(
                        ProjectDTO.class,
                        R.layout.project_item,
                        ProjectViewHolder.class,
                        companiesRef
                ) {
                    @Override
                    protected void populateViewHolder(ProjectViewHolder h, ProjectDTO model, int position) {
                        Log.d(TAG, "####### populateViewHolder: " + model.getProjectName() + " " + model.getProjectID());
                        count.setText("" + adapter.getItemCount());
                        h.project.setText(model.getProjectName());
                        h.date.setText(sdf.format(new Date(model.getDateRegistered())));
                        h.addStaff.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                addStaff();
                            }
                        });
                        h.addMonitors.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                addMonitors();
                            }
                        });
                    }
                };

        recyclerView.setAdapter(adapter);

    }

    private void addStaff() {
        Log.d(TAG, "addStaff: ");
    }

    private void addMonitors() {
        Log.d(TAG, "addMonitors: ");
    }

    static final Locale loc = Locale.getDefault();
    static final SimpleDateFormat sdf = new SimpleDateFormat("EEEE, dd/MMMM/yyyy HH:mm", loc);

    @Override
    public void onStop() {
        super.onStop();
        Log.w(TAG, "---------------- onStop: ");
        mAuth.removeAuthStateListener(mAuthListener);

    }



    private void setFields() {

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

//        if (id == R.id.action_settings) {
//            return true;
//        }

        return super.onOptionsItemSelected(item);
    }

    public static class ProjectViewHolder extends RecyclerView.ViewHolder {
        protected TextView project, date;
        protected ImageView addStaff, addMonitors;


        public ProjectViewHolder(View itemView) {
            super(itemView);
            project = (TextView) itemView.findViewById(R.id.name);
            date = (TextView) itemView.findViewById(R.id.date);
            addStaff = (ImageView) itemView.findViewById(R.id.addStaff);
            addMonitors = (ImageView) itemView.findViewById(R.id.addUsers);
        }

    }
}

package com.boha.monitor.firebase.activities;

import android.content.Intent;
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
import com.boha.monitor.library.data.MonitorCompanyDTO;
import com.boha.monitor.library.util.DataUtil;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CompanyActivity extends AppCompatActivity {

    static final String TAG = CompanyActivity.class.getSimpleName();

    private FirebaseDatabase db;
    private FirebaseAnalytics analytics;
    private Snackbar snackbar;
    private EditText companyName;
    private ImageView tick;
    private TextView count;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = FirebaseDatabase.getInstance();
        analytics = FirebaseAnalytics.getInstance(getApplicationContext());
        companyName = (EditText) findViewById(R.id.companyName);
        count = (TextView) findViewById(R.id.count);
        recyclerView = (RecyclerView) findViewById(R.id.recycler);
        LinearLayoutManager llm = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(llm);
        recyclerView.setHasFixedSize(true);
        tick = (ImageView) findViewById(R.id.addCompany);
        tick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addCompany();
            }
        });

        sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent m = new Intent(getApplicationContext(), UserActivity.class);
                startActivity(m);
            }
        });

    }


    private void addCompany() {
        final MonitorCompanyDTO c = new MonitorCompanyDTO();
        c.setCompanyName(companyName.getText().toString());
        c.setDateRegistered(new Date().getTime());

        DataUtil.addCompany(c, new DataUtil.DataAddedListener() {
            @Override
            public void onResponse(String key) {
                Log.i(TAG, "onResponse: " + key);
                Bundle params = new Bundle();
                params.putString("companyName", c.getCompanyName());
                analytics.logEvent("Company registered on Monitor", params);
            }

            @Override
            public void onError(String message) {
                Log.e(TAG, "onError: " + message);
            }
        });
    }

    SharedPreferences sp;


    FirebaseRecyclerAdapter<MonitorCompanyDTO, CompanyViewHolder> adapter;

    @Override
    public void onStart() {
        super.onStart();
        Log.i(TAG, "+++++++++++++++++++ onStart: FirebaseAuth.getInstance(");
        final DatabaseReference companiesRef = db.getReference(DataUtil.MONITOR_DB)
                .child(DataUtil.COMPANIES);

        adapter =
                new FirebaseRecyclerAdapter<MonitorCompanyDTO, CompanyViewHolder>(
                        MonitorCompanyDTO.class,
                        R.layout.company_item,
                        CompanyViewHolder.class,
                        companiesRef
                ) {
                    @Override
                    protected void populateViewHolder(CompanyViewHolder h, final MonitorCompanyDTO model, int position) {
                        Log.d(TAG, "####### populateViewHolder: " + model.getCompanyName() + " " + model.getCompanyID());
                        count.setText("" + adapter.getItemCount());
                        h.company.setText(model.getCompanyName());
                        h.date.setText(sdf.format(new Date(model.getDateRegistered())));

                        h.addUsers.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                goAddUsers(model);
                            }
                        });
                        h.importUsers.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                company = model;
                                importUsers();
                            }
                        });
                        h.importProjects.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                goAddProjects(model);
                            }
                        });
                    }
                };

        recyclerView.setAdapter(adapter);

    }

    private void goAddProjects(MonitorCompanyDTO co) {
        Log.d(TAG, "goAddProjects: ");
        Intent m = new Intent(getApplicationContext(), ProvinceImportActivity.class);
        m.putExtra("company", co);
        startActivity(m);
    }

    private MonitorCompanyDTO company;

    private void importUsers() {
        Intent m = new Intent(getApplicationContext(),UserImportActivity.class);
        m.putExtra("company", company);
        startActivity(m);
    }
    private void goAddUsers(MonitorCompanyDTO co) {

        Log.d(TAG, "goAddUsers: ");
        Intent m = new Intent(getApplicationContext(), UserActivity.class);
        m.putExtra("company", co);
        startActivity(m);
    }

    static final Locale loc = Locale.getDefault();
    static final SimpleDateFormat sdf = new SimpleDateFormat("EEEE, dd/MMMM/yyyy HH:mm", loc);

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
        int id = item.getItemId();
        if (id == R.id.action_help) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public static class CompanyViewHolder extends RecyclerView.ViewHolder {
        protected TextView company, date;
        protected ImageView addUsers, importUsers, importProjects;


        public CompanyViewHolder(View itemView) {
            super(itemView);
            company = (TextView) itemView.findViewById(R.id.name);
            date = (TextView) itemView.findViewById(R.id.date);
            addUsers = (ImageView) itemView.findViewById(R.id.addUsers);
            importProjects = (ImageView) itemView.findViewById(R.id.importProjects);
            importUsers = (ImageView) itemView.findViewById(R.id.importUsers);
        }

    }
}

package com.boha.monitor.firebase.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.boha.monitor.firebase.R;
import com.boha.monitor.firebase.dto.MonitorCompanyDTO;
import com.boha.monitor.firebase.util.DataUtil;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CompanyActivity extends AppCompatActivity {

    static final String TAG = CompanyActivity.class.getSimpleName();
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseAuth mAuth;
    private FirebaseDatabase db;
    private FirebaseAnalytics analytics;
    private Snackbar snackbar;
    private EditText companyName;
    private ImageView tick;
    private TextView count;
    private RecyclerView recyclerView;
    private List<MonitorCompanyDTO> companies;
    private int counter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance();
        analytics = FirebaseAnalytics.getInstance(getApplicationContext());
        companyName = (EditText)findViewById(R.id.companyName);
        count = (TextView) findViewById(R.id.count);
        recyclerView = (RecyclerView) findViewById(R.id.recycler);
        LinearLayoutManager llm = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL,false);
        recyclerView.setLayoutManager(llm);
        recyclerView.setHasFixedSize(true);
        tick = (ImageView)findViewById(R.id.addCompany);
        tick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addCompany();
            }
        });

        sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        checkStatus();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addCompany();
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
                params.putString( "companyName", c.getCompanyName() );
                analytics.logEvent("Company registered on Monitor", params);
            }

            @Override
            public void onError(String message) {
                Log.e(TAG, "onError: " + message );
            }
        });
    }

    SharedPreferences sp;

    private void checkStatus() {
        Log.i(TAG, "checkStatus: ---------- check Firebase user log in");
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    Log.i(TAG, "++++++++++++++ onAuthStateChanged:signed_in:" + user.getUid()
                            + " " + user.getEmail());
                } else {
                    // User is signed out
                    Log.d(TAG, "-----------onAuthStateChanged:signed_out - start sign in");
                    signIn();
                }

            }
        };

        mAuth.addAuthStateListener(mAuthListener);

    }
    FirebaseRecyclerAdapter<MonitorCompanyDTO, CompanyViewHolder> adapter;
    @Override
    public void onStart() {
        super.onStart();
        Log.i(TAG, "+++++++++++++++++++ onStart: FirebaseAuth.getInstance(");
        mAuth = FirebaseAuth.getInstance();
        final DatabaseReference companiesRef = db.getReference(DataUtil.MONITOR_DB)
                .child(DataUtil.COMPANIES);

         adapter  =
                new FirebaseRecyclerAdapter<MonitorCompanyDTO, CompanyViewHolder>(
                        MonitorCompanyDTO.class,
                        R.layout.company_item,
                        CompanyViewHolder.class,
                        companiesRef
                ) {
                    @Override
                    protected void populateViewHolder(CompanyViewHolder h, MonitorCompanyDTO model, int position) {
                        Log.d(TAG, "####### populateViewHolder: " + model.getCompanyName() + " " + model.getCompanyID());
                        count.setText("" + adapter.getItemCount());
                        h.company.setText(model.getCompanyName());
                        h.date.setText(sdf.format(new Date(model.getDateRegistered())));
                        h.addProjects.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                goAddProjects();
                            }
                        });
                        h.addUsers.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                goAddUsers();
                            }
                        });
                    }
                };

        recyclerView.setAdapter(adapter);

    }

    private void goAddProjects() {
        Log.d(TAG, "goAddProjects: ");
    }
    private void goAddUsers() {
        Log.d(TAG, "goAddUsers: ");
    }
    static final Locale loc = Locale.getDefault();
    static final SimpleDateFormat sdf = new SimpleDateFormat("EEEE, dd/MMMM/yyyy HH:mm", loc);

    @Override
    public void onStop() {
        super.onStop();
        Log.w(TAG, "---------------- onStop: ");
        mAuth.removeAuthStateListener(mAuthListener);

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

    private void setFields() {

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    public static class CompanyViewHolder extends RecyclerView.ViewHolder {
        protected TextView company, date;
        protected ImageView addProjects, addUsers;


        public CompanyViewHolder(View itemView) {
            super(itemView);
            company = (TextView) itemView.findViewById(R.id.name);
            date = (TextView) itemView.findViewById(R.id.date);
            addProjects = (ImageView) itemView.findViewById(R.id.addProjects);
            addUsers = (ImageView) itemView.findViewById(R.id.addUsers);
        }

    }
}

package com.boha.monitor.firebase.activities;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.boha.monitor.firebase.R;
import com.boha.monitor.firebase.data.MonitorDTO;
import com.boha.monitor.firebase.data.ProjectDTO;
import com.boha.monitor.firebase.data.UserDTO;
import com.boha.monitor.firebase.util.Constants;
import com.boha.monitor.firebase.util.DataUtil;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

public class MonitorAssignActivity extends AppCompatActivity {

    static final String TAG = MonitorAssignActivity.class.getSimpleName();
    private UserDTO user;
    FirebaseDatabase db;
    FirebaseRecyclerAdapter<ProjectDTO, ProjectViewHolder> adapter;
    RecyclerView recyclerView;
    TextView name, assignCount, label;
    Button btnSend;
    Snackbar bar;
    private InterstitialAd mInterstitialAd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monitor_assign);
//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);

        db = FirebaseDatabase.getInstance();
        recyclerView = (RecyclerView) findViewById(R.id.recycler);
        LinearLayoutManager lm = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(lm);
        recyclerView.setHasFixedSize(true);
        name = (TextView) findViewById(R.id.name);
        label = (TextView) findViewById(R.id.label);
        label.setText("MPS Projects");
        assignCount = (TextView) findViewById(R.id.count);

        user = (UserDTO) getIntent().getSerializableExtra("user");
        name.setText(user.getFullName());

        setTitle("Assign Monitor Projects");
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
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

    boolean showAd = true;
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
    private boolean monitorExists(ProjectDTO p) {
        for (MonitorDTO m : p.getMonitorList()) {
            if (m.getUserID().equalsIgnoreCase(user.getUserID())) {
                return true;
            }
        }
        return false;
    }

    private void addMonitor(MonitorDTO m, String projectName) {
        DataUtil.addMonitor(m, projectName, new DataUtil.DataAddedListener() {
            @Override
            public void onResponse(String key) {
                Log.e(TAG, "onResponse: monitor added response: " + key);
                bar = Snackbar.make(name, "Monitor assigned to project",
                        Snackbar.LENGTH_INDEFINITE);
                bar.setActionTextColor(ContextCompat.getColor(getApplicationContext(), R.color.green_200));
                bar.setAction("Cool", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        bar.dismiss();
                    }
                });
                bar.show();
            }

            @Override
            public void onError(String message) {
                bar = Snackbar.make(name, message,
                        Snackbar.LENGTH_INDEFINITE);
                bar.setActionTextColor(ContextCompat.getColor(getApplicationContext(), R.color.red_500));
                bar.setAction("Not Good", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        bar.dismiss();
                    }
                });
                bar.show();
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.i(TAG, "+++++++++++++++++++ onStart: DatabaseReference");
        if (user == null) {
            return;
        }
        final DatabaseReference projectsRef = db.getReference(DataUtil.MONITOR_DB)
                .child(DataUtil.COMPANIES)
                .child(user.getCompanyID())
                .child(DataUtil.PROJECTS);

        Query q = projectsRef.orderByChild("projectName");
        Log.d(TAG, "onStart: " + projectsRef.toString());
        adapter =
                new FirebaseRecyclerAdapter<ProjectDTO, ProjectViewHolder>(
                        ProjectDTO.class,
                        R.layout.monitor_assign_item,
                        ProjectViewHolder.class,
                        q
                ) {
                    @Override
                    protected void populateViewHolder(final ProjectViewHolder h, final ProjectDTO model, int position) {
                        Log.d(TAG, "####### populateViewHolder: " + model.getProjectName());
                        h.name.setText(model.getProjectName());

                        if (monitorExists(model)) {
                            h.name.setTextColor(ContextCompat.getColor(
                                    getApplicationContext(), R.color.green_700));
                            h.done.setVisibility(View.VISIBLE);
                            h.count.setBackground(ContextCompat.getDrawable(
                                    getApplicationContext(), R.drawable.xgreen_oval_small));
                        } else {
                            h.name.setTextColor(ContextCompat.getColor(
                                    getApplicationContext(), R.color.black));
                            h.count.setBackground(ContextCompat.getDrawable(
                                    getApplicationContext(), R.drawable.xgrey_oval_small));
                            h.done.setVisibility(View.GONE);
                        }
                        h.card.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (!monitorExists(model)) {
                                    MonitorDTO m = new MonitorDTO();
                                    m.setCompanyID(user.getCompanyID());
                                    m.setProjectID(model.getProjectID());
                                    m.setFullName(user.getFullName());
                                    m.setActiveFlag(1);
                                    m.setUserID(user.getUserID());
                                    confirm(m,model.getProjectName());
                                }
                            }
                        });
                        int count = 0;
                        for (int i = 0; i < adapter.getItemCount(); i++) {
                            ProjectDTO p = adapter.getItem(i);
                            if (monitorExists(p)) {
                                count++;
                            }
                        }
                        assignCount.setText("" + count);
                        h.count.setText("" + (position + 1));

                    }
                };

        recyclerView.setAdapter(adapter);

    }

    private void confirm(final MonitorDTO m, final String projectName) {
        AlertDialog.Builder d = new AlertDialog.Builder(this);
        d.setTitle("Confirm Assignment")
                .setMessage("Do you want to assign " + projectName + " to " + user.getFullName() + "?")
                .setPositiveButton("yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        addMonitor(m, projectName);
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .show();
    }

    public static class ProjectViewHolder extends RecyclerView.ViewHolder {
        protected TextView name, count;
        protected ImageView done;
        protected View card;


        public ProjectViewHolder(View itemView) {
            super(itemView);
            count = (TextView) itemView.findViewById(R.id.count);
            name = (TextView) itemView.findViewById(R.id.name);
            card = itemView.findViewById(R.id.card);
            done = (ImageView) itemView.findViewById(R.id.done);

        }

    }

}

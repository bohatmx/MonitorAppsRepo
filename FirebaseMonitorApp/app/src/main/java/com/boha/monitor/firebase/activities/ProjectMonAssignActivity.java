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
import android.widget.ImageView;
import android.widget.TextView;

import com.boha.monitor.firebase.R;
import com.boha.monitor.library.data.MonitorDTO;
import com.boha.monitor.library.data.ProjectDTO;
import com.boha.monitor.library.data.UserDTO;
import com.boha.monitor.library.data.UserProjectDTO;
import com.boha.monitor.library.util.DataUtil;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

public class ProjectMonAssignActivity extends AppCompatActivity {

    static final String TAG = ProjectMonAssignActivity.class.getSimpleName();
    private ProjectDTO project;
    FirebaseDatabase db;
    FirebaseRecyclerAdapter<UserDTO, UserViewHolder> adapter;
    RecyclerView recyclerView;
    TextView name, assignCount, label;
    Snackbar bar;


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
        label = (TextView) findViewById(R.id.label);
        label.setText("MPS Users");
        name = (TextView) findViewById(R.id.name);
        assignCount = (TextView) findViewById(R.id.count);

        project = (ProjectDTO) getIntent().getSerializableExtra("project");
        name.setText(project.getProjectName());

        setTitle("Assign Project Monitors");
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }


    private boolean monitorExists(UserDTO user) {
        if (user.getUserProjectList() == null
                || user.getUserProjectList().isEmpty()) {
            return false;
        }
        for (UserProjectDTO m : user.getUserProjectList()) {
            if (m.getProjectID().equalsIgnoreCase(project.getProjectID())) {
                return true;
            }
        }
        return false;
    }

    private void addMonitor(final MonitorDTO m, String projectName) {
        DataUtil.addMonitor(m, projectName, new DataUtil.DataAddedListener() {
            @Override
            public void onResponse(String key) {
                Log.e(TAG, "onResponse: monitor added response: " + key);
                project.getMonitorList().add(m);
                assignCount.setText("" + project.getMonitorList().size());
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
        if (project == null) {
            return;
        }
        final DatabaseReference projectsRef = db.getReference(DataUtil.MONITOR_DB)
                .child(DataUtil.USERS);

        Query query = projectsRef.orderByChild("companyID").equalTo(project.getCompanyID());
        Log.d(TAG, "onStart: " + projectsRef.toString());
        adapter =
                new FirebaseRecyclerAdapter<UserDTO, UserViewHolder>(
                        UserDTO.class,
                        R.layout.monitor_assign_item,
                        UserViewHolder.class,
                        query
                ) {
                    @Override
                    protected void populateViewHolder(final UserViewHolder h, final UserDTO model, int position) {
                        Log.d(TAG, "####### populateViewHolder: " + model.getFullName());
                        h.name.setText(model.getFullName());

                        if (monitorExists(model)) {
                            h.name.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.green_700));
                            h.done.setVisibility(View.VISIBLE);
                            h.count.setBackground(ContextCompat.getDrawable(
                                    getApplicationContext(),R.drawable.xgreen_oval_small));
                        } else {
                            h.name.setTextColor(ContextCompat.getColor(
                                    getApplicationContext(), R.color.black));
                            h.count.setBackground(ContextCompat.getDrawable(
                                    getApplicationContext(),R.drawable.xgrey_oval_small));
                            h.done.setVisibility(View.GONE);
                        }
                        h.count.setText("" + (position + 1));
                        h.card.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (!monitorExists(model)) {
                                    MonitorDTO m = new MonitorDTO();
                                    m.setCompanyID(project.getCompanyID());
                                    m.setProjectID(project.getProjectID());
                                    m.setFullName(model.getFullName());
                                    m.setActiveFlag(1);
                                    m.setUserID(model.getUserID());
                                    confirm(m,project.getProjectName());
                                }
                            }
                        });
                        int count = 0;
                        for (int i = 0; i < adapter.getItemCount(); i++) {
                            UserDTO p = adapter.getItem(i);
                            if (monitorExists(p)) {
                                count++;
                            }
                        }
                        assignCount.setText("" + count);

                    }
                };

        recyclerView.setAdapter(adapter);

    }

    private void confirm(final MonitorDTO m, final String projectName) {
        AlertDialog.Builder d = new AlertDialog.Builder(this);
        d.setTitle("Confirm Assignment")
                .setMessage("Do you want to assign " + projectName + " to " + m.getFullName() + "?")
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

    public static class UserViewHolder extends RecyclerView.ViewHolder {
        protected TextView name, count;
        protected ImageView done;
        protected View card;


        public UserViewHolder(View itemView) {
            super(itemView);
            name = (TextView) itemView.findViewById(R.id.name);
            count = (TextView) itemView.findViewById(R.id.count);
            card = itemView.findViewById(R.id.card);
            done = (ImageView) itemView.findViewById(R.id.done);

        }

    }

}

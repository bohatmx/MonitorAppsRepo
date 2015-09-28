package com.boha.monitor.library.activities;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.boha.monitor.library.dto.ProjectDTO;
import com.boha.monitor.library.dto.TaskTypeDTO;
import com.boha.monitor.library.fragments.TaskTypeListFragment;
import com.boha.monitor.library.services.RequestSyncService;
import com.boha.monitor.library.util.ThemeChooser;
import com.boha.monitor.library.util.Util;
import com.boha.platform.library.R;

public class TaskTypeListActivity extends AppCompatActivity implements TaskTypeListFragment.TaskTypeListener{

    ProjectDTO projectDTO;
    TaskTypeListFragment taskTypeListFragment;
    int themeDarkColor, type;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.w(LOG, "## onCreate");
        ThemeChooser.setTheme(this);
        setContentView(R.layout.activity_task_type_list);

        themeDarkColor = getIntent().getIntExtra("darkColor", R.color.teal_900);
        projectDTO = (ProjectDTO) getIntent().getSerializableExtra("project");
        type = getIntent().getIntExtra("type",0);
        if (savedInstanceState != null) {
            projectDTO = (ProjectDTO)savedInstanceState.getSerializable("project");
        }
        if (projectDTO == null) {
            Log.e(LOG,"Project is NULL");
            finish();
            return;
        }
         taskTypeListFragment = (TaskTypeListFragment)
                getSupportFragmentManager().findFragmentById(R.id.fragment);
//        taskTypeListFragment.setProject(projectDTO, type);
        taskTypeListFragment.setDarkColor(themeDarkColor);

        Util.setCustomActionBar(getApplicationContext(), getSupportActionBar(),
                projectDTO.getProjectName(), projectDTO.getCityName(),
                ContextCompat.getDrawable(getApplicationContext(), R.drawable.glasses48));    }

    @Override
    public void onSaveInstanceState(Bundle b) {
        Log.w(LOG,"## onSaveInstanceState");
        b.putSerializable("project",projectDTO);
        b.putInt("themeDarkColor",themeDarkColor);
        super.onSaveInstanceState(b);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_task_type_list, menu);
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

    @Override
    public void onTaskTypeClicked(TaskTypeDTO taskType) {
//        Intent w = new Intent(this,ProjectTaskListActivity.class);
//        w.putExtra("project",projectDTO);
//        w.putExtra("taskType",taskType);
//        w.putExtra("type",type);
//        w.putExtra("darkColor", themeDarkColor);
//
//        startActivity(w);
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.i(LOG, "## onStart Bind to RequestSyncService");
        Intent intent = new Intent(this, RequestSyncService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

    }
    @Override
    public void onStop() {
        super.onStop();
        Log.e(LOG, "## onStop unBind from RequestSyncService");
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }

    }
    boolean mBound;
    RequestSyncService mService;


    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            Log.w(LOG, "## RequestSyncService ServiceConnection onServiceConnected");
            RequestSyncService.LocalBinder binder = (RequestSyncService.LocalBinder) service;
            mService = binder.getService();
            mBound = true;
            mService.startSyncCachedRequests(new RequestSyncService.RequestSyncListener() {
                @Override
                public void onTasksSynced(int goodResponses, int badResponses) {
                    Log.e(LOG,"### onTasksSynced, goodResponses: "
                            + goodResponses + " badResponses: " + badResponses);
                }

                @Override
                public void onError(String message) {
                    Log.e(LOG, message);
                }
            });

        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            Log.w(LOG, "## PhotoUploadService onServiceDisconnected");
            mBound = false;
        }
    };

    static final String LOG = TaskTypeListActivity.class.getSimpleName();
}

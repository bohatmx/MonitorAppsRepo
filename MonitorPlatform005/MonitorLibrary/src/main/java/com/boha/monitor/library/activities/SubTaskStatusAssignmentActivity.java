package com.boha.monitor.library.activities;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.boha.monitor.library.R;
import com.boha.monitor.library.dto.ProjectSiteDTO;
import com.boha.monitor.library.dto.ProjectSiteTaskDTO;
import com.boha.monitor.library.dto.ProjectSiteTaskStatusDTO;
import com.boha.monitor.library.dto.SubTaskStatusDTO;
import com.boha.monitor.library.dto.transfer.ResponseDTO;
import com.boha.monitor.library.fragments.SubTaskStatusAssignmentFragment;
import com.boha.monitor.library.services.RequestSyncService;

import java.util.ArrayList;
import java.util.List;

public class SubTaskStatusAssignmentActivity extends AppCompatActivity implements SubTaskStatusAssignmentFragment.SubTaskStatusAssignmentListener{

    ProjectSiteTaskDTO projectSiteTask;
    ProjectSiteDTO projectSite;
    ProjectSiteTaskStatusDTO projectSiteTaskStatus;
    static final String LOG = SubTaskStatusAssignmentActivity.class.getSimpleName();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.w(LOG, "############# onCreate.................................not called.......");
        setContentView(R.layout.activity_subtask_status);
        subTaskStatusAssignmentFragment = (SubTaskStatusAssignmentFragment)
                getSupportFragmentManager().findFragmentById(R.id.fragment);
        projectSite = (ProjectSiteDTO)getIntent().getSerializableExtra("projectSite");
        projectSiteTask = (ProjectSiteTaskDTO)getIntent().getSerializableExtra("projectSiteTask");
        if (projectSiteTask == null)
            throw new UnsupportedOperationException("### projectSiteTask is NULL from Intent");
        subTaskStatusAssignmentFragment.setProjectSiteTask(projectSite, projectSiteTask);

        setTitle(projectSiteTask.getProjectSiteName());
        getSupportActionBar().setSubtitle(projectSiteTask.getProjectName());
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.e(LOG,"############## onResume");
    }

    SubTaskStatusAssignmentFragment subTaskStatusAssignmentFragment;
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_sub_task_status, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected( MenuItem item) {
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
    protected void onStart() {
        super.onStart();
        Log.w(LOG, "## onStart Bind to RequestSyncService");
        Intent intent = new Intent(this, RequestSyncService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.e(LOG, "## onStop unBind from RequestSyncService");
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }

    }
    @Override
    protected void onDestroy() {
        super.onStop();
        Log.e(LOG, "## onDestroy - nulling everything!");
        mService = null;
        mConnection = null;


    }

    boolean mBound;

    RequestSyncService mService;
    ProjectSiteTaskStatusDTO status;


    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            Log.w(LOG, "## DownloadService: ServiceConnection onServiceConnected");
            RequestSyncService.LocalBinder binder = (RequestSyncService.LocalBinder) service;
            mService = binder.getService();
            mBound = true;
            mService.startSyncCachedRequests(new RequestSyncService.RequestSyncListener() {
                @Override
                public void onTasksSynced(int goodResponses, int badResponses) {
                    Log.i(LOG,"%%% onTasksSynced good: " + goodResponses + " bad: " + badResponses);
                }

                @Override
                public void onError(String message) {

                }
            });

        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            Log.w(LOG, "## RequestSyncService onServiceDisconnected");
            mBound = false;
        }
    };


    List<SubTaskStatusDTO> subTaskStatusList = new ArrayList<>();

    @Override
    public void onSubTaskStatusCompleted( SubTaskStatusDTO subTaskStatus) {
        subTaskStatusList.add(subTaskStatus);
        Log.d(LOG,"### onSubTaskStatusCompleted, added to list: "
                + subTaskStatus.getSubTaskName() + " "
                + subTaskStatus.getTaskStatus().getTaskStatusName());
    }

    @Override
    public void onMainStatusCompleted(ProjectSiteTaskStatusDTO status) {
        this.status = status;
    }

    @Override
    public void onBackPressed() {
        if (status == null) {
            subTaskStatusAssignmentFragment.processMainStatus();
            return;
        }
        if (!subTaskStatusList.isEmpty()) {
            Intent w = new Intent();
            ResponseDTO resp = new ResponseDTO();
            resp.setSubTaskStatusList(subTaskStatusList);
            w.putExtra("response", resp);
            if (status != null) {
                w.putExtra("status", status);
            }
            setResult(RESULT_OK, w);
        } else {
            setResult(RESULT_CANCELED);
        }
        finish();
    }
}

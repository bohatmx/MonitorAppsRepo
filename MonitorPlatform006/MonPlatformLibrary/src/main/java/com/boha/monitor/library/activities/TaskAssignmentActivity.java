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
import com.boha.monitor.library.dialogs.StatusDialog;
import com.boha.monitor.library.dto.CompanyDTO;
import com.boha.monitor.library.dto.ProjectSiteDTO;
import com.boha.monitor.library.dto.ProjectSiteTaskDTO;
import com.boha.monitor.library.dto.ProjectSiteTaskStatusDTO;
import com.boha.monitor.library.dto.transfer.PhotoUploadDTO;
import com.boha.monitor.library.dto.transfer.ResponseDTO;
import com.boha.monitor.library.fragments.SiteTaskAndStatusAssignmentFragment;
import com.boha.monitor.library.services.RequestSyncService;

import java.util.ArrayList;
import java.util.List;

import static com.boha.monitor.library.util.Util.showErrorToast;
import static com.boha.monitor.library.util.Util.showToast;

public class TaskAssignmentActivity extends AppCompatActivity implements
        SiteTaskAndStatusAssignmentFragment.ProjectSiteTaskListener {


    Context ctx;
    ProjectSiteDTO site;
    CompanyDTO company;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_task_assignment);
        ctx = getApplicationContext();
        site = (ProjectSiteDTO) getIntent()
                .getSerializableExtra("projectSite");
        int type = getIntent().getIntExtra("type", SiteTaskAndStatusAssignmentFragment.OPERATIONS);
        company = (CompanyDTO)getIntent().getSerializableExtra("company");

        siteTaskAndStatusAssignmentFragment = (SiteTaskAndStatusAssignmentFragment)
                getSupportFragmentManager().findFragmentById(R.id.fragment);
        siteTaskAndStatusAssignmentFragment.setProjectSite(site, type);
        setTitle(site.getProjectSiteName());
        getSupportActionBar().setSubtitle(site.getProjectName());
        siteTaskAndStatusAssignmentFragment.setProjectSiteTaskList(site.getProjectSiteTaskList());
    }

    SiteTaskAndStatusAssignmentFragment siteTaskAndStatusAssignmentFragment;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.task_assignment, menu);
        mMenu = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected( MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_add) {
            siteTaskAndStatusAssignmentFragment.popupTaskList();
            return true;
        }
        if (id == R.id.action_help) {
            showToast(ctx, ctx.getString(R.string.under_cons));
            return true;
        }
        if (id == R.id.action_camera) {
            Intent i = new Intent(this, PictureActivity.class);
            i.putExtra("projectSite", site);
            i.putExtra("type", PhotoUploadDTO.SITE_IMAGE);
            startActivity(i);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onTaskClicked(ProjectSiteTaskDTO task) {

    }

//    @Override
//    public void onSubTaskStatusAssignmentRequested(ProjectSiteTaskDTO task) {
//        Intent w = new Intent(this, SubTaskStatusAssignmentActivity.class);
//        w.putExtra("projectSiteTask", task);
//        w.putExtra("projectSite", site);
//        startActivityForResult(w, SUBTASK_STATUS_ASSIGNMENT_REQUESTED);
//    }

    static final int SUBTASK_STATUS_ASSIGNMENT_REQUESTED = 8133;
    @Override
    public void onProjectSiteTaskAdded( ProjectSiteTaskDTO task) {
        Log.w(LOG, "## onProjectSiteTaskAdded " + task.getTask().getTaskName());
        projectSiteTaskList.add(task);
    }


    List<ProjectSiteTaskDTO> projectSiteTaskList = new ArrayList<>();

    @Override
    public void onProjectSiteTaskDeleted() {

    }

    @Override
    public void onSubTaskListRequested( ProjectSiteTaskDTO task, ProjectSiteTaskStatusDTO taskStatus) {

    }

    static final int SUBTASK_ASSIGNMENT = 11413;

    @Override
    public void onActivityResult(int reqCode, int resCode,  Intent data) {
        Log.w(LOG,"+++ onActivityResult reqCode: " + reqCode + " resCode: " + resCode);
        switch (reqCode) {
            case SUBTASK_STATUS_ASSIGNMENT_REQUESTED:
                if (resCode == RESULT_OK) {
                    ResponseDTO resp = (ResponseDTO) data.getSerializableExtra("response");
                    ProjectSiteTaskStatusDTO status = (ProjectSiteTaskStatusDTO)data.getSerializableExtra("status");
                    siteTaskAndStatusAssignmentFragment.updateList(status);
                }
                break;
        }
    }

    @Override
    public void onStatusDialogRequested(ProjectSiteDTO projectSite, ProjectSiteTaskDTO siteTask) {
        StatusDialog d = new StatusDialog();
        d.setProjectSite(projectSite);
        d.setProjectSiteTask(siteTask);
        d.setContext(getApplicationContext());
        d.setListener(new StatusDialog.StatusDialogListener() {
            @Override
            public void onStatusAdded(ProjectSiteTaskStatusDTO taskStatus) {
            }

            @Override
            public void onError(final String message) {
                Log.e("TaskAssignmentActivity", "---- ERROR websocket - " + message);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showErrorToast(ctx, message);
                    }
                });
            }
        });
        d.show(getFragmentManager(), "DIAG_STATUS");
    }

    @Override
    public void onProjectSiteTaskStatusAdded( ProjectSiteTaskStatusDTO taskStatus) {
        Log.w(LOG, "## onProjectSiteTaskStatusAdded " + taskStatus.getTask().getTaskName() + " "
                + taskStatus.getTaskStatus().getTaskStatusName());

        projectSiteTaskStatusList.add(taskStatus);
    }


    List<ProjectSiteTaskStatusDTO> projectSiteTaskStatusList = new ArrayList<>();

    @Override
    public void onCameraRequested(ProjectSiteTaskDTO siteTask, int type) {
        Intent i = new Intent(this, PictureActivity.class);
        i.putExtra("type", PhotoUploadDTO.TASK_IMAGE);
        i.putExtra("projectSiteTask", siteTask);
        startActivityForResult(i, TASK_PICTURE_REQUIRED);
    }

    static final int TASK_PICTURE_REQUIRED = 9582;
    private Menu mMenu;

    @Override
    public void onBackPressed() {
        ResponseDTO r = new ResponseDTO();
        r.setProjectSiteTaskStatusList(projectSiteTaskStatusList);
        r.setProjectSiteTaskList(projectSiteTaskList);
        Intent i = new Intent();
        i.putExtra("response", r);

        setResult(RESULT_OK, i);
        finish();
    }

    @Override
    public void onPause() {
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        super.onPause();
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
        ctx = null;
        mConnection = null;


    }

    boolean mBound;

    RequestSyncService mService;


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



    static final String LOG = TaskAssignmentActivity.class.getSimpleName();
}

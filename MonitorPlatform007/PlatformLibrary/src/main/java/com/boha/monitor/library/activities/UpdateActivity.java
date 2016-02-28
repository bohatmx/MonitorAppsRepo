package com.boha.monitor.library.activities;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;

import com.boha.monitor.library.dto.MonitorDTO;
import com.boha.monitor.library.dto.PhotoUploadDTO;
import com.boha.monitor.library.dto.ProjectDTO;
import com.boha.monitor.library.dto.ProjectTaskDTO;
import com.boha.monitor.library.dto.ProjectTaskStatusDTO;
import com.boha.monitor.library.dto.RequestDTO;
import com.boha.monitor.library.dto.ResponseDTO;
import com.boha.monitor.library.dto.StaffDTO;
import com.boha.monitor.library.fragments.MediaDialogFragment;
import com.boha.monitor.library.fragments.ProjectTaskListFragment;
import com.boha.monitor.library.fragments.TaskStatusUpdateFragment;
import com.boha.monitor.library.fragments.TaskListFragment;
import com.boha.monitor.library.services.PhotoUploadService;
import com.boha.monitor.library.util.CacheUtil;
import com.boha.monitor.library.util.NetUtil;
import com.boha.monitor.library.util.SharedUtil;
import com.boha.monitor.library.util.Snappy;
import com.boha.monitor.library.util.ThemeChooser;
import com.boha.monitor.library.util.Util;
import com.boha.monitor.library.util.WebCheck;
import com.boha.platform.library.R;

import java.util.List;

/**
 * This class manages the task status update process
 */
public class UpdateActivity extends AppCompatActivity
        implements ProjectTaskListFragment.ProjectTaskListener, TaskStatusUpdateFragment.TaskStatusUpdateListener {

    private ProjectDTO project;
    private ProjectTaskDTO projectTask;
    private ProjectTaskListFragment projectTaskListFragment;
    private TaskStatusUpdateFragment taskStatusUpdateFragment;
    private TaskListFragment taskListFragment;
    private int type, darkColor, primaryColor, position;
    public static final int NO_TYPES = 1, TYPES = 2;
    private boolean isStatusUpdate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeChooser.setTheme(this);
        Log.d(LOG, "UpdateActivity onCreate .....");
        Resources.Theme theme = getTheme();
        TypedValue typedValue = new TypedValue();
        theme.resolveAttribute(R.attr.colorPrimaryDark, typedValue, true);
        darkColor = typedValue.data;
        theme.resolveAttribute(R.attr.colorPrimary, typedValue, true);
        primaryColor = typedValue.data;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update);
        project = (ProjectDTO) getIntent().getSerializableExtra("project");
        type = getIntent().getIntExtra("type", 0);

        monitor = SharedUtil.getMonitor(getApplicationContext());
        staff = SharedUtil.getCompanyStaff(getApplicationContext());
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Util.setCustomActionBar(
                getApplicationContext(),
                getSupportActionBar(),
                project.getProjectName(), project.getCityName(),
                ContextCompat.getDrawable(getApplicationContext(), R.drawable.glasses));

        if (findViewById(R.id.frameLayout) != null) {
            if (savedInstanceState != null) {
                return;
            }
            getRealProject();
        }
    }

    private void getRealProject() {
        Snappy.getProject((MonApp) getApplication(),
                project.getProjectID(), new Snappy.SnappyProjectListener() {
                    @Override
                    public void onProjectFound(ProjectDTO p) {
                        project = p;
                        addTaskFragment();
                    }

                    @Override
                    public void onError() {
                        Util.showErrorToast(getApplicationContext(),"Unable to get project from cache");
                    }
                });
    }

    private void addTaskFragment() {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();

        //ft.setCustomAnimations(R.anim.enter_from_left, R.anim.exit_to_right);
        projectTaskListFragment = new ProjectTaskListFragment();
        projectTaskListFragment.setMonApp((MonApp) getApplication());
        projectTaskListFragment.setThemeColors(primaryColor, darkColor);
        projectTaskListFragment.setListener(this);
        projectTaskListFragment.setProject(project);

        ft.add(R.id.frameLayout, projectTaskListFragment);
        ft.commit();
    }

    private void replaceWithStatusFragment() {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();

        //ft.setCustomAnimations(R.anim.fade_in, R.anim.fade_out);
        taskStatusUpdateFragment = new TaskStatusUpdateFragment();
        taskStatusUpdateFragment.setMonApp((MonApp) getApplication());
        taskStatusUpdateFragment.setProjectTask(projectTask);
        taskStatusUpdateFragment.setThemeColors(primaryColor, darkColor);
        taskStatusUpdateFragment.setListener(this);

        ft.replace(R.id.frameLayout, taskStatusUpdateFragment);
        ft.commit();
    }

    @Override
    public void onStatusUpdateRequested(ProjectTaskDTO task, int position) {
        this.projectTask = task;
        this.position = position;
        isStatusUpdate = true;
        replaceWithStatusFragment();
    }

    static final int GET_PROJECT_PHOTO = 1385, GET_PROJECT_VIDEO = 1389;

    @Override
    public void onCameraRequested(final ProjectDTO project) {

        MediaDialogFragment mdf = new MediaDialogFragment();
        mdf.setListener(new MediaDialogFragment.MediaDialogListener() {
            @Override
            public void onVideoSelected() {
                Intent w = new Intent(getApplicationContext(), VideoActivity.class);
                w.putExtra("project", project);
                startActivityForResult(w, GET_PROJECT_VIDEO);
            }

            @Override
            public void onPhotoSelected() {
                Intent w = new Intent(getApplicationContext(), PictureActivity.class);
                w.putExtra("project", project);
                w.putExtra("type", PhotoUploadDTO.PROJECT_IMAGE);
                startActivityForResult(w, GET_PROJECT_PHOTO);
            }
        });
        mdf.show(getSupportFragmentManager(), "xxx");

    }

    @Override
    public void onStatusCameraRequested(ProjectTaskDTO task, ProjectTaskStatusDTO projectTaskStatus) {
        projectTask.getProjectTaskStatusList().add(projectTaskStatus);

        MediaDialogFragment mdf = new MediaDialogFragment();
        mdf.setListener(new MediaDialogFragment.MediaDialogListener() {
            @Override
            public void onVideoSelected() {
                Intent w = new Intent(getApplicationContext(), VideoActivity.class);
                w.putExtra("projectTask", projectTask);
                startActivityForResult(w, GET_PROJECT_TASK_VIDEO);
            }

            @Override
            public void onPhotoSelected() {

                Intent w = new Intent(getApplicationContext(), PictureActivity.class);
                w.putExtra("projectTask", projectTask);
                w.putExtra("type", PhotoUploadDTO.TASK_IMAGE);
                startActivityForResult(w, GET_PROJECT_TASK_PHOTO);
            }
        });
        mdf.show(getSupportFragmentManager(), "xxx");
    }

    @Override
    public void onProjectTaskCameraRequested(final ProjectTaskDTO projectTask) {

        MediaDialogFragment mdf = new MediaDialogFragment();
        mdf.setListener(new MediaDialogFragment.MediaDialogListener() {
            @Override
            public void onVideoSelected() {
                Intent w = new Intent(getApplicationContext(), VideoActivity.class);
                w.putExtra("projectTask", projectTask);
                startActivityForResult(w, GET_PROJECT_TASK_VIDEO);
            }

            @Override
            public void onPhotoSelected() {

                Intent w = new Intent(getApplicationContext(), PictureActivity.class);
                w.putExtra("projectTask", projectTask);
                w.putExtra("type", PhotoUploadDTO.TASK_IMAGE);
                startActivityForResult(w, GET_PROJECT_TASK_PHOTO);
            }
        });
        mdf.show(getSupportFragmentManager(), "PROJTASK1");

    }

    /**
     * A project task has had its status updated. Insert new status
     * into the projectTask status list and let fragment resfresh its ui
     *
     * @param p
     */
    @Override
    public void onStatusComplete(final ProjectDTO p) {
        Log.i(LOG, "onStatusComplete ... projectTaskListFragment.buildList");
        statusCompleted = true;
        project = p;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                replaceWithTaskList();
                isStatusUpdate = false;
            }
        });


    }

    boolean statusCompleted;

    private void replaceWithTaskList() {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.setCustomAnimations(R.anim.enter_from_left, R.anim.exit_to_right);
        projectTaskListFragment = new ProjectTaskListFragment();
        projectTaskListFragment.setMonApp((MonApp) getApplication());
        projectTaskListFragment.setListener(this);
        projectTaskListFragment.setProject(project);
        projectTaskListFragment.setThemeColors(primaryColor, darkColor);
        projectTaskListFragment.setSelectedIndex(position);

        ft.replace(R.id.frameLayout, projectTaskListFragment);
        ft.commit();
    }

    @Override
    public void onCancelStatusUpdate(ProjectTaskDTO projectTask) {
        replaceWithTaskList();
    }

    @Override
    public void onActivityResult(int reqCode, int resCode, Intent data) {
        Log.i(LOG, "## onActivityResult");
        switch (reqCode) {

            case GET_PROJECT_PHOTO:
                if (resCode == RESULT_OK) {
//                    if (resCode == RESULT_OK) {
//                        boolean isTaken =  data.getBooleanExtra("pictureTakenOK", false);
//                    } else {
//                    }
                }

                break;
            case GET_PROJECT_TASK_PHOTO:
                if (resCode == RESULT_OK) {
                    boolean isTaken = data.getBooleanExtra("pictureTakenOK", false);
                    taskStatusUpdateFragment.onPictureTaken(isTaken);
                    statusCompleted = true;
                } else {
                    taskStatusUpdateFragment.onPictureTaken(false);
                }
                break;
            case GET_PROJECT_VIDEO:

                break;
            case GET_PROJECT_TASK_VIDEO:

                break;
        }
    }

    StaffDTO staff;
    MonitorDTO monitor;
    boolean cachingBusy;

    private void refreshData(final Integer projectID) {
        Log.w(LOG, "###### refreshData projectID: " + projectID.intValue());


        RequestDTO w = new RequestDTO();
        w.setRequestType(RequestDTO.GET_PROJECT_TASKS);
        w.setStaffID(staff.getStaffID());


        if (WebCheck.checkNetworkAvailability(getApplicationContext()).isNetworkUnavailable()) {
            return;
        }
        setBusy(true);
        NetUtil.sendRequest(getApplicationContext(), w, new NetUtil.NetUtilListener() {
            @Override
            public void onResponse(final ResponseDTO response) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        setBusy(false);
                        if (response.getStatusCode() == 0) {
                            project.setProjectTaskList(response.getProjectTaskList());
                            projectTaskListFragment.setProject(project);
                            CacheUtil.cacheProject(project, null);
                        }

                    }
                });
            }

            @Override
            public void onError(final String message) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        setBusy(false);
                        Util.showErrorToast(getApplicationContext(), message);
                    }
                });
            }


        });

    }


    @Override
    public void onBackPressed() {
        if (cachingBusy) {
            onBackPressed();
            return;
        }
        if (isStatusUpdate) {
            isStatusUpdate = false;
            replaceWithTaskList();
        } else {
            Intent w = new Intent();
            w.putExtra("statusCompleted", statusCompleted);
            setResult(RESULT_OK, w);
            finish();
        }
    }

    Menu mMenu;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_status_update, menu);
        mMenu = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_refresh) {
            return true;
        }
        if (id == R.id.action_help) {
            Util.showToast(getApplicationContext(), "Help under construction");
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void setBusy(boolean busy) {
        setRefreshActionButtonState(busy);
    }

    public void setRefreshActionButtonState(final boolean refreshing) {
        if (mMenu != null) {
            final MenuItem refreshItem = mMenu.findItem(R.id.action_help);
            if (refreshItem != null) {
                if (refreshing) {
                    refreshItem.setActionView(R.layout.action_bar_progess);
                } else {
                    refreshItem.setActionView(null);
                }
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.i(LOG, "## onStart Bind to PhotoUploadService");
        Intent intent = new Intent(this, PhotoUploadService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

    }

    @Override
    public void onStop() {
        super.onStop();
        Log.e(LOG, "## onStop unBind from PhotoUploadService");
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }

    }

    boolean mBound, rBound;
    PhotoUploadService mService;

    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            Log.w(LOG, "## PhotoUploadService ServiceConnection onServiceConnected");
            PhotoUploadService.LocalBinder binder = (PhotoUploadService.LocalBinder) service;
            mService = binder.getService();
            mBound = true;
            mService.uploadCachedPhotos(new PhotoUploadService.UploadListener() {
                @Override
                public void onUploadsComplete(final List<PhotoUploadDTO> list) {
                    Log.w(LOG, "$$$ onUploadsComplete, list: " + list.size());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (!list.isEmpty()) {
                                refreshData(project.getProjectID());
                            }
                        }
                    });

                }
            });
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            Log.w(LOG, "## PhotoUploadService onServiceDisconnected");
            mBound = false;
        }
    };

    static final int GET_PROJECT_TASK_PHOTO = 6382,
            GET_PROJECT_TASK_VIDEO = 7896;
    static final String LOG = UpdateActivity.class.getSimpleName();
}

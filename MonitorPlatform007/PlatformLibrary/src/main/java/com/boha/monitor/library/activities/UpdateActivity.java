package com.boha.monitor.library.activities;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
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
import com.boha.monitor.library.fragments.ProjectTaskListFragment;
import com.boha.monitor.library.fragments.TaskStatusUpdateFragment;
import com.boha.monitor.library.fragments.TaskTypeListFragment;
import com.boha.monitor.library.util.CacheUtil;
import com.boha.monitor.library.util.NetUtil;
import com.boha.monitor.library.util.SharedUtil;
import com.boha.monitor.library.util.ThemeChooser;
import com.boha.monitor.library.util.Util;
import com.boha.monitor.library.util.WebCheck;
import com.boha.platform.library.R;

/**
 * This class manages the task status update process
 */
public class UpdateActivity extends AppCompatActivity
        implements ProjectTaskListFragment.ProjectTaskListener, TaskStatusUpdateFragment.TaskStatusUpdateListener {

    private ProjectDTO project;
    private ProjectTaskDTO projectTask;
    private ProjectTaskListFragment projectTaskListFragment;
    private TaskStatusUpdateFragment taskStatusUpdateFragment;
    private TaskTypeListFragment taskTypeListFragment;
    private int type, darkColor, primaryColor, position;
    public static final int NO_TYPES = 1, TYPES = 2;
    private boolean isStatusUpdate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeChooser.setTheme(this);
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

        //getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_menu);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Util.setCustomActionBar(
                getApplicationContext(),
                getSupportActionBar(),
                project.getProjectName(), project.getCityName(),
                ContextCompat.getDrawable(getApplicationContext(), R.drawable.glasses48));

        if (findViewById(R.id.frameLayout) != null) {
            if (savedInstanceState != null) {
                return;
            }
            addTaskFragment();
        }
    }

    private void addTaskFragment() {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();

        ft.setCustomAnimations(R.anim.enter_from_left, R.anim.exit_to_right);
        projectTaskListFragment = ProjectTaskListFragment.newInstance(project);
        projectTaskListFragment.setThemeColors(primaryColor, darkColor);
        projectTaskListFragment.setListener(this);
        ft.add(R.id.frameLayout, projectTaskListFragment);
        ft.commit();
    }

    private void replaceWithStatusFragment() {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();

        ft.setCustomAnimations(R.anim.fade_in, R.anim.fade_out);
        taskStatusUpdateFragment = new TaskStatusUpdateFragment();
        taskStatusUpdateFragment.setProjectTask(projectTask);
        taskStatusUpdateFragment.setType(type);
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

    @Override
    public void onCameraRequested(ProjectTaskDTO task) {

    }

    @Override
    public void onStatusCameraRequested(ProjectTaskDTO task, ProjectTaskStatusDTO projectTaskStatus) {
        projectTask.getProjectTaskStatusList().add(projectTaskStatus);

        Intent w = new Intent(this, PictureActivity.class);
        w.putExtra("projectTask", projectTask);
        w.putExtra("type", PhotoUploadDTO.TASK_IMAGE);
        startActivityForResult(w, GET_PROJECT_TASK_PHOTO);
    }

    @Override
    public void onProjectTaskCameraRequested(ProjectTaskDTO projectTask) {
        Intent w = new Intent(this, PictureActivity.class);
        w.putExtra("projectTask", projectTask);
        w.putExtra("type", PhotoUploadDTO.TASK_IMAGE);
        startActivityForResult(w, GET_PROJECT_TASK_PHOTO);
    }

    @Override
    public void onStatusComplete(ProjectTaskDTO projectTask) {

        replaceWithTaskList();
        isStatusUpdate = false;
        refreshData(projectTask.getProjectID());
    }

    private void replaceWithTaskList() {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.setCustomAnimations(R.anim.enter_from_left, R.anim.exit_to_right);
        projectTaskListFragment = ProjectTaskListFragment.newInstance(project);
        projectTaskListFragment.setListener(this);
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

            case GET_PROJECT_TASK_PHOTO:
                if (resCode == RESULT_OK) {
                    ResponseDTO resp = (ResponseDTO) data.getSerializableExtra("response");
                    taskStatusUpdateFragment.displayPhotos(resp.getPhotoUploadList());
                } else {
                    taskStatusUpdateFragment.onNoPhotoTaken();
                }
                break;
        }
    }

    private void refreshData(final Integer projectID) {
        Log.w(LOG, "###### refreshData projectID: " + projectID.intValue());
        final StaffDTO staff = SharedUtil.getCompanyStaff(getApplicationContext());
        final MonitorDTO monitor = SharedUtil.getMonitor(getApplicationContext());

        RequestDTO w = new RequestDTO();
        if (staff != null) {
            w.setRequestType(RequestDTO.GET_STAFF_DATA);
            w.setStaffID(staff.getStaffID());
        } else {
            w.setRequestType(RequestDTO.GET_MONITOR_PROJECTS);
            w.setMonitorID(monitor.getMonitorID());
        }

        if (WebCheck.checkNetworkAvailability(getApplicationContext()).isNetworkUnavailable()) {
            return;
        }
        NetUtil.sendRequest(getApplicationContext(), w, new NetUtil.NetUtilListener() {
            @Override
            public void onResponse(final ResponseDTO response) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        for (ProjectDTO x : response.getProjectList()) {
                            if (x.getProjectID().intValue() == projectID.intValue()) {
                                project = x;
                                break;
                            }
                        }
                        projectTaskListFragment.setProject(project);
                        if (staff != null) {
                            CacheUtil.cacheStaffData(getApplicationContext(), response, null);
                        } else {
                            CacheUtil.cacheMonitorProjects(getApplicationContext(), response, null);
                        }

                    }
                });
            }

            @Override
            public void onError(final String message) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Util.showErrorToast(getApplicationContext(), message);
                    }
                });
            }

            @Override
            public void onWebSocketClose() {

            }
        });

    }

    @Override
    public void onBackPressed() {
        if (isStatusUpdate) {
            isStatusUpdate = false;
            replaceWithTaskList();
        } else {
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

    static final int GET_PROJECT_TASK_PHOTO = 6382;
    static final String LOG = UpdateActivity.class.getSimpleName();
}

package com.boha.monitor.library.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.boha.monitor.library.dto.PhotoUploadDTO;
import com.boha.monitor.library.dto.ProjectDTO;
import com.boha.monitor.library.dto.ProjectTaskDTO;
import com.boha.monitor.library.dto.ProjectTaskStatusDTO;
import com.boha.monitor.library.dto.ResponseDTO;
import com.boha.monitor.library.dto.TaskTypeDTO;
import com.boha.monitor.library.fragments.MessagingFragment;
import com.boha.monitor.library.fragments.MonitorListFragment;
import com.boha.monitor.library.fragments.MonitorProfileFragment;
import com.boha.monitor.library.fragments.PageFragment;
import com.boha.monitor.library.fragments.ProjectListFragment;
import com.boha.monitor.library.fragments.ProjectTaskListFragment;
import com.boha.monitor.library.fragments.TaskStatusUpdateFragment;
import com.boha.monitor.library.fragments.TaskTypeListFragment;
import com.boha.monitor.library.util.DepthPageTransformer;
import com.boha.monitor.library.util.ThemeChooser;
import com.boha.monitor.library.util.Util;
import com.boha.platform.library.R;

import java.util.ArrayList;
import java.util.List;

public class MainUpdateActivity extends AppCompatActivity
        implements TaskTypeListFragment.TaskTypeListener,
        ProjectTaskListFragment.StatusUpdateListener,
        TaskStatusUpdateFragment.TaskStatusUpdateListener {


    /**
     * The {@link ViewPager} that will host the section contents.
     */
    ViewPager mViewPager;
    PagerAdapter adapter;
    ProjectDTO project;
    int type, darkColor, primaryColor;
    public static final int NO_TYPES = 1, TYPES = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeChooser.setTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_update);

        project = (ProjectDTO) getIntent().getSerializableExtra("project");
        type = getIntent().getIntExtra("type", 0);
        darkColor = getIntent().getIntExtra("darkColor", R.color.black);
        mViewPager = (ViewPager) findViewById(R.id.viewpager);

        buildFirstPage();
        Util.setCustomActionBar(
                getApplicationContext(),
                getSupportActionBar(),
                project.getProjectName(), project.getCityName(),
                ContextCompat.getDrawable(getApplicationContext(), R.drawable.glasses48));


    }

    private void buildFirstPage() {
        pageFragmentList = new ArrayList<>();

        switch (type) {
            case NO_TYPES:
                ProjectTaskListFragment projectTaskListFragment = ProjectTaskListFragment.newInstance(project);
                projectTaskListFragment.setTaskType(null);
                projectTaskListFragment.setThemeColors(primaryColor, darkColor);
                pageFragmentList.add(projectTaskListFragment);
                break;
            case TYPES:
                TaskTypeListFragment taskTypeListFragment =
                        TaskTypeListFragment.newInstance(project, type);
                taskTypeListFragment.setThemeColors(primaryColor, darkColor);
                pageFragmentList.add(taskTypeListFragment);
                break;
        }



        adapter = new PagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(adapter);
        mViewPager.setPageTransformer(true, new DepthPageTransformer());

    }


    @Override
    public void onTaskTypeClicked(TaskTypeDTO taskType) {
        if (getProjectTaskListFragment() != null) {
            getProjectTaskListFragment().setTaskType(taskType);
            mViewPager.setCurrentItem(1, true);
        } else {
            ProjectTaskListFragment projectTaskListFragment = ProjectTaskListFragment.newInstance(project);
            projectTaskListFragment.setTaskType(taskType);
            projectTaskListFragment.setThemeColors(primaryColor, darkColor);
            pageFragmentList.add(projectTaskListFragment);
            adapter.notifyDataSetChanged();
            mViewPager.setCurrentItem(1, true);
        }

    }

    private ProjectTaskListFragment getProjectTaskListFragment() {
        ProjectTaskListFragment fragment = null;
        for (PageFragment pf : pageFragmentList) {
            if (pf instanceof ProjectTaskListFragment) {
                fragment = (ProjectTaskListFragment) pf;
                break;
            }
        }
        return fragment;
    }

    private TaskStatusUpdateFragment getTaskStatusUpdateFragment() {
        TaskStatusUpdateFragment fragment = null;
        for (PageFragment pf : pageFragmentList) {
            if (pf instanceof TaskStatusUpdateFragment) {
                fragment = (TaskStatusUpdateFragment) pf;
                break;
            }
        }
        return fragment;
    }

    @Override
    public void onStatusUpdateRequested(ProjectTaskDTO task, int position) {
        if (getTaskStatusUpdateFragment() != null) {
            getTaskStatusUpdateFragment().setProjectTask(task);
            mViewPager.setCurrentItem(2, true);
        } else {
            TaskStatusUpdateFragment taskStatusUpdateFragment = TaskStatusUpdateFragment.newInstance(task, type);
            taskStatusUpdateFragment.setProjectTask(task);
            taskStatusUpdateFragment.setThemeColors(primaryColor, darkColor);
            pageFragmentList.add(taskStatusUpdateFragment);
            adapter.notifyDataSetChanged();
            mViewPager.setCurrentItem(2, true);
        }
    }

    @Override
    public void onCameraRequested(ProjectTaskDTO task) {

    }

    static final int GET_PROJECT_TASK_PHOTO = 6382;

    @Override
    public void onStatusCameraRequested(ProjectTaskDTO projectTask, ProjectTaskStatusDTO projectTaskStatus) {
        Intent w = new Intent(this, PictureActivity.class);
        w.putExtra("projectTask", projectTask);
        w.putExtra("projectTaskStatus", projectTaskStatus);
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
        int index = 0;
        for (PageFragment pageFragment: pageFragmentList) {
            if (pageFragment instanceof TaskStatusUpdateFragment) {
                break;
            }
            index++;
        }
        pageFragmentList.remove(index);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onActivityResult(int reqCode, int resCode, Intent data) {

        switch (reqCode) {

            case GET_PROJECT_TASK_PHOTO:
                if (resCode == RESULT_OK) {
                    ResponseDTO resp = (ResponseDTO) data.getSerializableExtra("response");
                    for (PageFragment f : pageFragmentList) {
                        if (f instanceof TaskStatusUpdateFragment) {
                            TaskStatusUpdateFragment fr = (TaskStatusUpdateFragment) f;
                            fr.displayPhotos(resp.getPhotoUploadList());
                        }
                    }
                }
                break;
        }
    }

    Menu mMenu;
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main_update, menu);
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
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void setBusy(boolean busy) {
        setRefreshActionButtonState(busy);
    }

    private class PagerAdapter extends FragmentStatePagerAdapter {

        public PagerAdapter(android.support.v4.app.FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {

            return (Fragment) pageFragmentList.get(i);
        }

        @Override
        public int getCount() {
            return pageFragmentList.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            PageFragment pf = pageFragmentList.get(position);
            String title = "No Title";
            if (pf instanceof ProjectListFragment) {
                title = getString(R.string.projects);
            }
            if (pf instanceof MonitorListFragment) {
                title = getString(R.string.monitors);
            }
            if (pf instanceof MessagingFragment) {
                title = getString(R.string.messaging);
            }
            if (pf instanceof MonitorProfileFragment) {
                title = getString(R.string.profile);
            }

            return title;
        }
    }
    public void setRefreshActionButtonState(final boolean refreshing) {
        if (mMenu != null) {
            final MenuItem refreshItem = mMenu.findItem(R.id.action_refresh);
            if (refreshItem != null) {
                if (refreshing) {
                    refreshItem.setActionView(R.layout.action_bar_progess);
                } else {
                    refreshItem.setActionView(null);
                }
            }
        }
    }

    List<PageFragment> pageFragmentList;
}

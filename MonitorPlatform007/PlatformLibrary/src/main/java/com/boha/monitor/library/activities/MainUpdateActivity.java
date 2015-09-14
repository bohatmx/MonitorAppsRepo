package com.boha.monitor.library.activities;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.boha.monitor.library.dto.ProjectDTO;
import com.boha.monitor.library.dto.ProjectTaskDTO;
import com.boha.monitor.library.dto.ProjectTaskStatusDTO;
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
import com.boha.monitor.library.util.Util;
import com.boha.platform.library.R;

import java.util.ArrayList;
import java.util.List;

public class MainUpdateActivity extends AppCompatActivity
        implements TaskTypeListFragment.TaskTypeListener,
        ProjectTaskListFragment.StatusUpdateListener,
        TaskStatusUpdateFragment.TaskStatusUpdateListener{


    /**
     * The {@link ViewPager} that will host the section contents.
     */
    ViewPager mViewPager;
    PagerAdapter adapter;
    ProjectDTO project;
    int type, darkColor, primaryColor;

    TaskTypeListFragment taskTypeListFragment;
    ProjectTaskListFragment projectTaskListFragment;
    TaskStatusUpdateFragment taskStatusUpdateFragment;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_update);

        project = (ProjectDTO)getIntent().getSerializableExtra("project");
        type = getIntent().getIntExtra("type",0);
        darkColor = getIntent().getIntExtra("darkColor",R.color.black);
        mViewPager = (ViewPager) findViewById(R.id.viewpager);

        buildFirstPage();

        Util.setCustomActionBar(
                getApplicationContext(),
                getSupportActionBar(),
                project.getProjectName(),project.getCityName(),
                ContextCompat.getDrawable(getApplicationContext(),R.drawable.glasses48));


    }

    private void buildFirstPage() {
        pageFragmentList = new ArrayList<>();

        taskTypeListFragment = TaskTypeListFragment.newInstance(project, type);
        taskTypeListFragment.setThemeColors(primaryColor,darkColor);
        pageFragmentList.add(taskTypeListFragment);

        adapter = new PagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(adapter);
        mViewPager.setPageTransformer(true, new DepthPageTransformer());

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main_update, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onTaskTypeClicked(TaskTypeDTO taskType) {
        if (pageFragmentList.size() > 1) {
            pageFragmentList = new ArrayList<>();
            pageFragmentList.add(taskTypeListFragment);
            adapter.notifyDataSetChanged();
        }

        projectTaskListFragment = ProjectTaskListFragment.newInstance(project);
        projectTaskListFragment.setTaskType(taskType);

        pageFragmentList.add(projectTaskListFragment);
        adapter.notifyDataSetChanged();
        mViewPager.setCurrentItem(1,true);
    }

    @Override
    public void onStatusUpdateRequested(ProjectTaskDTO task, int position) {
        if (pageFragmentList.size() > 2) {
            pageFragmentList.remove(2);
            adapter.notifyDataSetChanged();
        }
        taskStatusUpdateFragment = null;
        taskStatusUpdateFragment = TaskStatusUpdateFragment.newInstance(task,type);
        pageFragmentList.add(taskStatusUpdateFragment);
        adapter.notifyDataSetChanged();
        mViewPager.setCurrentItem(2,true);
    }

    @Override
    public void onCameraRequested(ProjectTaskDTO task) {

    }

    @Override
    public void onStatusCameraRequested(ProjectTaskDTO projectTask, ProjectTaskStatusDTO projectTaskStatus) {

    }

    @Override
    public void setBusy(boolean busy) {

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

    List<PageFragment> pageFragmentList;
}

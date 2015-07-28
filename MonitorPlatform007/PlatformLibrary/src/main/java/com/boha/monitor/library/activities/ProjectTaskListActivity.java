package com.boha.monitor.library.activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.boha.monitor.library.dto.ProjectDTO;
import com.boha.monitor.library.dto.ProjectTaskDTO;
import com.boha.monitor.library.dto.ProjectTaskStatusDTO;
import com.boha.monitor.library.dto.TaskTypeDTO;
import com.boha.monitor.library.fragments.ProjectTaskListFragment;
import com.boha.monitor.library.util.ThemeChooser;
import com.boha.platform.library.R;

import java.util.ArrayList;

public class ProjectTaskListActivity extends AppCompatActivity implements ProjectTaskListFragment.StatusUpdateListener{

    ProjectTaskListFragment projectTaskListFragment;
    ProjectDTO project;
    TaskTypeDTO taskType;
    int darkColor;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        ThemeChooser.setTheme(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status_update);

        project = (ProjectDTO)getIntent().getSerializableExtra("project");
        taskType = (TaskTypeDTO)getIntent().getSerializableExtra("taskType");
        darkColor = getIntent().getIntExtra("darkColor", getApplicationContext().getResources().getColor(R.color.blue_300));
        Log.d("StatusUpdateActivity", "+++ onCreate - darkColor: " + darkColor);
        projectTaskListFragment = (ProjectTaskListFragment)getSupportFragmentManager().findFragmentById(R.id.fragment);
        projectTaskListFragment.setDarkColor(darkColor);

        projectTaskListFragment.setTaskType(taskType);
        projectTaskListFragment.setProject(project);

        android.support.v7.app.ActionBar bar = getSupportActionBar();
        bar.setTitle(project.getProjectName());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_status_update, menu);
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
    public void onPause() {
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        super.onPause();
    }

    @Override
    public void onStatusUpdateRequested(ProjectTaskDTO task, int position) {
        projectTask = task;
        Intent w = new Intent(this, TaskUpdateActivity.class);
        w.putExtra("projectTask",task);
        w.putExtra("project",project);
        startActivityForResult(w,TASK_UPDATE_REQUEST);
    }

    ProjectTaskDTO projectTask;
    @Override
    public void onCameraRequested(ProjectTaskDTO task) {
        Log.e("StatusUpdateActivity", "### onCameraRequested ...");
    }

    static final int TASK_UPDATE_REQUEST = 2119;
    @Override
    public void onActivityResult(int reqCode, int resultCode, Intent data) {
        switch (reqCode) {
            case TASK_UPDATE_REQUEST:
                if (resultCode == RESULT_OK) {
                    ProjectTaskStatusDTO x = (ProjectTaskStatusDTO)data.getSerializableExtra("projectTaskStatus");
                    if (projectTask.getProjectTaskStatusList() == null) {
                        projectTask.setProjectTaskStatusList(new ArrayList<ProjectTaskStatusDTO>());
                    }
                    projectTask.getProjectTaskStatusList().add(x);
                    projectTaskListFragment.refreshProjectTask(projectTask);
                }
        }
    }
}

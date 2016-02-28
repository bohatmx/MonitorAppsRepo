package com.boha.monitor.library.activities;

import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;

import com.boha.monitor.library.dto.ProjectDTO;
import com.boha.monitor.library.dto.ProjectTaskDTO;
import com.boha.monitor.library.fragments.ProjectTaskFragment;
import com.boha.monitor.library.util.SharedUtil;
import com.boha.monitor.library.util.Util;
import com.boha.platform.library.R;

import java.util.List;

public class ProjectTaskActivity extends AppCompatActivity implements ProjectTaskFragment.ProjectTaskListener{

    ProjectTaskFragment projectTaskFragment;
    int type;
    ProjectDTO project;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project_task);
        type = getIntent().getIntExtra("type",ProjectTaskFragment.ADD_NEW_PROJECT);
        project = (ProjectDTO) getIntent().getSerializableExtra("project");

        projectTaskFragment = (ProjectTaskFragment)
                getSupportFragmentManager().findFragmentById(R.id.fragment);
        projectTaskFragment.setApp((MonApp)getApplication());
        projectTaskFragment.setProject(project);
        projectTaskFragment.setType(type);

        Util.setCustomActionBar(getApplicationContext(), getSupportActionBar(),
                SharedUtil.getCompany(getApplicationContext()).getCompanyName(), "Project Tasks",
                ContextCompat.getDrawable(getApplicationContext(), com.boha.platform.library.R.drawable.glasses));


    }

    boolean areTasksAssigned;
    List<ProjectTaskDTO> projectTaskList;
    @Override
    public void onTasksAssigned(List<ProjectTaskDTO> projectTaskList) {
        areTasksAssigned = true;
        this.projectTaskList = projectTaskList;
    }
    @Override
    public void onBackPressed() {
        if (areTasksAssigned) {
            Intent m = new Intent();
            m.putExtra("tasksAssigned", areTasksAssigned);
            setResult(RESULT_OK,m);
        }
        finish();
    }
}

package com.boha.monitor.library.activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.boha.monitor.library.dto.PhotoUploadDTO;
import com.boha.monitor.library.dto.ProjectTaskDTO;
import com.boha.monitor.library.fragments.TaskStatusUpdateFragment;
import com.boha.monitor.library.util.ThemeChooser;
import com.boha.platform.library.R;

public class TaskUpdateActivity extends AppCompatActivity implements TaskStatusUpdateFragment.TaskStatusUpdateListener{

    TaskStatusUpdateFragment taskStatusUpdateFragment;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ThemeChooser.setTheme(this);
        setContentView(R.layout.activity_task_update);

        ProjectTaskDTO task = (ProjectTaskDTO) getIntent().getSerializableExtra("projectTask");
        taskStatusUpdateFragment = (TaskStatusUpdateFragment)getSupportFragmentManager().findFragmentById(R.id.fragment);
        taskStatusUpdateFragment.setProjectTask(task);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_task_update, menu);
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
    public void onCameraRequested(ProjectTaskDTO projectTask) {
        Log.e("TaskUpdateActivity","onCameraRequested, projectTask coming in ");
        Intent w = new Intent(this,PictureActivity.class);
        w.putExtra("type", PhotoUploadDTO.TASK_IMAGE);
        w.putExtra("projectTask",projectTask);
        startActivityForResult(w,REQUEST_CAMERA);
    }

    static final int REQUEST_CAMERA = 1432;
}

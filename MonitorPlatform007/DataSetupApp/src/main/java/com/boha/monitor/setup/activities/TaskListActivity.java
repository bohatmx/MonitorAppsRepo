package com.boha.monitor.setup.activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.boha.monitor.library.dto.TaskDTO;
import com.boha.monitor.library.dto.TaskTypeDTO;
import com.boha.monitor.setup.R;
import com.boha.monitor.setup.fragments.TaskListFragment;

public class TaskListActivity extends AppCompatActivity implements TaskListFragment.TaskListener{

    TaskListFragment taskListFragment;
    TaskTypeDTO taskType;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task);
        taskType = (TaskTypeDTO)getIntent().getSerializableExtra("taskType");
        taskListFragment = (TaskListFragment)getSupportFragmentManager().findFragmentById(R.id.fragment);
        taskListFragment.setTaskType(taskType);

        setTitle("TaskType Tasks");
        getSupportActionBar().setSubtitle(taskType.getTaskTypeName());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_task, menu);
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
    public void onTaskClicked(TaskDTO task) {

    }

    @Override
    public void onSubTaskCountClicked(TaskDTO task) {

    }

    @Override
    public void onIconDeleteClicked(TaskDTO task, int position) {

    }

    @Override
    public void onIconEditClicked(TaskDTO task, int position) {

    }

    @Override
    public void setBusy(boolean busy) {

    }
    @Override
    public void onPause() {
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        super.onPause();
    }
}

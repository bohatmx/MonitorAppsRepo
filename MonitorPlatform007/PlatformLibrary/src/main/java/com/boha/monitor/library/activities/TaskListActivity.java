package com.boha.monitor.library.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.boha.monitor.library.dto.CompanyDTO;
import com.boha.monitor.library.dto.ProjectDTO;
import com.boha.monitor.library.dto.TaskDTO;
import com.boha.monitor.library.fragments.TaskListFragment;
import com.boha.monitor.library.services.DataRefreshService;
import com.boha.monitor.library.util.SharedUtil;
import com.boha.monitor.library.util.ThemeChooser;
import com.boha.monitor.library.util.Util;
import com.boha.platform.library.R;

public class TaskListActivity extends AppCompatActivity implements TaskListFragment.TaskListListener {

    TaskListFragment taskListFragment;
    int themeDarkColor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.w(LOG, "## onCreate");
        ThemeChooser.setTheme(this);
        setContentView(R.layout.activity_task_type_list);

        taskListFragment = (TaskListFragment)
                getSupportFragmentManager().findFragmentById(R.id.fragment);
        taskListFragment.setApp((MonApp) getApplication());
        taskListFragment.setDarkColor(themeDarkColor);

        CompanyDTO c = SharedUtil.getCompany(getApplicationContext());
        Util.setCustomActionBar(getApplicationContext(), getSupportActionBar(),
                c.getCompanyName(), "Tasks",
                ContextCompat.getDrawable(getApplicationContext(), R.drawable.glasses));
    }

    @Override
    public void onSaveInstanceState(Bundle b) {
        Log.w(LOG, "## onSaveInstanceState");
        super.onSaveInstanceState(b);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_task_type_list, menu);
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
    public void onStart() {
        super.onStart();
        Log.i(LOG, "## onStart Bind to RequestSyncService");
//        Intent intent = new Intent(this, RequestSyncService.class);
//        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

    }

    @Override
    public void onStop() {
        super.onStop();
        Log.e(LOG, "## onStop unBind from RequestSyncService");
//        if (mBound) {
//            unbindService(mConnection);
//            mBound = false;
//        }

    }

    boolean mBound;


    static final String LOG = TaskListActivity.class.getSimpleName();

    boolean tasksAdded;

    @Override
    public void onTaskAdded(TaskDTO task) {
        tasksAdded = true;
    }

    @Override
    public void onBackPressed() {
        if (tasksAdded) {
            Intent m = new Intent(getApplicationContext(), DataRefreshService.class);
            startService(m);
        }
        finish();
    }
}

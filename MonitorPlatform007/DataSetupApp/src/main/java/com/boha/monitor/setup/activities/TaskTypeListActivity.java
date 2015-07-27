package com.boha.monitor.setup.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.boha.monitor.library.dto.CompanyDTO;
import com.boha.monitor.library.dto.PortfolioDTO;
import com.boha.monitor.library.dto.ProgrammeDTO;
import com.boha.monitor.library.dto.ResponseDTO;
import com.boha.monitor.library.dto.TaskTypeDTO;
import com.boha.monitor.library.util.CacheUtil;
import com.boha.monitor.setup.R;
import com.boha.monitor.setup.adapters.TaskTypeDataAdapter;
import com.boha.monitor.setup.fragments.TaskTypeListFragment;

public class TaskTypeListActivity extends AppCompatActivity implements TaskTypeDataAdapter.TaskTypeListener {

    TaskTypeListFragment taskTypeListFragment;
    ProgrammeDTO programme;
    Integer programmeID;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_type_list);

        programmeID = getIntent().getIntExtra("programmeID",0);
        taskTypeListFragment = (TaskTypeListFragment) getSupportFragmentManager().findFragmentById(R.id.fragment);
        CacheUtil.getCachedData(getApplicationContext(), CacheUtil.CACHE_DATA, new CacheUtil.CacheUtilListener() {
            @Override
            public void onFileDataDeserialized(ResponseDTO response) {
                if (response.getCompanyList() != null) {
                    for (CompanyDTO w : response.getCompanyList()) {
                        for (PortfolioDTO x : w.getPortfolioList()) {
                            for (ProgrammeDTO y : x.getProgrammeList()) {
                                if (programmeID.intValue() == y.getProgrammeID().intValue()) {
                                    programme = y;
                                    taskTypeListFragment.setProgramme(programme);
                                    setTitle("Task Types/Categories");
                                    getSupportActionBar().setSubtitle(programme.getProgrammeName());
                                }
                            }
                        }
                    }
                }
            }

            @Override
            public void onDataCached() {

            }

            @Override
            public void onError() {

            }
        });


    }

    @Override
    public void onSaveInstanceState(Bundle b) {
        b.putSerializable("programme", programme);
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
    public void onTaskTypeClicked(TaskTypeDTO taskType) {
        Intent w = new Intent(this,TaskListActivity.class);
        w.putExtra("taskType",taskType);
        startActivity(w);
    }

    @Override
    public void onTaskCountClicked(TaskTypeDTO taskType) {

        Intent w = new Intent(this,TaskListActivity.class);
        w.putExtra("taskType",taskType);
        startActivity(w);
    }

    @Override
    public void onIconDeleteClicked(TaskTypeDTO taskType, int position) {

    }

    @Override
    public void onIconEditClicked(TaskTypeDTO taskType, int position) {

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

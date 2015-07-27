package com.boha.monitor.setup.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.boha.monitor.library.dto.ProgrammeDTO;
import com.boha.monitor.library.dto.ProjectDTO;
import com.boha.monitor.library.dto.ResponseDTO;
import com.boha.monitor.library.dto.TaskTypeDTO;
import com.boha.monitor.library.util.Util;
import com.boha.monitor.setup.R;
import com.boha.monitor.setup.fragments.FileImportFragment;

import java.util.List;

public class FileImportActivity extends AppCompatActivity implements FileImportFragment.ImportListener{

    FileImportFragment fileImportFragment;
    ProgrammeDTO programme;
    int importType;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_import);

        programme = (ProgrammeDTO)getIntent().getSerializableExtra("programme");
        importType = getIntent().getIntExtra("importType", 0);

        fileImportFragment = (FileImportFragment)getSupportFragmentManager().findFragmentById(R.id.fragment);
        fileImportFragment.setProgramme(programme);
        fileImportFragment.setImportType(importType);

        setTitle(programme.getProgrammeName());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_data_import, menu);
        mMenu = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_info) {
            Util.showToast(this,getString(R.string.under_cons));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onTasksImported(List<TaskTypeDTO> taskTypeList) {
        setRefreshActionButtonState(false);
        Log.d(LOG, "onTasksImported");
        response = new ResponseDTO();
        response.setTaskTypeList(taskTypeList);
        Log.i(LOG, "+++ taskTypes imported: " + taskTypeList.size());
        taskImportDone = true;
        onBackPressed();
    }

    @Override
    public void onProjectsImported(List<ProjectDTO> projectList) {
        setRefreshActionButtonState(false);
        Log.d(LOG, "onProjectsImported");
        response = new ResponseDTO();
        response.setProjectList(projectList);
        Log.i(LOG, "+++ projects imported: " + projectList.size());
        projectImportDone = true;
        onBackPressed();
    }

    @Override
    public void onError(String message) {
        setRefreshActionButtonState(false);

    }

    @Override
    public void setBusy(boolean busy) {
        setRefreshActionButtonState(busy);
    }


    Menu mMenu;
    ResponseDTO response;
    public void setRefreshActionButtonState(final boolean refreshing) {
        if (mMenu != null) {
            final MenuItem refreshItem = mMenu.findItem(R.id.action_info);
            if (refreshItem != null) {
                if (refreshing) {
                    refreshItem.setActionView(R.layout.action_bar_progess);
                } else {
                    refreshItem.setActionView(null);
                }
            }
        }
    }
    static final String LOG = FileImportActivity.class.getSimpleName();
    boolean taskImportDone, projectImportDone;
    @Override
    public void onBackPressed() {
        if (taskImportDone) {
            Snackbar.make(fileImportFragment.getList(),"Task data imported OK",Snackbar.LENGTH_LONG).show();
            Intent w = new Intent();
            w.putExtra("taskImportDone",taskImportDone);
            w.putExtra("taskTypeList",response);
            setResult(RESULT_OK, w);
        }
        if (projectImportDone) {
            Snackbar.make(fileImportFragment.getList(),"Project data imported OK",Snackbar.LENGTH_LONG).show();
            Intent w = new Intent();
            w.putExtra("projectImportDone",projectImportDone);
            w.putExtra("projectList",response);
            setResult(RESULT_OK, w);
        }
        finish();
    }
}

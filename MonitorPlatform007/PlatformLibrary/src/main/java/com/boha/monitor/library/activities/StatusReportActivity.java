package com.boha.monitor.library.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.boha.monitor.library.dto.ProjectDTO;
import com.boha.monitor.library.fragments.StatusReportFragment;
import com.boha.monitor.library.util.ThemeChooser;
import com.boha.monitor.library.util.Util;
import com.boha.platform.library.R;

public class StatusReportActivity extends AppCompatActivity
        implements StatusReportFragment.StatusReportListener {
StatusReportFragment statusReportFragment;
    ProjectDTO project;
    boolean siteLocationConfirmed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeChooser.setTheme(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status_report);
        statusReportFragment = (StatusReportFragment) getSupportFragmentManager().findFragmentById(R.id.fragment);
        project = (ProjectDTO) getIntent().getSerializableExtra("project");
        statusReportFragment.setProject(project);


        Util.setCustomActionBar(getApplicationContext(),getSupportActionBar(),
                project.getProjectName(), project.getCityName(),
                ContextCompat.getDrawable(getApplicationContext(),R.drawable.glasses48));
    }


    @Override
    public void onStart() {
        super.onStart();

    }

    @Override
    public void onStop() {
        super.onStop();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        mMenu = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected( MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_refresh) {
            statusReportFragment.getProjectStatus();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onBackPressed() {
        Log.w(LOG, "######### onBackPressed");
        if (siteLocationConfirmed) {
            Intent w = new Intent();
            w.putExtra("projectSite", project);
            setResult(RESULT_OK, w);
        } else {
            setResult(RESULT_CANCELED);
        }

        finish();
    }



    static final String LOG = StatusReportActivity.class.getSimpleName();


    @Override
    public void onPause() {
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        super.onPause();
    }

    Menu mMenu;

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


    @Override
    public void setBusy(boolean busy) {
        setRefreshActionButtonState(busy);
    }
}

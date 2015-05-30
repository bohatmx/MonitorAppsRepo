package com.boha.monitor.library.activities;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.boha.monitor.library.R;
import com.boha.monitor.library.dto.ProjectSiteDTO;
import com.boha.monitor.library.fragments.SiteStatusReportFragment;
import com.boha.monitor.library.util.Util;


public class SiteStatusReportActivity extends AppCompatActivity implements SiteStatusReportFragment.SiteStatusReportListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_site_status_list);
        ctx = getApplicationContext();
        statusReportFragment = (SiteStatusReportFragment) getSupportFragmentManager().findFragmentById(R.id.fragment);
        projectSite = (ProjectSiteDTO) getIntent().getSerializableExtra("projectSite");
        statusReportFragment.setProjectSite(projectSite);

        setTitle(ctx.getString(R.string.site_colon) + projectSite.getProjectSiteName());
        getSupportActionBar().setSubtitle(projectSite.getProjectName());
    }

    Context ctx;
    ProjectSiteDTO projectSite;
    SiteStatusReportFragment statusReportFragment;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.status_report, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected( MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
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
    public void onNoDataAvailable() {

        Util.showErrorToast(ctx, ctx.getString(R.string.status_not));
        finish();
    }
}

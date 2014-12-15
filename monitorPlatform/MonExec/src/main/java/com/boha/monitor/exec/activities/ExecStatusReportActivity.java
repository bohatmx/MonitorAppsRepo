package com.boha.monitor.exec.activities;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.boha.monitor.exec.R;
import com.com.boha.monitor.library.dto.ProjectDTO;
import com.com.boha.monitor.library.dto.ProjectSiteDTO;
import com.com.boha.monitor.library.dto.ProjectSiteTaskStatusDTO;
import com.com.boha.monitor.library.dto.transfer.RequestDTO;
import com.com.boha.monitor.library.dto.transfer.ResponseDTO;
import com.com.boha.monitor.library.fragments.ExecProjectSiteListFragment;
import com.com.boha.monitor.library.fragments.ExecProjectSiteStatusListFragment;
import com.com.boha.monitor.library.util.CacheUtil;
import com.com.boha.monitor.library.util.Statics;
import com.com.boha.monitor.library.util.Util;
import com.com.boha.monitor.library.util.WebCheck;
import com.com.boha.monitor.library.util.WebCheckResult;
import com.com.boha.monitor.library.util.WebSocketUtil;

public class ExecStatusReportActivity extends ActionBarActivity
        implements ExecProjectSiteListFragment.ExecProjectSiteListListener,
        ExecProjectSiteStatusListFragment.ExecProjectSiteStatusListListener{

    ProjectDTO project;
    Context ctx;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exec_status_report);
        ctx = getApplicationContext();
        project = (ProjectDTO)getIntent().getSerializableExtra("project");
        execProjectSiteListFragment = (ExecProjectSiteListFragment)getSupportFragmentManager()
                .findFragmentById(R.id.EXEC_STAT_fragment1);
        execProjectSiteStatusListFragment = (ExecProjectSiteStatusListFragment)getSupportFragmentManager()
                .findFragmentById(R.id.EXEC_STAT_fragment2);
        execProjectSiteListFragment.setProject(project);

        if (!project.getProjectSiteList().isEmpty()) {
            projectSite = project.getProjectSiteList().get(0);
            getCachedSiteStatus(projectSite);
        }
    }

    ProjectSiteDTO projectSite;
    ExecProjectSiteListFragment execProjectSiteListFragment;
    ExecProjectSiteStatusListFragment execProjectSiteStatusListFragment;
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_exec_status_report, menu);
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
    public void onProjectSiteClicked(ProjectSiteDTO site) {
        getCachedSiteStatus(site);
    }


    private void getCachedSiteStatus(final ProjectSiteDTO site) {
        Log.w(LOG,"## getting site status from local cache");
        CacheUtil.getCachedSiteData(ctx,site.getProjectSiteID(), new CacheUtil.CacheSiteListener() {
            @Override
            public void onSiteReturnedFromCache(ProjectSiteDTO cachedSite) {
                if (cachedSite != null) {
                    execProjectSiteStatusListFragment.setProjectSite(cachedSite);
                }
                WebCheckResult ww = WebCheck.checkNetworkAvailability(ctx);
                if (ww.isWifiConnected()) {
                    //
                    getSiteStatusCloud(projectSite);
                }
            }

            @Override
            public void onDataCached() {

            }

            @Override
            public void onError() {
                Log.e(LOG,"-- cache error, getting data from the cloud if wifi good");
                WebCheckResult ww = WebCheck.checkNetworkAvailability(ctx);
                if (ww.isWifiConnected()) {
                    getSiteStatusCloud(site);
                }
            }
        });



    }

    static final String LOG = ExecStatusReportActivity.class.getSimpleName();
    private void getSiteStatusCloud(ProjectSiteDTO site) {
        Log.w(LOG,"## getting site status from the cloud");
        RequestDTO w = new RequestDTO(RequestDTO.GET_SITE_STATUS);
        w.setProjectSiteID(site.getProjectSiteID());
        WebSocketUtil.sendRequest(ctx, Statics.COMPANY_ENDPOINT,w,new WebSocketUtil.WebSocketListener() {
            @Override
            public void onMessage(final ResponseDTO response) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (response.getStatusCode() > 0) {
                            Util.showErrorToast(ctx,response.getMessage());
                            return;
                        }
                        if (!response.getProjectSiteList().isEmpty()) {
                            execProjectSiteStatusListFragment.setProjectSite(response.getProjectSiteList().get(0));
                            CacheUtil.cacheSiteData(ctx, response.getProjectSiteList().get(0), new CacheUtil.CacheSiteListener() {
                                @Override
                                public void onSiteReturnedFromCache(ProjectSiteDTO site) {

                                }

                                @Override
                                public void onDataCached() {

                                }

                                @Override
                                public void onError() {

                                }
                            });
                        }
                    }
                });
            }

            @Override
            public void onClose() {

            }

            @Override
            public void onError(String message) {

            }
        });
    }

    @Override
    public void onTaskStatusClicked(ProjectSiteTaskStatusDTO status) {

    }
}

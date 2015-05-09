package com.boha.monitor.library.activities;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.boha.monitor.library.R;
import com.boha.monitor.library.dto.CompanyStaffDTO;
import com.boha.monitor.library.dto.ProjectDTO;
import com.boha.monitor.library.dto.ProjectSiteDTO;
import com.boha.monitor.library.dto.ProjectSiteTaskDTO;
import com.boha.monitor.library.dto.transfer.RequestDTO;
import com.boha.monitor.library.dto.transfer.ResponseDTO;
import com.boha.monitor.library.fragments.ExecProjectSiteListFragment;
import com.boha.monitor.library.fragments.ExecProjectSiteStatusListFragment;
import com.boha.monitor.library.services.StatusSyncService;
import com.boha.monitor.library.util.CacheUtil;
import com.boha.monitor.library.util.ErrorUtil;
import com.boha.monitor.library.util.SharedUtil;
import com.boha.monitor.library.util.Statics;
import com.boha.monitor.library.util.Util;
import com.boha.monitor.library.util.WebCheck;
import com.boha.monitor.library.util.WebCheckResult;
import com.boha.monitor.library.util.WebSocketUtil;




public class ExecStatusReportActivity extends AppCompatActivity
        implements ExecProjectSiteListFragment.ExecProjectSiteListListener,
        ExecProjectSiteStatusListFragment.ExecProjectSiteStatusListListener {

    ProjectDTO project;
    Context ctx;
    TextView txtProject, txtStatusCount;
    ProgressBar progressBar;

    @Override
    protected void onCreate( Bundle state) {
        super.onCreate(state);
        Log.i(LOG,"### onCreate");
        setContentView(R.layout.activity_exec_status_report);
        ctx = getApplicationContext();
        txtProject = (TextView) findViewById(R.id.HERO_STATUS_projectName);
        txtStatusCount = (TextView) findViewById(R.id.HERO_STATUS_statusCount);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar.setVisibility(View.GONE);

        if (state != null) {
            project = (ProjectDTO) state.getSerializable("project");
        } else {
            project = (ProjectDTO) getIntent().getSerializableExtra("project");
        }

        txtProject.setText(project.getProjectName());
        if (project.getStatusCount() != null)
            txtStatusCount.setText("" + project.getStatusCount().intValue());
        execProjectSiteListFragment = (ExecProjectSiteListFragment) getSupportFragmentManager()
                .findFragmentById(R.id.EXEC_STAT_fragment1);
        execProjectSiteStatusListFragment = (ExecProjectSiteStatusListFragment) getSupportFragmentManager()
                .findFragmentById(R.id.EXEC_STAT_fragment2);
        execProjectSiteListFragment.setProject(project);

        if (!project.getProjectSiteList().isEmpty()) {
            projectSite = project.getProjectSiteList().get(0);
            getCachedSiteStatus(projectSite);
        }
        //
        setTitle(SharedUtil.getCompany(ctx).getCompanyName());
        CompanyStaffDTO staff = SharedUtil.getCompanyStaff(ctx);
        getSupportActionBar().setSubtitle(staff.getFullName());
        Statics.setRobotoFontLight(ctx,txtProject);

        getCachedProjectData();
    }

    private void getProjectData() {
        RequestDTO w = new RequestDTO(RequestDTO.GET_PROJECT_SITES);
        w.setProjectID(project.getProjectID());

        WebSocketUtil.sendRequest(ctx,Statics.COMPANY_ENDPOINT,w,new WebSocketUtil.WebSocketListener() {
            @Override
            public void onMessage( final ResponseDTO response) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (!ErrorUtil.checkServerError(ctx,response)) {
                            return;
                        }
                        project.setProjectSiteList(response.getProjectSiteList());
                        execProjectSiteListFragment.setProject(project);
                        CacheUtil.cacheProjectData(ctx,response, project.getProjectID(), null);
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
    private void getCachedProjectData() {

        CacheUtil.getCachedProjectData(ctx, project.getProjectID(), new CacheUtil.CacheUtilListener() {
            @Override
            public void onFileDataDeserialized( ResponseDTO response) {
                if (response != null) {
                    if (!response.getProjectSiteList().isEmpty()) {
                        project.setProjectSiteList(response.getProjectSiteList());
                        execProjectSiteListFragment.setProject(project);
                    }
                }
//                WebCheckResult wcr = WebCheck.checkNetworkAvailability(ctx,true);
//                if (wcr.isWifiConnected()) {
//                    getProjectData();
//                } else {
//                    Util.showToast(ctx,ctx.getString(R.string.connect_wifi));
//                }
                getProjectData();
            }

            @Override
            public void onDataCached() {
            }

            @Override
            public void onError() {
                getProjectData();
            }
        });

    }
    ProjectSiteDTO projectSite;
    ExecProjectSiteListFragment execProjectSiteListFragment;
    ExecProjectSiteStatusListFragment execProjectSiteStatusListFragment;

    @Override
    public void onSaveInstanceState( Bundle b) {
        Log.e(LOG, "## onSaveInstanceState");
        b.putSerializable("project",project);
        super.onSaveInstanceState(b);
    }
    @Override
    public void onRestoreInstanceState( Bundle b) {
        Log.w(LOG,"### onRestoreInstanceState");
        project = (ProjectDTO) b.getSerializable("project");
        super.onRestoreInstanceState(b);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_exec_status_report, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected( MenuItem item) {
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
    public void onProjectSiteClicked( ProjectSiteDTO site) {
        projectSite = site;
        getCachedSiteStatus(site);
    }

    @Override
    public void onProjectStatusSyncRequested( final ProjectDTO project) {
        Log.w(LOG, "## onProjectStatusSyncRequested - call service method startSyncService");
        progressBar.setVisibility(View.VISIBLE);
        progressBar.setMax(project.getProjectSiteList().size());
        progressBar.setProgress(0);

        Intent i = new Intent();
        i.putExtra("project", project);
        mService.startSyncService(i, new StatusSyncService.StatusSyncServiceListener() {
            @Override
            public void onStatusSyncComplete(String message) {
                Log.d(LOG,message);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setVisibility(View.GONE);
                        Util.showToast(ctx,getString(R.string.status_download_done));
                    }
                });

            }

            @Override
            public void onSiteSyncComplete( ProjectSiteDTO site, final int count) {
                Log.e(LOG,"++++ onSiteSyncComplete index returned: " + count + " site: " + site.getProjectSiteName());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setProgress(count);
                    }
                });

            }
        });


    }


    private void getCachedSiteStatus( final ProjectSiteDTO site) {
        Log.w(LOG, "## getting site status from local cache: " + site.getProjectSiteName());
        CacheUtil.getCachedSiteData(ctx, site.getProjectSiteID(), new CacheUtil.CacheSiteListener() {
            @Override
            public void onSiteReturnedFromCache( ProjectSiteDTO cachedSite) {
                if (cachedSite != null) {
                    execProjectSiteStatusListFragment.setProjectSite(cachedSite);
                }
//                WebCheckResult ww = WebCheck.checkNetworkAvailability(ctx);
//                if (ww.isWifiConnected()) {
//                    getSiteStatusCloud(projectSite);
//                }
                getSiteStatusCloud(projectSite);
            }

            @Override
            public void onDataCached() {

            }

            @Override
            public void onError() {
                Log.e(LOG, "-- cache error, getting data from the cloud if wifi good");
                WebCheckResult ww = WebCheck.checkNetworkAvailability(ctx);
                if (ww.isWifiConnected()) {
                    getSiteStatusCloud(site);
                }
            }
        });


    }

    static final String LOG = ExecStatusReportActivity.class.getSimpleName();

    private void getSiteStatusCloud( ProjectSiteDTO site) {
        Log.w(LOG, "## getting site status from the cloud");
        RequestDTO w = new RequestDTO(RequestDTO.GET_SITE_STATUS);
        w.setProjectSiteID(site.getProjectSiteID());
        WebSocketUtil.sendRequest(ctx, Statics.COMPANY_ENDPOINT, w, new WebSocketUtil.WebSocketListener() {
            @Override
            public void onMessage( final ResponseDTO response) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (response.getStatusCode() > 0) {
                            Util.showErrorToast(ctx, response.getMessage());
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
    public void onTaskClicked(ProjectSiteTaskDTO task) {

    }
    @Override
    protected void onStart() {
        super.onStart();
        Log.w(LOG, "## onStart Bind to StatusSyncService");
        Intent intent = new Intent(this, StatusSyncService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.e(LOG, "## onStop unBind from StatusSyncService");
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }
    }
    /** Defines callbacks for service binding, passed to bindService() */

    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                        IBinder service) {
            Log.w(LOG, "## ServiceConnection onServiceConnected");
            StatusSyncService.LocalBinder binder = (StatusSyncService.LocalBinder) service;
            mService = ((StatusSyncService.LocalBinder)service).getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            Log.w(LOG, "## onServiceDisconnected");
            mBound = false;
        }
    };
    boolean mBound;
    StatusSyncService mService;

    @Override
    public void onPause() {
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        super.onPause();
    }

}

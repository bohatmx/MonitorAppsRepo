package com.boha.monitor.pmanager;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerTitleStrip;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListPopupWindow;
import android.widget.ProgressBar;

import com.com.boha.monitor.library.activities.MonApp;
import com.com.boha.monitor.library.dto.CompanyDTO;
import com.com.boha.monitor.library.dto.CompanyStaffDTO;
import com.com.boha.monitor.library.dto.ProjectDTO;
import com.com.boha.monitor.library.dto.transfer.RequestDTO;
import com.com.boha.monitor.library.dto.transfer.ResponseDTO;
import com.com.boha.monitor.library.fragments.PageFragment;
import com.com.boha.monitor.library.fragments.ProjectListFragment;
import com.com.boha.monitor.library.fragments.StatusReportFragment;
import com.com.boha.monitor.library.services.PhotoUploadService;
import com.com.boha.monitor.library.services.RequestSyncService;
import com.com.boha.monitor.library.util.CacheUtil;
import com.com.boha.monitor.library.util.ErrorUtil;
import com.com.boha.monitor.library.util.NetUtil;
import com.com.boha.monitor.library.util.SharedUtil;
import com.com.boha.monitor.library.util.Util;
import com.com.boha.monitor.library.util.WebCheck;
import com.com.boha.monitor.library.util.WebCheckResult;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import java.util.ArrayList;
import java.util.List;


public class ProjectPagerActivity extends ActionBarActivity {
    View handle;
    ProgressBar progressBar;
    static final String LOG = ProjectPagerActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project_pager);
        ctx = getApplicationContext();
        handle = findViewById(R.id.TIT_handle);
        mPager = (ViewPager) findViewById(R.id.TIT_pager);
        PagerTitleStrip strip = (PagerTitleStrip) findViewById(R.id.pager_title_strip);
        strip.setVisibility(View.GONE);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar.setVisibility(View.GONE);
        getCachedCompanyData();
        setTitle(SharedUtil.getCompany(ctx).getCompanyName());
        CompanyStaffDTO staff = SharedUtil.getCompanyStaff(ctx);
        getSupportActionBar().setSubtitle(staff.getFullName());

        MonApp app = (MonApp) getApplication();
        Tracker t = app.getTracker(MonApp.TrackerName.APP_TRACKER);

        Log.e(LOG, "### got Tracker form MonApp: " + t.toString());
        t.setScreenName("ProjectPagerActivity");
        t.send(new HitBuilders.AppViewBuilder().build());

//        Thread.setDefaultUncaughtExceptionHandler(
//                new UnhandledExceptionHandler(this));
    }

    private void getCachedCompanyData() {

        progressBar.setVisibility(View.VISIBLE);
        CacheUtil.getCachedData(getApplicationContext(), CacheUtil.CACHE_DATA, new CacheUtil.CacheUtilListener() {

            @Override
            public void onFileDataDeserialized(ResponseDTO r) {
                progressBar.setVisibility(View.GONE);
                if (r.getCompany() != null) {
                    company = r.getCompany();
                    response = r;
                    buildPages();
                } else {
                    getCompanyData();
                }

            }

            @Override
            public void onDataCached() {

            }

            @Override
            public void onError() {
                getCompanyData();
            }
        });

    }

    private CompanyDTO company;

    private void getCompanyData() {
        Log.w(LOG, "############# getCompanyData from the cloud..........");
        RequestDTO w = new RequestDTO();
        w.setRequestType(RequestDTO.GET_COMPANY_DATA);
        w.setCompanyID(SharedUtil.getCompany(ctx).getCompanyID());
        progressBar.setVisibility(View.VISIBLE);

        NetUtil.sendRequest(ctx,w,new NetUtil.NetUtilListener() {
            @Override
            public void onResponse(final ResponseDTO r) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setVisibility(View.GONE);
                        Log.e(LOG, "## getCompanyData responded...statusCode: " + r.getStatusCode());
                        if (!ErrorUtil.checkServerError(ctx, r)) {
                            return;
                        }
                        company = r.getCompany();
                        response = r;
                        buildPages();
                        if (isRefreshing) {
                            isRefreshing = false;
                            statusReportFragment.getProjectStatus();
                        }
                        CacheUtil.cacheData(ctx, r, CacheUtil.CACHE_DATA, new CacheUtil.CacheUtilListener() {
                            @Override
                            public void onFileDataDeserialized(ResponseDTO response) {

                            }

                            @Override
                            public void onDataCached() {
                                Intent i = new Intent(ctx, PhotoUploadService.class);
                                startService(i);
                            }

                            @Override
                            public void onError() {

                            }
                        });


                    }
                });
            }

            @Override
            public void onError(final String message) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setVisibility(View.GONE);
                        Util.showErrorToast(ctx, message);
                    }
                });
            }

            @Override
            public void onWebSocketClose() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setVisibility(View.GONE);
                    }
                });
            }
        });
    }

    Menu mMenu;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.manager_pager, menu);
        mMenu = menu;
        return true;
    }

    boolean isRefreshing;
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_help) {
            Util.showToast(ctx, ctx.getString(R.string.under_cons));
            return true;
        }
        if (id == R.id.action_refresh) {
            isRefreshing = true;
            getCompanyData();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    private void buildPages() {

        pageFragmentList = new ArrayList<>();
        projectListFragment = ProjectListFragment.newInstance(response);
        statusReportFragment = StatusReportFragment.newInstance(response);

        pageFragmentList.add(projectListFragment);
        pageFragmentList.add(statusReportFragment);


        adapter = new PagerAdapter(getSupportFragmentManager());
        mPager.setAdapter(adapter);
        mPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageSelected(int arg0) {
                currentPageIndex = arg0;
                progressBar.setVisibility(View.GONE);

                if (pageFragmentList.get(currentPageIndex) instanceof StatusReportFragment) {
                    statusReportFragment.getCachedStatus();
                }
            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {
            }

            @Override
            public void onPageScrollStateChanged(int arg0) {
            }
        });

    }

    ProjectListFragment projectListFragment;
    StatusReportFragment statusReportFragment;
    PagerAdapter adapter;
    ViewPager mPager;
    Context ctx;
    ResponseDTO response;
    int currentPageIndex;
    ProjectDTO project;
    ListPopupWindow actionsWindow;
    List<String> list;
    static final int NEW_STATUS_EXPECTED = 2937;
    static final int PICTURE_REQUESTED = 9133;

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PICTURE_REQUESTED) {

        }
        if (requestCode == NEW_STATUS_EXPECTED) {
            if (resultCode == RESULT_OK) {
                int count = data.getIntExtra("newStatusDone", 0);
                projectListFragment.updateStatusCount(count);
            }
        }
    }

    private class PagerAdapter extends FragmentStatePagerAdapter {

        public PagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {

            return (Fragment) pageFragmentList.get(i);
        }

        @Override
        public int getCount() {
            return pageFragmentList.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            String title = "Title";

            switch (position) {
                case 0:
                    title = ctx.getResources().getString(R.string.company_projects);
                    break;
                case 1:
                    title = ctx.getString(R.string.status_report);
                    break;

            }
            return title;
        }
    }

    @Override
    public void onPause() {
        overridePendingTransition(com.boha.monitor.library.R.anim.slide_in_left, com.boha.monitor.library.R.anim.slide_out_right);
        super.onPause();
    }

    private List<PageFragment> pageFragmentList;

    @Override
    protected void onStart() {
        super.onStart();
        Log.w(LOG, "## onStart Bind to RequestSyncService and PhotoUploadService");
        Intent intent = new Intent(this, RequestSyncService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

        Intent intent2 = new Intent(this, PhotoUploadService.class);
        bindService(intent2, pConnection, Context.BIND_AUTO_CREATE);


    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.e(LOG, "## onStop unBind from RequestSyncService and PhotoUploadService");
        try {
            if (mBound) {
                unbindService(mConnection);
                mBound = false;
            }
            if (pBound) {
                unbindService(pConnection);
                pBound = false;
            }
        } catch (Exception e) {
            Log.e(LOG, "-- Problem with unbinding services", e);
        }

    }


    boolean mBound, pBound;
    RequestSyncService mService;
    PhotoUploadService pService;

    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            Log.w(LOG, "## RequestSyncService ServiceConnection onServiceConnected");
            RequestSyncService.LocalBinder binder = (RequestSyncService.LocalBinder) service;
            mService = binder.getService();
            mBound = true;
            WebCheckResult wcr = WebCheck.checkNetworkAvailability(ctx, true);
            if (wcr.isWifiConnected()) {
                mService.startSyncCachedRequests(new RequestSyncService.RequestSyncListener() {
                    @Override
                    public void onTasksSynced(int goodResponses, int badResponses) {
                        Log.i(LOG, "** cached requests sync, good: " + goodResponses + " bad: " + badResponses);
                    }

                    @Override
                    public void onError(String message) {
                        Log.e(LOG, message);
                    }
                });
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            Log.w(LOG, "## RequestSyncService onServiceDisconnected");
            mBound = false;
        }
    };

    private ServiceConnection pConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            Log.w(LOG, "## PhotoUploadService ServiceConnection onServiceConnected");
            PhotoUploadService.LocalBinder binder = (PhotoUploadService.LocalBinder) service;
            pService = binder.getService();
            pBound = true;
            WebCheckResult wcr = WebCheck.checkNetworkAvailability(ctx, true);
            if (wcr.isWifiConnected()) {
                pService.uploadCachedPhotos(new PhotoUploadService.UploadListener() {
                    @Override
                    public void onUploadsComplete(int count) {

                    }
                });
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            Log.w(LOG, "## RequestSyncService onServiceDisconnected");
            mBound = false;
        }
    };

    @Override
    public void onBackPressed() {
        AlertDialog.Builder d = new AlertDialog.Builder(this);
        d.setTitle(getString(R.string.confirm_exit))
                .setMessage(getString(R.string.exit_question))
                .setPositiveButton(ctx.getString(R.string.yes), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                })
                .setNegativeButton(ctx.getString(R.string.no), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .show();
    }

}

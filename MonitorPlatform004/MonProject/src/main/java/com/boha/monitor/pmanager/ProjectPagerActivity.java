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
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.com.boha.monitor.library.activities.ClaimAndInvoicePagerActivity;
import com.com.boha.monitor.library.activities.MonitorMapActivity;
import com.com.boha.monitor.library.activities.PictureActivity;
import com.com.boha.monitor.library.activities.PictureRecyclerGridActivity;
import com.com.boha.monitor.library.activities.SitePagerActivity;
import com.com.boha.monitor.library.adapters.DrawerAdapter;
import com.com.boha.monitor.library.dialogs.ProjectDialog;
import com.com.boha.monitor.library.dto.CompanyStaffDTO;
import com.com.boha.monitor.library.dto.ProjectDTO;
import com.com.boha.monitor.library.dto.transfer.PhotoUploadDTO;
import com.com.boha.monitor.library.dto.transfer.RequestDTO;
import com.com.boha.monitor.library.dto.transfer.ResponseDTO;
import com.com.boha.monitor.library.fragments.PageFragment;
import com.com.boha.monitor.library.fragments.ProjectListFragment;
import com.com.boha.monitor.library.fragments.SiteTaskAndStatusAssignmentFragment;
import com.com.boha.monitor.library.fragments.StatusReportFragment;
import com.com.boha.monitor.library.services.RequestSyncService;
import com.com.boha.monitor.library.util.CacheUtil;
import com.com.boha.monitor.library.util.ErrorUtil;
import com.com.boha.monitor.library.util.SharedUtil;
import com.com.boha.monitor.library.util.Statics;
import com.com.boha.monitor.library.util.Util;
import com.com.boha.monitor.library.util.WebCheck;
import com.com.boha.monitor.library.util.WebCheckResult;
import com.com.boha.monitor.library.util.WebSocketUtil;

import java.util.ArrayList;
import java.util.List;


public class ProjectPagerActivity extends ActionBarActivity
        implements ProjectListFragment.ProjectListListener,
        StatusReportFragment.StatusReportListener {

    private DrawerLayout mDrawerLayout;
    private DrawerAdapter mDrawerAdapter;
    private LayoutInflater inflater;
    ProgressBar progressBar;
    static final String LOG = ProjectPagerActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project_pager);
        ctx = getApplicationContext();
        inflater = getLayoutInflater();
        mPager = (ViewPager) findViewById(R.id.TIT_pager);
        if (mPager == null) throw new UnsupportedOperationException("Fucking pager is null");
        PagerTitleStrip strip = (PagerTitleStrip) findViewById(R.id.pager_title_strip);
        strip.setVisibility(View.GONE);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        getCachedCompanyData();
        setTitle(SharedUtil.getCompany(ctx).getCompanyName());
        CompanyStaffDTO staff = SharedUtil.getCompanyStaff(ctx);
        getSupportActionBar().setSubtitle(staff.getFullName());

    }

    private void getCachedCompanyData() {
        CacheUtil.getCachedData(getApplicationContext(), CacheUtil.CACHE_DATA, new CacheUtil.CacheUtilListener() {
            @Override
            public void onFileDataDeserialized(ResponseDTO r) {
                if (r != null) {
                    if (r.getCompany() != null) {
                        response = r;
                        buildPages();
                    } else {
                        getCompanyData();
                    }
                }
                WebCheckResult wcr = WebCheck.checkNetworkAvailability(ctx);
                if (wcr.isWifiConnected()) {
                    Log.w(LOG, "## starting RequestSyncService ...");
                    mService.startSyncCachedRequests();
                    Log.d(LOG,"## waiting a couple of seconds for RequestSyncService to sing and dance");
                    Util.pretendFlash(progressBar, 1000, 2, new Util.UtilAnimationListener() {
                        @Override
                        public void onAnimationEnded() {
                            getCompanyData();
                        }
                    });
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

    private void getCompanyData() {
        Log.w(LOG, "############# getCompanyData.............");
        RequestDTO w = new RequestDTO();
        w.setRequestType(RequestDTO.GET_COMPANY_DATA);
        w.setCompanyID(SharedUtil.getCompany(ctx).getCompanyID());

        progressBar.setVisibility(View.VISIBLE);
        WebSocketUtil.sendRequest(ctx, Statics.COMPANY_ENDPOINT, w, new WebSocketUtil.WebSocketListener() {
            @Override
            public void onMessage(final ResponseDTO r) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setVisibility(View.GONE);
                        Log.e(LOG, "## getCompanyData responded...statusCode: " + r.getStatusCode());
                        if (!ErrorUtil.checkServerError(ctx, r)) {
                            return;
                        }
                        response = r;
                        buildPages();
                        CacheUtil.cacheData(ctx, r, CacheUtil.CACHE_DATA, new CacheUtil.CacheUtilListener() {
                            @Override
                            public void onFileDataDeserialized(ResponseDTO response) {

                            }

                            @Override
                            public void onDataCached() {
                                if (mService != null) {
                                    try {
                                        //mService.startSyncCachedRequests();
                                    } catch (Exception e) {
                                        Log.e(LOG, "startSyncCachedRequests failed", e);
                                    }
                                }
                            }

                            @Override
                            public void onError() {

                            }
                        });

                    }
                });

            }

            @Override
            public void onClose() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setVisibility(View.GONE);
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
        });
    }

    Menu mMenu;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.manager_pager, menu);
        mMenu = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_help) {
            Util.showToast(ctx, ctx.getString(R.string.under_cons));
            return true;
        }
        if (id == R.id.action_refresh) {
            WebCheckResult r = WebCheck.checkNetworkAvailability(ctx);
            if (!r.isWifiConnected()) {
                Util.showToast(ctx, getString(R.string.wifi_not_available));
                return true;
            }
            getCompanyData();
            return true;
        }
        if (id == R.id.action_gallery) {
            Intent i = new Intent(this, PictureRecyclerGridActivity.class);
            startActivity(i);
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
                if (pageFragmentList.get(currentPageIndex) instanceof StatusReportFragment) {
                    statusReportFragment.getProjectStatus();
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

    @Override
    public void onProjectClicked(ProjectDTO project) {

    }

    @Override
    public void onProjectEditDialogRequested(ProjectDTO project) {
        ProjectDialog pd = new ProjectDialog();
        pd.setAction(ProjectDTO.ACTION_UPDATE);
        pd.setContext(ctx);
        pd.setProject(project);
        pd.setListener(new ProjectDialog.ProjectDialogListener() {
            @Override
            public void onProjectAdded(final ProjectDTO project) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        projectListFragment.addProject(project);
                    }
                });
            }

            @Override
            public void onProjectUpdated(ProjectDTO project) {

            }

            @Override
            public void onError(String message) {

            }
        });
        pd.show(getFragmentManager(), "PDIAG");
    }

    @Override
    public void onProjectSitesRequested(ProjectDTO project) {

        Intent i = new Intent(this, SitePagerActivity.class);
        i.putExtra("project", project);
        i.putExtra("type", SiteTaskAndStatusAssignmentFragment.PROJECT_MANAGER);
        startActivityForResult(i, NEW_STATUS_EXPECTED);
        WebCheckResult w = WebCheck.checkNetworkAvailability(ctx);
        if (w.isWifiConnected()) {
            mService.startSyncCachedRequests();
        }


    }

    static final int NEW_STATUS_EXPECTED = 2937;

    @Override
    public void onProjectPictureRequested(ProjectDTO project) {
        Intent i = new Intent(this, PictureActivity.class);
        i.putExtra("type", PhotoUploadDTO.PROJECT_IMAGE);
        i.putExtra("project", project);
        startActivity(i);
    }

    @Override
    public void onGalleryRequested(ProjectDTO project) {
        Intent i = new Intent(this, PictureRecyclerGridActivity.class);
        i.putExtra("project", project);
      //  i.putExtra("type", ImagePagerActivity.PROJECT);
        startActivity(i);
    }

    @Override
    public void onMapRequested(ProjectDTO project) {
        Intent i = new Intent(this, MonitorMapActivity.class);
        i.putExtra("project", project);
        startActivity(i);
    }

    @Override
    public void onClaimsAndInvoicesRequested(ProjectDTO project) {
        Intent i = new Intent(this, ClaimAndInvoicePagerActivity.class);
        i.putExtra("project", project);
        startActivity(i);
    }

    @Override
    public void onStatusReportRequested() {
        mPager.setCurrentItem(1, true);
    }

    static final int PICTURE_REQUESTED = 9133;

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PICTURE_REQUESTED) {
            if (resultCode == RESULT_OK) {

            }
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
                    break;
                case 2:
                    break;
                case 3:
                    break;
                case 4:
                    break;

                default:
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
    private ListView drawerListView;
    private String[] titles;
    private List<String> sTitles = new ArrayList<>();

    @Override
    protected void onStart() {
        super.onStart();
        Log.w(LOG, "## onStart Bind to RequestSyncService and PhotoUploadService");
        Intent intent = new Intent(this, RequestSyncService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);


    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.e(LOG, "## onStop unBind from RequestSyncService and PhotoUploadService");
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }

    }


    boolean mBound;
    RequestSyncService mService;

    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            Log.w(LOG, "## RequestSyncService ServiceConnection onServiceConnected");
            RequestSyncService.LocalBinder binder = (RequestSyncService.LocalBinder) service;
            mService = binder.getService();
            mBound = true;
            mService.startSyncCachedRequests();
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

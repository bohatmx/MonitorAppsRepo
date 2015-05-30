package com.boha.monitor.operations;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.IBinder;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.widget.DrawerLayout;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.boha.monitor.library.activities.AppInvitationActivity;
import com.boha.monitor.library.activities.ProjectSitePagerActivity;
import com.boha.monitor.library.activities.StaffActivity;
import com.boha.monitor.library.activities.StaffPictureActivity;
import com.boha.monitor.library.adapters.DrawerAdapter;
import com.boha.monitor.library.dto.CompanyDTO;
import com.boha.monitor.library.dto.CompanyStaffDTO;
import com.boha.monitor.library.dto.ProjectDTO;
import com.boha.monitor.library.dto.transfer.PhotoUploadDTO;
import com.boha.monitor.library.dto.transfer.RequestDTO;
import com.boha.monitor.library.dto.transfer.ResponseDTO;
import com.boha.monitor.library.fragments.PageFragment;
import com.boha.monitor.library.fragments.ProjectListFragment;
import com.boha.monitor.library.fragments.ProjectStatusTypeListFragment;
import com.boha.monitor.library.fragments.StaffListFragment;
import com.boha.monitor.library.fragments.StatusReportFragment;
import com.boha.monitor.library.fragments.TaskListFragment;
import com.boha.monitor.library.fragments.TaskStatusListFragment;
import com.boha.monitor.library.services.PhotoUploadService;
import com.boha.monitor.library.services.RequestSyncService;
import com.boha.monitor.library.util.CacheUtil;
import com.boha.monitor.library.util.ErrorUtil;
import com.boha.monitor.library.util.NetUtil;
import com.boha.monitor.library.util.SharedUtil;
import com.boha.monitor.library.util.Util;

import java.util.ArrayList;
import java.util.List;

public class DrawerActivity extends AppCompatActivity
        implements NavigationDrawerFragment.NavigationDrawerListener,
                StaffListFragment.CompanyStaffListListener,
        ProjectListFragment.ProjectListFragmentListener{

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    TaskListFragment taskListFragment;
    ProjectStatusTypeListFragment projectStatusTypeListFragment;
    StaffListFragment staffListFragment;
    StatusReportFragment statusReportFragment;
    ProjectListFragment projectListFragment;
    TaskStatusListFragment taskStatusListFragment;
    private CharSequence mTitle;
    ProgressBar progressBar;
    Context ctx;
    Activity activity;
    boolean mBound;
    RequestSyncService mService;
    int themeDarkColor, themePrimaryColor;
    int logo;
    ViewPager mPager;
    boolean isRefreshing;
    CompanyDTO company;
    static final String LOG = DrawerActivity.class.getSimpleName();
    ResponseDTO response;
    List<ProjectDTO> projectList;
    int currentPageIndex;
    static final int NUM_ITEMS = 5;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawer);
        ctx = getApplicationContext();
        activity = this;
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar.setVisibility(View.GONE);
        //change topVIEW TO MATCH APP THEME
        Resources.Theme theme = getTheme();
        TypedValue typedValue = new TypedValue();
        theme.resolveAttribute(R.attr.colorPrimaryDark, typedValue, true);
        themeDarkColor = typedValue.data;
        theme.resolveAttribute(R.attr.colorPrimary, typedValue, true);
        themePrimaryColor = typedValue.data;

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        mNavigationDrawerFragment.setPrimaryDarkColor(themeDarkColor);
        mNavigationDrawerFragment.setPrimaryColor(themePrimaryColor);
        logo = R.drawable.logo;
        //
        mNavigationDrawerFragment.setUp(R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout), NavigationDrawerFragment.FROM_MAIN);
        mPager = (ViewPager) findViewById(R.id.pager);
        mPager.setOffscreenPageLimit(NUM_ITEMS);
        getCachedCompanyData();

//        // Set up the drawer.
//        mNavigationDrawerFragment.setUp(
//                R.id.navigation_drawer,
//                (DrawerLayout) findViewById(R.id.drawer_layout));
    }

    private void getCachedCompanyData() {
        progressBar.setVisibility(View.VISIBLE);
        CacheUtil.getCachedData(getApplicationContext(), CacheUtil.CACHE_DATA, new CacheUtil.CacheUtilListener() {

            @Override
            public void onFileDataDeserialized(ResponseDTO r) {
                progressBar.setVisibility(View.GONE);
                if (r != null) {
                    if (r.getCompany() != null) {
                        company = r.getCompany();
                        response = r;
                        buildPages();

                    }
                }

                getCompanyData();

            }

            @Override
            public void onDataCached() {

            }

            @Override
            public void onError() {
                Log.e(LOG, "## cache onError, getting data from cloud");
                getCompanyData();
            }
        });

    }
    @Override
    public void onSiteListRequested(ProjectDTO project) {
        Intent i = new Intent(this, ProjectSitePagerActivity.class);
        i.putExtra("project", project);
        startActivityForResult(i, SITE_LIST_REQUESTED);
    }

    @Override
    public void onStatusReportRequested() {
        int index = 0;
        for (PageFragment d : pageFragmentList) {
            if (d instanceof StatusReportFragment) {
                break;
            }
            index++;
        }
        mPager.setCurrentItem(index, true);
    }


    /**
     * Adapter to manage fragments in view pager
     */
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
            PageFragment pf = pageFragmentList.get(position);
            if (pf instanceof ProjectListFragment) {
                return getString(R.string.projects);
            }
            if (pf instanceof StatusReportFragment) {
                return getString(R.string.status_report);
            }
            if (pf instanceof StaffListFragment) {
                return getString(R.string.team_member);
            }
            if (pf instanceof TaskListFragment) {
                return getString(R.string.tasks);
            }
            if (pf instanceof ProjectStatusTypeListFragment) {
                return getString(R.string.project_status);
            }

            return "No Title";
        }
    }


    public void onSectionAttached(int number) {
        switch (number) {
            case 1:
                mTitle = getString(R.string.title_section1);
                break;
            case 2:
                mTitle = getString(R.string.title_section2);
                break;
            case 3:
                mTitle = getString(R.string.title_section3);
                break;
        }
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.operations_pager, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }



    private void getCompanyData() {
        Log.w(LOG, "############# getCompanyData from the cloud..........");
        RequestDTO w = new RequestDTO();
        w.setRequestType(RequestDTO.GET_COMPANY_DATA);
        w.setCompanyID(SharedUtil.getCompany(ctx).getCompanyID());
        progressBar.setVisibility(View.VISIBLE);

        NetUtil.sendRequest(ctx, w, new NetUtil.NetUtilListener() {
            @Override
            public void onResponse(final ResponseDTO r) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setVisibility(View.GONE);
                        Log.e(LOG, "## getCompanyData responded.....statusCode: " + r.getStatusCode());
                        if (!ErrorUtil.checkServerError(ctx, r)) {
                            return;
                        }
                        company = r.getCompany();
                        if (company == null) {
                            Log.e(LOG, "??? why is this call made???, company is NULL");
                            return;
                        }
                        projectList = company.getProjectList();
                        response = r;
                        buildPages();
                        if (SharedUtil.getLastProjectID(ctx) == null) {
                            if (!projectList.isEmpty()) {
                                SharedUtil.saveLastProjectID(ctx, projectList.get(0).getProjectID());
                            }
                        }

                        statusReportFragment.getProjectStatus();

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
                        Intent i = new Intent(getApplicationContext(), PhotoUploadService.class);
                        startService(i);


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
    private void buildPages() {

        pageFragmentList = new ArrayList<>();
        Bundle data1 = new Bundle();
        data1.putSerializable("response", response);
        data1.putInt("type", ProjectListFragment.OPERATIONS_TYPE);

        projectListFragment = ProjectListFragment.newInstance(
                response, ProjectListFragment.OPERATIONS_TYPE);
        projectListFragment.setPageTitle(getString(R.string.projects));


        staffListFragment = new StaffListFragment();
        staffListFragment.setArguments(data1);
        staffListFragment.setPageTitle(getString(R.string.team_member));

        taskStatusListFragment = new TaskStatusListFragment();
        taskStatusListFragment.setArguments(data1);
        taskStatusListFragment.setPageTitle(getString(R.string.status));

        projectStatusTypeListFragment = new ProjectStatusTypeListFragment();
        projectStatusTypeListFragment.setArguments(data1);
        projectStatusTypeListFragment.setPageTitle(getString(R.string.project_status));

        taskListFragment = new TaskListFragment();
        taskListFragment.setArguments(data1);
        taskListFragment.setPageTitle(getString(R.string.tasks));

        statusReportFragment = new StatusReportFragment();
        statusReportFragment.setArguments(data1);
        statusReportFragment.setPageTitle(getString(R.string.status_report));

        pageFragmentList.add(projectListFragment);
        pageFragmentList.add(statusReportFragment);
        pageFragmentList.add(staffListFragment);
        pageFragmentList.add(taskListFragment);
        pageFragmentList.add(taskStatusListFragment);
        pageFragmentList.add(projectStatusTypeListFragment);


        initializeAdapter();

    }

    private void initializeAdapter() {
        pagerAdapter = new PagerAdapter(getSupportFragmentManager());
        mPager.setAdapter(pagerAdapter);
        mPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageSelected(int arg0) {
                currentPageIndex = arg0;
                PageFragment pf = pageFragmentList.get(currentPageIndex);

                if (pf instanceof TaskListFragment) {
                    taskListFragment.animateHeroHeight();
                }
                if (pf instanceof ProjectStatusTypeListFragment) {
                    projectStatusTypeListFragment.animateHeroHeight();
                }
                if (pf instanceof StaffListFragment) {
                    staffListFragment.animateHeroHeight();
                }
                if (pf instanceof StatusReportFragment) {
                    statusReportFragment.animateHeroHeight();
                }
                if (pf instanceof ProjectListFragment) {
                    projectListFragment.setLastProject();
                    projectListFragment.animateHeroHeight();
                }
                if (pf instanceof TaskStatusListFragment) {
                    taskStatusListFragment.animateHeroHeight();
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


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_refresh) {
            isRefreshing = true;
            getCompanyData();
            return true;
        }
        if (id == R.id.action_help) {
            Util.showErrorToast(ctx, ctx.getString(R.string.under_cons));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestinationSelected(int position, String text) {

    }


    @Override
    public void onNewCompanyStaff() {
        Intent i = new Intent(this, StaffActivity.class);
        startActivityForResult(i, NEW_STAFF_REQUESTED);
    }

    @Override
    public void onCompanyStaffInvitationRequested(List<CompanyStaffDTO> companyStaffList, int index) {

        Intent i = new Intent(this, AppInvitationActivity.class);
        ResponseDTO r = new ResponseDTO();
        r.setCompanyStaffList(companyStaffList);
        i.putExtra("response", r);
        i.putExtra("index", index);
        startActivity(i);
    }


    static final int PICTURE_REQUESTED = 9133, NEW_STAFF_REQUESTED = 9134;
    CompanyStaffDTO companyStaff;

    @Override
    public void onCompanyStaffPictureRequested(CompanyStaffDTO companyStaff) {
        this.companyStaff = companyStaff;
        Intent i = new Intent(this, StaffPictureActivity.class);
        i.putExtra("companyStaff", companyStaff);
        i.putExtra("type", PhotoUploadDTO.STAFF_IMAGE);
        startActivityForResult(i, PICTURE_REQUESTED);
    }

    @Override
    public void onCompanyStaffEditRequested(CompanyStaffDTO companyStaff) {
        Intent i = new Intent(this, StaffActivity.class);
        i.putExtra("companyStaff", companyStaff);
        startActivityForResult(i, EDIT_STAFF_REQUESTED);
    }

    static final int EDIT_STAFF_REQUESTED = 1132;

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.w(LOG, "## onActivityResult, requestCode: " + requestCode);
        if (requestCode == PICTURE_REQUESTED) {
            if (resultCode == RESULT_OK) {
                Log.e(LOG, "############# refresh picture,  stafflist ");
                staffListFragment.refreshList(companyStaff);
            }
        }

        if (requestCode == EDIT_STAFF_REQUESTED) {
            if (resultCode == RESULT_OK) {
                CompanyStaffDTO staff = (CompanyStaffDTO) data.getSerializableExtra("companyStaff");
                staffListFragment.addCompanyStaff(staff);
            }
        }
        if (requestCode == NEW_STATUS_EXPECTED) {
            if (resultCode == RESULT_OK) {
                int count = data.getIntExtra("newStatusDone", 0);
                projectListFragment.updateStatusCount(count);
            }
        }
        if (requestCode == SITE_LIST_REQUESTED) {
            if (resultCode == RESULT_OK) {
                boolean refreshNeeded = data.getBooleanExtra("refreshNeeded", false);
                if (refreshNeeded) {
                    Log.e(LOG, "+++ refreshNeeded: " + refreshNeeded);
                    getCompanyData();
                }
            }
        }
    }
    @Override
    protected void onStart() {
        super.onStart();
        Log.w(LOG, "## onStart Bind to RequestSyncService");
        Intent intent = new Intent(this, RequestSyncService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);


    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.e(LOG, "## onStop unBind from RequestSyncService");
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }

    }

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
    @Override
    public void onPause() {
        overridePendingTransition(com.boha.monitor.library.R.anim.slide_in_left, com.boha.monitor.library.R.anim.slide_out_right);
        super.onPause();
    }


    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            Log.w(LOG, "## RequestSyncService ServiceConnection: onServiceConnected");
            RequestSyncService.LocalBinder binder = (RequestSyncService.LocalBinder) service;
            mService = binder.getService();
            mBound = true;
            mService.startSyncCachedRequests(new RequestSyncService.RequestSyncListener() {
                @Override
                public void onTasksSynced(int goodResponses, int badResponses) {
                    Log.w(LOG, "@@ cached requests done, good: " + goodResponses + " bad: " + badResponses);
                }

                @Override
                public void onError(String message) {

                }
            });

        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            Log.w(LOG, "## RequestSyncService onServiceDisconnected");
            mBound = false;
        }
    };

    List<PageFragment> pageFragmentList;
    PagerAdapter pagerAdapter;
    static final int NEW_STATUS_EXPECTED = 2936;
    static final int SITE_LIST_REQUESTED = 6131;

}

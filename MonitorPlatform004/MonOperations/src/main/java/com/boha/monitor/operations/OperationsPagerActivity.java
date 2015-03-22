package com.boha.monitor.operations;

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
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.com.boha.monitor.library.activities.AppInvitationActivity;
import com.com.boha.monitor.library.activities.ProjectSitePagerActivity;
import com.com.boha.monitor.library.activities.StaffActivity;
import com.com.boha.monitor.library.activities.StaffPictureActivity;
import com.com.boha.monitor.library.adapters.DrawerAdapter;
import com.com.boha.monitor.library.dto.CompanyDTO;
import com.com.boha.monitor.library.dto.CompanyStaffDTO;
import com.com.boha.monitor.library.dto.ProjectDTO;
import com.com.boha.monitor.library.dto.transfer.PhotoUploadDTO;
import com.com.boha.monitor.library.dto.transfer.RequestDTO;
import com.com.boha.monitor.library.dto.transfer.ResponseDTO;
import com.com.boha.monitor.library.fragments.PageFragment;
import com.com.boha.monitor.library.fragments.ProjectListFragment;
import com.com.boha.monitor.library.fragments.ProjectStatusTypeListFragment;
import com.com.boha.monitor.library.fragments.StaffListFragment;
import com.com.boha.monitor.library.fragments.StatusReportFragment;
import com.com.boha.monitor.library.fragments.TaskListFragment;
import com.com.boha.monitor.library.fragments.TaskStatusListFragment;
import com.com.boha.monitor.library.services.PhotoUploadService;
import com.com.boha.monitor.library.services.RequestSyncService;
import com.com.boha.monitor.library.util.CacheUtil;
import com.com.boha.monitor.library.util.ErrorUtil;
import com.com.boha.monitor.library.util.NetUtil;
import com.com.boha.monitor.library.util.SharedUtil;
import com.com.boha.monitor.library.util.Util;

import java.util.ArrayList;
import java.util.List;


public class OperationsPagerActivity extends ActionBarActivity
        implements
        StaffListFragment.CompanyStaffListListener,
        ProjectListFragment.ProjectListFragmentListener {

    private DrawerLayout mDrawerLayout;
    private DrawerAdapter mDrawerAdapter;
    private List<ProjectDTO> projectList;
    ProgressBar progressBar;
    boolean isRefreshing;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.drawer);
        ctx = getApplicationContext();
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mPager = (ViewPager) findViewById(R.id.pager);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar.setVisibility(View.GONE);
        PagerTitleStrip s = (PagerTitleStrip) findViewById(R.id.pager_title_strip);
        s.setVisibility(View.GONE);
        drawerListView = (ListView) findViewById(R.id.left_drawer);
        titles = getResources().getStringArray(R.array.action_items);
        getCachedCompanyData();
        setTitle(SharedUtil.getCompany(ctx).getCompanyName());
        CompanyStaffDTO staff = SharedUtil.getCompanyStaff(ctx);
        getSupportActionBar().setSubtitle(staff.getFullName());


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
                for (String s : titles) {
                    sTitles.add(s);
                }
                mDrawerAdapter = new DrawerAdapter(getApplicationContext(), R.layout.drawer_item, sTitles, company);
                drawerListView.setAdapter(mDrawerAdapter);
                LayoutInflater in = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View v = in.inflate(R.layout.hero_image, null);
                ImageView img = (ImageView) v.findViewById(R.id.HERO_image);
                img.setImageDrawable(Util.getRandomHeroImage(ctx));
                drawerListView.setBackgroundColor(ctx.getResources().getColor(com.boha.monitor.library.R.color.white));
                drawerListView.addHeaderView(v);
                drawerListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        TextView tv = (TextView) view.findViewById(R.id.DI_txtTitle);
                        Log.w(LOG, "##### onItemClick, index: " + i + " title: " + tv.getText().toString());
                        mPager.setCurrentItem(i - 1, true);
                        mDrawerLayout.closeDrawers();
                    }
                });

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

    private CompanyDTO company;

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.operations_pager, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
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


    private void buildPages() {

        pageFragmentList = new ArrayList<>();
        projectListFragment = new ProjectListFragment();
        Bundle data1 = new Bundle();
        data1.putSerializable("response", response);
        data1.putInt("type", ProjectListFragment.OPERATIONS_TYPE);
        projectListFragment.setArguments(data1);

        staffListFragment = new StaffListFragment();
        staffListFragment.setArguments(data1);

        taskStatusListFragment = new TaskStatusListFragment();
        taskStatusListFragment.setArguments(data1);

        projectStatusTypeListFragment = new ProjectStatusTypeListFragment();
        projectStatusTypeListFragment.setArguments(data1);

        taskListFragment = new TaskListFragment();
        taskListFragment.setArguments(data1);

        statusReportFragment = new StatusReportFragment();
        statusReportFragment.setArguments(data1);

        pageFragmentList.add(projectListFragment);
        pageFragmentList.add(statusReportFragment);
        pageFragmentList.add(staffListFragment);
        pageFragmentList.add(taskListFragment);
        pageFragmentList.add(taskStatusListFragment);
        pageFragmentList.add(projectStatusTypeListFragment);


        initializeAdapter();

    }

    private void initializeAdapter() {
        adapter = new PagerAdapter(getSupportFragmentManager());
        mPager.setAdapter(adapter);
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

    ProjectListFragment projectListFragment;
    StaffListFragment staffListFragment;
    TaskStatusListFragment taskStatusListFragment;
    ProjectStatusTypeListFragment projectStatusTypeListFragment;
    TaskListFragment taskListFragment;
    StatusReportFragment statusReportFragment;
    PagerAdapter adapter;
    ViewPager mPager;
    Context ctx;
    ResponseDTO response;
    int currentPageIndex;


    static final int NEW_STATUS_EXPECTED = 2936;


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


    static final String LOG = OperationsPagerActivity.class.getSimpleName();


    static final int SITE_LIST_REQUESTED = 6131;

    @Override
    public void onSiteListRequested(ProjectDTO project) {
        Intent i = new Intent(this, ProjectSitePagerActivity.class);
        i.putExtra("project", project);
        startActivityForResult(i, SITE_LIST_REQUESTED);
    }

    @Override
    public void onStatusReportRequested() {
        int index = 0;
        for (PageFragment d: pageFragmentList) {
            if (d instanceof StatusReportFragment) {
                break;
            }
            index++;
        }
        mPager.setCurrentItem(index, true);
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

            PageFragment pf = pageFragmentList.get(position);
            if (pf instanceof ProjectListFragment) {
                title = ctx.getString(R.string.company_projects);
            }
            if (pf instanceof StaffListFragment) {
                title = ctx.getString(R.string.company_staff);
            }

            if (pf instanceof TaskStatusListFragment) {
                title = ctx.getString(R.string.task_status);
            }
            if (pf instanceof ProjectStatusTypeListFragment) {
                title = ctx.getString(R.string.project_status);
            }
            if (pf instanceof TaskListFragment) {
                title = ctx.getString(R.string.tasks);
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


    boolean mBound;
    RequestSyncService mService;

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

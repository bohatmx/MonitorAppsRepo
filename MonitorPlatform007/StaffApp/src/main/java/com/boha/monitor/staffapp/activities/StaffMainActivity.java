package com.boha.monitor.staffapp.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.PagerTitleStrip;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.boha.monitor.library.activities.GPSActivity;
import com.boha.monitor.library.activities.MonitorMapActivity;
import com.boha.monitor.library.activities.PhotoListActivity;
import com.boha.monitor.library.activities.PictureActivity;
import com.boha.monitor.library.activities.ProjectMapActivity;
import com.boha.monitor.library.activities.StatusReportActivity;
import com.boha.monitor.library.activities.TaskTypeListActivity;
import com.boha.monitor.library.dto.CompanyDTO;
import com.boha.monitor.library.dto.MonitorDTO;
import com.boha.monitor.library.dto.PhotoUploadDTO;
import com.boha.monitor.library.dto.ProjectDTO;
import com.boha.monitor.library.dto.RequestDTO;
import com.boha.monitor.library.dto.ResponseDTO;
import com.boha.monitor.library.dto.StaffDTO;
import com.boha.monitor.library.fragments.MonitorListFragment;
import com.boha.monitor.library.fragments.PageFragment;
import com.boha.monitor.library.fragments.ProjectListFragment;
import com.boha.monitor.library.fragments.StaffFragment;
import com.boha.monitor.library.fragments.StaffListFragment;
import com.boha.monitor.library.fragments.TaskTypeListFragment;
import com.boha.monitor.library.util.CacheUtil;
import com.boha.monitor.library.util.DepthPageTransformer;
import com.boha.monitor.library.util.NetUtil;
import com.boha.monitor.library.util.SharedUtil;
import com.boha.monitor.library.util.Util;
import com.boha.monitor.staffapp.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.List;

public class StaffMainActivity extends AppCompatActivity implements
        MonitorListFragment.MonitorListListener,
        StaffFragment.StaffFragmentListener,
        StaffListFragment.CompanyStaffListListener,
        ProjectListFragment.ProjectListFragmentListener,
        LocationListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    StaffFragment staffFragment;
    MonitorListFragment monitorListFragment;
    StaffListFragment staffListFragment;
    ProjectListFragment projectListFragment;
    ActionBar actionBar;
    Location mLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_staff_main);
        ctx = getApplicationContext();
        Resources.Theme theme = getTheme();
        TypedValue typedValue = new TypedValue();
        theme.resolveAttribute(R.attr.colorPrimaryDark, typedValue, true);
        themeDarkColor = typedValue.data;
        theme.resolveAttribute(R.attr.colorPrimary, typedValue, true);
        themePrimaryColor = typedValue.data;

        actionBar = getSupportActionBar();
        actionBar.setHomeAsUpIndicator(R.drawable.ic_menu);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setLogo(R.drawable.ic_action_globe);


        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navImage = (ImageView) findViewById(R.id.NAVHEADER_image);
        navText = (TextView) findViewById(R.id.NAVHEADER_text);
        navText.setText(SharedUtil.getCompany(ctx).getCompanyName());


        mPager = (ViewPager) findViewById(R.id.viewpager);
        mPager.setOffscreenPageLimit(10);
        PagerTitleStrip strip = (PagerTitleStrip) mPager.findViewById(R.id.pager_title_strip);
        strip.setVisibility(View.GONE);
        strip.setBackgroundColor(themeDarkColor);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Here's a Snackbar", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(themeDarkColor);
            window.setNavigationBarColor(themeDarkColor);
        }

        setMenuDestinations();
        getCache();
        mDrawerLayout.openDrawer(GravityCompat.START);
        setTitle("MonPlatform");
    }

    private void setMenuDestinations() {

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {

                mDrawerLayout.closeDrawers();
                if (menuItem.getItemId() == R.id.nav_project) {
                    mPager.setCurrentItem(0, true);
                    return true;
                }
                if (menuItem.getItemId() == R.id.nav_staff_list) {
                    mPager.setCurrentItem(1, true);
                    return true;
                }
                if (menuItem.getItemId() == R.id.nav_monitors) {
                    mPager.setCurrentItem(2, true);
                    return true;
                }
                if (menuItem.getItemId() == R.id.nav_projectMaps) {
                    Intent w = new Intent(ctx, ProjectMapActivity.class);
                    w.putExtra("type", ProjectMapActivity.STAFF);
                    startActivity(w);

                    return true;
                }


                return false;
            }
        });

    }


    private void getCache() {
        CacheUtil.getCachedStaffData(ctx, new CacheUtil.CacheUtilListener() {
            @Override
            public void onFileDataDeserialized(ResponseDTO r) {
                response = r;
                buildPages();
            }

            @Override
            public void onDataCached() {

            }

            @Override
            public void onError() {

            }
        });
    }

    private void getRemoteStaffData() {
        RequestDTO w = new RequestDTO(RequestDTO.GET_STAFF_DATA);
        w.setStaffID(SharedUtil.getCompanyStaff(ctx).getStaffID());

        companyDataRefreshed = false;
        setRefreshActionButtonState(true);
        NetUtil.sendRequest(getApplicationContext(), w, new NetUtil.NetUtilListener() {
            @Override
            public void onResponse(final ResponseDTO r) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        setRefreshActionButtonState(false);
                        companyDataRefreshed = true;
                        response = r;
                        CacheUtil.cacheStaffData(getApplicationContext(), r, new CacheUtil.CacheUtilListener() {
                            @Override
                            public void onFileDataDeserialized(ResponseDTO response) {

                            }

                            @Override
                            public void onDataCached() {
                                buildPages();
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
                        setRefreshActionButtonState(false);
                        Util.showErrorToast(getApplicationContext(), message);
                    }
                });
            }

            @Override
            public void onWebSocketClose() {

            }
        });
    }

    private void buildPages() {
        pageFragmentList = new ArrayList<>();

        staffFragment = StaffFragment.newInstance(SharedUtil.getCompanyStaff(ctx));
        monitorListFragment = MonitorListFragment.newInstance(response.getMonitorList());
        staffListFragment = StaffListFragment.newInstance(response.getStaffList());
        projectListFragment = ProjectListFragment.newInstance(response);


        pageFragmentList.add(projectListFragment);
        pageFragmentList.add(staffListFragment);
        pageFragmentList.add(monitorListFragment);

        adapter = new CompanyPagerAdapter(getSupportFragmentManager());
        mPager.setAdapter(adapter);
        mPager.setPageTransformer(true, new DepthPageTransformer());
//            mPager.setPageTransformer(true, new ZoomPageTransformer());

        mPager.setCurrentItem(currentPageIndex, true);
        mPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                currentPageIndex = position;
                pageFragmentList.get(position).animateHeroHeight();
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

    }

    protected void startLocationUpdates() {
        Log.d(LOG, "### startLocationUpdates ....");
        if (googleApiClient.isConnected()) {
            mRequestingLocationUpdates = true;
            LocationServices.FusedLocationApi.requestLocationUpdates(
                    googleApiClient, mLocationRequest, this);
            Log.d(LOG, "## GoogleApiClient connected, requesting location updates ...");
        } else {
            Log.e(LOG, "------- GoogleApiClient is NOT connected, not sure where we are...");
            googleApiClient.connect();

        }
    }

    protected void stopLocationUpdates() {
        Log.e(LOG, "### stopLocationUpdates ...");
        if (googleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(
                    googleApiClient, this);
            mRequestingLocationUpdates = false;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        mMenu = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_refresh) {
            getRemoteStaffData();
            return true;
        }
        if (id == R.id.action_help) {
            return true;
        }

        if (id == android.R.id.home) {
            if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
                mDrawerLayout.closeDrawers();
            } else {
                mDrawerLayout.openDrawer(GravityCompat.START);
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onStart() {
        Log.d(LOG,
                "## onStart - GoogleApiClient connecting ... ");
        if (googleApiClient != null) {
            googleApiClient.connect();
        }
//        Log.i(LOG, "## onStart Bind to PhotoUploadService, RequestService");
//        Intent intent = new Intent(this, PhotoUploadService.class);
//        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
//
//        Intent intentw = new Intent(this, RequestService.class);
//        bindService(intentw, rConnection, Context.BIND_AUTO_CREATE);
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (googleApiClient != null) {
            googleApiClient.disconnect();
            Log.e(LOG, "### onStop - locationClient disconnecting ");
        }
//        Log.e(LOG, "## onStop unBind from PhotoUploadService, RequestService");
//        if (mBound) {
//            unbindService(mConnection);
//            mBound = false;
//        }
//        if (rBound) {
//            unbindService(rConnection);
//            rBound = false;
//        }

    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.i(LOG,
                "+++  GoogleApiClient onConnected() ...");
        mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(
                googleApiClient);

        mLocationRequest = LocationRequest.create();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setFastestInterval(500);
        startLocationUpdates();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d(LOG, "onLocationChanged " + location.getLatitude()
                + " " + location.getLongitude() + " " + location.getAccuracy());

        if (location.getAccuracy() <= ACCURACY_THRESHOLD) {
            this.location = location;
            stopLocationUpdates();
            mRequestingLocationUpdates = false;
            if (directionRequired) {
                directionRequired = false;
                onDirectionsRequired(project);
            }

        }

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    @Override
    public void onMonitorSelected(MonitorDTO monitor) {

    }

    @Override
    public void onMonitorPhotoRequired(MonitorDTO monitor) {

    }

    @Override
    public void onMonitorEditRequested(MonitorDTO monitor) {

    }

    @Override
    public void onMessagingRequested(MonitorDTO monitor) {

    }

    @Override
    public void onLocationSendRequired(List<Integer> monitorList, List<Integer> staffList) {

    }


    static final int CHECK_FOR_REFRESH = 3121;
    boolean companyDataRefreshed;

    @Override
    public void onActivityResult(int reqCode, int resCode, Intent data) {

        switch (reqCode) {

            case CHECK_FOR_REFRESH:
                if (resCode == RESULT_OK) {
                    ResponseDTO x = (ResponseDTO) data.getSerializableExtra("response");
                    company.setStaffList(x.getStaffList());
                    company.setMonitorList(x.getMonitorList());
                    company.setProjectStatusTypeList(x.getProjectStatusTypeList());
                    company.setTaskStatusTypeList(x.getTaskStatusTypeList());
                    company.setPortfolioList(x.getPortfolioList());
                    companyDataRefreshed = true;

                }
        }
    }

    @Override
    public void onStaffAdded(StaffDTO companyStaff) {

    }

    @Override
    public void onStaffUpdated(StaffDTO companyStaff) {

    }

    @Override
    public void onAppInvitationRequested(StaffDTO staff, int appType) {

    }

    @Override
    public void onNewCompanyStaff() {

    }

    @Override
    public void onCompanyStaffInvitationRequested(List<StaffDTO> companyStaffList, int index) {

    }

    @Override
    public void onCompanyStaffPictureRequested(StaffDTO companyStaff) {

    }

    @Override
    public void onCompanyStaffEditRequested(StaffDTO companyStaff) {

    }

    static final int REQUEST_CAMERA = 3329, LOCATION_REQUESTED = 9031;

    @Override
    public void onCameraRequired(ProjectDTO project) {
        SharedUtil.saveLastProjectID(ctx, project.getProjectID());
        Intent w = new Intent(this, PictureActivity.class);
        w.putExtra("project", project);
        w.putExtra("type", PhotoUploadDTO.PROJECT_IMAGE);
        startActivityForResult(w, REQUEST_CAMERA);
    }

    @Override
    public void onStatusUpdateRequired(ProjectDTO project) {
        SharedUtil.saveLastProjectID(ctx, project.getProjectID());
        Intent w = new Intent(this, TaskTypeListActivity.class);
        w.putExtra("project", project);
        w.putExtra("darkColor", themeDarkColor);
        w.putExtra("type", TaskTypeListFragment.STAFF);
        startActivity(w);
    }

    Activity activity;

    @Override
    public void onLocationRequired(final ProjectDTO project) {
        SharedUtil.saveLastProjectID(ctx, project.getProjectID());
        activity = this;
        if (project.getLatitude() != null) {
            Intent w = new Intent(this, MonitorMapActivity.class);
            w.putExtra("project", project);
            startActivity(w);
            return;
        }

        AlertDialog.Builder c = new AlertDialog.Builder(this);
        c.setTitle("Project Location")
                .setMessage("You are about to set the location coordinates of the project: " + project.getProjectName() +
                        ". Please step as close as possible to the project and begin GPS scan.")
                .setPositiveButton("Start Scan", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent w = new Intent(activity, GPSActivity.class);
                        w.putExtra("project", project);
                        startActivityForResult(w, LOCATION_REQUESTED);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .show();

    }

    boolean directionRequired;
    ProjectDTO project;

    @Override
    public void onDirectionsRequired(ProjectDTO project) {
        SharedUtil.saveLastProjectID(ctx, project.getProjectID());
        if (project.getLatitude() == null) {
            Util.showErrorToast(ctx, "Project has not been located yet!");
            return;
        }
        if (mLocation == null) {
            directionRequired = true;
            this.project = project;
            startLocationUpdates();
            return;
        }
        Log.i(LOG, "startDirectionsMap ..........");
        String url = "http://maps.google.com/maps?saddr="
                + mLocation.getLatitude() + "," + mLocation.getLongitude()
                + "&daddr=" + project.getLatitude() + "," + project.getLongitude() + "&mode=driving";
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        intent.setClassName("com.google.android.apps.maps",
                "com.google.android.maps.MapsActivity");
        startActivity(intent);
    }

    @Override
    public void onMessagingRequired(ProjectDTO project) {
        SharedUtil.saveLastProjectID(ctx, project.getProjectID());
    }

    @Override
    public void onGalleryRequired(ProjectDTO project) {
        SharedUtil.saveLastProjectID(ctx, project.getProjectID());
        Intent w = new Intent(this, PhotoListActivity.class);
        w.putExtra("project", project);
        startActivity(w);
    }

    @Override
    public void onStatusReportRequired(ProjectDTO project) {
        SharedUtil.saveLastProjectID(ctx, project.getProjectID());
        SharedUtil.saveLastProjectID(ctx, project.getProjectID());
        Intent w = new Intent(this, StatusReportActivity.class);
        w.putExtra("project", project);
        w.putExtra("darkColor", themeDarkColor);
        startActivity(w);
    }

    @Override
    public void onMapRequired(ProjectDTO project) {
        SharedUtil.saveLastProjectID(ctx, project.getProjectID());
    }

    /**
     * Adapter to manage fragments in view pager
     */
    private static class CompanyPagerAdapter extends FragmentStatePagerAdapter {

        public CompanyPagerAdapter(FragmentManager fm) {
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
            return pf.getPageTitle();
        }
    }

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

    static final String LOG = StaffMainActivity.class.getSimpleName();
    static final int ACCURACY_THRESHOLD = 20;
    private DrawerLayout mDrawerLayout;
    CompanyPagerAdapter adapter;
    Context ctx;
    int currentPageIndex;
    Location mCurrentLocation;
    ResponseDTO response;
    PagerTitleStrip strip;
    ViewPager mPager;
    static List<PageFragment> pageFragmentList;
    boolean mRequestingLocationUpdates;
    LocationRequest mLocationRequest;
    GoogleApiClient googleApiClient;
    ProgressBar progressBar;
    int themeDarkColor, themePrimaryColor, logo;
    boolean goToAlerts;
    ImageView navImage;
    TextView navText;
    Location location;
    NavigationView navigationView;
    Menu mMenu;
    CompanyDTO company;
}

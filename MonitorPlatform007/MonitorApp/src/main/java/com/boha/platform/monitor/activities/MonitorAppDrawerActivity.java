package com.boha.platform.monitor.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Resources;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
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

import com.boha.monitor.library.activities.GPSActivity;
import com.boha.monitor.library.activities.MonitorMapActivity;
import com.boha.monitor.library.activities.MonitorPictureActivity;
import com.boha.monitor.library.activities.PhotoListActivity;
import com.boha.monitor.library.activities.PictureActivity;
import com.boha.monitor.library.activities.TaskTypeListActivity;
import com.boha.monitor.library.activities.ThemeSelectorActivity;
import com.boha.monitor.library.dto.ChatMessageDTO;
import com.boha.monitor.library.dto.LocationTrackerDTO;
import com.boha.monitor.library.dto.MonitorDTO;
import com.boha.monitor.library.dto.PhotoUploadDTO;
import com.boha.monitor.library.dto.ProjectDTO;
import com.boha.monitor.library.dto.ProjectTaskDTO;
import com.boha.monitor.library.dto.RequestDTO;
import com.boha.monitor.library.dto.ResponseDTO;
import com.boha.monitor.library.fragments.MessagingFragment;
import com.boha.monitor.library.fragments.MonitorListFragment;
import com.boha.monitor.library.fragments.MonitorProfileFragment;
import com.boha.monitor.library.fragments.PageFragment;
import com.boha.monitor.library.fragments.ProjectListFragment;
import com.boha.monitor.library.services.PhotoUploadService;
import com.boha.monitor.library.services.RequestSyncService;
import com.boha.monitor.library.util.CacheUtil;
import com.boha.monitor.library.util.DepthPageTransformer;
import com.boha.monitor.library.util.NetUtil;
import com.boha.monitor.library.util.SharedUtil;
import com.boha.monitor.library.util.ThemeChooser;
import com.boha.monitor.library.util.Util;
import com.boha.platform.monitor.R;
import com.boha.platform.monitor.fragments.NavigationDrawerFragment;
import com.boha.platform.monitor.fragments.NoProjectsAssignedFragment;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class MonitorAppDrawerActivity extends AppCompatActivity
        implements
        LocationListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        NavigationDrawerFragment.NavigationDrawerListener,
        ProjectListFragment.ProjectListFragmentListener,
        MonitorListFragment.MonitorListListener,
        MessagingFragment.MessagingListener,
        MonitorProfileFragment.MonitorProfileListener {

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;
    int themeDarkColor, themePrimaryColor, logo, currentPageIndex;
    ViewPager mPager;
    PagerAdapter pagerAdapter;
    MessagingFragment messagingFragment;
    MonitorProfileFragment monitorProfileFragment;
    MonitorListFragment monitorListFragment;
    ProjectListFragment projectListFragment;
    NoProjectsAssignedFragment noProjectsAssignedFragment;
    List<PageFragment> pageFragmentList;
    ResponseDTO response;
    Context ctx;

    Location mLocation;
    GoogleApiClient mGoogleApiClient;
    LocationRequest mLocationRequest;
    boolean mRequestingLocationUpdates;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ctx = getApplicationContext();

        ThemeChooser.setTheme(this);
        Resources.Theme theme = getTheme();
        TypedValue typedValue = new TypedValue();
        theme.resolveAttribute(R.attr.colorPrimaryDark, typedValue, true);
        themeDarkColor = typedValue.data;
        theme.resolveAttribute(R.attr.colorPrimary, typedValue, true);
        themePrimaryColor = typedValue.data;


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_drawer);

        final ActionBar ab = getSupportActionBar();
        ab.setHomeAsUpIndicator(R.drawable.ic_menu);
        ab.setDisplayHomeAsUpEnabled(true);


        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        mNavigationDrawerFragment.setPrimaryDarkColor(themeDarkColor);
        mNavigationDrawerFragment.setPrimaryColor(themePrimaryColor);
        logo = R.drawable.ic_action_pin;
        //
        mNavigationDrawerFragment.setUp(R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout), NavigationDrawerFragment.FROM_MAIN);
        mPager = (ViewPager) findViewById(R.id.pager);
        PagerTitleStrip strip = (PagerTitleStrip) findViewById(R.id.pager_title_strip);
        strip.setBackgroundColor(themeDarkColor);
        strip.setVisibility(View.GONE);
        mPager.setOffscreenPageLimit(4);

        getCachedData();
        android.support.v7.app.ActionBar bar = getSupportActionBar();
        bar.setTitle(SharedUtil.getCompany(ctx).getCompanyName());
    }


    private void getCachedData() {
        CacheUtil.getCachedMonitorProjects(ctx, new CacheUtil.CacheUtilListener() {
            @Override
            public void onFileDataDeserialized(ResponseDTO r) {
                if (r.getProjectList() != null && !r.getProjectList().isEmpty()) {
                    response = r;
                    buildPages();
                }
                //
                getRemoteData();

            }

            @Override
            public void onDataCached() {

            }

            @Override
            public void onError() {
                getRemoteData();
            }
        });
    }

    private void getRemoteData() {

        RequestDTO w = new RequestDTO(RequestDTO.GET_MONITOR_PROJECTS);
        w.setMonitorID(SharedUtil.getMonitor(ctx).getMonitorID());

        setRefreshActionButtonState(true);
        Util.setActionBarIconSpinning(mMenu, R.id.action_refresh, true);
        NetUtil.sendRequest(ctx, w, new NetUtil.NetUtilListener() {
            @Override
            public void onResponse(final ResponseDTO r) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        setRefreshActionButtonState(false);
                        response = r;
                        if (response.getStatusCode() > 0) {
                            Util.showErrorToast(ctx, response.getMessage());
                            return;
                        }

                        for (ProjectDTO d : response.getProjectList()) {
                            for (ProjectTaskDTO pt : d.getProjectTaskList()) {
                                pt.setLatitude(d.getLatitude());
                                pt.setLongitude(d.getLongitude());
                            }
                        }
                        buildPages();
                        CacheUtil.cacheMonitorProjects(ctx, response, null);
                    }
                });
            }

            @Override
            public void onError(final String message) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        setRefreshActionButtonState(false);
                        Util.showErrorToast(ctx, message);
                    }
                });
            }

            @Override
            public void onWebSocketClose() {

            }
        });


    }

    private void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
    }


    private void buildPages() {
        pageFragmentList = new ArrayList<>();


        if (!response.getProjectList().isEmpty()) {
            projectListFragment = ProjectListFragment.newInstance(response);
            projectListFragment.setPageTitle(getString(R.string.projects));
            projectListFragment.setThemeColors(themePrimaryColor, themeDarkColor);
        } else {
            noProjectsAssignedFragment = NoProjectsAssignedFragment.newInstance();
            noProjectsAssignedFragment.setPageTitle("No Projects Assigned");
            noProjectsAssignedFragment.setThemeColors(themePrimaryColor,themeDarkColor);

        }

        monitorProfileFragment = MonitorProfileFragment.newInstance(SharedUtil.getMonitor(ctx));
        monitorProfileFragment.setPageTitle(getString(R.string.profile));
        monitorProfileFragment.setThemeColors(themePrimaryColor,themeDarkColor);


        HashMap<Integer, MonitorDTO> map = new HashMap<>();
        for (ProjectDTO dto : response.getProjectList()) {
            for (MonitorDTO x : dto.getMonitorList()) {
                map.put(x.getMonitorID().intValue(), x);
            }
        }
        List<MonitorDTO> list = new ArrayList<>();
        Set<Integer> set = map.keySet();
        for (Integer id : set) {
            list.add(map.get(id));
        }
        monitorListFragment = MonitorListFragment.newInstance(list);
        monitorListFragment.setPageTitle(getString(R.string.monitors));
        monitorListFragment.setThemeColors(themePrimaryColor,themeDarkColor);

        messagingFragment = MessagingFragment.newInstance(list);
        messagingFragment.setPageTitle(getString(R.string.messaging));
        messagingFragment.setThemeColors(themePrimaryColor,themeDarkColor);

        if (!response.getProjectList().isEmpty()) {
            pageFragmentList.add(projectListFragment);
        } else {
            pageFragmentList.add(noProjectsAssignedFragment);
        }
        pageFragmentList.add(monitorProfileFragment);
        pageFragmentList.add(monitorListFragment);
        pageFragmentList.add(messagingFragment);

        pagerAdapter = new PagerAdapter(getSupportFragmentManager());
        mPager.setAdapter(pagerAdapter);

        mPager.setPageTransformer(true, new DepthPageTransformer());

        mPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                currentPageIndex = position;
                PageFragment pf = pageFragmentList.get(currentPageIndex);

                if (pf instanceof ProjectListFragment) {
                    projectListFragment.animateHeroHeight();
                }
                if (pf instanceof MonitorProfileFragment) {
                    monitorProfileFragment.animateHeroHeight();
                }
                if (pf instanceof MessagingFragment) {
                    messagingFragment.animateHeroHeight();
                }
                if (pf instanceof MonitorListFragment) {
                    monitorListFragment.animateHeroHeight();
                }
                if (pf instanceof ProjectListFragment) {
                    projectListFragment.setLastProject();
                    projectListFragment.animateHeroHeight();
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        mPager.setCurrentItem(currentPageIndex);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            getMenuInflater().inflate(R.menu.main_drawer, menu);
            mMenu = menu;
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            Util.showToast(ctx, getString(R.string.under_cons));
            return true;
        }
        if (id == R.id.action_refresh) {
            getRemoteData();
            return true;
        }
        if (id == R.id.action_theme) {
            Intent w = new Intent(this, ThemeSelectorActivity.class);
            w.putExtra("darkColor", themeDarkColor);
            startActivityForResult(w, REQUEST_THEME_CHANGE);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    static final int REQUEST_THEME_CHANGE = 9631, LOCATION_REQUESTED = 6754;
    static final String LOG = MonitorAppDrawerActivity.class.getSimpleName();

    @Override
    public void onActivityResult(int reqCode, int resCode, Intent data) {
        Log.e(LOG, "##------> onActivityResult reqCode: "
                + reqCode + " resCode: " + resCode);
        switch (reqCode) {

            case REQUEST_THEME_CHANGE:
                finish();
                Intent w = new Intent(this, MonitorAppDrawerActivity.class);
                startActivity(w);

                break;

            case LOCATION_REQUESTED:
                if (resCode == RESULT_OK) {
                    Snackbar.make(mPager, "Project location confirmed", Snackbar.LENGTH_LONG).show();
                }
                break;
            case MONITOR_PICTURE_REQUESTED:
                if (resCode == RESULT_OK) {
                    PhotoUploadDTO x = (PhotoUploadDTO) data.getSerializableExtra("photo");
                    monitorProfileFragment.setPicture(x);
                    mNavigationDrawerFragment.setPicture(x);
                }
                break;
        }
    }

    @Override
    public void onDestinationSelected(int position, String text) {

        int index = 0;
        for (PageFragment d : pageFragmentList) {
            if (d.getPageTitle().equalsIgnoreCase(text)) {
                mPager.setCurrentItem(index, true);
                break;
            }
            index++;
        }
    }

    @Override
    public void onMessageSelected(ChatMessageDTO message) {

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
        messagingFragment.animateHeroHeight();
        mPager.setCurrentItem(3, true);
    }

    boolean sendLocation;

    @Override
    public void onLocationSendRequired(List<Integer> monitorList,
                                       List<Integer> staffList) {
        sendLocation = true;
        this.monitorList = monitorList;
        this.staffList = staffList;

        setBusy(true);
        startLocationUpdates();

    }


    static final int REQUEST_CAMERA = 3329;

    @Override
    public void onCameraRequired(ProjectDTO project) {

        Intent w = new Intent(this, PictureActivity.class);
        w.putExtra("project", project);
        w.putExtra("type", PhotoUploadDTO.PROJECT_IMAGE);
        startActivityForResult(w, REQUEST_CAMERA);
    }

    @Override
    public void onStatusUpdateRequired(ProjectDTO project) {
        Log.d("MainDrawerActivity", "&& onStatusUpdateRequired themeDarkColor: " + themeDarkColor);
        Intent w = new Intent(this, TaskTypeListActivity.class);
        w.putExtra("project", project);
        w.putExtra("darkColor", themeDarkColor);
        startActivity(w);

    }

    private Activity activity;

    @Override
    public void onLocationRequired(final ProjectDTO project) {

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

    @Override
    public void onDirectionsRequired(ProjectDTO project) {
        if (project.getLatitude() == null) {
            Util.showErrorToast(ctx, "Project has not been located yet!");
            return;
        }
        Log.i(LOG, "startDirectionsMap ..........");
        String url = "http://maps.google.com/maps?saddr="
                + mLocation.getLatitude() + "," + mLocation.getLongitude()
                + "&daddr=" + project.getLatitude() + "," + project.getLongitude() + "&mode=driving";
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        intent.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
        startActivity(intent);
    }

    @Override
    public void onMessagingRequired(ProjectDTO project) {

    }

    @Override
    public void onGalleryRequired(ProjectDTO project) {

        Intent w = new Intent(this, PhotoListActivity.class);
        w.putExtra("project", project);
        startActivity(w);
    }

    @Override
    public void onStatusReportRequired(ProjectDTO project) {

    }

    @Override
    public void onMapRequired(ProjectDTO project) {

    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.i(LOG,
                "+++  onConnected() -  requestLocationUpdates ...");
        mLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);
        if (mLocation != null) {
            Log.w(LOG, "## requesting location updates ....lastLocation: "
                    + mLocation.getLatitude() + " "
                    + mLocation.getLongitude() + " acc: "
                    + mLocation.getAccuracy());
        }
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setFastestInterval(1000);
//        startLocationUpdates();

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    protected void startLocationUpdates() {
        Log.w(LOG, "###### startLocationUpdates: " + new Date().toString());
        if (mGoogleApiClient.isConnected()) {
            mRequestingLocationUpdates = true;
            LocationServices.FusedLocationApi.requestLocationUpdates(
                    mGoogleApiClient, mLocationRequest, this);
        }
    }

    protected void stopLocationUpdates() {
        Log.w(LOG, "###### stopLocationUpdates - " + new Date().toString());
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(
                    mGoogleApiClient, this);
        }
    }


    @Override
    public void onLocationChanged(Location loc) {
        Log.d(LOG, "## onLocationChanged accuracy = " + loc.getAccuracy()
                + " - " + new Date().toString());

        if (loc.getAccuracy() <= ACCURACY) {
            mLocation = loc;
            stopLocationUpdates();
            if (sendLocation) {
                sendLocation = false;
                submitTrack();
            }
        }
    }

    private void submitTrack() {
        RequestDTO w = new RequestDTO(RequestDTO.SEND_LOCATION);
        LocationTrackerDTO dto = new LocationTrackerDTO();
        MonitorDTO monitor = SharedUtil.getMonitor(ctx);

        dto.setMonitorID(monitor.getMonitorID());
        dto.setDateTracked(new Date().getTime());
        dto.setLatitude(mLocation.getLatitude());
        dto.setLongitude(mLocation.getLongitude());
        dto.setAccuracy(mLocation.getAccuracy());
        dto.setMonitorName(monitor.getFullName());
        dto.setMonitorList(monitorList);
        dto.setStaffList(staffList);
        dto.setGcmDevice(SharedUtil.getGCMDevice(ctx));
        dto.getGcmDevice().setRegistrationID(null);
        w.setLocationTracker(dto);

        setBusy(true);
        NetUtil.sendRequest(ctx, w, new NetUtil.NetUtilListener() {
            @Override
            public void onResponse(ResponseDTO response) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        setBusy(false);
                        Util.showToast(ctx, "Location has been sent");
                    }
                });
            }

            @Override
            public void onError(String message) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        setBusy(false);
                    }
                });
            }

            @Override
            public void onWebSocketClose() {

            }
        });

    }

    List<Integer> monitorList, staffList;

    static final int ACCURACY = 15, MONITOR_PICTURE_REQUESTED = 3412;

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    @Override
    public void onProfileUpdated(MonitorDTO monitor) {

    }

    @Override
    public void onMonitorPictureRequested(MonitorDTO monitor) {

        Intent w = new Intent(this, MonitorPictureActivity.class);
        w.putExtra("monitor", monitor);
        startActivityForResult(w, MONITOR_PICTURE_REQUESTED);
    }

    @Override
    public void setBusy(boolean busy) {
        setRefreshActionButtonState(busy);
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
            PageFragment pf = pageFragmentList.get(position);
            String title = "No Title";
            if (pf instanceof ProjectListFragment) {
                title = getString(R.string.projects);
            }
            if (pf instanceof MonitorListFragment) {
                title = getString(R.string.monitors);
            }
            if (pf instanceof MessagingFragment) {
                title = getString(R.string.messaging);
            }
            if (pf instanceof MonitorProfileFragment) {
                title = getString(R.string.profile);
            }

            return title;
        }
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
    public void onPause() {
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        super.onPause();
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.i(LOG, "## onStart Bind to PhotoUploadService, RequestSyncService");
        Intent intent = new Intent(this, PhotoUploadService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

        Intent intentw = new Intent(this, RequestSyncService.class);
        bindService(intentw, rConnection, Context.BIND_AUTO_CREATE);
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.e(LOG, "## onStop unBind from PhotoUploadService, RequestSyncService");
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }
        if (rBound) {
            unbindService(rConnection);
            rBound = false;
        }

    }

    boolean mBound, rBound;
    PhotoUploadService mService;
    RequestSyncService rService;


    private ServiceConnection rConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            Log.w(LOG, "## RequestSyncService ServiceConnection onServiceConnected");
            RequestSyncService.LocalBinder binder = (RequestSyncService.LocalBinder) service;
            rService = binder.getService();
            rBound = true;
            rService.startSyncCachedRequests(new RequestSyncService.RequestSyncListener() {
                @Override
                public void onTasksSynced(int goodResponses, int badResponses) {
                    Log.i(LOG, "## onTasksSynced, goodResponses: " + goodResponses + " badResponses: " + badResponses);
                }

                @Override
                public void onError(String message) {
                    Log.e(LOG, "Error with sync: " + message);
                }
            });

        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            Log.w(LOG, "## RequestSyncService onServiceDisconnected");
            mBound = false;
        }
    };

    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            Log.w(LOG, "## PhotoUploadService ServiceConnection onServiceConnected");
            PhotoUploadService.LocalBinder binder = (PhotoUploadService.LocalBinder) service;
            mService = binder.getService();
            mBound = true;
            mService.uploadCachedPhotos(new PhotoUploadService.UploadListener() {
                @Override
                public void onUploadsComplete(List<PhotoUploadDTO> list) {
                    Log.w(LOG, "$$$ onUploadsComplete, list: " + list.size());
                }
            });
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            Log.w(LOG, "## PhotoUploadService onServiceDisconnected");
            mBound = false;
        }
    };

}

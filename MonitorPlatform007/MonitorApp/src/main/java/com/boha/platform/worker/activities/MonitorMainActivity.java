package com.boha.platform.worker.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.res.Resources;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
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
import com.boha.monitor.library.activities.MonApp;
import com.boha.monitor.library.activities.PhotoListActivity;
import com.boha.monitor.library.activities.PictureActivity;
import com.boha.monitor.library.activities.ProjectMapActivity;
import com.boha.monitor.library.activities.StatusReportActivity;
import com.boha.monitor.library.activities.ThemeSelectorActivity;
import com.boha.monitor.library.activities.UpdateActivity;
import com.boha.monitor.library.activities.VideoActivity;
import com.boha.monitor.library.dto.ChatMessageDTO;
import com.boha.monitor.library.dto.LocationTrackerDTO;
import com.boha.monitor.library.dto.MonitorDTO;
import com.boha.monitor.library.dto.Person;
import com.boha.monitor.library.dto.PhotoUploadDTO;
import com.boha.monitor.library.dto.ProjectDTO;
import com.boha.monitor.library.dto.RequestDTO;
import com.boha.monitor.library.dto.ResponseDTO;
import com.boha.monitor.library.dto.StaffDTO;
import com.boha.monitor.library.dto.VideoUploadDTO;
import com.boha.monitor.library.fragments.MediaDialogFragment;
import com.boha.monitor.library.fragments.MessagingFragment;
import com.boha.monitor.library.fragments.MonitorListFragment;
import com.boha.monitor.library.fragments.ProfileFragment;
import com.boha.monitor.library.fragments.PageFragment;
import com.boha.monitor.library.fragments.ProjectListFragment;
import com.boha.monitor.library.fragments.SimpleMessageFragment;
import com.boha.monitor.library.fragments.StaffListFragment;
import com.boha.monitor.library.services.DataRefreshService;
import com.boha.monitor.library.services.PhotoUploadService;
import com.boha.monitor.library.services.VideoUploadService;
import com.boha.monitor.library.util.CacheUtil;
import com.boha.monitor.library.util.DepthPageTransformer;
import com.boha.monitor.library.util.NetUtil;
import com.boha.monitor.library.util.SharedUtil;
import com.boha.monitor.library.util.Snappy;
import com.boha.monitor.library.util.ThemeChooser;
import com.boha.monitor.library.util.Util;
import com.boha.platform.worker.R;
import com.boha.platform.worker.fragments.NavigationDrawerFragment;
import com.boha.platform.worker.fragments.NoProjectsAssignedFragment;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * The Monitor App main activity. Started by SignInActivity
 */
public class MonitorMainActivity extends AppCompatActivity
        implements
        LocationListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        SimpleMessageFragment.SimpleMessageFragmentListener,
        NavigationDrawerFragment.NavigationDrawerListener,
        ProjectListFragment.ProjectListFragmentListener,
        MonitorListFragment.MonitorListListener,
        MessagingFragment.MessagingListener,
        StaffListFragment.CompanyStaffListListener,
        ProfileFragment.ProfileListener {

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;
    int themeDarkColor, themePrimaryColor, logo, currentPageIndex;
    ViewPager mPager;
    MonitorPagerAdapter monitorPagerAdapter;
    MessagingFragment messagingFragment;
    ProfileFragment profileFragment;
    MonitorListFragment monitorListFragment;
    ProjectListFragment projectListFragment;
    StaffListFragment staffListFragment;
    NoProjectsAssignedFragment noProjectsAssignedFragment;
    SimpleMessageFragment simpleMessageFragment;
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
        Log.d(LOG, "################## MonitorMainActivity onCreate");
        ThemeChooser.setTheme(this);
        Resources.Theme theme = getTheme();
        TypedValue typedValue = new TypedValue();
        theme.resolveAttribute(R.attr.colorPrimaryDark, typedValue, true);
        themeDarkColor = typedValue.data;
        theme.resolveAttribute(R.attr.colorPrimary, typedValue, true);
        themePrimaryColor = typedValue.data;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_drawer);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .addApi(AppIndex.API).build();

        setFields();
        buildPages();

        //receive notification when DataRefreshService has completed work
        IntentFilter mStatusIntentFilter = new IntentFilter(
                DataRefreshService.BROADCAST_ACTION);
        DataRefreshDoneReceiver receiver = new DataRefreshDoneReceiver();
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, mStatusIntentFilter);
    }

    private void setFields() {
        final ActionBar ab = getSupportActionBar();
        ab.setHomeAsUpIndicator(R.drawable.ic_menu);
        ab.setDisplayHomeAsUpEnabled(true);

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
        strip.setTextColor(ContextCompat.getColor(ctx, R.color.white));
        strip.setVisibility(View.VISIBLE);
        mPager.setOffscreenPageLimit(4);

        Util.setCustomActionBar(ctx, ab,
                SharedUtil.getCompany(ctx).getCompanyName(),
                "Project Monitoring",
                ContextCompat.getDrawable(ctx, R.drawable.glasses));
        mNavigationDrawerFragment.openDrawer();
    }

    @Override
    public void onResume() {
        super.onResume();

    }


    private void getRemoteData() {

        if (busyGettingRemoteData) {
            return;
        }
        RequestDTO w = new RequestDTO(RequestDTO.GET_MONITOR_PROJECTS);
        w.setMonitorID(SharedUtil.getMonitor(ctx).getMonitorID());

        setRefreshActionButtonState(true);
        busyGettingRemoteData = true;
        Snackbar.make(mPager, "Refreshing your data. May take a minute or two ...", Snackbar.LENGTH_LONG).show();

        Intent m = new Intent(ctx,DataRefreshService.class);
        startService(m);

    }


    private void buildPages() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                MonApp app = (MonApp) getApplication();
                pageFragmentList = new ArrayList<>();

                projectListFragment = new ProjectListFragment();
                projectListFragment.setMonApp(app);
                projectListFragment.setPageTitle(getString(R.string.projects));
                projectListFragment.setThemeColors(themePrimaryColor, themeDarkColor);

                profileFragment = new ProfileFragment();
                profileFragment.setMonApp(app);
                profileFragment.setPageTitle(getString(R.string.profile));
                profileFragment.setThemeColors(themePrimaryColor, themeDarkColor);
                profileFragment.setMonitor(SharedUtil.getMonitor(ctx));

                monitorListFragment = new MonitorListFragment();
                monitorListFragment.setMonApp(app);
                monitorListFragment.setPageTitle(getString(R.string.monitors));
                monitorListFragment.setThemeColors(themePrimaryColor, themeDarkColor);

                staffListFragment = new StaffListFragment();
                staffListFragment.setMonApp(app);
                staffListFragment.setPageTitle(getString(R.string.projects));
                staffListFragment.setThemeColors(themePrimaryColor, themeDarkColor);

                pageFragmentList.add(projectListFragment);
                pageFragmentList.add(staffListFragment);
                pageFragmentList.add(monitorListFragment);
                pageFragmentList.add(profileFragment);

                monitorPagerAdapter = new MonitorPagerAdapter(getSupportFragmentManager());
                mPager.setAdapter(monitorPagerAdapter);

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
                        if (pf instanceof ProfileFragment) {
                            profileFragment.animateHeroHeight();
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
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_drawer, menu);
        mMenu = menu;

        checkSettings();
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //todo remove
        if (id == R.id.action_sign_in) {
            Intent w = new Intent(this, SignInActivity.class);
            w.putExtra("force", true);
            startActivity(w);
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
    static final String LOG = MonitorMainActivity.class.getSimpleName();

    @Override
    public void onActivityResult(int reqCode, int resCode, Intent data) {
        Log.d(LOG, "##------> onActivityResult reqCode: "
                + reqCode + " resCode: " + resCode);
        switch (reqCode) {

            case REQUEST_STATUS_UPDATE:

                break;
            case REQUEST_THEME_CHANGE:
                finish();
                Intent w = new Intent(this, MonitorMainActivity.class);
                startActivity(w);

                break;

            case LOCATION_REQUESTED:
                if (resCode == RESULT_OK) {
                    Snackbar.make(mPager, "Project location confirmed", Snackbar.LENGTH_LONG).show();
                }
                break;
            case MONITOR_PICTURE_REQUESTED:
                if (resCode == RESULT_OK) {
                    String x = data.getStringExtra("file");
                    PhotoUploadDTO p = new PhotoUploadDTO();
                    p.setThumbFilePath(x);
                    profileFragment.setPicture(p);
                    mNavigationDrawerFragment.setPicture(p);
                    SharedUtil.savePhoto(ctx, p);
                }
                break;
            default:
                Log.e(LOG, "Switch statement is falling thru");
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
    public void onProjectAssignmentRequested(MonitorDTO monitor) {
        //Not available for Monitor app
    }

    @Override
    public void onMonitorPhotoRequired(MonitorDTO monitor) {

    }

    @Override
    public void onMonitorEditRequested(MonitorDTO monitor) {

    }

    @Override
    public void onMessagingRequested(MonitorDTO monitor) {
        simpleMessageFragment.animateHeroHeight();
        List<MonitorDTO> list = new ArrayList<>();
        list.add(monitor);
        simpleMessageFragment.setMonitorList(list);
        simpleMessageFragment.openMessageToMonitors(list);
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

    @Override
    public void onCompanyStaffInvitationRequested(List<StaffDTO> companyStaffList, int index) {

    }

    @Override
    public void onStaffPictureRequested(StaffDTO companyStaff) {

    }

    @Override
    public void onStaffEditRequested(StaffDTO companyStaff) {

    }


    static final int REQUEST_CAMERA_PHOTO = 3329,
            REQUEST_CAMERA_VIDEO = 3339,
            REQUEST_STATUS_UPDATE = 9687;

    @Override
    public void onCameraRequired(final ProjectDTO project) {
        SharedUtil.saveLastProjectID(ctx, project.getProjectID());

        MediaDialogFragment mdf = new MediaDialogFragment();
        mdf.setListener(new MediaDialogFragment.MediaDialogListener() {
            @Override
            public void onVideoSelected() {
                Intent w = new Intent(getApplicationContext(), VideoActivity.class);
                w.putExtra("project", project);
                startActivityForResult(w, REQUEST_CAMERA_VIDEO);
            }

            @Override
            public void onPhotoSelected() {

                Intent w = new Intent(getApplicationContext(), PictureActivity.class);
                w.putExtra("project", project);
                w.putExtra("type", PhotoUploadDTO.PROJECT_IMAGE);
                startActivityForResult(w, REQUEST_CAMERA_PHOTO);
            }
        });
        mdf.show(getSupportFragmentManager(), "xxx");

    }

    @Override
    public void onStatusUpdateRequired(final ProjectDTO project) {
        SharedUtil.saveLastProjectID(ctx, project.getProjectID());

        CacheUtil.getCachedMonitorProjects(ctx, new CacheUtil.CacheUtilListener() {
            @Override
            public void onFileDataDeserialized(ResponseDTO response) {
                int type = UpdateActivity.NO_TYPES;
                if (!response.getTaskTypeList().isEmpty()) {
                    type = UpdateActivity.TYPES;
                }
                Intent w = new Intent(getApplicationContext(),
                        UpdateActivity.class);
                w.putExtra("project", project);
                w.putExtra("darkColor", themeDarkColor);
                w.putExtra("type", type);
                startActivityForResult(w, REQUEST_STATUS_UPDATE);
            }

            @Override
            public void onDataCached() {

            }

            @Override
            public void onError() {

            }
        });


    }

    private Activity activity;

    @Override
    public void onLocationRequired(final ProjectDTO project) {
        SharedUtil.saveLastProjectID(ctx, project.getProjectID());

        activity = this;
        if (project.getLatitude() != null) {
            Intent w = new Intent(this, ProjectMapActivity.class);
            ResponseDTO responseDTO = new ResponseDTO();
            responseDTO.setProjectList(new ArrayList<ProjectDTO>());
            responseDTO.getProjectList().add(project);
            w.putExtra("projects", responseDTO);
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
        SharedUtil.saveLastProjectID(ctx, project.getProjectID());
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
    public void onProjectTasksRequired(ProjectDTO project) {

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
        Intent w = new Intent(this, StatusReportActivity.class);
        w.putExtra("project", project);
        startActivity(w);
    }

    @Override
    public void onMapRequired(ProjectDTO project) {
        SharedUtil.saveLastProjectID(ctx, project.getProjectID());
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
        mLocationRequest.setInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setFastestInterval(500);

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


        });

    }

    List<Integer> monitorList, staffList;

    static final int ACCURACY = 20, MONITOR_PICTURE_REQUESTED = 3412;

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }


    @Override
    public void onUpdated(Person person) {

    }

    @Override
    public void onAdded(Person person) {

    }

    @Override
    public void onPictureRequested(Person person) {
        Intent w = new Intent(this, PictureActivity.class);
        w.putExtra("monitor", (MonitorDTO) person);
        startActivityForResult(w, MONITOR_PICTURE_REQUESTED);
    }

    @Override
    public void onNewCompanyStaff() {

    }

    @Override
    public void setBusy(boolean busy) {
        setRefreshActionButtonState(busy);
    }

    @Override
    public void onProjectAssigmentWanted(StaffDTO staff) {

    }

    private class MonitorPagerAdapter extends FragmentStatePagerAdapter {

        public MonitorPagerAdapter(FragmentManager fm) {
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
        Log.i(LOG, "## onStart Binding to PhotoUploadService, VideoUploadService");
        Intent intent = new Intent(this, PhotoUploadService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

        Intent intentz = new Intent(this, VideoUploadService.class);
        bindService(intentz, vConnection, Context.BIND_AUTO_CREATE);

        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
//        // ATTENTION: This was auto-generated to implement the App Indexing API.
//        // See https://g.co/AppIndexing/AndroidStudio for more information.
//        Action viewAction = Action.newAction(
//                Action.TYPE_VIEW, // TODO: choose an action type.
//                "MonitorAppDrawer Page", // TODO: Define a title for the content shown.
//                // TODO: If you have web page content that matches this app activity's content,
//                // make sure this auto-generated web page URL is correct.
//                // Otherwise, set the URL to null.
//                Uri.parse("http://host/path"),
//                // TODO: Make sure this auto-generated app deep link URI is correct.
//                Uri.parse("android-app://com.boha.platform.monitor.activities/http/host/path")
//        );
//        AppIndex.AppIndexApi.start(mGoogleApiClient, viewAction);
    }

    @Override
    public void onStop() {
        super.onStop();
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
//        Action viewAction = Action.newAction(
//                Action.TYPE_VIEW, // TODO: choose an action type.
//                "MonitorAppDrawer Page", // TODO: Define a title for the content shown.
//                // TODO: If you have web page content that matches this app activity's content,
//                // make sure this auto-generated web page URL is correct.
//                // Otherwise, set the URL to null.
//                Uri.parse("http://host/path"),
//                // TODO: Make sure this auto-generated app deep link URI is correct.
//                Uri.parse("android-app://com.boha.platform.monitor.activities/http/host/path")
//        );
//        AppIndex.AppIndexApi.end(mGoogleApiClient, viewAction);
        Log.e(LOG, "## onStop unBind from PhotoUploadService, RequestSyncService, VideoUploadService");
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }

        if (vBound) {
            unbindService(vConnection);
            vBound = false;
        }

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        mGoogleApiClient.disconnect();
    }

    boolean mBound, rBound, vBound;
    PhotoUploadService mService;
    VideoUploadService vService;


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
                public void onUploadsComplete(final List<PhotoUploadDTO> list) {
                    Log.w(LOG, "$$$ onUploadsComplete, list: " + list.size());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (!list.isEmpty()) {
                                getRemoteData();
                            }
                        }
                    });

                }
            });
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            Log.w(LOG, "## PhotoUploadService onServiceDisconnected");
            mBound = false;
        }
    };

    private ServiceConnection vConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            Log.w(LOG, "## VideoUploadService ServiceConnection onServiceConnected");
            VideoUploadService.LocalBinder binder = (VideoUploadService.LocalBinder) service;
            vService = binder.getService();
            vBound = true;
            vService.uploadCachedVideos(new VideoUploadService.UploadListener() {
                @Override
                public void onUploadsComplete(List<VideoUploadDTO> list) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            setBusy(false);
                        }
                    });
                }

                @Override
                public void onUploadStarted() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            setBusy(true);
                        }
                    });
                }
            });

        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            Log.w(LOG, "## VideoUploadService onServiceDisconnected");
            vBound = false;
        }
    };

    // Broadcast receiver for receiving status updates from DataRefreshService
    private class DataRefreshDoneReceiver extends BroadcastReceiver {

        // Called when the BroadcastReceiver gets an Intent it's registered to receive
        @Override
        public void onReceive(Context context, Intent intent) {
            setRefreshActionButtonState(false);
            Log.e(LOG, "+++++++DataRefreshDoneReceiver onReceive, data must have been refreshed: "
                    + intent.toString());
            Log.d(LOG, "+++++++++++++++++ starting refreshes on all fragments .....");
            projectListFragment.getProjectList();
            monitorListFragment.getMonitorList();
        }
    }

    boolean busyGettingRemoteData;

    private boolean checkSettings() {


        LocationManager service = (LocationManager) getSystemService(LOCATION_SERVICE);
        boolean gpsEnabled = service.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean networkEnabled = service.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        Log.i(LOG, "GPS enabled: " + gpsEnabled + " networkEnabled: " + networkEnabled);

        if (!isLocationEnabled()) {
            Log.e(LOG, "extra check - isLocationEnabled: " + false);
        }
        if (!gpsEnabled && !networkEnabled && !isLocationEnabled()) {
            showSettingDialog();
        } else {
            return true;
        }
        return true;
    }

    public void showSettingDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("Location Setting");
        builder.setMessage("The app needs Location setting to be turned on so that it can start the river search." +
                "\n\nDo you want to turn it on?");

        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent("com.google.android.gms.location.settings.GOOGLE_LOCATION_SETTINGS");
                startActivity(intent);
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.show();
    }

    public boolean isLocationEnabled() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            try {
                int locationMode = Settings.Secure.getInt(getContentResolver(), Settings.Secure.LOCATION_MODE);
                return locationMode != Settings.Secure.LOCATION_MODE_OFF;

            } catch (Settings.SettingNotFoundException e) {
                e.printStackTrace();
            }

        } else {
            String locationProviders = Settings.Secure.getString(getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
            if (locationProviders == null || locationProviders.equalsIgnoreCase("null") || locationProviders.isEmpty()) {
                return false;
            }

        }

        return false;
    }

}

package com.boha.platform.worker.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
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
import com.boha.monitor.library.activities.YouTubePlayerActivity;
import com.boha.monitor.library.dto.ChatMessageDTO;
import com.boha.monitor.library.dto.LocationTrackerDTO;
import com.boha.monitor.library.dto.MonitorDTO;
import com.boha.monitor.library.dto.Person;
import com.boha.monitor.library.dto.PhotoUploadDTO;
import com.boha.monitor.library.dto.ProjectDTO;
import com.boha.monitor.library.dto.RequestDTO;
import com.boha.monitor.library.dto.ResponseDTO;
import com.boha.monitor.library.dto.SimpleMessageDTO;
import com.boha.monitor.library.dto.StaffDTO;
import com.boha.monitor.library.dto.VideoUploadDTO;
import com.boha.monitor.library.fragments.MediaDialogFragment;
import com.boha.monitor.library.fragments.MessagingFragment;
import com.boha.monitor.library.fragments.MonitorListFragment;
import com.boha.monitor.library.fragments.PageFragment;
import com.boha.monitor.library.fragments.ProfileFragment;
import com.boha.monitor.library.fragments.ProjectListFragment;
import com.boha.monitor.library.fragments.StaffListFragment;
import com.boha.monitor.library.fragments.TaskListFragment;
import com.boha.monitor.library.services.DataRefreshService;
import com.boha.monitor.library.services.LocationTrackerReceiver;
import com.boha.monitor.library.services.PhotoUploadService;
import com.boha.monitor.library.services.YouTubeService;
import com.boha.monitor.library.util.DepthPageTransformer;
import com.boha.monitor.library.util.MonLog;
import com.boha.monitor.library.util.NetUtil;
import com.boha.monitor.library.util.SharedUtil;
import com.boha.monitor.library.util.Snappy;
import com.boha.monitor.library.util.ThemeChooser;
import com.boha.monitor.library.util.Util;
import com.boha.monitor.library.util.WebCheck;
import com.boha.platform.worker.R;
import com.boha.platform.worker.fragments.NavigationDrawerFragment;
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
    List<PageFragment> pageFragmentList;
    ResponseDTO response;
    Context ctx;

    Location mLocation;
    GoogleApiClient mGoogleApiClient;
    LocationRequest mLocationRequest;
    boolean mRequestingLocationUpdates;

    LocationRequestedReceiver locationRequestedReceiver;
    DataRefreshDoneReceiver dataRefreshDoneReceiver;
    PhotoUploadedReceiver photoUploadedReceiver;
    BroadcastReceiver broadcastReceiver;
    YouTubeVideoUploadedReceiver youTubeVideoUploadedReceiver;


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
                .build();

        setFields();
        setBroadcastReceivers();
        checkAirplane();


    }
    static final int AIRPLANE_MODE_SETTINGS = 253;
    private void setBroadcastReceivers() {
        //receive notification when DataRefreshService has completed work
        IntentFilter mStatusIntentFilter = new IntentFilter(
                DataRefreshService.BROADCAST_ACTION);
        dataRefreshDoneReceiver = new DataRefreshDoneReceiver();
        LocalBroadcastManager.getInstance(this).registerReceiver(
                dataRefreshDoneReceiver,mStatusIntentFilter);

        //receive notification when LocationTrackerReceiver has received location request
        IntentFilter mStatusIntentFilter2 = new IntentFilter(
                LocationTrackerReceiver.BROADCAST_ACTION);
        locationRequestedReceiver = new LocationRequestedReceiver();
        LocalBroadcastManager.getInstance(this).registerReceiver(
                locationRequestedReceiver, mStatusIntentFilter2);

        //receive notification when PhotoUploadService has uploaded photos
        IntentFilter mStatusIntentFilter3 = new IntentFilter(
                PhotoUploadService.BROADCAST_ACTION);
        photoUploadedReceiver = new PhotoUploadedReceiver();
        LocalBroadcastManager.getInstance(this).registerReceiver(
                photoUploadedReceiver,
                mStatusIntentFilter3);

        //receive notification of Airplane Mode
        IntentFilter intentFilter = new IntentFilter(
                "android.intent.action.AIRPLANE_MODE");
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.e(LOG, "####### Airplane Mode state changed, intent: " + intent.toString());
                checkAirplane();
            }
        };
        registerReceiver(broadcastReceiver, intentFilter);

        //receive notification when YouTubeService has uploaded videos
        IntentFilter mStatusIntentFilter4 = new IntentFilter(
                YouTubeService.BROADCAST_VIDEO_UPLOADED);
        youTubeVideoUploadedReceiver = new YouTubeVideoUploadedReceiver();
        LocalBroadcastManager.getInstance(this).registerReceiver(
                youTubeVideoUploadedReceiver,
                mStatusIntentFilter4);

    }
    private void checkAirplane() {
        if (WebCheck.isAirplaneModeOn(ctx)) {
            AlertDialog.Builder dg = new AlertDialog.Builder(this);
            dg.setTitle("Airplane Mode")
                    .setMessage("The device is in Airplane mode. Do you want to go to Settings to change this?")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            startActivityForResult(
                                    new Intent(Settings.ACTION_AIRPLANE_MODE_SETTINGS), AIRPLANE_MODE_SETTINGS);
                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    })
                    .show();

        }
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
                (DrawerLayout) findViewById(R.id.drawer_layout),
                NavigationDrawerFragment.FROM_MAIN);
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
        getCachedData();

    }


    private void getRemoteData() {

        if (busyGettingRemoteData) {
            return;
        }
        RequestDTO w = new RequestDTO(RequestDTO.GET_MONITOR_PROJECTS);
        w.setMonitorID(SharedUtil.getMonitor(ctx).getMonitorID());

        //setRefreshActionButtonState(true);
        busyGettingRemoteData = true;
        Snackbar.make(mPager, "Refreshing your data. May take a minute or two ...", Snackbar.LENGTH_LONG).show();

        Intent m = new Intent(ctx, DataRefreshService.class);
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
                projectListFragment.setProjectList(response.getProjectList());
                projectListFragment.setPageTitle(getString(R.string.projects));
                projectListFragment.setThemeColors(themePrimaryColor, themeDarkColor);

                profileFragment = new ProfileFragment();
                profileFragment.setMonApp(app);
                profileFragment.setPageTitle(getString(R.string.profile));
                profileFragment.setThemeColors(themePrimaryColor, themeDarkColor);
                profileFragment.setMonitor(SharedUtil.getMonitor(ctx));

                monitorListFragment = new MonitorListFragment();
                monitorListFragment.setMonApp(app);
                monitorListFragment.setMonitorList(response.getMonitorList());
                monitorListFragment.setPageTitle(getString(R.string.monitors));
                monitorListFragment.setThemeColors(themePrimaryColor, themeDarkColor);

                staffListFragment = new StaffListFragment();
                staffListFragment.setMonApp(app);
                staffListFragment.setStaffList(response.getStaffList());
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
                        if (pf instanceof StaffListFragment) {
                            staffListFragment.animateHeroHeight();
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

            case AIRPLANE_MODE_SETTINGS:
                if (WebCheck.isAirplaneModeOn(ctx)) {
                    startActivityForResult(
                            new Intent(android.provider.Settings.ACTION_SETTINGS), AIRPLANE_MODE_SETTINGS);
                }
                break;
            case REQUEST_STATUS_UPDATE:
                if (resCode == RESULT_OK) {
                    boolean statusCompleted =
                            data.getBooleanExtra("statusCompleted", false);
                    if (statusCompleted) {
                        MonLog.e(ctx, LOG, "statusCompleted, projectListFragment.getProjectList();");
                        getCachedData();
                    }
                }
                Log.i(LOG, "+++++++ onActivityResult, back from UpdateActivity. starting loc update");
                startLocationUpdates();
                break;
            case REQUEST_CAMERA_PHOTO:
                if (resCode == RESULT_OK) {
                    ResponseDTO photos = (ResponseDTO) data.getSerializableExtra("photos");
                    Log.w(LOG, "onActivityResult Photos taken: " + photos.getPhotoUploadList().size());
                    startLocationUpdates();
                } else {
                    Log.e(LOG, "onActivityResult, no photo taken");
                }
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
        Log.e(LOG, "onMessagingRequested: " + monitor.getFullName());
    }

    boolean sendLocation, locationSendRequested;

    @Override
    public void onLocationSendRequired(List<Integer> monitorList,
                                       List<Integer> staffList) {
        MonLog.w(ctx,LOG,"..... onLocationSendRequired");
        locationSendRequested = true;
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

        Intent w = new Intent(this, UpdateActivity.class);
        w.putExtra("project", project);
        w.putExtra("darkColor", themeDarkColor);
        w.putExtra("type", TaskListFragment.STAFF);
        startActivityForResult(w, REQUEST_STATUS_UPDATE);


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
    public void onVideoPlayListRequired(ProjectDTO project) {
        SharedUtil.saveLastProjectID(ctx, project.getProjectID());
        Snappy.getProject((MonApp)getApplication(), project.getProjectID(), new Snappy.SnappyProjectListener() {
            @Override
            public void onProjectFound(ProjectDTO project) {
                if (project.getVideoUploadList().isEmpty()) {
                    return;
                }
                Intent w = new Intent(ctx, YouTubePlayerActivity.class);
                ResponseDTO responseDTO = new ResponseDTO();
                responseDTO.setVideoUploadList(project.getVideoUploadList());
                w.putExtra("videoList", responseDTO);
                startActivity(w);
            }

            @Override
            public void onError() {

            }
        });
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
        Intent w = new Intent(ctx, ProjectMapActivity.class);
        ResponseDTO responseDTO = new ResponseDTO();
        responseDTO.setProjectList(new ArrayList<ProjectDTO>());
        responseDTO.getProjectList().add(project);
        w.putExtra("projects", responseDTO);
        startActivity(w);
    }

    @Override
    public void onRefreshRequired() {
        getRemoteData();
    }

    @Override
    public void onPositioningRequired(int position) {

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
        mLocationRequest.setInterval(2000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setFastestInterval(1000);
        startLocationUpdates();

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    protected void startLocationUpdates() {

        if (mGoogleApiClient.isConnected()) {
            mRequestingLocationUpdates = true;
            LocationServices.FusedLocationApi.requestLocationUpdates(
                    mGoogleApiClient, mLocationRequest, this);
            Log.w(LOG, "###### startLocationUpdates, requestLocationUpdates fired: "
                    + new Date().toString());
        } else {
            Log.e(LOG, "********* mGoogleApiClient is NOT CONNECTED ... trying to connect again");
            mGoogleApiClient.connect();
        }
    }

    protected void stopLocationUpdates() {

        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(
                    mGoogleApiClient, this);
            Log.w(LOG, "###### stopLocationUpdates - removeLocationUpdates fired: " + new Date().toString());
        }
    }


    @Override
    public void onLocationChanged(Location loc) {
        Log.d(LOG, "##=====> onLocationChanged accuracy = " + loc.getAccuracy()
                + " - " + new Date().toString());

        if (loc.getAccuracy() <= ACCURACY) {
            mLocation = loc;
            if (projectListFragment == null) {
                return;
            }
            stopLocationUpdates();

            projectListFragment.setLocation(loc);

            if (simpleMessage != null) {
                staffList.clear();
                monitorList.clear();
                submitLocation();
                simpleMessage = null;
                return;
            }
            if (sendLocation) {
                sendLocation = false;
                submitRegularTrack();
                return;
            }
            if (locationSendRequested) {
                locationSendRequested = false;
                submitLocation();
            }

        }
    }

    private void submitLocation() {
        Log.w(LOG, "###### submitLocation");
        RequestDTO w = new RequestDTO(RequestDTO.SEND_LOCATION);
        LocationTrackerDTO dto = new LocationTrackerDTO();
        MonitorDTO monitor = SharedUtil.getMonitor(ctx);

        dto.setMonitorID(monitor.getMonitorID());
        dto.setDateTracked(new Date().getTime());
        dto.setLatitude(mLocation.getLatitude());
        dto.setLongitude(mLocation.getLongitude());
        dto.setAccuracy(mLocation.getAccuracy());
        dto.setMonitorName(monitor.getFullName());
        if (simpleMessage != null) {
            if (simpleMessage.getMonitorID() != null) {
                dto.getMonitorList().add(simpleMessage.getMonitorID());
            }
            if (simpleMessage.getStaffID() != null) {
                dto.getStaffList().add(simpleMessage.getStaffID());
            }
        } else {
            dto.setMonitorList(monitorList);
            dto.setStaffList(staffList);
        }
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
                        simpleMessage = null;
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

    boolean trackUploadBusy;

    private void submitRegularTrack() {
        if (trackUploadBusy) {
            Log.d(LOG, "submitRegularTrack trackUploadBusy .............................");
            return;
        }
        trackUploadBusy = true;
        Log.w(LOG, "############## submitRegularTrack .......");
        RequestDTO w = new RequestDTO(RequestDTO.ADD_LOCATION_TRACK);
        LocationTrackerDTO dto = new LocationTrackerDTO();
        MonitorDTO monitor = SharedUtil.getMonitor(ctx);

        dto.setMonitorID(monitor.getMonitorID());
        dto.setDateTracked(new Date().getTime());
        dto.setLatitude(mLocation.getLatitude());
        dto.setLongitude(mLocation.getLongitude());
        dto.setAccuracy(mLocation.getAccuracy());
        dto.setMonitorName(monitor.getFullName());

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
                        trackUploadBusy = false;
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

    static final int ACCURACY = 50, MONITOR_PICTURE_REQUESTED = 3412;

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
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
            MonLog.d(ctx, LOG, "------- onStart mGoogleApiClient connecting ...");
        }
    }

    @Override
    public void onStop() {
        super.onStop();

        mGoogleApiClient.disconnect();
        MonLog.d(ctx, LOG, "------- onStop mGoogleApiClient disconnected");
        MonLog.w(ctx,LOG,"----------- onStop - Unregister broadcast receivers");
        try {
            MonLog.w(ctx, LOG, "----------- onStop - Unregister broadcast receivers");
            LocalBroadcastManager.getInstance(this).unregisterReceiver(dataRefreshDoneReceiver);
            LocalBroadcastManager.getInstance(this).unregisterReceiver(locationRequestedReceiver);
            LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
            LocalBroadcastManager.getInstance(this).unregisterReceiver(photoUploadedReceiver);
            LocalBroadcastManager.getInstance(this).unregisterReceiver(youTubeVideoUploadedReceiver);

        } catch (Exception e) {
            Log.e(LOG,"Unable to unregister receivers",e);
        }
    }


    // Broadcast receiver for receiving status updates from DataRefreshService
    private class DataRefreshDoneReceiver extends BroadcastReceiver {

        // Called when the BroadcastReceiver gets an Intent it's registered to receive
        @Override
        public void onReceive(Context context, Intent intent) {
            setRefreshActionButtonState(false);
            Log.e(LOG, "@@@@@@@@@@@@@@@@@@@@@@@@ DataRefreshDoneReceiver onReceive, data must have been refreshed: "
                    + intent.toString());
            getCachedData();
        }
    }

    // Broadcast receiver for receiving location request
    private class LocationRequestedReceiver extends BroadcastReceiver {

        // Called when the BroadcastReceiver gets an Intent it's registered to receive
        @Override
        public void onReceive(Context context, Intent intent) {
            simpleMessage = (SimpleMessageDTO) intent.getSerializableExtra("simpleMessage");

            Log.e(LOG, "+++++++++++++++++++++++ LocationRequestedReceiver onReceive, location requested: "
                    + intent.toString());
            sendLocation = true;
            trackUploadBusy = false;
            startLocationUpdates();
        }
    }

    // Broadcast receiver for receiving status updates from PhotoUploadService
    private class PhotoUploadedReceiver extends BroadcastReceiver {

        // Called when the BroadcastReceiver gets an Intent it's registered to receive
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.e(LOG, "*************************** PhotoUploadedReceiver onReceive, photo uploaded: "
                    + intent.toString());
            getCachedData();
            Log.e(LOG, "Photo has been uploaded OK");

        }
    }
    // Broadcast receiver for receiving status updates from YouTubeService
    private class YouTubeVideoUploadedReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            MonLog.e(ctx,LOG, "+++++++ YouTubeVideoUploadedReceiver onReceive, photo uploaded: "
                    + intent.toString());
            MonLog.w(ctx,LOG,
                    "YouTube video has been uploaded OK");
            VideoUploadDTO v = (VideoUploadDTO)intent.getSerializableExtra("video");
            showYouTubeVideoUploaded(v.getYouTubeID());


        }
    }
    private void showYouTubeVideoUploaded(String youTubeID) {
        Util.showToast(ctx,"YouTube video has been uploaded: " + youTubeID);
    }

    SimpleMessageDTO simpleMessage;
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
    MonApp app;
    private void getCachedData() {
        app = (MonApp)getApplicationContext();
        response = new ResponseDTO();
        Snappy.getProjectList(app, new Snappy.SnappyReadListener() {
            @Override
            public void onDataRead(ResponseDTO r) {
                response.setProjectList(r.getProjectList());
                Snappy.getStaffList(app, new Snappy.SnappyReadListener() {
                    @Override
                    public void onDataRead(ResponseDTO r) {
                        response.setStaffList(r.getStaffList());
                        Snappy.getMonitorList(app, new Snappy.SnappyReadListener() {
                            @Override
                            public void onDataRead(ResponseDTO r) {
                                response.setMonitorList(r.getMonitorList());
                                buildPages();
                            }

                            @Override
                            public void onError(String message) {

                            }
                        });
                    }

                    @Override
                    public void onError(String message) {

                    }
                });
            }

            @Override
            public void onError(String message) {

            }
        });
    }
}

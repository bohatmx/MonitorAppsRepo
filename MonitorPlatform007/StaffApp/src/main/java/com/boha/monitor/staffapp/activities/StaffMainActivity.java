package com.boha.monitor.staffapp.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Resources;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.content.ContextCompat;
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
import com.boha.monitor.library.activities.PhotoListActivity;
import com.boha.monitor.library.activities.PictureActivity;
import com.boha.monitor.library.activities.ProfilePhotoActivity;
import com.boha.monitor.library.activities.ProjectMapActivity;
import com.boha.monitor.library.activities.StatusReportActivity;
import com.boha.monitor.library.activities.ThemeSelectorActivity;
import com.boha.monitor.library.activities.UpdateActivity;
import com.boha.monitor.library.activities.VideoActivity;
import com.boha.monitor.library.dto.CompanyDTO;
import com.boha.monitor.library.dto.LocationTrackerDTO;
import com.boha.monitor.library.dto.MonitorDTO;
import com.boha.monitor.library.dto.PhotoUploadDTO;
import com.boha.monitor.library.dto.ProjectDTO;
import com.boha.monitor.library.dto.RequestDTO;
import com.boha.monitor.library.dto.ResponseDTO;
import com.boha.monitor.library.dto.StaffDTO;
import com.boha.monitor.library.fragments.MediaDialogFragment;
import com.boha.monitor.library.fragments.MonitorListFragment;
import com.boha.monitor.library.fragments.PageFragment;
import com.boha.monitor.library.fragments.ProjectListFragment;
import com.boha.monitor.library.fragments.SimpleMessageFragment;
import com.boha.monitor.library.fragments.StaffListFragment;
import com.boha.monitor.library.fragments.StaffProfileFragment;
import com.boha.monitor.library.fragments.TaskTypeListFragment;
import com.boha.monitor.library.services.PhotoUploadService;
import com.boha.monitor.library.services.RequestSyncService;
import com.boha.monitor.library.util.CacheUtil;
import com.boha.monitor.library.util.DepthPageTransformer;
import com.boha.monitor.library.util.NetUtil;
import com.boha.monitor.library.util.SharedUtil;
import com.boha.monitor.library.util.Statics;
import com.boha.monitor.library.util.ThemeChooser;
import com.boha.monitor.library.util.Util;
import com.boha.monitor.staffapp.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class StaffMainActivity extends AppCompatActivity implements
        MonitorListFragment.MonitorListListener,
        StaffProfileFragment.StaffFragmentListener,
        StaffListFragment.CompanyStaffListListener,
        ProjectListFragment.ProjectListFragmentListener,
        SimpleMessageFragment.SimpleMessageFragmentListener,
        LocationListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    StaffProfileFragment staffProfileFragment;
    MonitorListFragment monitorListFragment;
    StaffListFragment staffListFragment;
    ProjectListFragment projectListFragment;
    SimpleMessageFragment simpleMessageFragment;
    ActionBar actionBar;
    Location mLocation;

    @Override
    public void onResume() {
        super.onResume();
        navImage.setImageDrawable(Util.getRandomBackgroundImage(ctx));


    }

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
        setContentView(R.layout.activity_staff_main);

        actionBar = getSupportActionBar();
        actionBar.setHomeAsUpIndicator(R.drawable.ic_menu);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setLogo(R.drawable.ic_action_globe);


        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        navigationView = (NavigationView) findViewById(R.id.nav_view);

        navImage = (ImageView) navigationView.findViewById(R.id.NAVHEADER_image);
        navText = (TextView) navigationView.findViewById(R.id.NAVHEADER_text);
        navText.setText(SharedUtil.getCompanyStaff(ctx).getFullName());
        try {
            Statics.setRobotoFontLight(getApplicationContext(), navText);
            Drawable globe = ContextCompat.getDrawable(ctx, R.drawable.ic_action_globe);
            globe.setColorFilter(themeDarkColor, PorterDuff.Mode.SRC_IN);
            navigationView.getMenu().getItem(0).setIcon(globe);

            Drawable face = ContextCompat.getDrawable(ctx, R.drawable.ic_action_face);
            face.setColorFilter(themeDarkColor, PorterDuff.Mode.SRC_IN);
            navigationView.getMenu().getItem(1).setIcon(face);

            Drawable map = ContextCompat.getDrawable(ctx, R.drawable.ic_action_map);
            map.setColorFilter(themeDarkColor, PorterDuff.Mode.SRC_IN);
            navigationView.getMenu().getItem(2).setIcon(map);

            navigationView.getMenu().getItem(3).getSubMenu().getItem(0).setIcon(face);
            navigationView.getMenu().getItem(3).getSubMenu().getItem(1).setIcon(face);

        } catch (Exception e) {
            Log.e(LOG, "Problem colorizing menu items");
        }


        mPager = (ViewPager) findViewById(R.id.viewpager);
        mPager.setOffscreenPageLimit(4);
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
        mDrawerLayout.openDrawer(GravityCompat.START);
        getCache();
        Util.setCustomActionBar(getApplicationContext(), getSupportActionBar(),
                SharedUtil.getCompany(ctx).getCompanyName(), "Project Monitoring",
                ContextCompat.getDrawable(getApplicationContext(), com.boha.platform.library.R.drawable.glasses48));

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
                if (menuItem.getItemId() == R.id.nav_messaging) {
                    mPager.setCurrentItem(3, true);
                    return true;
                }

                if (menuItem.getItemId() == R.id.nav_profile) {
                    mPager.setCurrentItem(4, true);
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
                    List<ProjectDTO> list = getProjectsLocationConfirmed();
                    if (!list.isEmpty()) {
                        Intent w = new Intent(ctx, ProjectMapActivity.class);
                        w.putExtra("type", ProjectMapActivity.STAFF);
                        ResponseDTO r = new ResponseDTO();
                        r.setProjectList(list);
                        w.putExtra("projects",r);
                        startActivity(w);
                        return true;
                    } else {
                        Util.showToast(getApplicationContext(),"Projects have not been located via GPS");
                    }

                }


                return false;
            }
        });

    }

    private List<ProjectDTO> getProjectsLocationConfirmed() {
        List<ProjectDTO> list = new ArrayList<>();
        for (ProjectDTO m: response.getProjectList()) {
            if (m.getLocationConfirmed() != null) {
                list.add(m);
            }
        }
        return list;
    }
    private void getCache() {
        CacheUtil.getCachedStaffData(ctx, new CacheUtil.CacheUtilListener() {
            @Override
            public void onFileDataDeserialized(ResponseDTO r) {
                response = r;
                if (!response.getProjectList().isEmpty()) {
                    buildPages();
                } else {
                    getRemoteStaffData();
                }

            }

            @Override
            public void onDataCached() {

            }

            @Override
            public void onError() {
                getRemoteStaffData();
            }
        });
    }

    private void getRemoteStaffData() {
        RequestDTO w = new RequestDTO(RequestDTO.GET_STAFF_DATA);
        w.setStaffID(SharedUtil.getCompanyStaff(ctx).getStaffID());

        companyDataRefreshed = false;
        setRefreshActionButtonState(true);
        Snackbar.make(mPager,"Refreshing your data, this may take a minute or two ...", Snackbar.LENGTH_LONG).show();
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
                                if (projectListFragment == null) {
                                    buildPages();
                                } else {
                                    projectListFragment.refreshProjectList(response.getProjectList());
                                    monitorListFragment.refreshMonitorList(response.getMonitorList());
                                    staffListFragment.refreshStaffList(response.getStaffList());
                                }
                                if (r.getProjectList().isEmpty()) {
                                    Util.showErrorToast(ctx, "Projects have not been assigned yet");
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

        staffProfileFragment = StaffProfileFragment.newInstance(SharedUtil.getCompanyStaff(ctx));
        monitorListFragment = MonitorListFragment.newInstance(response.getMonitorList(), MonitorListFragment.STAFF);
        staffListFragment = StaffListFragment.newInstance(response.getStaffList());
        projectListFragment = ProjectListFragment.newInstance(response);
        simpleMessageFragment = new SimpleMessageFragment();


        staffProfileFragment.setThemeColors(themePrimaryColor,themeDarkColor);
        monitorListFragment.setThemeColors(themePrimaryColor,themeDarkColor);
        staffListFragment.setThemeColors(themePrimaryColor,themeDarkColor);
        projectListFragment.setThemeColors(themePrimaryColor,themeDarkColor);
        simpleMessageFragment.setThemeColors(themePrimaryColor,themeDarkColor);

        pageFragmentList.add(projectListFragment);
        pageFragmentList.add(staffListFragment);
        pageFragmentList.add(monitorListFragment);
        pageFragmentList.add(simpleMessageFragment);
        pageFragmentList.add(staffProfileFragment);

        adapter = new StaffPagerAdapter(getSupportFragmentManager());
        mPager.setAdapter(adapter);
        mPager.setPageTransformer(true, new DepthPageTransformer());

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

    static final int THEME_REQUESTED = 1762;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_refresh) {
            getRemoteStaffData();
            return true;
        }
        if (id == R.id.action_theme) {
            Intent w = new Intent(this, ThemeSelectorActivity.class);
            w.putExtra("darkColor", themeDarkColor);
            startActivityForResult(w, THEME_REQUESTED);
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
        Log.i(LOG, "## onStart Bind to PhotoUploadService, RequestService");
        Intent intent = new Intent(this, PhotoUploadService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

        Intent intentw = new Intent(this, RequestSyncService.class);
        bindService(intentw, rConnection, Context.BIND_AUTO_CREATE);
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (googleApiClient != null) {
            googleApiClient.disconnect();
            Log.e(LOG, "### onStop - locationClient disconnecting ");
        }
        Log.e(LOG, "## onStop unBind from PhotoUploadService, RequestService");
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }
        if (rBound) {
            unbindService(rConnection);
            rBound = false;
        }

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
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d(LOG, "onLocationChanged " + location.getLatitude()
                + " " + location.getLongitude() + " " + location.getAccuracy());

        if (location.getAccuracy() <= ACCURACY_THRESHOLD) {
            mLocation = location;
            stopLocationUpdates();
            mRequestingLocationUpdates = false;

            if (directionRequired) {
                directionRequired = false;
                Log.i(LOG, "startDirectionsMap ..........");
                String url = "http://maps.google.com/maps?saddr="
                        + mLocation.getLatitude() + "," + mLocation.getLongitude()
                        + "&daddr=" + project.getLatitude() + "," + project.getLongitude() + "&mode=driving";
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                intent.setClassName("com.google.android.apps.maps",
                        "com.google.android.maps.MapsActivity");
                startActivity(intent);
            }
            if (sendLocation) {
                sendLocation = false;
                submitTrack();
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

    boolean sendLocation;
    List<Integer> monitorList, staffList;
    @Override
    public void onLocationSendRequired(List<Integer> monitorList, List<Integer> staffList) {
        sendLocation = true;
        this.monitorList = monitorList;
        this.staffList = staffList;

        setBusy(true);
        Snackbar.make(mPager,"Getting device GPS coordinates, may take a few seconds ...",Snackbar.LENGTH_LONG).show();
        startLocationUpdates();
    }
    private void submitTrack() {
        RequestDTO w = new RequestDTO(RequestDTO.SEND_LOCATION);
        LocationTrackerDTO dto = new LocationTrackerDTO();
        StaffDTO staff = SharedUtil.getCompanyStaff(ctx);

        dto.setStaffID(staff.getStaffID());
        dto.setDateTracked(new Date().getTime());
        dto.setLatitude(mLocation.getLatitude());
        dto.setLongitude(mLocation.getLongitude());
        dto.setAccuracy(mLocation.getAccuracy());
        dto.setStaffName(staff.getFullName());
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


    static final int CHECK_FOR_REFRESH = 3121, STAFF_PICTURE_REQUESTED = 3472;
    boolean companyDataRefreshed;

    @Override
    public void onActivityResult(int reqCode, int resCode, Intent data) {
        Log.d(LOG, "onActivityResult reqCode " + reqCode + " resCode " + resCode);
        switch (reqCode) {
            case REQUEST_STATUS_UPDATE:
                getCache();
                break;
            case STAFF_PICTURE_REQUESTED:
                if (resCode == RESULT_OK) {
                    PhotoUploadDTO x = (PhotoUploadDTO) data.getSerializableExtra("photo");
                    SharedUtil.savePhoto(getApplicationContext(),x);
                    Log.e(LOG,"photo returned uri: " + x.getUri());
                    staffProfileFragment.setPicture(x);
                }
                break;
            case LOCATION_REQUESTED:
                if (resCode == RESULT_OK) {
                    getRemoteStaffData();
                }
                break;
            case THEME_REQUESTED:
                if (resCode == RESULT_OK) {
                    finish();
                    Intent w = new Intent(this, StaffMainActivity.class);
                    startActivity(w);
                }
                break;
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
    public void setBusy(boolean busy) {
        setRefreshActionButtonState(busy);
    }

    @Override
    public void onStaffPictureRequired(StaffDTO staff) {
        Intent w = new Intent(this, ProfilePhotoActivity.class);
        w.putExtra("staff", staff);
        startActivityForResult(w, STAFF_PICTURE_REQUESTED);
    }

    @Override
    public void onStaffAdded(StaffDTO staff) {

    }

    @Override
    public void onStaffUpdated(StaffDTO staff) {

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

    static final int REQUEST_CAMERA = 3329,
    REQUEST_VIDEO = 3488,
    LOCATION_REQUESTED = 9031, REQUEST_STATUS_UPDATE = 3291;

    @Override
    public void onCameraRequired(final ProjectDTO project) {
        SharedUtil.saveLastProjectID(ctx, project.getProjectID());
        MediaDialogFragment mdf = new MediaDialogFragment();
        mdf.setCancelable(false);
        mdf.setListener(new MediaDialogFragment.MediaDialogListener() {
            @Override
            public void onVideoSelected() {
                Intent w = new Intent(getApplicationContext(), VideoActivity.class);
                w.putExtra("project", project);
                startActivityForResult(w, REQUEST_VIDEO);
            }

            @Override
            public void onPhotoSelected() {

                Intent w = new Intent(getApplicationContext(), PictureActivity.class);
                w.putExtra("project", project);
                w.putExtra("type", PhotoUploadDTO.PROJECT_IMAGE);
                startActivityForResult(w, REQUEST_CAMERA);
            }
        });
        mdf.show(getSupportFragmentManager(), "projectDiag");

    }

    @Override
    public void onStatusUpdateRequired(ProjectDTO project) {
        SharedUtil.saveLastProjectID(ctx, project.getProjectID());
//        Intent w = new Intent(this, TaskTypeListActivity.class);
        Intent w = new Intent(this, UpdateActivity.class);
        w.putExtra("project", project);
        w.putExtra("darkColor", themeDarkColor);
        w.putExtra("type", TaskTypeListFragment.STAFF);
        startActivityForResult(w, REQUEST_STATUS_UPDATE);
    }

    Activity activity;

    @Override
    public void onLocationRequired(final ProjectDTO project) {
        SharedUtil.saveLastProjectID(ctx, project.getProjectID());
        activity = this;
        if (project.getLatitude() != null) {
            AlertDialog.Builder c = new AlertDialog.Builder(this);
            c.setTitle("Project Location")
                    .setMessage("Do you want to update the location of the project?\n\n"
                            + project.getProjectName())
                    .setPositiveButton("Update GPS Location", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent w = new Intent(activity, GPSActivity.class);
                            w.putExtra("project", project);
                            startActivityForResult(w, LOCATION_REQUESTED);
                        }
                    })
                    .setNegativeButton("View Map", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent w = new Intent(activity,ProjectMapActivity.class);
                            w.putExtra("project", project);
                            startActivity(w);
                        }
                    })
                    .show();

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
        directionRequired = true;
        this.project = project;
        startLocationUpdates();

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
    private static class StaffPagerAdapter extends FragmentStatePagerAdapter {

        public StaffPagerAdapter(FragmentManager fm) {
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


    static final String LOG = StaffMainActivity.class.getSimpleName();
    static final int ACCURACY_THRESHOLD = 20;
    private DrawerLayout mDrawerLayout;
    StaffPagerAdapter adapter;
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
    NavigationView navigationView;
    Menu mMenu;
    CompanyDTO company;
}

package com.boha.monitor.supervisor.activities;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
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
import com.boha.monitor.library.activities.DeviceListActivity;
import com.boha.monitor.library.activities.MonApp;
import com.boha.monitor.library.activities.PhotoListActivity;
import com.boha.monitor.library.activities.PictureActivity;
import com.boha.monitor.library.activities.ProfileActivity;
import com.boha.monitor.library.activities.ProjectMapActivity;
import com.boha.monitor.library.activities.ProjectSelectionActivity;
import com.boha.monitor.library.activities.ProjectTaskActivity;
import com.boha.monitor.library.activities.StatusReportActivity;
import com.boha.monitor.library.activities.TaskListActivity;
import com.boha.monitor.library.activities.ThemeSelectorActivity;
import com.boha.monitor.library.activities.UpdateActivity;
import com.boha.monitor.library.activities.VideoActivity;
import com.boha.monitor.library.dto.CompanyDTO;
import com.boha.monitor.library.dto.LocationTrackerDTO;
import com.boha.monitor.library.dto.MonitorDTO;
import com.boha.monitor.library.dto.MonitorProjectDTO;
import com.boha.monitor.library.dto.Person;
import com.boha.monitor.library.dto.PhotoUploadDTO;
import com.boha.monitor.library.dto.ProjectDTO;
import com.boha.monitor.library.dto.RequestDTO;
import com.boha.monitor.library.dto.ResponseDTO;
import com.boha.monitor.library.dto.StaffDTO;
import com.boha.monitor.library.dto.StaffProjectDTO;
import com.boha.monitor.library.fragments.MediaDialogFragment;
import com.boha.monitor.library.fragments.MonitorListFragment;
import com.boha.monitor.library.fragments.ProfileFragment;
import com.boha.monitor.library.fragments.PageFragment;
import com.boha.monitor.library.fragments.ProjectListFragment;
import com.boha.monitor.library.fragments.ProjectTaskFragment;
import com.boha.monitor.library.fragments.SimpleMessageFragment;
import com.boha.monitor.library.fragments.StaffListFragment;
import com.boha.monitor.library.fragments.TaskListFragment;
import com.boha.monitor.library.services.DataRefreshService;
import com.boha.monitor.library.services.PhotoUploadService;
import com.boha.monitor.library.util.DepthPageTransformer;
import com.boha.monitor.library.util.NetUtil;
import com.boha.monitor.library.util.SharedUtil;
import com.boha.monitor.library.util.Snappy;
import com.boha.monitor.library.util.ThemeChooser;
import com.boha.monitor.library.util.Util;
import com.boha.monitor.supervisor.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import hugo.weaving.DebugLog;

/**
 * This class is the main activity that receives control from
 * the SignInActivity. It controls the sliding drawer menu and
 * hosts a ViewPager that contains the UI fragments.
 * Uses a GoogleApiClient object for location requirements
 *
 * @see ProjectListFragment
 * @see MonitorListFragment
 * @see StaffListFragment
 */
public class StaffMainActivity extends AppCompatActivity implements
        MonitorListFragment.MonitorListListener,
        StaffListFragment.CompanyStaffListListener,
        ProjectListFragment.ProjectListFragmentListener,
        ProfileFragment.ProfileListener,
        SimpleMessageFragment.SimpleMessageFragmentListener,
        LocationListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    ProfileFragment profileFragment;
    MonitorListFragment monitorListFragment;
    StaffListFragment staffListFragment;
    ProjectListFragment projectListFragment;
    SimpleMessageFragment simpleMessageFragment;
    ActionBar actionBar;
    Location mLocation;

    @Override
    public void onResume() {
        Log.w(LOG, "++++++++ ############## onResume ");
        super.onResume();
        if (navImage != null) {
            navImage.setImageDrawable(Util.getRandomBackgroundImage(ctx));
        } else {
            Log.e(LOG, "navImage is null");
        }
        buildPages();

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

        setFields();

        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        setMenuDestinations();
        mDrawerLayout.openDrawer(GravityCompat.START);

        //receive notification when DataRefreshService has completed work
        IntentFilter mStatusIntentFilter = new IntentFilter(
                DataRefreshService.BROADCAST_ACTION);
        DataRefreshDoneReceiver receiver = new DataRefreshDoneReceiver();
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver,mStatusIntentFilter);
    }

    private void setFields() {
        actionBar = getSupportActionBar();
        actionBar.setHomeAsUpIndicator(R.drawable.ic_menu);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setLogo(R.drawable.ic_action_globe);


        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        navigationView = (NavigationView) mDrawerLayout.findViewById(R.id.nav_view);

        navImage = (ImageView) navigationView.getHeaderView(0).findViewById(R.id.NAVHEADER_image);
        navText = (TextView) navigationView.getHeaderView(0).findViewById(R.id.NAVHEADER_text);
        if (navText != null) {
            navText.setText(SharedUtil.getCompanyStaff(ctx).getFullName());
        }
//        try {
//            Statics.setRobotoFontLight(getApplicationContext(), navText);
//            Drawable globe = ContextCompat.getDrawable(ctx, R.drawable.ic_action_globe);
//            globe.setColorFilter(themeDarkColor, PorterDuff.Mode.SRC_IN);
//            navigationView.getMenu().getItem(0).setIcon(globe);
//
//            Drawable face = ContextCompat.getDrawable(ctx, R.drawable.ic_action_face);
//            face.setColorFilter(themeDarkColor, PorterDuff.Mode.SRC_IN);
//            navigationView.getMenu().getItem(1).setIcon(face);
//
//            Drawable map = ContextCompat.getDrawable(ctx, R.drawable.ic_action_map);
//            map.setColorFilter(themeDarkColor, PorterDuff.Mode.SRC_IN);
//            navigationView.getMenu().getItem(2).setIcon(map);
//
//
//            navigationView.getMenu().getItem(3).getSubMenu().getItem(0).setIcon(face);
//            navigationView.getMenu().getItem(3).getSubMenu().getItem(1).setIcon(face);
//
//        } catch (Exception e) {
//            Log.e(LOG, "Problem colorizing menu items", e);
//        }


        mPager = (ViewPager) findViewById(R.id.viewpager);
        mPager.setOffscreenPageLimit(4);
        PagerTitleStrip strip = (PagerTitleStrip) mPager.findViewById(R.id.pager_title_strip);
        strip.setVisibility(View.VISIBLE);
        strip.setBackgroundColor(themeDarkColor);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(themeDarkColor);
            window.setNavigationBarColor(themeDarkColor);
        }
        Util.setCustomActionBar(getApplicationContext(), getSupportActionBar(),
                SharedUtil.getCompany(ctx).getCompanyName(), "Project Monitoring",
                ContextCompat.getDrawable(getApplicationContext(), com.boha.platform.library.R.drawable.glasses));

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
                    Snappy.getProjectList((MonApp) getApplication(), new Snappy.SnappyReadListener() {
                        @Override
                        public void onDataRead(ResponseDTO response) {
                            List<ProjectDTO> list = new ArrayList<>();
                            for (ProjectDTO p : response.getProjectList()) {
                                if (p.getLatitude() != null) {
                                    list.add(p);
                                }
                            }
                            if (!list.isEmpty()) {
                                Intent w = new Intent(ctx, ProjectMapActivity.class);
                                w.putExtra("type", ProjectMapActivity.STAFF);
                                ResponseDTO r = new ResponseDTO();
                                r.setProjectList(list);
                                w.putExtra("projects", r);
                                startActivity(w);
                            } else {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Util.showToast(getApplicationContext(), "Projects have not been located via GPS");
                                    }
                                });

                            }

                        }

                        @Override
                        public void onError(String message) {

                        }
                    });
                    return true;

                }
                if (menuItem.getItemId() == R.id.nav_devices) {
                    Intent m = new Intent(ctx, DeviceListActivity.class);
                    startActivity(m);
                }


                return false;
            }
        });

    }

    private List<ProjectDTO> getProjectsLocationConfirmed() {
        List<ProjectDTO> list = new ArrayList<>();
        for (ProjectDTO m : response.getProjectList()) {
            if (m.getLocationConfirmed() != null) {
                list.add(m);
            }
        }
        return list;
    }
//
//    @DebugLog
//    private void getCache() {
//        Snackbar.make(mPager, "Refreshing your data, this may take a minute or two ...", Snackbar.LENGTH_LONG).show();
//        setBusy(true);
//
//        if (response == null) {
//            response = new ResponseDTO();
//        }
//        Snappy.getProjectList(ctx, new Snappy.SnappyReadListener() {
//            @Override
//            public void onDataRead(ResponseDTO r) {
//                response.setProjectList(r.getProjectList());
//                Snappy.getStaffList(ctx, new Snappy.SnappyReadListener() {
//                    @Override
//                    public void onDataRead(ResponseDTO r) {
//                        response.setStaffList(r.getStaffList());
//                        Snappy.getMonitorList(ctx, new Snappy.SnappyReadListener() {
//                            @Override
//                            public void onDataRead(ResponseDTO r) {
//                                response.setTaskStatusTypeList(r.getTaskStatusTypeList());
//                                Log.i(LOG, "Yebo!! we got da data");
//                                buildPages();
//                            }
//
//                            @Override
//                            public void onError(String message) {
//
//                            }
//                        });
//                    }
//
//                    @Override
//                    public void onError(String message) {
//
//                    }
//                });
//            }
//
//            @Override
//            public void onError(String message) {
//
//            }
//        });
//
//
//
//    }

    @DebugLog
    private void getRemoteStaffData(boolean showBusy) {
        RequestDTO w = new RequestDTO(RequestDTO.GET_STAFF_DATA);
        w.setStaffID(SharedUtil.getCompanyStaff(ctx).getStaffID());

        companyDataRefreshed = false;
        setRefreshActionButtonState(showBusy);
        if (showBusy) {
            Snackbar.make(mPager, "Refreshing your data, this may take a minute or two ...", Snackbar.LENGTH_LONG).show();
        }
        Intent m = new Intent(ctx,DataRefreshService.class);
        startService(m);
    }

    @DebugLog
    private void buildPages() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                pageFragmentList = new ArrayList<>();
                MonApp app = (MonApp) getApplication();
                StaffDTO staff = SharedUtil.getCompanyStaff(ctx);
                profileFragment = new ProfileFragment();
                profileFragment.setStaff(staff);
                profileFragment.setMonApp(app);
                profileFragment.setPersonType(ProfileFragment.STAFF);
                if (staff != null) {
                    profileFragment.setEditType(ProfileFragment.UPDATE_PERSON);
                } else {
                    profileFragment.setEditType(ProfileFragment.ADD_PERSON);
                }

                monitorListFragment = new MonitorListFragment();
                monitorListFragment.setMonApp(app);
                staffListFragment = new StaffListFragment();
                staffListFragment.setMonApp(app);
                projectListFragment = new ProjectListFragment();
                projectListFragment.setMonApp(app);


                profileFragment.setThemeColors(themePrimaryColor, themeDarkColor);
                monitorListFragment.setThemeColors(themePrimaryColor, themeDarkColor);
                staffListFragment.setThemeColors(themePrimaryColor, themeDarkColor);
                projectListFragment.setThemeColors(themePrimaryColor, themeDarkColor);

                pageFragmentList.add(projectListFragment);
                pageFragmentList.add(staffListFragment);
                pageFragmentList.add(monitorListFragment);
                pageFragmentList.add(profileFragment);

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
        });


    }

    @DebugLog
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

    @DebugLog
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

    static final int THEME_REQUESTED = 1762, MONITOR_PROFILE_EDITED = 1766,
            STAFF_PROFILE_EDITED = 1777;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_refresh) {
            getRemoteStaffData(true);
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
        if (id == R.id.action_add_monitor) {
            Intent m = new Intent(ctx, ProfileActivity.class);
            m.putExtra("personType", ProfileFragment.MONITOR);
            m.putExtra("editType", ProfileFragment.ADD_PERSON);

            startActivityForResult(m, MONITOR_PROFILE_EDITED);
            return true;
        }
        if (id == R.id.action_add_staff) {
            Intent m = new Intent(ctx, ProfileActivity.class);
            m.putExtra("personType", ProfileFragment.STAFF);
            m.putExtra("editType", ProfileFragment.ADD_PERSON);

            startActivityForResult(m, STAFF_PROFILE_EDITED);
            return true;
        }
        if (id == R.id.action_add_task) {
            Intent m = new Intent(ctx, TaskListActivity.class);
            startActivity(m);
            return true;
        }
        if (id == R.id.action_add_project) {
            Intent m = new Intent(ctx, ProjectTaskActivity.class);
            m.putExtra("type", ProjectTaskFragment.ADD_NEW_PROJECT);
            startActivity(m);
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
    public void onSaveInstanceState(Bundle b) {
        Log.w(LOG, "onSaveInstanceState");
        b.putSerializable("selectedProject", selectedProject);
        super.onSaveInstanceState(b);
    }

    @Override
    public void onRestoreInstanceState(Bundle b) {
        Log.w(LOG, "onRestoreInstanceState");
        selectedProject = (ProjectDTO) b.getSerializable("selectedProject");
        super.onRestoreInstanceState(b);
    }

    @Override
    @DebugLog
    public void onStart() {
        Log.d(LOG,
                "## onStart - GoogleApiClient connecting ... ");
        if (googleApiClient != null) {
            googleApiClient.connect();
        }

        super.onStart();
    }

    @Override
    @DebugLog
    public void onStop() {
        super.onStop();
        if (googleApiClient != null) {
            googleApiClient.disconnect();
            Log.e(LOG, "### onStop - locationClient disconnecting ");
        }

    }

    @Override
    @DebugLog
    public void onConnected(Bundle bundle) {
        Log.i(LOG,
                "+++  GoogleApiClient onConnected() ...");
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
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
                        + "&daddr=" + selectedProject.getLatitude() + "," + selectedProject.getLongitude() + "&mode=driving";
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
    public void onProjectAssignmentRequested(MonitorDTO monitor) {
        Intent w = new Intent(getApplicationContext(), ProjectSelectionActivity.class);
        w.putExtra("monitor", monitor);
        startActivityForResult(w, MONITOR_PROJECT_ASSIGNMENT);
    }

    @Override
    public void onMonitorPhotoRequired(MonitorDTO monitor) {

    }

    @Override
    public void onMonitorEditRequested(MonitorDTO monitor) {

        Intent w = new Intent(ctx,ProfileActivity.class);
        w.putExtra("monitor",monitor);
        w.putExtra("personType",ProfileFragment.MONITOR);
        startActivityForResult(w, MONITOR_PROFILE_EDITED);
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
        Snackbar.make(mPager, "Getting device GPS coordinates, may take a few seconds ...", Snackbar.LENGTH_LONG).show();
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


        });

    }


    static final int CHECK_FOR_REFRESH = 3121, STAFF_PICTURE_REQUESTED = 3472;
    boolean companyDataRefreshed;


    @Override
    @DebugLog
    public void onActivityResult(int reqCode, final int resCode, Intent data) {
        Log.d(LOG, "onActivityResult reqCode " + reqCode + " resCode " + resCode);
        switch (reqCode) {
            case ADD_PROJECT_TASKS_REQUIRED:
                if (resCode ==  RESULT_OK) {
                    Intent m = new Intent(getApplicationContext(),DataRefreshService.class);
                    startService(m);
                }
                break;
            case STAFF_PROJECT_ASSIGNMENT:
                if (resCode == RESULT_OK) {
                    ResponseDTO w = (ResponseDTO)data.getSerializableExtra("staffProjectList");
                    final List<StaffProjectDTO> spList = w.getStaffProjectList();
                    final Integer id = spList.get(0).getStaffID();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Snappy.getStaffList((MonApp) getApplication(), new Snappy.SnappyReadListener() {
                                @Override
                                public void onDataRead(ResponseDTO response) {
                                    for (StaffDTO sp: response.getStaffList()) {
                                        if (sp.getStaffID().intValue() == id) {
                                            sp.setStaffProjectList(spList);
                                            sp.setProjectCount(spList.size());
                                        }
                                    }
                                    Snappy.writeStaffList((MonApp) getApplication(), response.getStaffList(), new Snappy.SnappyWriteListener() {
                                        @Override
                                        public void onDataWritten() {
                                            Log.e(LOG,"onActivityResult onDataWritten: staffList updated");
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    staffListFragment.getStaffList();
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
                    });

                }
                break;
            case MONITOR_PROJECT_ASSIGNMENT:
                if (resCode == RESULT_OK) {
                    ResponseDTO w = (ResponseDTO)data.getSerializableExtra("monitorProjectList");
                    final List<MonitorProjectDTO> spListm = w.getMonitorProjectList();
                    final Integer id = spListm.get(0).getMonitorID();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Snappy.getMonitorList((MonApp) getApplication(), new Snappy.SnappyReadListener() {
                                @Override
                                public void onDataRead(ResponseDTO response) {
                                    for (MonitorDTO sp: response.getMonitorList()) {
                                        if (sp.getMonitorID().intValue() == id) {
                                            sp.setMonitorProjectList(spListm);
                                            sp.setProjectCount(spListm.size());
                                        }
                                    }
                                    Snappy.writeMonitorList((MonApp) getApplication(), response.getMonitorList(), new Snappy.SnappyWriteListener() {
                                        @Override
                                        public void onDataWritten() {
                                            Log.e(LOG,"onActivityResult onDataWritten: monitorList updated");
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    monitorListFragment.getMonitorList();
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
                    });

                }
                break;
            case MONITOR_PROFILE_EDITED:
                if (resCode == RESULT_OK) {
                    monitorListFragment.getMonitorList();
                }

                break;
            case STAFF_PROFILE_EDITED:
                if (resCode == RESULT_OK) {
                    staffListFragment.getStaffList();
                }
                break;
            case REQUEST_CAMERA:
                if (resCode == RESULT_OK) {
                }
                break;
            case REQUEST_STATUS_UPDATE:
                if (resCode == RESULT_OK) {
                    boolean statusCompleted =
                            data.getBooleanExtra("statusCompleted", false);
                    if (statusCompleted) {
                        Log.e(LOG, "statusCompleted, projectListFragment.getProjectList();");
                        projectListFragment.getProjectList();
                    }
                }
                break;
            case STAFF_PICTURE_REQUESTED:
                if (resCode == RESULT_OK) {
                    PhotoUploadDTO x = (PhotoUploadDTO) data.getSerializableExtra("photo");
                    SharedUtil.savePhoto(getApplicationContext(), x);
                    Log.e(LOG, "photo returned uri: " + x.getUri());
                    profileFragment.setPicture(x);
                }
                break;
            case LOCATION_REQUESTED:
                if (resCode == RESULT_OK) {
                    getRemoteStaffData(true);
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
    public void onUpdated(Person person) {

    }

    @Override
    public void onAdded(Person person) {

    }

    @Override
    public void onPictureRequested(Person person) {
        Intent w = new Intent(getApplicationContext(), PictureActivity.class);
        if (person instanceof MonitorDTO) {
            w.putExtra("monitor", (MonitorDTO) person);
        }
        if (person instanceof StaffDTO) {
            w.putExtra("staff", (StaffDTO) person);
        }
        startActivityForResult(w, PICTURE_REQUESTED);
    }

    static final int PICTURE_REQUESTED = 364, STAFF_PROJECT_ASSIGNMENT = 544,
            MONITOR_PROJECT_ASSIGNMENT = 908;

    @Override
    public void setBusy(boolean busy) {
        setRefreshActionButtonState(busy);
    }

    @Override
    public void onProjectAssigmentWanted(StaffDTO staff) {

        Intent w = new Intent(getApplicationContext(), ProjectSelectionActivity.class);
        w.putExtra("staff", staff);
        startActivityForResult(w, STAFF_PROJECT_ASSIGNMENT);
    }

    @Override
    public void onNewCompanyStaff() {

    }


    @Override
    public void onCompanyStaffInvitationRequested(List<StaffDTO> companyStaffList, int index) {

    }

    @Override
    public void onStaffPictureRequested(StaffDTO companyStaff) {

    }

    @Override
    public void onStaffEditRequested(StaffDTO companyStaff) {

        Intent w = new Intent(ctx, ProfileActivity.class);
        w.putExtra("staff", companyStaff);
        w.putExtra("personType", ProfileFragment.STAFF);
        startActivityForResult(w,STAFF_EDIT_REQUESTED);
    }

    static final int REQUEST_CAMERA = 3329,
            REQUEST_VIDEO = 3488, ADD_PROJECT_TASKS_REQUIRED = 8076,
            LOCATION_REQUESTED = 9031, REQUEST_STATUS_UPDATE = 3291, STAFF_EDIT_REQUESTED = 1524;

    ProjectDTO selectedProject;

    @Override
    @DebugLog
    public void onCameraRequired(final ProjectDTO project) {
        selectedProject = project;
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
    @DebugLog
    public void onStatusUpdateRequired(ProjectDTO project) {
        SharedUtil.saveLastProjectID(ctx, project.getProjectID());
        selectedProject = project;
        Intent w = new Intent(this, UpdateActivity.class);
        w.putExtra("project", project);
        w.putExtra("darkColor", themeDarkColor);
        w.putExtra("type", TaskListFragment.STAFF);
        startActivityForResult(w, REQUEST_STATUS_UPDATE);
    }

    Activity activity;

    @Override
    @DebugLog
    public void onLocationRequired(final ProjectDTO project) {
        SharedUtil.saveLastProjectID(ctx, project.getProjectID());
        selectedProject = project;
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
                            Intent w = new Intent(activity, ProjectMapActivity.class);
                            ResponseDTO responseDTO = new ResponseDTO();
                            responseDTO.setProjectList(new ArrayList<ProjectDTO>());
                            responseDTO.getProjectList().add(project);
                            w.putExtra("projects", responseDTO);
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
    //ProjectDTO project;

    @Override
    public void onDirectionsRequired(ProjectDTO project) {
        SharedUtil.saveLastProjectID(ctx, project.getProjectID());
        selectedProject = project;
        if (project.getLatitude() == null) {
            Util.showErrorToast(ctx, "Project has not been located yet!");
            return;
        }
        directionRequired = true;
        //this.project = project;
        startLocationUpdates();

    }

    @Override
    public void onProjectTasksRequired(ProjectDTO project) {
        Intent m = new Intent(getApplicationContext(), ProjectTaskActivity.class);
        m.putExtra("type", ProjectTaskFragment.ASSIGN_TASKS_TO_ONE_PROJECT);
        m.putExtra("project",project);
        startActivityForResult(m, ADD_PROJECT_TASKS_REQUIRED);
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



    // Broadcast receiver for receiving status updates from DataRefreshService
    private class DataRefreshDoneReceiver extends BroadcastReceiver {

        // Called when the BroadcastReceiver gets an Intent it's registered to receive
        @Override
        public void onReceive(Context context, Intent intent) {
            setRefreshActionButtonState(false);
            Log.e(LOG,"+++++++DataRefreshDoneReceiver onReceive, data must have been refreshed: "
                    + intent.toString());
            Log.d(LOG,"+++++++++++++++++ starting refreshes on all fragments .....");
            projectListFragment.getProjectList();
            staffListFragment.getStaffList();
            monitorListFragment.getMonitorList();

        }
    }

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

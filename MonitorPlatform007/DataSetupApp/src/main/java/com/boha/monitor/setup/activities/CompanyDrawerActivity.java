package com.boha.monitor.setup.activities;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.location.Location;
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

import com.boha.monitor.library.activities.ProfilePhotoActivity;
import com.boha.monitor.library.dto.CompanyDTO;
import com.boha.monitor.library.dto.MonitorDTO;
import com.boha.monitor.library.dto.PortfolioDTO;
import com.boha.monitor.library.dto.RequestDTO;
import com.boha.monitor.library.dto.ResponseDTO;
import com.boha.monitor.library.dto.StaffDTO;
import com.boha.monitor.library.fragments.MonitorListFragment;
import com.boha.monitor.library.fragments.PageFragment;
import com.boha.monitor.library.fragments.StaffProfileFragment;
import com.boha.monitor.library.fragments.StaffListFragment;
import com.boha.monitor.library.util.CacheUtil;
import com.boha.monitor.library.util.DepthPageTransformer;
import com.boha.monitor.library.util.NetUtil;
import com.boha.monitor.library.util.SharedUtil;
import com.boha.monitor.library.util.Util;
import com.boha.monitor.setup.R;
import com.boha.monitor.setup.fragments.NavigationDrawerFragment;
import com.boha.monitor.setup.fragments.PortfolioListFragment;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.List;

public class CompanyDrawerActivity extends AppCompatActivity implements
        PortfolioListFragment.PortfolioFragmentListener,
        MonitorListFragment.MonitorListListener,
        StaffProfileFragment.StaffFragmentListener,
        StaffListFragment.CompanyStaffListListener,
        LocationListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener{

    PortfolioListFragment portfolioListFragment;
    MonitorListFragment monitorListFragment;
    StaffListFragment staffListFragment;
    ActionBar actionBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        ctx = getApplicationContext();
        Resources.Theme theme = getTheme();
        TypedValue typedValue = new TypedValue();
        theme.resolveAttribute(R.attr.colorPrimaryDark, typedValue, true);
        themeDarkColor = typedValue.data;
        theme.resolveAttribute(R.attr.colorPrimary, typedValue, true);
        themePrimaryColor = typedValue.data;


        Integer companyID = getIntent().getIntExtra("companyID", 0);


        actionBar = getSupportActionBar();
        actionBar.setHomeAsUpIndicator(R.drawable.ic_menu);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setLogo(R.drawable.ic_action_language_white);


        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navImage = (ImageView) findViewById(R.id.NAVHEADER_image);
        navText = (TextView) findViewById(R.id.NAVHEADER_text);
        navText.setText(SharedUtil.getCompany(ctx).getCompanyName());


        mPager = (ViewPager) findViewById(R.id.viewpager);
        PagerTitleStrip strip = (PagerTitleStrip) mPager.findViewById(R.id.pager_title_strip);
        strip.setVisibility(View.VISIBLE);
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

        //Menu menu = navigationView.getMenu();
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {

                mDrawerLayout.closeDrawers();
                if (menuItem.getItemId() == R.id.nav_portfolio) {
                    mPager.setCurrentItem(0, true);
                    return true;
                }
                if (menuItem.getItemId() == R.id.nav_staff) {
                    mPager.setCurrentItem(1, true);
                    return true;
                }
                if (menuItem.getItemId() == R.id.nav_monitors) {
                    mPager.setCurrentItem(2, true);
                    return true;
                }
                if (menuItem.getItemId() == R.id.nav_staffProjects) {
                    mPager.setCurrentItem(3, true);
                    return true;
                }
                if (menuItem.getItemId() == R.id.nav_monProjects) {
                    mPager.setCurrentItem(4, true);
                    return true;
                }
                if (menuItem.getItemId() == R.id.nav_projectMaps) {
                    mPager.setCurrentItem(5, true);
                    return true;
                }


                return false;
            }
        });

    }


    private void getCache() {
        CacheUtil.getCachedPortfolioList(ctx, new CacheUtil.CacheUtilListener() {
            @Override
            public void onFileDataDeserialized(ResponseDTO r) {
                if (r.getPortfolioList() != null && !r.getPortfolioList().isEmpty()) {
                    response = r;
                    buildPages();
                    portfolioListFragment.setPortfolioList(r.getPortfolioList());
                }
                refreshPortfolioList();
            }

            @Override
            public void onDataCached() {

            }

            @Override
            public void onError() {

            }
        });
    }
    private void refreshPortfolioList() {
        RequestDTO w = new RequestDTO(RequestDTO.GET_PORTFOLIO_LIST);
        w.setCompanyID(SharedUtil.getCompany(getApplicationContext()).getCompanyID());

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
                        CacheUtil.cachePortfolios(getApplicationContext(), r, new CacheUtil.CacheUtilListener() {
                            @Override
                            public void onFileDataDeserialized(ResponseDTO response) {

                            }

                            @Override
                            public void onDataCached() {
                                buildPages();
                                portfolioListFragment.setPortfolioList(r.getPortfolioList());
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
                        Util.showErrorToast(getApplicationContext(), message);
                    }
                });
            }

        });
    }

    private void buildPages() {
        pageFragmentList = new ArrayList<>();
        portfolioListFragment = PortfolioListFragment.newInstance(response.getPortfolioList());
        monitorListFragment = new MonitorListFragment();
        staffListFragment = new StaffListFragment();


        pageFragmentList.add(portfolioListFragment);
        pageFragmentList.add(monitorListFragment);
        pageFragmentList.add(staffListFragment);

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
            //startLocationUpdates = true;

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
        getMenuInflater().inflate(R.menu.menu_main_drawer, menu);
        mMenu = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_refresh) {
            refreshPortfolioList();
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
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d(LOG,"onLocationChanged " + location.getLatitude()
                + " " + location.getLongitude() + " " + location.getAccuracy());

        if (location.getAccuracy() <= ACCURACY_THRESHOLD) {
            this.location = location;
            stopLocationUpdates();
            mRequestingLocationUpdates = false;

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



    @Override
    public void setBusy(boolean busy) {
        setRefreshActionButtonState(busy);
    }

    @Override
    public void onProjectAssigmentWanted(StaffDTO staff) {

    }

    static final int STAFF_PICTURE_REQUESTED = 3472;
    @Override
    public void onStaffPictureRequired(StaffDTO staff) {
        Intent w = new Intent(this, ProfilePhotoActivity.class);
        w.putExtra("staff", staff);
        startActivityForResult(w, STAFF_PICTURE_REQUESTED);
    }

    @Override
    public void onPortfolioClicked(PortfolioDTO portfolio) {

    }

    @Override
    public void onProgrammeCountClicked(PortfolioDTO portfolio) {
        Intent w = new Intent(this, ProgrammeListActivity.class);
        w.putExtra("portfolioID",portfolio.getPortfolioID());
        startActivityForResult(w,CHECK_FOR_REFRESH);
    }

    static final int CHECK_FOR_REFRESH = 3121;
    boolean companyDataRefreshed;
    @Override
    public void onActivityResult(int reqCode, int resCode, Intent data) {

        switch (reqCode) {

            case CHECK_FOR_REFRESH:
                if (resCode == RESULT_OK) {
                    ResponseDTO x = (ResponseDTO)data.getSerializableExtra("response");
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
    public void onIconDeleteClicked(PortfolioDTO portfolio, int position) {

    }

    @Override
    public void onIconEditClicked(PortfolioDTO portfolio, int position) {

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
    public void onStaffPictureRequested(StaffDTO companyStaff) {

    }

    @Override
    public void onStaffEditRequested(StaffDTO companyStaff) {

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
    static final String LOG = CompanyDrawerActivity.class.getSimpleName();
    static final int ACCURACY_THRESHOLD = 10;
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
    private NavigationDrawerFragment mNavigationDrawerFragment;
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

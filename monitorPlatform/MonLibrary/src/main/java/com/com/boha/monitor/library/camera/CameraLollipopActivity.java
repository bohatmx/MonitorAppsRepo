package com.com.boha.monitor.library.camera;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerTitleStrip;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.boha.monitor.library.R;
import com.com.boha.monitor.library.dto.ProjectSiteDTO;
import com.com.boha.monitor.library.dto.ProjectSiteTaskDTO;
import com.com.boha.monitor.library.dto.ProjectSiteTaskStatusDTO;
import com.com.boha.monitor.library.dto.transfer.ResponseDTO;
import com.com.boha.monitor.library.fragments.LocalGalleryFragment;
import com.com.boha.monitor.library.fragments.PageFragment;
import com.com.boha.monitor.library.util.PMException;
import com.com.boha.monitor.library.util.Util;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;

import org.acra.ACRA;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class CameraLollipopActivity extends ActionBarActivity
        implements LocationListener,
        GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener,
        CameraFragment.CameraListener {
    LocationRequest mLocationRequest;
    LocationClient mLocationClient;
    CameraFragment cameraFragment;
    private List<PageFragment> pageFragmentList;
    ProjectSiteTaskStatusDTO projectSiteTaskStatus;
    ProjectSiteDTO projectSite;
    ProjectSiteTaskDTO projectSiteTask;
    ViewPager mPager;
    PagerAdapter adapter;
    int currentPageIndex;
    PagerTitleStrip pagerTitleStrip;
    LocalGalleryFragment localGalleryFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera22);
        mPager = (ViewPager)findViewById(R.id.pager);
        pagerTitleStrip = (PagerTitleStrip) findViewById(R.id.pager_title_strip);

        projectSite = (ProjectSiteDTO)getIntent().getSerializableExtra("projectSite");
        projectSiteTaskStatus = (ProjectSiteTaskStatusDTO)getIntent().getSerializableExtra("projectSiteTaskStatus");
        projectSiteTask = (ProjectSiteTaskDTO)getIntent().getSerializableExtra("projectSiteTask");

        mLocationRequest = LocationRequest.create();
        mLocationRequest.setInterval(2000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setFastestInterval(1000);

        mLocationClient = new LocationClient(getApplicationContext(), this,
                this);
        buildPages();
    }


    private void buildPages() {
        pageFragmentList = new ArrayList<>();
        cameraFragment = CameraFragment.newInstance();
        Bundle data = new Bundle();
        if (projectSite != null) {
            data.putSerializable("projectSite",projectSite);
        }
        if (projectSiteTaskStatus != null) {
            data.putSerializable("projectSiteTaskStatus",projectSiteTaskStatus);
        }
        if (projectSiteTask != null) {
            data.putSerializable("projectSiteTask",projectSiteTask);
        }
        cameraFragment.setArguments(data);
        localGalleryFragment = new LocalGalleryFragment();
        ResponseDTO r = new ResponseDTO();
        r.setSiteImageFileNameList(new ArrayList<String>());
        for (File f: fileList) {
            r.getSiteImageFileNameList().add(f.getAbsolutePath());
        }
        Bundle data2 = new Bundle();
        data2.putSerializable("response", r);
        //
        pageFragmentList.add(cameraFragment);
        pageFragmentList.add(localGalleryFragment);

        adapter = new PagerAdapter(getSupportFragmentManager());
        mPager.setAdapter(adapter);
        mPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageSelected(int arg0) {
                currentPageIndex = arg0;

            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {

            }

            @Override
            public void onPageScrollStateChanged(int arg0) {
            }
        });

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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_camera2, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
         int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mLocationClient != null) {
            mLocationClient.connect();
            Log.i(LOG,
                    "### onStart - locationClient connecting ... ");
        }

    }

    @Override
    public void onStop() {
        Log.d(LOG,
                "#### onStop");
        if (mLocationClient != null) {
            if (mLocationClient.isConnected()) {
                stopPeriodicUpdates();
            }
            mLocationClient.disconnect();
            Log.e(LOG, "### onStop - locationClient disconnected");
        }
        super.onStop();
    }

    private void stopPeriodicUpdates() {
        mLocationClient.removeLocationUpdates(this);
        Log.e(LOG,
                "### stopPeriodicUpdates - removeLocationUpdates");
    }

    @Override
    public void onLocationChanged(Location l) {

        if (this.location == null) {
            this.location = l;
        } else {
            if (l.getAccuracy() == ACCURACY_THRESHOLD || l.getAccuracy() < ACCURACY_THRESHOLD) {
                if (l.getAccuracy() == location.getAccuracy() || l.getAccuracy() < location.getAccuracy()) {
                    this.location = l;
                    Log.w(LOG, "### onLocationChanged, location accuracy: "
                            + location.getAccuracy());
                    cameraFragment.setLocation(location);
                    if (location.getAccuracy() < 8) {
                        stopPeriodicUpdates();
                    }
                }
            }
        }
      }

    Location location;
    static final float ACCURACY_THRESHOLD = 10;

    @Override
    public void onConnected(Bundle bundle) {
        Log.i(LOG,
                "### ---> LocationClient onConnected() -  >> ");
        location = mLocationClient.getLastLocation();
        mLocationClient.requestLocationUpdates(mLocationRequest, this);
    }

    @Override
    public void onDisconnected() {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        ACRA.getErrorReporter().handleSilentException(new PMException(
                "Google LocationClient onConnectionFailed: " + connectionResult.getErrorCode()));
    }

    static final String LOG = CameraLollipopActivity.class.getSimpleName();

    @Override
    public void onPictureCaptured(File fullFile, File thumbFile) {
        fileList.add(fullFile);
        Log.w(LOG,"## new picture file added to list: " + fileList.size()
        + " fullFile: " + fullFile.length());

        if (vb == null) {
            vb = (Vibrator)getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);
        }
        vb.vibrate(200);
        localGalleryFragment.addPicture(fullFile.getAbsolutePath());
    }
Vibrator vb;
    @Override
    public void onError(String message) {
        Util.showErrorToast(getApplicationContext(),message);
    }
    List<File> fileList = new ArrayList<>();
}

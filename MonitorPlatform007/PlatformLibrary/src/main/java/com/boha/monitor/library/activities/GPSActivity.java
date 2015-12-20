package com.boha.monitor.library.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;

import com.boha.monitor.library.dto.ProjectDTO;
import com.boha.monitor.library.fragments.GPSScanFragment;
import com.boha.monitor.library.util.ThemeChooser;
import com.boha.monitor.library.util.Util;
import com.boha.platform.library.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.util.Date;

/**
 * This activity manages the setting up of a project's
 * GPS coordinates. This is done via the GPSScanFragment
 * The activity expects to be handed a ProjectDTO object which is
 * passed on the fragment.
 * It controls the GoogleApiClient that is used for location requests
 *
 * @see GPSScanFragment
 * @see ProjectDTO
 */
public class GPSActivity extends AppCompatActivity
        implements GPSScanFragment.GPSScanFragmentListener, LocationListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    Location mLocation;
    GoogleApiClient mGoogleApiClient;
    LocationRequest mLocationRequest;
    boolean mRequestingLocationUpdates;
    GPSScanFragment gpsScanFragment;
    ProjectDTO project;
    boolean siteLocationConfirmed;
    int themeDarkColor, themePrimaryColor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        ThemeChooser.setTheme(this);
        Resources.Theme theme = getTheme();
        TypedValue typedValue = new TypedValue();
        theme.resolveAttribute(R.attr.colorPrimaryDark, typedValue, true);
        themeDarkColor = typedValue.data;
        theme.resolveAttribute(R.attr.colorPrimary, typedValue, true);
        themePrimaryColor = typedValue.data;

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gps);
        gpsScanFragment = (GPSScanFragment) getSupportFragmentManager().findFragmentById(R.id.fragment);
        project = (ProjectDTO) getIntent().getSerializableExtra("project");
        gpsScanFragment.setProject(project);


        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        Util.setCustomActionBar(getApplicationContext(), getSupportActionBar(),
                project.getProjectName(),project.getCityName(),
                ContextCompat.getDrawable(getApplicationContext(), R.drawable.glasses48));

    }


    @Override
    public void onStart() {
        super.onStart();
        Log.w(LOG, "######### onStart");
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(LOG, "######### onStop");
        if (mGoogleApiClient != null) {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.gps_menu, menu);
        mMenu = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected( MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onStartScanRequested() {
        startLocationUpdates();
    }

    @Override
    public void onLocationConfirmed(ProjectDTO project) {
        Log.e(LOG, "######### onLocationConfirmed");
        this.project = project;
        this.project.setLocationConfirmed(true);
        siteLocationConfirmed = true;
        onBackPressed();
    }

    @Override
    public void onEndScanRequested() {
        Log.e(LOG,"%%%% onEndScanRequested");
        stopLocationUpdates();
    }

    /**
     * Open a map showing project location
     * @param project
     */
    @Override
    public void onMapRequested( ProjectDTO project) {
        if (project.getLatitude() != null) {
            Intent i = new Intent(getApplicationContext(), MonitorMapActivity.class);
            i.putExtra("projectSite", project);
            startActivityForResult(i, MAP_REQUESTED);
        }
    }

    @Override
    public void setBusy(boolean busy) {
        setRefreshActionButtonState(busy);
    }

    @Override
    public void onBackPressed() {
        Log.w(LOG, "######### onBackPressed");
        if (siteLocationConfirmed) {
            Intent w = new Intent();
            w.putExtra("project", project);
            setResult(RESULT_OK, w);
        } else {
            setResult(RESULT_CANCELED);
        }

        finish();
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

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    static final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;

    public void startLocationUpdates() {
        int permissionCheck = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
            return;
        }

        Log.w(LOG, "###### startLocationUpdates: " + new Date().toString());
        if (mGoogleApiClient.isConnected()) {
            mRequestingLocationUpdates = true;
            LocationServices.FusedLocationApi.requestLocationUpdates(
                    mGoogleApiClient, mLocationRequest, this);
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startLocationUpdates();

                } else {
                    throw new UnsupportedOperationException();
                }
                return;
            }
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
    public void onLocationChanged( Location loc) {
        Log.d(LOG, "## onLocationChanged accuracy = " + loc.getAccuracy()
                + " - " + new Date().toString());
        mLocation = loc;
        gpsScanFragment.setLocation(mLocation);
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    static final int MAP_REQUESTED = 9007;
    static final String LOG = GPSActivity.class.getSimpleName();

    @Override
    public void onActivityResult(int reqCode, int res,  Intent data) {
        Log.e(LOG, "##### onActivityResult requestCode: " + reqCode + " resultCode: " + res);
        switch (reqCode) {
            case MAP_REQUESTED:
                Log.w(LOG, "### map has returned with data?");
                if (res == RESULT_OK) {
                    project = (ProjectDTO) data.getSerializableExtra("project");
                }
                break;

        }

    }

    @Override
    public void onPause() {
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        super.onPause();
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

}

package com.boha.monitor.library.activities;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.boha.monitor.library.R;
import com.boha.monitor.library.dto.ProjectSiteDTO;
import com.boha.monitor.library.fragments.GPSScanFragment;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.util.Date;

public class GPSActivity extends AppCompatActivity
        implements GPSScanFragment.GPSScanFragmentListener, LocationListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    Location mLocation;
    GoogleApiClient mGoogleApiClient;
    LocationRequest mLocationRequest;
    boolean mRequestingLocationUpdates;
    GPSScanFragment gpsScanFragment;
    ProjectSiteDTO site;
    boolean siteLocationConfirmed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e(LOG, "######### onCreate");
        setContentView(R.layout.activity_gps);
        gpsScanFragment = (GPSScanFragment) getSupportFragmentManager().findFragmentById(R.id.fragment);
        site = (ProjectSiteDTO) getIntent().getSerializableExtra("projectSite");
        gpsScanFragment.setProjectSite(site);


        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        setTitle(getString(R.string.site_coords));
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
        getMenuInflater().inflate(R.menu.menu_gps, menu);
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
    public void onLocationConfirmed(ProjectSiteDTO projectSite) {
        Log.e(LOG, "######### onLocationConfirmed");
        site = projectSite;
        site.setLocationConfirmed(1);
        siteLocationConfirmed = true;
    }

    @Override
    public void onEndScanRequested() {
        Log.e(LOG,"%%%% onEndScanRequested");
        stopLocationUpdates();
    }

    @Override
    public void onMapRequested( ProjectSiteDTO projectSite) {
        //todo - start mapActivity
        if (projectSite.getLatitude() != null) {
            Intent i = new Intent(getApplicationContext(), MonitorMapActivity.class);
            i.putExtra("projectSite", projectSite);
            startActivityForResult(i, MAP_REQUESTED);
        }
    }

    @Override
    public void onBackPressed() {
        Log.w(LOG, "######### onBackPressed");
        if (siteLocationConfirmed) {
            Intent w = new Intent();
            w.putExtra("projectSite", site);
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
        mLocationRequest.setInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setFastestInterval(1000);
        startLocationUpdates();

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
                    site = (ProjectSiteDTO) data.getSerializableExtra("projectSite");
                }
                break;

        }

    }

    @Override
    public void onPause() {
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        super.onPause();
    }


}

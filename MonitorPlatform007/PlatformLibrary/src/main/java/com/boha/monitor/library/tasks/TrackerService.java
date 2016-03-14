package com.boha.monitor.library.tasks;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.boha.monitor.library.dto.LocationTrackerDTO;
import com.boha.monitor.library.dto.MonitorDTO;
import com.boha.monitor.library.dto.RequestDTO;
import com.boha.monitor.library.dto.ResponseDTO;
import com.boha.monitor.library.dto.StaffDTO;
import com.boha.monitor.library.util.MonLog;
import com.boha.monitor.library.util.OKHttpException;
import com.boha.monitor.library.util.OKUtil;
import com.boha.monitor.library.util.SharedUtil;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.gcm.GcmTaskService;
import com.google.android.gms.gcm.TaskParams;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.util.Date;

public class TrackerService extends GcmTaskService
        implements LocationListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    boolean mRequestingLocationUpdates;
    LocationRequest mLocationRequest;
    GoogleApiClient googleApiClient;
    Location mCurrentLocation;
    static final String LOG = TrackerService.class.getSimpleName();
    static final float ACCURACY = 30f;
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onRunTask(TaskParams taskParams) {
        Log.w(LOG, "############ onRunTask");
        return 0;
    }

    @Override
    public void onCreate() {
        Log.w(LOG, "############ onCreate, build googleApiClient");
        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        googleApiClient.connect();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        MonLog.e(getApplicationContext(), LOG, "Google Play Services connected, start location request");
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(
                googleApiClient);

        mLocationRequest = LocationRequest.create();
        mLocationRequest.setInterval(2000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setFastestInterval(1000);
        startLocationUpdates();
    }

    protected void startLocationUpdates() {
        MonLog.d(getApplicationContext(), LOG, "### startLocationUpdates ....");
        if (googleApiClient.isConnected()) {
            mRequestingLocationUpdates = true;
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            LocationServices.FusedLocationApi.requestLocationUpdates(
                    googleApiClient, mLocationRequest, this);
            MonLog.d(getApplicationContext(), LOG, "## requesting location updates ...");
        } else {
            MonLog.e(getApplicationContext(), LOG, "------- GoogleApiClient is NOT connected, not sure where we are...");
            googleApiClient.connect();

        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onLocationChanged(Location location) {
        MonLog.w(getApplicationContext(),LOG,"....onLocationChanged, accuracy: " + location.getAccuracy());
        if (location.getAccuracy() <= ACCURACY) {
            mCurrentLocation = location;
            submitRegularTrack();
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            LocationServices.FusedLocationApi.removeLocationUpdates(
                    googleApiClient, this);
            MonLog.i(getApplicationContext(),LOG,"removeLocationUpdates fired. stop gps scan");
        }
    }
    private void submitRegularTrack() {

        MonLog.w(getApplicationContext(),LOG,"$$$$$$$$$$$$$$$$$$$$$ submitRegularTrack ........");
        RequestDTO w = new RequestDTO(RequestDTO.ADD_LOCATION_TRACK);
        LocationTrackerDTO dto = new LocationTrackerDTO();
        StaffDTO staff = SharedUtil.getCompanyStaff(getApplicationContext());
        MonitorDTO mon = SharedUtil.getMonitor(getApplicationContext());
        if (staff != null) {
            dto.setStaffID(staff.getStaffID());
            dto.setStaffName(staff.getFullName());
        }
        if (mon != null) {
            dto.setMonitorID(mon.getMonitorID());
            dto.setMonitorName(mon.getFullName());
        }
        dto.setDateTracked(new Date().getTime());
        dto.setLatitude(mCurrentLocation.getLatitude());
        dto.setLongitude(mCurrentLocation.getLongitude());
        dto.setAccuracy(mCurrentLocation.getAccuracy());
        dto.setGcmDevice(SharedUtil.getGCMDevice(getApplicationContext()));
        dto.getGcmDevice().setRegistrationID(null);
        w.setLocationTracker(dto);

        OKUtil okUtil = new OKUtil();
        try {
            okUtil.sendGETRequest(getApplicationContext(), w, new OKUtil.OKListener() {
                @Override
                public void onResponse(ResponseDTO response) {
                    stopTracker();
                }

                @Override
                public void onError(String message) {
                    Log.e(LOG,message);
                    stopTracker();
                }
            });
        } catch (OKHttpException e) {
            Log.e(LOG,e.getMessage(),e);
        }


    }
    private void stopTracker() {
        this.stopSelf();
        MonLog.w(getApplicationContext(),LOG,"TrackerService complete...shutting down");

    }
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }



}

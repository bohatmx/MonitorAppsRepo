package com.boha.monitor.library.services;

import android.app.Service;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.boha.monitor.library.dto.LocationTrackerDTO;
import com.boha.monitor.library.dto.MonitorDTO;
import com.boha.monitor.library.dto.RequestDTO;
import com.boha.monitor.library.dto.ResponseDTO;
import com.boha.monitor.library.dto.StaffDTO;
import com.boha.monitor.library.util.CacheUtil;
import com.boha.monitor.library.util.NetUtil;
import com.boha.monitor.library.util.SharedUtil;
import com.boha.platform.library.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class GPSService extends Service implements LocationListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {
    public GPSService() {
    }

    Location mLocation;
    GoogleApiClient mGoogleApiClient;
    LocationRequest mLocationRequest;
    boolean mRequestingLocationUpdates;


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.w(LOG, "###### onStartCommand");
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();

        return 0;
    }


    @Override
    public IBinder onBind(Intent intent) {
        Log.i(LOG, "### onBind, intent: " + intent.toString());
        throw new UnsupportedOperationException("not done yet");
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
        mLocationRequest.setInterval(3000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setFastestInterval(2000);

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
        Log.e(LOG, "###### stopLocationUpdates - " + new Date().toString());
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(
                    mGoogleApiClient, this);
        }
    }


    @Override
    public void onLocationChanged(Location loc) {
        Log.d(LOG, "## onLocationChanged accuracy = " + loc.getAccuracy()
                + " - " + new Date().toString());

        if (this.mLocation == null) {
            this.mLocation = loc;
        }
        if (loc.getAccuracy() <= ACCURACY_THRESHOLD) {
            this.mLocation = loc;
            stopLocationUpdates();
            CacheUtil.getCachedTrackerData(getApplicationContext(), new CacheUtil.CacheUtilListener() {
                @Override
                public void onFileDataDeserialized(final ResponseDTO r) {
                    if (r.getLocationTrackerList() == null) {
                        r.setLocationTrackerList(new ArrayList<LocationTrackerDTO>());
                    }
                    LocationTrackerDTO dto = new LocationTrackerDTO();
                    StaffDTO s = SharedUtil.getCompanyStaff(
                            getApplicationContext());
                    if (s != null)
                        dto.setStaffID(s.getStaffID());
                    MonitorDTO m = SharedUtil.getMonitor(
                            getApplicationContext());
                    if (m != null)
                        dto.setMonitorID(m.getMonitorID());
                    dto.setAccuracy(mLocation.getAccuracy());
                    dto.setDateTracked(new Date().getTime());
                    dto.setLatitude(mLocation.getLatitude());
                    dto.setLongitude(mLocation.getLongitude());
                    dto.setGeocodedAddress(getAddress());
                    if (dto.getGeocodedAddress() == null) {
                        dto.setGeocodedAddress(getString(R.string.no_address));
                    }
                    r.getLocationTrackerList().add(dto);
                    response = r;
                    CacheUtil.cacheTrackerData(getApplicationContext(),
                            r, new CacheUtil.CacheUtilListener() {
                                @Override
                                public void onFileDataDeserialized(ResponseDTO response) {

                                }

                                @Override
                                public void onDataCached() {
                                    if (response.getLocationTrackerList().size() < MINIMUM_TRACKER_EVENTS) {
                                        Log.d(LOG, "## tracker locations < " + MINIMUM_TRACKER_EVENTS
                                                + ", list has: "
                                                + response.getLocationTrackerList().size() + " ==> Bypassing send for now: " + new Date().toString());
                                        return;
                                    }

                                    send(response.getLocationTrackerList());
                                }

                                @Override
                                public void onError() {

                                }
                            });
                }

                @Override
                public void onDataCached() {

                }

                @Override
                public void onError() {

                }
            });

        }
    }

    private String getAddress() {

        Geocoder geocoder = new Geocoder(getApplicationContext(),
                Locale.getDefault());
        try {
            List<Address> list = geocoder.getFromLocation(
                    mLocation.getLatitude(), mLocation.getLongitude(), 1);
            if (list == null || list.isEmpty()) {
                return getApplicationContext().getString(R.string.no_address);
            }
            Address address = list.get(0);
            int cnt = address.getMaxAddressLineIndex();
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < cnt; i++) {
                sb.append(address.getAddressLine(i));
                if (i < (cnt - 1)) {
                    sb.append(", ");
                }
            }
            Log.w(LOG, "## Address found: " + sb.toString());
            return sb.toString();

        } catch (IOException e) {
            Log.e(LOG, "Failed", e);
            return getString(R.string.no_address);
        } catch (IllegalArgumentException e) {
            Log.e(LOG, "Failed", e);
            return getString(R.string.no_address);
        }
    }

    private int index, batches;
    private ResponseDTO response;


    private void send(List<LocationTrackerDTO> list) {
        RequestDTO dto = new RequestDTO(RequestDTO.ADD_LOCATION_TRACKERS);
        dto.setLocationTrackerList(list);
        Log.e(LOG, "### sending location tracks to server: " + list.size());
        NetUtil.sendRequest(getApplicationContext(), dto, new NetUtil.NetUtilListener() {
            @Override
            public void onResponse(ResponseDTO response) {
                Log.e(LOG, response.getMessage());
                if (response.getStatusCode() == 0) {
                    ResponseDTO w = new ResponseDTO();
                    w.setLocationTrackerList(new ArrayList<LocationTrackerDTO>());
                    CacheUtil.cacheTrackerData(getApplicationContext(),w, null);
                }
            }

            @Override
            public void onError(String message) {
                Log.e(LOG, message);
            }

            @Override
            public void onWebSocketClose() {
                Log.e(LOG, "### onWebSocketClose");
            }
        });
    }

    static final float ACCURACY_THRESHOLD = 15;
    static final int MINIMUM_TRACKER_EVENTS = 4;

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    static final String LOG = GPSService.class.getSimpleName();

    class Batch {

    }
}

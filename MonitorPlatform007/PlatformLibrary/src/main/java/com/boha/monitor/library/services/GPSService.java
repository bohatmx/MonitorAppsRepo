package com.boha.monitor.library.services;

import android.app.Service;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.boha.monitor.library.dto.GcmDeviceDTO;
import com.boha.monitor.library.dto.LocationTrackerDTO;
import com.boha.monitor.library.dto.MonitorDTO;
import com.boha.monitor.library.dto.RequestDTO;
import com.boha.monitor.library.dto.ResponseDTO;
import com.boha.monitor.library.dto.SimpleMessageDTO;
import com.boha.monitor.library.dto.StaffDTO;
import com.boha.monitor.library.util.CacheUtil;
import com.boha.monitor.library.util.NetUtil;
import com.boha.monitor.library.util.SharedUtil;
import com.boha.monitor.library.util.WebCheck;
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

    Location mLocation;
    GoogleApiClient mGoogleApiClient;
    LocationRequest mLocationRequest;
    boolean mRequestingLocationUpdates;
    List<LocationTrackerDTO> locationTrackerList;
    SimpleMessageDTO simpleMessage;

    public GPSService() {
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            Log.w(LOG, "###### onStartCommand, intent: " + intent.toString());
            simpleMessage = (SimpleMessageDTO) intent.getSerializableExtra("simpleMessage");
        } else {
            Log.w(LOG, "###### onStartCommand null intent: ");
        }

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();

        return 0;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public void onConnected(Bundle bundle) {
        Log.i(LOG,
                "+++  onConnected() -  requestLocationUpdates ...");
        mLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);
        if (mLocation != null) {
            Log.w(LOG, "## GPSService requesting location updates ....lastLocation: "
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

    GcmDeviceDTO gcmDevice;

    protected void startLocationUpdates() {
        Log.w(LOG, "###### startLocationUpdates: " + new Date().toString());
        gcmDevice = SharedUtil.getGCMDevice(getApplicationContext());
        if (gcmDevice == null) {
            Log.e(LOG, "## gcmDevice is null. Not tracking this device yet");
            return;
        }
        if (mGoogleApiClient.isConnected()) {
            mRequestingLocationUpdates = true;
            LocationServices.FusedLocationApi.requestLocationUpdates(
                    mGoogleApiClient, mLocationRequest, this);
        }
    }

    protected void stopLocationUpdates() {

        if (mGoogleApiClient.isConnected()) {
            Log.e(LOG, "###### stopLocationUpdates - " + new Date().toString());
            LocationServices.FusedLocationApi.removeLocationUpdates(
                    mGoogleApiClient, this);
        }
    }


    @Override
    public void onLocationChanged(Location loc) {
        Log.d(LOG, "## GPSService - onLocationChanged accuracy = " + loc.getAccuracy()
                + " - " + new Date().toString());

        if (loc.getAccuracy() <= ACCURACY_THRESHOLD) {
            this.mLocation = loc;
            stopLocationUpdates();

            if (simpleMessage == null) {
                saveLocation();
            } else {
                sendLocationResponse();
            }
        }
    }

    private void sendLocationResponse() {
        final SimpleMessageDTO sm = new SimpleMessageDTO();
        final LocationTrackerDTO dto = new LocationTrackerDTO();
        StaffDTO s = SharedUtil.getCompanyStaff(
                getApplicationContext());
        if (s != null) {
            dto.setStaffID(s.getStaffID());
            dto.setStaffName(s.getFullName());
            sm.setStaffName(s.getFullName());
        }
        MonitorDTO m = SharedUtil.getMonitor(
                getApplicationContext());
        if (m != null) {
            dto.setMonitorID(m.getMonitorID());
            dto.setMonitorName(m.getFullName());
            sm.setMonitorName(m.getFullName());
        }
        dto.setAccuracy(mLocation.getAccuracy());
        dto.setDateTracked(new Date().getTime());
        dto.setLatitude(mLocation.getLatitude());
        dto.setLongitude(mLocation.getLongitude());
        try {
            dto.setGeocodedAddress(getAddress());
        } catch (Exception e) {

        }

        sm.setLocationTracker(dto);
        sm.setStaffList(new ArrayList<Integer>());
        if (simpleMessage.getStaffID() != null)
            sm.getStaffList().add(simpleMessage.getStaffID());
        sm.setMonitorList(new ArrayList<Integer>());
        if (simpleMessage.getMonitorID() != null)
            sm.getMonitorList().add(simpleMessage.getMonitorID());

        RequestDTO w = new RequestDTO(RequestDTO.SEND_SIMPLE_MESSAGE);
        w.setSimpleMessage(sm);
        Log.d(LOG, "Sending location request response...");
        NetUtil.sendRequest(getApplicationContext(), w, new NetUtil.NetUtilListener() {
            @Override
            public void onResponse(ResponseDTO response) {
                Log.d(LOG, "location response sent to requestor");
                simpleMessage = null;
            }

            @Override
            public void onError(String message) {
                Log.e(LOG, "location response sent: " + message);
                message = null;
            }

            @Override
            public void onWebSocketClose() {

            }
        });


    }

    private void saveLocation() {

        final LocationTrackerDTO dto = new LocationTrackerDTO();
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
        try {
            dto.setGeocodedAddress(getAddress());
            if (dto.getGeocodedAddress() == null) {
                dto.setGeocodedAddress(getString(R.string.no_address));
            }
        } catch (Exception e) {
            dto.setGeocodedAddress(getString(R.string.no_address));
        }

        CacheUtil.addLocationTrack(getApplicationContext(), dto, new CacheUtil.AddLocationTrackerListener() {
            @Override
            public void onLocationTrackerAdded(ResponseDTO response) {
                Log.i(LOG, "onLocationTrackerAdded, tracks: " + response.getLocationTrackerList().size());
                locationTrackerList = response.getLocationTrackerList();
                index = 0;
                if (!WebCheck.checkNetworkAvailability(getApplicationContext()).isNetworkUnavailable()) {

                    try {
                        Log.e(LOG, "Sleeping for a few seconds ...");
                        Thread.sleep(5000);
                        Log.i(LOG, "waking up... controlSend...");
                        controlSend();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }
            }
        });


    }

    private void controlSend() {

        if (index < locationTrackerList.size()) {
            send(locationTrackerList.get(index));
        } else {
            Log.d(LOG, "## " + locationTrackerList.size() + " Tracks have been sent up, cleaning cache ...");
            locationTrackerList.clear();
            ResponseDTO w = new ResponseDTO();
            w.setLocationTrackerList(new ArrayList<LocationTrackerDTO>());
            CacheUtil.cacheTrackerData(getApplicationContext(), w, new CacheUtil.CacheUtilListener() {
                @Override
                public void onFileDataDeserialized(ResponseDTO response) {

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

        if (WebCheck.checkNetworkAvailability(getApplicationContext()).isNetworkUnavailable()) {
            return "Address not available";
        }
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
            Log.w(LOG, "## GPSService Address found: " + sb.toString());
            return sb.toString();

        } catch (IOException e) {
            Log.e(LOG, "Failed", e);
            return getString(R.string.no_address);
        } catch (IllegalArgumentException e) {
            Log.e(LOG, "Failed", e);
            return getString(R.string.no_address);
        }
    }

    private int index;

    private void send(LocationTrackerDTO dto) {

        final RequestDTO w = new RequestDTO(RequestDTO.ADD_LOCATION_TRACK);
        if (SharedUtil.getMonitor(getApplicationContext()) != null) {
            dto.setMonitorID(SharedUtil.getMonitor(getApplicationContext()).getMonitorID());
        }
        if (SharedUtil.getCompanyStaff(getApplicationContext()) != null) {
            dto.setStaffID(SharedUtil.getCompanyStaff(getApplicationContext()).getStaffID());
        }
        if (SharedUtil.getGCMDevice(getApplicationContext()) != null) {
            GcmDeviceDTO x = new GcmDeviceDTO();
            x.setGcmDeviceID(SharedUtil.getGCMDevice(getApplicationContext()).getGcmDeviceID());
            dto.setGcmDevice(x);
        }
        w.setLocationTracker(dto);

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                NetUtil.sendRequest(getApplicationContext(), w, new NetUtil.NetUtilListener() {
                    @Override
                    public void onResponse(ResponseDTO response) {
                        Log.e(LOG, response.getMessage());
                        if (response.getStatusCode() == 0) {
                            index++;
                            controlSend();

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
        });
        thread.start();

    }

    static final float ACCURACY_THRESHOLD = 30;

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    static final String LOG = GPSService.class.getSimpleName();

}

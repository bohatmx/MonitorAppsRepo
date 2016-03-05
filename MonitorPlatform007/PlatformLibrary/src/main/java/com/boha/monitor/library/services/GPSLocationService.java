package com.boha.monitor.library.services;

import android.Manifest;
import android.app.Activity;
import android.app.IntentService;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.boha.monitor.library.dto.GcmDeviceDTO;
import com.boha.monitor.library.dto.LocationTrackerDTO;
import com.boha.monitor.library.dto.MonitorDTO;
import com.boha.monitor.library.dto.RequestDTO;
import com.boha.monitor.library.dto.ResponseDTO;
import com.boha.monitor.library.dto.SimpleMessageDTO;
import com.boha.monitor.library.dto.SimpleMessageDestinationDTO;
import com.boha.monitor.library.dto.StaffDTO;
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

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class GPSLocationService extends IntentService {

    public GPSLocationService() {
        super("GPSLocationService");
    }

    SimpleMessageDTO simpleMessage;
    static final String LOG = GPSLocationService.class.getSimpleName();

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public GPSLocationService(String name) {
        super(name);
    }

    private void sendLocationResponse(double latitude, double longitude, float accuracy) {
        final SimpleMessageDTO sm = new SimpleMessageDTO();
        sm.setSimpleMessageDestinationList(new ArrayList<SimpleMessageDestinationDTO>());
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
        dto.setAccuracy(accuracy);
        dto.setDateTracked(new Date().getTime());
        dto.setLatitude(latitude);
        dto.setLongitude(longitude);
        dto.setGcmDevice(SharedUtil.getGCMDevice(getApplicationContext()));

        sm.setLocationTracker(dto);

        if (simpleMessage.getStaffID() != null) {
            SimpleMessageDestinationDTO dest = new SimpleMessageDestinationDTO();
            dest.setStaffID(simpleMessage.getStaffID());
            sm.getSimpleMessageDestinationList().add(dest);
        }
        if (simpleMessage.getMonitorID() != null) {
            SimpleMessageDestinationDTO dest = new SimpleMessageDestinationDTO();
            dest.setMonitorID(simpleMessage.getMonitorID());
            sm.getSimpleMessageDestinationList().add(dest);
        }

        RequestDTO w = new RequestDTO(RequestDTO.SEND_SIMPLE_MESSAGE);
        w.setZipResponse(false);
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
                Log.e(LOG, "location response error: " + message);
                simpleMessage = null;
            }


        });


    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.e(LOG, "***** onHandleIntent, intent = " + intent.toString());
        LocationTrackerDTO lt = null;
        SimpleMessageDTO simple = null;
        if (intent != null) {
            simple = (SimpleMessageDTO) intent.getSerializableExtra("simpleMessage");
            lt = (LocationTrackerDTO) intent.getSerializableExtra("track");
            if (simple != null) {

            }
            if (lt != null) {
                RequestDTO w = new RequestDTO(RequestDTO.ADD_LOCATION_TRACK);
                lt.setDateTracked(new Date().getTime());
                w.setLocationTracker(lt);
                w.setZipResponse(false);
                NetUtil.sendRequest(getApplicationContext(), w, new NetUtil.NetUtilListener() {
                    @Override
                    public void onResponse(ResponseDTO response) {
                        Log.w(LOG, "++++++ Track sent OK");
                    }

                    @Override
                    public void onError(String message) {
                        Log.e(LOG, message);
                    }


                });
            } else {
                double latitude = intent.getDoubleExtra("latitude", 0);
                double longitude = intent.getDoubleExtra("longitude", 0);
                float accuracy = intent.getFloatExtra("accuarcy", 0);
                if (latitude != 0) {
                    sendLocationResponse(latitude, longitude, accuracy);
                }
            }
        }

    }
}

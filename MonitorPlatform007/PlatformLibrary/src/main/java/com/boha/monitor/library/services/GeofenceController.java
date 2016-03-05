package com.boha.monitor.library.services;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by aubreymalabie on 3/4/16.
 */
public class GeofenceController {

    private final String TAG = GeofenceController.class.getName();

    private Context context;
    private GoogleApiClient googleApiClient;
    private Gson gson;
    private SharedPreferences prefs;
    private GeofenceControllerListener listener;


    private List<NamedGeofence> namedGeofences;
    private List<NamedGeofence> namedGeofencesToRemove;

    private Geofence geofenceToAdd;
    private NamedGeofence namedGeofenceToAdd;

    private static GeofenceController INSTANCE;

    public static GeofenceController getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new GeofenceController();
        }
        return INSTANCE;
    }

    public void init(Context context) {
        Log.e(TAG,"GeofenceController init");
        this.context = context;
        gson = new Gson();
        namedGeofences = new ArrayList<>();
        namedGeofencesToRemove = new ArrayList<>();
    }
    public void addGeofence(NamedGeofence namedGeofence,
                            GeofenceControllerListener listener) {
        Log.d(TAG,".................. addGeofence: " + namedGeofence.toString());
        this.namedGeofenceToAdd = namedGeofence;
        this.geofenceToAdd = namedGeofence.getGeofence();
        this.listener = listener;

        connectWithCallbacks(connectionAddListener);
    }
    public interface GeofenceControllerListener {
        void onGeofencesUpdated();

        void onError();
    }

    private GoogleApiClient.ConnectionCallbacks connectionAddListener =
            new GoogleApiClient.ConnectionCallbacks() {
                @Override
                public void onConnected(Bundle bundle) {
                    Log.e(TAG,"GoogleApiClient onConnected");
// 1. Create an IntentService PendingIntent
                    Intent intent = new Intent(context, GeofenceIntentService.class);
                    PendingIntent pendingIntent =
                            PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

// 2. Associate the service PendingIntent with the geofence and call addGeofences
                    if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        return;
                    }
                    PendingResult<Status> result =
                            LocationServices.GeofencingApi.addGeofences(
                            googleApiClient, getAddGeofencingRequest(), pendingIntent);

// 3. Implement PendingResult callback
                    result.setResultCallback(new ResultCallback<Status>() {

                        @Override
                        public void onResult(Status status) {
                            Log.w(TAG,"PendingResult onResult, status: " + status.toString());
                            if (status.isSuccess()) {
                                // 4. If successful, save the geofence
                                saveGeofence();
                            } else {
                                // 5. If not successful, log and send an error
                                Log.e(TAG, "Registering geofence failed: " + status.getStatusMessage() +
                                        " : " + status.getStatusCode());
                                sendError();
                            }
                        }
                    });
                }

                @Override
                public void onConnectionSuspended(int i) {

                }
            };

    private GoogleApiClient.OnConnectionFailedListener connectionFailedListener =
            new GoogleApiClient.OnConnectionFailedListener() {
                @Override
                public void onConnectionFailed(ConnectionResult connectionResult) {

                }
            };

    List<Geofence> geofencesToAdd = new ArrayList<>();
    private GeofencingRequest getAddGeofencingRequest() {
        Log.d(TAG,"getAddGeofencingRequest ............................");
        geofencesToAdd.add(geofenceToAdd);
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER
                | GeofencingRequest.INITIAL_TRIGGER_DWELL);
        builder.addGeofences(geofencesToAdd);
        GeofencingRequest gr = builder.build();
        return builder.build();
    }

    private void connectWithCallbacks(GoogleApiClient.ConnectionCallbacks callbacks) {
        Log.d(TAG,"connectWithCallbacks, setting up googleApiClient ............");
        googleApiClient = new GoogleApiClient.Builder(context)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(callbacks)
                .addOnConnectionFailedListener(connectionFailedListener)
                .build();
        googleApiClient.connect();
    }

    private void sendError() {
        if (listener != null) {
            listener.onError();
        }
    }

    private void saveGeofence() {
        namedGeofences.add(namedGeofenceToAdd);
        if (listener != null) {
            listener.onGeofencesUpdated();
        }
    }
}
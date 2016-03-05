package com.boha.monitor.library.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

public class GeofenceBroadcastReceiver extends BroadcastReceiver {
    static final String LOG = GeofenceBroadcastReceiver.class.getSimpleName();
    public GeofenceBroadcastReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.w(LOG,"..... onReceive, intent = " + intent.toString());
        GeofencingEvent event = GeofencingEvent.fromIntent(intent);
        String transition = mapTransition(event.getGeofenceTransition());


    }

    private String mapTransition(int event) {
        switch (event) {
            case Geofence.GEOFENCE_TRANSITION_ENTER:
                return "ENTER";
            case Geofence.GEOFENCE_TRANSITION_EXIT:
                return "EXIT";
            default:
                return "UNKNOWN";
        }
    }
}

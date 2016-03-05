package com.boha.monitor.library.services;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import java.util.List;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class GeofenceIntentService extends IntentService {
    static final String LOG = GeofenceIntentService.class.getSimpleName();
    public static final String GEOFENCE_EVENT = "geoFenceEvent";
    public static final String BROADCAST_ACTION =
            "com.boha.monitor.GEOFENCE.EVENT";

    public GeofenceIntentService() {
        super("GeofenceIntentService");
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        Log.e(LOG, "++++++++++++++++ onHandleIntent, intent: " + intent.toString());
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        if (geofencingEvent.hasError()) {
            Log.e(LOG, "geofencingEvent hasError: " + geofencingEvent.getErrorCode());
            return;
        }

        Log.d(LOG, "geofencingEvent: " + geofencingEvent.getTriggeringGeofences().size());
        // Get the transition type.
        int geofenceTransition = geofencingEvent.getGeofenceTransition();
        // Test that the reported transition was of interest.
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER
                || geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {

            List triggeringGeofences = geofencingEvent.getTriggeringGeofences();
            Intent m = new Intent(BROADCAST_ACTION);
            switch (geofenceTransition) {
                case Geofence.GEOFENCE_TRANSITION_ENTER:
                    Log.e(LOG, "***** device has entered the geofence");
                    m.putExtra("event", "ENTER");
                    LocalBroadcastManager.getInstance(getApplicationContext())
                            .sendBroadcast(m);
                    break;

                case Geofence.GEOFENCE_TRANSITION_EXIT:
                    Log.e(LOG, "***** device has exited the geofence; broadcasting the fact!");
                    m.putExtra("event", "EXIT");
                    LocalBroadcastManager.getInstance(getApplicationContext())
                            .sendBroadcast(m);
                    break;
            }

        } else {
            Log.e(LOG, "geofence_transition_invalid_type" +
                    geofenceTransition);
        }

    }
}

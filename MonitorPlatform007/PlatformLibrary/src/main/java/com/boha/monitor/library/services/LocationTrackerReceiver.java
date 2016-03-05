package com.boha.monitor.library.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.util.Date;

public class LocationTrackerReceiver extends BroadcastReceiver {
    public LocationTrackerReceiver() {
    }

    public static final String LOCATION_REQUESTED = "trackLocationRequested";
    public static final String BROADCAST_ACTION =
            "com.boha.tracker.LOCATION.REQUESTED";

    @Override
    public void onReceive( Context context, Intent intent) {
        Log.w(LOG, "@@@@ LocationTrackerReceiver responding to loc request alarm. Broadcasting Request! ");

        Intent m = new Intent(BROADCAST_ACTION);
        m.putExtra(LOCATION_REQUESTED, true);
        LocalBroadcastManager.getInstance(context)
                .sendBroadcast(m);
    }


    static final String LOG = LocationTrackerReceiver.class.getSimpleName();
}

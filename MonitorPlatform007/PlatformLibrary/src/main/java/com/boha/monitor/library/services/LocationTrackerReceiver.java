package com.boha.monitor.library.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.Date;

public class LocationTrackerReceiver extends BroadcastReceiver {
    public LocationTrackerReceiver() {
    }



    @Override
    public void onReceive( Context context, Intent intent) {
        Log.d(LOG,"### onReceive, starting GPSService... " + new Date().toString());
        //Intent x = new Intent(context,GPSService.class);
        Intent x = new Intent(context,GPSLocationService.class);
        context.startService(x);
    }


    static final String LOG = LocationTrackerReceiver.class.getSimpleName();
}

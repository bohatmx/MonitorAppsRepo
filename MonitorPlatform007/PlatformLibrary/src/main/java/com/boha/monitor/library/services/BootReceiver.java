package com.boha.monitor.library.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.boha.monitor.library.util.MonLog;


public class BootReceiver extends BroadcastReceiver {
    public BootReceiver() {
    }

    @Override
    public void onReceive(Context context,  Intent intent) {
        Log.e(LOG, "### ########## -----------------------------------> BootReceiver onReceive, intent: " + intent);

        //Broadcast to LocationTrackerReceiver
        MonLog.e(context,LOG,"+++++ Broadcasting request for location");
        Intent m = new Intent(LocationTrackerReceiver.BROADCAST_ACTION);
        m.putExtra(LocationTrackerReceiver.LOCATION_REQUESTED, true);
        LocalBroadcastManager.getInstance(context)
                .sendBroadcast(m);
    }


    static final String LOG = BootReceiver.class.getSimpleName();
}

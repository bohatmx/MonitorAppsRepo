package com.boha.monitor.library.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;


public class BootReceiver extends BroadcastReceiver {
    public BootReceiver() {
    }

    @Override
    public void onReceive(Context context,  Intent intent) {
        Log.e(LOG, "### ########## -----------------------------------> BootReceiver onReceive, intent: " + intent);

    }


    static final String LOG = BootReceiver.class.getSimpleName();
}

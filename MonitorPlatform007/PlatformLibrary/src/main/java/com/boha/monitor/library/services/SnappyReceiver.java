package com.boha.monitor.library.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.Date;

public class SnappyReceiver extends BroadcastReceiver {
    public SnappyReceiver() {
    }



    @Override
    public void onReceive( Context context, Intent intent) {
        Log.d(LOG,"### onReceive, starting RequestSyncService... " + new Date().toString());
        Intent x = new Intent(context,RequestIntentService.class);
        context.startService(x);
    }


    static final String LOG = SnappyReceiver.class.getSimpleName();
}

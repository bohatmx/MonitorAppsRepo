package com.boha.monitor.library.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.Date;

public class RequestAlarmReceiver extends BroadcastReceiver {
    public RequestAlarmReceiver() {
    }



    @Override
    public void onReceive( Context context, Intent intent) {
        Log.e(LOG,"### onReceive, starting RequestIntentService... " + new Date().toString());
        Intent x = new Intent(context,RequestIntentService.class);
        context.startService(x);
    }


    static final String LOG = RequestAlarmReceiver.class.getSimpleName();
}

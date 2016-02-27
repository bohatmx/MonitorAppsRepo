package com.boha.monitor.library.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.Date;

public class DataRefreshReceiver extends BroadcastReceiver {
    public DataRefreshReceiver() {
    }



    @Override
    public void onReceive( Context context, Intent intent) {
        Log.e(LOG,"### onReceive, starting DataRefreshService... " + new Date().toString());
        Intent x = new Intent(context,DataRefreshService.class);
        context.startService(x);
    }


    static final String LOG = DataRefreshReceiver.class.getSimpleName();
}
//http://bohamaker.com/
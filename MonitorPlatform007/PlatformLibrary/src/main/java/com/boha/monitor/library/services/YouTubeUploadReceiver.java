package com.boha.monitor.library.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class YouTubeUploadReceiver extends BroadcastReceiver {
    public YouTubeUploadReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.w("YouTubeUploadReceiver","&&&&&&&&&&& onReceive, starting YouTubeService. intent: " + intent.toString());
        Intent m = new Intent(context,YouTubeService.class);
        context.startService(m);
    }
}

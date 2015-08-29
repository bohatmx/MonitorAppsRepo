package com.boha.monitor.setup.services;

/**
 * Created by aubreyM on 15/08/16.
 */

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.boha.monitor.library.activities.MonitorMapActivity;
import com.boha.monitor.library.dto.LocationTrackerDTO;
import com.boha.platform.library.MainActivity;
import com.boha.platform.library.R;
import com.google.android.gms.gcm.GcmListenerService;
import com.google.gson.Gson;

public class DataSetupGCMListenerService extends GcmListenerService {
    private static final Gson GSON = new Gson();

    private static final String TAG = "DataGCMListenerService";
    /**
     * Called when message is received.
     *
     * @param from SenderID of the sender.
     * @param data Data bundle containing message data as key/value pairs.
     *             For Set of keys use data.keySet().
     */
    // [START receive_message]
    @Override
    public void onMessageReceived(String from, Bundle data) {
        String message = data.getString("message");
        if (message != null) {
            Log.d(TAG, "** GCM message From: " + from);
            Log.d(TAG, "Message: " + message);
            sendNotification(message);
        } else {
            message = data.getString("track");
            LocationTrackerDTO m = GSON.fromJson(message, LocationTrackerDTO.class);
            Log.d(TAG, "** GCM message From: " + from);
            Log.d(TAG, "Track: " + message);
            sendNotification(m);
        }

    }
    static final int LOCATION_REQUEST_CODE = 7763;
    private void sendNotification(LocationTrackerDTO message) {
        Intent intent = new Intent(this, MonitorMapActivity.class);
        intent.putExtra("track", message);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                LOCATION_REQUEST_CODE, intent,
                PendingIntent.FLAG_ONE_SHOT);

        String name = "unknown";
        if (message.getMonitorName() != null) {
            name = message.getMonitorName();
        }
        if (message.getStaffName() != null) {
            name = message.getStaffName();
        }
        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_action_globe)
                .setContentTitle(name + " - " + "Current Location")
                .setContentText("This is a location sent to you")
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
    }
    /**
     * Create and show a simple notification containing the received GCM message.
     *
     * @param message GCM message received.
     */
    private void sendNotification(String message) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.glasses48)
                .setContentTitle(getString(com.boha.monitor.setup.R.string.welcome_msg))
                .setContentText(message)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
    }
}
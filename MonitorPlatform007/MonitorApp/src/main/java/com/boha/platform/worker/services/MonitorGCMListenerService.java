package com.boha.platform.worker.services;

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
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.boha.monitor.library.activities.MonitorMapActivity;
import com.boha.monitor.library.dto.LocationTrackerDTO;
import com.boha.monitor.library.dto.SimpleMessageDTO;
import com.boha.platform.library.MainActivity;
import com.boha.platform.library.R;
import com.google.android.gms.gcm.GcmListenerService;
import com.google.gson.Gson;

import java.util.Date;

/**
 * MonitorGCMListenerService listens for Google Cloud Messaging (GCM) messages
 * and directs the response to the appropriate handler. Messages are cached on the
 * device and where appropriate, a notification is set up and dispatched.
 */
public class MonitorGCMListenerService extends GcmListenerService {

    private static final Gson GSON = new Gson();
    private static final String TAG = "MonGCMListenerService";

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
        Log.i(TAG, "######onMessageReceived, data: " + data.toString());
        String message = data.getString("message");
        if (message != null) {
            Log.d(TAG, "** GCM message From: " + from);
            Log.d(TAG, "Message: " + message);
            sendNotification(message);
            return;
        }
        message = data.getString("track");
        if (message != null) {
            LocationTrackerDTO m = GSON.fromJson(message, LocationTrackerDTO.class);
            Log.d(TAG, "** GCM track message From: " + from);
            Log.d(TAG, "Track: " + message);
            sendNotification(m);
            return;
        }
        message = data.getString("simpleMessage");
        if (message != null) {
            SimpleMessageDTO m = GSON.fromJson(message, SimpleMessageDTO.class);
            m.setDateReceived(new Date().getTime());
            if (m.getLocationRequest() == null) {
                m.setLocationRequest(Boolean.FALSE);
            }
            Log.d(TAG, "** GCM simpleMessage From: " + from);
            Log.d(TAG, "SimpleMessage: " + m.getMessage());
            broadcastMessage(m);
            return;
        }

    }

    public static final String LOCATION_REQUESTED = "locationRequested";
    public static final String BROADCAST_ACTION =
            "com.boha.monitor.LOCATION.REQUESTED";

    /**
     * Cache incoming SimpleMessageDTO on the device and send
     * notification when done.
     *
     * @param message
     */
    private void broadcastMessage(final SimpleMessageDTO message) {
        if (message.getLocationRequest().equals(Boolean.TRUE)) {
            //todo use broadcast service to ask for location from MonitormainActivity
            Log.w(TAG, "@@@@ MonGCMListenerService responding to loc request. Broadcasting Request! ");

            Intent m = new Intent(BROADCAST_ACTION);
            m.putExtra(LOCATION_REQUESTED, true);
            m.putExtra("simpleMessage",message);
            LocalBroadcastManager.getInstance(getApplicationContext())
                    .sendBroadcast(m);
            return;
        }
       sendNotification(message);
    }

    /**
     * Build and send SimpleMessageDTO notification
     *
     * @param simpleMessage
     */
    private void sendNotification(SimpleMessageDTO simpleMessage) {
//        Intent intent = new Intent(this, SimpleMessagingActivity.class);
//        intent.putExtra("simpleMessage", simpleMessage);
//        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//        PendingIntent pendingIntent = PendingIntent.getActivity(this,
//                LOCATION_REQUEST_CODE, intent,
//                PendingIntent.FLAG_ONE_SHOT);

        String name = "unknown";
        if (simpleMessage.getMonitorName() != null) {
            name = simpleMessage.getMonitorName();
        }
        if (simpleMessage.getStaffName() != null) {
            name = simpleMessage.getStaffName();
        }
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.glasses)
                .setContentTitle(name + " - " + "Message received")
                .setContentText(simpleMessage.getMessage())
                .setAutoCancel(true)
                .setSound(defaultSoundUri);
//                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
    }

    static final int LOCATION_REQUEST_CODE = 7763;

    /**
     * Build and send LocationTrackerDTO notification
     * This notification will display map when clicked
     *
     * @param track
     * @see MonitorMapActivity
     */
    private void sendNotification(LocationTrackerDTO track) {
        Intent intent = new Intent(this, MonitorMapActivity.class);
        intent.putExtra("track", track);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                LOCATION_REQUEST_CODE, intent,
                PendingIntent.FLAG_ONE_SHOT);

        String name = "unknown";
        if (track.getMonitorName() != null) {
            name = track.getMonitorName();
        }
        if (track.getStaffName() != null) {
            name = track.getStaffName();
        }
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.glasses)
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

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.glasses)
                .setContentTitle(getString(R.string.welcome_msg))
                .setContentText(message)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
    }
}
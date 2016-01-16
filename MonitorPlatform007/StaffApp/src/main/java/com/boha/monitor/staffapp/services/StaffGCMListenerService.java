package com.boha.monitor.staffapp.services;

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
import com.boha.monitor.library.activities.SimpleMessagingActivity;
import com.boha.monitor.library.dto.LocationTrackerDTO;
import com.boha.monitor.library.dto.ResponseDTO;
import com.boha.monitor.library.dto.SimpleMessageDTO;
import com.boha.monitor.library.util.CacheUtil;
import com.boha.platform.library.MainActivity;
import com.boha.platform.library.R;
import com.google.android.gms.gcm.GcmListenerService;
import com.google.gson.Gson;

import java.util.Date;

/**
 * This service listens for incoming Google Cloud Messaging messages.
 * It creates and sends notifications based on the type of message received.
 */
public class StaffGCMListenerService extends GcmListenerService {

    private static final Gson GSON = new Gson();
    private static final String TAG = "StaGCMListenerService";

    /**
     * Called when a Google Cloud Messaging message is received.
     *
     * @param from SenderID of the sender.
     * @param data Data bundle containing message data as key/value pairs.
     *             For Set of keys use data.keySet().
     */
    @Override
    public void onMessageReceived(String from, Bundle data) {
        Log.i(TAG,"###### onMessageReceived, data: " + data.toString());
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
            cacheMessage(m);
            return;
        }

    }

    /**
     * Cache the received SimpleMessageDTO on the device
     * @param message
     */
    private void cacheMessage(final SimpleMessageDTO message) {
        if (message.getLocationTracker() != null) {
            sendNotification(message.getLocationTracker());
            return;
        }
        CacheUtil.getCachedMessages(getApplicationContext(), new CacheUtil.CacheUtilListener() {
            @Override
            public void onFileDataDeserialized(ResponseDTO response) {
                response.getSimpleMessageList().add(message);
                CacheUtil.cacheMessages(getApplicationContext(), response, new CacheUtil.CacheUtilListener() {
                    @Override
                    public void onFileDataDeserialized(ResponseDTO response) {

                    }

                    @Override
                    public void onDataCached() {
                        sendNotification(message);
                    }

                    @Override
                    public void onError() {

                    }
                });
            }

            @Override
            public void onDataCached() {

            }

            @Override
            public void onError() {

            }
        });
    }

    /**
     * Build and send SimpleMessageDTO notification
     * @see SimpleMessagingActivity
     * @param simpleMessage
     */
    private void sendNotification(SimpleMessageDTO simpleMessage) {
        Intent intent = new Intent(this, SimpleMessagingActivity.class);
        intent.putExtra("simpleMessage",simpleMessage);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                LOCATION_REQUEST_CODE, intent,
                PendingIntent.FLAG_ONE_SHOT);

        String name = "unknown";
        if (simpleMessage.getMonitorName() != null) {
            name = simpleMessage.getMonitorName();
        }
        if (simpleMessage.getStaffName() != null) {
            name = simpleMessage.getStaffName();
        }
        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.glasses48)
                .setContentTitle(name + " - " + "Message received")
                .setContentText(simpleMessage.getMessage())
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
    }

    static final int LOCATION_REQUEST_CODE = 7763;

    /**
     * Build and send LocationTrackerDTO notification. Tapping
     * this notification pops up the received location on a map
     * @see MonitorMapActivity
     * @param track
     */
    private void sendNotification(LocationTrackerDTO track) {
        Intent intent = new Intent(this, MonitorMapActivity.class);
        intent.putExtra("track",track);
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
        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.glasses48)
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
package com.boha.monitor.firebase.services;

import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

/**
 * Created by aubreymalabie on 5/30/16.
 */
public class MonFirebaseMessageService extends FirebaseMessagingService {


   static final String TAG = MonFirebaseMessageService.class.getSimpleName();
    @Override
    public void onMessageReceived(RemoteMessage message) {
        Log.w(TAG, "++++++++++++++++++ onMessageReceived: from: " + message.getFrom() + ": "
                + message.getNotification().getBody());
        String note = message.getNotification().getBody();
    }
    @Override
    public void onMessageSent(String messageID) {

    }
    @Override
    public void onSendError(String messageID, Exception e) {
        Log.e(TAG, "onSendError: ", e);
    }
}

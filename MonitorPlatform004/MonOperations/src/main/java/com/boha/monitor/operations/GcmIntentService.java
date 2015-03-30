package com.boha.monitor.operations;


import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.boha.monitor.library.R;
import com.com.boha.monitor.library.activities.ChatActivity;
import com.com.boha.monitor.library.activities.MonApp;
import com.com.boha.monitor.library.activities.TaskStatusNotificationActivity;
import com.com.boha.monitor.library.dto.ChatMessageDTO;
import com.com.boha.monitor.library.dto.ProjectSiteTaskStatusDTO;
import com.com.boha.monitor.library.dto.transfer.ResponseDTO;
import com.com.boha.monitor.library.util.CacheUtil;
import com.com.boha.monitor.library.util.GCMUtil;
import com.google.android.gcm.GCMBaseIntentService;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.gson.Gson;

import java.util.ArrayList;

public class GcmIntentService extends GCMBaseIntentService {
	public static final int NOTIFICATION_ID = 1;
	private NotificationManager mNotificationManager;
	NotificationCompat.Builder builder;

	public GcmIntentService() {
		super(GCMUtil.GCM_SENDER_ID);
	}

	@Override
	protected void onError(Context arg0, String arg1) {
		Log.i(TAG, "onError ... " + arg1);

	}

	@Override
	protected void onMessage(Context arg0, Intent intent) {
		Log.w(TAG, "onMessage ..:..gcm message here... " + intent.getExtras().toString());
		Bundle extras = intent.getExtras();
		GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
		String messageType = gcm.getMessageType(intent);
		Log.d(TAG, "GCM messageType = " + messageType);
		if (!extras.isEmpty()) { 
			if (GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR
					.equals(messageType)) {
				Log.e(TAG, "GoogleCloudMessaging - MESSAGE_TYPE_SEND_ERROR");
			} else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED
					.equals(messageType)) {
				Log.e(TAG, "GoogleCloudMessaging - MESSAGE_TYPE_SEND_ERROR");
				
			} else if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE
					.equals(messageType)) {

				sendNotification(intent);
			}
		}
		// Release the wake lock provided by the WakefulBroadcastReceiver.
		//GcmBroadcastReceiver.completeWakefulIntent(intent);

	}

	@Override
	protected void onRegistered(Context arg0, String arg1) {
		Log.i(TAG, "onRegistered ... " + arg1);

	}

    ChatMessageDTO chatMessageDTO;
	@Override
	protected void onUnregistered(Context arg0, String arg1) {
		Log.i(TAG, "onUnRegistered ... " + arg1);
	}
	Gson gson = new Gson();
    static final int CHAT_MESSAGE = 1, STATUS_MESSAGE = 2, STRING_MESSAGE = 3;
	private void sendNotification(Intent msgIntent) {
		Log.w(TAG,"## sendNotification ...");
		mNotificationManager = (NotificationManager) this
				.getSystemService(NOTIFICATION_SERVICE);
		String message = msgIntent.getExtras().getString("message");
        String contentText = "";
		ProjectSiteTaskStatusDTO dto = null;
        int type = STRING_MESSAGE;
		try {
			dto = gson.fromJson(message, ProjectSiteTaskStatusDTO.class);
            type = STATUS_MESSAGE;
            contentText = "Task updated";
		} catch (Exception e) {
			Log.d(TAG, "not a task status message ");
		}

        try {
            chatMessageDTO = gson.fromJson(message, ChatMessageDTO.class);
            type = CHAT_MESSAGE;
            contentText = chatMessageDTO.getMessage();
        } catch (Exception e) {
            Log.d(TAG, "not a chat message ");
        }

        Intent resultIntent = null;
        switch (type) {
            case CHAT_MESSAGE:
                resultIntent = new Intent(this, ChatActivity.class);
                resultIntent.putExtra("message", chatMessageDTO);
                Log.i(TAG,"########## this IS a chatMessage: " + chatMessageDTO.getMessage());
                CacheUtil.getCachedData(getApplicationContext(),CacheUtil.CACHE_CHAT, new CacheUtil.CacheUtilListener() {
                    @Override
                    public void onFileDataDeserialized(ResponseDTO response) {
                        if (response.getChatMessageList() == null) {
                            response.setChatMessageList(new ArrayList<ChatMessageDTO>());
                        }
                        response.getChatMessageList().add(0,chatMessageDTO);
                        CacheUtil.cacheData(getApplicationContext(),response,CacheUtil.CACHE_CHAT,new CacheUtil.CacheUtilListener() {
                            @Override
                            public void onFileDataDeserialized(ResponseDTO response) {

                            }

                            @Override
                            public void onDataCached() {
                                MonApp app = (MonApp) getApplication();
                                if (app.isChatActivityVisible() && chatMessageDTO != null) {
                                    Log.e(TAG,"****** ChatActivity is up and visible, REFRESHING!!");
                                    ChatActivity x = app.getChatActivity();
                                    if (x != null) {
                                        x.refresh();
                                    }
                                    return;
                                }
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
                break;
            case STATUS_MESSAGE:
                resultIntent = new Intent(this, TaskStatusNotificationActivity.class);
                resultIntent.putExtra("taskStatus", dto);
                Log.i(TAG,"## this IS a taskStatus");
                break;
            case STRING_MESSAGE:
                resultIntent = new Intent(this, ChatActivity.class);
                resultIntent.putExtra("string", message);
                contentText = message;
                Log.i(TAG,"## message is just a string: " + message);
                break;
        }

        if (resultIntent == null) {
            return;
        }

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(OperationsPagerActivity.class);
        stackBuilder.addNextIntent(resultIntent);

        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                .setContentIntent(resultPendingIntent)
                .addAction(R.drawable.ic_action_overflow, "More", resultPendingIntent)
                .setSmallIcon(R.drawable.light_32)
				.setContentTitle("MonitorMessage")
				.setContentText(contentText);

		mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
        Log.i(TAG,"#### sendNotification DONE!!");
	}
	
	static final String TAG = "GcmIntentService";

}
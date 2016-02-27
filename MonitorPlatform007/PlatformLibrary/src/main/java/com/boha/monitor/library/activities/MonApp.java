package com.boha.monitor.library.activities;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.os.Bundle;
import android.os.StrictMode;
import android.os.SystemClock;
import android.support.multidex.MultiDex;
import android.util.Log;

import com.boha.monitor.library.dto.MonitorDTO;
import com.boha.monitor.library.dto.StaffDTO;
import com.boha.monitor.library.services.DataRefreshReceiver;
import com.boha.monitor.library.services.LocationTrackerReceiver;
import com.boha.monitor.library.services.RequestAlarmReceiver;
import com.boha.monitor.library.util.SharedUtil;
import com.boha.monitor.library.util.Statics;
import com.boha.platform.library.R;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;
import com.snappydb.DB;
import com.snappydb.DBFactory;
import com.snappydb.SnappydbException;
import com.squareup.picasso.OkHttpDownloader;
import com.squareup.picasso.Picasso;

import org.acra.ACRA;
import org.acra.ReportField;
import org.acra.annotation.ReportsCrashes;

import java.io.File;
import java.util.HashMap;


/**
 * Created by aubreyM on 2014/05/17.
 * Copyright (c) 2014 Aubrey Malabie. All rights reserved.
 */


@ReportsCrashes(
//        formKey = "",
        formUri = Statics.CRASH_REPORTS_URL,
        customReportContent = {
                ReportField.APP_VERSION_NAME,
                ReportField.APP_VERSION_CODE,
                ReportField.ANDROID_VERSION,
                ReportField.PHONE_MODEL,
                ReportField.BRAND,
                ReportField.STACK_TRACE,
                ReportField.PACKAGE_NAME,
                ReportField.CUSTOM_DATA,
                ReportField.LOGCAT},
        socketTimeout = 10000
)
/**
 * Main Application for Monitor Apps. Sets up a number of services and capabilities:
 * Google Analytics
 * Picasso
 * ACRA crash reporting //todo replace with something else
 * Start device location tracker alarm
 */
public class MonApp extends Application implements Application.ActivityLifecycleCallbacks {
    static final String PROPERTY_ID = "UA-53661372-2";
    HashMap<TrackerName, Tracker> mTrackers = new HashMap<>();
    private AlarmManager alarmMgr1, alarmMgr2, alarmMgr3;
    private PendingIntent alarmIntent1,alarmIntent2, alarmIntent3;
    private ChatMessageListActivity chatMessageListActivity;
    private boolean messageActivityVisible;
    static final String LOG = MonApp.class.getSimpleName();
    public static Picasso picasso;
    public DB snappyDB;

    public DB getSnappyDB() {
        try {
            if (snappyDB == null || !snappyDB.isOpen()) {
                snappyDB = DBFactory.open(getApplicationContext());
            }
            if (snappyDB.isOpen()) {
                //Log.e(LOG,"getSnappyDB - database is OPEN!");
            }
        } catch (SnappydbException e) {
            e.printStackTrace();
        }

        return snappyDB;
    }


    static final long MAX_CACHE_SIZE = 1024 * 1024 * 1024; // 1 GB cache on device
//
//    public static RefWatcher getRefWatcher(Context context) {
//        MonApp application = (MonApp) context.getApplicationContext();
//        return application.refWatcher;
//    }
//
//    private RefWatcher refWatcher;

    public enum TrackerName {
        APP_TRACKER, // Tracker used only in this app.
        GLOBAL_TRACKER, // Tracker used by all the apps from a company. eg: roll-up tracking.
        ECOMMERCE_TRACKER, // Tracker used by all ecommerce transactions from a company.
    }

    public synchronized Tracker getTracker(TrackerName trackerId) {
        if (!mTrackers.containsKey(trackerId)) {
            GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
            Tracker t = null;
            if (trackerId == TrackerName.APP_TRACKER) {
                t = analytics.newTracker(PROPERTY_ID);
            }
            if (trackerId == TrackerName.GLOBAL_TRACKER) {
                t = analytics.newTracker(R.xml.global_tracker);
            }
            mTrackers.put(trackerId, t);
        }
        Log.i(LOG, "## analytics tracker ID: " + trackerId.toString());
        return mTrackers.get(trackerId);
    }

    @Override
    public void onCreate() {
        MultiDex.install(getApplicationContext());
        super.onCreate();
        StringBuilder sb = new StringBuilder();
        sb.append("\n\n\n#######################################\n");
        sb.append("#######################################\n");
        sb.append("###\n");
        sb.append("###  Monitor App has started, setting up resources ...............\n");
        sb.append("###\n");
        sb.append("#######################################\n\n");

        Log.d(LOG, sb.toString());
        boolean isDebuggable = 0 != (getApplicationInfo().flags &= ApplicationInfo.FLAG_DEBUGGABLE);
        //refWatcher = LeakCanary.install(this);
        registerActivityLifecycleCallbacks(this);

        // create Picasso.Builder object
        File picassoCacheDir = getCacheDir();
        Log.w(LOG, "####### images in picasso cache: " + picassoCacheDir.listFiles().length);
        Picasso.Builder picassoBuilder = new Picasso.Builder(getApplicationContext());
        picassoBuilder.downloader(new OkHttpDownloader(picassoCacheDir, MAX_CACHE_SIZE));
        picasso = picassoBuilder.build();
        try {
            Picasso.setSingletonInstance(picasso);
        } catch (IllegalStateException ignored) {
            // Picasso instance was already set
            // cannot set it after Picasso.with(Context) was already in use
        }
        //SnappyDB
        try {
            snappyDB = DBFactory.open(getApplicationContext());
            Log.w(LOG,"################ SnappyDB has been opened");
        } catch (SnappydbException e) {
            e.printStackTrace();
        }

        if (isDebuggable) {
            Picasso.with(getApplicationContext())
                    .setIndicatorsEnabled(false);
            Picasso.with(getApplicationContext())
                    .setLoggingEnabled(false);
        }
        if (!isDebuggable) {
            StrictMode.enableDefaults();
            Log.e(LOG, "###### StrictMode defaults enabled");
            ACRA.init(this);
            StaffDTO staff = SharedUtil.getCompanyStaff(getApplicationContext());
            MonitorDTO mon = SharedUtil.getMonitor(getApplicationContext());
            if (staff != null) {
                ACRA.getErrorReporter().putCustomData("staffID", "" + staff.getStaffID());
            }
            if (mon != null) {
                ACRA.getErrorReporter().putCustomData("monitorID", "" + mon.getMonitorID());
            }

            Log.e(LOG, "###### ACRA Crash Reporting has been initiated");
        } else {
            Log.d(LOG, "###### ACRA Crash Reporting has NOT been initiated, in DEBUG mode");
        }

        startDataRefreshAlarm();
        startRequestCacheAlarm();
        startLocationAlarm();

    }

    public void startLocationAlarm() {
        alarmMgr1 = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        Intent m = new Intent(getApplicationContext(), LocationTrackerReceiver.class);
        alarmIntent1 = PendingIntent.getBroadcast(getApplicationContext(), 123, m, 0);

        alarmMgr1.setInexactRepeating(AlarmManager.ELAPSED_REALTIME,
                SystemClock.elapsedRealtime(), HALF_HOUR, alarmIntent1);

        Log.d(LOG, "###### Location AlarmManager: alarm set to pull the device tracker trigger every: HALF_HOUR");
    }
    public void startRequestCacheAlarm() {
        alarmMgr2 = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        Intent m = new Intent(getApplicationContext(), RequestAlarmReceiver.class);
        alarmIntent2 = PendingIntent.getBroadcast(getApplicationContext(), 124, m, 0);

        alarmMgr2.setInexactRepeating(AlarmManager.ELAPSED_REALTIME,
                SystemClock.elapsedRealtime(), FIVE_MINUTES, alarmIntent2);

        Log.d(LOG, "###### Request Cache AlarmManager: alarm set to send cached requests every: FIVE_MINUTES");
    }
    public void startDataRefreshAlarm() {
        alarmMgr3 = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        Intent m = new Intent(getApplicationContext(), DataRefreshReceiver.class);
        alarmIntent3 = PendingIntent.getBroadcast(getApplicationContext(), 125, m, 0);

        alarmMgr3.setInexactRepeating(AlarmManager.ELAPSED_REALTIME,
                SystemClock.elapsedRealtime(), HOUR, alarmIntent3);

        Log.w(LOG, "###### Data Refresh AlarmManager: alarm set to refresh data every: HOUR");
    }

    static final int
            ONE_MINUTE = 60 * 1000,
            FIVE_MINUTES = ONE_MINUTE * 5,
            FIFTEEN_MINUTES = FIVE_MINUTES * 3,
            HALF_HOUR = FIFTEEN_MINUTES * 2,
            HOUR = HALF_HOUR * 2;


    public ChatMessageListActivity getChatMessageListActivity() {
        return chatMessageListActivity;
    }

    public void refreshChatMessages() {
        if (chatMessageListActivity != null) {
            chatMessageListActivity.refreshMessages();
        }
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        if (activity instanceof ChatMessageListActivity) {
            chatMessageListActivity = (ChatMessageListActivity) activity;
            messageActivityVisible = true;
            Log.d(LOG, "ChatMessageListActivity onActivityCreated, messageActivityVisible: " + messageActivityVisible);
        }
    }

    @Override
    public void onActivityStarted(Activity activity) {
            Log.d(LOG, "$$$$ onActivityStarted: " + activity.getPackageName()
             + " " + activity.getComponentName());

    }

    @Override
    public void onActivityResumed(Activity activity) {
        if (activity instanceof ChatMessageListActivity) {
            messageActivityVisible = true;
            chatMessageListActivity = (ChatMessageListActivity) activity;
            Log.d(LOG, "ChatMessageListActivity onActivityResumed, messageActivityVisible: " + messageActivityVisible);
        }
    }

    @Override
    public void onActivityPaused(Activity activity) {
        if (activity instanceof ChatMessageListActivity) {
            messageActivityVisible = false;
            Log.d(LOG, "ChatMessageListActivity onActivityPaused, messageActivityVisible: " + messageActivityVisible);
        }
    }

    @Override
    public void onActivityStopped(Activity activity) {
        if (activity instanceof ChatMessageListActivity) {
            messageActivityVisible = false;
            Log.d(LOG, "ChatMessageListActivity onActivityStopped, messageActivityVisible: " + messageActivityVisible);
        }
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

    }

    @Override
    public void onActivityDestroyed(Activity activity) {
        if (activity instanceof ChatMessageListActivity) {
            messageActivityVisible = false;
            Log.d(LOG, "ChatMessageListActivity onActivityDestroyed, messageActivityVisible: " + messageActivityVisible);
        }
    }

    public boolean isMessageActivityVisible() {
        return messageActivityVisible;
    }


    public final static int THEME_BLUE = 20;
    public final static int THEME_INDIGO = 1;
    public final static int THEME_RED = 2,
            THEME_TEAL = 3,
            THEME_BLUE_GRAY = 4,
            THEME_ORANGE = 5,
            THEME_PINK = 6,
            THEME_CYAN = 7,
            THEME_GREEN = 8,
            THEME_LIGHT_GREEN = 9,
            THEME_LIME = 10,
            THEME_AMBER = 11,
            THEME_GREY = 12,
            THEME_BROWN = 14,
            THEME_PURPLE = 15;
}


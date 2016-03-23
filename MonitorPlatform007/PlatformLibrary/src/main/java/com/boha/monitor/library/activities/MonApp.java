package com.boha.monitor.library.activities;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.pm.ApplicationInfo;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.multidex.MultiDex;
import android.util.Log;

import com.boha.monitor.library.dto.MonitorDTO;
import com.boha.monitor.library.dto.StaffDTO;
import com.boha.monitor.library.services.DataTaskService;
import com.boha.monitor.library.tasks.PhotoTaskService;
import com.boha.monitor.library.tasks.RequestsTaskService;
import com.boha.monitor.library.tasks.TrackerService;
import com.boha.monitor.library.tasks.YouTubeTaskService;
import com.boha.monitor.library.util.SharedUtil;
import com.boha.monitor.library.util.Statics;
import com.boha.platform.library.R;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.gcm.GcmNetworkManager;
import com.google.android.gms.gcm.PeriodicTask;
import com.snappydb.DB;
import com.snappydb.DBFactory;
import com.snappydb.SnappydbException;
import com.squareup.leakcanary.LeakCanary;
import com.squareup.leakcanary.RefWatcher;
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
    private AlarmManager alarmMgr1, alarmMgr2, alarmMgr3,alarmMgr4, alarmMgr5;
    private PendingIntent alarmIntent1,alarmIntent2, alarmIntent3,alarmIntent4, alarmIntent5;
    private boolean messageActivityVisible;
    static final String LOG = MonApp.class.getSimpleName();
    private GcmNetworkManager mGcmNetworkManager;
    public static Picasso picasso;
    private DB snappyDB;
    private  RefWatcher refWatcher;

    public  RefWatcher getRefWatcher() {
        return refWatcher;
    }

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
        refWatcher = LeakCanary.install(this);
        boolean isDebuggable = 0 != (getApplicationInfo().flags &= ApplicationInfo.FLAG_DEBUGGABLE);
        //refWatcher = LeakCanary.install(this);
        registerActivityLifecycleCallbacks(this);
        mGcmNetworkManager = GcmNetworkManager.getInstance(this);

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

        startLocationTask();
        startDataTask();
        startPhotoTask();
        startYouTubeTask();
        startRequestsTask();

    }

    static final int MINUTE_IN_SECONDS = 60,
            HALF_HOUR_IN_SECONDS = 60 * 30,
            HOUR_IN_SECONDS = 60 * 60,
            TWO_HOURS_IN_SECONDS = HOUR_IN_SECONDS * 2,
            FIVE_MINUTE_IN_SECONDS = 60 * 5;
    public static final String LOCATION_TAG = "TrackerServiceTask",
        REQUESTS_TAG = "requestsTag",
        DATA_TAG = "DataRefresh", PHOTO_TAG = "PhotoUpload", YOUTUBE_TAG = "YouTubeUpload" ;

    private void startRequestsTask() {
        PeriodicTask task = new PeriodicTask.Builder()
                .setService(RequestsTaskService.class)
                .setPeriod(HALF_HOUR_IN_SECONDS)
                .setPersisted(true)
                .setTag(REQUESTS_TAG)
                .setRequiredNetwork(PeriodicTask.NETWORK_STATE_CONNECTED)
                .build();

        mGcmNetworkManager.schedule(task);
        Log.i(LOG, "###### mGcmNetworkManager task for REQUESTS upload scheduled: " );
    }
    private void startPhotoTask() {
        PeriodicTask task = new PeriodicTask.Builder()
                .setService(PhotoTaskService.class)
                .setPeriod(HALF_HOUR_IN_SECONDS)
                .setPersisted(true)
                .setTag(PHOTO_TAG)
                .setRequiredNetwork(PeriodicTask.NETWORK_STATE_UNMETERED)
                .build();

        mGcmNetworkManager.schedule(task);
        Log.i(LOG, "###### mGcmNetworkManager task for PHOTO upload scheduled: " );
    }
    private void startYouTubeTask() {
        PeriodicTask task = new PeriodicTask.Builder()
                .setService(YouTubeTaskService.class)
                .setPeriod(HALF_HOUR_IN_SECONDS)
                .setPersisted(true)
                .setTag(YOUTUBE_TAG)
                .setRequiredNetwork(PeriodicTask.NETWORK_STATE_UNMETERED)
                .build();

        mGcmNetworkManager.schedule(task);
        Log.i(LOG, "###### mGcmNetworkManager task for YouTUBE scheduled: " );
    }
    private void startLocationTask() {
        PeriodicTask task = new PeriodicTask.Builder()
                .setService(TrackerService.class)
                .setPeriod(HALF_HOUR_IN_SECONDS)
                .setPersisted(true)
                .setTag(LOCATION_TAG)
                .setRequiredNetwork(PeriodicTask.NETWORK_STATE_CONNECTED)
                .build();

        mGcmNetworkManager.schedule(task);
        Log.i(LOG, "###### mGcmNetworkManager task for LOCATION scheduled: " );
    }
    private void startDataTask() {
        PeriodicTask task = new PeriodicTask.Builder()
                .setService(DataTaskService.class)
                .setPeriod(TWO_HOURS_IN_SECONDS)
                .setPersisted(true)
                .setTag(DATA_TAG)
                .setRequiredNetwork(PeriodicTask.NETWORK_STATE_UNMETERED)
                .build();

        mGcmNetworkManager.schedule(task);
        Log.i(LOG, "###### mGcmNetworkManager task for DATA scheduled: " );
    }

    static final int
            ONE_MINUTE = 60 * 1000,
            TWO_MINUTES = ONE_MINUTE * 2,
            FIVE_MINUTES = ONE_MINUTE * 5,
            FIFTEEN_MINUTES = FIVE_MINUTES * 3,
            HALF_HOUR = FIFTEEN_MINUTES * 2,
            HOUR = HALF_HOUR * 2,
            THREE_HOURS = HOUR * 3;



    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

    }

    @Override
    public void onActivityStarted(Activity activity) {
            Log.d(LOG, "$$$$ onActivityStarted: " + activity.getPackageName()
             + " " + activity.getComponentName());

    }

    @Override
    public void onActivityResumed(Activity activity) {

    }

    @Override
    public void onActivityPaused(Activity activity) {

    }

    @Override
    public void onActivityStopped(Activity activity) {

    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

    }

    @Override
    public void onActivityDestroyed(Activity activity) {

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


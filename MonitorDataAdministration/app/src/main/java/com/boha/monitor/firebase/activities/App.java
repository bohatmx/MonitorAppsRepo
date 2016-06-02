package com.boha.monitor.firebase.activities;

import android.support.multidex.MultiDexApplication;
import android.util.Log;

import com.firebase.client.Firebase;
import com.google.firebase.analytics.FirebaseAnalytics;

/**
 * Created by aubreymalabie on 5/24/16.
 */
public class App extends MultiDexApplication {

    static final String TAG = App.class.getSimpleName();

    FirebaseAnalytics mFirebaseAnalytics;
    @Override
    public void onCreate() {
        super.onCreate();
        Log.e(TAG, "####################### Monitor App, starting ..............." );
        Firebase.setAndroidContext(getApplicationContext());
        Log.w(TAG, "++++ onCreate: ######### Firebase Android Context set" );

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        Log.w(TAG, "++++++++ onCreate: FirebaseAnalytics has been initialized" );
    }
}

package com.boha.supervisor.m35.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * Created by aubreymalabie on 5/22/16.
 */

public class Util {

    public static final String COMPANIES_KEY = "companies";

    static final String TAG = Util.class.getSimpleName();
    public static void setCompaniesKey(Context ctx, String key) {
        SharedPreferences sp = PreferenceManager
                .getDefaultSharedPreferences(ctx);

        SharedPreferences.Editor ed = sp.edit();
        ed.putString(COMPANIES_KEY, key);
        ed.commit();

        Log.w(TAG, "#### companies key saved: " + key);

    }
    public static String getCompaniesKey(Context ctx) {
        SharedPreferences sp = PreferenceManager
                .getDefaultSharedPreferences(ctx);
        String key = sp.getString(COMPANIES_KEY,null);
        if (key != null) {
            Log.w(TAG, "#### companies key retrieved: " + key);
        }
        return key;

    }
}

package com.boha.monitor.firebase.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * Created by aubreymalabie on 5/30/16.
 */
public class SharedUtil {

    static final String EMAIL = "email", PASSWORD = "passwd", TAG = SharedUtil.class.getSimpleName();

    public static void setCreds(String email, String password, Context ctx) {

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(ctx);

        SharedPreferences.Editor e = sp.edit();
        if (email != null)
            e.putString(EMAIL, email);
        if (password != null)
            e.putString(PASSWORD, password);
        e.commit();

        Log.i(TAG, "setCreds: credentials saved: " + email);

    }

    public static String getEmail(Context ctx) {

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(ctx);
        String email = sp.getString(EMAIL, null);
        if (email != null)
            Log.i(TAG, "getEmail: email retrieved: " + email);
        return email;

    }

    public static String getPassword(Context ctx) {

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(ctx);
        String passwd = sp.getString(PASSWORD, null);
        if (passwd != null)
            Log.i(TAG, "getPassword: password retrieved: ");
        return passwd;

    }
}

package com.boha.monitor.library.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.boha.monitor.library.data.MonitorCompanyDTO;
import com.boha.monitor.library.data.UserDTO;
import com.google.gson.Gson;

/**
 * Created by aubreymalabie on 5/30/16.
 */
public class SharedUtil {

    static final String EMAIL = "email",
    TOKEN = "token",
    PASSWORD = "passwd", TAG = SharedUtil.class.getSimpleName();

    public static void setFCMtoken(String token, Context ctx) {

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(ctx);

        SharedPreferences.Editor e = sp.edit();
            e.putString(TOKEN, token);
        e.commit();

        Log.i(TAG, "setFCMtoken: token saved: " + token);

    }

    public static String getFCMtoken(Context ctx) {

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(ctx);
        String token = sp.getString(TOKEN, null);
        if (token != null)
            Log.i(TAG, "getFCMtoken: FCM token retrieved: " + token);
        return token;

    }
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
    public static void setCompany(MonitorCompanyDTO co, Context ctx) {

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(ctx);

        SharedPreferences.Editor e = sp.edit();
        e.putString("company",gson.toJson(co));
        e.commit();

        Log.i(TAG, "setCompany: company saved: " + co.getCompanyName());

    }

    public static MonitorCompanyDTO getCompany(Context ctx) {

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(ctx);
        String json = sp.getString("company", null);
        if (json != null) {
            Log.i(TAG, "getCompany: company retrieved: " + json);
            return gson.fromJson(json,MonitorCompanyDTO.class);
        }

        return null;

    }
    public static void setUser(UserDTO user, Context ctx) {

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(ctx);

        SharedPreferences.Editor e = sp.edit();
        e.putString("user",gson.toJson(user));
        e.commit();

        Log.i(TAG, "setUser, User saved: " + user.getFullName());

    }

    public static UserDTO getUser(Context ctx) {

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(ctx);
        String json = sp.getString("user", null);
        if (json != null) {
            Log.i(TAG, "getUser: user retrieved: " + json);
            return gson.fromJson(json,UserDTO.class);
        }

        return null;

    }
    static final Gson gson = new Gson();
}

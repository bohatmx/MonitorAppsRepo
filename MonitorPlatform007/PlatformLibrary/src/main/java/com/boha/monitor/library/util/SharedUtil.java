package com.boha.monitor.library.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;
import android.util.Log;

import com.boha.monitor.library.dto.CompanyDTO;
import com.boha.monitor.library.dto.CreditCardDTO;
import com.boha.monitor.library.dto.GcmDeviceDTO;
import com.boha.monitor.library.dto.MonitorDTO;
import com.boha.monitor.library.dto.PhotoUploadDTO;
import com.boha.monitor.library.dto.StaffDTO;
import com.google.gson.Gson;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Created by aubreyM on 2014/10/12.
 */
public class SharedUtil {
    static final Gson gson = new Gson();
    public static final String
            COMPANY_STAFF_JSON = "companyStaff",
            COMPANY_JSON = "company",
            GCMDEVICE = "gcmd",
            MONITOR_JSON = "monitor",
            PROJECT_ID = "projectID",
            GCM_REGISTRATION_ID = "gcm",
            SESSION_ID = "sessionID",
            SITE_LOCATION = "siteLocation",
            DRAWER = "drawer",
            THEME = "theme",
            CREDIT_CARD = "credCard",
            PHOTO = "photo",
            LOG = "SharedUtil",
            REMINDER_TIME = "reminderTime",
            APP_VERSION = "appVersion";
    public static void setThemeSelection(Context ctx, int theme) {
        SharedPreferences sp = PreferenceManager
                .getDefaultSharedPreferences(ctx);

        SharedPreferences.Editor ed = sp.edit();
        ed.putInt(THEME, theme);
        ed.commit();

        Log.w(LOG, "#### theme saved: " + theme);

    }
    public static int getThemeSelection(Context ctx) {
        SharedPreferences sp = PreferenceManager
                .getDefaultSharedPreferences(ctx);
        int j = sp.getInt(THEME, -1);
        Log.i(LOG, "#### theme retrieved: " + j);
        return j;
    }
    public static void setDrawerCount(Context ctx, int count) {
        SharedPreferences sp = PreferenceManager
                .getDefaultSharedPreferences(ctx);
        SharedPreferences.Editor ed = sp.edit();
        ed.putInt(DRAWER, count);
        ed.commit();
    }
    public static int getDrawerCount( Context context) {
        final SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(context);
        int count = prefs.getInt(DRAWER, 0);

        return count;
    }
    public static final int MAX_SLIDING_TAB_VIEWS = 1000;
    /**
     * Stores the registration ID and app versionCode in the application's
     * {@code SharedPreferences}.
     *
     * @param context application's context.
     * @param regId   registration ID
     */
    public static void storeRegistrationId( Context context, String regId) {
        final SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(context);
        int appVersion = getAppVersion(context);
        Log.i(LOG, "Saving regId on app version " + appVersion);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(GCM_REGISTRATION_ID, regId);
        editor.putInt(APP_VERSION, appVersion);
        editor.commit();
        Log.e(LOG, "GCM registrationId saved in prefs! Yebo!!!");
    }

    /**
     * Gets the current registration ID for application on GCM service.
     * <p/>
     * If result is empty, the app needs to register.
     *
     * @return registration ID, or empty string if there is no existing
     * registration ID.
     */
    public static String getRegistrationId( Context context) {
        final SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(context);
        String registrationId = prefs.getString(GCM_REGISTRATION_ID, null);
        if (registrationId == null) {
            Log.i(LOG, "GCM Registration ID not found on device.");
            return null;
        }
        // Check if app was updated; if so, it must clear the registration ID
        // since the existing regID is not guaranteed to work with the new
        // app version.
        int registeredVersion = prefs.getInt(APP_VERSION, Integer.MIN_VALUE);
        int currentVersion = SharedUtil.getAppVersion(context);
        if (registeredVersion != currentVersion) {
            Log.i(LOG, "App version changed.");
            return null;
        }
        return registrationId;
    }
    public static void saveReminderTime( Context ctx,  Date date) {

        SharedPreferences sp = PreferenceManager
                .getDefaultSharedPreferences(ctx);
        SharedPreferences.Editor ed = sp.edit();
        ed.putLong(REMINDER_TIME, date.getTime());
        ed.commit();
        Log.e("SharedUtil", "%%%%% reminderTime: " + date + " saved in SharedPreferences");
    }

    public static Date getReminderTime( Context ctx) {

        SharedPreferences sp = PreferenceManager
                .getDefaultSharedPreferences(ctx);
        long t = sp.getLong(REMINDER_TIME, 0);
        if (t == 0) {
            Calendar cal = GregorianCalendar.getInstance();
            cal.roll(Calendar.DAY_OF_YEAR, false);
            return cal.getTime();
        }
        return new Date(t);
    }
    public static void saveSessionID( Context ctx, String sessionID) {

        SharedPreferences sp = PreferenceManager
                .getDefaultSharedPreferences(ctx);
        SharedPreferences.Editor ed = sp.edit();
        ed.putString(SESSION_ID, sessionID);
        ed.commit();
        Log.e("SharedUtil", "%%%%% SessionID: " + sessionID + " saved in SharedPreferences");
    }


    public static String getSessionID( Context ctx) {

        SharedPreferences sp = PreferenceManager
                .getDefaultSharedPreferences(ctx);
        return sp.getString(SESSION_ID, null);
    }
    public static void saveMonitor( Context ctx,  MonitorDTO dto) {

        SharedPreferences sp = PreferenceManager
                .getDefaultSharedPreferences(ctx);
        String x = gson.toJson(dto);
        SharedPreferences.Editor ed = sp.edit();
        ed.putString(MONITOR_JSON, x);
        ed.commit();
        Log.e("SharedUtil", "%%%%% Monitor: " + dto.getFirstName() + " " + dto.getLastName() + " saved in SharedPreferences");
    }


    public static MonitorDTO getMonitor( Context ctx) {

        SharedPreferences sp = PreferenceManager
                .getDefaultSharedPreferences(ctx);
        String mon = sp.getString(MONITOR_JSON, null);
        MonitorDTO monitorDTO = null;
        if (mon != null) {
            monitorDTO = gson.fromJson(mon, MonitorDTO.class);

        }
        return monitorDTO;
    }
    public static void saveCompanyStaff( Context ctx,  StaffDTO dto) {

        SharedPreferences sp = PreferenceManager
                .getDefaultSharedPreferences(ctx);
        String x = gson.toJson(dto);
        SharedPreferences.Editor ed = sp.edit();
        ed.putString(COMPANY_STAFF_JSON, x);
        ed.commit();
        Log.e("SharedUtil", "%%%%% CompanyStaff: " + dto.getFirstName() + " " + dto.getLastName() + " saved in SharedPreferences");
    }


    public static StaffDTO getCompanyStaff( Context ctx) {

        SharedPreferences sp = PreferenceManager
                .getDefaultSharedPreferences(ctx);
        String adm = sp.getString(COMPANY_STAFF_JSON, null);
        StaffDTO golfGroup = null;
        if (adm != null) {
            golfGroup = gson.fromJson(adm, StaffDTO.class);

        }
        return golfGroup;
    }
    public static void saveCompany( Context ctx,  CompanyDTO dto) {

        CompanyDTO xx = new CompanyDTO();
        xx.setCompanyName(dto.getCompanyName());
        xx.setCompanyID(dto.getCompanyID());

        SharedPreferences sp = PreferenceManager
                .getDefaultSharedPreferences(ctx);
        String x = gson.toJson(xx);
        SharedPreferences.Editor ed = sp.edit();
        ed.putString(COMPANY_JSON, x);
        ed.commit();
        Log.e("SharedUtil", "%%%%% Company: " + dto.getCompanyName() + " saved in SharedPreferences");
    }


    public static CompanyDTO getCompany( Context ctx) {

        SharedPreferences sp = PreferenceManager
                .getDefaultSharedPreferences(ctx);
        String adm = sp.getString(COMPANY_JSON, null);
        CompanyDTO co = null;
        if (adm != null) {
            co = gson.fromJson(adm, CompanyDTO.class);

        }
        return co;
    }

    public static void saveCreditCard( Context ctx,  CreditCardDTO dto) {

        SharedPreferences sp = PreferenceManager
                .getDefaultSharedPreferences(ctx);
        String x = gson.toJson(dto);
        SharedPreferences.Editor ed = sp.edit();
        ed.putString(CREDIT_CARD, x);
        ed.commit();
        Log.e("SharedUtil", "%%%%% CreditCard saved in SharedPreferences");
    }


    public static CreditCardDTO getCreditCard( Context ctx) {

        SharedPreferences sp = PreferenceManager
                .getDefaultSharedPreferences(ctx);
        String adm = sp.getString(CREDIT_CARD, null);
        CreditCardDTO co = null;
        if (adm != null) {
            co = gson.fromJson(adm, CreditCardDTO.class);

        }
        return co;
    }

    public static void saveGCMDevice( Context ctx,  GcmDeviceDTO dto) {


        SharedPreferences sp = PreferenceManager
                .getDefaultSharedPreferences(ctx);
        String x = gson.toJson(dto);
        SharedPreferences.Editor ed = sp.edit();
        ed.putString(GCMDEVICE, x);
        ed.commit();
        System.out.println("%%%%% Device saved in SharedPreferences");
    }


    public static GcmDeviceDTO getGCMDevice( Context ctx) {

        SharedPreferences sp = PreferenceManager
                .getDefaultSharedPreferences(ctx);
        String adm = sp.getString(GCMDEVICE, null);
        GcmDeviceDTO co = null;
        if (adm != null) {
            co = gson.fromJson(adm, GcmDeviceDTO.class);

        }
        Log.e("SharedUtil", "%%%%% Device found in SharedPreferences: " + co.getModel());
        return co;
    }

    public static void savePhoto( Context ctx,  PhotoUploadDTO dto) {


        SharedPreferences sp = PreferenceManager
                .getDefaultSharedPreferences(ctx);
        String x = gson.toJson(dto);
        SharedPreferences.Editor ed = sp.edit();
        ed.putString(PHOTO, x);
        ed.commit();
        Log.i("SharedUtil", "%%%%% Photo saved in SharedPreferences");
    }


    public static PhotoUploadDTO getPhoto( Context ctx) {

        SharedPreferences sp = PreferenceManager
                .getDefaultSharedPreferences(ctx);
        String adm = sp.getString(PHOTO, null);
        PhotoUploadDTO co = null;
        if (adm != null) {
            co = gson.fromJson(adm, PhotoUploadDTO.class);

        }
        return co;
    }


    /**
     * @return Application's version code from the {@code PackageManager}.
     */
    public static int getAppVersion( Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }


    public static void saveLastProjectID( Context ctx, Integer projectID) {

        SharedPreferences sp = PreferenceManager
                .getDefaultSharedPreferences(ctx);
        SharedPreferences.Editor ed = sp.edit();
        ed.putInt(PROJECT_ID, projectID);
        ed.commit();
        Log.e("SharedUtil", "%%%%% projectID: " + projectID + " saved in SharedPreferences");
    }

    public static Integer getLastProjectID( Context ctx) {

        SharedPreferences sp = PreferenceManager
                .getDefaultSharedPreferences(ctx);
        int id = sp.getInt(PROJECT_ID, 0);
        return id;
    }
}

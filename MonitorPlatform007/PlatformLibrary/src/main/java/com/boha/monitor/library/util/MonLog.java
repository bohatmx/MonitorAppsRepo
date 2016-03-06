package com.boha.monitor.library.util;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.util.Log;

/**
 * Created by aubreymalabie on 3/6/16.
 */
public class MonLog {


    public static void e(Context ctx, String LOG, String message) {
        if (isDebuggable(ctx)) {
            Log.e(LOG,message);
        }

    }
    public static void d(Context ctx, String LOG, String message) {
        if (isDebuggable(ctx)) {
            Log.d(LOG,message);
        }

    }
    public static void w(Context ctx, String LOG, String message) {
        if (isDebuggable(ctx)) {
            Log.w(LOG,message);
        }

    }
    public static void v(Context ctx, String LOG, String message) {
        if (isDebuggable(ctx)) {
            Log.v(LOG,message);
        }

    }
    public static void i(Context ctx, String LOG, String message) {
        if (isDebuggable(ctx)) {
            Log.i(LOG,message);
        }

    }
    private static boolean isDebuggable(Context ctx) {
        boolean isDebuggable = 0 != (ctx.getApplicationInfo().flags
                &= ApplicationInfo.FLAG_DEBUGGABLE);
        return isDebuggable;
    }
}

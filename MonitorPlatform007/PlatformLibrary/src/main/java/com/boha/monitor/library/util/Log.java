package com.boha.monitor.library.util;

import android.content.Context;
import android.content.pm.ApplicationInfo;

/**
 * Created by aubreyM on 15/12/04.
 */
public class Log {

    public static void e(Context ctx,String logName, String message) {
        boolean isDebuggable = 0 != (ctx.getApplicationInfo().flags
                &= ApplicationInfo.FLAG_DEBUGGABLE);


    }
}

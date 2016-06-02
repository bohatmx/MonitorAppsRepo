package com.boha.monitor.library.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.File;
import java.util.Random;

/**
 * Created by aubreymalabie on 5/22/16.
 */

public class Util {

    public static final String COMPANIES_KEY = "companies";

    static final String TAG = Util.class.getSimpleName();

    static Random random = new Random(System.currentTimeMillis());
    public static String getOneTimePassword() {

        int n1 = random.nextInt(9);
        int n2 = random.nextInt(9);
        int n3 = random.nextInt(9);
        int n4 = random.nextInt(9);
        int n5 = random.nextInt(9);
        int n6 = random.nextInt(9);
        StringBuilder sb = new StringBuilder();
        sb.append(n1).append(n2).append(n3).append(n4);
        sb.append(n5).append(n6);
        return sb.toString();
    }

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
    static public boolean hasStorage(boolean requireWriteAccess) {
        String state = Environment.getExternalStorageState();
        Log.w("Util", "--------- disk storage state is: " + state);

        if (Environment.MEDIA_MOUNTED.equals(state)) {
            if (requireWriteAccess) {
                boolean writable = checkFsWritable();
                Log.i("Util", "************ storage is writable: " + writable);
                return writable;
            } else {
                return true;
            }
        } else if (!requireWriteAccess && Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }

    private static boolean checkFsWritable() {
        // Create a temporary file to see whether a volume is really writeable.
        // It's important not to put it in the root directory which may have a
        // limit on the number of files.
        String directoryName = Environment.getExternalStorageDirectory().toString() + "/DCIM";
        File directory = new File(directoryName);
        if (!directory.isDirectory()) {
            if (!directory.mkdirs()) {
                return false;
            }
        }
        return directory.canWrite();
    }
}

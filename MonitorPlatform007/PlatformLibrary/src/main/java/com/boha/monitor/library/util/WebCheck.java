package com.boha.monitor.library.util;


import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;


/**
 *  Check status and type of active network on the device
 */
public class WebCheck {
    static ConnectivityManager connectivity;

    private static void logInfo(NetworkInfo networkInfo) {
        StringBuilder sb = new StringBuilder();
        sb.append("\n### Network Info ").append(" - ");
        sb.append("Network Type: ").append(networkInfo.getTypeName()).append(", ");
        sb.append("State: ").append(networkInfo.getState().name()).append(", ");
        sb.append("Network Name: ").append(networkInfo.getExtraInfo()).append("\n");
        String res = sb.toString();
        System.out.println(res);
    }

    /**
     * Check availability and connectedness of the network
     * @param ctx
     * @return
     */
    public static WebCheckResult checkNetworkAvailability(Context ctx) {
        if (connectivity == null) {
            connectivity = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
        }

        NetworkInfo activeNetworkInfo = connectivity.getActiveNetworkInfo();
        WebCheckResult result = new WebCheckResult();
        if (activeNetworkInfo ==  null) {
            result.setNetworkUnavailable(true);
            Log.e(TAG, "Network unavailable, could be in flight mode");
            return result;
        }
        if (activeNetworkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
            if (activeNetworkInfo.isAvailable()) {
                result.setWifiAvailable(true);
            }
            if (activeNetworkInfo.isConnected()) {
                result.setWifiConnected(true);
            }
            if (result.isWifiConnected()) {
                result.setNetworkUnavailable(false);
            } else {
                result.setNetworkUnavailable(true);
            }
        }
        if (activeNetworkInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
            if (activeNetworkInfo.isAvailable()) {
                result.setMobileAvailable(true);
            }
            if (activeNetworkInfo.isConnected()) {
                result.setMobileConnected(true);
            }
            if (result.isMobileConnected()) {
                result.setNetworkUnavailable(false);
            } else {
                result.setNetworkUnavailable(true);
            }
        }

        logInfo(activeNetworkInfo);
        return result;

    }

    public static final String TAG = "WebCheck";
}

package com.boha.monitor.library.util;

import android.content.Context;
import android.support.annotation.RequiresPermission;

import com.boha.monitor.library.dto.RequestDTO;
import com.boha.monitor.library.dto.RequestList;
import com.boha.monitor.library.dto.ResponseDTO;
import com.boha.platform.library.R;
import com.google.gson.Gson;

import java.math.BigDecimal;


/**
 * Created by aubreyM on 15/01/31.
 */
public class NetUtil {
    public interface NetUtilListener {
        public void onResponse(ResponseDTO response);

        public void onError(String message);

    }

    static final String LOG = NetUtil.class.getSimpleName();
    static NetUtilListener listener;

    @RequiresPermission
    public static void sendRequest(Context ctx, RequestDTO request, NetUtilListener utilListener) {
        listener = utilListener;
        WebCheckResult wcr = WebCheck.checkNetworkAvailability(ctx);
        if (wcr.isNetworkUnavailable()) {
            utilListener.onError(ctx.getString(R.string.net_not_avail));
            return;
        }
        sendViaHttp(ctx, request);

    }

    @RequiresPermission
    public static void sendRequest(Context ctx, RequestList requestList, NetUtilListener utilListener) {
        listener = utilListener;
        WebCheckResult wcr = WebCheck.checkNetworkAvailability(ctx);
        if (!wcr.isNetworkUnavailable()) {
            utilListener.onError(ctx.getString(R.string.net_not_avail));
            return;
        }
        sendListViaHttp(ctx, requestList);

    }

    private static void sendViaHttp(Context ctx, RequestDTO request) {
        OKUtil util = new OKUtil();
        try {
            util.sendGETRequest(ctx,request, new OKUtil.OKListener() {
                @Override
                public void onResponse(ResponseDTO response) {
                    if (response.getStatusCode() > 0) {
                        listener.onError(response.getMessage());
                        return;
                    }
                    listener.onResponse(response);
                }

                @Override
                public void onError(String message) {
                    listener.onError(message);
                }
            });
        } catch (OKHttpException e) {
            listener.onError("Communications Error: " + e.msg);
        }
    }

    private static void sendListViaHttp(Context ctx, RequestList requestList) {
        OKUtil util = new OKUtil();
        try {
            util.sendPOSTRequest(ctx,requestList, new OKUtil.OKListener() {
                @Override
                public void onResponse(ResponseDTO response) {
                    listener.onResponse(response);
                }

                @Override
                public void onError(String message) {
                    listener.onError(message);
                }
            });
        } catch (OKHttpException e) {
            listener.onError("Communications Error: " + e.msg);
        }
    }

    public static double getElapsed(long start, long end) {
        BigDecimal m = new BigDecimal(end - start).divide(new BigDecimal(1000));
        return m.doubleValue();
    }

    static Gson gson = new Gson();
}

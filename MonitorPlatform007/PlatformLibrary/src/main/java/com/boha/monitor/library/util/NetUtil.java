package com.boha.monitor.library.util;

import android.content.Context;
import android.support.annotation.RequiresPermission;
import android.util.Log;

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
        public void onWebSocketClose();
    }

    static final String LOG = NetUtil.class.getSimpleName();
    static NetUtilListener listener;
    @RequiresPermission
    public static void sendRequest( Context ctx,  RequestDTO request,  NetUtilListener utilListener) {
        listener = utilListener;
        WebCheckResult wcr = WebCheck.checkNetworkAvailability(ctx);
        if (wcr.isNetworkUnavailable()) {
            utilListener.onError(ctx.getString(R.string.net_not_avail));
            return;
        }

        if (request.isRideWebSocket()) {
            sendViaWebSocket(ctx, request);
        } else {
            sendViaHttp(ctx, request);
        }
    }
    @RequiresPermission
    public static void sendRequest( Context ctx,  RequestList requestList,  NetUtilListener utilListener) {
        listener = utilListener;
        Log.e(LOG, "########### sendRequestList ... isRideWebSocket: " + requestList.isRideWebSocket());
        WebCheckResult wcr = WebCheck.checkNetworkAvailability(ctx);
        if (!wcr.isNetworkUnavailable()) {
            utilListener.onError(ctx.getString(R.string.net_not_avail));
            return;
        }

        if (requestList.isRideWebSocket()) {
            sendListViaWebSocket(ctx, requestList);
        } else {
            sendListViaHttp(ctx, requestList);
        }
    }
    private static void sendViaWebSocket(Context ctx, RequestDTO request) {
        final long start = System.currentTimeMillis();
        WebSocketUtil.sendRequest(ctx, Statics.COMPANY_ENDPOINT, request, new WebSocketUtil.WebSocketListener() {
            @Override
            public void onMessage(ResponseDTO response) {
                final long end = System.currentTimeMillis();
                Log.e(LOG, "HTTP call completed. elapsed server time: " + response.getElapsedRequestTimeInSeconds()
                        + "\nroundTrip elapsed: " + getElapsed(start,end) + " size: " + getLength(gson.toJson(response).length()));
                if (response.getStatusCode() == 0) {
                    listener.onResponse(response);
                } else {
                    listener.onError(response.getMessage());
                }
            }

            @Override
            public void onError(String message) {
                final long end = System.currentTimeMillis();
                Log.e(LOG, "HTTP call completed. " + message + "\n"
                        + " roundTrip elapsed: " + getElapsed(start,end));
                listener.onError(message);
            }
        });

    }
    private static void sendViaHttp(Context ctx, RequestDTO request)  {
        try {
            OKUtil.doGet(request, new OKUtil.OKListener() {
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
//        final long start = System.currentTimeMillis();
//        BaseVolley.sendRequest(Statics.GATEWAY_SERVLET, request, ctx, new BaseVolley.BohaVolleyListener() {
//
//
//            @Override
//            public void onResponseReceived(ResponseDTO response) {
//                final long end = System.currentTimeMillis();
//                Log.e(LOG, "HTTP call completed. elapsed server time: " + response.getElapsedRequestTimeInSeconds()
//                        + "\nroundTrip elapsed: " + getElapsed(start, end) + " size: " + getLength(gson.toJson(response).length()));
//                if (response.getStatusCode() == 0) {
//                    listener.onResponse(response);
//                } else {
//                    listener.onError(response.getMessage());
//                }
//            }
//
//            @Override
//            public void onVolleyError(VolleyError error) {
//                Log.e(LOG, "-- Volley Error: " + error.getMessage());
//                final long end = System.currentTimeMillis();
//                Log.e(LOG, "HTTP networking. ERROR occured - "
//                        + " roundTrip elapsed: " + getElapsed(start, end));
//                listener.onError("Error communicating with server");
//            }
//        });
    }
    private static String getLength(int length) {
        Double d = Double.parseDouble("" + length)/Double.parseDouble("1024");

        BigDecimal bd = new BigDecimal(d).setScale(2,BigDecimal.ROUND_UP);
        return bd.toString() + "K";
    }
    private static void sendListViaWebSocket(Context ctx, RequestList requestList) {
        final long start = System.currentTimeMillis();
        WebSocketUtil.sendRequest(ctx, Statics.CACHED_REQUEST_ENDPOINT, requestList, new WebSocketUtil.WebSocketListener() {
            @Override
            public void onMessage(ResponseDTO response) {
                final long end = System.currentTimeMillis();
                Log.e(LOG, "sendListViaWebSocket  completed. elapsed server time: " + response.getElapsedRequestTimeInSeconds()
                        + " roundTrip elapsed: " + getElapsed(start,end));
                if (response.getStatusCode() == 0) {
                    listener.onResponse(response);
                } else {
                    listener.onError(response.getMessage());
                }
            }

            @Override
            public void onError(String message) {
                final long end = System.currentTimeMillis();
                Log.e(LOG, "sendListViaWebSocket call completed. " + message + "\n"
                        + " roundTrip elapsed: " + getElapsed(start,end));
                listener.onError(message);
            }
        });

    }
    private static void sendListViaHttp(Context ctx, RequestList requestList) {
        final long start = System.currentTimeMillis();
        try {
            OKUtil.doPost(requestList, new OKUtil.OKListener() {
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

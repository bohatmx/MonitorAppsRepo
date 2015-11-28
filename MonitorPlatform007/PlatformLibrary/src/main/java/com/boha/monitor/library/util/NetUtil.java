package com.boha.monitor.library.util;

import android.content.Context;
import android.util.Log;

import com.android.volley.VolleyError;
import com.boha.monitor.library.dto.RequestDTO;
import com.boha.monitor.library.dto.RequestList;
import com.boha.monitor.library.dto.ResponseDTO;
import com.boha.monitor.library.toolbox.BaseVolley;
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
    public static void sendRequest( Context ctx,  RequestDTO request,  NetUtilListener utilListener) {
        Log.d(LOG, "sendRequest() called with: " + "ctx = [" + ctx + "], request = [" + request + "], utilListener = [" + utilListener + "]");
        listener = utilListener;
        Log.d(LOG,"########### sendRequest ... isRideWebSocket: " + request.isRideWebSocket());
        WebCheckResult wcr = WebCheck.checkNetworkAvailability(ctx, true);
        if (!wcr.isMobileConnected() && !wcr.isWifiConnected()) {
            utilListener.onError(ctx.getString(R.string.net_not_avail));
            return;
        }

        request.setRequestCacheID(null);
        if (request.isRideWebSocket()) {
            sendViaWebSocket(ctx, request);
        } else {
            sendViaHttp(ctx, request);
        }
    }
    public static void sendRequest( Context ctx,  RequestList requestList,  NetUtilListener utilListener) {
        listener = utilListener;
        Log.e(LOG, "########### sendRequestList ... isRideWebSocket: " + requestList.isRideWebSocket());
        WebCheckResult wcr = WebCheck.checkNetworkAvailability(ctx, true);
        if (!wcr.isMobileConnected() && !wcr.isWifiConnected()) {
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
                Log.e(LOG, "HTTP call completed. " + message + "\n"
                        + " roundTrip elapsed: " + getElapsed(start,end));
                listener.onError(message);
            }
        });

    }
    private static void sendViaHttp(Context ctx, RequestDTO request) {
        final long start = System.currentTimeMillis();
        BaseVolley.getRemoteData(Statics.GATEWAY_SERVLET, request, ctx, new BaseVolley.BohaVolleyListener() {


            @Override
            public void onResponseReceived(ResponseDTO response) {
                final long end = System.currentTimeMillis();
                Log.e(LOG, "HTTP call completed. elapsed server time: " + response.getElapsedRequestTimeInSeconds()
                        + " roundTrip elapsed: " + getElapsed(start,end));
                if (response.getStatusCode() == 0) {
                    listener.onResponse(response);
                } else {
                    listener.onError(response.getMessage());
                }
            }

            @Override
            public void onVolleyError(VolleyError error) {
                Log.e(LOG, "-- Volley Error: " + error.getMessage());
                final long end = System.currentTimeMillis();
                Log.e(LOG, "HTTP networking. ERROR occured - "
                        + " roundTrip elapsed: " + getElapsed(start,end));
                listener.onError("Error communicating with server");
            }
        });
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
        BaseVolley.getRemoteData(Statics.CACHED_REQUEST_SERVLET, requestList, ctx, new BaseVolley.BohaVolleyListener() {


            @Override
            public void onResponseReceived(ResponseDTO response) {
                final long end = System.currentTimeMillis();
                Log.e(LOG, "sendListViaHttp completed. elapsed server time: " + response.getElapsedRequestTimeInSeconds()
                        + " roundTrip elapsed: " + getElapsed(start,end));
                listener.onResponse(response);
            }

            @Override
            public void onVolleyError(VolleyError error) {
                final long end = System.currentTimeMillis();
                Log.e(LOG, "sendListViaWebSocket ERROR encountered. "
                        + " roundTrip elapsed: " + getElapsed(start,end));
                Log.e(LOG, "-- Volley Error: " + error.getMessage());
                listener.onError("Error communicating with server");
            }
        });
    }

    public static double getElapsed(long start, long end) {
        BigDecimal m = new BigDecimal(end - start).divide(new BigDecimal(1000));
        return m.doubleValue();
    }
    static Gson gson = new Gson();
}

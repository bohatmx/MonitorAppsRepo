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
        listener = utilListener;
        Log.e(LOG,"########### sendRequest ... isRideWebSocket: " + request.isRideWebSocket());
        WebCheckResult wcr = WebCheck.checkNetworkAvailability(ctx, true);
        if (!wcr.isMobileConnected() && !wcr.isWifiConnected()) {
            utilListener.onError(ctx.getString(R.string.net_not_avail));
            return;
        }

        if (request.isRideWebSocket()) {
            sendViaWebSocket(ctx,request);
        } else {
            sendViaHttp(ctx,request);
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
            sendListViaWebSocket(ctx,requestList);
        } else {
            sendListViaHttp(ctx,requestList);
        }
    }
    private static void sendViaWebSocket(Context ctx, RequestDTO request) {

        WebSocketUtil.sendRequest(ctx, Statics.COMPANY_ENDPOINT, request, new WebSocketUtil.WebSocketListener() {
            @Override
            public void onMessage(ResponseDTO response) {
                listener.onResponse(response);
            }

            @Override
            public void onError(String message) {
                listener.onError(message);
            }
        });

    }
    private static void sendViaHttp(Context ctx, RequestDTO request) {
        BaseVolley.getRemoteData(Statics.GATEWAY_SERVLET, request, ctx, new BaseVolley.BohaVolleyListener() {


            @Override
            public void onResponseReceived(ResponseDTO response) {
                listener.onResponse(response);
            }

            @Override
            public void onVolleyError(VolleyError error) {
                Log.e(LOG, "-- Volley Error: " + error.getMessage());
                listener.onError("Error communicating with server");
            }
        });
    }
    private static void sendListViaWebSocket(Context ctx, RequestList requestList) {

        WebSocketUtil.sendRequest(ctx, Statics.CACHED_REQUEST_ENDPOINT, requestList, new WebSocketUtil.WebSocketListener() {
            @Override
            public void onMessage(ResponseDTO response) {
                listener.onResponse(response);
            }

            @Override
            public void onError(String message) {
                listener.onError(message);
            }
        });

    }
    private static void sendListViaHttp(Context ctx, RequestList requestList) {
        BaseVolley.getRemoteData(Statics.CACHED_REQUEST_SERVLET, requestList, ctx, new BaseVolley.BohaVolleyListener() {


            @Override
            public void onResponseReceived(ResponseDTO response) {
                listener.onResponse(response);
            }

            @Override
            public void onVolleyError(VolleyError error) {
                Log.e(LOG, "-- Volley Error: " + error.getMessage());
                listener.onError("Error communicating with server");
            }
        });
    }


    static Gson gson = new Gson();
}

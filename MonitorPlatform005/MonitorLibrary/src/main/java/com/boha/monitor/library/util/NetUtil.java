package com.boha.monitor.library.util;

import android.content.Context;

import com.android.volley.VolleyError;
import com.boha.monitor.library.R;
import com.boha.monitor.library.dto.transfer.RequestDTO;
import com.boha.monitor.library.dto.transfer.ResponseDTO;
import com.boha.monitor.library.toolbox.BaseVolley;
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

    static NetUtilListener listener;

    public static void sendRequest( Context ctx,  RequestDTO request,  NetUtilListener utilListener) {
        listener = utilListener;

        WebCheckResult wcr = WebCheck.checkNetworkAvailability(ctx,true);
        if (!wcr.isMobileConnected() && !wcr.isWifiConnected()) {
            utilListener.onError(ctx.getString(R.string.net_unavailable));
            return;
        }

        if (request.getRideWebSocket()) {
            sendViaWebSocket(ctx,request);
        } else {
            sendViaHttp(ctx,request);
        }
    }
    private static void sendViaWebSocket(Context ctx, RequestDTO request) {

        WebSocketUtil.sendRequest(ctx,Statics.COMPANY_ENDPOINT,request, new WebSocketUtil.WebSocketListener() {
            @Override
            public void onMessage(ResponseDTO response) {
                listener.onResponse(response);
            }

            @Override
            public void onClose() {
                listener.onWebSocketClose();
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
                listener.onError("Error communicating with server");
            }
        });
    }


    static Gson gson = new Gson();
}

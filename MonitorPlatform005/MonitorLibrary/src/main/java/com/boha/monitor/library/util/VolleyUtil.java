package com.boha.monitor.library.util;

import android.content.Context;
import android.util.Log;

import com.android.volley.VolleyError;
import com.boha.monitor.library.R;
import com.boha.monitor.library.dto.transfer.RequestDTO;
import com.boha.monitor.library.dto.transfer.ResponseDTO;
import com.boha.monitor.library.toolbox.BaseVolley;



/**
 * Created by aubreyM on 15/01/18.
 */
public class VolleyUtil {

    static final String LOG = VolleyUtil.class.getSimpleName();

    public static void sendRequest( final Context ctx, RequestDTO request,
                                    final WebSocketUtil.WebSocketListener webSocketListener) {
        Log.e(LOG, "### sending http request .....");
        String suffix = Statics.GATEWAY_SERVLET + "?JSON=";
        BaseVolley.getRemoteData(suffix,request, ctx, new BaseVolley.BohaVolleyListener() {
            @Override
            public void onResponseReceived( ResponseDTO response) {
                if (response.getStatusCode() > 0) {
                    webSocketListener.onError(response.getMessage());
                } else {
                    webSocketListener.onMessage(response);
                }
            }

            @Override
            public void onVolleyError(VolleyError error) {
                webSocketListener.onError(ctx.getString(R.string.network_error));
            }
        });
    }
}

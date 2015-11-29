package com.boha.monitor.library.toolbox;

import android.content.Context;
import android.util.Log;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkError;
import com.android.volley.Request.Method;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.boha.monitor.library.dto.RequestDTO;
import com.boha.monitor.library.dto.RequestList;
import com.boha.monitor.library.dto.ResponseDTO;
import com.boha.monitor.library.util.Statics;
import com.google.gson.Gson;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.concurrent.TimeUnit;

/**
 * Utility class to encapsulate calls to the remote server via the Volley Networking library.
 * Uses BohaVolleyListener to inform caller on status of the communications request
 *
 * @author Aubrey Malabie
 */
public class BaseVolley {

    /**
     * Informs whoever implements this interface when a communications request is concluded
     */
    public interface BohaVolleyListener {
        public void onResponseReceived(ResponseDTO response);
        public void onVolleyError(VolleyError error);
    }

    static BohaVolleyListener bohaVolleyListener;

    /**
     * This method gets a Volley based communications request started
     *
     * @param suffix   the suffix pointing to the destination servlet
     * @param request  the request object in JSON format
     * @param context  the Activity context
     * @param listener the listener implementor who wants to know abdout call status
     */
    public static void sendRequest(String suffix, RequestDTO request,
                                   Context context, BohaVolleyListener listener) {

        ctx = context;
        bohaVolleyListener = listener;
        if (requestQueue == null) {
            requestQueue = BohaVolley.getRequestQueue(ctx);
        }
        String json = null, jj = null;

        Gson gson = new Gson();
        try {
            jj = gson.toJson(request);
            json = URLEncoder.encode(jj, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        retries = 0;
        StringBuilder sb = new StringBuilder();
        sb.append(Statics.URL).append(suffix);
        sb.append(json);
        String url = sb.toString();
        Log.d(TAG, "...sending remote request: ....size: "+ url.length() +"...>\n"  + Statics.URL + suffix  + jj);
        bohaRequest = new BohaRequest(Method.POST, url,
                onSuccessListener(), onErrorListener());
        bohaRequest.setRetryPolicy(new DefaultRetryPolicy((int) TimeUnit.SECONDS.toMillis(120),
                MAX_RETRIES, BACK_OFF_MULTIPLIER));
        requestQueue.add(bohaRequest);
    }

    public static void sendRequest(String suffix, RequestList requestList,
                                   Context context, BohaVolleyListener listener) {

        ctx = context;
        bohaVolleyListener = listener;
        if (requestQueue == null) {
            requestQueue = BohaVolley.getRequestQueue(ctx);
        }
        String json = null, jj = null;

        Gson gson = new Gson();
        try {
            jj = gson.toJson(requestList);
            json = URLEncoder.encode(jj, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        retries = 0;
        StringBuilder sb = new StringBuilder();
        sb.append(Statics.URL).append(suffix);
        sb.append(json);
        String url = sb.toString();
        Log.d(TAG, "...sending remote requestList: ....size: " + url.length() + "...>\n" + Statics.URL + suffix + jj);
        bohaRequest = new BohaRequest(Method.POST, url,
                onSuccessListener(), onErrorListener());
        bohaRequest.setRetryPolicy(new DefaultRetryPolicy((int) TimeUnit.SECONDS.toMillis(120),
                MAX_RETRIES, BACK_OFF_MULTIPLIER));
        requestQueue.add(bohaRequest);
    }





    public static void sendRequest(String suffix, RequestDTO request,
                                   Context context, int timeOutSeconds, BohaVolleyListener listener) {

        ctx = context;
        bohaVolleyListener = listener;
        if (requestQueue == null) {
            Log.w(TAG, "sendRequest requestQueue is null, getting it ...: ");
            requestQueue = BohaVolley.getRequestQueue(ctx);
        } else {
            Log.e(TAG, "********** sendRequest requestQueue is NOT NULL - Kool");
        }
        String json = null, jj = null;

        Gson gson = new Gson();
        try {
            jj = gson.toJson(request);
            json = URLEncoder.encode(jj, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        retries = 0;
        String x = Statics.URL + suffix + json;
        Log.i(TAG, "...sending remote request: ...size: "+ x.length() +"...>\n"  + Statics.URL + suffix + jj);
        bohaRequest = new BohaRequest(Method.POST, x,
                onSuccessListener(), onErrorListener());
        bohaRequest.setRetryPolicy(new DefaultRetryPolicy((int) TimeUnit.SECONDS.toMillis(timeOutSeconds),
                MAX_RETRIES, BACK_OFF_MULTIPLIER));
        requestQueue.add(bohaRequest);
    }


    private static Response.Listener<ResponseDTO> onSuccessListener() {
        return new Response.Listener<ResponseDTO>() {
            @Override
            public void onResponse( ResponseDTO r) {
                response = r;
                Log.d(TAG, "Yup! ...response object received, status code: " + r.getStatusCode());
                if (r.getStatusCode() > 0) {
                    try {
                        Log.w(TAG, response.getMessage());
                    } catch (Exception e) {}
                }
                bohaVolleyListener.onResponseReceived(response);

            }
        };
    }


    private static Response.ErrorListener onErrorListener() {
        return new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                if (error instanceof TimeoutError) {
                    retries++;
                    if (retries < MAX_RETRIES) {
                        waitABit();
                        Log.e(TAG, "onErrorResponse: Retrying after timeout error ...retries = " + retries);
                        requestQueue.add(bohaRequest);
                        return;
                    } else {
                        bohaVolleyListener.onVolleyError(error);
                    }
                }
                if (error instanceof NetworkError) {
                    Log.w(TAG, "onErrorResponse Network Error: ");
                    NetworkError ne = (NetworkError) error;
                    if (ne.networkResponse != null) {
                        Log.e(TAG, "volley networkResponse status code: "
                                + ne.networkResponse.statusCode);
                    }
                    retries++;
                    if (retries < MAX_RETRIES) {
                        waitABit();
                        Log.e(TAG, "onErrorResponse: Retrying after NetworkError ...retries = " + retries);
                        requestQueue.add(bohaRequest);
                        return;
                    } else {
                        bohaVolleyListener.onVolleyError(error);
                    }

                }

                bohaVolleyListener.onVolleyError(error);
            }
        };
    }

    private static void waitABit() {
        Log.d(TAG, "...going to sleep for: " + (SLEEP_TIME/1000) + " seconds before retrying.....");
        try {
            Thread.sleep(SLEEP_TIME);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    private static ResponseDTO response;
    private static Context ctx;

    protected static BohaRequest bohaRequest;
    protected static RequestQueue requestQueue;
    protected ImageLoader imageLoader;
    protected static String suff;
    static final String TAG = "BaseVolley";
    static final int MAX_RETRIES = 2, BACK_OFF_MULTIPLIER = 2;
    static final long SLEEP_TIME = 3000;


    static int retries;


    public static RequestQueue getRequestQueue() {
        return requestQueue;
    }
}
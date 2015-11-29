package com.boha.monitor.library.util;

import android.content.Context;
import android.util.Log;

import com.boha.monitor.library.dto.RequestDTO;
import com.boha.monitor.library.dto.RequestList;
import com.boha.monitor.library.dto.ResponseDTO;
import com.google.gson.Gson;

import org.acra.ACRA;

import java.nio.ByteBuffer;
import java.text.DecimalFormat;

import de.tavendo.autobahn.WebSocketConnection;
import de.tavendo.autobahn.WebSocketException;
import de.tavendo.autobahn.WebSocketHandler;
import de.tavendo.autobahn.WebSocketOptions;

/**
 * Convenience class to wrap Autobahn WebSocket library. Sends RequestDTO as JSON
 * string to web application.
 * <p>
 * Created by aubreyM on 15/04/19.
 */
public class WebSocketUtil {

    static final String LOG = WebSocketUtil.class.getSimpleName();
    static WebSocketListener webSocketListener;
    static WebSocketConnection mConnection = new WebSocketConnection();
    static final Gson GSON = new Gson();
    static Context context;
    static final int CONNECT_RETRIES = 100, WAIT_INTERVAL = 2000;
    static int retryCount;

    public interface WebSocketListener {
        void onMessage(ResponseDTO response);

        void onError(String message);
    }

    public static void sendRequest(Context ctx, final String suffix,
                                   RequestDTO w, WebSocketListener listener) {
        webSocketListener = listener;
        context = ctx;
        retryCount = 0;
        final String url = Statics.WEBSOCKET_URL + suffix;
        final String json = GSON.toJson(w);
        try {

            if (mConnection.isConnected()) {
                Log.i(LOG, "### WebSocket Status: Connected. using: " + url + " sending: \n" + json);
                mConnection.sendTextMessage(json);
            } else {
                connect(url, json);
            }
        } catch (Exception e) {
            Log.e(LOG, "WebSocketUtil sendRequest: ", e);
        }
    }

    public static void sendRequest(Context ctx, final String suffix,
                                   RequestList w, WebSocketListener listener) {
        webSocketListener = listener;
        context = ctx;
        retryCount = 0;
        final String url = Statics.WEBSOCKET_URL + suffix;
        final String json = GSON.toJson(w);
        try {

            if (mConnection.isConnected()) {
                Log.i(LOG, "### WebSocket Status: Connected. using: " + url + " sending: \n" + json);
                mConnection.sendTextMessage(json);
            } else {
                connect(url, json);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void connect(final String url, final String json) {
        Log.d(LOG, "&&&&&&& CONNECT TO WEBSOCKET, retryCount: " + retryCount);
        WebSocketOptions options = new WebSocketOptions();
        options.setSocketConnectTimeout(5000);
        options.setSocketReceiveTimeout(5000);
        try {
            mConnection.connect(url, new WebSocketHandler() {
                @Override
                public void onOpen() {
                    Log.w(LOG, "OnOpen: Connected to " + url + " sending...: \n" + json);
                    if (mConnection.isConnected()) {
                        mConnection.sendTextMessage(json);
                    } else {
                        Log.e(LOG,"-- mConnection is not connected. wtf?");
                    }
                }

                @Override
                public void onTextMessage(String payload) {
                    Log.d(LOG, "+++ onTextMessage payload size: " + getSize(payload.length()));
                    retryCount = 0;
                    try {
                        ResponseDTO r = GSON.fromJson(payload, ResponseDTO.class);
                        if (r.getSessionID() != null) {
                            Log.i(LOG, "Response with sessionID: " + r.getSessionID());
                        } else {
                            if (r.getStatusCode() == 0) {
                                webSocketListener.onMessage(r);
                            } else {
                                webSocketListener.onError(r.getMessage());
                            }
                        }
                    } catch (Exception e) {
                        webSocketListener.onError("Communications with server failed");
                    }
                }

                @Override
                public void onBinaryMessage(byte[] payload) {
                    retryCount = 0;
                    ByteBuffer byteBuffer = ByteBuffer.wrap(payload);
                    parseData(byteBuffer);
                }

                @Override
                public void onClose(int code, String reason) {
                    Log.e(LOG, "Connection lost. " + reason + ". will issue disconnect");
//                    connect(url,json);
//                    mConnection.disconnect();
//                    mConnection = new WebSocketConnection();
//                    try {
//                        if (retryCount < CONNECT_RETRIES) {
//                            retryCount++;
//                            final Timer timer = new Timer();
//                            timer.schedule(new TimerTask() {
//                                @Override
//                                public void run() {
//                                    timer.cancel();
//                                    connect(url, json);
//                                }
//                            }, retryCount * WAIT_INTERVAL);
//
//                        } else {
//                            webSocketListener.onError(context.getString(R.string.comms_interrupt));
//                        }
//                    } catch (Exception e) {
//
//                    }


                }
            }, options);

        } catch (IllegalStateException e) {
            Log.e(LOG, "IllegalStateException .", e);
        } catch (WebSocketException e) {
            Log.e(LOG, "WebSocketException failed.", e);
            webSocketListener.onError(e.getMessage());
        } catch (Exception e) {
            Log.e(LOG, "Exception failed.", e);
            webSocketListener.onError(e.getMessage());
        }
    }

    private static void parseData(ByteBuffer bb) {
        Log.i(LOG, "### parseData ByteBuffer capacity: " + ZipUtil.getKilobytes(bb.capacity()));
        String content = null;
        try {
            try {
                content = new String(bb.array());
                ResponseDTO response = GSON.fromJson(content, ResponseDTO.class);
                if (response.getStatusCode() == 0) {
                    webSocketListener.onMessage(response);
                } else {
                    webSocketListener.onError(response.getMessage());
                }
                return;

            } catch (Exception e) {
                content = ZipUtil.uncompressGZip(bb);
            }

            if (content != null) {
                ResponseDTO response = GSON.fromJson(content, ResponseDTO.class);
                if (response.getStatusCode() == 0) {
                    Log.w(LOG, "### response status code is 0 - OK");
                    webSocketListener.onMessage(response);
                } else {
                    Log.e(LOG, "--- response status code is " + response.getStatusCode() + " - server found ERROR");
                    webSocketListener.onError(response.getMessage());
                }
            } else {
                Log.e(LOG, "-- Content from server failed. Response content is null");
                try {
                    ACRA.getErrorReporter().handleException(new UnsupportedOperationException("Response content is NULL"), false);
                } catch (Exception ex) {
                }
                webSocketListener.onError("Content from server failed. Response is null");
            }

        } catch (Exception e) {
            Log.e(LOG, "parseData Failed", e);
            try {
                ACRA.getErrorReporter().handleException(e, false);
            } catch (Exception ex) {
            }
            webSocketListener.onError("Failed to unpack server response. Please try again.");
        }
    }

    private static String getSize(int size) {
        Double x = Double.parseDouble("" + size);
        Double y = x / Double.parseDouble("1024");
        return df.format(y) + "KB";
    }

    static final DecimalFormat df = new DecimalFormat("###,###,###,###,###,##0.00");
}


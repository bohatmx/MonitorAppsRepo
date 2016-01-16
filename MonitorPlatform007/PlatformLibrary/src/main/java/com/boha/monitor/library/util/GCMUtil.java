package com.boha.monitor.library.util;


/**
 * Created by aubreyM on 2014/05/11.
 */

import android.content.Context;
import android.os.Build;
import android.util.Log;

import com.boha.monitor.library.dto.GcmDeviceDTO;
import com.boha.monitor.library.dto.RequestDTO;
import com.boha.monitor.library.dto.ResponseDTO;
import com.boha.platform.library.R;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.io.IOException;

/**
 * Handle registration of device to Google Cloud Messaging
 */
public class GCMUtil {
    public interface GCMUtilListener {
        public void onDeviceRegistered(String id);

        public void onGCMError();
    }

    static Context ctx;
    static GCMUtilListener gcmUtilListener;
    static String registrationID, msg;
    static final String LOG = "GCMUtil";
    static GoogleCloudMessaging gcm;
    static boolean weCool;


    /**
     * Start device registration to Google Cloud Messaging
     * Receive GCM registration string and complete GCM registration by calling back-end
     *
     * @see com.boha.monitor.library.util.GCMUtil.GCMUtilListener
     * @param context
     * @param listener
     */
    public static void startGCMRegistration(final Context context, final GCMUtilListener listener) {
        ctx = context;
        gcmUtilListener = listener;
        weCool = false;

        Thread gcmThread = new Thread(new Runnable() {
            @Override
            public void run() {
                Log.e(LOG, "... startin GCM registration");
                try {
                    if (gcm == null) {
                        gcm = GoogleCloudMessaging.getInstance(ctx);
                    }
                    registrationID = gcm.register(context.getString(R.string.gcm_sender_id));
                    msg = "Device registered, registration ID = \n" + registrationID;
                    SharedUtil.storeRegistrationId(ctx, registrationID);
                    RequestDTO w = new RequestDTO();
                    w.setRequestType(RequestDTO.SEND_GCM_REGISTRATION);
                    w.setGcmRegistrationID(registrationID);
                    NetUtil.sendRequest(ctx, w, new NetUtil.NetUtilListener() {
                        @Override
                        public void onResponse(final ResponseDTO response) {
                            if (response.getStatusCode() == 0) {
                                Log.w(LOG, "############ Device registered to Google on MONITOR PLATFORM server GCM regime");
                                GcmDeviceDTO gcmDevice = new GcmDeviceDTO();
                                gcmDevice.setManufacturer(Build.MANUFACTURER);
                                gcmDevice.setModel(Build.MODEL);
                                gcmDevice.setSerialNumber(Build.SERIAL);
                                gcmDevice.setAndroidVersion(Build.VERSION.RELEASE);
                                gcmDevice.setProduct(Build.PRODUCT);
                                gcmDevice.setApp(ctx.getPackageName());
                                gcmDevice.setRegistrationID(registrationID);
                                SharedUtil.saveGCMDevice(ctx,gcmDevice);
                                weCool = true;
                                listener.onDeviceRegistered(registrationID);
                            }
                        }

                        @Override
                        public void onError(final String message) {
                            if (weCool) {
                                return;
                            }
                            Log.e(LOG, "############ Device failed to register on server GCM regime\n" + message);
//                            listener.onGCMError();
                        }

                        @Override
                        public void onWebSocketClose() {
                            Log.d(LOG, "############## GCMUtil onWebSocketClose");
                        }
                    });

                    Log.i(LOG, msg);

                } catch (IOException e) {
                    listener.onGCMError();
                }
            }
        });
        gcmThread.start();
    }



}

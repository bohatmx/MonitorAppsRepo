package com.boha.monitor.library.services;

/**
 * Created by aubreyM on 15/08/16.
 */
import android.content.Intent;
import android.util.Log;

import com.google.android.gms.iid.InstanceIDListenerService;

public class MonInstanceIDListenerService extends InstanceIDListenerService {

    private static final String TAG = "MyInstanceIDLS";

    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. This call is initiated by the
     * InstanceID provider.
     */
    @Override
    public void onTokenRefresh() {
        // Fetch updated Instance ID token and notify our app's server of any changes (if applicable).
        Log.e(TAG,"##### MonInstanceIDListenerService onTokenRefresh");
        Intent intent = new Intent(this, RegistrationIntentService.class);
        startService(intent);
    }
}
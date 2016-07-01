package com.boha.monitor.firebase.services;

import android.util.Log;

import com.boha.monitor.firebase.data.MonitorCompanyDTO;
import com.boha.monitor.firebase.data.UserDTO;
import com.boha.monitor.firebase.util.DataUtil;
import com.boha.monitor.firebase.util.SharedUtil;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

public class MyFirebaseInstanceIDService extends FirebaseInstanceIdService {
    public MyFirebaseInstanceIDService() {
    }

    static final String TAG = MyFirebaseInstanceIDService.class.getSimpleName();

    @Override
    public void onTokenRefresh() {
        String token = FirebaseInstanceId.getInstance().getToken();
        Log.i(TAG, "onTokenRefreshed: " + token);
        SharedUtil.setFCMtoken(token, getApplicationContext());
        sendToken(token);
    }

    private void sendToken(String token) {

        MonitorCompanyDTO co = SharedUtil.getCompany(getApplicationContext());
        if (co == null) {
            Log.e(TAG, "sendToken: Company is NULL - not sending token ......." );
            return;
        }
        Log.w(TAG, "+++++++++++++++++++ sendToken: send FCM token and update user record");
        UserDTO user = SharedUtil.getUser(getApplicationContext());
        if (user == null) {
            return;
        }
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference ref = db.getReference(DataUtil.MONITOR_DB)
                .child(DataUtil.COMPANIES)
                .child(co.getCompanyID())
                .child(DataUtil.USERS)
                .child(user.getUserID())
                .child("fcmToken");
        ref.setValue(token);
        Log.e(TAG, "sendToken: token sent to database " + token );
    }
}

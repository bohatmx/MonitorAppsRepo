package com.boha.monitor.library.services;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.util.Log;

import com.boha.monitor.library.activities.MonApp;
import com.boha.monitor.library.dto.RequestDTO;
import com.boha.monitor.library.dto.RequestList;
import com.boha.monitor.library.dto.ResponseDTO;
import com.boha.monitor.library.util.OKHttpException;
import com.boha.monitor.library.util.OKUtil;
import com.boha.monitor.library.util.Snappy;
import com.google.gson.Gson;
import com.snappydb.DB;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class RequestIntentService extends IntentService {

    public RequestIntentService() {
        super("RequestIntentService");
    }
    int good, bad;
    Gson gson = new Gson();
    static final String REQUEST = Snappy.REQUEST,
            LOG = RequestIntentService.class.getSimpleName();
    MonApp app;
    @Override
    protected void onHandleIntent(Intent intent) {
        Log.w(LOG,"################### onHandleIntent");
        try {
            app = (MonApp) getApplication();
            final DB snappyDB = app.getSnappyDB();
            OKUtil okUtil = new OKUtil();


            String[] keys = snappyDB.findKeys(REQUEST);
            ResponseDTO response = new ResponseDTO();
            for (String key : keys) {
                String json = snappyDB.get(key);
                RequestDTO dto = gson.fromJson(json,RequestDTO.class);
                if (dto.getDateUploaded() == null)
                    response.getRequestList().add(dto);
            }
            android.util.Log.d(LOG, "............................." +
                    "Read requests with null dateUploaded: " + response.getRequestList().size()
            + " all requests: " + keys.length);
            List<RequestDTO> mList = new ArrayList<>();
            for (RequestDTO w : response.getRequestList()) {
                if (w.getDateUploaded() == null) {
                    mList.add(w);
                }
            }
            if (mList.isEmpty()) {
                Log.w(LOG, "No requests to upload..........quittin");
                return;
            }
            try {
                Log.e(LOG, ".......about to send cached requests: " + mList.size());
                final RequestList rList = new RequestList();
                rList.getRequests().addAll(mList);
                okUtil.sendPOSTRequest(getApplicationContext(), rList, new OKUtil.OKListener() {
                    @Override
                    public void onResponse(ResponseDTO response) {
                        Log.w(LOG,"...onResponse statusCode: " + response.getStatusCode());
                        doWork(rList.getRequests());

                    }

                    @Override
                    public void onError(String message) {
                        Log.e(LOG, message);
                    }
                });
            } catch (OKHttpException e) {
                e.printStackTrace();
            }
        } catch (Exception e) {

        }
    }
    
    private void doWork(List<RequestDTO> list) {
        DB snappyDB = app.getSnappyDB();
        try {
            RequestList rList = new RequestList();
            rList.setRequests(list);
            if (!snappyDB.isOpen()) {
                app.getSnappyDB();
            }
            for (RequestDTO w : rList.getRequests()) {
                String json = snappyDB.get(REQUEST + w.getRequestDate());
                RequestDTO req = gson.fromJson(json, RequestDTO.class);
                snappyDB.del(REQUEST + w.getRequestDate());
                String[] keys = snappyDB.findKeys(REQUEST);
                android.util.Log.w(LOG, "*** Request deleted on Snappy, requestType: " + req.getRequestType() + " " +
                        new Date(req.getRequestDate()).toString()
                        + " requests remaining: " + keys.length);
            }
        } catch (Exception e) {
            Log.e(LOG,"Fail",e);
        }
    }

}
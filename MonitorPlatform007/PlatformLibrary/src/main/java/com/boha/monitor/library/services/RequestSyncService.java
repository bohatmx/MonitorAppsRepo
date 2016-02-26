package com.boha.monitor.library.services;

import android.app.IntentService;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.boha.monitor.library.activities.MonApp;
import com.boha.monitor.library.dto.RequestDTO;
import com.boha.monitor.library.dto.RequestList;
import com.boha.monitor.library.dto.ResponseDTO;
import com.boha.monitor.library.util.OKHttpException;
import com.boha.monitor.library.util.OKUtil;
import com.boha.monitor.library.util.Snappy;
import com.boha.monitor.library.util.WebCheck;
import com.boha.monitor.library.util.WebCheckResult;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages the uploading of offline requests from a list held in cache. Uploads all
 * cached requests in a single batch. Cloud server returns number of good and bad
 * responses. These requests are cached on the device when wifi is not connected. These
 * requests typically are generated from SiteTaskAndStatusAssignmentFragment,
 * SubTaskStatusAssignmentFragment
 * <p/>
 * Typically these requests would be status and location related and the user does not
 * need to view individual responses.
 * <p/>
 * It may be started by a startService call or may be bound to an activity via the
 * IBinder interface. When bound, uses RequestSyncListener to communicate with the binding
 * activity
 * <p/>
 * Entry points: onHandleIntent, startSyncCachedRequests (when bound to activity)
 */
public class RequestSyncService extends IntentService {

    public RequestSyncService() {
        super("RequestSyncService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.w(LOG, "### @@@@@@@@@@@@@@@@@@@@@@@@@@@ - RequestSyncService onHandleIntent");
        WebCheckResult wc = WebCheck.checkNetworkAvailability(getApplicationContext());
        if (wc.isNetworkUnavailable()) {
            Log.e(LOG,"........isNetworkUnavailable: " + wc.isNetworkUnavailable() + ", Service quitting ... ");
            return;
        }
        MonApp app = (MonApp) getApplication();
        app.getSnappyDB();
        Snappy.getRequests(app, new Snappy.SnappyReadListener() {
            @Override
            public void onDataRead(ResponseDTO response) {
                Log.w(LOG, "......................................." +
                        "onDataRead ... about to send if appropriate");
                if (response == null) {
                    list = new ArrayList<>();
                } else {
                    list = response.getRequestList();
                }
                if (list.isEmpty()) {
                    Log.e(LOG, "#### no requests in list from cache, quitting this scene...");
                    if (requestSyncListener != null)
                        requestSyncListener.onTasksSynced(0, 0);
                } else {
                    Log.w(LOG, "...processing requests found in cache: " + list.size());
                    sendRequest(list);
                }
            }

            @Override
            public void onError(String message) {

            }
        });
    }

    List<RequestDTO> list = new ArrayList<>();
    int good, bad, index;
    OKUtil okUtil = new OKUtil();


    private void sendRequest(final List<RequestDTO> list) {
        Log.d(LOG, ".......... sendRequest list: " + list.size());
        List<RequestDTO> mList = new ArrayList<>();
        for (RequestDTO w : list) {
            if (w.getDateUploaded() == null) {
                mList.add(w);
            }
        }
        if (mList.isEmpty()) {
            Log.w(LOG, "No requests to upload");
            return;
        }
        try {
            Log.e(LOG, ".......about to send cached requests: " + mList.size());
            RequestList rList = new RequestList();
            rList.getRequests().addAll(mList);
            okUtil.sendPOSTRequest(getApplicationContext(), rList, new OKUtil.OKListener() {
                @Override
                public void onResponse(ResponseDTO response) {
                    if (response.getStatusCode() == 0) {
                        good = response.getGoodCount();
                        Snappy.updateRequestDates((MonApp) getApplication(), list);
                    } else {
                        bad = response.getBadCount();
                    }

                }

                @Override
                public void onError(String message) {
                    Log.e(LOG, message);
                }
            });
        } catch (OKHttpException e) {
            e.printStackTrace();
        }

    }

    static final String LOG = RequestSyncService.class.getSimpleName();

    public class LocalBinder extends Binder {

        public RequestSyncService getService() {
            return RequestSyncService.this;
        }
    }


    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    private final IBinder mBinder = new LocalBinder();

//    public void startSyncCachedRequests(RequestSyncListener rsl) {
//        requestSyncListener = rsl;
//        onHandleIntent(null);
//    }


    public interface RequestSyncListener {
        public void onTasksSynced(int goodResponses, int badResponses);

        public void onError(String message);
    }

    RequestSyncListener requestSyncListener;
}

package com.boha.monitor.library.services;

import android.app.IntentService;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.boha.monitor.library.dto.RequestDTO;
import com.boha.monitor.library.dto.RequestList;
import com.boha.monitor.library.dto.ResponseDTO;
import com.boha.monitor.library.util.NetUtil;
import com.boha.monitor.library.util.RequestCacheUtil;

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
        Log.w(LOG, "### RequestSyncService onHandleIntent");
        RequestCacheUtil.getRequests(getApplicationContext(), new RequestCacheUtil.RequestCacheListener() {
            @Override
            public void onError(String message) {}

            @Override
            public void onRequestAdded() {}

            @Override
            public void onRequestsRetrieved(RequestList list) {
                requestList = list;
                Log.i(LOG, "++ RequestCache returned from disk, entries: "
                        + requestList.getRequests().size());
                if (requestList.getRequests().isEmpty()) {
                    if (requestSyncListener != null) {
                        requestSyncListener.onTasksSynced(0,0);
                    }
                    return;

                }
                if (requestList.isRideWebSocket()) {
                    NetUtil.sendRequest(getApplicationContext(), list, new NetUtil.NetUtilListener() {
                        @Override
                        public void onResponse(ResponseDTO response) {
                            Log.i(LOG, "** cached requests sent up via websocket! good responses: " + response.getGoodCount() +
                                    " bad responses: " + response.getBadCount());
                            RequestCacheUtil.clearCache(getApplicationContext(),null);
                            if (requestSyncListener != null) {
                                if (response != null) {
                                    requestSyncListener.onTasksSynced(response.getGoodCount(),
                                            response.getBadCount());
                                }
                            }
                        }

                        @Override
                        public void onError(String message) {
                            if (requestSyncListener != null)
                                requestSyncListener.onError(message);
                        }

                        @Override
                        public void onWebSocketClose() {

                        }
                    });
                } else {
                    controlRequestUpload();
                }
            }

        });

    }

    int index, batches, batchIndex;
    static final int BATCH_SIZE = 12;

    private void controlRequestUpload() {

        if (requestList.getRequests().isEmpty()) {
            Log.d(LOG, "#### no requests cached, quitting...");
            requestSyncListener.onTasksSynced(0, 0);
            return;
        }

        index = 0;
        batches = requestList.getRequests().size() / BATCH_SIZE;
        int rem = requestList.getRequests().size() % BATCH_SIZE;
        if (rem > 0) {
            batches++;
        }
        lists = new ArrayList<>();
        for (int i = 0; i < batches; i++) {
            lists.add(new RequestList());
        }
        batchIndex = 0;
        prepareBatch();

    }

    List<RequestList> lists;
    RequestList requestList;

    private void prepareBatch() {

        RequestList rex = new RequestList();
        rex.setRequests(new ArrayList<RequestDTO>());
        for (RequestDTO x: requestList.getRequests()) {
            rex.getRequests().add(x);
            if ((index + 1) % BATCH_SIZE == 0) {
                lists.get(batchIndex).setRequests(rex.getRequests());
                rex.setRequests(new ArrayList<RequestDTO>());
                batchIndex++;
            }
            index++;
        }

        batchIndex = 0;
        controlBatch();
    }

    int good, bad;
    private void controlBatch() {
        if (batchIndex < lists.size()) {
            sendBatch(lists.get(batchIndex));
            return;
        }
        RequestCacheUtil.clearCache(getApplicationContext(),null);
        if (requestSyncListener != null)
            requestSyncListener.onTasksSynced(good, bad);
    }
    private void sendBatch (final RequestList list) {
        NetUtil.sendRequest(getApplicationContext(), list, new NetUtil.NetUtilListener() {
            @Override
            public void onResponse(ResponseDTO response) {
                Log.i(LOG, "** cached requests sent up! good responses: " + response.getGoodCount() +
                        " bad responses: " + response.getBadCount());
                good += response.getGoodCount();
                bad += response.getBadCount();
                batchIndex++;
                controlBatch();

            }

            @Override
            public void onError(String message) {
                if (requestSyncListener != null)
                    requestSyncListener.onError(message);
            }

            @Override
            public void onWebSocketClose() {

            }
        });

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

    public void startSyncCachedRequests(RequestSyncListener rsl) {
        requestSyncListener = rsl;
        onHandleIntent(null);
    }


    public interface RequestSyncListener {
        public void onTasksSynced(int goodResponses, int badResponses);

        public void onError(String message);
    }

    RequestSyncListener requestSyncListener;
}

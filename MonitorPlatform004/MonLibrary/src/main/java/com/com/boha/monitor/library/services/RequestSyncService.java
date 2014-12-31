package com.com.boha.monitor.library.services;

import android.app.IntentService;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.com.boha.monitor.library.dto.transfer.RequestList;
import com.com.boha.monitor.library.dto.transfer.ResponseDTO;
import com.com.boha.monitor.library.util.CacheUtil;
import com.com.boha.monitor.library.util.ErrorUtil;
import com.com.boha.monitor.library.util.WebCheck;
import com.com.boha.monitor.library.util.WebCheckResult;
import com.com.boha.monitor.library.util.WebSocketUtilForRequests;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
/**
 * Manages the uploading of offline requests from a list held in cache. Uploads all
 * cached requests in a single batch. Cloud server returns number of good and bad
 * responses. These requests are cached on the device when wifi is not connected. These
 * requests typically are generated from SiteTaskAndStatusAssignmentFragment,
 * SubTaskStatusAssignmentFragment
 *
 * Typically these requests would be status and location related and the user does not
 * need to view individual responses.
 *
 * It may be started by a startService call or may be bound to an activity via the
 * IBinder interface. When bound, uses RequestSyncListener to communicate with the binding
 * activity
 *
 * Entry points: onHandleIntent, startSyncCachedRequests (when bound to activity)
 */
public class RequestSyncService extends IntentService {

    public RequestSyncService() {
        super("RequestSyncService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.e(LOG, "### RequestSyncService onHandleIntent");
        CacheUtil.getCachedRequests(getApplicationContext(), new CacheUtil.CacheRequestListener() {
            @Override
            public void onDataCached() {

            }

            @Override
            public void onRequestCacheReturned(RequestCache cache) {
                if (cache != null) {
                    requestCache = cache;
                    Log.i(LOG, "++ RequestCache returned from disk, entries: "
                            + requestCache.getRequestCacheEntryList().size());
                    print();
                    controlRequestUpload();
                } else {
                    Log.e(LOG, "-- requestCache is null");
                }
            }

            @Override
            public void onError() {
                Log.e(LOG, "## Problem with getting RequestCache, may be first time through");
            }
        });

    }

    int currentIndex;

    private void controlRequestUpload() {
        WebCheckResult r = WebCheck.checkNetworkAvailability(getApplicationContext());
        if (r.isWifiConnected()) {
            Log.i(LOG, "%%% WIFI is connected and cached requests about to be sent to cloud....");
            RequestList list = new RequestList();
            for (RequestCacheEntry e : requestCache.getRequestCacheEntryList()) {
                list.getRequests().add(e.getRequest());
            }
            if (list.getRequests().isEmpty()) {
                Log.d(LOG,"#### no requests cached, quitting...");
                return;
            }
            Log.w(LOG, "### sending list of cached requests: " + list.getRequests().size());
            WebSocketUtilForRequests.sendRequest(getApplicationContext(), list, new WebSocketUtilForRequests.WebSocketListener() {
                @Override
                public void onMessage(ResponseDTO response) {
                    if (!ErrorUtil.checkServerError(getApplicationContext(), response)) {
                        return;
                    }
                    Log.i(LOG, "** cached requests sent up! good responses: " + response.getGoodCount() +
                            " bad responses: " + response.getBadCount());
                    //Util.showToast(getApplicationContext(), getString(R.string.cache_uploaded));
                    for (RequestCacheEntry e : requestCache.getRequestCacheEntryList()) {
                        e.setDateUploaded(new Date());
                    }
                    cleanupCache();
                    requestSyncListener.onTasksSynced(response.getGoodCount(),response.getBadCount());
                }

                @Override
                public void onClose() {

                }

                @Override
                public void onError(String message) {
                    requestSyncListener.onError(message);
                }
            });
        } else {
            Log.e(LOG, "WIFI is NOT connected so cannot sync");
        }
    }

    private void cleanupCache() {
        List<RequestCacheEntry> list = new ArrayList<>();
        for (RequestCacheEntry e : requestCache.getRequestCacheEntryList()) {
            if (e.getDateUploaded() == null) {
                list.add(e);
            }
        }
        Log.i(LOG, "cache cleaned up, pending: " + list.size());
        requestCache.setRequestCacheEntryList(list);
        CacheUtil.cacheRequest(getApplicationContext(), requestCache, null);
    }

    RequestCache requestCache;
    static final String LOG = RequestSyncService.class.getSimpleName();

    private void print() {
        for (RequestCacheEntry e : requestCache.getRequestCacheEntryList()) {
            Log.w(LOG, "+++ " + e.getDateRequested() + " requestType: " + e.getRequest().getRequestType());
        }
    }
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

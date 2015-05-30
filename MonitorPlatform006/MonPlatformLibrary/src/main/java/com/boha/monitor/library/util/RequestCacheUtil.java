package com.boha.monitor.library.util;

import android.content.Context;
import android.util.Log;

import com.boha.monitor.library.dto.transfer.RequestDTO;
import com.boha.monitor.library.services.RequestCache;
import com.boha.monitor.library.services.RequestCacheEntry;

import java.util.Date;

/**
 * Created by aubreyM on 14/12/13.
 */
public class RequestCacheUtil {

    public static void addRequest(final Context ctx, final RequestDTO request,
                            final CacheUtil.CacheRequestListener listener) {
        final RequestCacheEntry e = new RequestCacheEntry();
        e.setRequest(request);
        e.setDateRequested(new Date());
        initialize(ctx, new CacheUtil.CacheRequestListener() {
            @Override
            public void onDataCached() {}

            @Override
            public void onRequestCacheReturned( RequestCache cache) {
                if (cache != null) {
                    Log.i(LOG, "*** RequestCache has been retrieved from cache: "
                            + cache.getRequestCacheEntryList().size());
                } else {
                    cache = new RequestCache();
                }
                cache.getRequestCacheEntryList().add(e);
                CacheUtil.cacheRequest(ctx, cache, new CacheUtil.CacheRequestListener() {
                    @Override
                    public void onDataCached() {
                        Log.i(LOG, "*** Request has been cached");
                        if (listener != null)
                            listener.onDataCached();

                    }

                    @Override
                    public void onRequestCacheReturned(RequestCache cache) {

                    }

                    @Override
                    public void onError() {

                    }
                });

            }

            @Override
            public void onError() {
                Log.e(LOG, "--- failed to add initial request to cache");
            }
        });


    }


    private static void initialize(Context ctx,  final CacheUtil.CacheRequestListener listener) {
        Log.d(LOG, "*** initialize the request cache - fetch from disk");
        CacheUtil.getCachedRequests(ctx, new CacheUtil.CacheRequestListener() {
            @Override
            public void onDataCached() {

            }

            @Override
            public void onRequestCacheReturned( RequestCache cache) {
                Log.w(LOG, "RequestCache returned, list: " +
                        cache.getRequestCacheEntryList().size());
                listener.onRequestCacheReturned(cache);
            }

            @Override
            public void onError() {
                Log.e(LOG, "----- RequestCache failed to initialize");
            }
        });
    }

    static final String LOG = RequestCacheUtil.class.getSimpleName();
}

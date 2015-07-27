package com.boha.monitor.library.util;

import android.content.Context;
import android.util.Log;

import com.boha.monitor.library.dto.RequestDTO;
import com.boha.monitor.library.dto.RequestList;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by aubreyM on 14/12/13.
 */
public class RequestCacheUtil {

    static final Gson GSON = new Gson();
    static final String JSON_REQUEST = "requests.json";

    public interface RequestCacheListener {
        void onError(String message);

        void onRequestAdded();

        void onRequestsRetrieved(RequestList requestList);
    }

    static RequestCacheListener mListener;


    public static void clearCache(final Context ctx,
                                  final RequestCacheListener listener) {
        mListener = listener;
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                RequestList cache;
                FileInputStream stream;
                try {
                    stream = ctx.openFileInput(JSON_REQUEST);
                    String x = getStringFromInputStream(stream);
                    cache = GSON.fromJson(x, RequestList.class);
                    Log.i(LOG, "++ request cache retrieved prior to adding; requests: " + cache.getRequests().size());
                    cache.setRequests(new ArrayList<RequestDTO>());
                    saveRequest(ctx,null,cache);
                } catch (FileNotFoundException e) {
                    Log.d(LOG, "## request cache file not found. not initialised yet. no problem, creating new cache");
                    cache = new RequestList();
                    try {
                        saveRequest(ctx,null,cache);
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }


                } catch (IOException e) {
                    Log.v(LOG, "-- Failed to retrieve cache", e);
                    if (listener != null)
                        listener.onError("Error adding request to cache");
                }
            }
        });

        thread.start();
    }

    public static void addRequests(final Context ctx, final List<RequestDTO> list, final boolean clearCache,
                                   final RequestCacheListener listener) {
        mListener = listener;
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                RequestList cache;
                FileInputStream stream;
                try {
                    stream = ctx.openFileInput(JSON_REQUEST);
                    String x = getStringFromInputStream(stream);
                    cache = GSON.fromJson(x, RequestList.class);
                    Log.i(LOG, "++ request cache retrieved prior to adding; requests: " + cache.getRequests().size());
                    if (clearCache) {
                        cache.setRequests(new ArrayList<RequestDTO>());
                    }
                    for (RequestDTO f : list) {
                        saveRequest(ctx, f, cache);
                    }
                    if (listener != null)
                        listener.onRequestAdded();
                } catch (FileNotFoundException e) {
                    Log.d(LOG, "## request cache file not found. not initialised yet. no problem, creating new cache");
                    cache = new RequestList();
                    try {
                        for (RequestDTO f : list) {
                            saveRequest(ctx, f, cache);
                        }
                        if (listener != null)
                            listener.onRequestAdded();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }


                } catch (IOException e) {
                    Log.v(LOG, "-- Failed to retrieve cache", e);
                    if (listener != null)
                        listener.onError("Error adding request to cache");
                }
            }
        });

        thread.start();
    }

    public static void addRequest(final Context ctx, final RequestDTO request,
                                  final RequestCacheListener listener) {
        mListener = listener;
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                RequestList cache;
                FileInputStream stream;
                try {
                    stream = ctx.openFileInput(JSON_REQUEST);
                    String x = getStringFromInputStream(stream);
                    cache = GSON.fromJson(x, RequestList.class);
                    Log.i(LOG, "++ request cache retrieved prior to adding; requests: " + cache.getRequests().size());
                    saveRequest(ctx, request, cache);
                    listener.onRequestAdded();
                } catch (FileNotFoundException e) {
                    Log.d(LOG, "## request cache file not found. not initialised yet. no problem, creating new cache");
                    cache = new RequestList();
                    try {
                        saveRequest(ctx, request, cache);
                        listener.onRequestAdded();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }


                } catch (IOException e) {
                    Log.v(LOG, "-- Failed to retrieve cache", e);
                    listener.onError("Error adding request to cache");
                }
            }
        });

        thread.start();
    }

    public static void getRequests(final Context ctx,
                                   final RequestCacheListener listener) {
        mListener = listener;
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                RequestList cache = new RequestList();
                FileInputStream stream;
                try {
                    stream = ctx.openFileInput(JSON_REQUEST);
                    String x = getStringFromInputStream(stream);
                    cache = GSON.fromJson(x, RequestList.class);
                    Log.i(LOG, "++ request cache retrieved");

                    mListener.onRequestsRetrieved(cache);
                } catch (FileNotFoundException e) {
                    Log.d(LOG, "## cache file not found. not initialised yet. no problem, creating new cache");
                    mListener.onRequestsRetrieved(cache);


                } catch (IOException e) {
                    Log.v(LOG, "-- Failed to retrieve cache", e);
                    mListener.onRequestsRetrieved(cache);
                }
            }
        });

        thread.start();
    }


    private static void saveRequest(final Context ctx, final RequestDTO request, RequestList cache) throws IOException {
        if (request != null)
            cache.getRequests().add(request);
        String json = GSON.toJson(cache);

        FileOutputStream outputStream = ctx.openFileOutput(JSON_REQUEST, Context.MODE_PRIVATE);
        write(outputStream, json);
        File file = ctx.getFileStreamPath(JSON_REQUEST);
        if (file != null) {
            Log.e(LOG, "Request cache written - file length: " + file.length() + " requests: " + cache.getRequests().size());
        }
    }

    private static void write(FileOutputStream outputStream, String json) throws IOException {
        outputStream.write(json.getBytes());
        outputStream.close();
    }

    private static String getStringFromInputStream(InputStream is) throws IOException {

        BufferedReader br = null;
        StringBuilder sb = new StringBuilder();
        String line;
        try {
            br = new BufferedReader(new InputStreamReader(is));
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

        } finally {
            if (br != null) {
                br.close();
            }
        }
        String json = sb.toString();
        return json;

    }


    static final String LOG = RequestCacheUtil.class.getSimpleName();
}

package com.boha.monitor.library.util;

import android.content.Context;
import android.util.Log;

import com.boha.monitor.library.dto.ResponseDTO;
import com.boha.monitor.library.dto.VideoUploadDTO;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * Created by aubreyM on 2014/04/16.
 */
public class CacheVideoUtil {

    public interface CacheVideoListener {
        void onDataDeserialized(ResponseDTO response);
        void onError(String message);
        void onDataCached();
    }


    public static void removeUploadedVideo(final Context ctx, final VideoUploadDTO dto,
                                           final CacheVideoListener listener) {
        getCachedVideoList(ctx, new CacheVideoListener() {
            @Override
            public void onDataDeserialized(ResponseDTO response) {
                int index = 0;
                for (VideoUploadDTO x: response.getVideoUploadList()) {
                    if (x.getDateTaken().longValue() == dto.getDateTaken().longValue()) {
                        File file = new File(dto.getVideoUri());
                        boolean isDeleted = file.delete();
                        if (isDeleted) {
                            Log.d(LOG,"**** video file deleted: " + file.getAbsolutePath());
                        } else {
                            Log.e(LOG,"#### video file delete problem");
                        }
                        break;
                    }
                    index++;
                }
                response.getVideoUploadList().remove(index);
                cacheVideo(ctx,response,listener);
            }

            @Override
            public void onError(String message) {

            }

            @Override
            public void onDataCached() {

            }
        });
    }

    /**
     * Add VideoUploadDTO to local cache prior to uploading
     * @param context
     * @param r
     * @param cacheVideoListener
     */
    public static void addVideo(final Context context, final VideoUploadDTO r,
                                final CacheVideoListener cacheVideoListener) {

        getCachedVideoList(context, new CacheVideoListener() {
            @Override
            public void onDataDeserialized(ResponseDTO response) {
                response.getVideoUploadList().add(r);
                cacheVideo(context,response,cacheVideoListener);
            }

            @Override
            public void onError(String message) {

            }

            @Override
            public void onDataCached() {
            }
        });

    }

    /**
     * Cache list of VideoUploadDTO (inside ResponseDTO container)
     * @param context
     * @param r
     * @param videoList
     */
    private static void cacheVideo(final Context context, final ResponseDTO r,
                                  final CacheVideoListener videoList) {

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                String json = null;
                File file = null;
                FileOutputStream outputStream;
                try {
                    json = gson.toJson(r);
                    outputStream = context.openFileOutput(VIDEO_CLIP_FILENAME, Context.MODE_PRIVATE);
                    outputStream.write(json.getBytes());
                    outputStream.close();

                    file = context.getFileStreamPath(VIDEO_CLIP_FILENAME);
                    Log.i(LOG, "VideoUploadDTO cached: " + file.getAbsolutePath() +
                            " - length: " + file.length());
                    videoList.onDataCached();
                } catch (IOException e) {
                    Log.e(LOG, "Failed to cache data", e);
                    videoList.onError("Failed to cache data");
                }
            }
        });

        thread.start();
    }

    /**
     * Get list of VideoUploadDTO from local cache
     * @param context
     * @param cacheVideoListener
     */
    public static void getCachedVideoList(final Context context,
                                          final CacheVideoListener cacheVideoListener) {

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                ResponseDTO vcc = null;
                FileInputStream stream;
                try {
                    stream = context.openFileInput(VIDEO_CLIP_FILENAME);
                    String json = getStringFromInputStream(stream);
                    Log.i(LOG, "VideoUpload list: " + json.length());
                    vcc = gson.fromJson(json, ResponseDTO.class);
                    cacheVideoListener.onDataDeserialized(vcc);
                } catch (Exception e) {
                    vcc = new ResponseDTO();
                    vcc.setVideoUploadList(new ArrayList<VideoUploadDTO>());
                    cacheVideoListener.onDataDeserialized(vcc);
                }
            }
        });

        thread.start();
    }

    static ResponseDTO response;

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
    static final String LOG = "CacheVideoUtil";
    static final String
            VIDEO_CLIP_FILENAME = "videoUploads.json";
    static final Gson gson = new Gson();
}

package com.boha.monitor.library.util;

import android.content.Context;
import android.util.Log;

import com.boha.monitor.library.dto.RequestDTO;
import com.boha.monitor.library.dto.ResponseDTO;
import com.boha.monitor.library.dto.VideoUploadDTO;
import com.cloudinary.Cloudinary;

import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by aubreyM on 15/06/08.
 */
public class CDNVideoUploader {
    public interface CDNVideoUploaderListener {
        void onFileUploaded(VideoUploadDTO video);

        void onError(String message);
    }

    static CDNVideoUploaderListener mListener;
    static final String
            LOG = CDNVideoUploader.class.getSimpleName(),
            VIDEO = "video";


    public static void uploadVideoFile(final Context ctx, final VideoUploadDTO dto, CDNVideoUploaderListener uploaderListener) {
        mListener = uploaderListener;
        Log.d(LOG, "##### starting CDNVideoUploader, uploading: " + dto.getVideoUri());
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                final long start = System.currentTimeMillis();
                try {
                    Map config = new HashMap();

                    config.put("cloud_name", CDNUploader.CLOUD_NAME);
                    config.put("api_key", CDNUploader.API_KEY);
                    config.put("api_secret", CDNUploader.API_SECRET);
                    config.put("resource_type", VIDEO);

                    Cloudinary cloudinary = new Cloudinary(config);
                    File file = new File(dto.getFilePath());
                    Log.d(LOG,"## videoFile: " + file.getAbsolutePath()
                            + " length: " + file.length());

                    Map map = cloudinary.uploader().upload(file, config);

                    long end = System.currentTimeMillis();
                    Log.i(LOG, "---- video uploaded: " + map.get("url") + " elapsed: "
                            + Util.getElapsed(start, end) + " seconds");

                    dto.setUrl((String) map.get("url"));
                    dto.setSecureUrl((String) map.get("secure_url"));
                    dto.setSignature((String) map.get("signature"));
                    dto.seteTag((String) map.get("etag"));
                    dto.setHeight((int) map.get("height"));
                    dto.setWidth((int) map.get("width"));
                    dto.setBytes((int) map.get("bytes"));
                    dto.setDateUploaded(new Date().getTime());

                    //dto.setAudioCodec((String) map.get("audioCodec"));

                    RequestDTO w = new RequestDTO(RequestDTO.ADD_VIDEO);
                    w.setVideoUpload(dto);

                    NetUtil.sendRequest(ctx, w, new NetUtil.NetUtilListener() {
                        @Override
                        public void onResponse(ResponseDTO response) {
                            Log.i(LOG, "#### video metadata sent to server");
                            mListener.onFileUploaded(dto);
                        }

                        @Override
                        public void onError(String message) {
                            Log.e(LOG, message);
                            dto.setDateUploaded(null);
                            mListener.onError(message);
                        }

                        @Override
                        public void onWebSocketClose() {

                        }
                    });
                } catch (Exception e) {
                    Log.e(LOG, "CDN upload Failed", e);
                    mListener.onError("CDN Error uploading video");

                }
            }
        });

        thread.start();
    }
}

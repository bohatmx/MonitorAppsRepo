package com.boha.monitor.library.util;

import android.content.Context;
import android.util.Log;

import com.boha.monitor.library.dto.PhotoUploadDTO;
import com.boha.monitor.library.dto.RequestDTO;
import com.boha.monitor.library.dto.ResponseDTO;
import com.cloudinary.Cloudinary;

import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by aubreyM on 15/06/08.
 */
public class CDNUploader {
    public interface CDNUploaderListener {
        void onFileUploaded(PhotoUploadDTO photo);
        void onError(String message);
    }
    static CDNUploaderListener mListener;
    static final String LOG = CDNUploader.class.getSimpleName();
    static final String
            API_KEY = "397571984789619",
            API_SECRET = "2RBq1clEHC5X_0eQlNP-K3yhA8U",
            CLOUD_NAME = "bohatmx";


    private static String getPublicID(final Context ctx,PhotoUploadDTO dto) {
        StringBuilder sb = new StringBuilder();
        sb.append("mpc").append(SharedUtil.getCompany(ctx).getCompanyID());
        sb.append("/p").append(dto.getProjectID());

        return sb.toString();
    }

    public static void uploadFile(final Context ctx, final PhotoUploadDTO dto, CDNUploaderListener uploaderListener) {
        mListener = uploaderListener;
        Log.d(LOG,"##### starting CDNUploader uploadFile: " + dto.getThumbFilePath());
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                final long start = System.currentTimeMillis();
                Map config = new HashMap();
                config.put("cloud_name", CLOUD_NAME);
                config.put("api_key", API_KEY);
                config.put("api_secret", API_SECRET);
                config.put("public_id", getPublicID(ctx,dto));

                Cloudinary cloudinary = new Cloudinary(config);

                File file = new File(dto.getThumbFilePath());
                Map map = null;
                try {
                    map = cloudinary.uploader().upload(file, config);
                } catch (Exception e) {
                    Log.e(LOG, "CDN Failed", e);
                    mListener.onError("Error uploading image to CDN");
                }

                long end = System.currentTimeMillis();
                Log.i(LOG, "---- photo uploaded: "+ map.get("url") + " elapsed: "
                        + Util.getElapsed(start, end) + " seconds");

                dto.setUri((String) map.get("url"));
                dto.setSecureUrl((String) map.get("secure_url"));
                dto.setSignature((String) map.get("signature"));
                dto.seteTag((String) map.get("etag"));
                dto.setHeight((int) map.get("height"));
                dto.setWidth((int) map.get("width"));
                dto.setBytes((int) map.get("bytes"));
                dto.setDateUploaded(new Date().getTime());

                RequestDTO w = new RequestDTO(RequestDTO.ADD_PHOTO);
                w.setPhotoUpload(dto);

                NetUtil.sendRequest(ctx, w, new NetUtil.NetUtilListener() {
                    @Override
                    public void onResponse(ResponseDTO response) {
                        Log.i(LOG, "#### photo metadata sent to server");
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

            }
        });
        thread.start();
    }
}

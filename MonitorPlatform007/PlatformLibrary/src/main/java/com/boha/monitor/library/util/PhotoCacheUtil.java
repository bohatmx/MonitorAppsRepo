package com.boha.monitor.library.util;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.boha.monitor.library.dto.PhotoUploadDTO;
import com.boha.monitor.library.dto.ResponseDTO;
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
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Created by aubreyM on 2014/12/30.
 */
public class PhotoCacheUtil {

    public interface PhotoCacheListener {
        public void onFileDataDeserialized(ResponseDTO response);

        public void onDataCached(PhotoUploadDTO photo);

        public void onError();
    }

    public interface PhotoCacheRetrieveListener {
        public void onFileDataDeserialized(ResponseDTO response);

        public void onDataCached();

        public void onError();
    }

    static PhotoCacheListener photoCacheListener;

    static PhotoCacheRetrieveListener photoCacheRetrieveListener = new PhotoCacheRetrieveListener() {
        @Override
        public void onFileDataDeserialized(ResponseDTO response) {

        }

        @Override
        public void onDataCached() {

        }

        @Override
        public void onError() {

        }
    };


    static ResponseDTO response = new ResponseDTO();
    static PhotoUploadDTO photoUpload;
    static Context ctx;
    static final String JSON_PHOTO = "photos.json";


    public static void cachePhoto(Context context, final PhotoUploadDTO photo, PhotoCacheListener listener) {
        photoUpload = photo;
        photoCacheListener = listener;
        ctx = context;
        new CacheRetrieveForUpdateTask().execute();
    }


    public static void getCachedPhotos(Context context, PhotoCacheListener listener) {
        photoCacheListener = listener;
        ctx = context;
        new CacheRetrieveTask().execute();
    }

    public static void clearCache(Context context, final List<PhotoUploadDTO> uploadedList) {
        ctx = context;
        getCachedPhotos(context, new PhotoCacheListener() {
            @Override
            public void onFileDataDeserialized(ResponseDTO r) {
                List<PhotoUploadDTO> pending = new ArrayList<>();
                for (PhotoUploadDTO p : r.getPhotoUploadList()) {
                    for (PhotoUploadDTO ps : uploadedList) {
                        if (ps.getThumbFilePath().equalsIgnoreCase(p.getThumbFilePath())) {
                            p.setDateUploaded(new Date().getTime());
                            File f = new File(ps.getThumbFilePath());
                            if (f.exists()) {
                                boolean del = f.delete();
                                Log.w(LOG, "### deleted image file: " + ps.getThumbFilePath() + " - " + del);
                            }
                        }
                    }
                }
                for (PhotoUploadDTO p : r.getPhotoUploadList()) {
                    if (p.getDateUploaded() == null) {
                        pending.add(p);
                    }
                }
                r.setPhotoUploadList(pending);
                response = r;
                Log.i(LOG, "## after clearing cache, pending photos: " + pending.size() + " - writing new cache");
                new CacheTask().execute();

            }

            @Override
            public void onDataCached(PhotoUploadDTO p) {

            }

            @Override
            public void onError() {

            }
        });
    }

    static final int CACHE_LIMIT = 100;
    public static void updateUploadedPhoto(Context context, final PhotoUploadDTO photo) {
        ctx = context;
        getCachedPhotos(context, new PhotoCacheListener() {
            @Override
            public void onFileDataDeserialized(ResponseDTO r) {
                List<PhotoUploadDTO> pending = new ArrayList<>(r.getPhotoUploadList().size());

                for (PhotoUploadDTO p : r.getPhotoUploadList()) {
                    if (photo.getThumbFilePath().equalsIgnoreCase(p.getThumbFilePath())) {
                        p.setDateUploaded(photo.getDateUploaded());
                    }
                    pending.add(p);
                }
                Collections.sort(pending);
                if (pending.size() > CACHE_LIMIT) {
                    if (pending.get(pending.size() - 1).getDateUploaded() != null) {
                        File file = new File(pending.get(pending.size() - 1).getThumbFilePath());
                        if (file.exists()) {
                            file.delete();
                        }
                        pending.remove(pending.size() - 1);
                    }
                }
                r.setPhotoUploadList(pending);

                response = r;
                int uploaded = 0, pendingCount = 0;
                for (PhotoUploadDTO pp : response.getPhotoUploadList()) {
                    if (pp.getDateUploaded() == null) {
                        pendingCount++;
                    } else {
                        uploaded++;
                    }
                }
                Log.e(LOG, "## after updating uploaded file, pending uploads: " + pendingCount
                        + " - uploaded OK: " + uploaded + " total photos in cache: " + response.getPhotoUploadList().size());
                new CacheTask().execute();

            }

            @Override
            public void onDataCached(PhotoUploadDTO p) {

            }

            @Override
            public void onError() {

            }
        });
    }


    static class CacheTask extends AsyncTask<Void, Void, Integer> {


        @Override
        protected Integer doInBackground(Void... voids) {

            FileOutputStream outputStream;
            try {
                String json = gson.toJson(response);
                outputStream = ctx.openFileOutput(JSON_PHOTO, Context.MODE_PRIVATE);
                write(outputStream, json);
                if (!response.getPhotoUploadList().isEmpty()) {
                    Log.w(LOG, "### Photos in cache ###: " + response.getPhotoUploadList().size());
                } else {
                    Log.w(LOG, "### no photos in cache");
                }

            } catch (IOException e) {
                Log.e(LOG, "Failed to cache data", e);
                return 9;
            }
            return 0;
        }

        private void write(FileOutputStream outputStream, String json) throws IOException {
            outputStream.write(json.getBytes());
            outputStream.close();
        }

        @Override
        protected void onPostExecute(Integer v) {
            if (photoCacheListener != null) {
                if (v > 0) {
                    photoCacheListener.onError();
                } else
                    photoCacheListener.onDataCached(photoUpload);
            }

        }
    }

    static class CacheRetrieveTask extends AsyncTask<Void, Void, ResponseDTO> {

        private ResponseDTO getData(FileInputStream stream) throws IOException {
            String json = getStringFromInputStream(stream);
            ResponseDTO response = gson.fromJson(json, ResponseDTO.class);
            return response;
        }

        @Override
        protected ResponseDTO doInBackground(Void... voids) {
            ResponseDTO response = new ResponseDTO();
            response.setPhotoUploadList(new ArrayList<PhotoUploadDTO>());
            FileInputStream stream;
            try {
                stream = ctx.openFileInput(JSON_PHOTO);
                response = getData(stream);
            } catch (FileNotFoundException e) {
                Log.w(LOG, "############# cache file not found. not initialised yet. no problem, type = PHOTOS");
                return response;

            } catch (IOException e) {
                Log.e(LOG, "#### doInBackground - returning a new response object, type = PHOTOS");
            }

            return response;
        }

        @Override
        protected void onPostExecute(ResponseDTO v) {
            if (photoCacheListener == null)
                return;
            else {
                photoCacheListener.onFileDataDeserialized(v);
            }


        }
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

    static final String LOG = PhotoCacheUtil.class.getSimpleName();
    static final Gson gson = new Gson();

    static class CacheRetrieveForUpdateTask extends AsyncTask<Void, Void, ResponseDTO> {

        private ResponseDTO getData(FileInputStream stream) throws IOException {
            String json = getStringFromInputStream(stream);
            ResponseDTO response = gson.fromJson(json, ResponseDTO.class);
            return response;
        }

        @Override
        protected ResponseDTO doInBackground(Void... voids) {
            ResponseDTO response = new ResponseDTO();
            response.setPhotoUploadList(new ArrayList<PhotoUploadDTO>());
            FileInputStream stream;
            try {
                stream = ctx.openFileInput(JSON_PHOTO);
                response = getData(stream);
            } catch (FileNotFoundException e) {
                Log.w(LOG, "############# cache file not found. not initialised yet. no problem, type = PHOTOS");
                return response;

            } catch (IOException e) {
                Log.d(LOG, "#### doInBackground - returning a new response object, type = PHOTOS");
            }

            return response;
        }

        @Override
        protected void onPostExecute(ResponseDTO v) {

            if (v.getPhotoUploadList() == null) {
                v.setPhotoUploadList(new ArrayList<PhotoUploadDTO>());
            }
            v.getPhotoUploadList().add(photoUpload);
            response = v;
            new CacheTask().execute();

        }
    }
}

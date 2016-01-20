package com.boha.monitor.library.services;

import android.app.IntentService;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.boha.monitor.library.dto.PhotoUploadDTO;
import com.boha.monitor.library.dto.ResponseDTO;
import com.boha.monitor.library.util.CDNUploader;
import com.boha.monitor.library.util.PhotoCacheUtil;
import com.boha.monitor.library.util.Util;
import com.boha.monitor.library.util.WebCheck;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages the uploading of photos from a list held in cache. Uploads each photo
 * and notifies any activity bound to it on completion. Otherwise these cached photos
 * are uploaded in a silent process not visible to the user.
 * <p/>
 * It may be started by a startService call or may be bound to an activity via the
 * IBinder interface.
 * <p/>
 * Entry points: onHandleIntent, uploadCachedVideos
 */
public class PhotoUploadService extends IntentService {

    public PhotoUploadService() {
        super("PhotoUploadService");
    }

    public interface UploadListener {
        public void onUploadsComplete(List<PhotoUploadDTO> list);
    }

    UploadListener uploadListener;
    public static final String JSON_PHOTO = "photos.json";

    public void uploadCachedPhotos(UploadListener listener) {
        uploadListener = listener;
        Log.d(LOG, "#### uploadCachedPhotos, getting cached photos - will start uploads if wifi is up");
        if (WebCheck.checkNetworkAvailability(getApplicationContext()).isNetworkUnavailable()) {
            Log.e(LOG, "--- No Network: boolean = isNetworkUnavailable");
            return;
        }

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                ResponseDTO response = new ResponseDTO();
                response.setPhotoUploadList(new ArrayList<PhotoUploadDTO>());
                FileInputStream stream;
                try {
                    stream = getApplicationContext().openFileInput(JSON_PHOTO);
                    response = Util.getResponseData(stream);
                    list = response.getPhotoUploadList();

                    if (list.isEmpty()) {
                        Log.w(LOG, "--- no cached photos for upload");
                        if (uploadListener != null)
                            uploadListener.onUploadsComplete(new ArrayList<PhotoUploadDTO>());
                        return;
                    }
                    getLog(response);
                    int pending = 0;
                    for (PhotoUploadDTO x: list) {
                        if (x.getDateUploaded() == null) {
                            pending++;
                        }
                    }
                    if (pending == 0) {
                        if (uploadListener != null)
                            uploadListener.onUploadsComplete(new ArrayList<PhotoUploadDTO>());
                        return;
                    } else {
                        Log.e(LOG,"### ...pending photo uploads: " + pending);
                    }
                    index = 0;
                    controlUploads();
                } catch (FileNotFoundException e) {
                    Log.w(LOG, "############# photo cache file not found. possibly virgin trip");

                } catch (IOException e) {
                    Log.e(LOG, "Failed", e);
                }
            }
        });
        thread.start();


    }

    private static void getLog(ResponseDTO cache) {
        StringBuilder sb = new StringBuilder();
        sb.append("## Photos currently in the cache: ")
                .append(cache.getPhotoUploadList().size()).append(" - ");
        int up = 0, not = 0;
        for (PhotoUploadDTO p : cache.getPhotoUploadList()) {
            if (p.getDateUploaded() != null)
                up++;
            else
                not++;

        }
        sb.append("photos uploaded: " + up + " pending: " + not);
        Log.i(LOG, sb.toString());
    }


    List<PhotoUploadDTO> uploadedList = new ArrayList<>();


    @Override
    protected void onHandleIntent(Intent intent) {
        Log.w(LOG, "## PhotoUploadService onHandleIntent .... starting service");

        if (intent != null) {
            PhotoCacheUtil.getCachedPhotos(getApplicationContext(), new PhotoCacheUtil.PhotoCacheListener() {
                @Override
                public void onFileDataDeserialized(ResponseDTO response) {
                    uploadedList = new ArrayList<>();
                    list = response.getPhotoUploadList();
                    index = 0;
                    controlUploads();

                }

                @Override
                public void onDataCached(PhotoUploadDTO photo) {

                }

                @Override
                public void onError() {

                }
            });
        }



    }

    static List<PhotoUploadDTO> list;
    int index;

    private void controlUploads() {
        if (index < list.size()) {
            if (list.get(index).getDateUploaded() == null) {
                executeUpload(list.get(index));
            } else {
                index++;
                controlUploads();
            }

        } else {
            if (uploadListener != null) {
                uploadListener.onUploadsComplete(uploadedList);
            }
        }

    }


    private void executeUpload(final PhotoUploadDTO dto) {
//        Log.d(LOG, "** executeUpload, projectID: " + dto.getProjectID());

        CDNUploader.uploadFile(getApplicationContext(), dto, new CDNUploader.CDNUploaderListener() {
            @Override
            public void onFileUploaded(PhotoUploadDTO photo) {
                uploadedList.add(dto);
                PhotoCacheUtil.updateUploadedPhoto(getApplicationContext(), dto);
                index++;
                controlUploads();
            }

            @Override
            public void onError(String message) {
                Log.e(LOG, message);
                failedUploads.add(dto);
                index++;
                controlUploads();
            }
        });

    }


    List<PhotoUploadDTO> failedUploads = new ArrayList<>();
    static final String LOG = PhotoUploadService.class.getSimpleName();
    public class LocalBinder extends Binder {

        public PhotoUploadService getService() {
            return PhotoUploadService.this;
        }

    }


    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    private final IBinder mBinder = new LocalBinder();

}

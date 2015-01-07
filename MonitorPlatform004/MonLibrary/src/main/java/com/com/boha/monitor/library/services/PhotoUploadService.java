package com.com.boha.monitor.library.services;

import android.app.IntentService;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.com.boha.monitor.library.dto.transfer.PhotoUploadDTO;
import com.com.boha.monitor.library.dto.transfer.ResponseDTO;
import com.com.boha.monitor.library.util.PhotoCacheUtil;
import com.com.boha.monitor.library.util.PictureUtil;
import com.com.boha.monitor.library.util.Util;
import com.com.boha.monitor.library.util.WebCheck;
import com.com.boha.monitor.library.util.WebCheckResult;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Manages the uploading of photos from a list held in cache. Uploads each photo
 * and notifies any activity bound to it on completion. Otherwise these cached photos
 * are uploaded in a silent process not visible to the user.
 * <p/>
 * It may be started by a startService call or may be bound to an activity via the
 * IBinder interface.
 * <p/>
 * Entry points: onHandleIntent, uploadCachedPhotos
 */
public class PhotoUploadService extends IntentService {

    public PhotoUploadService() {
        super("PhotoUploadService");
    }

    public interface UploadListener {
        public void onUploadsComplete(int count);
    }

    UploadListener uploadListener;
    int count;

    public void uploadCachedPhotos(UploadListener listener) {
        uploadListener = listener;
        Log.d(LOG, "#### uploadCachedPhotos, getting cached photos - will start uploads if wifi is up");
        PhotoCacheUtil.getCachedPhotos(getApplicationContext(), new PhotoCacheUtil.PhotoCacheListener() {
            @Override
            public void onFileDataDeserialized(ResponseDTO response) {
                Log.e(LOG, "##### cached photo list returned: " + response.getPhotoUploadList().size());
                list = response.getPhotoUploadList();
                if (list.isEmpty()) {
                    Log.w(LOG, "--- no cached photos for download");
                    if (uploadListener != null)
                        uploadListener.onUploadsComplete(0);
                    return;
                }
                getLog(response);
                webCheckResult = WebCheck.checkNetworkAvailability(getApplicationContext());
                if (!webCheckResult.isWifiConnected()) {
                    Log.e(LOG, "--- uploadCachedPhotos exiting. no wifi connected");
                    return;
                }
                onHandleIntent(null);
            }

            @Override
            public void onDataCached() {

            }

            @Override
            public void onError() {

            }
        });
    }

    private static void getLog(ResponseDTO cache) {
        StringBuilder sb = new StringBuilder();
        sb.append("## Photos currently in the cache: ")
                .append(cache.getPhotoUploadList().size()).append("\n");
        for (PhotoUploadDTO p : cache.getPhotoUploadList()) {
            sb.append("+++ ").append(p.getDateTaken().toString()).append(" lat: ").append(p.getLatitude());
            sb.append(" lng: ").append(p.getLatitude()).append(" acc: ").append(p.getAccuracy());
            if (p.getDateThumbUploaded() != null)
                sb.append(" ").append(sdf.format(p.getDateThumbUploaded())).append("\n");
            else
                sb.append(" NOT UPLOADED\n");

        }
        Log.w(LOG, sb.toString());
    }

    List<PhotoUploadDTO> uploadedList = new ArrayList<>();

    static final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.w(LOG, "## onHandleIntent");
        if (list == null) {
            uploadCachedPhotos(uploadListener);
            return;
        }
        retryCount = 0;
        webCheckResult = WebCheck.checkNetworkAvailability(getApplicationContext());
        if (webCheckResult.isWifiConnected()) {
            controlThumbUploads();
        }


    }

    static List<PhotoUploadDTO> list;
    int index;
    WebCheckResult webCheckResult;

    private void controlThumbUploads() {
        if (index < list.size()) {
            if (list.get(index).getDateThumbUploaded() == null) {
                executeThumbUpload(list.get(index));
            } else {
                index++;
                controlThumbUploads();
                return;
            }
        }
        if (index == list.size()) {
            if (failedUploads.isEmpty()) {
                Log.w(LOG, "&&& cleaning cache right up!");
                PhotoCacheUtil.clearCache(getApplicationContext(), uploadedList);
                if (uploadListener != null)
                    uploadListener.onUploadsComplete(uploadedList.size());
            } else {
                attemptFailedUploads();
            }
        }
    }

    static final int MAX_RETRIES = 3;
    int retryCount;
    private void attemptFailedUploads() {

        retryCount++;
        if (retryCount > MAX_RETRIES) {
            if (uploadListener != null) {
                uploadListener.onUploadsComplete(uploadedList.size());
            }
            return;
        }
        index = 0;
        list = failedUploads;
        failedUploads.clear();
        controlThumbUploads();

    }
    public int getIndex() {
        return index;
    }

    private void executeThumbUpload(final PhotoUploadDTO dto) {
        Log.d(LOG, "** executeThumbUpload, projectSiteID: " + dto.getProjectSiteID());
        dto.setFullPicture(false);
        if (dto.getPictureType() == 0) dto.setPictureType(PhotoUploadDTO.PROJECT_IMAGE);
        final long start = System.currentTimeMillis();
        PictureUtil.uploadImage(dto, false, getApplicationContext(), new PhotoUploadDTO.PhotoUploadedListener() {
            @Override
            public void onPhotoUploaded() {
                long end = System.currentTimeMillis();
                Log.i(LOG, "---- thumbnail uploaded, elapsed: " + Util.getElapsed(start, end) + " seconds");
                dto.setDateThumbUploaded(new Date());
                uploadedList.add(dto);

                index++;
                controlThumbUploads();
            }

            @Override
            public void onPhotoUploadFailed() {
                Log.e(LOG, "------<< onPhotoUploadFailed - check and tell someone");
                failedUploads.add(dto);
                index++;
                controlThumbUploads();
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

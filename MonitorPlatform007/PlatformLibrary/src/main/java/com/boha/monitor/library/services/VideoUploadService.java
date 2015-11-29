package com.boha.monitor.library.services;

import android.app.IntentService;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.boha.monitor.library.dto.ResponseDTO;
import com.boha.monitor.library.dto.VideoUploadDTO;
import com.boha.monitor.library.util.CDNVideoUploader;
import com.boha.monitor.library.util.CacheVideoUtil;
import com.boha.monitor.library.util.WebCheck;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages the uploading of videos from a list held in cache. Uploads each photo
 * and notifies any activity bound to it on completion. Otherwise these cached photos
 * are uploaded in a silent process not visible to the user.
 * <p/>
 * It may be started by a startService call or may be bound to an activity via the
 * IBinder interface.
 * <p/>
 * Entry points: onHandleIntent, uploadCachedVideos
 */
public class VideoUploadService extends IntentService {

    public VideoUploadService() {
        super("VideoUploadService");
    }

    public interface UploadListener {
        void onUploadsComplete(List<VideoUploadDTO> list);
        void onUploadStarted();
    }

    UploadListener uploadListener;

    public void uploadCachedVideos(UploadListener listener) {
        uploadListener = listener;
        Log.d(LOG, "#### uploadCachedVideos, getting cached videos - will start uploads if wifi is up");
        if (!WebCheck.checkNetworkAvailability(getApplicationContext()).isWifiConnected()) {
            Log.e(LOG, "--- No WIFI Network: no video upload allowed");
            return;
        }

        CacheVideoUtil.getCachedVideoList(getApplicationContext(), new CacheVideoUtil.CacheVideoListener() {
            @Override
            public void onDataDeserialized(ResponseDTO response) {
                list = response.getVideoUploadList();
                if (list.isEmpty()) {
                    Log.w(LOG, "--- no cached videos for upload");
                    if (uploadListener != null)
                        uploadListener.onUploadsComplete(new ArrayList<VideoUploadDTO>());
                    return;
                }
                getLog(response);
                int pending = 0;
                for (VideoUploadDTO x : list) {
                    if (x.getDateUploaded() == null) {
                        pending++;
                    }
                }
                if (pending == 0) {
                    if (uploadListener != null)
                        uploadListener.onUploadsComplete(new ArrayList<VideoUploadDTO>());
                    return;
                } else {
                    Log.e(LOG, "### ...pending video uploads: " + pending);
                }

                onHandleIntent(null);
            }

            @Override
            public void onError(String message) {

            }

            @Override
            public void onDataCached() {

            }
        });


    }

    private static void getLog(ResponseDTO cache) {
        StringBuilder sb = new StringBuilder();
        sb.append("## Videos currently in the cache: ")
                .append(cache.getVideoUploadList().size()).append(" - ");
        int up = 0, not = 0;
        for (VideoUploadDTO p : cache.getVideoUploadList()) {
            if (p.getDateUploaded() != null)
                up++;
            else
                not++;

        }
        sb.append("videos uploaded: " + up + " pending: " + not);
        Log.i(LOG, sb.toString());
    }


    List<VideoUploadDTO> uploadedList = new ArrayList<>();


    @Override
    protected void onHandleIntent(Intent intent) {
        Log.w(LOG, "## onHandleIntent .... starting service");
        if (list == null) {
            uploadCachedVideos(uploadListener);
            return;
        }
        failedUploads = new ArrayList<>();
        uploadedList = new ArrayList<>();
        controlUploads();


    }

    static List<VideoUploadDTO> list;
    int index;

    private void controlUploads() {
        if (index < list.size()) {
            if (list.get(index).getDateUploaded() == null) {
                executeVideoUpload(list.get(index));
            } else {
                index++;
                controlUploads();
            }

        } else {
            Log.d(LOG, "Failed uploads: " + failedUploads.size());
            if (uploadListener != null) {
                uploadListener.onUploadsComplete(uploadedList);
            }
        }

    }


    private void executeVideoUpload(final VideoUploadDTO dto) {
        Log.d(LOG, "** executeVideoUpload, projectID: " + dto.getProjectID());

        if (WebCheck.checkNetworkAvailability(getApplicationContext()).isNetworkUnavailable()) {
            Log.w(LOG,"Network is not available");
            return;
        }

        uploadListener.onUploadStarted();
        CDNVideoUploader.uploadVideoFile(getApplicationContext(), dto, new CDNVideoUploader.CDNVideoUploaderListener() {
            @Override
            public void onFileUploaded(VideoUploadDTO video) {
                uploadedList.add(dto);
                CacheVideoUtil.removeUploadedVideo(getApplicationContext(),video,null);
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


    List<VideoUploadDTO> failedUploads = new ArrayList<>();
    static final String LOG = VideoUploadService.class.getSimpleName();
    public class LocalBinder extends Binder {

        public VideoUploadService getService() {
            return VideoUploadService.this;
        }

    }


    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    private final IBinder mBinder = new LocalBinder();

}

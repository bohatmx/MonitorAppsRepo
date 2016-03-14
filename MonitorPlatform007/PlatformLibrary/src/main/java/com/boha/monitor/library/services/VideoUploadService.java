package com.boha.monitor.library.services;

import android.app.IntentService;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.boha.monitor.library.activities.MonApp;
import com.boha.monitor.library.dto.RequestDTO;
import com.boha.monitor.library.dto.ResponseDTO;
import com.boha.monitor.library.dto.VideoUploadDTO;
import com.boha.monitor.library.util.CDNVideoUploader;
import com.boha.monitor.library.util.MonLog;
import com.boha.monitor.library.util.OKHttpException;
import com.boha.monitor.library.util.OKUtil;
import com.boha.monitor.library.util.Snappy;
import com.boha.monitor.library.util.WebCheck;
import com.google.android.gms.common.Scopes;
import com.google.api.services.youtube.YouTubeScopes;

import java.util.ArrayList;
import java.util.Date;
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
    private List<VideoUploadDTO> uploadedList = new ArrayList<>();

    public static final String BROADCAST_AUTH_TOKEN_MISSING = "com.boha.AUTH_TOKEN_MISSING",
            BROADCAST_VIDEO_UPLOADED = "com.boha.VIDEO.UPLOADED";
    public static final String[] SCOPES = {Scopes.PROFILE, YouTubeScopes.YOUTUBE};
    private static List<VideoUploadDTO> list;
    private int index;
    private List<VideoUploadDTO> failedUploads = new ArrayList<>();
    static final String LOG = VideoUploadService.class.getSimpleName();


    @Override
    protected void onHandleIntent(Intent intent) {
        Log.w(LOG, "## onHandleIntent .... starting service, intent: " + intent);


        Snappy.getVideoList((MonApp) getApplication(), new Snappy.VideoListener() {
            @Override
            public void onVideoAdded() {}

            @Override
            public void onVideoDeleted() {}

            @Override
            public void onVideosListed(List<VideoUploadDTO> vList) {
                list = vList;
                failedUploads = new ArrayList<>();
                uploadedList = new ArrayList<>();
                index = 0;
                if (!list.isEmpty()) {
                    controlUploads();
                } else {
                    MonLog.d(getApplicationContext(),LOG,"No videos to upload, quittin");
                }
            }

            @Override
            public void onError() {
                MonLog.e(getApplicationContext(),LOG,"Snappy went BAD");
            }
        });


    }


    private void controlUploads() {
        if (index < list.size()) {
            if (list.get(index).getDateUploaded() == null) {
                executeVideoUpload(list.get(index));
            } else {
                index++;
                controlUploads();
            }

        } else {
            Log.d(LOG, "Video uploading complete. Failed uploads: " + failedUploads.size());
            Intent m = new Intent(BROADCAST_VIDEO_UPLOADED);
            LocalBroadcastManager.getInstance(getApplicationContext())
                    .sendBroadcast(m);

        }

    }


    private void executeVideoUpload(final VideoUploadDTO dto) {
        Log.d(LOG, "** executeVideoUpload, projectID: " + dto.getProjectID());

        if (WebCheck.checkNetworkAvailability(getApplicationContext()).isNetworkUnavailable()) {
            Log.w(LOG,"Network is not available...exitting");
            return;
        }

        CDNVideoUploader.uploadVideoFile(getApplicationContext(), dto, new CDNVideoUploader.CDNVideoUploaderListener() {
            @Override
            public void onFileUploaded(VideoUploadDTO video) {
                uploadedList.add(dto);
                Snappy.deleteVideo((MonApp)getApplication(),video, null);
                sendVideoMetadata(video);
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







    private void sendVideoMetadata(final VideoUploadDTO videoUpload) {
        RequestDTO w = new RequestDTO(RequestDTO.ADD_VIDEO);
        videoUpload.setDateUploaded(new Date().getTime());
        w.setVideoUpload(videoUpload);
        w.setZipResponse(false);
        OKUtil okUtil = new OKUtil();
        try {
            okUtil.sendGETRequest(getApplicationContext(), w, new OKUtil.OKListener() {
                @Override
                public void onResponse(ResponseDTO response) {
                    Snappy.deleteVideo((MonApp)getApplication(),videoUpload,null);

                }

                @Override
                public void onError(String message) {

                }
            });
        } catch (OKHttpException e) {
            e.printStackTrace();
        }
    }
}

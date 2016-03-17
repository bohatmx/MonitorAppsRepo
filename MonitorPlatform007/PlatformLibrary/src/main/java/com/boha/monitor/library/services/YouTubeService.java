package com.boha.monitor.library.services;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.app.IntentService;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.boha.monitor.library.activities.MonApp;
import com.boha.monitor.library.dto.RequestDTO;
import com.boha.monitor.library.dto.ResponseDTO;
import com.boha.monitor.library.dto.VideoUploadDTO;
import com.boha.monitor.library.util.MonLog;
import com.boha.monitor.library.util.OKUtil;
import com.boha.monitor.library.util.SharedUtil;
import com.boha.monitor.library.util.Snappy;
import com.boha.monitor.library.util.Util;
import com.boha.monitor.library.util.WebCheck;
import com.boha.monitor.library.util.bean.VideoFileException;
import com.boha.platform.library.R;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.googleapis.media.MediaHttpUploader;
import com.google.api.client.googleapis.media.MediaHttpUploaderProgressListener;
import com.google.api.client.http.InputStreamContent;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.Video;
import com.google.api.services.youtube.model.VideoSnippet;
import com.google.api.services.youtube.model.VideoStatus;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class YouTubeService extends IntentService {

    public YouTubeService() {
        super("YouTubeService");
    }

    String authToken;
    YouTube youtube;
    private NotificationManager mNotifyManager;
    private NotificationCompat.Builder mBuilder;
    int id = 1;

    static final String LOG = YouTubeService.class.getSimpleName();
    public static final String BROADCAST_VIDEO_UPLOADED =
            "com.boha.VIDEO_UPLOADED",
            BROADCAST_VIDEO_UPLOADED_BYTES =
                    "com.boha.VIDEO_UPLOADED_BYTES";

    @Override
    protected void onHandleIntent(Intent intent) {
        MonLog.w(getApplicationContext(), LOG, "########## ################# " +
                "onHandleIntent");
        if (!WebCheck.checkNetworkAvailability(getApplicationContext()).isWifiConnected()) {
            Log.e(LOG, "WIFI not connected, quittin YouTube video upload................");
            return;
        }

        authToken = SharedUtil.getAuthToken(getApplicationContext());
        if (authToken == null) {
            Log.e(LOG, "------ authToken is null. requesting token");
            chooseAccount();
            return;
        }
        try {
            if (intent != null) {
                VideoUploadDTO v = (VideoUploadDTO) intent.getSerializableExtra("video");
                if (v != null) {
                    uploadToYouTube(v, new File(v.getFilePath()));
                } else {
                    getVideoList();
                }
            } else {
                getVideoList();
            }
        } catch (Exception e) {
            Log.e(LOG, "Things fall apart with YouTube uploading", e);
        }
    }

    private GoogleCredential credential;

    private void getVideoList() {
        Snappy.getVideoList((MonApp) getApplication(), new Snappy.VideoListener() {
            @Override
            public void onVideoAdded() {
            }

            @Override
            public void onVideoDeleted() {
            }

            @Override
            public void onVideosListed(List<VideoUploadDTO> vList) {
                Log.d(LOG, "... back from Snappy, list: " + vList.size());
                if (!vList.isEmpty()) {
                    index = 0;
                    list = vList;
                    Log.d(LOG, "##### ------ getCreds for YouTube, then start controlUploads");
                    try {
                        credential = new GoogleCredential().setAccessToken(authToken);
                        youtube = new YouTube.Builder(new NetHttpTransport(),
                                new JacksonFactory(), credential)
                                .setApplicationName("com.boha.Monitor.App").build();

                        controlUploads();
                    } catch (Exception e) {
                        Log.e(LOG, "Failed to get YouTube creds", e);
                        //ACRA.getErrorReporter().handleSilentException(e);
                    }

                } else {
                    Log.d(LOG, "------ No videos to upload, quittin");

                }
            }

            @Override
            public void onError() {
                Log.e(LOG, "this service is fucking falling down");
            }
        });
    }

    List<VideoUploadDTO> list = new ArrayList<>();
    int index;

    private void controlUploads() {
        Log.d(LOG, "controlUploads ... index: " + index);
        if (index < list.size()) {
            try {
                uploadToYouTube(list.get(index),new File(list.get(0).getFilePath()));
            } catch (VideoFileException e) {
                //ACRA.getErrorReporter().handleSilentException(e);
            }
        } else {
            Log.w(LOG, "++++++ video upload(s) completed ...Yebo Gogo!!!");
        }
    }


    private static final String VIDEO_FILE_FORMAT = "video/*";
    private static final SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy HH:mm");

    private void uploadToYouTube(final VideoUploadDTO videoUpload, File file) throws VideoFileException {
        if (file == null) {
            return;
        }
        MonLog.d(getApplicationContext(), LOG, "++++++++++++++++ uploadToYouTube\nstarting video upload: "
                + videoUpload.getProjectName() + "\nfile path: " + videoUpload.getFilePath()
                + "\nlocalUri: " + videoUpload.getLocalUri());

        try {
            final Video video = new Video();
            VideoStatus status = new VideoStatus();
            status.setPrivacyStatus("public");
            video.setStatus(status);
            VideoSnippet snippet = new VideoSnippet();

            snippet.setTitle(videoUpload.getProjectName() + ": "
                    + sdf.format(new Date(videoUpload.getDateTaken())));
            snippet.setDescription(
                    "Project video clip from the Monitor Platform");

            // Set the keyword tags that you want to associate with the video.
            List<String> tags = new ArrayList<String>();
            tags.add("monitor");
            tags.add("platform");
            tags.add("project");
            snippet.setTags(tags);
            video.setSnippet(snippet);

            if (file == null) {
                throw new VideoFileException("File not found on disk, wtf?");
            }
            if (!file.exists()) {
                Log.e(LOG, "File does not exist");
                throw new VideoFileException("File does not exist, wtf?");
            } else {
                MonLog.d(getApplicationContext(), LOG,
                        "++++ Video file length: " + getLength(file.length()));
            }
            if (file.length() == 0) {
                Log.e(LOG, "$$$$ Video file length is ZERO. we are royally fucked!");
                throw new VideoFileException("Video file length is 0");
            }

            FileInputStream fis = new FileInputStream(file);
            InputStream is = new BufferedInputStream(fis);
            InputStreamContent mediaContent = new InputStreamContent(VIDEO_FILE_FORMAT, is);

            YouTube.Videos.Insert videoInsert = youtube.videos()
                    .insert("snippet,statistics,status", video, mediaContent);

            MediaHttpUploader uploader = videoInsert.getMediaHttpUploader();
            uploader.setDirectUploadEnabled(false);


            MediaHttpUploaderProgressListener progressListener = new MediaHttpUploaderProgressListener() {
                public void progressChanged(MediaHttpUploader uploader) throws IOException {
                    switch (uploader.getUploadState()) {
                        case INITIATION_STARTED:
                            Log.w(LOG, "******** Video upload Initiation Started ............");
                            break;
                        case INITIATION_COMPLETE:
                            Log.d(LOG, "******** Video upload Initiation Completed. actual upload starting");
                            //notification
                            mNotifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                            mBuilder = new NotificationCompat.Builder(getApplicationContext());
                            mBuilder.setContentTitle(videoUpload.getProjectName())
                                    .setContentText("YouTube Video Upload starting .... ")
                                    .setSmallIcon(R.drawable.glasses);
                            mBuilder.setProgress(100, 0, false);
                            mNotifyManager.notify(1245, mBuilder.build());
                            break;
                        case MEDIA_IN_PROGRESS:
                            Log.i(LOG, "******** Video bytes Uploaded : " + getLength(uploader.getNumBytesUploaded()));
                            Intent m = new Intent(BROADCAST_VIDEO_UPLOADED_BYTES);
                            m.putExtra("bytes", getLength(uploader.getNumBytesUploaded()));
                            m.putExtra("video", videoUpload);
                            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(m);
                            //notification
                            mNotifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                            mBuilder = new NotificationCompat.Builder(getApplicationContext());
                            mBuilder.setContentTitle(videoUpload.getProjectName())
                                    .setContentText("YouTube Video Upload progress, uploaded: " + getLength(uploader.getNumBytesUploaded()))
                                    .setSmallIcon(R.drawable.glasses);
                            File file = new File(videoUpload.getFilePath());
                            mBuilder.setProgress((int) file.length(), (int) uploader.getNumBytesUploaded(), false);
                            mNotifyManager.notify(1245, mBuilder.build());

                            break;
                        case MEDIA_COMPLETE:
                            Log.d(LOG, "******** Video upload - Upload Completed!");
                            //notification
                            mNotifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                            mBuilder = new NotificationCompat.Builder(getApplicationContext());
                            mBuilder.setContentTitle(videoUpload.getProjectName())
                                    .setContentText("YouTube Video Upload completed. You can swipe me off now! ")
                                    .setSmallIcon(R.drawable.glasses);
                            mBuilder.setProgress(100, 100, false);
                            mNotifyManager.notify(1245, mBuilder.build());
                            break;
                        case NOT_STARTED:
                            Log.w(LOG, "--------------------------- Upload Not Started!");
                            break;
                    }
                }
            };
            uploader.setProgressListener(progressListener);
            Bag bag = new Bag();
            bag.insert = videoInsert;
            bag.videoUpload = videoUpload;
            new MTask().execute(bag);


        } catch (GoogleJsonResponseException e) {
            Log.e(LOG, "Failed to upload video - GoogleJsonResponseException", e);
            throw new VideoFileException();
        } catch (IOException e) {
            Log.e(LOG, "Failed to upload video - IOException", e);
            throw new VideoFileException();
        } catch (Throwable t) {
            Log.e(LOG, "Failed to upload video - UNKNOWN ERROR", t);
            throw new VideoFileException();
        }
    }


    private class Bag {
        YouTube.Videos.Insert insert;
        VideoUploadDTO videoUpload;
    }

    private class MTask extends AsyncTask<Bag, Void, VideoUploadDTO> {

        @Override
        protected VideoUploadDTO doInBackground(Bag... params) {
            Bag bag = params[0];
            YouTube.Videos.Insert videoInsert = bag.insert;
            VideoUploadDTO videoUpload = bag.videoUpload;

            long start = System.currentTimeMillis();
            try {
                Video returnedVideo = videoInsert.execute();
                videoUpload.setYouTubeID(returnedVideo.getId());
                long end = System.currentTimeMillis();

                StringBuilder sb = new StringBuilder();
                sb.append("\n================== YouTube Returned Video Data ==================\n");
                sb.append("  - YouTubeId: " + returnedVideo.getId()).append("\n");
                sb.append("  - Title: " + returnedVideo.getSnippet().getTitle()).append("\n");
                sb.append("  - Tags: " + returnedVideo.getSnippet().getTags()).append("\n");
                sb.append("  - Privacy Status: " + returnedVideo.getStatus().getPrivacyStatus()).append("\n");
                sb.append("  - Video Count: " + returnedVideo.getStatistics().getViewCount()).append("\n\n");
                sb.append("  - Elapsed time: " + (end - start) / 1000).append(" seconds\n");
                sb.append("\n============================================================\n");
                Log.w(LOG, sb.toString());

                sendVideoMetadata(videoUpload);
            } catch (Exception e) {
                Log.e(LOG, "Failed video upload", e);
                return null;
            }
            return videoUpload;
        }

        private void sendVideoMetadata(final VideoUploadDTO videoUpload) {
            Log.w(LOG, "@@@@@ sendVideoMetadata to server, youtubeID: " + videoUpload.getYouTubeID());
            videoUpload.setDateUploaded(new Date().getTime());
            RequestDTO w = new RequestDTO(RequestDTO.ADD_VIDEO);
            w.setVideoUpload(videoUpload);
            w.setZipResponse(false);
            OKUtil okUtil = new OKUtil();
            try {
                ResponseDTO resp = okUtil.sendSynchronousGET(getApplicationContext(), w);

                Snappy.addVideo((MonApp) getApplication(), resp.getVideoUploadList().get(0),
                        Snappy.ADD_UPLOADED_VIDEO, new Snappy.VideoListener() {
                            @Override
                            public void onVideoAdded() {
                                MonLog.w(getApplicationContext(), LOG, "Response from server video metadata request, about to remove video from cache");
                                Snappy.deleteVideo((MonApp) getApplication(), videoUpload, null);
                            }

                            @Override
                            public void onVideoDeleted() {

                            }

                            @Override
                            public void onVideosListed(List<VideoUploadDTO> list) {

                            }

                            @Override
                            public void onError() {

                            }
                        });

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        private void waitABit() {
            long count = 0;
            for (long i = 0; i < 990000000; i++) {
                count++;
            }
        }
        @Override
        protected void onPostExecute(VideoUploadDTO videoUpload) {
            if (videoUpload != null) {
                Log.w(LOG, "..... seems like video uploaded OK, youTubeID: " + videoUpload.getYouTubeID());

                Intent m = new Intent(BROADCAST_VIDEO_UPLOADED);
                m.putExtra("video", videoUpload);
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(m);
                if (youTubeListener != null) {
                    youTubeListener.onVideoUploaded();
                }
                //waste some time here ....

                long start = System.currentTimeMillis();
                for (long i = 0; i < 6; i++) {
                    waitABit();
                }
                long end = System.currentTimeMillis();
                Log.i(LOG,"Fake wait took: " + (end-start)/1000 + " seconds");
                index++;
                controlUploads();
            }
        }
    }

    private String getLength(long length) {
        Double d = Double.parseDouble("" + length) / (1024 * 1024);
        return df.format(d) + " MB";

    }

    static final DecimalFormat df = new DecimalFormat("###,###,###,###,##0.00");

    IBinder myBinder = new MyBinder();

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(LOG, ".......... onBind done, YEBO!!");
        return myBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return false;
    }

    public class MyBinder extends Binder {
        public YouTubeService getService() {

            return YouTubeService.this;
        }
    }


    // Methods used by the binding client components

    private YouTubeListener youTubeListener;

    public void uploadVideo(VideoUploadDTO video, File file, YouTubeListener listener) {
        MonLog.w(getApplicationContext(),LOG,"+++ uploadVideo - bound service method called");
        authToken = SharedUtil.getAuthToken(getApplicationContext());
        try {
            youTubeListener = listener;
            credential = new GoogleCredential().setAccessToken(authToken);
            youtube = new YouTube.Builder(new NetHttpTransport(),
                    new JacksonFactory(), credential)
                    .setApplicationName("com.boha.Monitor.App").build();

            uploadToYouTube(video, file);

        } catch (Exception e) {
            listener.onError();
        }
    }

    public interface YouTubeListener {
        void onVideoUploaded();

        void onError();
    }
    AccountManager accountManager;
    Account account;
    private Account chooseAccount() {
        accountManager = AccountManager.get(getApplicationContext());
        Account[] accounts = accountManager.getAccountsByType("com.google");
        if (accounts.length > 0) {
            account = accounts[0];
            MonLog.w(getApplicationContext(), LOG, "##### account to be used: " + account.name);
            requestToken();
            return accounts[0];
        } else {
            Util.showErrorToast(getApplicationContext(), "No Google account found on the device. Cannot continue.");
        }
        return null;
    }

    private void requestToken() {
        accountManager.getAuthToken(account, "oauth2:" + SCOPE, null, true,
                new OnTokenAcquired(), null);
    }

    private final String SCOPE = "https://www.googleapis.com/auth/youtube";
    private class OnTokenAcquired implements AccountManagerCallback<Bundle> {

        @Override
        public void run(AccountManagerFuture<Bundle> result) {
            try {
                Bundle bundle = result.getResult();
                Log.w(LOG, "+++++++++++++++++ Token has been acquired");
                authToken = bundle
                        .getString(AccountManager.KEY_AUTHTOKEN);
                SharedUtil.saveAuthToken(getApplicationContext(), authToken);
                onHandleIntent(null);


            } catch (Exception e) {
                Log.e(LOG,"Failed YT auth",e);
                Util.showErrorToast(getApplicationContext(), "Unable to get YouTube authorisation token");
            }
        }
    }
}

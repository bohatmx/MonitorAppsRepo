package com.boha.monitor.library.activities;

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.boha.monitor.library.dto.ProjectDTO;
import com.boha.monitor.library.dto.ProjectTaskDTO;
import com.boha.monitor.library.dto.VideoUploadDTO;
import com.boha.monitor.library.services.YouTubeService;
import com.boha.monitor.library.util.MonLog;
import com.boha.monitor.library.util.SharedUtil;
import com.boha.monitor.library.util.Snappy;
import com.boha.monitor.library.util.Util;
import com.boha.platform.library.R;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class YouTubeActivity extends Activity {

    private static final int AUTHORIZATION_CODE = 1993;
    private static final int ACCOUNT_CODE = 1601;


    private AccountManager accountManager;
    private Account account;
    private ProjectDTO project;
    private ProjectTaskDTO projectTask;
    private FloatingActionButton fab;
    private static final int REQUEST_VIDEO_CAPTURE = 1098;
    private ImageView image;
    private String authToken;
    private boolean videoTaken;
    private TextView txtTitle;
    private Button btnUpload, btnPlay;
    private View btnLayout;

    private Context ctx;
    private MonApp monApp;
    static final SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy-HH-mm-ss");
    File videoFile;


    static final String LOG = YouTubeActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_you_tube);
        ctx = getApplicationContext();
        monApp = (MonApp) getApplication();

        project = (ProjectDTO) getIntent().getSerializableExtra("project");
        projectTask = (ProjectTaskDTO) getIntent().getSerializableExtra("projectTask");
        //// TODO: 3/12/16 remove
        if (project == null) {
            project = new ProjectDTO();
            project.setProjectID(68);
            project.setProjectName("Testing Infrastructure");
        }
        if (savedInstanceState != null) {
            MonLog.e(getApplicationContext(),LOG,"*** savedInstanceState is not null");
            project = (ProjectDTO)savedInstanceState.getSerializable("project");
            videoUpload = (VideoUploadDTO)savedInstanceState.getSerializable("video");
        }

        authToken = SharedUtil.getAuthToken(getApplicationContext());
        try {
            createVideoFile();
            chooseAccount();
            setFields();
        } catch (IOException e) {
            Log.e(LOG, "Unable to get video file", e);
            Util.showErrorToast(ctx, "Unable to get video file");
            finish();
        }


    }

    @Override
    public void onSaveInstanceState(Bundle b) {
        MonLog.w(getApplicationContext(),LOG,"---- onSaveInstanceState");
        if (videoUpload != null) {
            b.putSerializable("video",videoUpload);
        }
        if (project != null) {
            b.putSerializable("project",project);
        }
        super.onSaveInstanceState(b);
    }
    private void setFields() {
        image = (ImageView) findViewById(R.id.image);
        txtTitle = (TextView)findViewById(R.id.title);
        btnUpload = (Button)findViewById(R.id.btnUpload);
        btnPlay = (Button)findViewById(R.id.btnPlay);

        btnUpload.setVisibility(View.GONE);
        btnPlay.setVisibility(View.GONE);
        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchTakeVideoIntent();
            }
        });

        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.w(LOG, "OnClickListener ... start service ....");
                Snappy.addVideo(monApp, videoUpload, Snappy.ADD_VIDEO_FOR_UPLOAD, new Snappy.VideoListener() {
                    @Override
                    public void onVideoAdded() {
                        Intent m = new Intent(getApplicationContext(), YouTubeService.class);
                        startService(m);
                        btnUpload.setVisibility(View.GONE);
                        btnPlay.setVisibility(View.GONE);
                        fab.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onVideoDeleted() {
                    }

                    @Override
                    public void onVideosListed(List<VideoUploadDTO> list) {
                    }

                    @Override
                    public void onError() {
                        Util.showErrorToast(getApplicationContext(), "Unable to save video");
                    }
                });

            }
        });
        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent m = new Intent(getApplicationContext(),
                        LocalVideoPlayerActivity.class);
                m.putExtra("video", videoUpload);
                startActivity(m);
            }
        });
    }

    private void createVideoFile() throws IOException {
        if (ContextCompat.checkSelfPermission(ctx,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            Log.d(LOG, "WRITE_EXTERNAL_STORAGE permission not granted yet");
            return;
        }

        String imageFileName = ""+project.getProjectID() + "-"+ sdf.format(new Date()) + ".mp4";

        File root;
        if (Util.hasStorage(true)) {
            Log.i(LOG, "###### get file from getExternalStoragePublicDirectory");
            root = Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_PICTURES);
        } else {
            Log.i(LOG, "###### get file from getDataDirectory");
            root = Environment.getDataDirectory();
        }
        File dir = new File(root, "monitor");
        if (!dir.exists()) {
            dir.mkdir();
        }

        videoFile = new File(dir, imageFileName);
        Log.w(LOG, "Video file has been created: " + videoFile.getAbsolutePath());

    }

    private void dispatchTakeVideoIntent() {
        final Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        takeVideoIntent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
        takeVideoIntent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 600);
        takeVideoIntent.putExtra(MediaStore.EXTRA_SIZE_LIMIT, 6000000);
        takeVideoIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(videoFile));

        if (takeVideoIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takeVideoIntent, REQUEST_VIDEO_CAPTURE);
        } else {
            Util.showErrorToast(getApplicationContext(), "No video camera app available");
            finish();
        }
    }

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
        accountManager.getAuthToken(account, "oauth2:" + SCOPE, null, this,
                new OnTokenAcquired(), null);
    }

    private final String SCOPE = "https://www.googleapis.com/auth/youtube";

    GoogleCredential credential;

    private void getCreds() throws Exception {
        Log.d(LOG, "##### getCreds for YouTube");
        credential = new GoogleCredential().setAccessToken(authToken);
        final YouTube youtube = new YouTube.Builder(new NetHttpTransport(),
                new JacksonFactory(), credential)
                .setApplicationName("com.boha.Monitor.App").build();

    }

    private class OnTokenAcquired implements AccountManagerCallback<Bundle> {

        @Override
        public void run(AccountManagerFuture<Bundle> result) {
            try {
                Bundle bundle = result.getResult();
                Intent launch = (Intent) bundle.get(AccountManager.KEY_INTENT);
                if (launch != null) {
                    startActivityForResult(launch, AUTHORIZATION_CODE);
                } else {
                    Log.w(LOG, "+++++++++++++++++ Token has been acquired");
                    authToken = bundle
                            .getString(AccountManager.KEY_AUTHTOKEN);

                    SharedUtil.saveAuthToken(getApplicationContext(), authToken);
                    getCreds();
                }

            } catch (Exception e) {
                Log.e(LOG,"Failed YT auth",e);
                Util.showErrorToast(getApplicationContext(), "A:Unable to get YouTube authorisation token");
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQUEST_VIDEO_CAPTURE:
                if (resultCode == Activity.RESULT_OK) {
                    if (data != null) {
                        Uri uri = data.getData();
                        cacheVideo(uri);
                    } else {
                        Util.showErrorToast(ctx, "Unable to get video file");
                    }
                } else {
                    Log.e(LOG, "---------- Unable to getvideo file from camera");
                    Util.showErrorToast(ctx, "Unable to get video file");

                }
                break;
            case AUTHORIZATION_CODE:
                requestToken();
                break;
            case ACCOUNT_CODE:
                // invalidate old tokens which might be cached. we want a fresh
                // one, which is guaranteed to work
                invalidateToken();
                requestToken();
                break;
        }


    }

    private void invalidateToken() {
        AccountManager accountManager = AccountManager.get(this);
        accountManager.invalidateAuthToken("com.google",
                SharedUtil.getAuthToken(getApplicationContext()));

        SharedUtil.saveAuthToken(getApplicationContext(), null);
    }

    @Override
    public void onBackPressed() {
        MonLog.d(getApplicationContext(), LOG, "onBackPressed");

        if (videoTaken) {
            Intent m = new Intent();
            m.putExtra("videoTaken", videoTaken);
            setResult(RESULT_OK, m);
        } else {
            setResult(RESULT_CANCELED);
        }

        finish();
    }

    VideoUploadDTO videoUpload;

    /**
     * Cache video clip prior to uploading via a service
     */
    private void cacheVideo(Uri uri) {

        Log.e(LOG,"******** the computed path: " + uri.toString());
        String path = uri.toString().substring(5);
        MonLog.e(ctx,LOG,"Prior to caching, videoFile lenghth: " + videoFile.length());
        videoFile = new File(path);
        Log.w(LOG,"videoFile length: " + getLength(videoFile.length()));

//        //// TODO: 3/12/16 test the zipper
//        Zipper.compressFile(ctx, videoFile, new Zipper.ZipperListener() {
//            @Override
//            public void onZipped(File zipfFile) {
//                Log.e(LOG, "@@@@@ done compressing video file");
//            }
//
//            @Override
//            public void onError(String message) {
//
//            }
//        });
        videoUpload = getObject();
        videoUpload.setProjectName(project.getProjectName());
        videoUpload.setFilePath(videoFile.getAbsolutePath());

        videoUpload.setDateTaken(new Date().getTime());
        videoTaken = true;
        btnUpload.setVisibility(View.VISIBLE);
        btnPlay.setVisibility(View.VISIBLE);


    }


    private VideoUploadDTO getObject() {
        VideoUploadDTO dto = new VideoUploadDTO();
        if (SharedUtil.getCompanyStaff(ctx) != null) {
            dto.setStaffID(SharedUtil.getCompanyStaff(ctx).getStaffID());
        }
        if (SharedUtil.getMonitor(ctx) != null) {
            dto.setMonitorID(SharedUtil.getMonitor(ctx).getMonitorID());
        }
        if (project != null) {
            dto.setProjectID(project.getProjectID());
            dto.setProjectName(project.getProjectName());
        }
        if (projectTask != null) {
            dto.setProjectTaskID(projectTask.getProjectTaskID());
            dto.setProjectName(projectTask.getProjectName());
        }
        dto.setDateTaken(new Date().getTime());


        return dto;
    }

    static final DecimalFormat df = new DecimalFormat("###,###,###,###,###0.00");

    private String getLength(long length) {
        Double d = Double.parseDouble("" + length) / (1024 * 1024);
        return df.format(d) + " MB";

    }
}

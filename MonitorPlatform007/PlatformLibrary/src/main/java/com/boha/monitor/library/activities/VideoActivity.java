package com.boha.monitor.library.activities;

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.boha.monitor.library.dto.MonitorDTO;
import com.boha.monitor.library.dto.ProjectDTO;
import com.boha.monitor.library.dto.ProjectTaskDTO;
import com.boha.monitor.library.dto.ProjectTaskStatusDTO;
import com.boha.monitor.library.dto.RequestDTO;
import com.boha.monitor.library.dto.ResponseDTO;
import com.boha.monitor.library.dto.StaffDTO;
import com.boha.monitor.library.dto.VideoUploadDTO;
import com.boha.monitor.library.services.VideoUploadService;
import com.boha.monitor.library.util.MonLog;
import com.boha.monitor.library.util.OKHttpException;
import com.boha.monitor.library.util.OKUtil;
import com.boha.monitor.library.util.PMException;
import com.boha.monitor.library.util.SharedUtil;
import com.boha.monitor.library.util.Snappy;
import com.boha.monitor.library.util.ThemeChooser;
import com.boha.monitor.library.util.Util;
import com.boha.platform.library.R;

import java.io.File;

import java.util.Date;
import java.util.List;

/**
 * Created by aubreyM on 2014/04/21.
 */
public class VideoActivity extends AppCompatActivity  {

    LayoutInflater inflater;
    TextView txtTitle, txtResult;
    MonitorDTO monitor;
    Long localID;
    int themeDarkColor, themePrimaryColor;
    ProjectTaskStatusDTO projectTaskStatus;
    ResponseDTO response;
    ProjectDTO project;
    StaffDTO staff;
    ProjectTaskDTO projectTask;
    static final int REQUEST_VIDEO_CAPTURE = 1098;
    static final String LOG = VideoActivity.class.getSimpleName();
    Menu mMenu;
    int type;
    Context ctx;



    public void onCreate(Bundle savedInstanceState) {
        ThemeChooser.setTheme(this);
        Resources.Theme theme = getTheme();
        TypedValue typedValue = new TypedValue();
        theme.resolveAttribute(R.attr.colorPrimaryDark, typedValue, true);
        themeDarkColor = typedValue.data;
        theme.resolveAttribute(R.attr.colorPrimary, typedValue, true);
        themePrimaryColor = typedValue.data;

        super.onCreate(savedInstanceState);
        Log.d(LOG, "### onCreate............");
        ctx = getApplicationContext();
        ThemeChooser.setTheme(this);
        inflater = getLayoutInflater();
        setContentView(R.layout.activity_video);


        setFields();
        monApp = (MonApp) getApplication();


        //
        type = getIntent().getIntExtra("type", 0);
        project = (ProjectDTO) getIntent().getSerializableExtra("project");
        projectTask = (ProjectTaskDTO) getIntent().getSerializableExtra("projectTask");
        projectTaskStatus = (ProjectTaskStatusDTO) getIntent().getSerializableExtra("projectTaskStatus");



    }


    @Override
    public void onResume() {
        Log.d(LOG, "@@@ onResume...........");
        super.onResume();

    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        Log.e(LOG, "%%%%%%%%%%%% onRestoreInstanceState" + savedInstanceState);
        type = savedInstanceState.getInt("type", 0);
        projectTask = (ProjectTaskDTO) savedInstanceState.getSerializable("projectTask");
        project = (ProjectDTO) savedInstanceState.getSerializable("project");
        monitor = (MonitorDTO) savedInstanceState.getSerializable("monitor");
        staff = (StaffDTO) savedInstanceState.getSerializable("staff");
        response = (ResponseDTO) savedInstanceState.getSerializable("response");

        double lat = savedInstanceState.getDouble("latitude");
        double lng = savedInstanceState.getDouble("longitude");
        float acc = savedInstanceState.getFloat("accuracy");

        super.onRestoreInstanceState(savedInstanceState);
    }


    private void setFields() {
        activity = this;

        txtTitle = (TextView) findViewById(R.id.AV_title);
        txtResult = (TextView) findViewById(R.id.AV_result);
        fab = (FloatingActionButton) findViewById(R.id.fab);
        txtResult.setText("");

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchTakeVideoIntent();
            }
        });

    }

    @Override
    public void onActivityResult(final int requestCode, final int resultCode,
                                 final Intent data) {
        Log.e(LOG, "##### onActivityResult requestCode: " + requestCode + " resultCode: " + resultCode);
        switch (requestCode) {
            case REQUEST_VIDEO_CAPTURE:
                if (resultCode == Activity.RESULT_OK) {
                    Uri videoUri = data.getData();
                    cacheVideo(videoUri);
                } else {
                    Util.showErrorToast(ctx, "Unable to get video file");
                }
                break;
        }
    }


    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();


    }

    @Override
    public void onDestroy() {
        super.onDestroy();

    }

    VideoActivity activity;
    FloatingActionButton fab;

    private void dispatchTakeVideoIntent() {
        final Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);

        if (takeVideoIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takeVideoIntent, REQUEST_VIDEO_CAPTURE);
        } else {
            Util.showErrorToast(getApplicationContext(), "No video camera app available");
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.video, menu);
        mMenu = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return false;
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

    @Override
    public void onSaveInstanceState(Bundle b) {
        Log.e(LOG, "############################## onSaveInstanceState");
        b.putInt("type", type);

        if (projectTask != null) {
            b.putSerializable("projectTask", projectTask);
        }
        if (project != null) {
            b.putSerializable("project", project);
        }
        if (monitor != null) {
            b.putSerializable("monitor", monitor);
        }
        if (staff != null) {
            b.putSerializable("staff", staff);
        }
        if (response != null) {
            b.putSerializable("response", response);
        }


        super.onSaveInstanceState(b);
    }

    boolean videoTaken;

    /**
     * Cache video clip prior to uploading via a service
     *
     * @param uri
     */
    private void cacheVideo(final Uri uri) {
        Log.w(LOG, "**** cacheVideo uri: " + uri.toString());

        final VideoUploadDTO dto = getObject();
        dto.setVideoUri(uri.toString());
        String path = Util.getRealPathFromURI_API11to18(ctx, uri);
        if (path == null) {
            path = Util.getRealPathFromURI_API19(ctx, uri);
        }
        File file = new File(path);
        dto.setFilePath(file.getAbsolutePath());
        Log.d(LOG, "## -------- videoFile: " + file.getAbsolutePath()
                + " length: " + file.length());


        Snappy.addVideo(monApp, dto, Snappy.ADD_VIDEO_FOR_UPLOAD, new Snappy.VideoListener() {
            @Override
            public void onVideoAdded() {
                videoTaken = true;
                Intent m = new Intent(getApplicationContext(), VideoUploadService.class);
                startService(m);
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


    private VideoUploadDTO getObject() {
        VideoUploadDTO dto = new VideoUploadDTO();
        if (SharedUtil.getCompanyStaff(ctx) != null) {
            dto.setStaffID(SharedUtil.getCompanyStaff(ctx).getStaffID());
        }
        if (SharedUtil.getMonitor(ctx) != null) {
            dto.setMonitorID(SharedUtil.getMonitor(ctx).getMonitorID());
        }
        if (project != null) {
            dto.setProjectName(project.getProjectName());
        }
        if (projectTask != null) {
            dto.setProjectName(projectTask.getProjectName());
        }
        dto.setDateTaken(new Date().getTime());

        if (project != null) {
            dto.setProjectID(project.getProjectID());
        }
        if (projectTask != null) {
            dto.setProjectTaskID(projectTask.getProjectTaskID());
        }

        return dto;
    }

    public void setBusy(final boolean refreshing) {
        if (mMenu != null) {
            final MenuItem refreshItem = mMenu.findItem(R.id.action_refresh);
            if (refreshItem != null) {
                if (refreshing) {
                    refreshItem.setActionView(R.layout.action_bar_progess);
                } else {
                    refreshItem.setActionView(null);
                }
            }
        }
    }

    MonApp monApp;


}
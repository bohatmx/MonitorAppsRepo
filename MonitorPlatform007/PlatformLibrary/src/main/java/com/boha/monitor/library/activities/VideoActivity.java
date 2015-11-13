package com.boha.monitor.library.activities;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Resources;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
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
import com.boha.monitor.library.dto.ResponseDTO;
import com.boha.monitor.library.dto.StaffDTO;
import com.boha.monitor.library.dto.VideoUploadDTO;
import com.boha.monitor.library.services.VideoUploadService;
import com.boha.monitor.library.util.CacheVideoUtil;
import com.boha.monitor.library.util.PMException;
import com.boha.monitor.library.util.SharedUtil;
import com.boha.monitor.library.util.ThemeChooser;
import com.boha.monitor.library.util.Util;
import com.boha.platform.library.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import org.acra.ACRA;

import java.io.File;
import java.util.Date;
import java.util.List;

/**
 * Created by aubreyM on 2014/04/21.
 */
public class VideoActivity extends AppCompatActivity implements LocationListener,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    LocationRequest mLocationRequest;
    GoogleApiClient googleApiClient;
    LayoutInflater inflater;
    TextView txtTitle, txtResult;
    MonitorDTO monitor;
    Long localID;
    int themeDarkColor, themePrimaryColor;
    ProjectTaskStatusDTO projectTaskStatus;
    ResponseDTO response;
    boolean mBound;
    VideoUploadService mService;
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
        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        //
        type = getIntent().getIntExtra("type", 0);
        project = (ProjectDTO) getIntent().getSerializableExtra("project");
        projectTask = (ProjectTaskDTO) getIntent().getSerializableExtra("projectTask");
        projectTaskStatus = (ProjectTaskStatusDTO) getIntent().getSerializableExtra("projectTaskStatus");

        //dispatchTakeVideoIntent();
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
        location = new Location(LocationManager.GPS_PROVIDER);
        location.setLatitude(lat);
        location.setLongitude(lng);
        location.setAccuracy(acc);
        super.onRestoreInstanceState(savedInstanceState);
    }

    FloatingActionButton fab;

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
                    addVideo(videoUri);
                } else {
                    //todo message?
                }
                break;
        }
    }

    @Override
    public void onLocationChanged(Location loc) {
        Log.d(LOG, "## onLocationChanged accuracy = " + loc.getAccuracy());
        if (loc.getAccuracy() <= ACCURACY_THRESHOLD) {
            this.location = loc;
            stopLocationUpdates();
        }
    }


    @Override
    public void onStart() {
        Log.i(LOG,
                "## onStart - GoogleApiClient connecting ... ");
        if (googleApiClient != null) {
            googleApiClient.connect();
        }
        Log.i(LOG, "## onStart Bind to VideoUploadService");
        Intent intent = new Intent(this, VideoUploadService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (googleApiClient != null) {
            googleApiClient.disconnect();
            Log.d(LOG, "### onStop - GoogleApiClient disconnecting ");
        }
//        Log.d(LOG, "## onStop unBind from VideoUploadService");
//        if (mBound) {
//            unbindService(mConnection);
//            mBound = false;
//        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
//        if (googleApiClient != null) {
//            googleApiClient.disconnect();
//            Log.d(LOG, "### onStop - GoogleApiClient disconnecting ");
//        }
        Log.d(LOG, "## onStop unBind from VideoUploadService");
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }

    }

    Location location;
    static final float ACCURACY_THRESHOLD = 30;
    VideoActivity activity;
    boolean mRequestingLocationUpdates;

    @Override
    public void onConnected(Bundle bundle) {
        Log.i(LOG,
                "+++  onConnected() -  requestLocationUpdates ...");
        location = LocationServices.FusedLocationApi.getLastLocation(
                googleApiClient);
        Log.w(LOG, "## requesting location updates ....");
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setFastestInterval(500);
        startLocationUpdates();

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    protected void startLocationUpdates() {
        if (googleApiClient.isConnected()) {
            mRequestingLocationUpdates = true;
            LocationServices.FusedLocationApi.requestLocationUpdates(
                    googleApiClient, mLocationRequest, this);
        }
    }

    protected void stopLocationUpdates() {
        if (googleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(
                    googleApiClient, this);
        }
    }


    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        ACRA.getErrorReporter().handleSilentException(new PMException(
                "GoogleApiClient: onConnectionFailed - " + connectionResult.getErrorCode()));
    }


    private void dispatchTakeVideoIntent() {
        //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        final Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);

        if (takeVideoIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takeVideoIntent, REQUEST_VIDEO_CAPTURE);
        } else {
            Util.showErrorToast(getApplicationContext(),"No video camera app available");
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
        if (location != null) {
            b.putDouble("latitude", location.getLatitude());
            b.putDouble("longitude", location.getLongitude());
            b.putFloat("accuracy", location.getAccuracy());
        }

        super.onSaveInstanceState(b);
    }


    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            Log.w(LOG, "## VideoUploadService ServiceConnection onServiceConnected");
            VideoUploadService.LocalBinder binder = (VideoUploadService.LocalBinder) service;
            mService = binder.getService();
            mBound = true;
            mService.uploadCachedVideos(new VideoUploadService.UploadListener() {
                @Override
                public void onUploadsComplete(List<VideoUploadDTO> list) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            setBusy(false);
                        }
                    });

                    Log.w(LOG, "$$$ onUploadsComplete, list: " + list.size());
                }

                @Override
                public void onUploadStarted() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            setBusy(true);
                        }
                    });
                }
            });
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            Log.w(LOG, "## VideUploadService onServiceDisconnected");
            mBound = false;
        }
    };

    /**
     * Cache video clip prior to uploading via a service
     *
     * @param uri
     */
    private void addVideo(final Uri uri) {
        Log.w(LOG, "**** addVideo");

        final VideoUploadDTO dto = getObject();
        dto.setVideoUri(uri.toString());
        String path = Util.getRealPathFromURI_API11to18(ctx, uri);
        if (path == null) {
            path = Util.getRealPathFromURI_API19(ctx,uri);
        }
        File file = new File(path);
        dto.setFilePath(file.getAbsolutePath());
        Log.d(LOG,"## videoFile: " + file.getAbsolutePath()
        + " length: " + file.length());


        CacheVideoUtil.addVideo(getApplicationContext(), dto, new CacheVideoUtil.CacheVideoListener() {
            @Override
            public void onDataDeserialized(ResponseDTO response) {

            }

            @Override
            public void onError(String message) {

            }

            @Override
            public void onDataCached() {
                if (mBound) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            setBusy(true);
                        }
                    });
                    mService.uploadCachedVideos(new VideoUploadService.UploadListener() {
                        @Override
                        public void onUploadsComplete(List<VideoUploadDTO> list) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    setBusy(false);
                                }
                            });
                            Log.e(LOG, "##### videos uploaded: " + list.size());
                        }

                        @Override
                        public void onUploadStarted() {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    setBusy(true);
                                }
                            });
                        }
                    });
                }

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

        dto.setDateTaken(new Date().getTime());
        dto.setLatitude(location.getLatitude());
        dto.setLongitude(location.getLongitude());
        dto.setAccuracy(location.getAccuracy());
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


}
package com.boha.monitor.library.activities;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.boha.monitor.library.dto.PhotoUploadDTO;
import com.boha.monitor.library.dto.ProjectDTO;
import com.boha.monitor.library.dto.ProjectTaskDTO;
import com.boha.monitor.library.dto.ProjectTaskStatusDTO;
import com.boha.monitor.library.dto.ResponseDTO;
import com.boha.monitor.library.fragments.TaskStatusUpdateFragment;
import com.boha.monitor.library.services.RequestSyncService;
import com.boha.monitor.library.util.ThemeChooser;
import com.boha.platform.library.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TaskUpdateActivity extends AppCompatActivity
        implements TaskStatusUpdateFragment.TaskStatusUpdateListener,
        LocationListener,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    GoogleMap googleMap;
    GoogleApiClient mGoogleApiClient;
    LocationRequest mLocationRequest;
    Context ctx;
    TaskStatusUpdateFragment taskStatusUpdateFragment;
    View layout;
    ProjectDTO project;
    List<ProjectTaskStatusDTO> projectTaskStatusList = new ArrayList<>();

    static final String LOG = TaskUpdateActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ThemeChooser.setTheme(this);
        setContentView(R.layout.activity_task_update);
        layout = findViewById(R.id.layout);
        ctx = getApplicationContext();
        project = (ProjectDTO) getIntent().getSerializableExtra("project");
        ProjectTaskDTO task = (ProjectTaskDTO) getIntent().getSerializableExtra("projectTask");
        taskStatusUpdateFragment = (TaskStatusUpdateFragment) getSupportFragmentManager().findFragmentById(R.id.fragment);
        taskStatusUpdateFragment.setProjectTask(task);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        android.support.v7.app.ActionBar bar = getSupportActionBar();
        bar.setTitle(project.getProjectName());

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_task_update, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public static final int DISTANCE_MINIMUM_METRES = 1000;

    @Override
    public void onCameraRequested(final ProjectTaskDTO projectTask) {
        Log.e("TaskUpdateActivity", "onCameraRequested, projectTask coming in ");
        //todo Check that the device is near the project location

        if (project.getLatitude() != null) {
            Location x = new Location(LocationManager.GPS_PROVIDER);
            x.setLatitude(projectTask.getLatitude());
            x.setLongitude(projectTask.getLongitude());
            float dist = mLocation.distanceTo(x);
            Log.e(LOG, "#### distance from project: " + dist);

            if (dist > DISTANCE_MINIMUM_METRES) {
                AlertDialog.Builder diag = new AlertDialog.Builder(this);
                diag.setTitle("Project Task Photo")
                        .setMessage("You may not be at the site of the project. The project site is " + df.format(getKM(dist)) + " kilometres away.\n\nDo you still want to add a photo for this project?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                startTaskPictureActivity(projectTask);
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                return;
                            }
                        })
                        .show();

            }
        } else {
            AlertDialog.Builder diag = new AlertDialog.Builder(this);
            diag.setTitle("Project Task Photo")
                    .setMessage("The project has no location data. Do you want to take a Project picture?")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            startProjectPictureActivity();
                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            return;
                        }
                    })
                    .show();
        }

    }

    @Override
    public void onStatusReturned(ProjectTaskStatusDTO projectTaskStatus) {
        projectTaskStatusList.add(projectTaskStatus);
    }

    @Override
    public void onBackPressed() {
        if (!projectTaskStatusList.isEmpty()) {
            ResponseDTO w = new ResponseDTO();
            w.setProjectTaskStatusList(projectTaskStatusList);
            Intent d = new Intent();
            d.putExtra("projectTaskStatusList", w);
            setResult(RESULT_OK, d);
        }
        finish();
    }

    private void startProjectPictureActivity() {
        Intent w = new Intent(this, PictureActivity.class);
        w.putExtra("type", PhotoUploadDTO.PROJECT_IMAGE);
        w.putExtra("project", project);
        startActivityForResult(w, REQUEST_CAMERA);
        if (mBound) {
            mService.startSyncCachedRequests(new RequestSyncService.RequestSyncListener() {
                @Override
                public void onTasksSynced(int goodResponses, int badResponses) {
                    Log.e(LOG, "+++ onTasksSynced goodResponses: " + goodResponses + " badResponses: " + badResponses);
                    Snackbar.make(layout, "Task updates synced. Good responses: " + goodResponses, Snackbar.LENGTH_SHORT).show();
                }

                @Override
                public void onError(String message) {

                }
            });
        }
    }

    private void startTaskPictureActivity(ProjectTaskDTO projectTask) {
        Intent w = new Intent(this, PictureActivity.class);
        w.putExtra("type", PhotoUploadDTO.TASK_IMAGE);
        w.putExtra("projectTask", projectTask);
        startActivityForResult(w, REQUEST_CAMERA);
        if (mBound) {
            mService.startSyncCachedRequests(new RequestSyncService.RequestSyncListener() {
                @Override
                public void onTasksSynced(int goodResponses, int badResponses) {
                    Log.e(LOG, "+++ onTasksSynced goodResponses: " + goodResponses + " badResponses: " + badResponses);
                    Snackbar.make(layout, "Task updates synced. Good responses: " + goodResponses, Snackbar.LENGTH_SHORT).show();
                }

                @Override
                public void onError(String message) {

                }
            });
        }
    }

    static final DecimalFormat df = new DecimalFormat("###,###,###,##0.0");

    private double getKM(float dist) {
        Double d = Double.parseDouble("" + dist);
        Double e = d / 1000;

        return e.doubleValue();
    }

    static final int REQUEST_CAMERA = 1432;

    @Override
    public void onStart() {
        super.onStart();
        Log.i(LOG, "## onStart Bind to RequestSyncService");
        Intent intent = new Intent(this, RequestSyncService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }

    }

    @Override
    public void onStop() {
        super.onStop();
        Log.e(LOG, "## onStop unBind from RequestSyncService");
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }
        if (mGoogleApiClient != null) {
            mGoogleApiClient.disconnect();
        }

    }

    boolean mBound;
    RequestSyncService mService;


    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            Log.w(LOG, "## RequestSyncService ServiceConnection onServiceConnected");
            RequestSyncService.LocalBinder binder = (RequestSyncService.LocalBinder) service;
            mService = binder.getService();
            mBound = true;
            mService.startSyncCachedRequests(new RequestSyncService.RequestSyncListener() {
                @Override
                public void onTasksSynced(int goodResponses, int badResponses) {
                    Log.e(LOG, "### onTasksSynced, goodResponses: "
                            + goodResponses + " badResponses: " + badResponses);
                }

                @Override
                public void onError(String message) {
                    Log.e(LOG, message);
                }
            });

        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            Log.w(LOG, "## PhotoUploadService onServiceDisconnected");
            mBound = false;
        }
    };


    boolean mRequestingLocationUpdates;

    @Override
    public void onConnected(Bundle bundle) {
        Log.i(LOG,
                "+++  onConnected() -  requestLocationUpdates ...");
        mLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);
        if (mLocation != null) {
            Log.w(LOG, "## requesting location updates ....lastLocation: "
                    + mLocation.getLatitude() + " "
                    + mLocation.getLongitude() + " acc: "
                    + mLocation.getAccuracy());
        }
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setInterval(3000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setFastestInterval(1000);
        startLocationUpdates();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    protected void startLocationUpdates() {
        Log.w(LOG, "###### startLocationUpdates: " + new Date().toString());
        if (mGoogleApiClient.isConnected()) {
            mRequestingLocationUpdates = true;
            LocationServices.FusedLocationApi.requestLocationUpdates(
                    mGoogleApiClient, mLocationRequest, this);
        }
    }

    protected void stopLocationUpdates() {
        Log.w(LOG, "###### stopLocationUpdates - " + new Date().toString());
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(
                    mGoogleApiClient, this);
        }
    }


    @Override
    public void onLocationChanged(Location loc) {
        Log.d(LOG, "## onLocationChanged accuracy = " + loc.getAccuracy()
                + " - " + new Date().toString());

        if (loc.getAccuracy() <= ACCURACY) {
            mLocation = loc;
            stopLocationUpdates();
        }
    }


    static final int ACCURACY = 15;
    Location mLocation;

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }
}

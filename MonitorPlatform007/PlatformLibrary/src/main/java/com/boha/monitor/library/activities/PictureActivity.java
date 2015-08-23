package com.boha.monitor.library.activities;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.location.Location;
import android.location.LocationManager;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RadioButton;
import android.widget.TextView;

import com.boha.monitor.library.adapters.PhotoAdapter;
import com.boha.monitor.library.dto.MonitorDTO;
import com.boha.monitor.library.dto.PhotoUploadDTO;
import com.boha.monitor.library.dto.ProjectDTO;
import com.boha.monitor.library.dto.ProjectTaskDTO;
import com.boha.monitor.library.dto.ResponseDTO;
import com.boha.monitor.library.dto.StaffDTO;
import com.boha.monitor.library.dto.VideoClipDTO;
import com.boha.monitor.library.services.PhotoUploadService;
import com.boha.monitor.library.util.CacheVideoUtil;
import com.boha.monitor.library.util.ImageUtil;
import com.boha.monitor.library.util.PMException;
import com.boha.monitor.library.util.PhotoCacheUtil;
import com.boha.monitor.library.util.SharedUtil;
import com.boha.monitor.library.util.SpacesItemDecoration;
import com.boha.monitor.library.util.ThemeChooser;
import com.boha.monitor.library.util.Util;
import com.boha.monitor.library.util.VideoClipContainer;
import com.boha.platform.library.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import org.acra.ACRA;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Created by aubreyM on 2014/04/21.
 */
public class PictureActivity extends AppCompatActivity implements LocationListener,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    LocationRequest mLocationRequest;
    GoogleApiClient googleApiClient;
    LayoutInflater inflater;
    TextView txtProject, txtTaskName;
    MonitorDTO monitor;
    Long localID;
    int darkColor;
    RadioButton radioPhoto, radioVideo;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(LOG, "### onCreate............");
        ctx = getApplicationContext();
        ThemeChooser.setTheme(this);
        inflater = getLayoutInflater();
        setContentView(R.layout.camera);

        if (savedInstanceState != null) {
            Log.d(LOG, "## savedInstanceState is not null: ");
        }

        darkColor = getIntent().getIntExtra("darkColor", R.color.red_900);
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler);


//        StaggeredGridLayoutManager x = new StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);
        int spacingInPixels = getResources().getDimensionPixelSize(R.dimen.mon_divider_small);
        mRecyclerView.addItemDecoration(new SpacesItemDecoration(spacingInPixels));
        mRecyclerView.setLayoutManager(gridLayoutManager);
        setFields();
        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        //
        type = getIntent().getIntExtra("type", 0);

        switch (type) {
            case PhotoUploadDTO.STAFF_IMAGE:
                staff = (StaffDTO) getIntent().getSerializableExtra("staff");
                if (staff != null) {
                    txtProject.setText(staff.getFirstName() + " " + staff.getLastName());
                    dispatchTakePictureIntent();
                }
                break;
            case PhotoUploadDTO.PROJECT_IMAGE:
                project = (ProjectDTO) getIntent().getSerializableExtra("project");
                if (project != null) {
                    txtProject.setText(project.getProjectName());
                }
                break;
            case PhotoUploadDTO.MONITOR_IMAGE:
                monitor = (MonitorDTO) getIntent().getSerializableExtra("monitor");
                if (monitor != null) {
                    txtProject.setText(monitor.getFirstName() + " " + monitor.getLastName());
                }
                break;
            case PhotoUploadDTO.TASK_IMAGE:
                projectTask = (ProjectTaskDTO) getIntent().getSerializableExtra("projectTask");
                if (projectTask != null) {
                    txtProject.setText(projectTask.getProjectName());
                    txtTaskName.setText(projectTask.getTask().getTaskName());
                }
                break;
            default:
                projectTask = (ProjectTaskDTO) getIntent().getSerializableExtra("projectTask");
                if (projectTask != null) {
                    txtProject.setText(projectTask.getProjectName());
                    txtTaskName.setText(projectTask.getTask().getTaskName());
                    break;
                }
                throw new UnsupportedOperationException("No data passed to activity");
        }


        setTitle(SharedUtil.getCompany(ctx).getCompanyName());
        showCachedPhotos();


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
        String path = savedInstanceState.getString("photoFile");
        if (path != null) {
            photoFile = new File(path);
        }
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
        radioPhoto = (RadioButton) findViewById(R.id.CAM_radioPhoto);
        radioVideo = (RadioButton) findViewById(R.id.CAM_radioVideo);

        txtProject = (TextView) findViewById(R.id.CAM_projectName);
        txtTaskName = (TextView) findViewById(R.id.CAM_siteName);

        txtProject = (TextView) findViewById(R.id.CAM_projectName);
        txtTaskName = (TextView) findViewById(R.id.CAM_siteName);
        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setBackgroundColor(darkColor);
        txtProject.setText("");
        txtTaskName.setText("");


        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (radioPhoto.isChecked()) {
                    dispatchTakePictureIntent();
                }
                if (radioVideo.isChecked()) {
                    dispatchTakeVideoIntent();
                }
            }
        });

    }

    @Override
    public void onActivityResult(final int requestCode, final int resultCode,
                                 final Intent data) {
        Log.e(LOG, "##### onActivityResult requestCode: " + requestCode + " resultCode: " + resultCode);
        //
        switch (requestCode) {
            case CAPTURE_IMAGE:
                if (resultCode == Activity.RESULT_OK) {
                    if (resultCode == Activity.RESULT_OK) {
                        if (photoFile != null) {
                            Log.e(LOG, "++ hopefully photo file has a length: " + photoFile.length());

                            new PhotoTask().execute();
                        }
                    }
                    pictureChanged = true;

                }
                break;
            case REQUEST_VIDEO_CAPTURE:
                if (resultCode == Activity.RESULT_OK) {
                    Uri videoUri = data.getData();
                    new FileTask(videoUri);
                    //mVideoView.setVideoURI(videoUri);
                }
                break;
        }
    }

    @Override
    public void onLocationChanged(Location loc) {
        Log.d(LOG, "## onLocationChanged accuracy = " + loc.getAccuracy());

        if (this.location == null) {
            this.location = loc;
        }
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
        Log.i(LOG, "## onStart Bind to PhotoUploadService");
        Intent intent = new Intent(this, PhotoUploadService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();

        if (googleApiClient != null) {
            googleApiClient.disconnect();
            Log.e(LOG, "### onStop - GoogleApiClient disconnecting ");
        }
        Log.e(LOG, "## onStop unBind from PhotoUploadService");
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }

    }

    Location location;
    static final float ACCURACY_THRESHOLD = 15;
    PictureActivity activity;
    boolean mRequestingLocationUpdates;

    @Override
    public void onConnected(Bundle bundle) {
        Log.i(LOG,
                "+++  onConnected() -  requestLocationUpdates ...");
        location = LocationServices.FusedLocationApi.getLastLocation(
                googleApiClient);
        Log.w(LOG, "## requesting location updates ....");
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
                "Google LocationClient onConnectionFailed: " + connectionResult.getErrorCode()));
    }


    class FileTask {
        Uri uri;

        public FileTask(Uri uri) {
            this.uri = uri;
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    int res = getVideo();
                    if (res == 0) {

                    }
                }
            });
            thread.start();

        }

        protected int getVideo() {
            VideoClipDTO clip = new VideoClipDTO();
            clip.setVideoDate(new Date().getTime());
            clip.setUriString(uri.toString());
            File f;
            try {
                f = ImageUtil.getFileFromUri(ctx, uri);
                clip.setFilePath(f.getAbsolutePath());
                clip.setLength(f.length());
                if (projectTask != null) {
                    clip.setProjectID(projectTask.getProjectID());
                    clip.setProjectTaskID(projectTask.getProjectTaskID());
                }
                if (project != null) {
                    clip.setProjectID(project.getProjectID());
                }
                if (vcc == null) {
                    vcc = new VideoClipContainer();
                }
                vcc.addVideo(clip);
                CacheVideoUtil.cacheVideo(ctx, vcc);
            } catch (Exception e) {
                Log.e(LOG,"Video file save failed", e);
                return 9;
            }

            return 0;
        }
    }

    VideoClipContainer vcc;

    private void dispatchTakeVideoIntent() {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        final Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);

        CacheVideoUtil.getCachedVideo(ctx, new CacheVideoUtil.CacheVideoListener() {
            @Override
            public void onDataDeserialized(VideoClipContainer x) {
                vcc = x;
                if (vcc != null) {
                    if (takeVideoIntent.resolveActivity(getPackageManager()) != null) {
                        startActivityForResult(takeVideoIntent, REQUEST_VIDEO_CAPTURE);
                    }
                }
            }
        });


    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                Log.e(LOG, "Fuck!", ex);
                Util.showErrorToast(ctx, getString(R.string.file_error));
                return;
            }

            if (photoFile != null) {
                Log.w(LOG, "dispatchTakePictureIntent - start pic intent");
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                        Uri.fromFile(photoFile));
                startActivityForResult(takePictureIntent, CAPTURE_IMAGE);
            }
        }
    }


    private File createImageFile() throws IOException {
        // Create an image file name

        String imageFileName = "pic" + System.currentTimeMillis();
        switch (type) {
            case PhotoUploadDTO.PROJECT_IMAGE:
                imageFileName = "project" + project.getProjectName();
                break;
            case PhotoUploadDTO.MONITOR_IMAGE:
                imageFileName = monitor.getLastName() + "-" + monitor.getFirstName();
                break;
            case PhotoUploadDTO.STAFF_IMAGE:
                imageFileName = staff.getFirstName() + "" + staff.getLastName();
                break;
            case PhotoUploadDTO.TASK_IMAGE:
                imageFileName = projectTask.getProjectName() + "_" + projectTask.getTask().getTaskName();
                break;
        }
        imageFileName = imageFileName + "-" + System.currentTimeMillis();
        File root;
        if (Util.hasStorage(true)) {
            Log.i(LOG, "###### get file from getExternalStoragePublicDirectory");
            root = Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_PICTURES);
        } else {
            Log.i(LOG, "###### get file from getDataDirectory");
            root = Environment.getDataDirectory();
        }
        File pics = new File(root, "monitor_app");
        if (!pics.exists()) {
            pics.mkdir();
        }

        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                pics      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = "file:" + image.getAbsolutePath();
        return image;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // getMenuInflater().inflate(R.menu.camera, menu);
        mMenu = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

//        if (item.getItemId() == R.id.menu_video) {
//            dispatchTakeVideoIntent();
//            return true;
//        }
//        if (item.getItemId() == R.id.menu_gallery) {
//            //Intent i = new Intent(this, PictureRecyclerGridActivity.class);
//            //startActivity(i);
//            Util.showToast(ctx, ctx.getString(R.string.under_cons));
//            return true;
//        }
//        if (item.getItemId() == R.id.menu_camera) {
//            dispatchTakePictureIntent();
//            return true;
//        }

        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return false;
    }

    @Override
    public void onPause() {
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        super.onPause();
    }

    @Override
    public void onBackPressed() {

        if (isUploaded) {
            Log.d(LOG, "onBackPressed ... picture uploaded");
            ResponseDTO r = new ResponseDTO();
            Intent i = new Intent();
            i.putExtra("response", r);
            setResult(RESULT_OK, i);
        } else {
            Log.d(LOG, "onBackPressed ... cancelled");
            setResult(RESULT_CANCELED);
        }
        finish();
    }

    @Override
    public void onSaveInstanceState(Bundle b) {
        Log.e(LOG, "############################## onSaveInstanceState");
        b.putInt("type", type);
        if (currentThumbFile != null) {
            b.putString("thumbPath", currentThumbFile.getAbsolutePath());
        }
        if (photoFile != null) {
            b.putString("photoFile", photoFile.getAbsolutePath());
        }
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


    class PhotoTask extends AsyncTask<Void, Void, Integer> {


        @Override
        protected Integer doInBackground(Void... voids) {
            Log.w(LOG, "## PhotoTask starting doInBackground, file length: " + photoFile.length());
            pictureChanged = false;
            ExifInterface exif = null;
            if (photoFile == null || photoFile.length() == 0) {
                Log.e(LOG, "----- photoFile is null or length 0, exiting");
                return 99;
            }
            fileUri = Uri.fromFile(photoFile);
            if (fileUri != null) {
                try {
                    exif = new ExifInterface(photoFile.getAbsolutePath());
                    String orient = exif.getAttribute(ExifInterface.TAG_ORIENTATION);
                    Log.i(LOG, "@@@@@@@@@@@@@@@@@@@@@@ Orientation says: " + orient);
                    float rotate = 0f;
                    if (orient.equalsIgnoreCase("6")) {
                        rotate = 90f;
                        Log.i(LOG, "@@@@@ picture, rotate = " + rotate);
                    }
                    try {
                        BitmapFactory.Options options = new BitmapFactory.Options();
                        options.inSampleSize = 2;
                        Bitmap bm = BitmapFactory.decodeFile(photoFile.getAbsolutePath(), options);
                        getLog(bm, "Raw Camera- sample size = 2");
                        Matrix matrixThumbnail = new Matrix();
                        matrixThumbnail.postScale(0.6f, 0.6f);

                        Bitmap thumb = Bitmap.createBitmap
                                (bm, 0, 0, bm.getWidth(),
                                        bm.getHeight(), matrixThumbnail, true);
                        getLog(thumb, "Thumb");

                        currentThumbFile = ImageUtil.getFileFromBitmap(thumb, "t" + System.currentTimeMillis() + ".jpg");
                        bitmapForScreen = ImageUtil.getBitmapFromUri(ctx, Uri.fromFile(currentThumbFile));

                        thumbUri = Uri.fromFile(currentThumbFile);
                        //write exif data
                        Util.writeLocationToExif(currentThumbFile.getAbsolutePath(), location);
                        boolean del = photoFile.delete();
                        Log.i(LOG, "## Thumbnail file length: " + currentThumbFile.length()
                                + " main image file deleted: " + del);
                    } catch (Exception e) {
                        Log.e(LOG, "$&*%$! Fuck it! unable to process bitmap", e);
                        return 9;
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    return 1;
                }

            }
            return 0;
        }

        @Override
        protected void onPostExecute(Integer result) {
            if (result > 0) {
                Util.showErrorToast(ctx, getString(R.string.camera_error));
                return;
            }
            if (thumbUri != null) {
                pictureChanged = true;
                try {
                    currentSessionPhotos.add(Uri.fromFile(currentThumbFile).toString());
                    addImageToScroller();

                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }
    }

    PhotoAdapter photoAdapter;
    RecyclerView mRecyclerView;
    List<PhotoUploadDTO> photoList;

    private void addImageToScroller() {
        Log.i(LOG, "## addImageToScroller");
        if (photoList == null) {
            photoList = new ArrayList<>();
        } else {
            Log.d(LOG, " ### photos in list: " + photoList.size());
        }
        switch (type) {
            case PhotoUploadDTO.TASK_IMAGE:
                addProjectTaskPicture(localID, new PhotoCacheUtil.PhotoCacheListener() {
                    @Override
                    public void onFileDataDeserialized(ResponseDTO response) {

                    }

                    @Override
                    public void onDataCached(PhotoUploadDTO photo) {
                        photoList.add(0,photo);
                        showCachedPhotos();
                    }

                    @Override
                    public void onError() {

                    }
                });
                break;
            case PhotoUploadDTO.MONITOR_IMAGE:
                addMonitorPicture(new PhotoCacheUtil.PhotoCacheListener() {
                    @Override
                    public void onFileDataDeserialized(ResponseDTO response) {

                    }

                    @Override
                    public void onDataCached(PhotoUploadDTO photo) {
                        photoList.add(0,photo);
                        showCachedPhotos();
                    }

                    @Override
                    public void onError() {

                    }
                });
                break;
            case PhotoUploadDTO.STAFF_IMAGE:
                addStaffPicture(new PhotoCacheUtil.PhotoCacheListener() {
                    @Override
                    public void onFileDataDeserialized(ResponseDTO response) {

                    }

                    @Override
                    public void onDataCached(PhotoUploadDTO photo) {
                        photoList.add(0,photo);
                        showCachedPhotos();
                    }

                    @Override
                    public void onError() {

                    }
                });
                break;
            case PhotoUploadDTO.PROJECT_IMAGE:
                addProjectPicture(new PhotoCacheUtil.PhotoCacheListener() {
                    @Override
                    public void onFileDataDeserialized(ResponseDTO response) {

                    }

                    @Override
                    public void onDataCached(PhotoUploadDTO photo) {
                        photoList.add(0,photo);
                        showCachedPhotos();
                    }

                    @Override
                    public void onError() {

                    }
                });
                break;
        }


    }

    ResponseDTO response;

    private void showCachedPhotos() {
        if (mBound) {
            mService.uploadCachedPhotos(new PhotoUploadService.UploadListener() {
                @Override
                public void onUploadsComplete(List<PhotoUploadDTO> photoList) {
                    Log.e(LOG, "### onUploadsComplete: " + photoList.size());
                    isUploaded = true;
                    Snackbar.make(mRecyclerView, "Photo has been uploaded", Snackbar.LENGTH_SHORT).show();


                }
            });
        }
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                FileInputStream stream;
                try {
                    stream = getApplicationContext().openFileInput(PhotoUploadService.JSON_PHOTO);
                    response = Util.getResponseData(stream);
                    photoList = new ArrayList<>();
                    for (PhotoUploadDTO c : response.getPhotoUploadList()) {

                        switch (type) {
                            case PhotoUploadDTO.TASK_IMAGE:
                                if (projectTask.getProjectID().intValue() == c.getProjectID().intValue()) {
                                    photoList.add(c);
                                }
                                break;
                            case PhotoUploadDTO.PROJECT_IMAGE:
                                if (project.getProjectID().intValue() == c.getProjectID().intValue()) {
                                    photoList.add(c);
                                }
                                break;
                        }
                    }

                    if (photoList.isEmpty()) {
                        Snackbar.make(mRecyclerView, getString(R.string.no_prev_photos),
                                Snackbar.LENGTH_LONG).show();

                        return;
                    }
                    Collections.sort(photoList);
                    Log.i(LOG, "++ showCachedPhotos - photo cache retrieved, photos: " + response.getPhotoUploadList().size());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (photoAdapter == null) {
                                photoAdapter = new PhotoAdapter(photoList, PhotoAdapter.THUMB, getApplicationContext(), new PhotoAdapter.PictureListener() {
                                    @Override
                                    public void onPictureClicked(int position) {
                                        Log.e(LOG, "onPictureClicked: " + position);
                                        Intent w = new Intent(ctx, PhotoListActivity.class);
                                        w.putExtra("index", position);
                                        w.putExtra("response", response);

                                        startActivity(w);

                                    }
                                });
                                mRecyclerView.setAdapter(photoAdapter);
                            } else {
                                photoAdapter.notifyDataSetChanged();
                            }
                        }
                    });
                } catch (FileNotFoundException e) {
                    Log.w(LOG, "############# cache file not found.", e);

                } catch (IOException e) {
                    Log.e(LOG, "Failed", e);
                }
            }
        });
        thread.start();

    }

    List<String> currentSessionPhotos = new ArrayList<>();

    private void getLog(Bitmap bm, String which) {
        if (bm == null) return;
        Log.e(LOG, which + " - bitmap: width: "
                + bm.getWidth() + " height: "
                + bm.getHeight() + " rowBytes: "
                + bm.getRowBytes());
    }


    boolean mBound;
    PhotoUploadService mService;


    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            Log.w(LOG, "## PhotoUploadService ServiceConnection onServiceConnected");
            PhotoUploadService.LocalBinder binder = (PhotoUploadService.LocalBinder) service;
            mService = binder.getService();
            mBound = true;
            mService.uploadCachedPhotos(new PhotoUploadService.UploadListener() {
                @Override
                public void onUploadsComplete(List<PhotoUploadDTO> list) {
                    Log.w(LOG, "$$$ onUploadsComplete, list: " + list.size());
                }
            });
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            Log.w(LOG, "## PhotoUploadService onServiceDisconnected");
            mBound = false;
        }
    };

    String mCurrentPhotoPath;
    ProjectDTO project;
    StaffDTO staff;
    ProjectTaskDTO projectTask;

    File photoFile;
    boolean isUploaded;
    static final int REQUEST_VIDEO_CAPTURE = 1;


    File currentThumbFile;
    Uri thumbUri;
    static final String LOG = PictureActivity.class.getSimpleName();

    Menu mMenu;
    int type;

    boolean pictureChanged;
    Context ctx;
    Uri fileUri;
    public static final int CAPTURE_IMAGE = 9908;

    Bitmap bitmapForScreen;


    public void addMonitorPicture(final PhotoCacheUtil.PhotoCacheListener listener) {
        Log.w(LOG, "**** addMonitorPicture");
        final PhotoUploadDTO dto = getObject();
        dto.setPictureType(PhotoUploadDTO.MONITOR_IMAGE);
        dto.setAccuracy(location.getAccuracy());

        Log.w(LOG, "**** addPhotoToCache starting ... file: " + dto.getThumbFilePath());
        PhotoCacheUtil.cachePhoto(ctx, dto, new PhotoCacheUtil.PhotoCacheListener() {
            @Override
            public void onFileDataDeserialized(ResponseDTO response) {

            }

            @Override
            public void onDataCached(PhotoUploadDTO p) {
                Log.w(LOG, "### photo has been cached");
                listener.onDataCached(p);

            }

            @Override
            public void onError() {
                Util.showErrorToast(ctx, getString(R.string.photo_error));
            }
        });
    }

    private interface CacheListener {
        public void onCachingDone();
    }

    public void addProjectTaskPicture(Long localID, final PhotoCacheUtil.PhotoCacheListener listener) {
        Log.w(LOG, "**** addProjectTaskPicture");
        final PhotoUploadDTO dto = getObject();
        dto.setProjectID(projectTask.getProjectID());
        dto.setProjectTaskID(projectTask.getProjectTaskID());
        dto.setPictureType(PhotoUploadDTO.TASK_IMAGE);
        dto.setAccuracy(location.getAccuracy());
        dto.setThumbFilePath(currentThumbFile.getAbsolutePath());
        PhotoCacheUtil.cachePhoto(ctx, dto, new PhotoCacheUtil.PhotoCacheListener() {
            @Override
            public void onFileDataDeserialized(ResponseDTO response) {

            }

            @Override
            public void onDataCached(PhotoUploadDTO p) {
                Log.w(LOG, "### photo has been cached");
                listener.onDataCached(p);
            }

            @Override
            public void onError() {
                Util.showErrorToast(ctx, getString(R.string.photo_error));
            }
        });

    }

    public void addProjectPicture(final PhotoCacheUtil.PhotoCacheListener listener) {
        Log.w(LOG, "**** addProjectPicture");
        final PhotoUploadDTO dto = getObject();
        dto.setProjectID(project.getProjectID());
        dto.setPictureType(PhotoUploadDTO.PROJECT_IMAGE);
        dto.setThumbFilePath(currentThumbFile.getAbsolutePath());

        PhotoCacheUtil.cachePhoto(ctx, dto, new PhotoCacheUtil.PhotoCacheListener() {
            @Override
            public void onFileDataDeserialized(ResponseDTO response) {

            }

            @Override
            public void onDataCached(PhotoUploadDTO p) {
                Log.w(LOG, "### photo has been cached");
                listener.onDataCached(p);
            }

            @Override
            public void onError() {
                Util.showErrorToast(ctx, getString(R.string.photo_error));
            }
        });
    }


    private PhotoUploadDTO getObject() {
        PhotoUploadDTO dto = new PhotoUploadDTO();
        dto.setCompanyID(SharedUtil.getCompany(ctx).getCompanyID());
        if (SharedUtil.getCompanyStaff(ctx) != null) {
            dto.setStaffID(SharedUtil.getCompanyStaff(ctx).getStaffID());
        }
        if (SharedUtil.getMonitor(ctx) != null) {
            dto.setMonitorID(SharedUtil.getMonitor(ctx).getMonitorID());
        }

        dto.setThumbFilePath(currentThumbFile.getAbsolutePath());
        dto.setThumbFlag(1);
        dto.setDateTaken(new Date().getTime());
        dto.setLatitude(location.getLatitude());
        dto.setLongitude(location.getLongitude());
        dto.setAccuracy(location.getAccuracy());

        return dto;
    }


    public void addStaffPicture(final PhotoCacheUtil.PhotoCacheListener listener) {
        final PhotoUploadDTO dto = getObject();
        dto.setStaffID(staff.getStaffID());
        dto.setPictureType(PhotoUploadDTO.STAFF_IMAGE);
        dto.setThumbFilePath(currentThumbFile.getAbsolutePath());
        PhotoCacheUtil.cachePhoto(ctx, dto, new PhotoCacheUtil.PhotoCacheListener() {
            @Override
            public void onFileDataDeserialized(ResponseDTO response) {

            }

            @Override
            public void onDataCached(PhotoUploadDTO p) {
                Log.w(LOG, "### photo has been cached :)");
                listener.onDataCached(p);
            }

            @Override
            public void onError() {
                Util.showErrorToast(getApplicationContext(), getString(R.string.photo_error));
            }
        });
    }

}
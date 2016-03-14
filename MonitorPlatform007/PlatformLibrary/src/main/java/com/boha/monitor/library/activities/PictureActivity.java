package com.boha.monitor.library.activities;

import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.boha.monitor.library.dto.MonitorDTO;
import com.boha.monitor.library.dto.PhotoUploadDTO;
import com.boha.monitor.library.dto.ProjectDTO;
import com.boha.monitor.library.dto.ProjectTaskDTO;
import com.boha.monitor.library.dto.ProjectTaskStatusDTO;
import com.boha.monitor.library.dto.ResponseDTO;
import com.boha.monitor.library.dto.StaffDTO;
import com.boha.monitor.library.services.PhotoUploadService;
import com.boha.monitor.library.util.ImageUtil;
import com.boha.monitor.library.util.PMException;
import com.boha.monitor.library.util.ScalingUtilities;
import com.boha.monitor.library.util.SharedUtil;
import com.boha.monitor.library.util.Snappy;
import com.boha.monitor.library.util.ThemeChooser;
import com.boha.monitor.library.util.Util;
import com.boha.platform.library.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.squareup.picasso.Picasso;

import org.acra.ACRA;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * Manages picture taking - starts onboard camera app and caches
 * the resultant image. Invokes a service to uploadToYouTube the image to cloudinary CDN
 * <p/>
 * Created by aubreyM on 2014/04/21.
 */
public class PictureActivity extends AppCompatActivity implements LocationListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {
    LocationRequest mLocationRequest;
    GoogleApiClient googleApiClient;
    LayoutInflater inflater;
    TextView txtTitle, txtSubtitle, txtCount, txtMessage;
    ImageView imageView;
    MonitorDTO monitor;
    ProjectTaskStatusDTO projectTaskStatus;
    String mCurrentPhotoPath;
    ProjectDTO project;
    StaffDTO staff;
    ProjectTaskDTO projectTask;
    File photoFile;
    File currentThumbFile;

    Menu mMenu;
    int type;
    Context ctx;
    FloatingActionButton fab;
    public static final int CAPTURE_IMAGE = 9908;

    static final String LOG = PictureActivity.class.getSimpleName();

    public void onCreate(Bundle savedInstanceState) {
        Log.d(LOG, "PictureActivity onCreate");
        ThemeChooser.setTheme(this);
        super.onCreate(savedInstanceState);
        ctx = getApplicationContext();
        inflater = getLayoutInflater();
        setContentView(R.layout.camera);

        monitor = (MonitorDTO) getIntent().getSerializableExtra("monitor");
        staff = (StaffDTO) getIntent().getSerializableExtra("staff");
        project = (ProjectDTO) getIntent().getSerializableExtra("project");
        projectTask = (ProjectTaskDTO) getIntent().getSerializableExtra("projectTask");
        projectTaskStatus = (ProjectTaskStatusDTO) getIntent().getSerializableExtra("projectTaskStatus");

        setFields();
        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        //
        type = getIntent().getIntExtra("type", 0);
        if (monitor != null)
            type = PhotoUploadDTO.MONITOR_IMAGE;
        if (staff != null)
            type = PhotoUploadDTO.STAFF_IMAGE;
        if (project != null)
            type = PhotoUploadDTO.PROJECT_IMAGE;
        if (projectTask != null)
            type = PhotoUploadDTO.TASK_IMAGE;
        if (projectTaskStatus != null)
            type = PhotoUploadDTO.TASK_IMAGE;

        switch (type) {
            case PhotoUploadDTO.MONITOR_IMAGE:
                txtTitle.setText(monitor.getFullName());
                dispatchTakePictureIntent();
                Util.setCustomActionBar(getApplicationContext(), getSupportActionBar(),
                        monitor.getFullName(), "Profile Photo",
                        ContextCompat.getDrawable(getApplicationContext(), R.drawable.glasses));


                break;
            case PhotoUploadDTO.STAFF_IMAGE:
                txtTitle.setText(staff.getFirstName() + " " + staff.getLastName());
                dispatchTakePictureIntent();
                Util.setCustomActionBar(getApplicationContext(), getSupportActionBar(),
                        staff.getFullName(), "Profile Photo",
                        ContextCompat.getDrawable(getApplicationContext(), R.drawable.glasses));

                break;
            case PhotoUploadDTO.PROJECT_IMAGE:

                txtTitle.setText(project.getProjectName());
                Util.setCustomActionBar(getApplicationContext(), getSupportActionBar(),
                        project.getProjectName(), project.getCityName(),
                        ContextCompat.getDrawable(getApplicationContext(), R.drawable.glasses));
                break;

            case PhotoUploadDTO.TASK_IMAGE:
                txtTitle.setText(projectTask.getProjectName());
                txtSubtitle.setText(projectTask.getTask().getTaskName());
                Util.setCustomActionBar(getApplicationContext(), getSupportActionBar(),
                        projectTask.getProjectName(), projectTask.getTask().getTaskName(),
                        ContextCompat.getDrawable(getApplicationContext(), R.drawable.glasses));

                break;

        }


        checkPermissions();

        //receive notification when PhotoUploadService has uploaded photos
        IntentFilter mStatusIntentFilter3 = new IntentFilter(
                PhotoUploadService.BROADCAST_ACTION);
        PhotoUploadedReceiver receiver3 = new PhotoUploadedReceiver();
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver3,
                mStatusIntentFilter3);
    }


    @Override
    public void onResume() {
        Log.d(LOG, "@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ onResume...........");
        super.onResume();
        if (currentThumbFile != null) {
            Log.w(LOG, "onResume currentThumbFile size: " + currentThumbFile.length());
            Picasso.with(ctx).load(currentThumbFile).into(imageView);
        }


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
        String path = savedInstanceState.getString("thumbPath");
        if (path != null) {
            currentThumbFile = new File(path);
            Log.d(LOG, "onRestoreInstanceState currentThumbFile: " + currentThumbFile.length());
        }
        String path2 = savedInstanceState.getString("photoFile");
        if (path2 != null) {
            photoFile = new File(path2);
            Log.d(LOG, "onRestoreInstanceState photoFile: " + photoFile.length());
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


    private void setFields() {
        activity = this;
        txtTitle = (TextView) findViewById(R.id.CAM_title);
        txtSubtitle = (TextView) findViewById(R.id.CAM_subTitle);
        txtMessage = (TextView) findViewById(R.id.CAM_message);
        txtCount = (TextView) findViewById(R.id.CAM_count);
        imageView = (ImageView) findViewById(R.id.CAM_image);

        fab = (FloatingActionButton) findViewById(R.id.fab);
        txtTitle.setText("");
        txtSubtitle.setText("");
        txtCount.setText("0");
        txtMessage.setVisibility(View.GONE);


        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchTakePictureIntent();
            }
        });

    }

    static final int PROXIMITY_LIMIT = 500;

    /**
     * The camera app returns control to PictureActivity.
     * If the result code is OK, AsyncTask to process the image is started
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    public void onActivityResult(final int requestCode, final int resultCode,
                                 final Intent data) {
        Log.e(LOG, "##### onActivityResult requestCode: " + requestCode
                + " resultCode: " + resultCode);

        switch (requestCode) {
            case CAPTURE_IMAGE:
                if (resultCode == Activity.RESULT_OK) {
                    if (resultCode == Activity.RESULT_OK) {
                        Location pLoc = new Location("deviceLoc");
                        if (type == PhotoUploadDTO.PROJECT_IMAGE
                                || type == PhotoUploadDTO.TASK_IMAGE) {
                            if (project != null) {
                                pLoc.setLatitude(project.getLatitude());
                                pLoc.setLongitude(project.getLongitude());
                            }
                            if (projectTask != null) {
                                pLoc.setLatitude(projectTask.getLatitude());
                                pLoc.setLongitude(projectTask.getLongitude());
                            }

                            if (location != null) {
                                boolean locationIsWithin = Util.locationIsWithin(pLoc,
                                        location, PROXIMITY_LIMIT);
                                if (locationIsWithin) {
                                    new PhotoTask().execute();
                                } else {
                                    Util.showErrorToast(ctx,
                                            "You seem to be more than " + PROXIMITY_LIMIT +
                                                    " metres away from the project. Picture cannot be taken.");
                                    pictureTakenOK = false;
                                    onBackPressed();
                                }
                            } else {
                                Util.showErrorToast(ctx, "Your GPS location is currently undefined");
                            }
                        } else {
                            new PhotoTask().execute();
                        }
                    }
                } else {
                    Util.showErrorToast(ctx, getString(R.string.camera_error));
                }
                break;

        }
    }

    @Override
    public void onLocationChanged(Location loc) {
        Log.d(LOG, "## onLocationChanged accuracy = " + loc.getAccuracy()
                + " - " + new Date().toString());
        if (loc.getAccuracy() <= ACCURACY_THRESHOLD) {
            this.location = loc;

        }
    }

    /**
     * Connect the GoogleApiClient and bind to PhotoUploadService
     */
    @Override
    public void onStart() {
        Log.i(LOG,
                "## onStart - GoogleApiClient connecting ... binding service");
        if (googleApiClient != null) {
            googleApiClient.connect();
        }
        super.onStart();
    }

    @Override
    public void onDestroy() {

        super.onDestroy();
    }

    @Override
    public void onStop() {
        super.onStop();
        stopLocationUpdates();

    }

    Location location;
    static final float ACCURACY_THRESHOLD = 30;
    PictureActivity activity;
    boolean mRequestingLocationUpdates;

    @Override
    public void onConnected(Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        location = LocationServices.FusedLocationApi.getLastLocation(
                googleApiClient);
        if (location != null)
            Log.w(LOG, "## googleApiClient onConnected, requesting location updates ....getLastLocation acc: " + location.getAccuracy());
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setInterval(60000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setFastestInterval(10000);
        mRequestingLocationUpdates = true;
        LocationServices.FusedLocationApi.requestLocationUpdates(
                googleApiClient, mLocationRequest, this);



    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    /**
     * Start GPS location search
     */
    protected void startLocationUpdates() {
        if (googleApiClient.isConnected()) {
            mRequestingLocationUpdates = true;
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            LocationServices.FusedLocationApi.requestLocationUpdates(
                    googleApiClient, mLocationRequest, this);
        }
    }

    /**
     * Stop GPS location search
     */
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

    /**
     * Start default on-board camera app
     */
    private void dispatchTakePictureIntent() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            Log.e(LOG, "WRITE_EXTERNAL_STORAGE permission not granted yet");
            return;
        }
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            photoFile = null;
            try {
                createPhotoFile();
                if (photoFile != null) {
                    Log.w(LOG, "photoFile created: " + photoFile.getAbsolutePath());
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                            Uri.fromFile(photoFile));
                    startActivityForResult(takePictureIntent, CAPTURE_IMAGE);
                } else {
                    Util.showErrorToast(ctx, "Unable to start camera");
                }

            } catch (IOException ex) {
                Log.e(LOG, "Fuck!", ex);
                Util.showErrorToast(ctx, getString(R.string.file_error));
            }
        }
    }


    private void checkPermissions() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_WRITE_EXTERNAL_STORAGE);

            }

        }

    }

    static final int MY_PERMISSIONS_WRITE_EXTERNAL_STORAGE = 81;

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_WRITE_EXTERNAL_STORAGE: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.w(LOG, "ACCESS_FINE_LOCATION permission granted");
                    dispatchTakePictureIntent();

                } else {
                    Log.e(LOG, "ACCESS_FINE_LOCATION permission denied");

                }
                return;
            }


            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    private void createPhotoFile() throws IOException {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            Log.d(LOG, "WRITE_EXTERNAL_STORAGE permission not granted yet");
            return;
        }

        String imageFileName = "photoFile";

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
            boolean isOK = pics.mkdir();
            if (!isOK) {
                Util.showErrorToast(this, "Unable to get file storage for picture");
                return;
            }
        }

        photoFile = new File(pics, "photoFile.jpg");

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.camera_menu, menu);
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
        Log.d(LOG, "onPause");
        super.onPause();

    }

    @Override
    public void onBackPressed() {
        Intent i = new Intent();
        i.putExtra("pictureTakenOK", pictureTakenOK);

        if (pictureTakenOK) {
            Log.d(LOG, "onBackPressed ... picture cached and scheduled for uploadToYouTube");
            ResponseDTO resp = new ResponseDTO();
            resp.setPhotoUploadList(photoUploadList);
            i.putExtra("photos", resp);

            i.putExtra("file", currentThumbFile.getAbsolutePath());
            setResult(RESULT_OK, i);
        } else {
            Log.d(LOG, "onBackPressed ... cancelled");
            setResult(RESULT_CANCELED, i);
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


    /**
     * AsyncTask to process the image received from the camera
     */
    class PhotoTask extends AsyncTask<Void, Void, Integer> {


        /**
         * Scale the image to required size and delete the larger one
         *
         * @param voids
         * @return
         */
        @Override
        protected Integer doInBackground(Void... voids) {
            try {
                if (photoFile == null || photoFile.length() == 0) {
                    Log.e(LOG, "----->> photoFile is null or length 0, exiting");
                    return 99;
                } else {
                    Log.w(LOG, "## PhotoTask starting, photoFile length: "
                            + photoFile.length());
                }
                processFile();
                //resizePhoto();
            } catch (Exception e) {
                Log.e(LOG, "Camera file processing failed", e);
                return 9;
            }


            return 0;
        }

        private Bitmap decode(Bitmap bm, int width) {
            float aspectRatio = bm.getWidth() /
                    (float) bm.getHeight();
            int height = Math.round(width / aspectRatio);

            Bitmap x = Bitmap.createScaledBitmap(
                    bm, width, height, false);

            return x;
        }

        protected void processFile() throws Exception {
//            Bitmap bm = Util.decodeSampledBitmap(
//                    photoFile, 300, 400);
            //Bitmap bm = ImageUtil.getScaledImage(ctx,photoFile,360,500,false);
//            Bitmap bm = decode(BitmapFactory
//                    .decodeFile(photoFile.getAbsolutePath()), 400);
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 4;
            Bitmap main = BitmapFactory.decodeFile(photoFile.getAbsolutePath(), options);
            Bitmap bm = ScalingUtilities.createScaledBitmap(
                    main, 600, 800, ScalingUtilities.ScalingLogic.CROP);

            if (main.getWidth() > main.getHeight()) {
                Log.d(LOG, "*** this image in landscape");
                bm = Util.rotateBitmap(bm);

            }
            getLog(bm, "decoded Bitmap");
            currentThumbFile = ImageUtil.getFileFromBitmap(bm,
                    "t" + System.currentTimeMillis() + ".jpg");

            //write exif data
            if (location != null) {
                Util.writeLocationToExif(currentThumbFile.getAbsolutePath(), location);
            }
            main.recycle();
            bm.recycle();
            Log.i(LOG, "## photo file length: " + getLength(currentThumbFile.length())
                    + ", original size: " + getLength(photoFile.length()));

        }

        private void resizePhoto() throws Exception {
            Log.w(LOG, "## PhotoTask starting doInBackground, file length: " + photoFile.length());
            if (photoFile == null || photoFile.length() == 0) {
                Log.e(LOG, "----- photoFile is null or length 0, exiting");
                throw new Exception();
            }

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 4;
            Bitmap bm = BitmapFactory.decodeFile(photoFile.getAbsolutePath(), options);
            getLog(bm, "Raw Camera- sample size = 4");
            Matrix matrixThumbnail = new Matrix();
            matrixThumbnail.postScale(0.6f, 0.6f);

            Bitmap thumb = Bitmap.createBitmap
                    (bm, 0, 0, bm.getWidth(), bm.getHeight(), matrixThumbnail, true);
            getLog(thumb, "Photo bitMap created");

            currentThumbFile = ImageUtil.getFileFromBitmap(
                    thumb, "t" + System.currentTimeMillis() + ".jpg");

            //write exif data
            Util.writeLocationToExif(currentThumbFile.getAbsolutePath(), location);
            //clean up
            boolean del = photoFile.delete();
            bm.recycle();
            thumb.recycle();
            Log.i(LOG, "## Thumbnail file length: " + currentThumbFile.length()
                    + " main image file deleted: " + del);


        }

        private String getLength(long num) {
            BigDecimal decimal = new BigDecimal(num).divide(new BigDecimal(1024), 2, BigDecimal.ROUND_HALF_UP);

            return "" + decimal.doubleValue() + " KB";
        }

        @Override
        protected void onPostExecute(Integer result) {
            Log.e(LOG, "onPostExecute result: " + result.intValue());
            if (result > 0) {
                pictureTakenOK = false;
                Util.showErrorToast(ctx, getString(R.string.camera_error));
                return;
            }
            pictureTakenOK = true;
            Picasso.with(ctx).load(currentThumbFile).into(imageView);
            pictureCount++;
            txtCount.setText("" + pictureCount);
            cachePhoto();
        }
    }

    /**
     * Cache photo by appropriate type
     */
    private void cachePhoto() {

        switch (type) {
            case PhotoUploadDTO.TASK_IMAGE:
                addProjectTaskPicture();
                break;

            case PhotoUploadDTO.PROJECT_IMAGE:
                addProjectPicture();
                break;
            case PhotoUploadDTO.STAFF_IMAGE:
                addStaffPicture();
                break;
            case PhotoUploadDTO.MONITOR_IMAGE:
                addMonitorPicture();
                break;
        }
        //


    }

    ResponseDTO response;
    int pictureCount;
    boolean pictureTakenOK;
    List<PhotoUploadDTO> photoUploadList = new ArrayList<>();

    private void getLog(Bitmap bm, String which) {
        if (bm == null) return;
        Log.e(LOG, which + " - bitmap: width: "
                + bm.getWidth() + " height: "
                + bm.getHeight() + " rowBytes: "
                + bm.getRowBytes());
    }

    public void addProjectTaskPicture() {
        Log.w(LOG, "**** addProjectTaskPicture");
        final PhotoUploadDTO dto = getObject();
        dto.setProjectID(projectTask.getProjectID());
        dto.setProjectTaskID(projectTask.getProjectTaskID());
        dto.setPictureType(PhotoUploadDTO.TASK_IMAGE);
        dto.setAccuracy(location.getAccuracy());
        dto.setThumbFilePath(currentThumbFile.getAbsolutePath());
        if (projectTaskStatus != null) {
            ProjectTaskStatusDTO x = new ProjectTaskStatusDTO();
            x.setProjectTaskStatusID(projectTaskStatus.getProjectTaskStatusID());
            dto.setProjectTaskStatus(x);
        }

        saveAndUpload(dto);
    }

    private void saveAndUpload(final PhotoUploadDTO dto) {
        photoUploadList.add(dto);
        Snappy.cachePhotoForUpload((MonApp) getApplication(), dto, new Snappy.SnappyWriteListener() {
            @Override
            public void onDataWritten() {
                Intent a = new Intent(ctx, PhotoUploadService.class);
                startService(a);
            }

            @Override
            public void onError(String message) {

            }
        });

    }

    public void addProjectPicture() {
        Log.w(LOG, "**** addProjectPicture");
        final PhotoUploadDTO dto = getObject();
        dto.setProjectID(project.getProjectID());
        dto.setPictureType(PhotoUploadDTO.PROJECT_IMAGE);
        dto.setThumbFilePath(currentThumbFile.getAbsolutePath());

        saveAndUpload(dto);
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
        dto.setDateTaken(new Date().getTime());
        if (location != null) {
            dto.setLatitude(location.getLatitude());
            dto.setLongitude(location.getLongitude());
            dto.setAccuracy(location.getAccuracy());
        }

        return dto;
    }


    public void addStaffPicture() {
        final PhotoUploadDTO dto = getObject();
        dto.setStaffID(staff.getStaffID());
        dto.setPictureType(PhotoUploadDTO.STAFF_IMAGE);
        dto.setThumbFilePath(currentThumbFile.getAbsolutePath());
        saveAndUpload(dto);
    }

    public void addMonitorPicture() {
        final PhotoUploadDTO dto = getObject();
        dto.setMonitorID(monitor.getMonitorID());
        dto.setPictureType(PhotoUploadDTO.MONITOR_IMAGE);
        dto.setThumbFilePath(currentThumbFile.getAbsolutePath());
        saveAndUpload(dto);
    }

    // Broadcast receiver for receiving status updates from DataRefreshService
    private class PhotoUploadedReceiver extends BroadcastReceiver {

        // Called when the BroadcastReceiver gets an Intent it's registered to receive
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.e(LOG, "+++++++PhotoUploadedReceiver onReceive, photo uploaded: "
                    + intent.toString());
            Snackbar.make(txtTitle,
                    "Photo has been uploaded OK",
                    Snackbar.LENGTH_LONG).show();

        }
    }


    //**************** GEOFENCE
//
//    PendingIntent mGeofencePendingIntent;
//    List<Geofence> mGeofenceList = new ArrayList<>();
//
//    @Override
//    public void onResult(@NonNull Status status) {
//        Log.w(LOG, "... onResult: status: " + status.toString());
//        if (status.isSuccess()) {
//            Log.e(LOG, "### Methinks a Geofence has been created OK, fences: "
//            + mGeofenceList.size());
//        }
//
//
//    }
//
//    static final float RADIUS = 300;
//    static final int GEOFENCE_EXPIRATION = 3000;
//
//    private void addFence() {
//        if (ActivityCompat.checkSelfPermission(this,
//                Manifest.permission.ACCESS_FINE_LOCATION) !=
//                PackageManager.PERMISSION_GRANTED) {
//            return;
//        }
//        if (!googleApiClient.isConnected()) {
//            return;
//        }
//        Log.w(LOG, ".... add project geoFence ....: " + project.getProjectName());
//        mGeofenceList.add(new Geofence.Builder()
//                .setRequestId(project.getProjectName())
//                .setCircularRegion(
//                        project.getLatitude(),
//                        project.getLongitude(),
//                        RADIUS
//                )
//                .setExpirationDuration(GEOFENCE_EXPIRATION)
//                .setTransitionTypes(
//                                Geofence.GEOFENCE_TRANSITION_ENTER |
//                                Geofence.GEOFENCE_TRANSITION_EXIT)
//                .build());
//
//        LocationServices.GeofencingApi.addGeofences(
//                googleApiClient,
//                getGeofencingRequest(),
//                getGeofencePendingIntent()
//        ).setResultCallback(this);
//    }
//
//    private PendingIntent getGeofencePendingIntent() {
//        if (mGeofencePendingIntent != null) {
//            return mGeofencePendingIntent;
//        }
//        Log.w(LOG, "... getGeofencePendingIntent");
//        Intent intent = new Intent(this, GeofenceIntentService.class);
//        return PendingIntent.getService(this, 0, intent, PendingIntent.
//                FLAG_UPDATE_CURRENT);
//    }
//
//    private GeofencingRequest getGeofencingRequest() {
//        Log.w(LOG, "... getGeofencingRequest");
//        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
//        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER
//                | GeofencingRequest.INITIAL_TRIGGER_DWELL
//                | GeofencingRequest.INITIAL_TRIGGER_EXIT);
//        builder.addGeofences(mGeofenceList);
//        return builder.build();
//    }
//
//    // Broadcast receiver for receiving location request
//    private class GeofenceEventReceiver extends BroadcastReceiver {
//
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            String event = intent.getStringExtra("event");
//            Log.e(LOG, "+++++++GeofenceEventReceiver onReceive, location requested: "
//                    + intent.toString());
//            Statics.setRobotoFontLight(context,txtMessage);
//            if (event.equalsIgnoreCase("ENTER")) {
//                Log.w(LOG, "the device is INSIDE the project geofence");
//                amIinTheFence = true;
//                txtMessage.setVisibility(View.GONE);
//                Util.expand(fab, 1000, null);
//            }
//            if (event.equalsIgnoreCase("EXIT")) {
//                Log.w(LOG, "the device is OUTSIDE the project geofence");
//                amIinTheFence = false;
//                txtMessage.setVisibility(View.VISIBLE);
//                Util.collapse(fab, 1000, null);
//
//            }
//
//        }
//    }
//
//    boolean amIinTheFence;
}
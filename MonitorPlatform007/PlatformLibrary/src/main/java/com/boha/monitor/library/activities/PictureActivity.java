package com.boha.monitor.library.activities;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
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
import android.support.annotation.RequiresPermission;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
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
import com.boha.monitor.library.util.PhotoCacheUtil;
import com.boha.monitor.library.util.ScalingUtilities;
import com.boha.monitor.library.util.SharedUtil;
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
import java.util.Date;

/**
 * Manages picture taking - starts onboard camera app and caches
 * the resultant image. Invokes a service to upload the image to cloudinary CDN
 * <p/>
 * Created by aubreyM on 2014/04/21.
 */
public class PictureActivity extends AppCompatActivity implements LocationListener,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    LocationRequest mLocationRequest;
    GoogleApiClient googleApiClient;
    LayoutInflater inflater;
    TextView txtTitle, txtSubtitle, txtCount;
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
                    txtTitle.setText(staff.getFirstName() + " " + staff.getLastName());
                    dispatchTakePictureIntent();
                    Util.setCustomActionBar(getApplicationContext(), getSupportActionBar(),
                            staff.getFullName(), "Profile Photo",
                            ContextCompat.getDrawable(getApplicationContext(), R.drawable.glasses48));

                }
                break;
            case PhotoUploadDTO.PROJECT_IMAGE:
                project = (ProjectDTO) getIntent().getSerializableExtra("project");
                if (project != null) {
                    txtTitle.setText(project.getProjectName());
                    Util.setCustomActionBar(getApplicationContext(), getSupportActionBar(),
                            project.getProjectName(), project.getCityName(),
                            ContextCompat.getDrawable(getApplicationContext(), R.drawable.glasses48));
                }
                break;

            case PhotoUploadDTO.TASK_IMAGE:
                projectTask = (ProjectTaskDTO) getIntent().getSerializableExtra("projectTask");
                projectTaskStatus = (ProjectTaskStatusDTO) getIntent().getSerializableExtra("projectTaskStatus");
                if (projectTask != null) {
                    txtTitle.setText(projectTask.getProjectName());
                    txtSubtitle.setText(projectTask.getTask().getTaskName());
                    Util.setCustomActionBar(getApplicationContext(), getSupportActionBar(),
                            projectTask.getProjectName(), projectTask.getTask().getTaskName(),
                            ContextCompat.getDrawable(getApplicationContext(), R.drawable.glasses48));
                }
                break;
            default:
                projectTask = (ProjectTaskDTO) getIntent().getSerializableExtra("projectTask");
                projectTaskStatus = (ProjectTaskStatusDTO) getIntent().getSerializableExtra("projectTaskStatus");
                if (projectTask != null) {
                    txtTitle.setText(projectTask.getProjectName());
                    txtSubtitle.setText(projectTask.getTask().getTaskName());
                    Util.setCustomActionBar(getApplicationContext(), getSupportActionBar(),
                            projectTask.getProjectName(), projectTask.getTask().getTaskName(),
                            ContextCompat.getDrawable(getApplicationContext(), R.drawable.glasses48));

                    break;
                }
                throw new UnsupportedOperationException("No data passed to activity");
        }


        checkPermissions();
        Intent w = new Intent(ctx, PhotoUploadService.class);
        w.putExtra("sat", "sat");
        startService(w);

    }


    @Override
    public void onResume() {
        Log.d(LOG, "@@@@@@ onResume...........");
        super.onResume();
        if (currentThumbFile != null) {
            Log.w(LOG,"onResume currentThumbFile size: " + currentThumbFile.length());
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
            Log.d(LOG,"onRestoreInstanceState currentThumbFile: " + currentThumbFile.length());
        }
        String path2 = savedInstanceState.getString("photoFile");
        if (path2 != null) {
            photoFile = new File(path2);
            Log.d(LOG,"onRestoreInstanceState photoFile: " + photoFile.length());
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
        txtCount = (TextView) findViewById(R.id.CAM_count);
        imageView = (ImageView) findViewById(R.id.CAM_image);

        fab = (FloatingActionButton) findViewById(R.id.fab);
        txtTitle.setText("");
        txtSubtitle.setText("");
        txtCount.setText("0");


        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchTakePictureIntent();
            }
        });

    }

    static final int PROXIMITY_LIMIT = 200;

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
                        Location pLoc = new Location("");
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
                                            "You seem to be more than "+PROXIMITY_LIMIT+" metres away from the project. Picture cannot be taken.");
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
            Log.e(LOG,"device location updated, accuracy: " + loc.getAccuracy());

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
//        Log.i(LOG, "## onStart Bind to PhotoUploadService");
//        Intent intent = new Intent(this, PhotoUploadService.class);
//        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
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
        if (googleApiClient != null) {
            googleApiClient.disconnect();
            Log.e(LOG, "### onStop - GoogleApiClient disconnecting ");
        }
        Log.e(LOG, "## onStop unBind from PhotoUploadService");


    }

    Location location;
    static final float ACCURACY_THRESHOLD = 50;
    PictureActivity activity;
    boolean mRequestingLocationUpdates;

    @Override
    public void onConnected(Bundle bundle) {
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
    @RequiresPermission
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
            pics.mkdir();
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

        if (pictureTakenOK) {
            Log.d(LOG, "onBackPressed ... picture cached and scheduled for upload");
            ResponseDTO r = new ResponseDTO();
            Intent i = new Intent();
            i.putExtra("pictureTakenOK", pictureTakenOK);
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
                    main, 300, 400, ScalingUtilities.ScalingLogic.CROP);

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
            Log.e(LOG,"onPostExecute result: " + result.intValue());
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
        }
        //


    }

    ResponseDTO response;
    int pictureCount;
    boolean pictureTakenOK;

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
        PhotoCacheUtil.cachePhoto(ctx, dto, new PhotoCacheUtil.PhotoCacheListener() {
            @Override
            public void onFileDataDeserialized(ResponseDTO response) {

            }

            @Override
            public void onDataCached(PhotoUploadDTO p) {
//                Log.w(LOG, "### photo has been cached");
                Intent a = new Intent(ctx, PhotoUploadService.class);
                a.putExtra("photo", dto);
                startService(a);
//                mService.uploadCachedPhotos(new PhotoUploadService.UploadListener() {
//                    @Override
//                    public void onUploadsComplete(List<PhotoUploadDTO> list) {
//                        Snackbar.make(txtTitle, "Photos uploaded: " + list.size(), Snackbar.LENGTH_LONG);
//                    }
//                });
            }

            @Override
            public void onError() {
                Util.showErrorToast(ctx, getString(R.string.photo_error));
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
        dto.setThumbFlag(1);

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

}
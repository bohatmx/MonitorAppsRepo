package com.boha.monitor.library.activities;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
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
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.boha.monitor.library.R;
import com.boha.monitor.library.dto.CompanyStaffDTO;
import com.boha.monitor.library.dto.ProjectDTO;
import com.boha.monitor.library.dto.ProjectSiteDTO;
import com.boha.monitor.library.dto.ProjectSiteTaskDTO;
import com.boha.monitor.library.dto.VideoClipContainer;
import com.boha.monitor.library.dto.VideoClipDTO;
import com.boha.monitor.library.dto.transfer.PhotoUploadDTO;
import com.boha.monitor.library.dto.transfer.ResponseDTO;
import com.boha.monitor.library.services.PhotoUploadService;
import com.boha.monitor.library.util.CacheVideoUtil;
import com.boha.monitor.library.util.ImageUtil;
import com.boha.monitor.library.util.PMException;
import com.boha.monitor.library.util.PhotoCacheUtil;
import com.boha.monitor.library.util.SharedUtil;
import com.boha.monitor.library.util.Util;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.acra.ACRA;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by aubreyM on 2014/04/21.
 */
public class PictureActivity extends AppCompatActivity implements LocationListener,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    LocationRequest mLocationRequest;
    GoogleApiClient googleApiClient;
    LinearLayout imageContainerLayout;
    LayoutInflater inflater;
    TextView txtProject, txtSite;
    View projectLayout, siteLayout;


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(LOG, "### onCreate............");
        ctx = getApplicationContext();
        inflater = getLayoutInflater();
        setContentView(R.layout.camera);
        setFields();
        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        type = getIntent().getIntExtra("type", 0);
        switch (type) {
            case PhotoUploadDTO.STAFF_IMAGE:
                companyStaff = (CompanyStaffDTO) getIntent().getSerializableExtra("companyStaff");
                if (companyStaff != null) {
                    dispatchTakePictureIntent();
                }
                break;
            case PhotoUploadDTO.PROJECT_IMAGE:
                project = (ProjectDTO) getIntent().getSerializableExtra("project");
                if (project != null) {
                    txtProject.setText(project.getProjectName());
                    siteLayout.setVisibility(View.GONE);
                }
                break;
            case PhotoUploadDTO.SITE_IMAGE:
                projectSite = (ProjectSiteDTO) getIntent().getSerializableExtra("projectSite");
                if (projectSite != null) {
                    txtSite.setText(projectSite.getProjectSiteName());
                    txtProject.setText(projectSite.getProjectName());
                }
                break;
            case PhotoUploadDTO.TASK_IMAGE:
                projectSiteTask = (ProjectSiteTaskDTO) getIntent().getSerializableExtra("projectSiteTask");
                break;
        }






        setTitle(SharedUtil.getCompany(ctx).getCompanyName());
        getSupportActionBar().setSubtitle(SharedUtil.getCompanyStaff(ctx).getFullName());

    }


    @Override
    public void onResume() {
        Log.d(LOG, "@@@ onResume...........");
        super.onResume();

    }

    @Override
    public void onRestoreInstanceState( Bundle savedInstanceState) {
        Log.e(LOG, "%%%%%%%%%%%% onRestoreInstanceState" + savedInstanceState);
        type = savedInstanceState.getInt("type", 0);
        projectSite = (ProjectSiteDTO) savedInstanceState.getSerializable("projectSite");
        project = (ProjectDTO) savedInstanceState.getSerializable("project");
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
        Log.w(LOG, "### saved location accuracy: " + acc);
        super.onRestoreInstanceState(savedInstanceState);
    }

    ImageView imgCamera;
    Button btnStart;


    private void setFields() {
        activity = this;
        txtProject = (TextView) findViewById(R.id.CAM_projectName);
        txtSite = (TextView) findViewById(R.id.CAM_siteName);
        projectLayout = findViewById(R.id.CAM_projectLayout);
        siteLayout = findViewById(R.id.CAM_siteLayout);
        imageContainerLayout = (LinearLayout) findViewById(R.id.CAM_imageContainer);

        btnStart = (Button) findViewById(R.id.CAM_btnStart);
        imgCamera = (ImageView) findViewById(R.id.CAM_imgCamera);
        imgCamera.setVisibility(View.GONE);


        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Util.flashOnce(btnStart, 200, new Util.UtilAnimationListener() {
                    @Override
                    public void onAnimationEnded() {
                        btnStart.setVisibility(View.GONE);
                        dispatchTakePictureIntent();
                    }
                });

            }
        });

        imgCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Util.flashOnce(imgCamera, 200, new Util.UtilAnimationListener() {
                    @Override
                    public void onAnimationEnded() {

                        dispatchTakePictureIntent();
                    }
                });

            }
        });

    }

    @Override
    public void onActivityResult(final int requestCode, final int resultCode,
                                  final Intent data) {
        Log.e(LOG, "##### onActivityResult requestCode: " + requestCode + " resultCode: " + resultCode);
        //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
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

                Uri videoUri = data.getData();
                new FileTask().execute(videoUri);
                //mVideoView.setVideoURI(videoUri);
                break;
        }
    }

    @Override
    public void onLocationChanged( Location loc) {
        Log.d(LOG, "## onLocationChanged accuracy = " + loc.getAccuracy());

        if (this.location == null) {
            this.location = loc;
        }
        if (loc.getAccuracy() <= ACCURACY_THRESHOLD) {
            this.location = loc;
            stopLocationUpdates();
        }
    }

    private void uploadPhotos() {
        Log.e(LOG, "### uploadPhotos, the accuracy: " + location.getAccuracy());
        switch (type) {
            case PhotoUploadDTO.PROJECT_IMAGE:
                addProjectPicture(new CacheListener() {
                    @Override
                    public void onCachingDone() {
                        mService.uploadCachedPhotos(new PhotoUploadService.UploadListener() {
                            @Override
                            public void onUploadsComplete(int count) {
                                Log.i(LOG,"## project picture uploaded");
                            }
                        });
                    }
                });
                break;
            case PhotoUploadDTO.SITE_IMAGE:
                addSitePicture(new CacheListener() {
                    @Override
                    public void onCachingDone() {
                        mService.uploadCachedPhotos(new PhotoUploadService.UploadListener() {
                            @Override
                            public void onUploadsComplete(int count) {
                                Log.i(LOG,"## site picture uploaded");
                            }
                        });
                    }
                });
                break;
            case PhotoUploadDTO.STAFF_IMAGE:
                addStaffPicture(new CacheListener() {
                    @Override
                    public void onCachingDone() {
                        mService.uploadCachedPhotos(new PhotoUploadService.UploadListener() {
                            @Override
                            public void onUploadsComplete(int count) {
                                Log.i(LOG,"## staff picture uploaded");
                            }
                        });
                    }
                });
                break;
            case PhotoUploadDTO.TASK_IMAGE:
                addSiteTaskPicture(new CacheListener() {
                    @Override
                    public void onCachingDone() {
                        mService.uploadCachedPhotos(new PhotoUploadService.UploadListener() {
                            @Override
                            public void onUploadsComplete(int count) {
                                Log.i(LOG,"## task picture uploaded");
                            }
                        });
                    }
                });
                break;

        }
        isUploaded = true;

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
    static final float ACCURACY_THRESHOLD = 20;
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
    public void onConnectionFailed( ConnectionResult connectionResult) {
        ACRA.getErrorReporter().handleSilentException(new PMException(
                "Google LocationClient onConnectionFailed: " + connectionResult.getErrorCode()));
    }


    class FileTask extends AsyncTask<Uri, Void, Integer> {


        @Override
        protected Integer doInBackground(Uri... uris) {
            Uri uri = uris[0];
            VideoClipDTO clip = new VideoClipDTO();
            clip.setUriString(uri.toString());
            File f;
            try {
                f = ImageUtil.getFileFromUri(ctx, uri);
                clip.setFilePath(f.getAbsolutePath());
                clip.setLength(f.length());
            } catch (Exception e) {
                e.printStackTrace();
            }

            return 0;
        }
    }

    private void dispatchTakeVideoIntent() {
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
            case PhotoUploadDTO.SITE_IMAGE:
                imageFileName = projectSite.getProjectName() + "-" + projectSite.getProjectSiteName();
                break;
            case PhotoUploadDTO.STAFF_IMAGE:
                imageFileName = companyStaff.getFullName();
                break;
            case PhotoUploadDTO.TASK_IMAGE:
                imageFileName = projectSiteTask.getProjectName() + "-" + projectSiteTask.getProjectSiteName() + "_" + projectSiteTask.getTask().getTaskName();
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
    public boolean onOptionsItemSelected( MenuItem item) {

        if (item.getItemId() == R.id.menu_video) {
            dispatchTakeVideoIntent();
            return true;
        }
        if (item.getItemId() == R.id.menu_gallery) {
            //Intent i = new Intent(this, PictureRecyclerGridActivity.class);
            //startActivity(i);
            Util.showToast(ctx, ctx.getString(R.string.under_cons));
            return true;
        }
        if (item.getItemId() == R.id.menu_camera) {
            dispatchTakePictureIntent();
            return true;
        }

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
            r.setImageFileNameList(currentSessionPhotos);
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
    public void onSaveInstanceState( Bundle b) {
        Log.e(LOG, "############################## onSaveInstanceState");
        b.putInt("type", type);
        if (currentThumbFile != null) {
            b.putString("thumbPath", currentThumbFile.getAbsolutePath());
        }
        if (photoFile != null) {
            b.putString("photoFile", photoFile.getAbsolutePath());
        }
        if (projectSite != null) {
            b.putSerializable("projectSite", projectSite);
        }
        if (project != null) {
            b.putSerializable("project", project);
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
                        matrixThumbnail.postScale(0.4f, 0.4f);
                        Bitmap thumb = Bitmap.createBitmap
                                (bm, 0, 0, bm.getWidth(),
                                        bm.getHeight(), matrixThumbnail, true);
                        getLog(thumb, "Thumb");

                        //append date and gps coords to bitmap
                        //fullBm = ImageUtil.drawTextToBitmap(ctx,fullBm,location);
                        //thumb = ImageUtil.drawTextToBitmap(ctx,thumb,location);

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
                Util.showErrorToast(ctx, ctx.getResources().getString(R.string.camera_error));
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

    private void addImageToScroller() {
        Log.i(LOG, "## addImageToScroller");
        if (currentSessionPhotos.size() == 1) {
            imageContainerLayout.removeAllViews();
        }
        View v = inflater.inflate(R.layout.scroller_image_template, null);
        ImageView img = (ImageView) v.findViewById(R.id.image);
        TextView num = (TextView) v.findViewById(R.id.number);
        num.setText("" + currentSessionPhotos.size());
        final Uri uri = Uri.fromFile(currentThumbFile);
        ImageLoader.getInstance().displayImage(uri.toString(), img);
        img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (type == PhotoUploadDTO.STAFF_IMAGE) {
                    currentSessionPhotos.clear();
                    currentSessionPhotos.add(uri.toString());
                    onBackPressed();
                }
            }
        });
        imageContainerLayout.addView(v, 0);

        btnStart.setVisibility(View.GONE);
        imgCamera.setVisibility(View.VISIBLE);
        uploadPhotos();
        isUploaded = true;

        if (type == PhotoUploadDTO.STAFF_IMAGE) {
            Util.showToast(ctx, "Tap picture to select staff profile picture");
        }


    }


    List<String> currentSessionPhotos = new ArrayList<>();

    private void getLog( Bitmap bm, String which) {
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
                public void onUploadsComplete(int count) {
                    Log.w(LOG, "$$$ onUploadsComplete, list: " + count);
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
    ProjectSiteDTO projectSite;
    ProjectSiteTaskDTO projectSiteTask;
    CompanyStaffDTO companyStaff;

    File photoFile;
    private VideoClipContainer vcc;
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


    public void addSitePicture( final CacheListener listener) {
        Log.w(LOG, "**** addSitePicture .......");
        final PhotoUploadDTO dto = getObject();
        dto.setProjectID(projectSite.getProjectID());
        dto.setProjectSiteID(projectSite.getProjectSiteID());
        dto.setPictureType(PhotoUploadDTO.SITE_IMAGE);
        dto.setAccuracy(location.getAccuracy());
        dto.setThumbFilePath(currentThumbFile.getAbsolutePath());
        Log.w(LOG, "**** addPhotoToCache starting ... file: " + dto.getThumbFilePath());
        PhotoCacheUtil.cachePhoto(ctx, dto, new PhotoCacheUtil.PhotoCacheListener() {
            @Override
            public void onFileDataDeserialized(ResponseDTO response) {

            }

            @Override
            public void onDataCached() {
                Log.w(LOG, "### photo has been cached");
                listener.onCachingDone();

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

    public void addSiteTaskPicture( final CacheListener listener) {
        Log.w(LOG, "**** addSiteTaskPicture");
        final PhotoUploadDTO dto = getObject();
        dto.setProjectID(projectSiteTask.getProjectID());
        dto.setProjectSiteID(projectSiteTask.getProjectSiteID());
        dto.setProjectSiteTaskID(projectSiteTask.getProjectSiteTaskID());
        dto.setPictureType(PhotoUploadDTO.TASK_IMAGE);
        dto.setAccuracy(location.getAccuracy());
        dto.setThumbFilePath(currentThumbFile.getAbsolutePath());
        PhotoCacheUtil.cachePhoto(ctx, dto, new PhotoCacheUtil.PhotoCacheListener() {
            @Override
            public void onFileDataDeserialized(ResponseDTO response) {

            }

            @Override
            public void onDataCached() {
                Log.w(LOG, "### photo has been cached");
                listener.onCachingDone();
            }

            @Override
            public void onError() {
                Util.showErrorToast(ctx, getString(R.string.photo_error));
            }
        });

    }

    public void addProjectPicture( final CacheListener listener) {
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
            public void onDataCached() {
                Log.w(LOG, "### photo has been cached");
                listener.onCachingDone();
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
        dto.setCompanyStaffID(SharedUtil.getCompanyStaff(ctx).getCompanyStaffID());
        dto.setThumbFilePath(currentThumbFile.getAbsolutePath());
        dto.setThumbFlag(1);
        dto.setDateTaken(new Date().getTime());
        dto.setLatitude(location.getLatitude());
        dto.setLongitude(location.getLongitude());
        dto.setAccuracy(location.getAccuracy());
        dto.setTime(new Date().getTime());
        return dto;
    }


    public void addStaffPicture( final CacheListener listener) {
        final PhotoUploadDTO dto = getObject();
        dto.setCompanyStaffID(companyStaff.getCompanyStaffID());
        dto.setPictureType(PhotoUploadDTO.STAFF_IMAGE);
        dto.setThumbFilePath(currentThumbFile.getAbsolutePath());
        dto.setStaffPicture(true);
        PhotoCacheUtil.cachePhoto(ctx, dto, new PhotoCacheUtil.PhotoCacheListener() {
            @Override
            public void onFileDataDeserialized(ResponseDTO response) {

            }

            @Override
            public void onDataCached() {
                Log.w(LOG, "### photo has been cached :)");
                listener.onCachingDone();
            }

            @Override
            public void onError() {
                Util.showErrorToast(ctx, getString(R.string.photo_error));
            }
        });
    }

}
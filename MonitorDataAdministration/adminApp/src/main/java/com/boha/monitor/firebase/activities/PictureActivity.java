package com.boha.monitor.firebase.activities;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.boha.monitor.firebase.R;
import com.boha.monitor.firebase.data.PhotoUploadDTO;
import com.boha.monitor.firebase.data.ProjectDTO;
import com.boha.monitor.firebase.data.UserDTO;
import com.boha.monitor.firebase.util.Constants;
import com.boha.monitor.firebase.util.ImageUtil;
import com.boha.monitor.firebase.util.StorageUtil;
import com.boha.monitor.firebase.util.Util;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class PictureActivity extends AppCompatActivity implements LocationListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    Location mLocation;
    GoogleApiClient mGoogleApiClient;
    LocationRequest mLocationRequest;
    boolean mRequestingLocationUpdates;

    String mCurrentPhotoPath;
    static final int REQUEST_VIDEO_CAPTURE = 162, CAPTURE_IMAGE = 766;
    File photoFile;
    Uri fileUri;
    Snackbar snackbar;
    File currentThumbFile, currentFullFile;
    Uri thumbUri, fullUri;
    Bitmap bitmapForScreen;
    ImageView upload, camera, image;
    TextView title;

    UserDTO user;
    ProjectDTO project;
    int type;
    InterstitialAd mInterstitialAd;
    boolean showAd = true;
    ProgressBar progressBar;

    public static final int USER = 1, PROJECT = 2;

    static final String TAG = PictureActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pictures);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        user = (UserDTO)getIntent().getSerializableExtra("user");
        project = (ProjectDTO)getIntent().getSerializableExtra("project");
        if (user != null) {
            type = USER;
        }
        if (project != null) {
            type = PROJECT;
        }

        setFields();

        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId(Constants.INTERSTITIAL_AD);
        requestNewAd();
    }

    private void requestNewAd() {
        AdRequest request = new AdRequest.Builder()
                .addTestDevice("3E:F3:AA:D7:B9:B4:71:22:A7:F0:CD:4F:89:74:84:6A:92:8C:99:E0")
                .build();
        mInterstitialAd.loadAd(request);
        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                showAd = false;
                onBackPressed();
            }
        });
    }
    @Override
    public void onBackPressed() {
        if (showAd) {
            if (mInterstitialAd.isLoaded()) {
                mInterstitialAd.show();
                return;
            }
        }
        finish();
    }

    private void setFields() {
        progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        progressBar.setVisibility(View.GONE);

        image = (ImageView) findViewById(R.id.image);
        camera = (ImageView) findViewById(R.id.takePicture);
        upload = (ImageView) findViewById(R.id.upload);
        title = (TextView) findViewById(R.id.title);
        switch (type) {
            case USER:
                title.setText(user.getFullName());
                break;
            case PROJECT:
                title.setText(project.getProjectName());
                break;
        }
        camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchTakePictureIntent();
            }
        });
        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadPhoto();
            }
        });

    }


    private void uploadPhoto() {
        if (currentThumbFile == null) {
            Snackbar.make(image,"No image to upload", Snackbar.LENGTH_SHORT).show();
            return;
        }
        progressBar.setVisibility(View.VISIBLE);
        PhotoUploadDTO p = new PhotoUploadDTO();
        p.setDateTaken(new Date().getTime());
        p.setMarked(false);
        p.setLatitude(mLocation.getLatitude());
        p.setLongitude(mLocation.getLongitude());
        p.setDateUploaded(new Date().getTime());
        p.setFilePath(currentThumbFile.getPath());
        switch (type) {
            case USER:
                p.setUserID(user.getUserID());
                p.setMonitorName(user.getFullName());
                p.setCompanyID(user.getCompanyID());
                p.setBucketName("bucket-users");
                StorageUtil.uploadUserPhoto(user,p, new StorageUtil.StorageListener() {
                    @Override
                    public void onUploaded(String key) {
                        Log.e(TAG, "onUploaded: heita!" + key);
                        showSnack("User photo uploaded");
                        progressBar.setVisibility(View.GONE);
                    }

                    @Override
                    public void onError(String message) {
                        Log.e(TAG, "onError: " + message );
                        progressBar.setVisibility(View.GONE);
                        showSnack(message);
                    }
                });
                break;
            case PROJECT:
                p.setProjectID(project.getProjectID());
                p.setProjectName(project.getProjectName());
                p.setCompanyID(project.getCompanyID());
                p.setBucketName("bucket" + project.getProjectID());
                StorageUtil.uploadProjectPhoto(p, new StorageUtil.StorageListener() {
                    @Override
                    public void onUploaded(String key) {
                        Log.e(TAG, "onUploaded: heita!" + key);
                        showSnack("Project photo uploaded");
                        progressBar.setVisibility(View.GONE);
                    }

                    @Override
                    public void onError(String message) {
                        Log.e(TAG, "onError: " + message );
                        showSnack(message);
                        progressBar.setVisibility(View.GONE);
                    }
                });
                break;
        }

    }
    private void showSnack(String message) {
        snackbar = Snackbar.make(image,message, Snackbar.LENGTH_INDEFINITE);
        snackbar.setActionTextColor(ContextCompat.getColor(getApplicationContext(), R.color.green_200));
        snackbar.setAction("Cool", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                snackbar.dismiss();
            }
        });
        snackbar.show();
    }
    @Override
    public void onActivityResult(final int requestCode, final int resultCode,
                                 final Intent data) {
        switch (requestCode) {
            case CAPTURE_IMAGE:
                if (resultCode == Activity.RESULT_OK) {
                    if (resultCode == Activity.RESULT_OK) {
                        new PhotoTask().execute();
                    }
                }
                break;
            case REQUEST_VIDEO_CAPTURE:
                Uri videoUri = data.getData();
                //todo - get video
                break;
        }
    }

    private void dispatchTakeVideoIntent() {
        final Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        startActivityForResult(takeVideoIntent, REQUEST_VIDEO_CAPTURE);

    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a addMonitors activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                Log.e(TAG, "Fuck!", ex);
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                        Uri.fromFile(photoFile));
                startActivityForResult(takePictureIntent, CAPTURE_IMAGE);
            }
        }
    }


    private File createImageFile() throws IOException {
        // Create an image file name

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "MONITOR_" + timeStamp;
        File storageDir;
        if (Util.hasStorage(true)) {
            Log.i(TAG, "###### get file from getExternalStoragePublicDirectory");
            storageDir = Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_PICTURES);
        } else {
            Log.i(TAG, "###### get file from getDataDirectory");
            storageDir = Environment.getDataDirectory();
        }

        File image = File.createTempFile(imageFileName,".jpg",storageDir );
        mCurrentPhotoPath = "file:" + image.getAbsolutePath();
        return image;
    }

    class PhotoTask extends AsyncTask<Void, Void, Integer> {

        static final int MINIMUM = 1024 * 1024 * 1;
        @Override
        protected Integer doInBackground(Void... voids) {
            Log.e("TAG", " file length: " + photoFile.length());

            if (photoFile.length() < MINIMUM) {
                currentFullFile = photoFile;
                currentThumbFile = photoFile;
                thumbUri = Uri.fromFile(currentThumbFile);
                fullUri = Uri.fromFile(currentFullFile);
                return 0;
            }
            fileUri = Uri.fromFile(photoFile);
            if (fileUri != null) {
                try {

                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inSampleSize = 2;
                    Bitmap bm = BitmapFactory.decodeFile(photoFile.getAbsolutePath(), options);
                    getLog(bm, "Raw Camera");

                    //scale and rotate for the screen
                    Matrix matrix = new Matrix();
                    matrix.postScale(1.0f, 1.0f);
                    bitmapForScreen = Bitmap.createBitmap
                            (bm, 0, 0, bm.getWidth(), bm.getHeight(), matrix, true);

                    //get thumbnail for upload
                    Matrix matrixThumbnail = new Matrix();
                    matrixThumbnail.postScale(0.4f, 0.4f);
                    //matrixThumbnail.postRotate(rotate);
                    Bitmap thumb = Bitmap.createBitmap
                            (bitmapForScreen, 0, 0, bitmapForScreen.getWidth(),
                                    bitmapForScreen.getHeight(), matrixThumbnail, true);
                    getLog(thumb, "Thumb");


                    Matrix matrixF = new Matrix();
                    matrixF.postScale(0.6f, 0.6f);
                    Bitmap fullBm = Bitmap.createBitmap
                            (bitmapForScreen, 0, 0, bitmapForScreen.getWidth(),
                                    bitmapForScreen.getHeight(), matrixF, true);
                    //getLog(fullBm, "Full");
                    currentFullFile = ImageUtil.getFileFromBitmap(fullBm, "m" + System.currentTimeMillis() + ".jpg");
                    currentThumbFile = ImageUtil.getFileFromBitmap(thumb, "t" + System.currentTimeMillis() + ".jpg");
                    thumbUri = Uri.fromFile(currentThumbFile);
                    fullUri = Uri.fromFile(currentFullFile);
                    getFileLengths();


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
                snackbar = Snackbar.make(image, "Unable to process file from addMonitors", Snackbar.LENGTH_INDEFINITE);
                snackbar.setAction("Not Cool", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        snackbar.dismiss();
                    }
                });
                snackbar.setActionTextColor(ContextCompat.getColor(getApplicationContext(), R.color.red_500));
                snackbar.show();

                return;
            }
            image.setImageBitmap(bitmapForScreen);

        }
    }

    private void getLog(Bitmap bm, String which) {
        Log.e(TAG, which + " - bitmap: width: "
                + bm.getWidth() + " height: "
                + bm.getHeight() + " rowBytes: "
                + bm.getRowBytes());
    }

    private void getFileLengths() {
        Log.i(TAG, "Thumbnail file length: " + currentThumbFile.length());
        Log.i(TAG, "Full file length: " + currentFullFile.length());

    }

    @Override
    public void onStart() {
        super.onStart();
        Log.w(TAG, "######### onStart");
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
            progressBar.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "######### onStop");
        if (mGoogleApiClient != null) {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //getMenuInflater().inflate(R.menu.gps_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

//        if (id == R.id.action_settings) {
//            return true;
//        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.i(TAG,
                "+++  onConnected() -  requestLocationUpdates ...");
        mLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);
        if (mLocation != null) {
            Log.w(TAG, "## requesting location updates ....lastLocation: "
                    + mLocation.getLatitude() + " "
                    + mLocation.getLongitude() + " acc: "
                    + mLocation.getAccuracy());
        }
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setInterval(2000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setFastestInterval(1000);

        startLocationUpdates();

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    static final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;

    public void startLocationUpdates() {
        int permissionCheck = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
            return;
        }

        Log.w(TAG, "###### startLocationUpdates: " + new Date().toString());
        if (mGoogleApiClient.isConnected()) {
            mRequestingLocationUpdates = true;
            LocationServices.FusedLocationApi.requestLocationUpdates(
                    mGoogleApiClient, mLocationRequest, this);
            progressBar.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startLocationUpdates();

                } else {
                    throw new UnsupportedOperationException();
                }
                return;
            }
        }
    }

    protected void stopLocationUpdates() {
        Log.w(TAG, "###### stopLocationUpdates - " + new Date().toString());
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(
                    mGoogleApiClient, this);
            progressBar.setVisibility(View.GONE);
        }
    }


    static final float ACCURACY = 10;

    @Override
    public void onLocationChanged(Location loc) {
        Log.d(TAG, "## onLocationChanged accuracy = " + loc.getAccuracy()
                + " - " + new Date().toString());
        mLocation = loc;
        if (loc.getAccuracy() > ACCURACY) {
            return;
        }
        stopLocationUpdates();


    }
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }
}

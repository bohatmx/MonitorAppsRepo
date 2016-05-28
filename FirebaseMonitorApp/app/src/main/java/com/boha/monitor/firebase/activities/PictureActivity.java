package com.boha.monitor.firebase.activities;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.boha.monitor.firebase.R;
import com.boha.monitor.firebase.dto.PhotoUploadDTO;
import com.boha.monitor.firebase.dto.UserDTO;
import com.boha.monitor.firebase.util.ImageUtil;
import com.boha.monitor.firebase.util.StorageUtil;
import com.boha.monitor.firebase.util.Util;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class PictureActivity extends AppCompatActivity {

    String mCurrentPhotoPath;
    static final int REQUEST_VIDEO_CAPTURE = 162, CAPTURE_IMAGE = 766;
    File photoFile;
    ImageView image;
    FloatingActionButton fab;
    Uri fileUri;
    Snackbar snackbar;
    File currentThumbFile, currentFullFile;
    Uri thumbUri, fullUri;
    Bitmap bitmapForScreen;
    FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    static final String TAG = PictureActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picture);
//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);

        mAuth = FirebaseAuth.getInstance();
        checkStatus();
        image = (ImageView) findViewById(R.id.image);
        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dispatchTakePictureIntent();
            }
        });
    }
    private void checkStatus() {
        Log.i(TAG, "checkStatus: ---------- check Firebase user log in");
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    Log.i(TAG, "++++++++++++++ onAuthStateChanged:signed_in:" + user.getUid()
                            + " " + user.getEmail());
                    UserDTO x = new UserDTO();
                    x.setEmail(user.getEmail());
                    x.setUserID(user.getUid());
                    Log.w(TAG, "****************** - onAuthStateChanged: remember to save this app user somewhere");

                } else {
                    // User is signed out
                    Log.d(TAG, "-----------onAuthStateChanged:signed_out - start sign in");
                    signIn();
                }

            }
        };

        mAuth.addAuthStateListener(mAuthListener);

    }
    private void signIn() {
        Log.w(TAG, "signIn: ================ Firebase signin");
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        String email = "aubrey@mlab.co.za";
        String password = "kktiger3";

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.i(TAG, "####### signIn: onComplete: " + task.isSuccessful());

                        if (task.isSuccessful()) {
                            FirebaseUser user = task.getResult().getUser();
                            Log.i(TAG, "####### onComplete: we cool, name: "
                                    + user.getDisplayName() + " email: " + user.getEmail()
                                    + " uid: " + user.getUid() + " \ntoken: " + user.getToken(true));
                        } else {
                            Log.e(TAG, "------------ sign in FAILED");
                        }
                    }
                });
    }

    private void uploadPhoto() {
        PhotoUploadDTO p = new PhotoUploadDTO();
        p.setBucketName("mybucket_list");
        p.setDateTaken(new Date().getTime());
        p.setUserID("-KIP8VGt3zCwGie7mVgv");
        p.setMarked(false);
        p.setProjectID("-KIOekBDdxNcRCWK3kjk");
        p.setDateUploaded(new Date().getTime());
        p.setMonitorName("Peggy Monitor");
        p.setFilePath(currentThumbFile.getPath());
        p.setCompanyID("-KIOek3aRAC8Xvi3BTki");
        StorageUtil.uploadProjectPhoto(p, new StorageUtil.StorageListener() {
            @Override
            public void onUploaded(String key) {
                Log.e(TAG, "onUploaded: heita!" + key);
            }

            @Override
            public void onError(String message) {
                Log.e(TAG, "onError: " + message );
            }
        });
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
        // Ensure that there's a camera activity to handle the intent
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

        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
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
                snackbar = Snackbar.make(fab, "Unable to process file from camera", Snackbar.LENGTH_INDEFINITE);
                snackbar.setAction("OK", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        snackbar.dismiss();
                    }
                });
                return;
            }
            image.setImageBitmap(bitmapForScreen);
            uploadPhoto();

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


}

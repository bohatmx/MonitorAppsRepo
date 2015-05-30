package com.boha.monitor.library.activities;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.boha.monitor.library.R;
import com.boha.monitor.library.dto.CompanyStaffDTO;
import com.boha.monitor.library.dto.transfer.PhotoUploadDTO;
import com.boha.monitor.library.dto.transfer.ResponseDTO;
import com.boha.monitor.library.services.PhotoUploadService;
import com.boha.monitor.library.util.ImageUtil;
import com.boha.monitor.library.util.PhotoCacheUtil;
import com.boha.monitor.library.util.SharedUtil;
import com.boha.monitor.library.util.Util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

public class StaffPictureActivity extends AppCompatActivity {

    Button btnSave;
    ImageView image;
    CompanyStaffDTO companyStaff;
    Context ctx;

    File photoFile;
    ProgressBar progressBar;
    static final String LOG = StaffPictureActivity.class.getSimpleName();
    public static final int CAPTURE_IMAGE = 9509;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_staff_picture);
        ctx = getApplicationContext();
        image = (ImageView) findViewById(R.id.SPIC_image);
        progressBar = (ProgressBar)findViewById(R.id.progressBar);
        progressBar.setVisibility(View.GONE);
        btnSave = (Button) findViewById(R.id.SPIC_btn);
        btnSave.setVisibility(View.GONE);

        companyStaff = (CompanyStaffDTO)getIntent().getSerializableExtra("companyStaff");
        image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchTakePictureIntent();
            }
        });
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadPhoto();
            }
        });
        dispatchTakePictureIntent();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_staff_picture, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected( MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_help) {
            Util.showToast(ctx, getString(R.string.under_cons));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }



    Uri fileUri, thumbUri;

    File currentThumbFile;
    @Override
    public void onActivityResult(final int requestCode, final int resultCode,
                                 final Intent data) {
        Log.e(LOG, "##### onActivityResult requestCode: " + requestCode + " resultCode: " + resultCode);
        switch (requestCode) {
            case CAPTURE_IMAGE:
                if (resultCode == Activity.RESULT_OK) {
                    if (resultCode == Activity.RESULT_OK) {
                        if (photoFile != null) {
                            Log.e(LOG, "++ hopefully photo file has a length: "
                                    + photoFile.length());
                            new PhotoTask().execute();
                        }
                    }

                }
                break;

        }
    }

    class PhotoTask extends AsyncTask<Void, Void, Integer> {


        @Override
        protected Integer doInBackground(Void... voids) {
            Log.w(LOG, "## PhotoTask starting doInBackground, file length: " + photoFile.length());
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
                        //Util.writeLocationToExif(currentThumbFile.getAbsolutePath(), location);
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
                image.setImageBitmap(bitmapForScreen);
                btnSave.setVisibility(View.VISIBLE);

            }
        }
    }


    private void uploadPhoto() {
        Log.e(LOG, "### uploadPhoto");
        progressBar.setVisibility(View.VISIBLE);
        addStaffPicture(new CacheListener() {
            @Override
            public void onCachingDone() {
                if (mBound) {
                    mService.uploadCachedPhotos(new PhotoUploadService.UploadListener() {
                        @Override
                        public void onUploadsComplete(int count) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    progressBar.setVisibility(View.GONE);
                                    Log.i(LOG, "####### staff picture uploaded");
                                    isUploaded = true;
                                    onBackPressed();
                                }
                            });

                        }
                    });
                }
            }
        });


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


    private PhotoUploadDTO getObject() {
        PhotoUploadDTO dto = new PhotoUploadDTO();
        dto.setCompanyID(SharedUtil.getCompany(ctx).getCompanyID());
        dto.setCompanyStaffID(SharedUtil.getCompanyStaff(ctx).getCompanyStaffID());
        dto.setThumbFilePath(currentThumbFile.getAbsolutePath());
        dto.setThumbFlag(1);
        dto.setDateTaken(new Date().getTime());

        dto.setTime(new Date().getTime());
        return dto;
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
        String imageFileName = "pic" + System.currentTimeMillis();
        imageFileName = companyStaff.getFullName();
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

        mCurrentPhotoPath = "file:" + image.getAbsolutePath();
        return image;
    }
    private void getLog( Bitmap bm, String which) {
        if (bm == null) return;
        Log.e(LOG, which + " - bitmap: width: "
                + bm.getWidth() + " height: "
                + bm.getHeight() + " rowBytes: "
                + bm.getRowBytes());
    }

    String mCurrentPhotoPath;
    Bitmap bitmapForScreen;
    boolean isUploaded;

    private interface CacheListener {
        public void onCachingDone();
    }

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
    @Override
    public void onBackPressed() {

        if (isUploaded) {
            Log.d(LOG, "onBackPressed ... picture uploaded");
            ResponseDTO r = new ResponseDTO();
            r.setImageFileNameList(new ArrayList<String>());
            r.getImageFileNameList().add(currentThumbFile.getAbsolutePath());
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
    protected void onStart() {
        super.onStart();
        Log.w(LOG, "## onStart Bind to PhotoUploadService");
        Intent intent = new Intent(this, PhotoUploadService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);


    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.e(LOG, "## onStop unBind from PhotoUploadService");
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }

    }


    boolean mBound;
}

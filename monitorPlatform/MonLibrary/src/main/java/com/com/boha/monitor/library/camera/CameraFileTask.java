package com.com.boha.monitor.library.camera;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.location.Location;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.com.boha.monitor.library.util.ImageUtil;

import java.io.File;

/**
 * Created by aubreyM on 14/12/17.
 */
public class CameraFileTask {
    static final String LOG = CameraFileTask.class.getSimpleName();
    static Context ctx;
    static File photoFile, currentFullFile, currentThumbFile;
    static Uri fileUri;
    static CameraFileTaskListener listener;
    static Location location;

    public interface CameraFileTaskListener {
        public void onCameraFilesResized(File fullFile, File thumbFile);
        public void onError(String message);
    }

    public static void processCapturedFile(Context context, File file, Location loc, CameraFileTaskListener l) {
        ctx = context;
        listener = l;
        photoFile = file;
        location = loc;

        new PhotoTask().execute();
    }
    static class PhotoTask extends AsyncTask<Void, Void, Integer> {

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
                    try {
                        BitmapFactory.Options options = new BitmapFactory.Options();
                        options.inSampleSize = 2;
                        Bitmap bm = BitmapFactory.decodeFile(photoFile.getAbsolutePath(), options);
                        if (bm == null) {
                            Log.e(LOG, "---> Bitmap is null, file length: " + photoFile.length());
                        }
                        getLog(bm, "Raw Camera");
                        //get thumbnail for upload
                        Matrix matrixThumbnail = new Matrix();
                        matrixThumbnail.postScale(0.4f, 0.4f);
                        //matrixThumbnail.postRotate(rotate);
                        Bitmap thumb = Bitmap.createBitmap
                                (bm, 0, 0, bm.getWidth(),
                                        bm.getHeight(), matrixThumbnail, true);
                        getLog(thumb, "Thumb");

                        Matrix matrixF = new Matrix();
                        matrixF.postScale(0.75f, 0.75f);
                        //matrixF.postRotate(rotate);
                        Bitmap fullBm = Bitmap.createBitmap
                                (bm, 0, 0, bm.getWidth(),
                                        bm.getHeight(), matrixF, true);

                        getLog(fullBm, "Full");
                        //append date and gps coords to bitmap
                        //fullBm = ImageUtil.drawTextToBitmap(ctx,fullBm,location);
                        //thumb = ImageUtil.drawTextToBitmap(ctx,thumb,location);

                        currentFullFile = ImageUtil.getFileFromBitmap(fullBm, "m" + System.currentTimeMillis() + ".jpg");
                        currentThumbFile = ImageUtil.getFileFromBitmap(thumb, "t" + System.currentTimeMillis() + ".jpg");
                        Log.e(LOG, "## files created OK, from camera bitmap");

                        //write exif data
                        //Util.writeLocationToExif(currentFullFile.getAbsolutePath(), location);
                        //Util.writeLocationToExif(currentThumbFile.getAbsolutePath(), location);

                        getFileLengths();
                    } catch (Exception e) {
                        Log.e(LOG, "Fuck it! unable to process the bleeding bitmap", e);
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
                listener.onError("Unable to process image file: result: " + result);
                return;
            }
            listener.onCameraFilesResized(currentFullFile,currentThumbFile);
        }
    }

    private static void getLog(Bitmap bm, String which) {
        if (bm == null) return;
        Log.e(LOG, which + " - bitmap: width: "
                + bm.getWidth() + " height: "
                + bm.getHeight() + " rowBytes: "
                + bm.getRowBytes());
    }

    private static void getFileLengths() {
        Log.i(LOG, "Thumbnail file length: " + currentThumbFile.length());
        Log.i(LOG, "Full file length: " + currentFullFile.length());

    }


}

package com.boha.monitor.library.util;

/**
 * Created by aubreymalabie on 3/12/16.
 */

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import org.apache.commons.io.FileUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.zip.GZIPOutputStream;


public class Zipper {
    private static final int BUFFER = 2048;



    static Context ctx;
    static ZipperListener zipperListener;
    static final DecimalFormat df = new DecimalFormat("###,###,###,###,###.0");
    static final String LOG = Zipper.class.getSimpleName();
    static final SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy-HH-mm-ss");

    public interface ZipperListener {
        void onZipped(File zipfFile);
        void onError(String message);
    }

    public static void compressFile(Context context, File file, ZipperListener listener) {
        zipperListener = listener;
        ctx = context;
        new ZTask().execute(file);
    }
    private static class ZTask extends AsyncTask<File, Void, File> {

        @Override
        protected File doInBackground(File... params) {
            File inFile = params[0];
            File zipFile = null;
            try {
                zipFile = pack(inFile);
                pack2(inFile);

            } catch (IOException e) {
                return null;
            }
            return zipFile;
        }
        @Override
        protected void onPostExecute(File zipFile) {
            if (zipFile == null) {
                zipperListener.onError("Unable to complete the compression");
            } else {
                zipperListener.onZipped(zipFile);
            }
        }

        protected File pack(File file) throws IOException {
            Log.d(LOG, "*** pack file: " + file.getAbsolutePath()
                    + " length: " + getLength(file.length()));
            File out = getZipFile(ctx);
            String xx = FileUtils.readFileToString(file);
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            GZIPOutputStream gos = new GZIPOutputStream(os);
            gos.write(xx.getBytes());
            gos.close();
            byte[] compressed = os.toByteArray();
            os.close();
            FileUtils.writeByteArrayToFile(out, compressed);
            Log.e(LOG, "*** File packed1, " + out.getAbsolutePath()
                    + " length: " + getLength(out.length()));
            return out;
        }
        protected File pack2(File file) throws IOException {
            Log.d(LOG, "*** pack file: " + file.getAbsolutePath()
                    + " length: " + getLength(file.length()));
            File out = getZipFile(ctx);
            String xx = FileUtils.readFileToString(file);
            //ByteArrayOutputStream os = new ByteArrayOutputStream();
            FileOutputStream fos = new FileOutputStream(out);
            GZIPOutputStream gos = new GZIPOutputStream(fos);
            gos.write(xx.getBytes());
            gos.close();
            fos.close();
            Log.w(LOG, "*** File packed2, " + out.getAbsolutePath()
                    + " length: " + getLength(out.length()));
            return out;
        }
        private  File getZipFile(Context ctx) throws IOException {
            if (ContextCompat.checkSelfPermission(ctx,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                Log.d(LOG, "WRITE_EXTERNAL_STORAGE permission not granted yet");
                throw new IOException();
            }

            String imageFileName = "vid_" + sdf.format(new Date()) + ".zip";
            File root;
            if (Util.hasStorage(true)) {
                Log.i(LOG, "###### get file from getExternalStoragePublicDirectory");
                root = Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_PICTURES);
            } else {
                Log.i(LOG, "###### get file from getDataDirectory");
                root = Environment.getDataDirectory();
            }
            File dir = new File(root, "monitor");
            if (!dir.exists()) {
                dir.mkdir();
            }

            File videoFile = new File(dir, imageFileName);
            Log.w(LOG, "Empty Video zip file has been created: " + videoFile.getAbsolutePath()
                    + " size: " + getLength(videoFile.length()));

            return videoFile;

        }

        private  String getLength(long length) {
            Double d = Double.parseDouble("" + length) / (1024);
            return df.format(d) + " KB";

        }

    }
}


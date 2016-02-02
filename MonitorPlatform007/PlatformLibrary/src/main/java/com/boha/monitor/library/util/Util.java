package com.boha.monitor.library.util;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v7.app.ActionBar;
import android.text.Html;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.OvershootInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListPopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.boha.monitor.library.adapters.PopupListAdapter;
import com.boha.monitor.library.dto.ProjectDTO;
import com.boha.monitor.library.dto.RequestDTO;
import com.boha.monitor.library.dto.ResponseDTO;
import com.boha.monitor.library.dto.StaffDTO;
import com.boha.platform.library.R;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Grab bag of static helper methods for all sorts of things.
 * Mostly, the method names speak for themselves
 */
public class Util {
    @SuppressLint("NewApi")
    public static String getRealPathFromURI_API19(Context context, Uri uri) {
        String filePath = "";
        String wholeID = DocumentsContract.getDocumentId(uri);

        // Split at colon, use second item in the array
        String id = wholeID.split(":")[1];

        String[] column = {MediaStore.Images.Media.DATA};

        // where id is equal to
        String sel = MediaStore.Images.Media._ID + "=?";

        Cursor cursor = context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                column, sel, new String[]{id}, null);

        int columnIndex = cursor.getColumnIndex(column[0]);

        if (cursor.moveToFirst()) {
            filePath = cursor.getString(columnIndex);
        }
        cursor.close();
        return filePath;
    }


    @SuppressLint("NewApi")
    public static String getRealPathFromURI_API11to18(Context context, Uri contentUri) {
        String[] proj = {MediaStore.Images.Media.DATA};
        String result = null;

        CursorLoader cursorLoader = new CursorLoader(
                context,
                contentUri, proj, null, null, null);
        Cursor cursor = cursorLoader.loadInBackground();

        if (cursor != null) {
            int column_index =
                    cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            result = cursor.getString(column_index);
        }
        return result;
    }

    public static String getRealPathFromURI_BelowAPI11(Context context, Uri contentUri) {
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = context.getContentResolver().query(contentUri, proj, null, null, null);
        int column_index
                = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }

    public static String getAnotherHash(String hashMe) throws NoSuchAlgorithmException {
        MessageDigest md = null;
        md = MessageDigest.getInstance("SHA-512");
        md.update(hashMe.getBytes());
        byte byteData[] = md.digest();
        String base64 = Base64.encodeToString(byteData, Base64.NO_WRAP);

        return base64;
    }

    public static String getHash(String hashMe) throws NoSuchAlgorithmException {

        MessageDigest md = MessageDigest.getInstance("SHA-512");
        md.update(hashMe.getBytes());

        byte byteData[] = md.digest();

        //convert the byte to hex format method 1
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < byteData.length; i++) {
            sb.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16).substring(1));

        }


        System.out.println("Hex format : " + sb.toString());

        //convert the byte to hex format method 2
        StringBuffer hexString = new StringBuffer();
        for (int i = 0; i < byteData.length; i++) {
            String hex = Integer.toHexString(0xff & byteData[i]);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        System.out.println("Hex format : " + hexString.toString());
        return sb.toString();
    }

    public static Bitmap drawableToBitmap(Drawable drawable) {
        Bitmap bitmap = null;

        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            if (bitmapDrawable.getBitmap() != null) {
                return bitmapDrawable.getBitmap();
            }
        }

        if (drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888); // Single color bitmap will be created of 1x1 pixel
        } else {
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        }

        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    public interface UtilAnimationListener {
        public void onAnimationEnded();
    }

    public interface UtilPopupListener {
        public void onItemSelected(int index);
    }

    public static boolean locationIsWithin(Location projectLocation,
                                           Location currentLocation,
                                           int radiusMetres) {

        float distance = projectLocation.distanceTo(currentLocation);
        if (distance > radiusMetres) {
            return false;
        } else {
            return true;
        }

    }

    public static Bitmap createBitmapFromView(Context context, View view, DisplayMetrics displayMetrics) {
        view.measure(displayMetrics.widthPixels, displayMetrics.heightPixels);
        view.layout(0, 0, displayMetrics.widthPixels,
                displayMetrics.heightPixels);
        view.buildDrawingCache();
        Bitmap bitmap = Bitmap.createBitmap(view.getMeasuredWidth(),
                view.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);
        return bitmap;
    }

    public static Bitmap rotateBitmapOrientation(String photoFilePath,
                  int maxWidth, int maxHeight, int scale) throws IOException {

        // Create and configure BitmapFactory
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(photoFilePath, options);
        BitmapFactory.Options opts = new BitmapFactory.Options();
        Bitmap bm = BitmapFactory.decodeFile(photoFilePath, opts);
        bm = Bitmap.createScaledBitmap(bm,maxWidth,maxHeight,false);
        // Read EXIF Data
        ExifInterface exif = new ExifInterface(photoFilePath);
        String orientString = exif.getAttribute(ExifInterface.TAG_ORIENTATION);
        int orientation = orientString != null ? Integer.parseInt(orientString) : ExifInterface.ORIENTATION_NORMAL;
        int rotationAngle = 0;
        if (orientation == ExifInterface.ORIENTATION_ROTATE_90) {
            rotationAngle = 90;
        }
        if (orientation == ExifInterface.ORIENTATION_ROTATE_180) {
            rotationAngle = 180;
        }
        if (orientation == ExifInterface.ORIENTATION_ROTATE_270) {
            rotationAngle = 270;
        }
        // Rotate Bitmap
        Matrix matrix = new Matrix();
        //options.inSampleSize = scale;
        Log.d(LOG, "rotationAngle: " + rotationAngle);
        matrix.setRotate(rotationAngle, (float) bm.getWidth() / 2, (float) bm.getHeight() / 2);
        Bitmap rotatedBitmap = Bitmap.createBitmap(bm, 0, 0, options.outWidth, options.outHeight, matrix, true);

        // Return result
        return rotatedBitmap;
    }
    public static Bitmap rotateBitmap(Bitmap bm) throws IOException {

        BitmapFactory.Options options = new BitmapFactory.Options();
        // Rotate Bitmap
        Matrix matrix = new Matrix();
        Log.d(LOG, "rotationAngle: 90");
        matrix.setRotate(90, (float) bm.getWidth() / 2, (float) bm.getHeight() / 2);
        Bitmap rotatedBitmap = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), matrix, true);

        // Return result
        return rotatedBitmap;
    }
    public static Bitmap decodeSampledBitmap(File file,
                                             int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = false;
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        Bitmap out = BitmapFactory.decodeFile(file.getAbsolutePath(), options);


        return out;
    }
    public static Bitmap decodeSampledBitmapFromFile(File file,
                                                     int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(file.getAbsolutePath(), options);

        // Calculate inSampleSize
        int sampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        if (sampleSize == 1) {
            sampleSize = 8;
        }
        options.inSampleSize = sampleSize;

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(file.getAbsolutePath(), options);
    }

    public static Bitmap decodeSampledBitmap(File file,
                                             int scale) {

        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = scale;

        Bitmap out = BitmapFactory.decodeFile(file.getAbsolutePath(), options);

        return out;
    }

    public static Bitmap decodeSampledBitmapFromResource(Resources res, int resId,
                                                         int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res, resId, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(res, resId, options);
    }

    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }
        Log.d(LOG,"calculated sample size: " + inSampleSize);
        return inSampleSize;
    }
    public static Bitmap getResizedBitmap(Bitmap bm, int newWidth, int newHeight) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // CREATE A MATRIX FOR THE MANIPULATION
        Matrix matrix = new Matrix();
        // RESIZE THE BIT MAP
        matrix.postScale(scaleWidth, scaleHeight);

        // "RECREATE" THE NEW BITMAP
        Bitmap resizedBitmap = Bitmap.createBitmap(
                bm, 0, 0, width, height, matrix, false);
        bm.recycle();
        return resizedBitmap;
    }
    public static void rotateViewWithDelay(
            final Activity activity, final View view,
            final int duration, int delay, final UtilAnimationListener listener) {

        final Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        final ObjectAnimator an = ObjectAnimator.ofFloat(view, "rotation", 0.0f, 360f);
                        an.setDuration(duration);
                        an.setInterpolator(new AccelerateDecelerateInterpolator());
                        an.addListener(new Animator.AnimatorListener() {
                            @Override
                            public void onAnimationStart(Animator animation) {

                            }

                            @Override
                            public void onAnimationEnd(Animator animation) {
                                if (listener != null) {
                                    listener.onAnimationEnded();
                                }
                            }

                            @Override
                            public void onAnimationCancel(Animator animation) {

                            }

                            @Override
                            public void onAnimationRepeat(Animator animation) {

                            }
                        });
                        an.start();

                    }
                });

                timer.cancel();
            }

        }, delay);
    }

    /**
     * Create custom Action Bar
     *
     * @param ctx
     * @param actionBar
     * @param text
     * @param image
     * @return ImageView
     */
    public static ImageView setCustomActionBar(Context ctx,
                                               ActionBar actionBar, String text, Drawable image) {
        actionBar.setDisplayShowCustomEnabled(true);

        LayoutInflater inflator = (LayoutInflater)
                ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflator.inflate(R.layout.action_bar_logo, null);
        TextView txt = (TextView) v.findViewById(R.id.ACTION_BAR_text);
        ImageView logo = (ImageView) v.findViewById(R.id.ACTION_BAR_logo);
        txt.setText(text);
        //
        logo.setImageDrawable(image);
        actionBar.setCustomView(v);
        actionBar.setTitle("");
        return logo;
    }

    public static ImageView setCustomActionBar(Context ctx,
                                               ActionBar actionBar, String text, String subText, Drawable image) {
        actionBar.setDisplayShowCustomEnabled(true);

        LayoutInflater inflator = (LayoutInflater)
                ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflator.inflate(R.layout.action_bar_logo, null);
        TextView txt = (TextView) v.findViewById(R.id.ACTION_BAR_text);
        TextView sub = (TextView) v.findViewById(R.id.ACTION_BAR_subText);
        ImageView logo = (ImageView) v.findViewById(R.id.ACTION_BAR_logo);
        txt.setText(text);
        sub.setText(subText);
        //
        logo.setImageDrawable(image);
        actionBar.setCustomView(v);
        actionBar.setTitle("");
        return logo;
    }

    public static void animateHeight(final View view, int maxHeight, int duration) {
        ValueAnimator anim = ValueAnimator.ofInt(0, maxHeight);
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                int val = (Integer) valueAnimator.getAnimatedValue();
                ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
                layoutParams.height = val;
                view.setLayoutParams(layoutParams);
            }
        });
        anim.setDuration(duration);
        view.setVisibility(View.VISIBLE);
        anim.start();
    }

    public static void fadeIn(View view) {

        ObjectAnimator animator = ObjectAnimator.ofFloat(view, "alpha", 0.0f, 1f);
        animator.setDuration(300);
        animator.setInterpolator(new AccelerateInterpolator());
        animator.start();
    }

    public static void fadeIn(View view, int duration) {

        ObjectAnimator animator = ObjectAnimator.ofFloat(view, "alpha", 0.0f, 1f);
        animator.setDuration(duration);
        animator.setInterpolator(new AccelerateInterpolator());
        animator.start();
    }

    public static void fadeOut(View view, int duration, final UtilAnimationListener listener) {

        ObjectAnimator animator = ObjectAnimator.ofFloat(view, "alpha", 1.0f, 0f);
        animator.setDuration(duration);
        animator.setInterpolator(new AccelerateInterpolator());
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (listener != null)
                    listener.onAnimationEnded();
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        animator.start();
    }

    public static void showPopupBasic(Context ctx, Activity act,
                                      List<String> list,
                                      View anchorView, final UtilPopupListener listener) {
        final ListPopupWindow pop = new ListPopupWindow(act);
        pop.setAdapter(new PopupListAdapter(ctx, R.layout.xxsimple_spinner_item,
                list, false));
        pop.setAnchorView(anchorView);
        pop.setModal(true);
        pop.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                pop.dismiss();
                if (listener != null) {
                    listener.onItemSelected(position);
                }
            }
        });
        pop.show();
    }

    public static int getPopupWidth(Activity activity) {
        DisplayMetrics displaymetrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        int height = displaymetrics.heightPixels;
        int width = displaymetrics.widthPixels;
        Double d = Double.valueOf("" + width);
        Double e = d / 1.5;
        Log.w(LOG, "## popup width: " + e.intValue());
        return e.intValue();
    }

    public static int getPopupHorizontalOffset(Activity activity) {
        DisplayMetrics displaymetrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        int height = displaymetrics.heightPixels;
        int width = displaymetrics.widthPixels;
        Double d = Double.valueOf("" + width);
        Double e = d / 15;
        Log.w(LOG, "## horizontalOffset: " + e.intValue());
        return e.intValue();
    }

    private static int getWindowWidth(Activity activity) {
        DisplayMetrics displaymetrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        int height = displaymetrics.heightPixels;
        int width = displaymetrics.widthPixels;
        return width;
    }

    private static int getWindowHeight(Activity activity) {
        DisplayMetrics displaymetrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        int height = displaymetrics.heightPixels;
        return height;
    }

    public static void showPopupBasicWithHeroImage(Context ctx, Activity act,
                                                   List<String> list,
                                                   View anchorView, String caption, final UtilPopupListener listener) {
        final ListPopupWindow pop = new ListPopupWindow(act);
        LayoutInflater inf = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inf.inflate(R.layout.hero_image_popup, null);
        TextView txt = (TextView) v.findViewById(R.id.HERO_caption);
        CircleImageView imgp = (CircleImageView) v.findViewById(R.id.HERO_personImage);
        imgp.setVisibility(View.GONE);
        if (caption != null) {
            txt.setText(caption);
        } else {
            txt.setVisibility(View.INVISIBLE);
        }
        ImageView img = (ImageView) v.findViewById(R.id.HERO_image);
        img.setImageDrawable(getRandomBackgroundImage(ctx));

        pop.setPromptView(v);
        pop.setPromptPosition(ListPopupWindow.POSITION_PROMPT_ABOVE);
        pop.setAdapter(new PopupListAdapter(ctx, R.layout.xxsimple_spinner_item,
                list, false));
        pop.setAnchorView(anchorView);
        pop.setHorizontalOffset(getPopupHorizontalOffset(act));
        pop.setModal(true);
        pop.setWidth(getPopupWidth(act));
        pop.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                pop.dismiss();
                if (listener != null) {
                    listener.onItemSelected(position);
                }
            }
        });
        try {
            pop.show();
        } catch (Exception e) {
            Log.e(LOG, "-- popup failed, probably nullpointer", e);
        }
    }

    static final Locale lox = Locale.getDefault();
    static final SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy HH:mm", lox);


    public static String getStaffImageURL(Context ctx, Integer staffID) {
        StringBuilder sb = new StringBuilder();
        sb.append(Statics.IMAGE_URL);
        sb.append("company").append(SharedUtil.getCompany(ctx).getCompanyID());
        sb.append("/companyStaff/t").append(staffID).append(".jpg");

        return sb.toString();
    }

    public static void showPopup(Context ctx, Activity act,
                                 List<String> list,
                                 View anchorView, View promptView,
                                 Integer width, Integer height, Integer horizontalOffset, final UtilPopupListener listener) {
        final ListPopupWindow pop = new ListPopupWindow(act);
        pop.setPromptView(promptView);
        pop.setPromptPosition(ListPopupWindow.POSITION_PROMPT_ABOVE);
        pop.setAdapter(new PopupListAdapter(ctx, R.layout.xxsimple_spinner_item,
                list, false));
        pop.setAnchorView(anchorView);
        if (width != null) {
            pop.setWidth(width);
        }
        if (horizontalOffset != null) {
            pop.setHorizontalOffset(horizontalOffset);
        }
        if (height != null) {
            pop.setHeight(height);
        }
        pop.setModal(true);
        pop.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                pop.dismiss();
                if (listener != null) {
                    listener.onItemSelected(position);
                }
            }
        });
        pop.show();
    }

    public static void showConfirmAppInvitationDialog(final Context ctx, final Activity act,
                                                      final StaffDTO companyStaff, final int type) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(act);
        switch (type) {
//            case COMPANY_EXEC:
//                dialog.setTitle("Company Exec");
//                break;
//            case OPS:
//                dialog.setTitle("Portfolio Exec");
//                break;
//            case PROJ:
//                dialog.setTitle("Project Manager"));
//                break;
//            case PROGRAMME:
//                dialog.setTitle("Programme Exec");
//                break;
        }
        dialog.setMessage(ctx.getResources().getString(R.string.invite_dialog))
                .setPositiveButton(ctx.getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        sendInvitation(ctx, act, companyStaff, type);
                    }
                })
                .setNegativeButton(ctx.getResources().getString(R.string.no), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                }).show();
    }

    public static final int COMPANY_EXEC = 1, PORTFOLIO_EXEC = 2, PROJECT_MGR = 3, PROGRAMME_EXEC = 4;

    private static void sendInvitation(final Context ctx, Activity act, final StaffDTO companyStaff,
                                       int type) {
        StringBuilder sba = new StringBuilder();
        sba.append(getHeader(ctx, type));
        switch (type) {
            case COMPANY_EXEC:
                sba.append(getExecLink(ctx, companyStaff));
                break;
            case PORTFOLIO_EXEC:
                sba.append(getOperationsLink(ctx, companyStaff));
                sba.append(getFooter(ctx));
                break;
            case PROJECT_MGR:
                sba.append(getProjectManagerLink(ctx, companyStaff));
                break;
            case PROGRAMME_EXEC:
                sba.append(getSiteManagerLink(ctx, companyStaff));
                break;
        }
        sba.append(getFooter(ctx));

        Log.w(LOG, "before send intent, sba = \n" + sba.toString());
        final Intent shareIntent = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:" + companyStaff.getEmail()));
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, ctx.getResources().getString(R.string.subject));
        shareIntent.putExtra(
                Intent.EXTRA_TEXT,
                Html.fromHtml(sba.toString())
        );
        Log.e(LOG, shareIntent.toString());
        ctx.startActivity(Intent.createChooser(shareIntent, "Email:"));

        //update app date

        StaffDTO cs = new StaffDTO();
        cs.setAppInvitationDate(new Date().getTime());
        cs.setStaffID(companyStaff.getStaffID());
        RequestDTO w = new RequestDTO(RequestDTO.UPDATE_COMPANY_STAFF);
        w.setStaff(cs);

        NetUtil.sendRequest(ctx, w, new NetUtil.NetUtilListener() {
            @Override
            public void onResponse(final ResponseDTO response) {

            }

            @Override
            public void onError(final String message) {

            }

            @Override
            public void onWebSocketClose() {

            }
        });


    }

    private static String getHeader(Context ctx, int type) {
        StringBuilder sb = new StringBuilder();
        sb.append("<h2>").append(SharedUtil.getCompany(ctx).getCompanyName()).append("</h2>");
//        sb.append("<p>").append(ctx.getResources().getString(R.string.invited)).append("</p>");
        switch (type) {
//            case COMPANY_EXEC:
//                sb.append("<h3>").append(ctx.getString(R.string.exec_app)).append("</h3>");
//                break;
//            case OPS:
//                sb.append("<h3>").append(ctx.getString(R.string.operations_app)).append("</h3>");
//                break;
//            case PROJ:
//                sb.append("<h3>").append(ctx.getString(R.string.pm_app)).append("</h3>");
//                break;
//            case PROGRAMME:
//                sb.append("<h3>").append(ctx.getString(R.string.supervisor_app)).append("</h3>");
//                break;
        }


        return sb.toString();
    }

    private static String getFooter(Context ctx) {
        StringBuilder sb = new StringBuilder();
//        sb.append(ctx.getString(R.string.contact_us));
//        sb.append("<h2>").append(ctx.getResources().getString(R.string.enjoy)).append("</h2>");
        return sb.toString();
    }

    private static String getSiteManagerLink(Context ctx, StaffDTO companyStaff) {
        StringBuilder sb = new StringBuilder();
//        sb.append("<p>").append(ctx.getResources().getString(R.string.click_link)).append("</p>");
        sb.append("<p>").append(Statics.INVITE_SITE_MGR).append("</p>");
        sb.append(getPinNote(ctx, companyStaff));
        return sb.toString();
    }

    private static String getProjectManagerLink(Context ctx, StaffDTO companyStaff) {
        StringBuilder sb = new StringBuilder();
//        sb.append("<p>").append(ctx.getResources().getString(R.string.click_link)).append("</p>");
        sb.append("<p>").append(Statics.INVITE_PROJECT_MGR).append("</p>");
        sb.append(getPinNote(ctx, companyStaff));
        return sb.toString();
    }

    private static String getPinNote(Context ctx, StaffDTO companyStaff) {
        StringBuilder sb = new StringBuilder();
//        sb.append("<p>").append(ctx.getResources().getString(R.string.pin_note)).append("</p>");
        sb.append("<h4>").append(companyStaff.getPin()).append("</h4>");
        return sb.toString();
    }

    private static String getOperationsLink(Context ctx, StaffDTO companyStaff) {
        StringBuilder sb = new StringBuilder();
//        sb.append("<p>").append(ctx.getResources().getString(R.string.click_link)).append("</p>");
        sb.append("<p>").append(Statics.INVITE_OPERATIONS_MGR).append("</p>");
        sb.append(getPinNote(ctx, companyStaff));
        return sb.toString();
    }

    private static String getExecLink(Context ctx, StaffDTO companyStaff) {
        StringBuilder sb = new StringBuilder();
//        sb.append("<p>").append(ctx.getResources().getString(R.string.click_link)).append("</p>");
        sb.append("<p>").append(Statics.INVITE_EXEC).append("</p>");
        sb.append(getPinNote(ctx, companyStaff));
        return sb.toString();
    }

    public static void preen(final View v, final int duration, final int max, final UtilAnimationListener listener) {
        final ObjectAnimator an = ObjectAnimator.ofFloat(v, "alpha", 1, 1);
        an.setDuration(duration);
        an.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                count++;
                if (count > max) {
                    count = 0;
                    an.cancel();
                    if (listener != null)
                        listener.onAnimationEnded();
                    return;
                }
                flashSeveralTimes(v, duration, max, listener);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        an.start();
    }

    public static void showErrorToast(Context ctx, String caption) {
        LayoutInflater inflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View view = inflater.inflate(R.layout.toast_monitor_generic, null);
        TextView txt = (TextView) view.findViewById(R.id.MONTOAST_text);
        TextView ind = (TextView) view.findViewById(R.id.MONTOAST_indicator);
        ind.setText("E");
        Statics.setRobotoFontLight(ctx, txt);
        ind.setBackground(ContextCompat.getDrawable(ctx, R.drawable.xred_oval_small));
        txt.setTextColor(ContextCompat.getColor(ctx, R.color.absa_red));
        txt.setText(caption);
        Toast customtoast = new Toast(ctx);

        customtoast.setView(view);
        customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
        customtoast.setDuration(Toast.LENGTH_LONG);
        customtoast.show();
    }

    public static void showToast(Context ctx, String caption) {
        LayoutInflater inflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View view = inflater.inflate(R.layout.toast_monitor_generic, null);
        TextView txt = (TextView) view.findViewById(R.id.MONTOAST_text);
        Statics.setRobotoFontLight(ctx, txt);
        TextView ind = (TextView) view.findViewById(R.id.MONTOAST_indicator);
        ind.setText("M");
        ind.setBackground(ContextCompat.getDrawable(ctx, R.drawable.xblue_oval_small));
        txt.setTextColor(ContextCompat.getColor(ctx, R.color.blue));
        txt.setText(caption);

        Toast customtoast = new Toast(ctx);

        customtoast.setView(view);
        customtoast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
        customtoast.setDuration(Toast.LENGTH_SHORT);
        customtoast.show();
    }


    public static double getElapsed(long start, long end) {
        BigDecimal m = new BigDecimal(end - start).divide(new BigDecimal(1000));
        return m.doubleValue();
    }

    static final String LOG = Util.class.getSimpleName();

    static Random random = new Random(System.currentTimeMillis());
    static int maxFlashes, count;

    static final int DURATION_FAST = 100, PAUSE_FAST = 100,
            DURATION_MEDIUM = 300, PAUSE_MEDIUM = 300,
            DURATION_SLOW = 500, PAUSE_SLOW = 500;

    public static final int FLASH_SLOW = 1,
            FLASH_MEDIUM = 2,
            FLASH_FAST = 3,
            INFINITE_FLASHES = 9999;

    public static View getHeroView(Context ctx, String caption) {
        LayoutInflater inflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflater.inflate(R.layout.hero_image, null);
        ImageView img = (ImageView) v.findViewById(R.id.HERO_image);
        TextView txt = (TextView) v.findViewById(R.id.HERO_caption);
        img.setImageDrawable(getRandomHeroImage(ctx));
        txt.setText(caption);
        return v;
    }

    public static void expandOrCollapse(final View view, int duration, final boolean isExpandRequired, final UtilAnimationListener listener) {
        TranslateAnimation an = null;
        if (isExpandRequired) {
            an = new TranslateAnimation(0.0f, 0.0f, -view.getHeight(), 0.0f);
            view.setVisibility(View.VISIBLE);
        } else {
            an = new TranslateAnimation(0.0f, 0.0f, 0.0f, -view.getHeight());
        }
        an.setDuration(duration);
        an.setInterpolator(new AccelerateDecelerateInterpolator());
        an.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                view.setVisibility(View.GONE);
                if (listener != null)
                    listener.onAnimationEnded();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    public static void collapse(final View view, int duration, final UtilAnimationListener listener) {
        int finalHeight = view.getHeight();

        ValueAnimator mAnimator = slideAnimator(view, finalHeight, 0);
        mAnimator.setDuration(duration);
        mAnimator.setInterpolator(new AccelerateDecelerateInterpolator());

        mAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                view.setVisibility(View.GONE);
                if (listener != null)
                    listener.onAnimationEnded();
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        mAnimator.start();
    }

    public static void expand(View view, int duration, final UtilAnimationListener listener) {
        view.setVisibility(View.VISIBLE);

        final int widthSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        final int heightSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        view.measure(widthSpec, heightSpec);

        ValueAnimator mAnimator = slideAnimator(view, 0, view.getMeasuredHeight());
        mAnimator.setDuration(duration);
        mAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        mAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (listener != null) listener.onAnimationEnded();
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        mAnimator.start();

    }


    private static ValueAnimator slideAnimator(final View view, int start, int end) {

        ValueAnimator animator = ValueAnimator.ofInt(start, end);

        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                //Update Height
                int value = (Integer) valueAnimator.getAnimatedValue();
                ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
                layoutParams.height = value;
                view.setLayoutParams(layoutParams);
            }
        });
        return animator;
    }

    public static void resizeHeight(final View view, final int height, final long duration, final UtilAnimationListener listener) {
        Log.e(LOG, "##### view height is " + height);


        ResizeAnimation a = new ResizeAnimation(view, 0);
        a.setDuration(10);
        a.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                ResizeAnimation an = new ResizeAnimation(view, height);
                an.setDuration(duration);
                an.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        if (listener != null)
                            listener.onAnimationEnded();
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
                view.startAnimation(an);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        view.startAnimation(a);
    }

    public static void flashOnce(View view, long duration, final UtilAnimationListener listener) {
        ObjectAnimator an = ObjectAnimator.ofFloat(view, "alpha", 0, 1);
        an.setRepeatMode(ObjectAnimator.REVERSE);
        an.setDuration(duration);
        an.setInterpolator(new AccelerateDecelerateInterpolator());
        an.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (listener != null)
                    listener.onAnimationEnded();
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        an.start();

    }

    public static void flashInfinite(final View view, final long duration) {
        ObjectAnimator an = ObjectAnimator.ofFloat(view, "alpha", 0, 1);
        an.setRepeatMode(ObjectAnimator.REVERSE);
        an.setDuration(duration);
        an.setInterpolator(new AccelerateDecelerateInterpolator());
        an.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                flashInfinite(view, duration);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        an.start();

    }

    public static void flashSeveralTimes(final View view,
                                         final long duration, final int max,
                                         final UtilAnimationListener listener) {
        final ObjectAnimator an = ObjectAnimator.ofFloat(view, "alpha", 0, 1);
        an.setRepeatMode(ObjectAnimator.REVERSE);
        an.setDuration(duration);
        an.setInterpolator(new AccelerateDecelerateInterpolator());
        an.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                count++;
                if (count > max) {
                    count = 0;
                    an.cancel();
                    if (listener != null)
                        listener.onAnimationEnded();
                    return;
                }
                flashSeveralTimes(view, duration, max, listener);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        an.start();

    }

    public static void shakeX(final View v, int duration, int max, final UtilAnimationListener listener) {
        final ObjectAnimator an = ObjectAnimator.ofFloat(v, "x", v.getX(), v.getX() + 20f);
        an.setDuration(duration);
        an.setRepeatMode(ObjectAnimator.REVERSE);
        an.setRepeatCount(max);
        an.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (listener != null)
                    listener.onAnimationEnded();
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        an.start();
    }

    public static void flashTrafficLights(final TextView red, final TextView amber, final TextView green,
                                          final int max, final int pace) {
        maxFlashes = max;
        ObjectAnimator an = ObjectAnimator.ofFloat(red, "alpha", 0, 1);
        ObjectAnimator an2 = ObjectAnimator.ofFloat(amber, "alpha", 0, 1);
        ObjectAnimator an3 = ObjectAnimator.ofFloat(green, "alpha", 0, 1);
        AnimatorSet aSet = new AnimatorSet();
        switch (pace) {
            case FLASH_FAST:
                an.setDuration(DURATION_FAST);
                aSet.setStartDelay(PAUSE_FAST);
                break;
            case FLASH_MEDIUM:
                an.setDuration(DURATION_MEDIUM);
                aSet.setStartDelay(PAUSE_MEDIUM);
                break;
            case FLASH_SLOW:
                an.setDuration(DURATION_SLOW);
                aSet.setStartDelay(PAUSE_SLOW);
                break;
        }

        an.setInterpolator(new AccelerateDecelerateInterpolator());
        an2.setInterpolator(new AccelerateDecelerateInterpolator());
        an3.setInterpolator(new AccelerateDecelerateInterpolator());

        an.setRepeatMode(ObjectAnimator.REVERSE);
        an2.setRepeatMode(ObjectAnimator.REVERSE);
        an3.setRepeatMode(ObjectAnimator.REVERSE);

        List<Animator> animatorList = new ArrayList<>();
        animatorList.add((Animator) an);
        animatorList.add((Animator) an2);
        animatorList.add((Animator) an3);


        aSet.playSequentially(animatorList);
        aSet.setInterpolator(new AccelerateDecelerateInterpolator());


        aSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                count++;
                if (maxFlashes == INFINITE_FLASHES) {
                    flashTrafficLights(red, amber, green, max, pace);
                    return;
                }

                if (count > maxFlashes) {
                    count = 0;
                    return;
                }
                flashTrafficLights(red, amber, green, max, pace);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        aSet.start();
    }

    public static void shrink(View view, long duration, final UtilAnimationListener listener) {
        ObjectAnimator anx = ObjectAnimator.ofFloat(view, "scaleX", 1, 0);
        ObjectAnimator any = ObjectAnimator.ofFloat(view, "scaleY", 1, 0);

        anx.setDuration(duration);
        any.setDuration(duration);
        anx.setInterpolator(new AccelerateInterpolator());
        any.setInterpolator(new AccelerateInterpolator());

        AnimatorSet set = new AnimatorSet();
        List<Animator> animatorList = new ArrayList<>();
        animatorList.add((Animator) anx);
        animatorList.add((Animator) any);
        set.playTogether(animatorList);
        set.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (listener != null)
                    listener.onAnimationEnded();
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        set.start();
    }

    public static void explode(View view, long duration, final UtilAnimationListener listener) {
        ObjectAnimator anx = ObjectAnimator.ofFloat(view, "scaleX", 0, 1);
        ObjectAnimator any = ObjectAnimator.ofFloat(view, "scaleY", 0, 1);

        anx.setDuration(duration);
        any.setDuration(duration);
        anx.setInterpolator(new AccelerateInterpolator());
        any.setInterpolator(new AccelerateInterpolator());

        AnimatorSet set = new AnimatorSet();
        List<Animator> animatorList = new ArrayList<>();
        animatorList.add((Animator) anx);
        animatorList.add((Animator) any);
        set.playTogether(animatorList);
        set.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (listener != null)
                    listener.onAnimationEnded();
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        set.start();
    }


    public static Drawable getRandomHeroImageExec(Context ctx) {
        random = new Random(System.currentTimeMillis());
        int index = random.nextInt(17);
        switch (index) {
            case 0:
                return ContextCompat.getDrawable(ctx, R.drawable.banner_meeting);
            case 1:
                return ContextCompat.getDrawable(ctx,
                        R.drawable.banner_construction3);
            case 2:
                return ContextCompat.getDrawable(ctx,
                        R.drawable.banner_construction4);
            case 3:
                return ContextCompat.getDrawable(ctx,
                        R.drawable.banner_construction5);
            case 4:
                return ContextCompat.getDrawable(ctx,
                        R.drawable.banner_meeting3);
            case 5:
                return ContextCompat.getDrawable(ctx,
                        R.drawable.banner_construction7);
            case 6:
                return ContextCompat.getDrawable(ctx,
                        R.drawable.banner_construction8);
            case 7:
                return ContextCompat.getDrawable(ctx,
                        R.drawable.banner_construction9);
            case 8:
                return ContextCompat.getDrawable(ctx,
                        R.drawable.banner_construction10);
            case 9:
                return ContextCompat.getDrawable(ctx,
                        R.drawable.banner_construction11);
            case 10:
                return ContextCompat.getDrawable(ctx,
                        R.drawable.banner_construction13);
            case 11:
                return ContextCompat.getDrawable(ctx,
                        R.drawable.banner_construction14);
            case 12:
                return ContextCompat.getDrawable(ctx,
                        R.drawable.banner_report);
            case 13:
                return ContextCompat.getDrawable(ctx,
                        R.drawable.banner_meeting3);
            case 14:
                return ContextCompat.getDrawable(ctx,
                        R.drawable.banner_report2);
            case 15:
                return ContextCompat.getDrawable(ctx,
                        R.drawable.banner_report);
            case 16:
                return ContextCompat.getDrawable(ctx,
                        R.drawable.banner_meeting);
            case 17:
                return ContextCompat.getDrawable(ctx,
                        R.drawable.banner_report3);

        }
        return ContextCompat.getDrawable(ctx,
                R.drawable.banner_report2);
    }


    public static Drawable getRandomHeroImage(Context ctx) {
        random = new Random(System.currentTimeMillis());
        int index = random.nextInt(17);
        switch (index) {
            case 0:
                return ContextCompat.getDrawable(ctx, R.drawable.banner_construction10);
            case 1:
                return ContextCompat.getDrawable(ctx,
                        R.drawable.banner_construction3);
            case 2:
                return ContextCompat.getDrawable(ctx,
                        R.drawable.banner_construction4);
            case 3:
                return ContextCompat.getDrawable(ctx,
                        R.drawable.banner_construction5);
            case 4:
                return ContextCompat.getDrawable(ctx,
                        R.drawable.banner_construction5);
            case 5:
                return ContextCompat.getDrawable(ctx,
                        R.drawable.banner_construction7);
            case 6:
                return ContextCompat.getDrawable(ctx,
                        R.drawable.banner_construction8);
            case 7:
                return ContextCompat.getDrawable(ctx,
                        R.drawable.banner_construction9);
            case 8:
                return ContextCompat.getDrawable(ctx,
                        R.drawable.banner_construction10);
            case 9:
                return ContextCompat.getDrawable(ctx,
                        R.drawable.banner_construction11);
            case 10:
                return ContextCompat.getDrawable(ctx,
                        R.drawable.banner_construction13);
            case 11:
                return ContextCompat.getDrawable(ctx,
                        R.drawable.banner_construction14);
            case 12:
                return ContextCompat.getDrawable(ctx,
                        R.drawable.banner_report);
            case 13:
                return ContextCompat.getDrawable(ctx,
                        R.drawable.banner_report3);
            case 14:
                return ContextCompat.getDrawable(ctx,
                        R.drawable.banner_report2);
            case 15:
                return ContextCompat.getDrawable(ctx,
                        R.drawable.banner_report);
            case 16:
                return ContextCompat.getDrawable(ctx,
                        R.drawable.banner_report2);
            case 17:
                return ContextCompat.getDrawable(ctx,
                        R.drawable.banner_report3);

        }
        return ContextCompat.getDrawable(ctx,
                R.drawable.banner_report2);
    }

    public static Drawable getRandomBackgroundImage(Context ctx) {
        random = new Random(System.currentTimeMillis());
        int index = random.nextInt(14);
        switch (index) {
            case 0:
                return ContextCompat.getDrawable(ctx, R.drawable.back1);
            case 1:
                return ContextCompat.getDrawable(ctx,
                        R.drawable.back2);
            case 2:
                return ContextCompat.getDrawable(ctx,
                        R.drawable.back3);
            case 3:
                return ContextCompat.getDrawable(ctx,
                        R.drawable.back4);
            case 4:
                return ContextCompat.getDrawable(ctx,
                        R.drawable.back5);
            case 5:
                return ContextCompat.getDrawable(ctx,
                        R.drawable.back6);
            case 6:
                return ContextCompat.getDrawable(ctx,
                        R.drawable.back7);
            case 7:
                return ContextCompat.getDrawable(ctx,
                        R.drawable.back8);
            case 8:
                return ContextCompat.getDrawable(ctx,
                        R.drawable.back9);
            case 9:
                return ContextCompat.getDrawable(ctx,
                        R.drawable.back10);
            case 10:
                return ContextCompat.getDrawable(ctx,
                        R.drawable.back6);
            case 11:
                return ContextCompat.getDrawable(ctx,
                        R.drawable.back12);
            case 12:
                return ContextCompat.getDrawable(ctx,
                        R.drawable.back13);
            case 13:
                return ContextCompat.getDrawable(ctx,
                        R.drawable.back14);
            case 14:
                return ContextCompat.getDrawable(ctx,
                        R.drawable.back15);
            default:
                return ContextCompat.getDrawable(ctx,
                        R.drawable.back3);

        }

    }

    public static void writeLocationToExif(String filePath, Location loc) {
        try {
            ExifInterface ef = new ExifInterface(filePath);
            ef.setAttribute(ExifInterface.TAG_GPS_LATITUDE, decimalToDMS(loc.getLatitude()));
            ef.setAttribute(ExifInterface.TAG_GPS_LONGITUDE, decimalToDMS(loc.getLongitude()));
            if (loc.getLatitude() > 0)
                ef.setAttribute(ExifInterface.TAG_GPS_LATITUDE_REF, "N");
            else
                ef.setAttribute(ExifInterface.TAG_GPS_LATITUDE_REF, "S");
            if (loc.getLongitude() > 0)
                ef.setAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF, "E");
            else
                ef.setAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF, "W");
            ef.saveAttributes();
//            Log.e(LOG, "### Exif attributes written to " + filePath);
        } catch (IOException e) {
            Log.e(LOG,"could not write exif data in image",e);
        }
    }

    //-----------------------------------------------------------------------------------

    private static String decimalToDMS(double coord) {
        coord = coord > 0 ? coord : -coord;  // -105.9876543 -> 105.9876543
        String sOut = Integer.toString((int) coord) + "/1,";   // 105/1,
        coord = (coord % 1) * 60;         // .987654321 * 60 = 59.259258
        sOut = sOut + Integer.toString((int) coord) + "/1,";   // 105/1,59/1,
        coord = (coord % 1) * 60000;             // .259258 * 60000 = 15555
        sOut = sOut + Integer.toString((int) coord) + "/1000";   // 105/1,59/1,15555/1000
        // Log.i(LOG, "decimalToDMS coord: " + coord + " converted to: " + sOut);
        return sOut;
    }

    public static Location getLocationFromExif(String filePath) {
        String sLat = "", sLatR = "", sLon = "", sLonR = "";
        try {
            ExifInterface ef = new ExifInterface(filePath);
            sLat = ef.getAttribute(ExifInterface.TAG_GPS_LATITUDE);
            sLon = ef.getAttribute(ExifInterface.TAG_GPS_LONGITUDE);
            sLatR = ef.getAttribute(ExifInterface.TAG_GPS_LATITUDE_REF);
            sLonR = ef.getAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF);
        } catch (IOException e) {
            return null;
        }

        double lat = DMSToDouble(sLat);
        if (lat > 180.0) return null;
        double lon = DMSToDouble(sLon);
        if (lon > 180.0) return null;

        lat = sLatR.contains("S") ? -lat : lat;
        lon = sLonR.contains("W") ? -lon : lon;

        Location loc = new Location("exif");
        loc.setLatitude(lat);
        loc.setLongitude(lon);
        Log.i(LOG, "----> File Exif lat: " + loc.getLatitude() + " lng: " + loc.getLongitude());
        return loc;
    }

    //-------------------------------------------------------------------------
    private static double DMSToDouble(String sDMS) {
        double dRV = 999.0;
        try {
            String[] DMSs = sDMS.split(",", 3);
            String s[] = DMSs[0].split("/", 2);
            dRV = (new Double(s[0]) / new Double(s[1]));
            s = DMSs[1].split("/", 2);
            dRV += ((new Double(s[0]) / new Double(s[1])) / 60);
            s = DMSs[2].split("/", 2);
            dRV += ((new Double(s[0]) / new Double(s[1])) / 3600);
        } catch (Exception e) {
        }
        return dRV;
    }

    public static final long HOUR = 60 * 60 * 1000;
    public static final long DAY = 24 * HOUR;
    public static final long WEEK = 7 * DAY;
    public static final long WEEKS = 2 * WEEK;
    public static final long MONTH = 30 * DAY;

    public interface ProjectDataRefreshListener {
        public void onDataRefreshed(ProjectDTO project);

        public void onError(String message);
    }

    public static void refreshProjectData(final Activity activity,
                                          final Context ctx, final Integer projectID,
                                          final ProjectDataRefreshListener listener) {
        if (activity == null || ctx == null) {
            Log.e(LOG, "## activity passed in is null, exit");
            return;
        }
        Log.i(LOG, "######## refreshProjectData started ....");
        RequestDTO w = new RequestDTO(RequestDTO.GET_PROJECT_DATA);
        w.setProjectID(projectID);

        NetUtil.sendRequest(ctx, w, new NetUtil.NetUtilListener() {
            @Override
            public void onResponse(final ResponseDTO response) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (response.getStatusCode() > 0) {
                            Util.showErrorToast(ctx, response.getMessage());
                            return;
                        }
                        CacheUtil.cacheProjectData(ctx, response, projectID, new CacheUtil.CacheUtilListener() {
                            @Override
                            public void onFileDataDeserialized(ResponseDTO response) {

                            }

                            @Override
                            public void onDataCached() {
                                listener.onDataRefreshed(response.getProjectList().get(0));
                            }

                            @Override
                            public void onError() {
                                listener.onError("Failed to get Project data");
                            }
                        });
                    }
                });
            }

            @Override
            public void onError(final String message) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        listener.onError(message);
                    }
                });
            }

            @Override
            public void onWebSocketClose() {

            }
        });

    }


    public static String getTruncated(double num) {
        String x = "" + num;
        int idx = x.indexOf(".");
        String xy = x.substring(idx + 1);
        if (xy.length() > 2) {
            String y = x.substring(0, idx + 2);
            return y;
        } else {
            return x;
        }
    }


    public static Intent getMailIntent(Context ctx, String email, String message, String subject,
                                       File file) {

        Intent sendIntent = new Intent(Intent.ACTION_SEND);
        if (email == null) {
            sendIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{"",
                    "aubrey.malabie@gmail.com"});
        } else {
            sendIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{email});
        }

        if (subject == null) {
            sendIntent.putExtra(Intent.EXTRA_SUBJECT,
                    subject);
        } else {
            sendIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
        }
        sendIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
        sendIntent.putExtra(Intent.EXTRA_TEXT, message);

        sendIntent.setType("application/pdf");

        return sendIntent;
    }

    public static double getPercentage(int totalMarks, int attained) {
        BigDecimal total = new BigDecimal(totalMarks);
        BigDecimal totStu = new BigDecimal(attained);
        double perc = totStu.divide(total, 3, BigDecimal.ROUND_UP).doubleValue();
        perc = perc * 100;
        return perc;
    }

    public static String formatCellphone(String cellphone) {
        StringBuilder sb = new StringBuilder();
        String suff = cellphone.substring(0, 3);
        String p1 = cellphone.substring(3, 6);
        String p2 = cellphone.substring(6);
        sb.append(suff).append(" ");
        sb.append(p1).append(" ");
        sb.append(p2);
        return sb.toString();
    }

    public static void hide(View view, long duration) {
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", 1, 0);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", 1, 0);
        AnimatorSet animSetXY = new AnimatorSet();
        animSetXY.playTogether(scaleX, scaleY);
        animSetXY.setInterpolator(new AccelerateInterpolator());
        animSetXY.setDuration(duration);
        if (duration == 0) {
            animSetXY.setDuration(200);
        }
        animSetXY.start();
    }

    public static void show(View view, long duration) {
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", 0, 1);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", 0, 1);
        AnimatorSet animSetXY = new AnimatorSet();
        animSetXY.playTogether(scaleX, scaleY);
        animSetXY.setInterpolator(new OvershootInterpolator());
        animSetXY.setDuration(duration);
        if (duration == 0) {
            animSetXY.setDuration(300);
        }
        animSetXY.start();
    }

    public static void animateScaleY(View txt, long duration) {
        final ObjectAnimator an = ObjectAnimator.ofFloat(txt, "scaleY", 0);
        an.setRepeatCount(1);
        an.setDuration(duration);
        an.setRepeatMode(ValueAnimator.REVERSE);
        an.start();
    }

    public static void animateRotationY(View view, long duration) {
        final ObjectAnimator an = ObjectAnimator.ofFloat(view, "rotation", 0.0f, 360f);
        //an.setRepeatCount(ObjectAnimator.REVERSE);
        an.setDuration(duration);
        an.setInterpolator(new AccelerateDecelerateInterpolator());
        an.start();
    }

    public static void animateRollup(View view, long duration) {
        final ObjectAnimator an = ObjectAnimator.ofFloat(view, "scaleY", 0.0f);
        //an.setRepeatCount(ObjectAnimator.REVERSE);
        an.setDuration(duration);
        an.setInterpolator(new AccelerateDecelerateInterpolator());
        an.start();
    }

    public static void animateSlideRight(View view, long duration) {
        final ObjectAnimator an = ObjectAnimator.ofInt(
                view, "translate", 0, 100, 0, 100);
        an.setDuration(duration);
        an.setInterpolator(new AccelerateDecelerateInterpolator());
        an.start();
    }

    public static void animateFlipFade(Context ctx, View v) {
        AnimatorSet set = (AnimatorSet) AnimatorInflater.loadAnimator(ctx,
                R.animator.flip_fade);
        set.setTarget(v);
        set.start();
    }


    public static ArrayList<String> getRecurStrings(Date date) {
        Calendar cal = GregorianCalendar.getInstance();
        cal.setTime(date);

        int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
        String day = getDayOfWeek(dayOfWeek);
        boolean isWeekDay = false;
        if (dayOfWeek > 1 && dayOfWeek < 7) {
            isWeekDay = true;
        }
        Log.d("Util", "#########");
        Log.d("Util", "dayOfWeek: " + dayOfWeek + " day: " + day
                + " isWeekDay: " + isWeekDay);
        // which week?
        int week = 0;
        int dayOfMonth = cal.get(Calendar.DAY_OF_MONTH);
        if (dayOfMonth < 8) {
            week = 1;
        }
        if (dayOfMonth > 7 && dayOfMonth < 15) {
            week = 2;
        }
        if (dayOfMonth > 14 && dayOfMonth < 22) {
            week = 3;
        }
        if (dayOfMonth > 21) {
            week = 4;
        }
        Log.d("Util", "dayOfMonth: " + dayOfMonth + " week in month: "
                + getWeekOfMonth(week));

        ArrayList<String> list = new ArrayList<String>();
        list.add("One time event");
        list.add("Daily Event");

        list.add("Every Week day(Mon-Fri)");
        list.add("Weekly on " + getDayOfWeek(dayOfWeek));
        list.add("Monthly (every " + getWeekOfMonth(week) + " "
                + getDayOfWeek(dayOfWeek));
        list.add("Monthly on day " + dayOfMonth);
        String month = getMonth(cal.get(Calendar.MONTH));
        list.add("Yearly on " + dayOfMonth + " " + month);
        return list;
    }

    public static String getMonth(int mth) {
        switch (mth) {
            case 0:
                return "January";
            case 1:
                return "February";
            case 2:
                return "March";
            case 3:
                return "April";
            case 4:
                return "May";
            case 5:
                return "June";
            case 6:
                return "July";
            case 7:
                return "August";
            case 8:
                return "September";
            case 9:
                return "October";
            case 10:
                return "November";
            case 11:
                return "December";
        }
        return null;
    }


    public static String getWeekOfMonth(int i) {
        switch (i) {
            case 1:
                return FIRST_WEEK;
            case 2:
                return SECOND_WEEK;
            case 4:
                return FOURTH_WEEK;
            case 3:
                return THIRD_WEEK;

        }
        return MONDAY;
    }


    public static String getDayOfWeek(int i) {
        switch (i) {
            case 2:
                return MONDAY;
            case 3:
                return TUESDAY;
            case 4:
                return WEDNESDAY;
            case 5:
                return THURSDAY;
            case 6:
                return FRIDAY;
            case 7:
                return SATURDAY;
            case 1:
                return SUNDAY;

        }
        return MONDAY;
    }


    public static final String MONDAY = "Monday";
    public static final String TUESDAY = "Tuesday";
    public static final String WEDNESDAY = "Wednesday";
    public static final String THURSDAY = "Thursday";
    public static final String FRIDAY = "Friday";
    public static final String SATURDAY = "Saturday";
    public static final String SUNDAY = "Sunday";

    public static final String FIRST_WEEK = "First";
    public static final String SECOND_WEEK = "Second";
    public static final String THIRD_WEEK = "Third";
    public static final String FOURTH_WEEK = "Fourth";


    public static int[] getDateParts(Date date) {
        Calendar cal = GregorianCalendar.getInstance();
        cal.setTime(date);
        int[] ints = {cal.get(Calendar.DAY_OF_MONTH), cal.get(Calendar.MONTH),
                cal.get(Calendar.YEAR)};
        return ints;
    }


    public static File getDirectory(String dir) {
        File sd = Environment.getExternalStorageDirectory();
        File appDir = new File(sd, dir);
        if (!appDir.exists()) {
            appDir.mkdir();
        }

        return appDir;

    }

    public static String getLongTime(Date date) {
        SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");
        return df.format(date);
    }

    public static String getShortTime(Date date) {
        SimpleDateFormat df = new SimpleDateFormat("HH:mm");
        return df.format(date);
    }


    public static int[] getTime(Date date) {
        SimpleDateFormat df = new SimpleDateFormat("HH");
        String hr = df.format(date);
        df = new SimpleDateFormat("mm");
        String min = df.format(date);
        int[] time = {Integer.parseInt(hr), Integer.parseInt(min)};
        return time;
    }

    public static long getSimpleDate(Date date) {
        Calendar cal = GregorianCalendar.getInstance();
        cal.setTime(date);
        cal.set(GregorianCalendar.HOUR_OF_DAY, 0);
        cal.set(GregorianCalendar.MINUTE, 0);
        cal.set(GregorianCalendar.SECOND, 0);
        cal.set(GregorianCalendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
    }

    public static long getSimpleDate(int day, int month, int year) {
        Calendar cal = GregorianCalendar.getInstance();
        cal.set(GregorianCalendar.YEAR, year);
        cal.set(GregorianCalendar.MONTH, month);
        cal.set(GregorianCalendar.DAY_OF_MONTH, day);
        return getSimpleDate(cal.getTime());
    }

    public static String getLongerDate(int day, int month, int year) {
        Calendar cal = GregorianCalendar.getInstance();
        cal.set(GregorianCalendar.YEAR, year);
        cal.set(GregorianCalendar.MONTH, month);
        cal.set(GregorianCalendar.DAY_OF_MONTH, day);
        Date d = cal.getTime();
        SimpleDateFormat df = new SimpleDateFormat("EEEE, dd MMMM, yyyy");
        return df.format(d);
    }

    public static String getLongDate(int day, int month, int year) {
        Calendar cal = GregorianCalendar.getInstance();
        cal.set(GregorianCalendar.YEAR, year);
        cal.set(GregorianCalendar.MONTH, month);
        cal.set(GregorianCalendar.DAY_OF_MONTH, day);
        Date d = cal.getTime();
        SimpleDateFormat df = new SimpleDateFormat("EEE, dd MMM yyyy");
        return df.format(d);
    }

    public static String getLongestDate(int day, int month, int year) {
        Calendar cal = GregorianCalendar.getInstance();
        cal.set(GregorianCalendar.YEAR, year);
        cal.set(GregorianCalendar.MONTH, month);
        cal.set(GregorianCalendar.DAY_OF_MONTH, day);
        Date d = cal.getTime();
        SimpleDateFormat df = new SimpleDateFormat("EEEE, dd MMM yyyy");
        return df.format(d);
    }

    public static String getLongDate(Date date) {
        SimpleDateFormat df = new SimpleDateFormat("EEEE, dd MMM yyyy");
        return df.format(date);
    }

    public static String getLongerDate(Date date) {
        SimpleDateFormat df = new SimpleDateFormat("EEEE, dd MMMM yyyy");
        return df.format(date);
    }

    public static String getLongDateTime(Date date) {
        SimpleDateFormat df = new SimpleDateFormat(
                "EEEE, dd MMMM yyyy HH:mm:ss");
        return df.format(date);
    }

    public static Calendar getLongDateTimeNoSeconds(Calendar cal) {

        int year = cal.get(Calendar.YEAR);
        int mth = cal.get(Calendar.MONTH);
        int day = cal.get(Calendar.DAY_OF_MONTH);
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        int minute = cal.get(Calendar.MINUTE);

        Calendar c = GregorianCalendar.getInstance();
        c.set(Calendar.YEAR, year);
        c.set(Calendar.MONTH, mth);
        c.set(Calendar.DAY_OF_MONTH, day);
        c.set(Calendar.HOUR_OF_DAY, hour);
        c.set(Calendar.MINUTE, minute);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        Log.d("Util", "Reset date: " + getLongDateTimeNoSeconds(c.getTime()));
        return c;
    }

    public static String getLongDateTimeNoSeconds(Date date) {
        SimpleDateFormat df = new SimpleDateFormat("EEEE, dd MMMM yyyy HH:mm");
        return df.format(date);
    }

    public static String getLongDateForPDF(long date) {
        SimpleDateFormat df = new SimpleDateFormat("EEE dd MMM yyyy");
        Date d = new Date(date);
        return df.format(d);
    }


    public static String getShortDateForPDF(long startDate, long endDate) {
        SimpleDateFormat df = new SimpleDateFormat("dd MMM yyyy");
        Date dtStart = new Date(startDate);
        Date dtEnd = new Date(endDate);

        return df.format(dtStart) + " to " + df.format(dtEnd);
    }

    public static byte[] scaleImage(Context context, Uri photoUri) throws IOException {
        InputStream is = context.getContentResolver().openInputStream(photoUri);
        BitmapFactory.Options dbo = new BitmapFactory.Options();
        dbo.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(is, null, dbo);
        is.close();

        int rotatedWidth, rotatedHeight;
        int orientation = getOrientation(context, photoUri);

        if (orientation == 90 || orientation == 270) {
            rotatedWidth = dbo.outHeight;
            rotatedHeight = dbo.outWidth;
        } else {
            rotatedWidth = dbo.outWidth;
            rotatedHeight = dbo.outHeight;
        }

        Bitmap srcBitmap;
        is = context.getContentResolver().openInputStream(photoUri);
        if (rotatedWidth > MAX_IMAGE_DIMENSION || rotatedHeight > MAX_IMAGE_DIMENSION) {
            float widthRatio = ((float) rotatedWidth) / ((float) MAX_IMAGE_DIMENSION);
            float heightRatio = ((float) rotatedHeight) / ((float) MAX_IMAGE_DIMENSION);
            float maxRatio = Math.max(widthRatio, heightRatio);

            // Create the bitmap from file
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = (int) maxRatio;
            srcBitmap = BitmapFactory.decodeStream(is, null, options);
        } else {
            srcBitmap = BitmapFactory.decodeStream(is);
        }
        is.close();

        /* if the orientation is not 0 (or -1, which means we don't know), we
         * have to do a rotation. */
        if (orientation > 0) {
            Matrix matrix = new Matrix();
            matrix.postRotate(orientation);

            srcBitmap = Bitmap.createBitmap(srcBitmap, 0, 0,
                    srcBitmap.getWidth(), srcBitmap.getHeight(), matrix, true);
        }

        String type = context.getContentResolver().getType(photoUri);
        if (type == null) {
            type = "image/jpg";
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        if (type.equals("image/png")) {
            srcBitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        } else if (type.equals("image/jpg") || type.equals("image/jpeg")) {
            srcBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        }
        byte[] bMapArray = baos.toByteArray();
        baos.close();
        return bMapArray;
    }

    public static int getOrientation(Context context, Uri photoUri) {
        /* it's on the external media. */
        Cursor cursor = context.getContentResolver().query(photoUri,
                new String[]{MediaStore.Images.ImageColumns.ORIENTATION},
                null, null, null);

        if (cursor == null) {
            return 1;
        }
        if (cursor.getCount() != 1) {
            return -1;
        }

        cursor.moveToFirst();
        return cursor.getInt(0);
    }


    private static int MAX_IMAGE_DIMENSION = 720;

    static public boolean hasStorage(boolean requireWriteAccess) {
        String state = Environment.getExternalStorageState();
        Log.w("Util", "--------- disk storage state is: " + state);

        if (Environment.MEDIA_MOUNTED.equals(state)) {
            if (requireWriteAccess) {
                boolean writable = checkFsWritable();
                Log.i("Util", "************ storage is writable: " + writable);
                return writable;
            } else {
                return true;
            }
        } else if (!requireWriteAccess && Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }

    private static boolean checkFsWritable() {
        // Create a temporary file to see whether a volume is really writeable.
        // It's important not to put it in the root directory which may have a
        // limit on the number of files.
        String directoryName = Environment.getExternalStorageDirectory().toString() + "/DCIM";
        File directory = new File(directoryName);
        if (!directory.isDirectory()) {
            if (!directory.mkdirs()) {
                return false;
            }
        }
        return directory.canWrite();
    }


    public static void setActionBarIconSpinning(Menu mMenu, int menuItem, final boolean refreshing) {
        if (mMenu != null) {
            final MenuItem refreshItem = mMenu.findItem(menuItem);
            if (refreshItem != null) {
                if (refreshing) {
                    refreshItem.setActionView(R.layout.action_bar_progess);
                } else {
                    refreshItem.setActionView(null);
                }
            }
        }
    }

    private static String getStringFromInputStream(InputStream is) throws IOException {

        BufferedReader br = null;
        StringBuilder sb = new StringBuilder();
        String line;
        try {
            br = new BufferedReader(new InputStreamReader(is));
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

        } finally {
            if (br != null) {
                br.close();
            }
        }
        String json = sb.toString();
        return json;

    }

    static final Gson GSON = new Gson();

    public static ResponseDTO getResponseData(FileInputStream stream) throws IOException {
        String json = getStringFromInputStream(stream);
        ResponseDTO response = GSON.fromJson(json, ResponseDTO.class);
        return response;
    }

    public static Drawable getMapIcon(Context ctx, int index, ProjectDTO project) {
        Drawable drawable = null;
        switch (index) {
            case 0:
                drawable = ContextCompat.getDrawable(ctx, R.drawable.number_1);
                break;
            case 1:
                drawable = ContextCompat.getDrawable(ctx, R.drawable.number_2);
                break;
            case 2:
                drawable = ContextCompat.getDrawable(ctx, R.drawable.number_3);
                break;
            case 3:
                drawable = ContextCompat.getDrawable(ctx, R.drawable.number_4);
                break;
            case 4:
                drawable = ContextCompat.getDrawable(ctx, R.drawable.number_5);
                break;
            case 5:
                drawable = ContextCompat.getDrawable(ctx, R.drawable.number_6);
                break;
            case 6:
                drawable = ContextCompat.getDrawable(ctx, R.drawable.number_7);
                break;
            case 7:
                drawable = ContextCompat.getDrawable(ctx, R.drawable.number_8);
                break;
            case 8:
                drawable = ContextCompat.getDrawable(ctx, R.drawable.number_9);
                break;
            case 9:
                drawable = ContextCompat.getDrawable(ctx, R.drawable.number_10);
                break;
            case 10:
                drawable = ContextCompat.getDrawable(ctx, R.drawable.number_11);
                break;
            case 11:
                drawable = ContextCompat.getDrawable(ctx, R.drawable.number_12);
                break;
            case 12:
                drawable = ContextCompat.getDrawable(ctx, R.drawable.number_13);
                break;
            case 13:
                drawable = ContextCompat.getDrawable(ctx, R.drawable.number_14);
                break;

            case 14:
                drawable = ContextCompat.getDrawable(ctx, R.drawable.number_15);
                break;
            case 15:
                drawable = ContextCompat.getDrawable(ctx, R.drawable.number_16);
                break;
            case 16:
                drawable = ContextCompat.getDrawable(ctx, R.drawable.number_17);
                break;
            case 17:
                drawable = ContextCompat.getDrawable(ctx, R.drawable.number_18);
                break;
            case 18:
                drawable = ContextCompat.getDrawable(ctx, R.drawable.number_19);
                break;
            case 19:
                drawable = ContextCompat.getDrawable(ctx, R.drawable.number_20);
                break;
            case 20:
                drawable = ContextCompat.getDrawable(ctx, R.drawable.number_21);
                break;
            case 21:
                drawable = ContextCompat.getDrawable(ctx, R.drawable.number_22);
                break;
            case 22:
                drawable = ContextCompat.getDrawable(ctx, R.drawable.number_23);
                break;
            case 23:
                drawable = ContextCompat.getDrawable(ctx, R.drawable.number_24);
                break;
            case 24:
                drawable = ContextCompat.getDrawable(ctx, R.drawable.number_25);
                break;
            case 25:
                drawable = ContextCompat.getDrawable(ctx, R.drawable.number_26);
                break;
            case 26:
                drawable = ContextCompat.getDrawable(ctx, R.drawable.number_27);
                break;
            case 27:
                drawable = ContextCompat.getDrawable(ctx, R.drawable.number_28);
                break;

            case 28:
                drawable = ContextCompat.getDrawable(ctx, R.drawable.number_29);
                break;
            case 29:
                drawable = ContextCompat.getDrawable(ctx, R.drawable.number_30);
                break;
            case 30:
                drawable = ContextCompat.getDrawable(ctx, R.drawable.number_31);
                break;
            case 31:
                drawable = ContextCompat.getDrawable(ctx, R.drawable.number_32);
                break;
            case 32:
                drawable = ContextCompat.getDrawable(ctx, R.drawable.number_33);
                break;
            case 33:
                drawable = ContextCompat.getDrawable(ctx, R.drawable.number_34);
                break;
            case 34:
                drawable = ContextCompat.getDrawable(ctx, R.drawable.number_35);
                break;
            case 35:
                drawable = ContextCompat.getDrawable(ctx, R.drawable.number_36);
                break;
            case 36:
                drawable = ContextCompat.getDrawable(ctx, R.drawable.number_37);
                break;
            case 37:
                drawable = ContextCompat.getDrawable(ctx, R.drawable.number_38);
                break;
            case 38:
                drawable = ContextCompat.getDrawable(ctx, R.drawable.number_39);
                break;
            case 39:
                drawable = ContextCompat.getDrawable(ctx, R.drawable.number_40);
                break;
            case 40:
                drawable = ContextCompat.getDrawable(ctx, R.drawable.number_41);
                break;
            case 41:
                drawable = ContextCompat.getDrawable(ctx, R.drawable.number_42);
                break;
            ///////
            case 42:
                drawable = ContextCompat.getDrawable(ctx, R.drawable.number_43);
                break;
            case 43:
                drawable = ContextCompat.getDrawable(ctx, R.drawable.number_44);
                break;
            case 44:
                drawable = ContextCompat.getDrawable(ctx, R.drawable.number_45);
                break;
            case 45:
                drawable = ContextCompat.getDrawable(ctx, R.drawable.number_46);
                break;
            case 46:
                drawable = ContextCompat.getDrawable(ctx, R.drawable.number_47);
                break;
            case 47:
                drawable = ContextCompat.getDrawable(ctx, R.drawable.number_48);
                break;
            case 48:
                drawable = ContextCompat.getDrawable(ctx, R.drawable.number_49);
                break;
            case 49:
                drawable = ContextCompat.getDrawable(ctx, R.drawable.number_50);
                break;
            case 50:
                drawable = ContextCompat.getDrawable(ctx, R.drawable.number_51);
                break;
            case 51:
                drawable = ContextCompat.getDrawable(ctx, R.drawable.number_52);
                break;
            case 52:
                drawable = ContextCompat.getDrawable(ctx, R.drawable.number_53);
                break;
            case 53:
                drawable = ContextCompat.getDrawable(ctx, R.drawable.number_54);
                break;
            case 54:
                drawable = ContextCompat.getDrawable(ctx, R.drawable.number_55);
                break;
            case 55:
                drawable = ContextCompat.getDrawable(ctx, R.drawable.number_56);
                break;

            case 56:
                drawable = ContextCompat.getDrawable(ctx, R.drawable.number_57);
                break;
            case 57:
                drawable = ContextCompat.getDrawable(ctx, R.drawable.number_58);
                break;
            case 58:
                drawable = ContextCompat.getDrawable(ctx, R.drawable.number_59);
                break;
            case 59:
                drawable = ContextCompat.getDrawable(ctx, R.drawable.number_60);
                break;
            case 60:
                drawable = ContextCompat.getDrawable(ctx, R.drawable.number_61);
                break;
            case 61:
                drawable = ContextCompat.getDrawable(ctx, R.drawable.number_62);
                break;
            case 62:
                drawable = ContextCompat.getDrawable(ctx, R.drawable.number_63);
                break;
            case 63:
                drawable = ContextCompat.getDrawable(ctx, R.drawable.number_64);
                break;
            case 64:
                drawable = ContextCompat.getDrawable(ctx, R.drawable.number_65);
                break;
            case 65:
                drawable = ContextCompat.getDrawable(ctx, R.drawable.number_66);
                break;
            case 66:
                drawable = ContextCompat.getDrawable(ctx, R.drawable.number_67);
                break;
            case 67:
                drawable = ContextCompat.getDrawable(ctx, R.drawable.number_68);
                break;
            case 68:
                drawable = ContextCompat.getDrawable(ctx, R.drawable.number_69);
                break;
            case 69:
                drawable = ContextCompat.getDrawable(ctx, R.drawable.number_70);
                break;

            case 70:
                drawable = ContextCompat.getDrawable(ctx, R.drawable.number_71);
                break;
            case 71:
                drawable = ContextCompat.getDrawable(ctx, R.drawable.number_72);
                break;
            case 72:
                drawable = ContextCompat.getDrawable(ctx, R.drawable.number_73);
                break;
            case 73:
                drawable = ContextCompat.getDrawable(ctx, R.drawable.number_74);
                break;
            case 74:
                drawable = ContextCompat.getDrawable(ctx, R.drawable.number_75);
                break;
            case 75:
                drawable = ContextCompat.getDrawable(ctx, R.drawable.number_76);
                break;
            case 76:
                drawable = ContextCompat.getDrawable(ctx, R.drawable.number_77);
                break;
            case 77:
                drawable = ContextCompat.getDrawable(ctx, R.drawable.number_78);
                break;
            case 78:
                drawable = ContextCompat.getDrawable(ctx, R.drawable.number_79);
                break;
            case 79:
                drawable = ContextCompat.getDrawable(ctx, R.drawable.number_80);
                break;
            case 80:
                drawable = ContextCompat.getDrawable(ctx, R.drawable.number_81);
                break;
            case 81:
                drawable = ContextCompat.getDrawable(ctx, R.drawable.number_82);
                break;
            case 82:
                drawable = ContextCompat.getDrawable(ctx, R.drawable.number_83);
                break;
            case 83:
                drawable = ContextCompat.getDrawable(ctx, R.drawable.number_84);
                break;
            case 84:
                drawable = ContextCompat.getDrawable(ctx, R.drawable.number_85);
                break;
            case 85:
                drawable = ContextCompat.getDrawable(ctx, R.drawable.number_86);
                break;
            case 86:
                drawable = ContextCompat.getDrawable(ctx, R.drawable.number_87);
                break;
            case 87:
                drawable = ContextCompat.getDrawable(ctx, R.drawable.number_88);
                break;
            case 88:
                drawable = ContextCompat.getDrawable(ctx, R.drawable.number_89);
                break;
            case 89:
                drawable = ContextCompat.getDrawable(ctx, R.drawable.number_90);
                break;
        }

        return drawable;
    }
}

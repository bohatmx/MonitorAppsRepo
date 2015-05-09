package com.boha.monitor.library.activities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.boha.monitor.library.R;
import com.boha.monitor.library.dto.CompanyStaffDTO;
import com.boha.monitor.library.dto.ProjectDTO;
import com.boha.monitor.library.dto.ProjectSiteDTO;
import com.boha.monitor.library.dto.transfer.PhotoUploadDTO;
import com.boha.monitor.library.util.PhotoCache;
import com.boha.monitor.library.util.Statics;
import com.boha.monitor.library.util.Util;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class ImageActivity extends AppCompatActivity {

    TextView txtNumber, txtTitle, txtSubTitle, txtNext, txtPrev, txtDate;
    ImageView imageView;
    String url;
    PhotoUploadDTO photoUpload;
    ProjectDTO project;
    ProjectSiteDTO projectSite;
    CompanyStaffDTO companyStaff;
    PhotoCache photoCache;
    int index;
    Context ctx;
    float x1, x2;
    float y1, y2;

    @Override
    protected void onCreate( Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);
        ctx = getApplicationContext();
        if (savedInstanceState != null) {
            index = savedInstanceState.getInt("index");
            projectSite = (ProjectSiteDTO) savedInstanceState.getSerializable("projectSite");
            project = (ProjectDTO) savedInstanceState.getSerializable("project");
        } else {
            project = (ProjectDTO) getIntent().getSerializableExtra("project");
            projectSite = (ProjectSiteDTO) getIntent().getSerializableExtra("projectSite");
            index = getIntent().getIntExtra("index", 0);
        }
        setFields();
        txtNumber.setText("" + (index + 1));


        photoCache = new PhotoCache();
        PhotoUploadDTO p = photoCache.getPhotoUploadList().get(index);
        txtDate.setText(sdf.format(p.getDateTaken()));

        if (projectSite != null) {
            photoCache.setPhotoUploadList(projectSite.getPhotoUploadList());
            txtTitle.setText(projectSite.getProjectName());
            txtSubTitle.setText(projectSite.getProjectSiteName());

        }
        if (project != null) {
            photoCache.setPhotoUploadList(project.getPhotoUploadList());
            txtTitle.setText(project.getProjectName());
            txtSubTitle.setVisibility(View.GONE);
        }
        String url = Util.getPhotoURL(p);
        Picasso.with(ctx).load(url).into(imageView);


        Util.animateScaleY(imageView, 200);
        setHeader();
    }

    @Override
    public void onSaveInstanceState( Bundle b) {
        b.putInt("index", index);
        if (projectSite != null) {
            b.putSerializable("projectSite", projectSite);
        }
        if (project != null) {
            b.putSerializable("project", project);
        }

        super.onSaveInstanceState(b);
    }

    private static final Locale loc = Locale.getDefault();
    private static final SimpleDateFormat sdf = new SimpleDateFormat("EEEE, dd MMMM yyyy HH:mm", loc);

    private void setHeader() {

    }

    static final String LOG = ImageActivity.class.getSimpleName();

    private void setFields() {
        txtNumber = (TextView) findViewById(R.id.IMG_number);
        imageView = (ImageView) findViewById(R.id.IMG_image);
        txtTitle = (TextView) findViewById(R.id.IMG_title);
        txtSubTitle = (TextView) findViewById(R.id.IMG_subtitle);
        txtDate = (TextView) findViewById(R.id.IMG_date);
        txtNext = (TextView) findViewById(R.id.IMG_next);
        txtPrev = (TextView) findViewById(R.id.IMG_prev);

        Statics.setRobotoFontLight(ctx, txtTitle);
        Statics.setRobotoFontLight(ctx, txtSubTitle);
        Statics.setRobotoFontLight(ctx, txtDate);

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               showDialog();
            }
        });
        txtNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                index++;
                if (index == photoCache.getPhotoUploadList().size()) {
                    index = 0;
                }
                loadImage();
            }
        });
        txtPrev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (index == 0) return;
                index--;
                if (index < 0) {
                    index = 0;
                }
                loadImage();
            }
        });


    }

    private void showDialog() {
        AlertDialog.Builder d = new AlertDialog.Builder(this);
        d.setTitle(ctx.getString(R.string.check_image))
                .setMessage(ctx.getString(R.string.check_image_text))
                .setPositiveButton(ctx.getString(R.string.yes), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        startMap();
                    }
                })
                .setNegativeButton(ctx.getString(R.string.no), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .show();
    }
    private void startMap() {
        Intent i = new Intent(this,MonitorMapActivity.class);
        i.putExtra("projectSite",projectSite);
        i.putExtra("index", index);
        startActivity(i);
    }
    private void loadImage() {
        PhotoUploadDTO p = photoCache.getPhotoUploadList().get(index);

        String url = Util.getPhotoURL(p);

        ImageLoader.getInstance().displayImage(url, imageView, new ImageLoadingListener() {
            @Override
            public void onLoadingStarted(String s, View view) {

            }

            @Override
            public void onLoadingFailed(String s, View view, FailReason failReason) {

            }

            @Override
            public void onLoadingComplete(String s, View view,  Bitmap bitmap) {

                if (bitmap.getWidth() > bitmap.getHeight()) {
                    isLandscape = true;
                    //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                } else {
                    isLandscape = false;
                    //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                }
                Log.e(LOG, "------ onLoadingComplete - height: " + bitmap.getHeight() + " width: " + bitmap.getWidth()
                        + " isLandscape: " + isLandscape);
            }

            @Override
            public void onLoadingCancelled(String s, View view) {

            }
        });
        txtNumber.setText("" + (index + 1));
        txtDate.setText(sdf.format(p.getDateTaken()));
        animate();
        Util.animateRotationY(txtNext, 500);

    }

    private boolean isLandscape;

    private void animate() {
        Util.animateSlideRight(imageView, 500);
        Util.animateRotationY(txtNumber, 500);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.image, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected( MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_camera) {
            Intent i = new Intent(this,PictureActivity.class);
            if (projectSite != null) {
                i.putExtra("type", PhotoUploadDTO.SITE_IMAGE);
                i.putExtra("projectSite",projectSite);
            }
            if (project != null) {
                i.putExtra("type", PhotoUploadDTO.PROJECT_IMAGE);
                i.putExtra("project",project);
            }
            startActivityForResult(i, PICTURE_REQ);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    static final int PICTURE_REQ = 221;


    @Override
    public void onPause() {
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        super.onPause();
    }

}

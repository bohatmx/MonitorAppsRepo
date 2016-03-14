package com.boha.monitor.library.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.boha.monitor.library.dto.PhotoUploadDTO;
import com.boha.monitor.library.dto.ProjectDTO;
import com.boha.monitor.library.dto.RequestDTO;
import com.boha.monitor.library.dto.ResponseDTO;
import com.boha.monitor.library.fragments.PhotoScrollerFragment;
import com.boha.monitor.library.util.MenuColorizer;
import com.boha.monitor.library.util.MonLog;
import com.boha.monitor.library.util.NetUtil;
import com.boha.monitor.library.util.ThemeChooser;
import com.boha.monitor.library.util.Util;
import com.boha.platform.library.R;
import com.google.api.services.youtube.YouTube;

public class PhotoScrollerActivity extends AppCompatActivity implements PhotoScrollerFragment.PhotoListener {

    ResponseDTO response;
    PhotoScrollerFragment photoScrollerFragment;
    int position;
    ProjectDTO project;
    Menu mMenu;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeChooser.setTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_scroller);
        response = (ResponseDTO)getIntent().getSerializableExtra("photos");
        project = (ProjectDTO)getIntent().getSerializableExtra("project");
        position = getIntent().getIntExtra("position",0);

        photoScrollerFragment = (PhotoScrollerFragment)getSupportFragmentManager().findFragmentById(R.id.fragment);
        photoScrollerFragment.setPhotoList(response.getPhotoUploadList());
        photoScrollerFragment.setPosition(position);


        Util.setCustomActionBar(getApplicationContext(),
                getSupportActionBar(), project.getProjectName(),
                project.getCityName(),
                ContextCompat.getDrawable(getApplicationContext(), R.drawable.glasses));

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_photo_scroller, menu);
        mMenu = menu;
        MenuColorizer.colorMenu(this,menu, R.color.white);
        return true;
    }

    static final int THEME_REQUESTED = 1762;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_share) {
            if (photoScrollerFragment.getPhoto() != null) {
                startShare(photoScrollerFragment.getPhoto());
            }
            return true;
        }
        if (id == R.id.action_mark) {
            if (photoScrollerFragment.getPhoto() != null) {
                startMark(photoScrollerFragment.getPhoto());
            }

            return true;
        }
        if (id == R.id.action_help) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void startShare(PhotoUploadDTO photo) {
        //todo - create PhotoShareActivity -
        MonLog.d(getApplicationContext(),"PhotoScrollerActivity","will share photo in a while ............");
    }
    private void startMark(PhotoUploadDTO photo) {
        PhotoUploadDTO p = new PhotoUploadDTO();
        p.setPhotoUploadID(photo.getPhotoUploadID());
        p.setMarked(Boolean.TRUE);

        RequestDTO w = new RequestDTO(RequestDTO.UPDATE_PHOTO);
        w.setPhotoUpload(p);

        NetUtil.sendRequest(getApplicationContext(), w, new NetUtil.NetUtilListener() {
            @Override
            public void onResponse(ResponseDTO response) {
                MonLog.d(getApplicationContext(),"PhotoScrollerActivity","photo has been updated");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                    }
                });
            }

            @Override
            public void onError(final String message) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Util.showErrorToast(getApplicationContext(),message);
                    }
                });

            }
        });
        //https://www.youtube.com/channel/UCWHFm6uRFgfsOaXW31Bx2vg

    }
    @Override
    public void onPhotoClicked(PhotoUploadDTO photo) {
        Log.d(TAG, "onPhotoClicked() called with: " + "photo = [" + photo.getUri() + "]");
    }

    private static final String TAG = "PhotoScrollerActivity";
}

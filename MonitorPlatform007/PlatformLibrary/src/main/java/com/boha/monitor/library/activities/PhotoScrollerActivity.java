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
import com.boha.monitor.library.dto.ResponseDTO;
import com.boha.monitor.library.fragments.PhotoScrollerFragment;
import com.boha.monitor.library.util.MenuColorizer;
import com.boha.monitor.library.util.ThemeChooser;
import com.boha.monitor.library.util.Util;
import com.boha.platform.library.R;

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
                ContextCompat.getDrawable(getApplicationContext(), R.drawable.glasses48));

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
            return true;
        }
        if (id == R.id.action_mark) {
            Intent w = new Intent(this, ThemeSelectorActivity.class);

            return true;
        }
        if (id == R.id.action_help) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPhotoClicked(PhotoUploadDTO photo) {
        Log.d(TAG, "onPhotoClicked() called with: " + "photo = [" + photo.getUri() + "]");
    }

    private static final String TAG = "PhotoScrollerActivity";
}

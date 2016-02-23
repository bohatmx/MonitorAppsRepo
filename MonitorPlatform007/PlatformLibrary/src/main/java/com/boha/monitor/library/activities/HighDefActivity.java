package com.boha.monitor.library.activities;

import android.os.Bundle;
import android.app.Activity;
import android.support.v7.app.AppCompatActivity;

import com.boha.monitor.library.dto.PhotoUploadDTO;
import com.boha.monitor.library.fragments.HighDefFragment;
import com.boha.platform.library.R;

public class HighDefActivity extends AppCompatActivity {

    HighDefFragment highDefFragment;
    PhotoUploadDTO photo;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_high_def);

        photo = (PhotoUploadDTO)getIntent().getSerializableExtra("photo");
        highDefFragment = (HighDefFragment) getSupportFragmentManager().findFragmentById(R.id.fragment);
        highDefFragment.setPhoto(photo);
    }

}

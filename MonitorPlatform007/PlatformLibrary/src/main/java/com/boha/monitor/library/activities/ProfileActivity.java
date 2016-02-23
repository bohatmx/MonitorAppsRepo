package com.boha.monitor.library.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.boha.monitor.library.dto.MonitorDTO;
import com.boha.monitor.library.dto.Person;
import com.boha.monitor.library.dto.StaffDTO;
import com.boha.monitor.library.fragments.ProfileFragment;
import com.boha.monitor.library.fragments.StaffProfileFragment;
import com.boha.monitor.library.util.Snappy;
import com.boha.monitor.library.util.Util;
import com.boha.platform.library.R;

import java.util.ArrayList;
import java.util.List;

public class ProfileActivity extends AppCompatActivity
        implements ProfileFragment.ProfileListener {

    private int personType, editType,
            darkColor, primaryColor;

    ProfileFragment profileFragment;
    StaffProfileFragment staffProfileFragment;
    MonitorDTO monitor;
    StaffDTO staff;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monitor_profile);

        monitor = (MonitorDTO) getIntent().getSerializableExtra("monitor");
        staff = (StaffDTO) getIntent().getSerializableExtra("staff");
        personType = getIntent().getIntExtra("personType", 0);
        editType = getIntent().getIntExtra("editType", 0);

        if (findViewById(R.id.frameLayout) != null) {
            if (savedInstanceState != null) {
                return;
            }
            addProfileFragment();
        }
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        String name = "";
        if (monitor != null) {
            name = monitor.getFullName();
        }
        if (staff != null) {
            name = staff.getFullName();
        }
        Util.setCustomActionBar(
                getApplicationContext(),
                getSupportActionBar(),
                "Profile Management", name,
                ContextCompat.getDrawable(getApplicationContext(), R.drawable.glasses48));

    }

    private void addProfileFragment() {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();

        //ft.setCustomAnimations(R.anim.enter_from_left, R.anim.exit_to_right);
        profileFragment = new ProfileFragment();
        profileFragment.setThemeColors(primaryColor, darkColor);
        profileFragment.setListener(this);
        profileFragment.setMonitor(monitor);
        profileFragment.setStaff(staff);
        profileFragment.setEditType(editType);
        profileFragment.setPersonType(personType);

        ft.add(R.id.frameLayout, profileFragment);
        ft.commit();
    }

    boolean personAdded;
    Person person;
    @Override
    public void onBackPressed() {
        if (personAdded) {
            personAdded = false;
            Intent w = new Intent();
            if (person instanceof MonitorDTO) {
                w.putExtra("monitor", (MonitorDTO)person);
            }
            if (person instanceof StaffDTO) {
                w.putExtra("staff", (StaffDTO)person);
            }
            setResult(RESULT_OK, w);
        }
        finish();
    }

    @Override
    public void onUpdated(Person person) {

    }

    @Override
    public void onAdded(Person person) {
        this.person = person;
        personAdded = true;
    }

    @Override
    public void onPictureRequested(Person person) {

        Intent w = new Intent(getApplicationContext(),PictureActivity.class);
        if (person instanceof MonitorDTO) {
            w.putExtra("monitor", (MonitorDTO)person);
        }
        if (person instanceof StaffDTO) {
            w.putExtra("staff", (StaffDTO)person);
        }
        startActivityForResult(w, PICTURE_REQUESTED);
    }
    @Override
    public void onActivityResult(int req, int code, Intent data) {
        Log.d("ProfileActivity","onActivityResult req " + req + " code " + code);
        switch (req) {
            case PICTURE_REQUESTED:
                if (code == RESULT_OK) {
                    String path = data.getStringExtra("file");
                    Log.w("ProfileActivity", "image file path: " + path);
                    profileFragment.setPicture(path);
                }
                break;
        }
    }
    static final int PICTURE_REQUESTED = 364;
    @Override
    public void setBusy(boolean busy) {

    }
}

package com.boha.monitor.library.activities;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.boha.monitor.library.adapters.PhotoAdapter;
import com.boha.monitor.library.dto.PhotoUploadDTO;
import com.boha.monitor.library.dto.ProjectDTO;
import com.boha.monitor.library.dto.ProjectTaskDTO;
import com.boha.monitor.library.dto.ResponseDTO;
import com.boha.monitor.library.fragments.PageFragment;
import com.boha.monitor.library.fragments.PhotoGridFragment;
import com.boha.monitor.library.util.ThemeChooser;
import com.boha.monitor.library.util.Util;
import com.boha.platform.library.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class PhotoListActivity extends AppCompatActivity implements
        PhotoAdapter.PictureListener{

    public static final int
            PHOTO_LOCAL = 1,
            PHOTO_REMOTE = 2;
    ResponseDTO response;
    PagerAdapter adapter;
    ViewPager mPager;
    Context ctx;
    TextView txtCaption;
    int index, themeDarkColor, themePrimaryColor;
    ProjectDTO project;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeChooser.setTheme(this);
        Resources.Theme theme = getTheme();
        TypedValue typedValue = new TypedValue();
        theme.resolveAttribute(R.attr.colorPrimaryDark, typedValue, true);
        themeDarkColor = typedValue.data;
        theme.resolveAttribute(R.attr.colorPrimary, typedValue, true);
        themePrimaryColor = typedValue.data;

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo);
        ctx = getApplicationContext();
        mPager = (ViewPager)findViewById(R.id.pager);

        project = (ProjectDTO) getIntent().getSerializableExtra("project");
        response = (ResponseDTO) getIntent().getSerializableExtra("response");
        index = getIntent().getIntExtra("index", 0);

        if (project != null) {
            Util.setCustomActionBar(getApplicationContext(),
                    getSupportActionBar(), project.getProjectName(),
                    project.getCityName(),
                    ContextCompat.getDrawable(getApplicationContext(), R.drawable.glasses48));
            response = new ResponseDTO();
            response.setPhotoUploadList(new ArrayList<PhotoUploadDTO>());
            if (project.getPhotoUploadList() != null) {
                for (PhotoUploadDTO x : project.getPhotoUploadList()) {
                    response.getPhotoUploadList().add(x);

                }
            }

            for (ProjectTaskDTO s: project.getProjectTaskList()) {
                if (s.getPhotoUploadList() != null) {
                    for (PhotoUploadDTO z : s.getPhotoUploadList()) {
                        response.getPhotoUploadList().add(z);
                    }
                }
            }
        }
        if (response != null) {
            if (response.getPhotoUploadList() == null || response.getPhotoUploadList().isEmpty()) {
                Log.w("PhotoActivity", "--- no photos to display");
                finish();
            }
        }

        buildPhotoPages();

    }

    static final SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy");
    private void buildPhotoPages() {
        if (pageFragmentList == null) {
            pageFragmentList = new ArrayList<>();
        }

        Collections.sort(response.getPhotoUploadList());
        StringBuilder sb = new StringBuilder();
        if (!response.getPhotoUploadList().isEmpty()) {
            Date latest = new Date(response.getPhotoUploadList().get(0).getDateTaken().longValue());
            PhotoUploadDTO oldestPhoto = response.getPhotoUploadList().get(response.getPhotoUploadList().size() - 1);
            Date oldest = new java.sql.Date(oldestPhoto.getDateTaken().longValue());

            sb.append("Photos ").append(sdf.format(oldest));
            sb.append(" to ").append(sdf.format(latest));
            //getSupportActionBar().setSubtitle(sb.toString());

        }
        if (project != null) {
            ImageView logo = Util.setCustomActionBar(ctx, getSupportActionBar(),
                    project.getProjectName(),
                    sb.toString(),
                    ContextCompat.getDrawable(ctx, R.drawable.glasses48));
            logo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.e("PhotoListActivity", "### logo clicked");

                }
            });
        }
        pageFragmentList.add(PhotoGridFragment.newInstance(response));
//
//        int number = 0;
//        int total = response.getPhotoUploadList().size();
//        for (PhotoUploadDTO dto: response.getPhotoUploadList()) {
//            pageFragmentList.add(PhotoFragment.newInstance(dto, total - number));
//            number++;
//        }
        adapter = new PagerAdapter(getSupportFragmentManager());
        mPager.setOffscreenPageLimit(2);
        mPager.setAdapter(adapter);

//        mPager.setCurrentItem(index,true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_photo, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPictureClicked(PhotoUploadDTO photo, int position) {
        Log.e("PhotoListActivity", "photoClicked, id: " + photo.getPhotoUploadID() + " position: " + position);

        Intent w = new Intent(this, PhotoScrollerActivity.class);
        w.putExtra("photos", response);
        w.putExtra("position", position - 1);
        w.putExtra("project",project);

        startActivity(w);

    }

    private class PagerAdapter extends FragmentStatePagerAdapter {

        public PagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {

            return (Fragment) pageFragmentList.get(i);
        }

        @Override
        public int getCount() {
            return pageFragmentList.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            if (position == 0) {
                return "Photo Gallery";
            }
            return "Photo No. " + (response.getPhotoUploadList().size() - position + 1);
        }
    }

    List<PageFragment> pageFragmentList;
}

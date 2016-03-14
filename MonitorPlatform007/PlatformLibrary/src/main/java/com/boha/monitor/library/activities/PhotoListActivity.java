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
import com.boha.monitor.library.util.Snappy;
import com.boha.monitor.library.util.ThemeChooser;
import com.boha.monitor.library.util.Util;
import com.boha.platform.library.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class PhotoListActivity extends AppCompatActivity implements
        PhotoAdapter.PictureListener {


    ResponseDTO response;
    PagerAdapter adapter;
    ViewPager mPager;
    Context ctx;
    int index, themeDarkColor, themePrimaryColor;
    ProjectDTO project;
    List<PhotoUploadDTO> photoList;
    static final SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy");
    static final String LOG = PhotoListActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.e(LOG, "+++++++++++++++++++ PhotoListActivity onCreate");
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
        mPager = (ViewPager) findViewById(R.id.pager);

        project = (ProjectDTO) getIntent().getSerializableExtra("project");
        response = (ResponseDTO) getIntent().getSerializableExtra("response");
        index = getIntent().getIntExtra("index", 0);

        if (project != null) {
            Util.setCustomActionBar(getApplicationContext(),
                    getSupportActionBar(), project.getProjectName(),
                    project.getCityName(),
                    ContextCompat.getDrawable(getApplicationContext(), R.drawable.glasses));
            getCachedProject();
            return;
        }
        if (response != null) {
            if (!response.getPhotoUploadList().isEmpty()) {
                photoList = response.getPhotoUploadList();
                buildPhotoPages();
            }
        }


    }

    private void getCachedProject() {
        MonApp app = (MonApp) getApplication();
        Snappy.getProject(app, project.getProjectID(), new Snappy.SnappyProjectListener() {
            @Override
            public void onProjectFound(ProjectDTO p) {
                project = p;
                photoList = project.getPhotoUploadList();

                for (ProjectTaskDTO s : project.getProjectTaskList()) {
                    if (s.getPhotoUploadList() != null) {
                        for (PhotoUploadDTO z : s.getPhotoUploadList()) {
                            photoList.add(z);
                        }
                    }
                }

                buildPhotoPages();
            }

            @Override
            public void onError() {

            }
        });
    }



    private void buildPhotoPages() {
        pageFragmentList = new ArrayList<>();
        Collections.sort(photoList);
        StringBuilder sb = new StringBuilder();
        if (!photoList.isEmpty()) {
            Date latest = new Date(photoList.get(0).getDateTaken().longValue());
            PhotoUploadDTO oldestPhoto = photoList.get(photoList.size() - 1);
            Date oldest = new java.sql.Date(oldestPhoto.getDateTaken().longValue());

            sb.append("Photos ").append(sdf.format(oldest));
            sb.append(" to ").append(sdf.format(latest));
            //getSupportActionBar().setSubtitle(sb.toString());

        }
        if (project != null) {
            ImageView logo = Util.setCustomActionBar(ctx, getSupportActionBar(),
                    project.getProjectName(),
                    sb.toString(),
                    ContextCompat.getDrawable(ctx, R.drawable.glasses));
            logo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.e("PhotoListActivity", "### logo clicked");

                }
            });
        }
        pageFragmentList.add(PhotoGridFragment.newInstance(photoList));
        adapter = new PagerAdapter(getSupportFragmentManager());
        mPager.setOffscreenPageLimit(2);
        mPager.setAdapter(adapter);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_photo, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
         int id = item.getItemId();

        if (id == R.id.action_share) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPictureClicked(PhotoUploadDTO photo, int position) {
        Log.e("PhotoListActivity", "photoClicked, id: " + photo.getPhotoUploadID() + " position: " + position);

        Intent w = new Intent(this, PhotoScrollerActivity.class);
        response = new ResponseDTO();
        response.setPhotoUploadList(photoList);
        w.putExtra("photos", response);
        w.putExtra("position", position - 1);
        w.putExtra("project", project);

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

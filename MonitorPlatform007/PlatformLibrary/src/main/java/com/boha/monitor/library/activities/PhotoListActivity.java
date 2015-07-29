package com.boha.monitor.library.activities;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.boha.monitor.library.dto.PhotoUploadDTO;
import com.boha.monitor.library.dto.ProjectDTO;
import com.boha.monitor.library.dto.ProjectTaskDTO;
import com.boha.monitor.library.dto.ResponseDTO;
import com.boha.monitor.library.fragments.PageFragment;
import com.boha.monitor.library.fragments.PhotoFragment;
import com.boha.monitor.library.fragments.PhotoGridFragment;
import com.boha.platform.library.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PhotoListActivity extends AppCompatActivity {

    public static final int
            PHOTO_LOCAL = 1,
            PHOTO_REMOTE = 2;
    PhotoFragment photoFragment;
    ResponseDTO response;
    PagerAdapter adapter;
    ViewPager mPager;
    Context ctx;
    TextView txtCaption;
    int index;
    ProjectDTO project;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo);
        ctx = getApplicationContext();
        mPager = (ViewPager)findViewById(R.id.pager);


        project = (ProjectDTO) getIntent().getSerializableExtra("project");
        response = (ResponseDTO) getIntent().getSerializableExtra("response");
        index = getIntent().getIntExtra("index", 0);

        if (project != null) {
            setTitle(project.getProjectName());
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

    private void buildPhotoPages() {
        if (pageFragmentList == null) {
            pageFragmentList = new ArrayList<>();
        }

        Collections.sort(response.getPhotoUploadList());
        pageFragmentList.add(PhotoGridFragment.newInstance(response));

        for (PhotoUploadDTO dto: response.getPhotoUploadList()) {
            pageFragmentList.add(PhotoFragment.newInstance(dto));
        }
        adapter = new PagerAdapter(getSupportFragmentManager());
        mPager.setAdapter(adapter);

        mPager.setCurrentItem(index,true);
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
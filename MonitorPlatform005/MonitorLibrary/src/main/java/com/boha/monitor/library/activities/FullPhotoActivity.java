package com.boha.monitor.library.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;

import com.boha.monitor.library.R;
import com.boha.monitor.library.adapters.FullPictureAdapter;
import com.boha.monitor.library.dto.ProjectSiteDTO;
import com.boha.monitor.library.dto.transfer.PhotoUploadDTO;
import com.boha.monitor.library.util.DividerItemDecoration;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class FullPhotoActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    int index;
    FullPictureAdapter adapter;
    ProjectSiteDTO projectSite;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_photo);
        recyclerView = (RecyclerView)findViewById(R.id.FI_recyclerView);

        projectSite = (ProjectSiteDTO)getIntent().getSerializableExtra("projectSite");
        index = getIntent().getIntExtra("index",0);
        setTitle(projectSite.getProjectSiteName());
        getSupportActionBar().setSubtitle(projectSite.getProjectName());

        int counter = 0;
        for (PhotoUploadDTO p: projectSite.getPhotoUploadList()) {
            p.setIndex(projectSite.getPhotoUploadList().size() - counter );
            counter++;
        }
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext(),LinearLayoutManager.HORIZONTAL, false));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new DividerItemDecoration(getApplicationContext(), RecyclerView.HORIZONTAL));
        adapter = new FullPictureAdapter(projectSite.getPhotoUploadList(), 1, getApplicationContext(), new FullPictureAdapter.PictureListener() {
            @Override
            public void onPictureClicked(int position) {

            }
        });

        recyclerView.setAdapter(adapter);
        recyclerView.scrollToPosition(index);


//        int position = 0;
//        imageLayout.removeAllViews();
//        for (PhotoUploadDTO pu : resp.getPhotoUploadList()) {
//            View v = getLayoutInflater().inflate(R.layout.full_photo, null);
//            ImageView image = (ImageView) v.findViewById(R.id.FI_image);
//            ImageLoader.getInstance().displayImage(Statics.IMAGE_URL + pu.getUri(), image);
//            TextView num = (TextView) v.findViewById(R.id.FI_number);
//            TextView date = (TextView) v.findViewById(R.id.FI_date);
//            int sdf = resp.getPhotoUploadList().size() - (position);
//            num.setText("" + sdf);
//            date.setText(sdf.format(pu.getDateTaken()));
//            imageLayout.addView(v);
//            position++;
//            if (position == 30) {
//                break;
//            }
//        }
//        horizontalScrollView.post(new Runnable() {
//
//            @Override
//            public void run() {
//                int scrollTo = 0;
//                final View v = imageLayout.getChildAt(0);
//                scrollTo = v.getWidth() * index;
//                horizontalScrollView.scrollTo(scrollTo, 0);
//            }
//        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_full_photo, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected( MenuItem item) {
         int id = item.getItemId();

         if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    static final Locale loc = Locale.getDefault();
    static final SimpleDateFormat sdf = new SimpleDateFormat("EEEE, dd MMMM yyyy HH:mm", loc);
}

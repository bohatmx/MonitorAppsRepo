package com.com.boha.monitor.library.activities;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.boha.monitor.library.R;
import com.com.boha.monitor.library.adapters.PictureRecyclerAdapter;
import com.com.boha.monitor.library.dto.CompanyStaffDTO;
import com.com.boha.monitor.library.dto.ProjectSiteDTO;
import com.com.boha.monitor.library.util.DividerItemDecoration;
import com.com.boha.monitor.library.util.SharedUtil;
import com.com.boha.monitor.library.util.Statics;

public class PictureRecyclerGridActivity extends ActionBarActivity {

    RecyclerView list;
    TextView title;
    Context ctx;
    ProjectSiteDTO projectSite;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ctx = getApplicationContext();
        setContentView(R.layout.activity_picture_recycler_grid);
        list = (RecyclerView) findViewById(R.id.RCV_list);
        title = (TextView) findViewById(R.id.RCV_title);

        list.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
        list.setItemAnimator(new DefaultItemAnimator());
        list.addItemDecoration(new DividerItemDecoration(ctx, RecyclerView.VERTICAL));

        projectSite = (ProjectSiteDTO)getIntent().getSerializableExtra("projectSite");
        if (projectSite != null) {
            title.setText(getString(R.string.progress_images) + ": " + projectSite.getBeneficiary().getFullName());
            adapter = new PictureRecyclerAdapter(projectSite.getPhotoUploadList(), 1, ctx);
            list.setAdapter(adapter);
        }

        setTitle(SharedUtil.getCompany(ctx).getCompanyName());
        CompanyStaffDTO staff = SharedUtil.getCompanyStaff(ctx);
        getSupportActionBar().setSubtitle(staff.getFullName());
        Statics.setRobotoFontLight(ctx,title);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_picture_recycler_grid, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    PictureRecyclerAdapter adapter;

    @Override
    public void onPause() {
        overridePendingTransition(com.boha.monitor.library.R.anim.slide_in_left, com.boha.monitor.library.R.anim.slide_out_right);
        super.onPause();
    }

}

package com.boha.monitor.library.activities;

import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.boha.monitor.library.adapters.ProjectAdapter;
import com.boha.monitor.library.dto.ProjectDTO;
import com.boha.monitor.library.fragments.ProjectListFragment;
import com.boha.platform.library.R;

import java.util.ArrayList;
import java.util.List;

public class TestActivity extends AppCompatActivity {

    RecyclerView mRecyclerView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.atest);

        CollapsingToolbarLayout x = (CollapsingToolbarLayout)findViewById(R.id.toolBarLayout);
        x.setTitle("Crafty Old Gun!");

//        mRecyclerView = (RecyclerView) findViewById(R.id.recycler);
//
//        LinearLayoutManager llm = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
//        mRecyclerView.setLayoutManager(llm);
//
//        mRecyclerView.setHasFixedSize(true);
//
//
//        String prog = "Programme Numero Uno";
//        for (int i = 0; i < 10; i++) {
//            ProjectDTO y = new ProjectDTO();
//            y.setProjectName("This is Project #" +(i + 1));
//            y.setProgrammeName(prog);
//            projectList.add(y);
//        }
//        setList();

    }

    ProjectAdapter projectAdapter;
    List<ProjectDTO> projectList = new ArrayList<>();
    int darkColor = R.color.teal_900;
    static final String LOG = TestActivity.class.getSimpleName();


    private void setList() {
        Log.e("TestActivity", "### setList");


        projectAdapter = new ProjectAdapter(projectList, this, darkColor,new ProjectListFragment.ProjectListFragmentListener() {
            @Override
            public void onCameraRequired(ProjectDTO project) {
                Log.d(LOG, "### onCameraRequired");
            }

            @Override
            public void onStatusUpdateRequired(ProjectDTO project) {
                Log.d(LOG, "### onStatusUpdateRequired");
            }

            @Override
            public void onLocationRequired(ProjectDTO project) {
                Log.d(LOG, "### onLocationRequired");
            }

            @Override
            public void onDirectionsRequired(ProjectDTO project) {
                Log.d(LOG, "### onDirectionsRequired");
            }

            @Override
            public void onMessagingRequired(ProjectDTO project) {
                Log.d(LOG, "### onMessagingRequired");
            }

            @Override
            public void onGalleryRequired(ProjectDTO project) {
                Log.d(LOG, "### onGalleryRequired");
            }

            @Override
            public void onStatusReportRequired(ProjectDTO project) {
                Log.d(LOG, "### onStatusReportRequired");
            }

            @Override
            public void onMapRequired(ProjectDTO project) {
                Log.i("TestActivity", "### onMapRequired");
            }
        });
        mRecyclerView.setAdapter(projectAdapter);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_test, menu);
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
}

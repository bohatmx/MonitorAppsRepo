package com.boha.monitor.setup.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.boha.monitor.library.dto.PortfolioDTO;
import com.boha.monitor.library.dto.ProgrammeDTO;
import com.boha.monitor.library.dto.ProjectDTO;
import com.boha.monitor.library.dto.ResponseDTO;
import com.boha.monitor.library.util.CacheUtil;
import com.boha.monitor.library.util.DividerItemDecoration;
import com.boha.monitor.setup.R;
import com.boha.monitor.setup.adapters.ProjectDataAdapter;
import com.boha.monitor.setup.fragments.ProjectDataListFragment;
import com.yqritc.recyclerviewflexibledivider.HorizontalDividerItemDecoration;

import java.util.List;

public class ProjectDataListActivity extends AppCompatActivity implements ProjectDataAdapter.ProjectListener {

    ProjectDataListFragment projectDataListFragment;
    ProgrammeDTO programme;
    Integer programmeID, portfolioID;
    List<ProjectDTO> projectList;
    RecyclerView recyclerView;
    Context ctx;
    TextView txt;
    ProjectDataAdapter adapter;

    Menu mMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project_data);
        ctx = getApplicationContext();

        txt = (TextView)findViewById(R.id.text);

        portfolioID = getIntent().getIntExtra("portfolioID", 0);
        programmeID = getIntent().getIntExtra("programmeID", 0);
        getPortfolioList();
        setTitle("Project List");
    }

    private void setList() {
        recyclerView = (RecyclerView)findViewById(R.id.recycler);
        LinearLayoutManager manager = new LinearLayoutManager(ctx, LinearLayoutManager.VERTICAL,false);
        recyclerView.setLayoutManager(manager);
        recyclerView.addItemDecoration(new DividerItemDecoration(ctx, DividerItemDecoration.VERTICAL_LIST));
        recyclerView.addItemDecoration(
                new HorizontalDividerItemDecoration.Builder(this)
                        .color(R.color.grey)
                        .sizeResId(R.dimen.mon_divider)
                        .marginResId(R.dimen.mon_divider, R.dimen.mon_divider)
                        .build());
        adapter = new ProjectDataAdapter(projectList, ctx, new ProjectDataAdapter.ProjectListener() {
            @Override
            public void onProjectClicked(ProjectDTO project) {
            }

            @Override
            public void onTaskCountClicked(ProjectDTO project) {
            }

            @Override
            public void onStaffCount(ProjectDTO project) {
            }

            @Override
            public void onMonitorCount(ProjectDTO project) {
            }

            @Override
            public void onIconGetLocationClicked(ProjectDTO project) {
            }

            @Override
            public void onIconDeleteClicked(ProjectDTO project, int position) {
            }

            @Override
            public void onIconEditClicked(ProjectDTO project, int position) {
            }

            @Override
            public void setBusy(boolean busy) {
                setRefreshActionButtonState(busy);
            }
        });

        recyclerView.setAdapter(adapter);


    }

    private void getPortfolioList() {
        CacheUtil.getCachedPortfolioList(ctx, new CacheUtil.CacheUtilListener() {
            @Override
            public void onFileDataDeserialized(ResponseDTO response) {
                if (response.getPortfolioList() != null) {
                    for (PortfolioDTO x : response.getPortfolioList()) {
                        if (x.getPortfolioID().intValue() == portfolioID.intValue()) {
                            for (ProgrammeDTO y : x.getProgrammeList()) {
                                if (y.getProgrammeID().intValue() == programmeID.intValue()) {
                                    programme = y;
                                    txt.setText(programme.getProgrammeName());
                                    projectList = y.getProjectList();
                                    setList();
                                    break;
                                }
                            }
                        }
                    }
                }
            }

            @Override
            public void onDataCached() {

            }

            @Override
            public void onError() {

            }
        });    }

    boolean companyDataRefreshed;

    @Override
    public void onBackPressed() {
        if (companyDataRefreshed) {
            Intent w = new Intent();
            w.putExtra("programme", programme);
            setResult(RESULT_OK, w);
        } else {
            setResult(RESULT_CANCELED);
        }

        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_general, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_refresh) {
            getPortfolioList();
            return true;
        }
        if (id == R.id.action_help) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void setRefreshActionButtonState(final boolean refreshing) {
        if (mMenu != null) {
            final MenuItem refreshItem = mMenu.findItem(R.id.action_refresh);
            if (refreshItem != null) {
                if (refreshing) {
                    refreshItem.setActionView(R.layout.action_bar_progess);
                } else {
                    refreshItem.setActionView(null);
                }
            }
        }
    }

    @Override
    public void onProjectClicked(ProjectDTO project) {

    }

    @Override
    public void onTaskCountClicked(ProjectDTO project) {

    }

    @Override
    public void onStaffCount(ProjectDTO project) {

    }

    @Override
    public void onMonitorCount(ProjectDTO project) {

    }

    @Override
    public void onIconGetLocationClicked(ProjectDTO project) {

    }

    @Override
    public void onIconDeleteClicked(ProjectDTO project, int position) {

    }

    @Override
    public void onIconEditClicked(ProjectDTO project, int position) {

    }

    @Override
    public void setBusy(boolean busy) {

    }
    static final String LOG = ProjectDataListActivity.class.getSimpleName();
}

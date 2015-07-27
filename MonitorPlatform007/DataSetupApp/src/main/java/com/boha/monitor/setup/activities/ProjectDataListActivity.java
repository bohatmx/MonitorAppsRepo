package com.boha.monitor.setup.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.boha.monitor.library.dto.CompanyDTO;
import com.boha.monitor.library.dto.PortfolioDTO;
import com.boha.monitor.library.dto.ProgrammeDTO;
import com.boha.monitor.library.dto.ProjectDTO;
import com.boha.monitor.library.dto.ResponseDTO;
import com.boha.monitor.library.util.CacheUtil;
import com.boha.monitor.setup.R;
import com.boha.monitor.setup.adapters.ProjectDataAdapter;
import com.boha.monitor.setup.fragments.ProjectDataListFragment;

public class ProjectDataListActivity extends AppCompatActivity implements ProjectDataAdapter.ProjectListener {

    ProjectDataListFragment projectDataListFragment;
    ProgrammeDTO programme;
    Integer programmeID;

    Menu mMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project_list);

        programmeID = getIntent().getIntExtra("programmeID", 0);

        projectDataListFragment = (ProjectDataListFragment) getSupportFragmentManager()
                .findFragmentById(R.id.fragment);
        CacheUtil.getCachedData(getApplicationContext(), CacheUtil.CACHE_DATA, new CacheUtil.CacheUtilListener() {
            @Override
            public void onFileDataDeserialized(ResponseDTO response) {
                if (response.getCompanyList() != null) {
                    for (CompanyDTO w : response.getCompanyList()) {
                        for (PortfolioDTO x : w.getPortfolioList()) {
                            for (ProgrammeDTO y : x.getProgrammeList()) {
                                if (programmeID.intValue() == y.getProgrammeID().intValue()) {
                                    programme = y;
                                    projectDataListFragment.setProgramme(programme);
                                    setTitle(getString(R.string.prog_projects));
                                    getSupportActionBar().setSubtitle(programme.getProgrammeName());
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
        });


    }


    private void refreshCompanyData() {
//        RequestDTO w = new RequestDTO(RequestDTO.GET_COMPANY_DATA);
//        w.setCompanyID(programme.getCompanyID());
//
//        companyDataRefreshed = false;
//        setRefreshActionButtonState(true);
//        NetUtil.sendRequest(getApplicationContext(), w, new NetUtil.NetUtilListener() {
//            @Override
//            public void onResponse(final ResponseDTO response) {
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        setRefreshActionButtonState(false);
//
//                        companyDataRefreshed = true;
//                    }
//                });
//
//            }
//
//            @Override
//            public void onError(final String message) {
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        Util.showErrorToast(getApplicationContext(),message);
//                    }
//                });
//            }
//
//            @Override
//            public void onWebSocketClose() {
//
//            }
//        });
    }

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
            refreshCompanyData();
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
}

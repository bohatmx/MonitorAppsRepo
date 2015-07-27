package com.boha.monitor.setup.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.boha.monitor.library.dto.CompanyDTO;
import com.boha.monitor.library.dto.PortfolioDTO;
import com.boha.monitor.library.dto.ProgrammeDTO;
import com.boha.monitor.library.dto.RequestDTO;
import com.boha.monitor.library.dto.ResponseDTO;
import com.boha.monitor.library.util.CacheUtil;
import com.boha.monitor.library.util.NetUtil;
import com.boha.monitor.library.util.Util;
import com.boha.monitor.setup.R;
import com.boha.monitor.setup.adapters.ProgrammeAdapter;
import com.boha.monitor.setup.fragments.FileImportFragment;
import com.boha.monitor.setup.fragments.ProgrammeListFragment;

public class ProgrammeListActivity extends AppCompatActivity
        implements ProgrammeAdapter.ProgrammeListener {

    ProgrammeListFragment programmeListFragment;
    PortfolioDTO portfolio;
    Integer portfolioID;

    Menu mMenu;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_programme_list);

        portfolioID = getIntent().getIntExtra("portfolioID",0);
        CacheUtil.getCachedData(getApplicationContext(), CacheUtil.CACHE_DATA, new CacheUtil.CacheUtilListener() {
            @Override
            public void onFileDataDeserialized(ResponseDTO response) {
                if (response.getCompanyList() != null) {
                    for (CompanyDTO x : response.getCompanyList()) {
                        for (PortfolioDTO y : x.getPortfolioList()) {
                            if (y.getPortfolioID().intValue() == portfolioID.intValue()) {
                                programmeListFragment = (ProgrammeListFragment) getSupportFragmentManager().findFragmentById(R.id.fragment);
                                programmeListFragment.setPortfolio(y);

                                setTitle("Portfolio Programmes");
                                getSupportActionBar().setSubtitle(y.getPortfolioName());
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
        RequestDTO w = new RequestDTO(RequestDTO.GET_COMPANY_DATA);
        w.setCompanyID(portfolio.getCompanyID());

        companyDataRefreshed = false;
        setRefreshActionButtonState(true);
        NetUtil.sendRequest(getApplicationContext(), w, new NetUtil.NetUtilListener() {
            @Override
            public void onResponse(final ResponseDTO response) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        setRefreshActionButtonState(false);

                        companyDataRefreshed = true;
                    }
                });

            }

            @Override
            public void onError(final String message) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Util.showErrorToast(getApplicationContext(),message);
                    }
                });
            }

            @Override
            public void onWebSocketClose() {

            }
        });
    }
    boolean companyDataRefreshed;

    public void refreshProgramme(ProgrammeDTO programme) {

    }
    @Override
    public void onBackPressed() {
        if (companyDataRefreshed) {
            Intent w = new Intent();
            w.putExtra("programme", portfolio);
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

    @Override
    public void setBusy(boolean busy) {
        setRefreshActionButtonState(busy);
    }

    @Override
    public void onProgrammeClicked(ProgrammeDTO programme) {
        selectedProgramme = programme;
    }

    @Override
    public void onProjectCountClicked(ProgrammeDTO programme) {
        selectedProgramme = programme;
        Intent w = new Intent(this,ProjectDataListActivity.class);
        w.putExtra("programmeID",programme.getProgrammeID());
        startActivityForResult(w,PROJECTS_REQUESTED);
    }

    @Override
    public void onTaskTypeCountClicked(ProgrammeDTO programme) {
        selectedProgramme = programme;
        Intent w = new Intent(this,TaskTypeListActivity.class);
        w.putExtra("programmeID",programme.getProgrammeID());
        startActivityForResult(w, TASK_TYPES_REQUESTED);
    }



    @Override
    public void onTaskImportRequested(ProgrammeDTO programme) {
        selectedProgramme = programme;
        Intent w = new Intent(this, FileImportActivity.class);
        w.putExtra("programme",programme);
        w.putExtra("importType", FileImportFragment.IMPORT_TASKS);
        startActivityForResult(w, TASK_IMPORT_REQUESTED);
    }

    static final int
            TASK_IMPORT_REQUESTED = 965,
            PROJECT_IMPORT_REQUESTED = 966,
            TASK_TYPES_REQUESTED = 967,
            PROJECTS_REQUESTED = 968;

    ProgrammeDTO selectedProgramme;
    @Override
    public void onProjectImportRequested(ProgrammeDTO programme) {
        selectedProgramme = programme;
        Intent w = new Intent(this, FileImportActivity.class);
        w.putExtra("programme",programme);
        w.putExtra("importType", FileImportFragment.IMPORT_PROJECTS);
        startActivityForResult(w, PROJECT_IMPORT_REQUESTED);

    }

    @Override
    public void onActivityResult(int reqCode, int resultCode, Intent data) {

        switch (reqCode) {
            case TASK_IMPORT_REQUESTED:
                if (resultCode == RESULT_OK) {
                    Log.i("xx", "### Tasks imported");
                    ResponseDTO dto = (ResponseDTO)data.getSerializableExtra("taskTypeList");
                    selectedProgramme.setTaskTypeList(dto.getTaskTypeList());
                    programmeListFragment.refreshProgramme(selectedProgramme);
                }
                break;
            case PROJECT_IMPORT_REQUESTED:
                if (resultCode == RESULT_OK) {
                    Log.i("xx", "### Programme Projects imported");
                    ResponseDTO dto = (ResponseDTO)data.getSerializableExtra("projectList");
                    selectedProgramme.setProjectList(dto.getProjectList());
                    programmeListFragment.refreshProgramme(selectedProgramme);
                }
                break;
            case PROJECTS_REQUESTED:

                break;
        }
    }
    static final int PROJECT_ADDED = 4131;
    @Override
    public void onCompanyDataRefreshed(ResponseDTO response, Integer companyID) {

    }

    @Override
    public void onIconDeleteClicked(ProgrammeDTO programme, int position) {

    }

    @Override
    public void onIconEditClicked(ProgrammeDTO programme, int position) {

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
}

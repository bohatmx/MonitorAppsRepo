package com.boha.monitor.setup.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.boha.monitor.library.dto.PortfolioDTO;
import com.boha.monitor.library.dto.ProgrammeDTO;
import com.boha.monitor.library.dto.ResponseDTO;
import com.boha.monitor.library.util.CacheUtil;
import com.boha.monitor.library.util.DividerItemDecoration;
import com.boha.monitor.setup.R;
import com.boha.monitor.setup.adapters.ProgrammeAdapter;
import com.boha.monitor.setup.fragments.FileImportFragment;
import com.boha.monitor.setup.fragments.ProgrammeListFragment;

import java.util.List;

public class ProgrammeListActivity extends AppCompatActivity
        implements ProgrammeAdapter.ProgrammeListener {

    ProgrammeListFragment programmeListFragment;
    PortfolioDTO portfolio;
    Integer portfolioID;
    List<ProgrammeDTO> programmeList;
    RecyclerView recyclerView;
    TextView txt;
    ProgrammeAdapter adapter;
    Context ctx;

    Menu mMenu;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_programme_data);
        ctx = getApplicationContext();
        recyclerView = (RecyclerView)findViewById(R.id.recycler);
        txt = (TextView)findViewById(R.id.text);
        portfolioID = getIntent().getIntExtra("portfolioID",0);
        getCachedPortfolioData();
        setTitle("Programme List");

    }

    private void setList() {
        LinearLayoutManager llm = new LinearLayoutManager(ctx,LinearLayoutManager.VERTICAL,false);
        adapter = new ProgrammeAdapter(programmeList, ctx, new ProgrammeAdapter.ProgrammeListener() {
            @Override
            public void onProgrammeClicked(ProgrammeDTO programme) {

            }

            @Override
            public void onProjectCountClicked(ProgrammeDTO programme) {
                Intent w = new Intent(ctx, ProjectDataListActivity.class);
                w.putExtra("portfolioID", programme.getPortfolioID() );
                w.putExtra("programmeID", programme.getProgrammeID());

                startActivityForResult(w, 1324);
            }

            @Override
            public void onTaskTypeCountClicked(ProgrammeDTO programme) {

            }

            @Override
            public void onTaskImportRequested(ProgrammeDTO programme) {

            }

            @Override
            public void onProjectImportRequested(ProgrammeDTO programme) {

            }

            @Override
            public void onIconDeleteClicked(ProgrammeDTO programme, int position) {

            }

            @Override
            public void onIconEditClicked(ProgrammeDTO programme, int position) {

            }

            @Override
            public void onCompanyDataRefreshed(ResponseDTO response, Integer companyID) {

            }

            @Override
            public void setBusy(boolean busy) {

            }
        });

        recyclerView.setLayoutManager(llm);
        recyclerView.addItemDecoration(new DividerItemDecoration(ctx, DividerItemDecoration.VERTICAL_LIST ));
        recyclerView.setAdapter(adapter);
    }

    private void getCachedPortfolioData() {
        CacheUtil.getCachedPortfolioList(getApplicationContext(), new CacheUtil.CacheUtilListener() {
            @Override
            public void onFileDataDeserialized(ResponseDTO response) {

                if (response.getPortfolioList() == null || response.getPortfolioList().isEmpty()) {
                    return;
                } else {
                    for (PortfolioDTO v : response.getPortfolioList()) {
                        if (v.getPortfolioID().intValue() == portfolioID.intValue()) {
                            programmeList = v.getProgrammeList();
                            txt.setText(v.getPortfolioName());
                            portfolio = v;
                            setList();
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

    boolean companyDataRefreshed;


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
        w.putExtra("portfolioID",programme.getPortfolioID());
        startActivityForResult(w,PROJECTS_REQUESTED);
    }

    @Override
    public void onTaskTypeCountClicked(ProgrammeDTO programme) {
        selectedProgramme = programme;
        Intent w = new Intent(this,TaskTypeListActivity.class);
        w.putExtra("programmeID",programme.getProgrammeID());
        w.putExtra("portfolioID", programme.getPortfolioID());
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

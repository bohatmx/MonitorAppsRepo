package com.boha.monitor.setup.activities;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.boha.monitor.library.dto.MonitorDTO;
import com.boha.monitor.library.dto.PortfolioDTO;
import com.boha.monitor.library.dto.ProgrammeDTO;
import com.boha.monitor.library.dto.ResponseDTO;
import com.boha.monitor.library.dto.StaffDTO;
import com.boha.monitor.library.util.CacheUtil;
import com.boha.monitor.library.util.DividerItemDecoration;
import com.boha.monitor.setup.R;
import com.boha.monitor.setup.adapters.ProjectSelectionAdapter;

import java.util.ArrayList;
import java.util.List;

public class ProjectAssignmentActivity extends AppCompatActivity {

    StaffDTO staff;
    MonitorDTO monitor;
    List<ProgrammeDTO> programmeList;
    ProgrammeDTO programme;
    Context ctx;
    Spinner spinner;
    ProjectSelectionAdapter adapter;
    TextView txtName, txtCount;
    RecyclerView recycler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project_assignment);
        ctx = getApplicationContext();

        setFields();
        getCachedData();

    }

    private void getCachedData() {
        CacheUtil.getCachedPortfolioList(ctx, new CacheUtil.CacheUtilListener() {
            @Override
            public void onFileDataDeserialized(ResponseDTO response) {
                if (response.getPortfolioList() != null) {
                    for (PortfolioDTO x : response.getPortfolioList()) {
                        programmeList.addAll(x.getProgrammeList());
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

    private void setFields() {
        spinner = (Spinner)findViewById(R.id.spinner);
        txtCount = (TextView)findViewById(R.id.count);
        txtName = (TextView)findViewById(R.id.name);
        recycler = (RecyclerView)findViewById(R.id.recycler);
        LinearLayoutManager k = new LinearLayoutManager(ctx,LinearLayoutManager.VERTICAL, false);
        recycler.setLayoutManager(k);
        recycler.addItemDecoration(new DividerItemDecoration(ctx, DividerItemDecoration.VERTICAL_LIST));

        txtCount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

    }
    private void setList() {
        adapter = new ProjectSelectionAdapter(programme.getProjectList(),ctx);
        recycler.setAdapter(adapter);
    }
    private void setSpinner() {
        List<String> list = new ArrayList<>();
        list.add("Select Programme");
        for (ProgrammeDTO x: programmeList) {
            list.add(x.getProgrammeName());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(ctx,android.R.layout.simple_spinner_item, list);
        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    programme = null;
                } else {
                    programme = programmeList.get(position - 1);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }
    private void saveMonitorAssignments() {

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_general, menu);
        mMenu = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_refresh) {
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
    Menu mMenu;
}

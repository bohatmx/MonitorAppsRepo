package com.boha.monitor.library.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;

import com.boha.monitor.library.dto.MonitorDTO;
import com.boha.monitor.library.dto.MonitorProjectDTO;
import com.boha.monitor.library.dto.ResponseDTO;
import com.boha.monitor.library.dto.StaffDTO;
import com.boha.monitor.library.dto.StaffProjectDTO;
import com.boha.monitor.library.fragments.ProjectSelectionFragment;
import com.boha.monitor.library.util.SharedUtil;
import com.boha.monitor.library.util.Util;
import com.boha.platform.library.R;

import java.util.List;

public class ProjectSelectionActivity extends AppCompatActivity implements ProjectSelectionFragment.SelectionListener{

    ProjectSelectionFragment projectSelectionFragment;
    MonitorDTO monitor;
    StaffDTO staff;
    Context ctx;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project_selection);
        ctx = getApplicationContext();

        staff = (StaffDTO) getIntent().getSerializableExtra("staff");
        monitor = (MonitorDTO) getIntent().getSerializableExtra("monitor");
        projectSelectionFragment = (ProjectSelectionFragment)
                getSupportFragmentManager().findFragmentById(R.id.fragment);
        if (monitor != null)
            projectSelectionFragment.setMonitor(monitor);
        if (staff != null)
            projectSelectionFragment.setStaff(staff);

        ActionBar bar = getSupportActionBar();

        Util.setCustomActionBar(ctx, bar,
                SharedUtil.getCompany(ctx).getCompanyName(),
                "Project Assignment",
                ContextCompat.getDrawable(ctx, R.drawable.glasses48));

    }

    @Override
    public void onSelectionCompleteForStaff(List<StaffProjectDTO> list) {
        staffProjectList = list;
        onBackPressed();
    }

    @Override
    public void onSelectionCompleteForMonitor(List<MonitorProjectDTO> list) {
        monitorProjectList = list;
        onBackPressed();
    }
    List<MonitorProjectDTO> monitorProjectList;
    List<StaffProjectDTO> staffProjectList;
    @Override
    public void onBackPressed() {
        if (monitorProjectList != null) {
            ResponseDTO w = new ResponseDTO();
            w.setMonitorProjectList(monitorProjectList);
            Intent m = new Intent();
            m.putExtra("monitorProjectList", w);
            setResult(RESULT_OK,m);
        }
        if (staffProjectList != null) {
            ResponseDTO w = new ResponseDTO();
            w.setStaffProjectList(staffProjectList);
            Intent m = new Intent();
            m.putExtra("staffProjectList", w);
            setResult(RESULT_OK,m);
        }
        finish();
    }
}

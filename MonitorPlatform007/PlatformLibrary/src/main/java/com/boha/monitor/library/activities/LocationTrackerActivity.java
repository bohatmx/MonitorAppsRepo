package com.boha.monitor.library.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.boha.monitor.library.dto.LocationTrackerDTO;
import com.boha.monitor.library.dto.RequestDTO;
import com.boha.monitor.library.dto.ResponseDTO;
import com.boha.monitor.library.dto.StaffDTO;
import com.boha.monitor.library.fragments.LocationTrackerListFragment;
import com.boha.monitor.library.util.NetUtil;
import com.boha.monitor.library.util.SharedUtil;
import com.boha.monitor.library.util.Util;
import com.boha.platform.library.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class LocationTrackerActivity extends AppCompatActivity implements LocationTrackerListFragment.LocationTrackerListFragmentListener{

    LocationTrackerListFragment locationTrackerListFragment;
    Context ctx;
    List<LocationTrackerDTO> locationTrackerList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_tracker);
        ctx = getApplicationContext();
        locationTrackerListFragment = (LocationTrackerListFragment) getSupportFragmentManager().findFragmentById(R.id.fragment);
        refreshTrackerList();
    }

    public void refreshTrackerList() {
        //// TODO: 16/01/05 set up more query types, ie by staff, monitor & company
        RequestDTO req = new RequestDTO(RequestDTO.GET_LOCATION_TRACK_BY_COMPANY_IN_PERIOD);
        StaffDTO staff = SharedUtil.getCompanyStaff(ctx);
        if (staff != null) {
            req.setCompanyID(staff.getCompanyID());
        }

        NetUtil.sendRequest(ctx, req, new NetUtil.NetUtilListener() {
            @Override
            public void onResponse(ResponseDTO response) {
                locationTrackerList = response.getLocationTrackerList();
                Log.i("LTList", "found " + locationTrackerList.size() + " trackers");
                locationTrackerListFragment.setList(filterLatest());
            }

            @Override
            public void onError(String message) {
                Util.showErrorToast(ctx, message);
            }

            @Override
            public void onWebSocketClose() {

            }
        });
    }

    private List<LocationTrackerDTO> filterLatest() {
        HashMap<Integer, LocationTrackerDTO> map = new HashMap<>();
        List<LocationTrackerDTO> list = new ArrayList<>(locationTrackerList.size()/4);
        for (LocationTrackerDTO trk: locationTrackerList) {
            if (map.containsKey(trk.getGcmDevice().getGcmDeviceID())) {
                continue;
            }
            map.put(trk.getGcmDevice().getGcmDeviceID(),trk);
        }

        for (Integer id: map.keySet()) {
            list.add(map.get(id));
        }

        return list;

    }
    @Override
    public void setBusy(boolean busy) {

    }

    @Override
    public void displayOnMap(LocationTrackerDTO tracker) {
        Intent intent = new Intent(ctx, MonitorMapActivity.class);
        intent.putExtra("track",tracker);
        startActivity(intent);
    }
}

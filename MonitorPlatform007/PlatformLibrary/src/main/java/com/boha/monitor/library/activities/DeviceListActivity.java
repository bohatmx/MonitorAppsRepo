package com.boha.monitor.library.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.boha.monitor.library.dto.CompanyDTO;
import com.boha.monitor.library.dto.GcmDeviceDTO;
import com.boha.monitor.library.dto.LocationTrackerDTO;
import com.boha.monitor.library.dto.RequestDTO;
import com.boha.monitor.library.dto.ResponseDTO;
import com.boha.monitor.library.fragments.GcmDeviceFragment;
import com.boha.monitor.library.fragments.LocationTrackerListFragment;
import com.boha.monitor.library.util.NetUtil;
import com.boha.monitor.library.util.SharedUtil;
import com.boha.monitor.library.util.Util;
import com.boha.platform.library.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DeviceListActivity extends AppCompatActivity
        implements LocationTrackerListFragment.LocationTrackerListFragmentListener,
        GcmDeviceFragment.GcmDeviceListener{

    LocationTrackerListFragment locationTrackerListFragment;
    Context ctx;
    List<LocationTrackerDTO> locationTrackerList;
    List<GcmDeviceDTO> gcmDeviceList;
    static final String LOG = DeviceListActivity.class.getSimpleName();
    GcmDeviceFragment gcmDeviceFragment;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_tracker);
        ctx = getApplicationContext();
        locationTrackerListFragment = (LocationTrackerListFragment) getSupportFragmentManager().findFragmentById(R.id.fragment);
        getCompanyDevices();
    }


    private void getCompanyDevices() {
        RequestDTO req = new RequestDTO(RequestDTO.GET_COMPANY_DEVICES);
        CompanyDTO co = SharedUtil.getCompany(ctx);
        if (co != null) {
            req.setCompanyID(co.getCompanyID());
        }
        NetUtil.sendRequest(getApplicationContext(), req, new NetUtil.NetUtilListener() {
            @Override
            public void onResponse(final ResponseDTO response) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        gcmDeviceList = response.getGcmDeviceList();
                        Log.i(LOG, "company devices found: " + gcmDeviceList.size());
                        setFragment(response);
                        //// TODO: 16/02/02 cache company devices
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

    private void setFragment(ResponseDTO response) {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();

        ft.setCustomAnimations(R.anim.enter_from_left, R.anim.exit_to_right);
        gcmDeviceFragment = GcmDeviceFragment.newInstance(response);
        gcmDeviceFragment.setListener(this);
        ft.add(R.id.frameLayout, gcmDeviceFragment);
        ft.commit();
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

    @Override
    public void onDeviceClicked(final GcmDeviceDTO device) {
        Log.w(LOG,"onDeviceClicked: get locations of device");
        RequestDTO req = new RequestDTO(RequestDTO.GET_LOCATION_TRACK_BY_DEVICE);
        GcmDeviceDTO dto = new GcmDeviceDTO();
        dto.setGcmDeviceID(device.getGcmDeviceID());
        req.setGcmDevice(dto);
        NetUtil.sendRequest(getApplicationContext(), req, new NetUtil.NetUtilListener() {
            @Override
            public void onResponse(final ResponseDTO response) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        device.setTrackerList(response.getLocationTrackerList());
                        Log.i(LOG,"location list: " + device.getTrackerList().size());
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
}

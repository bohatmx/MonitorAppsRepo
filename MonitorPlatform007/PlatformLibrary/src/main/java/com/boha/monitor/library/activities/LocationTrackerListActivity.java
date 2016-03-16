package com.boha.monitor.library.activities;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.boha.monitor.library.dto.CompanyDTO;
import com.boha.monitor.library.dto.GcmDeviceDTO;
import com.boha.monitor.library.dto.LocationTrackerDTO;
import com.boha.monitor.library.dto.MonitorDTO;
import com.boha.monitor.library.dto.RequestDTO;
import com.boha.monitor.library.dto.ResponseDTO;
import com.boha.monitor.library.dto.StaffDTO;
import com.boha.monitor.library.fragments.GcmDeviceFragment;
import com.boha.monitor.library.fragments.LocationTrackerListFragment;
import com.boha.monitor.library.util.NetUtil;
import com.boha.monitor.library.util.SharedUtil;
import com.boha.monitor.library.util.Snappy;
import com.boha.monitor.library.util.ThemeChooser;
import com.boha.monitor.library.util.Util;
import com.boha.platform.library.R;

import java.util.Collections;
import java.util.List;

public class LocationTrackerListActivity extends AppCompatActivity
        implements LocationTrackerListFragment.LocationTrackerListFragmentListener,
        GcmDeviceFragment.GcmDeviceListener{

    LocationTrackerListFragment locationTrackerListFragment;
    Context ctx;
    List<LocationTrackerDTO> locationTrackerList;
    List<GcmDeviceDTO> gcmDeviceList;
    static final String LOG = LocationTrackerListActivity.class.getSimpleName();
    GcmDeviceFragment gcmDeviceFragment;
    int themeDarkColor, themePrimaryColor;
    FloatingActionButton fab;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeChooser.setTheme(this);
        Resources.Theme theme = getTheme();
        TypedValue typedValue = new TypedValue();
        theme.resolveAttribute(R.attr.colorPrimaryDark, typedValue, true);
        themeDarkColor = typedValue.data;
        theme.resolveAttribute(R.attr.colorPrimary, typedValue, true);
        themePrimaryColor = typedValue.data;

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_tracker);
        fab = (FloatingActionButton)findViewById(R.id.fab);
        ctx = getApplicationContext();
        locationTrackerListFragment = (LocationTrackerListFragment) getSupportFragmentManager().findFragmentById(R.id.fragment);

        Util.setCustomActionBar(getApplicationContext(), getSupportActionBar(),
                SharedUtil.getCompany(ctx).getCompanyName(), "Location of Monitor Devices",
                ContextCompat.getDrawable(getApplicationContext(), com.boha.platform.library.R.drawable.glasses));

        getCachedTracks();
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getCompanyTracks();
            }
        });
    }

    private void getCachedTracks() {
        Snappy.getCompanyTrackerList((MonApp) getApplication(), new Snappy.SnappyReadListener() {
            @Override
            public void onDataRead(ResponseDTO response) {
                Collections.sort(response.getLocationTrackerList());
                setTrackerFragment(response);
                getCompanyTracks();
            }

            @Override
            public void onError(String message) {
                Log.e(LOG,message);
                getCompanyTracks();
            }
        });
    }

    private void getCompanyTracks() {
        RequestDTO req = new RequestDTO(RequestDTO.GET_COMPANY_DEVICE_LOCATIONS_LATEST);
        CompanyDTO co = SharedUtil.getCompany(ctx);
        if (co != null) {
            req.setCompanyID(co.getCompanyID());
        }
        setBusyIndicator(true);
        NetUtil.sendRequest(getApplicationContext(), req, new NetUtil.NetUtilListener() {
            @Override
            public void onResponse(final ResponseDTO response) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        setBusyIndicator(false);
                        locationTrackerList = response.getLocationTrackerList();
                        Collections.sort(locationTrackerList);
                        setPhotos();
                        Snappy.saveCompanyTrackerList((MonApp) getApplication(), locationTrackerList, new Snappy.SnappyWriteListener() {
                            @Override
                            public void onDataWritten() {
                                Log.w(LOG,"company tracks written to cache: " + locationTrackerList.size());

                                if (locationTrackerListFragment == null) {
                                    setTrackerFragment(response);
                                } else {
                                    locationTrackerListFragment.setList(locationTrackerList);
                                }
                            }

                            @Override
                            public void onError(String message) {

                            }
                        });
                    }
                });
            }

            @Override
            public void onError(final String message) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        setBusyIndicator(false);
                        Util.showErrorToast(getApplicationContext(),message);
                    }
                });
            }


        });
    }

    private void setPhotos() {
        Snappy.getMonitorList((MonApp) getApplication(), new Snappy.SnappyReadListener() {
            @Override
            public void onDataRead(ResponseDTO response) {
                for (MonitorDTO m: response.getMonitorList()) {
                    if (m.getPhotoUploadList().isEmpty()) {
                        continue;
                    }
                    for (LocationTrackerDTO k: locationTrackerList) {
                        if (k.getMonitorID() == null) {
                            continue;
                        }
                        if (k.getMonitorID().intValue() == m.getMonitorID().intValue()) {
                            k.setPhoto(m.getPhotoUploadList().get(0));
                            break;
                        }
                    }
                }
                setStaffPhotos();

            }

            @Override
            public void onError(String message) {

            }
        });
    }
    private void setStaffPhotos() {
        Snappy.getStaffList((MonApp) getApplication(), new Snappy.SnappyReadListener() {
            @Override
            public void onDataRead(ResponseDTO response) {
                for (StaffDTO m: response.getStaffList()) {
                    if (m.getPhotoUploadList().isEmpty()) {
                        continue;
                    }
                    for (LocationTrackerDTO k : locationTrackerList) {
                        if (k.getStaffID() == null) {
                            continue;
                        }
                        if (k.getStaffID().intValue() == m.getStaffID().intValue()) {
                            k.setPhoto(m.getPhotoUploadList().get(0));
                            break;
                        }
                    }
                }
                Log.d(LOG,"Done getting staff photos");
            }

                @Override
                public void onError(String message) {

                }
            });
    }
    private void setDeviceFragment(ResponseDTO response) {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();

        ft.setCustomAnimations(R.anim.enter_from_left, R.anim.exit_to_right);
        gcmDeviceFragment = GcmDeviceFragment.newInstance(response);
        gcmDeviceFragment.setListener(this);
        ft.add(R.id.frameLayout, gcmDeviceFragment);
        ft.commit();
    }

    private void setTrackerFragment(ResponseDTO response) {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();

        ft.setCustomAnimations(R.anim.enter_from_left, R.anim.exit_to_right);
        locationTrackerListFragment = LocationTrackerListFragment.newInstance(response.getLocationTrackerList());
        locationTrackerListFragment.setListener(this);
        ft.add(R.id.frameLayout, locationTrackerListFragment);
        ft.commit();
    }

    @Override
    public void displayOnMap(LocationTrackerDTO tracker) {
        Intent intent = new Intent(ctx, MonitorMapActivity.class);
        intent.putExtra("track",tracker);
        startActivity(intent);
    }

    @Override
    public void onDeviceClicked(final GcmDeviceDTO device) {

    }

    Menu mMenu;
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.device_list, menu);
        mMenu = menu;

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //todo remove
        if (id == R.id.action_map) {
            Intent w = new Intent(this, MonitorMapActivity.class);
            ResponseDTO resp = new ResponseDTO();
            resp.setLocationTrackerList(locationTrackerList);
            w.putExtra("response",resp);
            startActivity(w);
            return true;
        }
        if (id == R.id.action_refresh) {
            getCompanyTracks();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    public void setBusyIndicator(final boolean refreshing) {
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

package com.boha.monitor.reporter;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;

import com.android.volley.VolleyError;
import com.boha.proximity.data.ErrorStoreAndroidDTO;
import com.boha.proximity.data.ErrorStoreDTO;
import com.boha.proximity.data.RequestDTO;
import com.boha.proximity.data.ResponseDTO;
import com.boha.proximity.library.Statics;
import com.boha.proximity.reporter.R;
import com.boha.monitor.reporter.fragments.AndroidCrashListFragment;
import com.boha.monitor.reporter.fragments.MGPageFragment;
import com.boha.monitor.reporter.fragments.ServerLogFragment;
import com.boha.monitor.reporter.fragments.SeverEventListFragment;
import com.boha.proximity.volley.BaseVolley;

import java.util.ArrayList;
import java.util.List;

public class EventActivity extends FragmentActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_pager);
        ctx = getApplicationContext();
        mPager = (ViewPager) findViewById(R.id.pager);
    }


    private void getCrashes() {

        RequestDTO w = new RequestDTO();
        w.setRequestType(RequestDTO.GET_ERROR_REPORTS);

        if (!BaseVolley.checkNetworkOnDevice(ctx)) {
            return;
        }
        setRefreshActionButtonState(true);
        BaseVolley.getRemoteData(Statics.SERVLET_ADMIN,w,ctx, new BaseVolley.BohaVolleyListener() {
            @Override
            public void onResponseReceived(ResponseDTO r) {
                setRefreshActionButtonState(false);
                if (r.getStatusCode() > 0) {
                    return;
                }
                response = r;
                buildPages();
            }

            @Override
            public void onVolleyError(VolleyError error) {
                setRefreshActionButtonState(false);


            }
        });
    }
    private void buildPages() {
        pageFragmentList = new ArrayList<MGPageFragment>();

        androidCrashListFragment = new AndroidCrashListFragment();
        ResponseDTO r1 = new ResponseDTO();
        Bundle data1 = new Bundle();
        r1.setErrorStoreAndroidList(response.getErrorStoreAndroidList());
        data1.putSerializable("response", r1);
        androidCrashListFragment.setArguments(data1);

        severEventListFragment = new SeverEventListFragment();
        ResponseDTO r2 = new ResponseDTO();
        Bundle data2 = new Bundle();
        r2.setErrorStoreList(response.getErrorStoreList());
        data2.putSerializable("response", r2);
        severEventListFragment.setArguments(data2);

        serverLogFragment = new ServerLogFragment();
        ResponseDTO r3 = new ResponseDTO();
        Bundle data3 = new Bundle();
        r3.setLog(response.getLog());
        data3.putSerializable("response", r3);
        serverLogFragment.setArguments(data3);

        pageFragmentList.add(androidCrashListFragment);
        pageFragmentList.add(severEventListFragment);
        pageFragmentList.add(serverLogFragment);
        initializeAdapter();
    }
    private void initializeAdapter() {
        mPagerAdapter = new PagerAdapter(getSupportFragmentManager());
        mPager.setAdapter(mPagerAdapter);
        mPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageSelected(int arg0) {
                currentPageIndex = arg0;
            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {

            }

            @Override
            public void onPageScrollStateChanged(int arg0) {
            }
        });
    }
    private class PagerAdapter extends FragmentStatePagerAdapter {

        public PagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {

            return (Fragment) pageFragmentList.get(i);
        }

        @Override
        public int getCount() {
            return pageFragmentList.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            String title = "Title";

            switch (position) {
                case 0:
                    title = "Mobile Device Crashes";
                    break;
                case 1:
                    title = "Server Events";
                    break;
                case 2:
                    title = "Server Log";
                    break;

                default:
                    break;
            }
            return title;
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.event, menu);
        mMenu = menu;
        if (response == null) {
            getCrashes();
        }
        return true;
    }

    public void setRefreshActionButtonState(final boolean refreshing) {
        if (mMenu != null) {
            final MenuItem refreshItem = mMenu.findItem(R.id.menu_refresh);
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
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.menu_refresh:
                getCrashes();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
Context ctx;
    ViewPager mPager;
    Menu mMenu;
    ResponseDTO response;
    PagerAdapter mPagerAdapter;
    int currentPageIndex;
    List<ErrorStoreAndroidDTO> errorStoreAndroidList;
    List<ErrorStoreDTO> errorStoreList;
    List<MGPageFragment> pageFragmentList;
    static final String LOG = "EventActivity";
    AndroidCrashListFragment androidCrashListFragment;
    SeverEventListFragment severEventListFragment;
    ServerLogFragment serverLogFragment;
}

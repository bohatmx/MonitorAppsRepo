package com.boha.monitor.setup.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.boha.monitor.library.dto.CompanyDTO;
import com.boha.monitor.library.dto.ResponseDTO;
import com.boha.monitor.library.util.CacheUtil;
import com.boha.monitor.setup.R;
import com.boha.monitor.setup.fragments.CompanyListFragment;
import com.google.gson.Gson;

public class CompanyListActivity extends AppCompatActivity
        implements CompanyListFragment.CompanyListener {

    CompanyListFragment companyListFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_company_list);

        companyListFragment = (CompanyListFragment) getSupportFragmentManager().findFragmentById(R.id.fragment);
        setTitle("Monitor");
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
            companyListFragment.getCompanyList();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCompanyClicked(CompanyDTO company) {

    }

    @Override
    public void onPortfolioCountClicked(final CompanyDTO company) {

        ResponseDTO w = new ResponseDTO();
        w.setCompany(company);
        CacheUtil.cacheCompanyData(getApplicationContext(), w, company.getCompanyID(), new CacheUtil.CacheUtilListener() {
            @Override
            public void onFileDataDeserialized(ResponseDTO response) {

            }

            @Override
            public void onDataCached() {
                Gson gson = new Gson();
                String json = gson.toJson(company);
                Log.e("CompanyListActivity", "json length of company: " + json.length());
                Intent w = new Intent(getApplicationContext(), CompanyDrawerActivity.class);
                w.putExtra("companyID", company.getCompanyID());
                startActivityForResult(w, CHECK_FOR_REFRESHED_DATA);
            }

            @Override
            public void onError() {

            }
        });

    }

    static final int CHECK_FOR_REFRESHED_DATA = 1121;


    @Override
    public void onIconDeleteClicked(CompanyDTO company, int position) {

    }

    @Override
    public void onIconEditClicked(CompanyDTO company, int position) {

    }

    @Override
    public void setBusy(boolean busy) {
        setRefreshActionButtonState(busy);
    }

    Menu mMenu;

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

    static final int REQUEST_IMPORT = 1333;

    @Override
    public void onActivityResult(int reqCode, int resCode, Intent data) {

        switch (reqCode) {
            case REQUEST_IMPORT:
                if (resCode == RESULT_OK) {
                    ResponseDTO resp = (ResponseDTO) data.getSerializableExtra("taskTypeList");
                    Log.i("", "Import completed, taskTypes = " + resp.getTaskTypeList().size());
                    companyListFragment.getCompanyList();
                }
                break;
            case CHECK_FOR_REFRESHED_DATA:
                if (resCode == RESULT_OK) {
                    CompanyDTO x = (CompanyDTO)data.getSerializableExtra("programme");
                    companyListFragment.refreshCompanyData(x);
                }
        }
    }
    @Override
    public void onPause() {
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        super.onPause();
    }
}

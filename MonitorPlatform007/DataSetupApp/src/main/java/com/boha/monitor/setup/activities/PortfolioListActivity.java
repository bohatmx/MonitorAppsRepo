package com.boha.monitor.setup.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.boha.monitor.library.dto.CompanyDTO;
import com.boha.monitor.library.dto.PortfolioDTO;
import com.boha.monitor.library.dto.RequestDTO;
import com.boha.monitor.library.dto.ResponseDTO;
import com.boha.monitor.library.util.CacheUtil;
import com.boha.monitor.library.util.NetUtil;
import com.boha.monitor.library.util.Util;
import com.boha.monitor.setup.R;
import com.boha.monitor.setup.fragments.PortfolioListFragment;

public class PortfolioListActivity extends AppCompatActivity implements PortfolioListFragment.PortfolioFragmentListener {

    PortfolioListFragment portfolioListFragment;
    CompanyDTO company;
    Integer companyID;
    Menu mMenu;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e("PortfolioListActivity", "##### onCreate");
        setContentView(R.layout.activity_portfolio_list);

        companyID = getIntent().getIntExtra("companyID",0);
        CacheUtil.getCachedCompanyData(getApplicationContext(), companyID, new CacheUtil.CacheUtilListener() {
            @Override
            public void onFileDataDeserialized(ResponseDTO response) {
                if (response.getCompany() != null) {
                    portfolioListFragment = (PortfolioListFragment) getSupportFragmentManager().findFragmentById(R.id.fragment);
                    portfolioListFragment.setCompany(response.getCompany());

                    setTitle("Company Portfolios");
                    getSupportActionBar().setSubtitle(response.getCompany().getCompanyName());
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
        w.setCompanyID(company.getCompanyID());

        companyDataRefreshed = false;
        setRefreshActionButtonState(true);
        NetUtil.sendRequest(getApplicationContext(), w, new NetUtil.NetUtilListener() {
            @Override
            public void onResponse(final ResponseDTO response) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        setRefreshActionButtonState(false);
                        company.setStaffList(response.getStaffList());
                        company.setMonitorList(response.getMonitorList());
                        company.setProjectStatusTypeList(response.getProjectStatusTypeList());
                        company.setTaskStatusTypeList(response.getTaskStatusTypeList());
                        company.setPortfolioList(response.getPortfolioList());
                        portfolioListFragment.setCompany(company);
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
    @Override
    public void onBackPressed() {
        if (companyDataRefreshed) {
            Intent w = new Intent();
            w.putExtra("programme",company);
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
    public void onPortfolioClicked(PortfolioDTO portfolio) {


    }

    @Override
    public void onProgrammeCountClicked(PortfolioDTO portfolio) {
        Intent w = new Intent(this, ProgrammeListActivity.class);
        w.putExtra("portfolioID",portfolio.getPortfolioID());
        startActivityForResult(w,CHECK_FOR_REFRESH);
    }

    static final int CHECK_FOR_REFRESH = 3121;
    @Override
    public void onActivityResult(int reqCode, int resCode, Intent data) {

        switch (reqCode) {

            case CHECK_FOR_REFRESH:
                if (resCode == RESULT_OK) {
                    ResponseDTO x = (ResponseDTO)data.getSerializableExtra("response");
                    company.setStaffList(x.getStaffList());
                    company.setMonitorList(x.getMonitorList());
                    company.setProjectStatusTypeList(x.getProjectStatusTypeList());
                    company.setTaskStatusTypeList(x.getTaskStatusTypeList());
                    company.setPortfolioList(x.getPortfolioList());
                    companyDataRefreshed = true;
                }
        }
    }

    @Override
    public void onIconDeleteClicked(PortfolioDTO portfolio, int position) {

    }

    @Override
    public void onIconEditClicked(PortfolioDTO portfolio, int position) {

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

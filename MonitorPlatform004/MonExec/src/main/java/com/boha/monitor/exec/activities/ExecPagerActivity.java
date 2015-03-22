package com.boha.monitor.exec.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerTitleStrip;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.boha.monitor.exec.R;
import com.boha.monitor.exec.fragments.ExecProjectGridFragment;
import com.com.boha.monitor.library.activities.SitePictureGridActivity;
import com.com.boha.monitor.library.adapters.DrawerAdapter;
import com.com.boha.monitor.library.dto.CompanyDTO;
import com.com.boha.monitor.library.dto.CompanyStaffDTO;
import com.com.boha.monitor.library.dto.ProjectDTO;
import com.com.boha.monitor.library.dto.ProjectSiteTaskStatusDTO;
import com.com.boha.monitor.library.dto.transfer.RequestDTO;
import com.com.boha.monitor.library.dto.transfer.ResponseDTO;
import com.com.boha.monitor.library.fragments.PageFragment;
import com.com.boha.monitor.library.fragments.ProjectListFragment;
import com.com.boha.monitor.library.fragments.StatusReportFragment;
import com.com.boha.monitor.library.util.CacheUtil;
import com.com.boha.monitor.library.util.ErrorUtil;
import com.com.boha.monitor.library.util.SharedUtil;
import com.com.boha.monitor.library.util.Statics;
import com.com.boha.monitor.library.util.Util;
import com.com.boha.monitor.library.util.WebCheck;
import com.com.boha.monitor.library.util.WebCheckResult;
import com.com.boha.monitor.library.util.WebSocketUtil;

import java.util.ArrayList;
import java.util.List;


public class ExecPagerActivity extends ActionBarActivity implements
        ExecProjectGridFragment.ExecProjectGridFragmentListener {

    private DrawerLayout mDrawerLayout;
    private DrawerAdapter mDrawerAdapter;
    private LayoutInflater inflater;
    ProgressBar progressBar;
    ProjectListFragment projectListFragment;
    StatusReportFragment statusReportFragment;

    PagerAdapter adapter;
    ViewPager mPager;
    Context ctx;
    ResponseDTO response;
    int currentPageIndex;


    static final int PICTURE_REQUESTED = 9133;
    static final String LOG = ExecPagerActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.drawer);
        ctx = getApplicationContext();
        inflater = getLayoutInflater();
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mPager = (ViewPager) findViewById(R.id.pager);
        PagerTitleStrip strip = (PagerTitleStrip) findViewById(R.id.pager_title_strip);
        //strip.setVisibility(View.GONE);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        drawerListView = (ListView) findViewById(R.id.left_drawer);
        titles = getResources().getStringArray(R.array.exec_action_items);
        setDrawerList();
        //
        setTitle(SharedUtil.getCompany(ctx).getCompanyName());
        CompanyStaffDTO staff = SharedUtil.getCompanyStaff(ctx);
        getSupportActionBar().setSubtitle(staff.getFullName());

    }

    private void setDrawerList() {
        CacheUtil.getCachedData(getApplicationContext(), CacheUtil.CACHE_DATA, new CacheUtil.CacheUtilListener() {
            @Override
            public void onFileDataDeserialized(ResponseDTO r) {
                CompanyDTO company = new CompanyDTO();
                if (r != null) {
                    if (r.getCompany() != null) {
                        response = r;
                        company = r.getCompany();
                        buildPages();
                    }
                }

                WebCheckResult wcr = WebCheck.checkNetworkAvailability(ctx);
                if (wcr.isWifiConnected()) {
                    getCompanyData();
                }
                for (String s : titles) {
                    sTitles.add(s);
                }
                mDrawerAdapter = new DrawerAdapter(getApplicationContext(), R.layout.drawer_item, sTitles, company);
                View v = inflater.inflate(R.layout.hero_image, null);
                ImageView img = (ImageView) v.findViewById(R.id.HERO_image);
                img.setImageDrawable(Util.getRandomHeroImage(ctx));
                TextView txt = (TextView) v.findViewById(R.id.HERO_caption);
                txt.setText(ctx.getString(R.string.projects));
                drawerListView.addHeaderView(v);
                drawerListView.setAdapter(mDrawerAdapter);
                drawerListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                        switch (i) {
                            case PROJECTS:
                                mPager.setCurrentItem(0, true);
                                break;
                            case STAFF:
                                mPager.setCurrentItem(1, true);
                                break;
                            case BENEFICIARIES:
                                break;
                            case CLIENTS:
                                break;
                            case INVOICES:
                                break;
                            case OVERALL_STATUS:
                                break;

                        }
                        mDrawerLayout.closeDrawers();
                    }
                });


            }

            @Override
            public void onDataCached() {

            }

            @Override
            public void onError() {

            }
        });
    }

    private void getCompanyData() {
        Log.w(LOG, "############# getCompanyData................");
        RequestDTO w = new RequestDTO();
        w.setRequestType(RequestDTO.GET_EXEC_COMPANY_DATA);
        w.setCompanyID(SharedUtil.getCompany(ctx).getCompanyID());

        progressBar.setVisibility(View.VISIBLE);
        WebSocketUtil.sendRequest(ctx, Statics.COMPANY_ENDPOINT, w, new WebSocketUtil.WebSocketListener() {
            @Override
            public void onMessage(final ResponseDTO r) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setVisibility(View.GONE);
                        if (!ErrorUtil.checkServerError(ctx, r)) {
                            return;
                        }


                        Log.e(LOG, "## getCompanyData responded...statusCode: " + r.getStatusCode());
                        response = r;
                        buildPages();
                        //cache company data for lookups
                        CacheUtil.cacheData(ctx, r, CacheUtil.CACHE_DATA, new CacheUtil.CacheUtilListener() {
                            @Override
                            public void onFileDataDeserialized(ResponseDTO response) {

                            }

                            @Override
                            public void onDataCached() {
//                                Log.i(LOG, "** companyData cached, about to start requestSyncService....");
//                                Intent i = new Intent(getApplicationContext(), RequestSyncService.class);
//                                startService(i);
                            }

                            @Override
                            public void onError() {

                            }
                        });


                    }
                });

            }

            @Override
            public void onClose() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setVisibility(View.GONE);
                    }
                });
            }

            @Override
            public void onError(final String message) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setVisibility(View.GONE);
                        Util.showErrorToast(ctx, message);
                    }
                });
            }
        });
    }

    List<ProjectSiteTaskStatusDTO> projectSiteTaskStatusList;

    public static final int PROJECTS = 0,
            STAFF = 1, CLIENTS = 2, OVERALL_STATUS = 3, BENEFICIARIES = 4, PROJECT_MAPS = 5,
            INVOICES = 6, HAPPY_LETTERS = 7, STATUS_NOTIFICATIONS = 8;



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_exec_pager, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_help) {
            Util.showToast(ctx, ctx.getString(R.string.under_cons));
            return true;
        }
        if (id == R.id.action_refresh) {
//            WebCheckResult r = WebCheck.checkNetworkAvailability(ctx);
//            if (!r.isWifiConnected()) {
//                Util.showToast(ctx,getString(R.string.connect_wifi));
//                return true;
//            }
            getCompanyData();
            return true;
        }
        if (id == R.id.action_gallery) {
            Intent i = new Intent(this, SitePictureGridActivity.class);
            startActivity(i);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    ExecProjectGridFragment execProjectGridFragment;

    private void buildPages() {

        if (pageFragmentList == null) {
            pageFragmentList = new ArrayList<>();
            execProjectGridFragment = new ExecProjectGridFragment();
            Bundle data1 = new Bundle();
            data1.putSerializable("response", response);
            execProjectGridFragment.setArguments(data1);

            statusReportFragment = new StatusReportFragment();
            statusReportFragment.setArguments(data1);

            pageFragmentList.add(execProjectGridFragment);
            pageFragmentList.add(statusReportFragment);

            adapter = new PagerAdapter(getSupportFragmentManager());
            mPager.setAdapter(adapter);
            mPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageSelected(int arg0) {
                    currentPageIndex = arg0;
                    if (pageFragmentList.get(currentPageIndex) instanceof StatusReportFragment) {
                        statusReportFragment.getCachedStatus();
                    }
                }

                @Override
                public void onPageScrolled(int arg0, float arg1, int arg2) {

                }

                @Override
                public void onPageScrollStateChanged(int arg0) {
                }
            });
        } else {
            //refresh pages
            execProjectGridFragment.refreshData(response);
        }

    }



    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

    }

    @Override
    public void onStatusCountClicked(ProjectDTO project) {

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
                    title = ctx.getResources().getString(R.string.company_projects);
                    break;
                case 1:
                    break;
                case 2:
                    break;
                case 3:
                    break;
                case 4:
                    break;

                default:
                    break;
            }
            return title;
        }
    }

    @Override
    public void onPause() {
        overridePendingTransition(com.boha.monitor.library.R.anim.slide_in_left, com.boha.monitor.library.R.anim.slide_out_right);
        super.onPause();
    }

    private List<PageFragment> pageFragmentList;
    private ListView drawerListView;
    private String[] titles;
    private List<String> sTitles = new ArrayList<>();
}

package com.com.boha.monitor.library.activities;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerTitleStrip;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import com.boha.monitor.library.R;
import com.com.boha.monitor.library.dialogs.ProjectSiteDialog;
import com.com.boha.monitor.library.dto.CompanyDTO;
import com.com.boha.monitor.library.dto.ProjectDTO;
import com.com.boha.monitor.library.dto.ProjectSiteDTO;
import com.com.boha.monitor.library.dto.transfer.PhotoUploadDTO;
import com.com.boha.monitor.library.dto.transfer.RequestDTO;
import com.com.boha.monitor.library.dto.transfer.ResponseDTO;
import com.com.boha.monitor.library.fragments.GPSScanFragment;
import com.com.boha.monitor.library.fragments.PageFragment;
import com.com.boha.monitor.library.fragments.ProjectSiteListFragment;
import com.com.boha.monitor.library.fragments.SiteTaskAndStatusAssignmentFragment;
import com.com.boha.monitor.library.services.PhotoUploadService;
import com.com.boha.monitor.library.services.RequestSyncService;
import com.com.boha.monitor.library.util.CacheUtil;
import com.com.boha.monitor.library.util.ErrorUtil;
import com.com.boha.monitor.library.util.Statics;
import com.com.boha.monitor.library.util.Util;
import com.com.boha.monitor.library.util.WebCheck;
import com.com.boha.monitor.library.util.WebCheckResult;
import com.com.boha.monitor.library.util.WebSocketUtil;

import java.util.ArrayList;
import java.util.List;

import static com.com.boha.monitor.library.util.Util.showErrorToast;
import static com.com.boha.monitor.library.util.Util.showToast;

public class ProjectSitePagerActivity extends ActionBarActivity
        implements
        ProjectSiteListFragment.ProjectSiteListListener {

    static final int NUM_ITEMS = 2;
    ProgressBar progressBar;
    CompanyDTO company;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.w(LOG, "##### onCreate ...........");
        setContentView(R.layout.activity_site_pager);
        ctx = getApplicationContext();

        mPager = (ViewPager) findViewById(R.id.SITE_pager);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        PagerTitleStrip strip = (PagerTitleStrip) findViewById(R.id.pager_title_strip);
        strip.setVisibility(View.GONE);

        project = (ProjectDTO) getIntent().getSerializableExtra("project");
        company = (CompanyDTO) getIntent().getSerializableExtra("company");
        type = getIntent().getIntExtra("type", SiteTaskAndStatusAssignmentFragment.OPERATIONS);

        setTitle("");
        getCachedProjectData();
    }

    private void getCachedProjectData() {
        CacheUtil.getCachedProjectData(ctx, project.getProjectID(), new CacheUtil.CacheUtilListener() {
            @Override
            public void onFileDataDeserialized(ResponseDTO response) {
                if (response.getProjectList() != null && !response.getProjectList().isEmpty()) {
                    project = response.getProjectList().get(0);
                    buildPages();
                    return;
                }

                getProjectData();

            }

            @Override
            public void onDataCached() {

            }

            @Override
            public void onError() {
                getProjectData();
            }
        });
    }

    private void getProjectData() {
        Log.w(LOG, "### getProjectData from the cloud");
        RequestDTO w = new RequestDTO(RequestDTO.GET_PROJECT_DATA);
        w.setProjectID(project.getProjectID());
        progressBar.setVisibility(View.VISIBLE);
        WebSocketUtil.sendRequest(ctx, Statics.COMPANY_ENDPOINT, w, new WebSocketUtil.WebSocketListener() {
            @Override
            public void onMessage(final ResponseDTO response) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setVisibility(View.GONE);
                        if (!ErrorUtil.checkServerError(ctx, response)) {
                            return;
                        }
                        if (!response.getProjectList().isEmpty()) {
                            project = response.getProjectList().get(0);
                            Log.i(LOG, "getProjectData returned data OK....");
                        } else {
                            Util.showErrorToast(ctx, "Project data not found. Please refresh data");
                            return;
                        }
                        buildPages();
                        CacheUtil.cacheProjectData(ctx, response, project.getProjectID(), new CacheUtil.CacheUtilListener() {
                            @Override
                            public void onFileDataDeserialized(ResponseDTO response) {

                            }

                            @Override
                            public void onDataCached() {

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
                        Log.e(LOG, "getProjectData --------------- websocket closed");
                    }
                });
            }

            @Override
            public void onError(final String message) {

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setVisibility(View.GONE);
                        showErrorToast(ctx, message);
                    }
                });
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.site_pager, menu);
        mMenu = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_refresh) {
            refresh();
            return true;
        }
        if (id == R.id.action_help) {
            showToast(ctx, ctx.getString(R.string.under_cons));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void refresh() {
        if (rBound) {
            rService.startSyncCachedRequests(new RequestSyncService.RequestSyncListener() {
                @Override
                public void onTasksSynced(int goodResponses, int badResponses) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (pBound) {
//                                Util.showToast(ctx, getString(R.string.uploading_photos));
                                pService.uploadCachedPhotos(new PhotoUploadService.UploadListener() {
                                    @Override
                                    public void onUploadsComplete(int count) {
//                                        getProjectData();
                                    }
                                });
                            }
                        }
                    });

                }

                @Override
                public void onError(String message) {

                }
            });
        }
        getProjectData();
    }



    @Override
    public void onStart() {
        super.onStart();

        Intent photoIntent = new Intent(this, PhotoUploadService.class);
        Intent requestIntent = new Intent(this, RequestSyncService.class);
        try {
            bindService(requestIntent, rConnection, Context.BIND_AUTO_CREATE);
            bindService(photoIntent, pConnection, Context.BIND_AUTO_CREATE);

        } catch (Exception e) {
            Log.e(LOG, "## problem with binding service", e);
        }

    }


    @Override
    public void onStop() {

        try {
            if (pBound) {
                unbindService(pConnection);
                pBound = false;
            }
            if (rBound) {
                unbindService(rConnection);
                rBound = false;
            }
        } catch (Exception e) {
            Log.e(LOG, "-- something wrong with unbind", e);
        }
        super.onStop();
    }

    /**
     * Defines callbacks for service binding, passed to bindService()
     */
    private ServiceConnection pConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            Log.w(LOG, "## PhotoUploadService ServiceConnection onServiceConnected");
            PhotoUploadService.LocalBinder binder = (PhotoUploadService.LocalBinder) service;
            pService = binder.getService();
            pBound = true;

                Log.w(LOG, "### starting PhotoUploadService ...");
                pService.uploadCachedPhotos(new PhotoUploadService.UploadListener() {
                    @Override
                    public void onUploadsComplete(int count) {
                        Log.i(LOG, "++ onUploadsComplete, count: " + count);
                        if (count > 0) {
                            refresh();
                        }
                    }
                });

        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            Log.w(LOG, "## PhotoUploadService onServiceDisconnected");
            pBound = false;
        }
    };

    private ServiceConnection rConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            Log.w(LOG, "## RequestSyncService ServiceConnection onServiceConnected");
            RequestSyncService.LocalBinder binder = (RequestSyncService.LocalBinder) service;
            rService = binder.getService();
            rBound = true;
            Log.w(LOG, "### starting RequestSyncService ...");
            rService.startSyncCachedRequests(new RequestSyncService.RequestSyncListener() {
                @Override
                public void onTasksSynced(int goodResponses, int badResponses) {
                    Log.e(LOG, "++ onTasksSynced, good: " + goodResponses + " bad: " + badResponses);
                }

                @Override
                public void onError(String message) {

                }
            });
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            Log.w(LOG, "## PhotoUploadService onServiceDisconnected");
            pBound = false;
        }
    };

    boolean pBound, rBound;
    PhotoUploadService pService;
    RequestSyncService rService;

    boolean photosLoaded;

    @Override
    public void onResume() {
        Log.e(LOG, "######### onResume ...");
        super.onResume();
    }

    private void buildPages() {

        pageFragmentList = new ArrayList<>();
        projectSiteListFragment = new ProjectSiteListFragment();
        Bundle data1 = new Bundle();
        data1.putSerializable("project", project);
        data1.putSerializable("company", company);
        data1.putInt("index", selectedSiteIndex);
        projectSiteListFragment.setArguments(data1);

        pageFragmentList.add(projectSiteListFragment);
        initializeAdapter();

    }

    private void initializeAdapter() {
        try {
            adapter = new PagerAdapter(getSupportFragmentManager());
            mPager.setAdapter(adapter);
            mPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageSelected(int arg0) {
                    currentPageIndex = arg0;
                    PageFragment pf = pageFragmentList.get(arg0);
                    if (pf instanceof ProjectSiteListFragment) {
                    }
                }

                @Override
                public void onPageScrolled(int arg0, float arg1, int arg2) {

                }

                @Override
                public void onPageScrollStateChanged(int arg0) {
                }
            });
        } catch (Exception e) {
            Log.e(LOG, "-- Some shit happened, probably IllegalState of some kind ...");
        }
    }

    @Override
    public void onProjectSiteClicked(ProjectSiteDTO projectSite, int index) {
        selectedSiteIndex = index;
    }

    @Override
    public void onProjectSiteEditRequested(ProjectSiteDTO projectSite, int index) {
        selectedSiteIndex = index;
        ProjectSiteDialog d = new ProjectSiteDialog();
        d.setContext(ctx);
        d.setProject(project);
        d.setProjectSite(projectSite);
        d.setAction(ProjectSiteDTO.ACTION_UPDATE);
        d.setListener(new ProjectSiteDialog.ProjectSiteDialogListener() {
            @Override
            public void onProjectSiteAdded(final ProjectSiteDTO site) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        projectSiteListFragment.addProjectSite(site);
                    }
                });
            }

            @Override
            public void onProjectSiteUpdated(ProjectSiteDTO project) {

            }

            @Override
            public void onError(final String message) {
                Log.e(LOG, "---- ERROR websocket - " + message);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showErrorToast(ctx, message);
                    }
                });
            }
        });
        d.show(getFragmentManager(), "PSD_DIAG");
    }

    @Override
    public void onProjectSiteTasksRequested(ProjectSiteDTO projectSite, int index) {
        selectedSiteIndex = index;
        this.projectSite = projectSite;
        Intent i = new Intent(this, TaskAssignmentActivity.class);
        i.putExtra("projectSite", projectSite);
        i.putExtra("company", company);
        i.putExtra("type", type);
        startActivityForResult(i, SITE_TASK_REQUEST);
    }

    @Override
    public void onCameraRequested(ProjectSiteDTO projectSite, int index) {

        Log.w(LOG, "%%% onCameraRequested siteID: " + projectSite.getProjectSiteID() + " index: " + index);
        selectedSiteIndex = index;
        this.projectSite = projectSite;
        Intent i = new Intent(this, PictureActivity.class);
        i.putExtra("projectSite", projectSite);
        i.putExtra("type", PhotoUploadDTO.SITE_IMAGE);
        startActivityForResult(i, SITE_PICTURE_REQUEST);


    }

    @Override
    public void onGalleryRequested(ProjectSiteDTO projectSite, int index) {
        selectedSiteIndex = index;
        this.projectSite = projectSite;
        Intent i = new Intent(this, SitePictureGridActivity.class);
        i.putExtra("projectSite", projectSite);
        //i.putExtra("type", ImagePagerActivity.SITE);
        startActivity(i);
    }

    @Override
    public void onPhotoListUpdated(final ProjectSiteDTO projectSite, int index) {
        Log.w(LOG, "------ onPhotoListUpdated site photos: " + projectSite.getPhotoUploadList().size());
        photosLoaded = true;
        selectedSiteIndex = index;
        this.projectSite.getPhotoUploadList().addAll(0, projectSite.getPhotoUploadList());

    }

    @Override
    public void onStatusListRequested(ProjectSiteDTO projectSite, int index) {
        Intent i = new Intent(this, SiteStatusReportActivity.class);
        i.putExtra("projectSite", projectSite);
        startActivity(i);
    }

    @Override
    public void onGPSRequested(ProjectSiteDTO projectSite, int index) {
        Log.e(LOG,"######### onGPSRequested");
        Intent f = new Intent(this, GPSActivity.class);
        f.putExtra("projectSite", projectSite);
        startActivityForResult(f,GPS_REQUESTED);

    }


    @Override
    public void onSiteOnMapRequested(ProjectSiteDTO projectSite, int index) {
        Intent i = new Intent(this, MonitorMapActivity.class);
        i.putExtra("projectSite", projectSite);
        startActivity(i);
    }

    int newStatusDone;
    static final int GPS_REQUESTED = 3001;

    @Override
    public void onNewStatusDone(int count) {
        newStatusDone += count;
    }

    @Override
    public void onPhotoUploadServiceRequested() {
        Log.e(LOG, "**** onPhotoUploadServiceRequested");
        WebCheckResult w = WebCheck.checkNetworkAvailability(ctx);
        if (w.isWifiConnected()) {
            pService.uploadCachedPhotos(new PhotoUploadService.UploadListener() {
                @Override
                public void onUploadsComplete(int count) {
                    Log.w(LOG, "+++ onUploadsComplete count: " + count);
                }
            });
        }

    }


    @Override
    public void onActivityResult(int reqCode, int res, Intent data) {
        Log.e(LOG, "##### onActivityResult requestCode: " + reqCode + " resultCode: " + res);
        switch (reqCode) {
            case GPS_REQUESTED:
                if (res == RESULT_OK) {
                    projectSite = (ProjectSiteDTO) data.getSerializableExtra("projectSite");
                    projectSiteListFragment.updateSiteLocation(projectSite);
                }
                break;
            case SITE_PICTURE_REQUEST:
                Log.i(LOG, "################### onActivityResult SITE_PICTURE_REQUEST");
                if (res == RESULT_OK) {
                    ResponseDTO r = (ResponseDTO) data.getSerializableExtra("response");
                    Log.w(LOG, "## refresh list with new local photos: " + r.getImageFileNameList());
                    projectSiteListFragment.refreshPhotoList(r.getImageFileNameList());
                }
                break;
            case SITE_TASK_REQUEST:

                if (res == RESULT_OK) {
                    ResponseDTO r = (ResponseDTO) data.getSerializableExtra("response");
                    Log.w(LOG, "## data returned, statusList: " + r.getProjectSiteTaskStatusList().size() +
                            " taskList: " + r.getProjectSiteTaskList().size());

                    projectSiteListFragment.refreshData(r);

                }
                break;
        }

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
            String title = "";

            switch (position) {
                case 0:
                    title = project.getProjectName();
                    break;


                default:
                    break;
            }
            return title;
        }
    }

    @Override
    public void onPause() {
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        super.onPause();
    }

    @Override
    public void onBackPressed() {
        if (pageFragmentList == null) return;
        if (pageFragmentList.get(currentPageIndex) instanceof GPSScanFragment) {
            mPager.setCurrentItem(0, true);
            return;
        }
        Intent i = new Intent();
        i.putExtra("newStatusDone", newStatusDone);
        setResult(RESULT_OK, i);

        finish();
    }


    int selectedSiteIndex;
    ProjectSiteDTO projectSite;
    ProjectSiteListFragment projectSiteListFragment;
    List<PageFragment> pageFragmentList;

    Context ctx;
    ViewPager mPager;
    int type;
    Menu mMenu;
    int currentPageIndex;
    PagerAdapter adapter;
    ProjectDTO project;
    static final String LOG = ProjectSitePagerActivity.class.getSimpleName();
    static final int SITE_PICTURE_REQUEST = 113,
            SITE_TASK_REQUEST = 114;

}

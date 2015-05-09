package com.boha.monitor.library.fragments;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListPopupWindow;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.boha.monitor.library.R;
import com.boha.monitor.library.adapters.StatusReportAdapter;
import com.boha.monitor.library.dto.ProjectDTO;
import com.boha.monitor.library.dto.ProjectSiteDTO;
import com.boha.monitor.library.dto.ProjectSiteTaskDTO;
import com.boha.monitor.library.dto.ProjectSiteTaskStatusDTO;
import com.boha.monitor.library.dto.TaskDTO;
import com.boha.monitor.library.dto.transfer.RequestDTO;
import com.boha.monitor.library.dto.transfer.ResponseDTO;
import com.boha.monitor.library.util.CacheUtil;
import com.boha.monitor.library.util.ErrorUtil;
import com.boha.monitor.library.util.NetUtil;
import com.boha.monitor.library.util.Statics;
import com.boha.monitor.library.util.Util;
import com.boha.monitor.library.util.WebSocketUtil;




import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static com.boha.monitor.library.util.Util.showErrorToast;

public class SiteStatusReportFragment extends Fragment implements PageFragment {



    public static SiteStatusReportFragment newInstance(ResponseDTO r) {
        SiteStatusReportFragment fragment = new SiteStatusReportFragment();
        Bundle args = new Bundle();
        args.putSerializable("response", r);
        fragment.setArguments(args);
        return fragment;
    }


    public SiteStatusReportFragment() {
    }

    public interface SiteStatusReportListener {
        public void onNoDataAvailable();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    Context ctx;
    TextView txtCount, txtSiteName;
    ImageView heroImage;

    ProjectSiteDTO projectSite;
    ProjectDTO project;
    TextView txtProject, txtEmpty, txtTitle;
    ListView listView;
    LayoutInflater inflater;
    StatusReportAdapter adapter;
    Button btnStart, btnEnd;
    Date startDate, endDate;
    View view, handle;
    static final Locale locale = Locale.getDefault();
    static final SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy", locale);
    List<ProjectDTO> projectList;
    ListPopupWindow popupWindow;
    ProgressBar progressBar;

    @Override
    public View onCreateView( LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(LOG, "########## onCreateView");
        this.inflater = inflater;
        ctx = getActivity();
        view = inflater.inflate(R.layout.fragment_site_status_list, container, false);
        setFields();


        return view;
    }

    private void setFields() {
        handle = view.findViewById(R.id.SITE_STATUS_handle);
        progressBar = (ProgressBar) view.findViewById(R.id.SITE_STATUS_progress);
        listView = (ListView) view.findViewById(R.id.SITE_STATUS_list);
        heroImage = (ImageView) view.findViewById(R.id.SITE_STATUS_heroImage);
        txtCount = (TextView) view.findViewById(R.id.SITE_STATUS_txtCount);
        txtTitle = (TextView) view.findViewById(R.id.SITE_STATUS_txtTitle);
        txtEmpty = (TextView) view.findViewById(R.id.SITE_STATUS_txtEmpty);

        heroImage.setImageDrawable(Util.getRandomHeroImage(ctx));
        Util.expand(heroImage, 1000, null);
        Statics.setRobotoFontLight(ctx, txtTitle);
        progressBar.setVisibility(View.GONE);
        txtTitle.setVisibility(View.GONE);
        txtEmpty.setVisibility(View.GONE);

        txtCount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Util.flashOnce(txtCount, 200, new Util.UtilAnimationListener() {
                    @Override
                    public void onAnimationEnded() {
                        getCachedSiteData();
                        txtCount.setAlpha(1.0f);
                    }
                });

            }
        });


    }

    List<ProjectSiteTaskStatusDTO> projectSiteTaskStatusList;

    public void setProjectSite(ProjectSiteDTO site) {
        this.projectSite = site;
        projectSiteTaskStatusList = new ArrayList<>();
        for (ProjectSiteTaskDTO task : projectSite.getProjectSiteTaskList()) {
            if (task.getProjectSiteTaskStatusList() != null && !task.getProjectSiteTaskStatusList().isEmpty()) {
                projectSiteTaskStatusList.addAll(task.getProjectSiteTaskStatusList());
            }
        }
        if (projectSiteTaskStatusList.isEmpty()) {
            getCachedSiteData();
        } else {
            setList();
        }
    }

    private void getCachedSiteData() {
        progressBar.setVisibility(View.VISIBLE);
        CacheUtil.getCachedSiteData(ctx, projectSite.getProjectSiteID(), new CacheUtil.CacheSiteListener() {
            @Override
            public void onSiteReturnedFromCache( ProjectSiteDTO site) {
                progressBar.setVisibility(View.GONE);
                if (site != null) {
                    projectSite = site;
                    projectSiteTaskStatusList = new ArrayList<>();
                    for (ProjectSiteTaskDTO task : projectSite.getProjectSiteTaskList()) {
                        if (task.getProjectSiteTaskStatusList() != null && !task.getProjectSiteTaskStatusList().isEmpty()) {
                            projectSiteTaskStatusList.addAll(task.getProjectSiteTaskStatusList());
                        }
                    }
                    if (projectSiteTaskStatusList.isEmpty()) {
                        Util.showToast(ctx, "No status updates have been recorded.");
                    } else {
                        setList();
                    }
                }
                getSiteData();
            }

            @Override
            public void onDataCached() {

            }

            @Override
            public void onError() {
                Log.e(LOG, "--- no cache exists for the site, going to the cloud");
                listener.onNoDataAvailable();
            }
        });
    }

    private void getSiteData() {

        RequestDTO w = new RequestDTO(RequestDTO.GET_SITE_STATUS);
        w.setProjectSiteID(projectSite.getProjectSiteID());
        progressBar.setVisibility(View.VISIBLE);
        NetUtil.sendRequest(ctx, w, new NetUtil.NetUtilListener() {
            @Override
            public void onResponse( final ResponseDTO response) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setVisibility(View.GONE);
                        if (!ErrorUtil.checkServerError(ctx, response)) {
                            return;
                        }
                        projectSite = response.getProjectSiteList().get(0);
                        projectSiteTaskStatusList = new ArrayList<>();
                        for (ProjectSiteTaskDTO task : projectSite.getProjectSiteTaskList()) {
                            if (task.getProjectSiteTaskStatusList() != null && !task.getProjectSiteTaskStatusList().isEmpty()) {
                                projectSiteTaskStatusList.addAll(task.getProjectSiteTaskStatusList());
                            }
                        }
                        if (projectSiteTaskStatusList.isEmpty()) {
                            Util.showToast(ctx, "No status updates have been recorded.");
                        } else {
                            setList();
                            CacheUtil.cacheSiteData(ctx, projectSite, null);
                        }

                    }

                });
            }

            @Override
            public void onError(final String message) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setVisibility(View.GONE);
                        showErrorToast(ctx, message);

                    }
                });
            }

            @Override
            public void onWebSocketClose() {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setVisibility(View.GONE);
                    }
                });
            }
        });

        WebSocketUtil.sendRequest(ctx, Statics.COMPANY_ENDPOINT, w, new WebSocketUtil.WebSocketListener() {
            @Override
            public void onMessage(final ResponseDTO response) {

            }

            @Override
            public void onClose() {

            }

            @Override
            public void onError(String message) {

            }
        });

    }

    private void setList() {
        Log.d(LOG, "########## setList");
        Collections.sort(projectSiteTaskStatusList);
        txtCount.setText("" + projectSiteTaskStatusList.size());
        adapter = new StatusReportAdapter(ctx, R.layout.status_report_card, projectSiteTaskStatusList, true);
        listView.setAdapter(adapter);

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof SiteStatusReportListener) {
            listener = (SiteStatusReportListener) activity;
        } else {
            throw new ClassCastException("Host " + activity.getLocalClassName() + "must implement SiteStatusReportListener");
        }
        Log.w(LOG, "this Fragment hosted by " + activity.getLocalClassName());
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    SiteStatusReportListener listener;
    TaskDTO task;
    static final String LOG = SiteStatusReportFragment.class.getSimpleName();

    @Override
    public void animateHeroHeight() {
        Util.animateRotationY(txtCount, 500);

    }

}

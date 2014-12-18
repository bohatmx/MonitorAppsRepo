package com.com.boha.monitor.library.fragments;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.boha.monitor.library.R;
import com.com.boha.monitor.library.activities.ImagePagerActivity;
import com.com.boha.monitor.library.activities.PictureRecyclerGridActivity;
import com.com.boha.monitor.library.adapters.SiteReportAdapter;
import com.com.boha.monitor.library.dto.ProjectSiteDTO;
import com.com.boha.monitor.library.dto.ProjectSiteTaskDTO;
import com.com.boha.monitor.library.dto.ProjectSiteTaskStatusDTO;
import com.com.boha.monitor.library.util.Statics;
import com.com.boha.monitor.library.util.Util;

import java.util.Collections;
import java.util.List;

/**
 * A simple {@link android.support.v4.app.Fragment} subclass.
 */
public class ExecProjectSiteStatusListFragment extends Fragment {

    View view;
    List<ProjectSiteTaskStatusDTO> projectSiteTaskStatusList;
    ProjectSiteDTO project;
    TextView txtTitle, txtBen, txtPhotos;
    ListView listView;
    ImageView imgCamera;

    public ExecProjectSiteStatusListFragment() {
        // Required empty public constructor
    }

    public interface ExecProjectSiteStatusListListener {
        public void onTaskClicked(ProjectSiteTaskDTO status);
    }

    ExecProjectSiteStatusListListener listener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.e(LOG, "## onCreateView ...............");
        view = inflater.inflate(R.layout.fragment_exec_status_list, container, false);
        ctx = getActivity();
        listView = (ListView) view.findViewById(R.id.EXEC_STX_list);
        txtTitle = (TextView) view.findViewById(R.id.EXEC_STX_title);
        txtBen = (TextView) view.findViewById(R.id.EXEC_STX_beneficiary);
        txtPhotos = (TextView) view.findViewById(R.id.EXEC_STX_photos);
        imgCamera = (ImageView)view.findViewById(R.id.EXEC_STX_camera);
        imgCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Util.flashOnce(imgCamera, 100, new Util.UtilAnimationListener() {
                    @Override
                    public void onAnimationEnded() {
                        Intent i = new Intent(getActivity(), PictureRecyclerGridActivity.class);
                        i.putExtra("projectSite",projectSite);
                        i.putExtra("type", ImagePagerActivity.SITE);
                        startActivity(i);
                    }
                });
            }
        });
        if (getArguments() != null) {
            projectSite = (ProjectSiteDTO) getArguments().getSerializable("projectSite");
            projectSiteTaskStatusList = projectSite.getProjectSiteTaskStatusList();
            txtTitle.setText(projectSite.getProjectSiteName());
        }
        if (projectSiteTaskStatusList != null) {
            setList();
        }
        Statics.setRobotoFontLight(ctx,txtBen);
        Statics.setRobotoFontLight(ctx,txtTitle);
        return view;
    }

    View headerView;
    TextView txtCaption;
    private void setList() {
        Collections.sort(projectSiteTaskList);
        Log.w(LOG, "## setList, task list size: " + projectSiteTaskList.size());
        adapter = new SiteReportAdapter(ctx, R.layout.site_report_item, projectSiteTaskList);

        if (headerView == null) {
            headerView = Util.getHeroView(ctx, projectSite.getProjectSiteName());
            txtCaption = (TextView)headerView.findViewById(R.id.HERO_caption);
        } else {
            txtCaption.setText(projectSite.getProjectSiteName());
        }
        if (listView.getHeaderViewsCount() == 0)
            listView.addHeaderView(headerView);

        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                listener.onTaskClicked(projectSiteTaskList.get(position));
            }
        });

        Util.flashSeveralTimes(txtTitle,100,3,null);
    }

    public void setProjectSite(ProjectSiteDTO projectSite) {
        Log.w(LOG, "## setProjectSite " + projectSite.getProjectSiteName());
        this.projectSite = projectSite;
        projectSiteTaskList = projectSite.getProjectSiteTaskList();
        txtTitle.setText(projectSite.getProjectSiteName());
        txtBen.setText(projectSite.getBeneficiary().getFullName());
        if (projectSite.getPhotoUploadList() != null) {
            txtPhotos.setText("" + projectSite.getPhotoUploadList().size());
        }
        setList();

    }

    @Override
    public void onAttach(Activity a) {
        if (a instanceof ExecProjectSiteStatusListListener) {
            listener = (ExecProjectSiteStatusListListener) a;
        } else {
            throw new UnsupportedOperationException("Host "
                    + a.getLocalClassName() + " must implement ExecProjectSiteStatusListListener");
        }
        Log.w(LOG, "## Fragment loaded and hosted by: " + a.getLocalClassName());
        super.onAttach(a);
    }

    static final String LOG = ExecProjectSiteStatusListFragment.class.getSimpleName();
    SiteReportAdapter adapter;
    Context ctx;
    ProjectSiteDTO projectSite;
    List<ProjectSiteTaskDTO> projectSiteTaskList;
}

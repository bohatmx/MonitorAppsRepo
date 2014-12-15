package com.com.boha.monitor.library.fragments;


import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.boha.monitor.library.R;
import com.com.boha.monitor.library.adapters.ExecStatusListAdapter;
import com.com.boha.monitor.library.dto.ProjectSiteDTO;
import com.com.boha.monitor.library.dto.ProjectSiteTaskStatusDTO;

import java.util.List;

/**
 * A simple {@link android.support.v4.app.Fragment} subclass.
 */
public class ExecProjectSiteStatusListFragment extends Fragment {

    View view;
    List<ProjectSiteTaskStatusDTO> projectSiteTaskStatusList;
    ProjectSiteDTO project;
    TextView txtTitle;
    ListView listView;

    public ExecProjectSiteStatusListFragment() {
        // Required empty public constructor
    }
    public interface ExecProjectSiteStatusListListener {
        public void onTaskStatusClicked(ProjectSiteTaskStatusDTO status);
    }
    ExecProjectSiteStatusListListener listener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.e(LOG, "## onCreateView ...............");
        view = inflater.inflate(R.layout.fragment_exec_status_list, container, false);
        ctx = getActivity();
        listView = (ListView)view.findViewById(R.id.EXEC_STX_list);
        if (getArguments() != null) {
            projectSite = (ProjectSiteDTO)getArguments().getSerializable("projectSite");
            projectSiteTaskStatusList = projectSite.getProjectSiteTaskStatusList();
        }
        if (projectSiteTaskStatusList != null) {
            setList();
        }
        return view;
    }

    private void setList() {
        Log.w(LOG, "## setList, status list: " + projectSiteTaskStatusList.size());
        adapter = new ExecStatusListAdapter(ctx,R.layout.quick_status, projectSiteTaskStatusList);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                listener.onTaskStatusClicked(projectSiteTaskStatusList.get(position));
            }
        });
    }

    public void setProjectSite(ProjectSiteDTO projectSite) {
        Log.w(LOG, "## setProjectSite " + projectSite.getProjectSiteName());
        this.projectSite = projectSite;
        projectSiteTaskStatusList = projectSite.getProjectSiteTaskList().get(0).getProjectSiteTaskStatusList();
        setList();
    }
    @Override
     public void onAttach(Activity a) {
        if (a instanceof ExecProjectSiteStatusListListener) {
            listener = (ExecProjectSiteStatusListListener)a;
        } else {
            throw new UnsupportedOperationException("Host "
                    + a.getLocalClassName() + " must implement ExecProjectSiteStatusListListener");
        }
        Log.w(LOG,"## Fragment loaded and hosted by: " + a.getLocalClassName());
        super.onAttach(a);
    }
    static final String LOG = ExecProjectSiteStatusListFragment.class.getSimpleName();
    ExecStatusListAdapter adapter;
    Context ctx;
    ProjectSiteDTO projectSite;
}

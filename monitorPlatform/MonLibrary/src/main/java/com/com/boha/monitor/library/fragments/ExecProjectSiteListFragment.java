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
import com.com.boha.monitor.library.adapters.ProjectSiteAdapter;
import com.com.boha.monitor.library.dto.ProjectDTO;
import com.com.boha.monitor.library.dto.ProjectSiteDTO;

import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class ExecProjectSiteListFragment extends Fragment {

    View view;
    List<ProjectSiteDTO> projectSiteList;
    ProjectDTO project;
    TextView txtTitle;
    ListView listView;

    public ExecProjectSiteListFragment() {
        // Required empty public constructor
    }
    public interface ExecProjectSiteListListener {
        public void onProjectSiteClicked(ProjectSiteDTO site);
    }
    ExecProjectSiteListListener listener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_exec_site_list, container, false);
        ctx = getActivity();

        listView = (ListView)view.findViewById(R.id.EXEC_PL_list);
        if (getArguments() != null) {
            project = (ProjectDTO)getArguments().getSerializable("project");
            projectSiteList = project.getProjectSiteList();
            setList();
        }
        return view;
    }

    private void setList() {
        Log.e(LOG,"## setting list");
        adapter = new ProjectSiteAdapter(ctx,R.layout.site_item, projectSiteList);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                listener.onProjectSiteClicked(projectSiteList.get(position));
            }
        });
    }

    public void setProject(ProjectDTO project) {
        Log.w(LOG,"## setting project");
        this.project = project;
        projectSiteList = project.getProjectSiteList();
        setList();
    }
    @Override
     public void onAttach(Activity a) {
        super.onAttach(a);
        if (a instanceof ExecProjectSiteListListener) {
            listener = (ExecProjectSiteListListener)a;
        } else {
            throw new UnsupportedOperationException("Host "
                    + a.getLocalClassName() + " must always implement ExecProjectSiteListListener");
        }
        Log.w(LOG,"## Fragment loaded and hosted by: " + a.getLocalClassName());
    }
    static final String LOG = ExecProjectSiteListFragment.class.getSimpleName();
    ProjectSiteAdapter adapter;
    Context ctx;
}

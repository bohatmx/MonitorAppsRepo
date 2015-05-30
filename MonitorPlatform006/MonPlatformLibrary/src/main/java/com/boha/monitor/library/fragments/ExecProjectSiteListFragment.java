package com.boha.monitor.library.fragments;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.boha.monitor.library.R;
import com.boha.monitor.library.adapters.TinySiteAdapter;
import com.boha.monitor.library.dto.ProjectDTO;
import com.boha.monitor.library.dto.ProjectSiteDTO;
import com.boha.monitor.library.util.Util;

import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class ExecProjectSiteListFragment extends Fragment {

    View view;
    List<ProjectSiteDTO> projectSiteList;
    ProjectDTO project;
    TextView txtTitle, txtCount;
    ListView listView;
    EditText editSearch;
    ImageView imgSearch, hero;
    ProgressBar progressBar;

    public ExecProjectSiteListFragment() {
        // Required empty public constructor
    }
    public interface ExecProjectSiteListListener {
        public void onProjectSiteClicked(ProjectSiteDTO site);
        public void onProjectStatusSyncRequested(ProjectDTO project);
    }
    ExecProjectSiteListListener listener;

    @Override
    public View onCreateView( LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_exec_site_list, container, false);
        ctx = getActivity();

        listView = (ListView)view.findViewById(R.id.EXEC_PL_list);
        txtCount = (TextView)view.findViewById(R.id.EXEC_PL_count);
        txtTitle = (TextView)view.findViewById(R.id.EXEC_PL_title);
        editSearch = (EditText)view.findViewById(R.id.SLT_editSearch);
        imgSearch = (ImageView) view.findViewById(R.id.SLT_imgSearch);
//        hero = (ImageView) view.findViewById(R.id.SLT_heroImage);
//        hero.setVisibility(View.GONE);
        imgSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Util.flashOnce(imgSearch, 100, new Util.UtilAnimationListener() {
                    @Override
                    public void onAnimationEnded() {
                        search();
                    }
                });
            }
        });
        txtTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Util.flashOnce(txtTitle, 100, new Util.UtilAnimationListener() {
                    @Override
                    public void onAnimationEnded() {
                        confirm();
                    }
                });
            }
        });

        if (getArguments() != null) {
            project = (ProjectDTO)getArguments().getSerializable("project");
            projectSiteList = project.getProjectSiteList();
            setList();
        }
        return view;
    }

    private void getProjectData() {

    }
    private void search() {
        if (editSearch.getText().toString().isEmpty()) {
            return;
        }
        hideKeyboard();
        int index = 0;
        boolean found = false;
        for (ProjectSiteDTO s: projectSiteList) {
            if (s.getProjectSiteName().contains(editSearch.getText().toString())) {
                found = true;
                break;
            }
            index++;
        }
        if (found) {
            listView.setSelection(index);
        } else {
            Util.showToast(ctx, "Site searched: " + editSearch.getText().toString() + " not found.");
        }
    }
    private void confirm() {
        AlertDialog.Builder d = new AlertDialog.Builder(getActivity());
        d.setTitle("Confirm Site Sync")
                .setMessage("Would you like to download all the status data for the sites? The data will be on the device should you be travelling or in a place with no WIFI networks")
                .setPositiveButton(ctx.getString(R.string.yes), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        listener.onProjectStatusSyncRequested(project);
                    }
                }).setNegativeButton(ctx.getString(R.string.no), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        }).show();
    }

    public void setSyncIndex(int index) {
        //Util.showToast(ctx, "Dealing with sync index " + index);
    }
    private void setList() {
        Log.e(LOG,"## setting list");
        txtCount.setText("" + projectSiteList.size());
        adapter = new TinySiteAdapter(ctx, R.layout.tiny_site_item, projectSiteList);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                for (ProjectSiteDTO s: projectSiteList) {
                    s.setSelected(false);
                }
                projectSiteList.get(position).setSelected(true);
                adapter.notifyDataSetChanged();
                listener.onProjectSiteClicked(projectSiteList.get(position));
            }
        });
    }

    public void setProject( ProjectDTO project) {
        Log.w(LOG,"## setting project");
        this.project = project;
        projectSiteList = project.getProjectSiteList();
        setList();

        if (projectSiteList != null && !projectSiteList.isEmpty()) {
            listener.onProjectSiteClicked(projectSiteList.get(0));
        }

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
    TinySiteAdapter adapter;
    Context ctx;
    void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) ctx
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(editSearch.getWindowToken(), 0);
        imm.hideSoftInputFromWindow(editSearch.getWindowToken(), 0);
    }
}

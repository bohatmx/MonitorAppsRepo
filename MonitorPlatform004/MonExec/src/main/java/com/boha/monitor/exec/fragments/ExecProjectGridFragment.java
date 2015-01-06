package com.boha.monitor.exec.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListPopupWindow;
import android.widget.TextView;

import com.boha.monitor.exec.R;
import com.com.boha.monitor.library.activities.ExecStatusReportActivity;
import com.boha.monitor.exec.adapters.ProjectExecAdapter;
import com.com.boha.monitor.library.activities.MonitorMapActivity;
import com.com.boha.monitor.library.adapters.ExecStatusListAdapter;
import com.com.boha.monitor.library.adapters.PopupListAdapter;
import com.com.boha.monitor.library.dto.ProjectDTO;
import com.com.boha.monitor.library.dto.transfer.ResponseDTO;
import com.com.boha.monitor.library.fragments.PageFragment;
import com.com.boha.monitor.library.util.Util;

import java.util.ArrayList;
import java.util.List;

public class ExecProjectGridFragment extends Fragment implements PageFragment{

    static final String LOG = ExecProjectGridFragment.class.getSimpleName();
    public ExecProjectGridFragment() {
        // Required empty public constructor
    }

    View view, heroLayout;
    TextView txtCount, txtTitle;
    EditText editSearch;
    GridView grid;
    ImageView imgHero, imgSearch;
    ProjectExecAdapter adapter;
    Context ctx;
    ListPopupWindow actionsWindow;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            ResponseDTO resp = (ResponseDTO) getArguments().getSerializable("response");
            projectList = resp.getCompany().getProjectList();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ctx = getActivity();
        view = inflater.inflate(R.layout.fragment_exec_project_grid, container, false);
        setFields();
        setGrid();

        return view;
    }

    public void refreshData(ResponseDTO response) {
        projectList = response.getCompany().getProjectList();

        setGrid();
    }
    private void setGrid() {
        adapter = new ProjectExecAdapter(ctx,R.layout.project_item, projectList);
        txtCount.setText("" + projectList.size());
        grid.setPadding(2, 2, 2, 2);
        grid.setAdapter(adapter);
        grid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                project = projectList.get(position);
                //setup list for pop
                final List<String> list = new ArrayList<String>();
                list.add(ctx.getString(R.string.quick_status));
                list.add(ctx.getString(R.string.claims_invoices));
                list.add(ctx.getString(R.string.status_reports));
                list.add(ctx.getString(R.string.project_map));
                list.add(ctx.getString(R.string.take_picture));
                list.add(ctx.getString(R.string.gallery));
                actionsWindow = new ListPopupWindow(getActivity());
                actionsWindow.setPromptView(Util.getHeroView(ctx, ctx.getString(R.string.select_action)));
                actionsWindow.setPromptPosition(ListPopupWindow.POSITION_PROMPT_ABOVE);
                actionsWindow.setAdapter(new PopupListAdapter(ctx,
                        com.boha.monitor.library.R.layout.xxsimple_spinner_item, list,false));
                actionsWindow.setAnchorView(txtTitle);
                actionsWindow.setWidth(Util.getPopupWidth(getActivity()));
                actionsWindow.setHorizontalOffset(Util.getPopupHorizontalOffset(getActivity()));
                actionsWindow.setModal(true);
                actionsWindow.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                        if (list.get(position).equalsIgnoreCase(ctx.getString(R.string.claims_invoices))) {

                        }
                        if (list.get(position).equalsIgnoreCase(ctx.getString(R.string.status_reports))) {
                            Intent i = new Intent(getActivity(), ExecStatusReportActivity.class);
                            i.putExtra("project",project);
                            startActivity(i);
                        }
                        if (list.get(position).equalsIgnoreCase(ctx.getString(R.string.project_map))) {
                            Intent i = new Intent(ctx, MonitorMapActivity.class);
                            i.putExtra("project",project);
                            startActivity(i);
                        }
                        if (list.get(position).equalsIgnoreCase(ctx.getString(R.string.take_picture))) {

                        }
                        if (list.get(position).equalsIgnoreCase(ctx.getString(R.string.gallery))) {

                        }
                        if (list.get(position).equalsIgnoreCase(ctx.getString(R.string.quick_status))) {
                            getQuickStatusPopup();
                        }
                        actionsWindow.dismiss();
                    }
                });
                actionsWindow.show();
            }
        });
    }
    private void getQuickStatusPopup() {

        ListPopupWindow win = new ListPopupWindow(ctx);
        win.setAnchorView(txtTitle);
        win.setPromptView(Util.getHeroView(ctx, project.getProjectName()));
        win.setPromptPosition(ListPopupWindow.POSITION_PROMPT_ABOVE);
        win.setWidth(Util.getPopupWidth(getActivity()));
        win.setHorizontalOffset(Util.getPopupHorizontalOffset(getActivity()));

        win.setAdapter(new ExecStatusListAdapter(ctx,R.layout.quick_status, project.getProjectSiteTaskStatusList()));
        win.show();
    }
    private void setFields() {
        txtCount = (TextView)view.findViewById(R.id.MAIN_count);
        txtTitle = (TextView)view.findViewById(R.id.MAIN_title);
        editSearch = (EditText)view.findViewById(R.id.MAIN_search);
        grid = (GridView)view.findViewById(R.id.MAIN_grid);
        imgHero = (ImageView)view.findViewById(R.id.MAIN_hero);
        imgSearch = (ImageView)view.findViewById(R.id.MAIN_imgSearch);
        heroLayout = view.findViewById(R.id.MAIN_heroLayout);

        imgHero.setImageDrawable(Util.getRandomHeroImageExec(ctx));

        imgSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                search();
                hideKeyboard();
            }
        });

    }
    private void search() {
        if (editSearch.getText().toString().isEmpty()) {
            return;
        }
        int index = 0;
        boolean found = false;
        for (ProjectDTO site: projectList) {
            if (site.getProjectName().contains(editSearch.getText().toString())) {
                found = true;
                break;
            }
            index++;
        }
        if (!found) {
           Util.showToast(ctx,ctx.getString(R.string.no_projects) + editSearch.getText().toString());

        } else {
            grid.setSelection(index);
        }

    }
    void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) ctx
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(editSearch.getWindowToken(), 0);
        imm.hideSoftInputFromWindow(editSearch.getWindowToken(), 0);
    }
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            listener = (ExecProjectGridFragmentListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement ExecProjectGridFragmentListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    @Override
    public void animateCounts() {

    }

    public interface ExecProjectGridFragmentListener {
        public void onStatusCountClicked(ProjectDTO project);
    }


    ExecProjectGridFragmentListener listener;
    ProjectDTO project;
    List<ProjectDTO> projectList;

}

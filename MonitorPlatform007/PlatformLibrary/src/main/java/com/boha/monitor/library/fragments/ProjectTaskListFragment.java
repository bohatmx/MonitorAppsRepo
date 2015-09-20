package com.boha.monitor.library.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.boha.monitor.library.activities.MonApp;
import com.boha.monitor.library.adapters.ProjectTaskAdapter;
import com.boha.monitor.library.dto.ProjectDTO;
import com.boha.monitor.library.dto.ProjectTaskDTO;
import com.boha.monitor.library.dto.TaskTypeDTO;
import com.boha.monitor.library.util.SimpleDividerItemDecoration;
import com.boha.platform.library.R;
import com.squareup.leakcanary.RefWatcher;
import com.yqritc.recyclerviewflexibledivider.HorizontalDividerItemDecoration;

import java.util.ArrayList;
import java.util.List;

import jp.wasabeef.recyclerview.animators.SlideInLeftAnimator;


public class ProjectTaskListFragment extends Fragment implements PageFragment{

    ProjectDTO project;
    TaskTypeDTO taskType;
    private View view;
    private TextView txtTaskType, txtCount;
    private RecyclerView mRecyclerView;
    private List<ProjectTaskDTO> projectTaskList;
    private ProjectTaskAdapter projectTaskAdapter;
    static final String LOG = ProjectTaskListFragment.class.getSimpleName();


    private void buildList() {
        projectTaskList = project.getProjectTaskList();
        if (taskType != null) {
            List<ProjectTaskDTO> list = new ArrayList<>();
            for (ProjectTaskDTO pt : projectTaskList) {
                if (pt.getTask().getTaskTypeID().intValue() == taskType.getTaskTypeID().intValue()) {
                    list.add(pt);
                }
            }

            projectTaskList = list;
        }
        Log.i(LOG,"++ project has been set");
        if (view != null) {
            setList();
        } else {
            Log.e(LOG, "$%#$## WTF?");
        }
    }
    public void setTaskType(TaskTypeDTO taskType) {
        this.taskType = taskType;

        if (txtTaskType != null) {
            txtTaskType.setText(taskType.getTaskTypeName());
            buildList();
        }
    }

    public static ProjectTaskListFragment newInstance(ProjectDTO project) {
        ProjectTaskListFragment fragment = new ProjectTaskListFragment();
        Bundle args = new Bundle();
        args.putSerializable("project", project);
        fragment.setArguments(args);
        return fragment;
    }

    public ProjectTaskListFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            project = (ProjectDTO) getArguments().getSerializable("project");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_project_task_list, container, false);
        Log.i(LOG,"++ onCreateView");
        txtCount = (TextView)view.findViewById(R.id.PRH_count);
        txtTaskType = (TextView)view.findViewById(R.id.PRH_programme);

        mRecyclerView = (RecyclerView) view.findViewById(R.id.recycler);
        mRecyclerView.addItemDecoration(new HorizontalDividerItemDecoration.Builder(getActivity())
                .color(ContextCompat.getColor(getActivity(), R.color.blue_gray_500))
                .sizeResId(R.dimen.mon_divider)
                .marginResId(R.dimen.mon_padding, R.dimen.mon_padding)
                .build());

        mRecyclerView.setItemAnimator(new SlideInLeftAnimator());
        LinearLayoutManager llm = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        GridLayoutManager glm = new GridLayoutManager(getActivity(), 2, GridLayoutManager.VERTICAL, true);
        mRecyclerView.addItemDecoration(new SimpleDividerItemDecoration(getActivity()));
        mRecyclerView.setLayoutManager(llm);
        mRecyclerView.setHasFixedSize(true);
        if (taskType != null) {
            txtTaskType.setText(taskType.getTaskTypeName());
        } else {
            txtTaskType.setText("");
        }


        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(LOG,"#### onResume");
        buildList();
    }
    private void setList() {
        Log.i(LOG,"+++ setList");
        txtCount.setText("" + projectTaskList.size());
        if (projectTaskList.isEmpty()) {
            return;
        }

        projectTaskAdapter = new ProjectTaskAdapter(projectTaskList, darkColor, getActivity(), new ProjectTaskAdapter.TaskListener() {
            @Override
            public void onTaskNameClicked(ProjectTaskDTO projTask, int position) {
                projectTask = projTask;
                mListener.onStatusUpdateRequested(projectTask,position);
            }


        });
        mRecyclerView.setAdapter(projectTaskAdapter);

    }
    private ProjectTaskDTO projectTask;
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        Log.d(LOG,"++ onAttach");
        try {
            mListener = (StatusUpdateListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement StatusUpdateListener");
        }
    }
    StatusUpdateListener mListener;
    @Override
    public void onDetach() {
        super.onDetach();
//        mListener = null;
    }

    @Override
    public void animateHeroHeight() {

    }
    String pageTitle;
    @Override
    public void setPageTitle(String title) {
        pageTitle = title;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        RefWatcher refWatcher = MonApp.getRefWatcher(getActivity());
        refWatcher.watch(this);
    }
    @Override
    public String getPageTitle() {
        return pageTitle;
    }

    public void refreshProjectTask(ProjectTaskDTO projectTask) {

        for (ProjectTaskDTO x: projectTaskList) {
            if (x.getProjectTaskID().intValue() == projectTask.getProjectTaskID().intValue()) {
                x = projectTask;
                projectTaskAdapter.notifyDataSetChanged();
                break;
            }
        }
    }
    public void setDarkColor(int darkColor) {
        this.darkColor = darkColor;
    }

    public interface StatusUpdateListener {
        void onStatusUpdateRequested(ProjectTaskDTO task, int position);
        void onCameraRequested(ProjectTaskDTO task);
    }
    int primaryColor, darkColor;
    @Override
    public void setThemeColors(int primaryColor, int darkColor) {
        this.primaryColor = primaryColor;
        this.darkColor = darkColor;
    }
}

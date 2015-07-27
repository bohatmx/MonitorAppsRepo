package com.boha.monitor.library.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.boha.monitor.library.adapters.TaskAdapter;
import com.boha.monitor.library.dto.ProjectDTO;
import com.boha.monitor.library.dto.ProjectTaskDTO;
import com.boha.monitor.library.dto.TaskTypeDTO;
import com.boha.platform.library.R;
import com.yqritc.recyclerviewflexibledivider.HorizontalDividerItemDecoration;

import java.util.ArrayList;
import java.util.List;

import jp.wasabeef.recyclerview.animators.SlideInLeftAnimator;


public class ProjectTaskListFragment extends Fragment implements PageFragment{

    ProjectDTO project;
    TaskTypeDTO taskType;
    private View view;
    private TextView txtProject, txtCount;
    private RecyclerView mRecyclerView;
    private List<ProjectTaskDTO> projectTaskList;
    private TaskAdapter taskAdapter;
    static final String LOG = ProjectTaskListFragment.class.getSimpleName();

    public void setProject(ProjectDTO project) {
        this.project = project;
        projectTaskList = project.getProjectTaskList();
        List<ProjectTaskDTO> list = new ArrayList<>();
        for (ProjectTaskDTO pt: projectTaskList) {
            if (pt.getTask().getTaskTypeID().intValue() == taskType.getTaskTypeID().intValue()) {
                list.add(pt);
            }
        }
        projectTaskList = list;
        Log.i(LOG,"++ project has been set");
        if (view != null) {
            setList();
        } else {
            Log.e(LOG,"$%#$## WTF?");
        }

    }

    public void setTaskType(TaskTypeDTO taskType) {
        this.taskType = taskType;
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
        Log.i(LOG,"++ onCreate");
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
        txtProject= (TextView)view.findViewById(R.id.PRH_programme);

        mRecyclerView = (RecyclerView) view.findViewById(R.id.recycler);
        mRecyclerView.addItemDecoration(new HorizontalDividerItemDecoration.Builder(getActivity())
                .color(getResources().getColor(R.color.blue_gray_500))
                .sizeResId(R.dimen.mon_divider)
                .marginResId(R.dimen.mon_padding, R.dimen.mon_padding)
                .build());

        mRecyclerView.setItemAnimator(new SlideInLeftAnimator());
        LinearLayoutManager llm = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        GridLayoutManager glm = new GridLayoutManager(getActivity(), 2, GridLayoutManager.VERTICAL, true);
        mRecyclerView.setLayoutManager(llm);
        mRecyclerView.setHasFixedSize(true);
        return view;
    }

    private void setList() {
        Log.i(LOG,"++ project has been set");
        txtCount.setText("" + projectTaskList.size());
        if (projectTaskList.isEmpty()) {
            return;
        }
        if (projectTaskList.get(0).getProjectName() != null) {
            txtProject.setText(projectTaskList.get(0).getProjectName());
        }


        taskAdapter = new TaskAdapter(projectTaskList, darkColor, getActivity(), new TaskAdapter.TaskListener() {
            @Override
            public void onTaskNameClicked(ProjectTaskDTO task) {
                mListener.onStatusUpdateRequested(task);
            }


        });
        mRecyclerView.setAdapter(taskAdapter);

    }
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

    @Override
    public void setPageTitle(String title) {

    }

    @Override
    public String getPageTitle() {
        return null;
    }
    int darkColor;

    public void setDarkColor(int darkColor) {
        this.darkColor = darkColor;
    }

    public interface StatusUpdateListener {
        void onStatusUpdateRequested(ProjectTaskDTO task);
        void onCameraRequested(ProjectTaskDTO task);
    }
}

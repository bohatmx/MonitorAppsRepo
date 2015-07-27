package com.boha.monitor.library.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.boha.monitor.library.adapters.TaskTypeAdapter;
import com.boha.monitor.library.dto.ProjectDTO;
import com.boha.monitor.library.dto.ResponseDTO;
import com.boha.monitor.library.dto.TaskTypeDTO;
import com.boha.monitor.library.util.CacheUtil;
import com.boha.monitor.library.util.SpacesItemDecoration;
import com.boha.monitor.library.util.Util;
import com.boha.platform.library.R;

import java.util.List;

import jp.wasabeef.recyclerview.animators.FadeInAnimator;

public class TaskTypeListFragment extends Fragment {


    private TaskTypeListener mListener;
    private List<TaskTypeDTO> taskTypeList;
    private ProjectDTO project;
    private TaskTypeAdapter adapter;
    private TextView txtCount, txtName;
    private RecyclerView mRecyclerView;
    ImageView hero;
    int darkColor;

    private View view;
    public static TaskTypeListFragment newInstance() {
        TaskTypeListFragment fragment = new TaskTypeListFragment();
        return fragment;
    }

    public TaskTypeListFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d("TaskTypeListFragment", "### onCreateView");
        view = inflater.inflate(R.layout.fragment_task_type_list, container, false);
        txtCount = (TextView)view.findViewById(R.id.PRH_count);
        txtName = (TextView)view.findViewById(R.id.PRH_programme);
        hero = (ImageView)view.findViewById(R.id.PRH_image);
        hero.setImageDrawable(Util.getRandomHeroImage(getActivity()));
        mRecyclerView = (RecyclerView) view.findViewById(R.id.recycler);
//        LinearLayoutManager llm = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        GridLayoutManager glm = new GridLayoutManager(getActivity(), 2, GridLayoutManager.VERTICAL, false);

        mRecyclerView.setItemAnimator(new FadeInAnimator());
        int spacingInPixels = getResources().getDimensionPixelSize(R.dimen.mon_divider);
        mRecyclerView.addItemDecoration(new SpacesItemDecoration(spacingInPixels));
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(glm);

        return view;
    }

    private void getCachedTypes() {
        Log.d("TaskTypeListFragment","### getCachedTypes");
        CacheUtil.getCachedMonitorProjects(getActivity(), new CacheUtil.CacheUtilListener() {
            @Override
            public void onFileDataDeserialized(ResponseDTO response) {
                if (response.getTaskTypeList() != null) {
                    taskTypeList = response.getTaskTypeList();
                    if (mRecyclerView != null) {
                        setList();
                    }
                }
            }

            @Override
            public void onDataCached() {

            }

            @Override
            public void onError() {

            }
        });
    }

    public void setDarkColor(int darkColor) {
        this.darkColor = darkColor;
    }

    public void setProject(ProjectDTO project) {
        Log.i("TaskTypeListFragment","### setProject");
        this.project = project;
        getCachedTypes();
    }

    private void setList() {
        txtCount.setText("" + taskTypeList.size());
        txtName.setText(project.getProjectName());
        Log.d("TaskTypeListFragment","### setList");



        adapter = new TaskTypeAdapter(taskTypeList, darkColor, getActivity(), new TaskTypeAdapter.TaskListener() {
            @Override
            public void onTaskTypeNameClicked(TaskTypeDTO tt) {

                mListener.onTaskTypeClicked(tt);
            }
        });
        mRecyclerView.setAdapter(adapter);

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (TaskTypeListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


    public interface TaskTypeListener {
        // TODO: Update argument type and name
        public void onTaskTypeClicked(TaskTypeDTO taskType);
    }

}

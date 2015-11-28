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

import com.boha.monitor.library.activities.MonApp;
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

public class TaskTypeListFragment extends Fragment implements PageFragment {


    private TaskTypeListener mListener;
    private List<TaskTypeDTO> taskTypeList;
    private ProjectDTO project;
    private TaskTypeAdapter adapter;
    private TextView txtCount, txtName;
    private RecyclerView mRecyclerView;
    ImageView hero;
    int darkColor, type;
    public static final int STAFF = 1, MONITOR = 2;

    private View view;

    public static TaskTypeListFragment newInstance(ProjectDTO project, int type) {
        TaskTypeListFragment fragment = new TaskTypeListFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable("project",project);
        bundle.putInt("type", type);
        fragment.setArguments(bundle);
        return fragment;
    }

    public TaskTypeListFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            type = getArguments().getInt("type",0);
            project = (ProjectDTO)getArguments().getSerializable("project");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d("TaskTypeListFragment", "### onCreateView");
        view = inflater.inflate(R.layout.fragment_task_type_list, container, false);
        txtCount = (TextView) view.findViewById(R.id.PRH_count);
        txtName = (TextView) view.findViewById(R.id.PRH_programme);
        hero = (ImageView) view.findViewById(R.id.PRH_image);
        hero.setImageDrawable(Util.getRandomBackgroundImage(getActivity()));
        mRecyclerView = (RecyclerView) view.findViewById(R.id.recycler);
//        LinearLayoutManager llm = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        GridLayoutManager glm = new GridLayoutManager(getActivity(), 2, GridLayoutManager.VERTICAL, false);

        mRecyclerView.setItemAnimator(new FadeInAnimator());
        int spacingInPixels = getResources().getDimensionPixelSize(R.dimen.mon_divider_small);
        mRecyclerView.addItemDecoration(new SpacesItemDecoration(spacingInPixels));
        mRecyclerView.setHasFixedSize(false);
        mRecyclerView.setLayoutManager(glm);

        getCachedTypes();

        return view;
    }

    private void getCachedTypes() {
        Log.d("TaskTypeListFragment", "### getCachedTypes");
        switch (type) {
            case MONITOR:
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
                break;
            case STAFF:
                CacheUtil.getCachedStaffData(getActivity(), new CacheUtil.CacheUtilListener() {
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
                break;
        }
    }

    public void setDarkColor(int darkColor) {
        this.darkColor = darkColor;
    }

    private void setList() {
        txtCount.setText("" + taskTypeList.size());
        txtName.setText(project.getProjectName());

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
                    + " must implement TaskTypeListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
//        RefWatcher refWatcher = MonApp.getRefWatcher(getActivity());
//        refWatcher.watch(this);
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

    @Override
    public void setThemeColors(int primaryColor, int darkColor) {
        this.darkColor = darkColor;

    }

    public interface TaskTypeListener {
         void onTaskTypeClicked(TaskTypeDTO taskType);
    }

}

package com.boha.monitor.library.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.boha.monitor.library.activities.MonApp;
import com.boha.monitor.library.adapters.TaskAdapter;
import com.boha.monitor.library.dto.ProjectDTO;
import com.boha.monitor.library.dto.RequestDTO;
import com.boha.monitor.library.dto.ResponseDTO;
import com.boha.monitor.library.dto.TaskDTO;
import com.boha.monitor.library.services.RequestIntentService;
import com.boha.monitor.library.util.SharedUtil;
import com.boha.monitor.library.util.SimpleDividerItemDecoration;
import com.boha.monitor.library.util.Snappy;
import com.boha.monitor.library.util.Util;
import com.boha.platform.library.R;

import java.util.List;


public class TaskListFragment extends Fragment implements PageFragment {


    private TaskListListener mListener;
    private List<TaskDTO> taskList;
    private ProjectDTO project;
    private TaskAdapter adapter;
    private TextView txtCount, txtName;
    private EditText editTaskName;
    private ImageView plusIcon;
    private RecyclerView mRecyclerView;
    ImageView hero;
    int darkColor, type;
    MonApp app;

    public void setApp(MonApp app) {
        this.app = app;
    }

    public void setListener(TaskListListener mListener) {
        this.mListener = mListener;
    }

    public static final int STAFF = 1, MONITOR = 2;

    private View view;

    public TaskListFragment() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d("TaskListFragment", "### onCreateView");
        view = inflater.inflate(R.layout.fragment_task_type_list, container, false);
        setFields();

        getCachedTasks();

        return view;
    }

    private void getCachedTasks() {
        Log.d("TaskListFragment", "### getCachedTasks");
        Snappy.getTaskList(app, new Snappy.SnappyReadListener() {
            @Override
            public void onDataRead(ResponseDTO response) {
                if (response.getTaskTypeList() != null) {
                    taskList = response.getTaskList();
                    if (mRecyclerView != null) {
                        setList();
                    }
                }
            }

            @Override
            public void onError(String message) {

            }
        });

    }

    private void addTask(final TaskDTO task) {
        RequestDTO w = new RequestDTO(RequestDTO.ADD_TASKS);
        w.getTaskList().add(task);

        Snappy.cacheRequest(app, w, new Snappy.SnappyWriteListener() {
            @Override
            public void onDataWritten() {

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        taskList.add(0,task);
                        adapter.notifyDataSetChanged();
                        txtCount.setText("" + taskList.size());

                        Intent m = new Intent(getContext(), RequestIntentService.class);
                        getActivity().startService(m);
                        mListener.onTaskAdded(task);
                    }
                });
            }

            @Override
            public void onError(String message) {
                Util.showErrorToast(getActivity(),message);
            }
        });

    }
    private void setFields() {
        txtCount = (TextView) view.findViewById(R.id.PRH_count);
        txtName = (TextView) view.findViewById(R.id.PRH_company);
        editTaskName = (EditText) view.findViewById(R.id.PRH_editName);
        hero = (ImageView) view.findViewById(R.id.PRH_image);
        plusIcon = (ImageView) view.findViewById(R.id.PRH_plusIcon);
        hero.setImageDrawable(Util.getRandomBackgroundImage(getActivity()));
        mRecyclerView = (RecyclerView) view.findViewById(R.id.recycler);
        LinearLayoutManager llm = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        mRecyclerView.addItemDecoration(new SimpleDividerItemDecoration(getActivity()));

        mRecyclerView.setHasFixedSize(false);
        mRecyclerView.setLayoutManager(llm);

        plusIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (editTaskName.getText().toString().isEmpty()) {
                    Util.showErrorToast(getActivity(),"Please enter name");
                    return;
                }
                TaskDTO t = new TaskDTO();
                t.setCompanyID(SharedUtil.getCompany(getActivity()).getCompanyID());
                t.setTaskName(editTaskName.getText().toString());

                addTask(t);
            }
        });


    }
    public void setDarkColor(int darkColor) {
        this.darkColor = darkColor;
    }

    private void setList() {
        txtCount.setText("" + taskList.size());
        txtName.setText(SharedUtil.getCompany(getActivity()).getCompanyName());

        adapter = new TaskAdapter(taskList, darkColor, getActivity(), new TaskAdapter.TaskListener() {
            @Override
            public void onTaskClicked(TaskDTO tt) {

            }
        });
        mRecyclerView.setAdapter(adapter);

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (TaskListListener) activity;
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

    public interface TaskListListener {
        void onTaskAdded(TaskDTO task);
    }

}

package com.boha.monitor.setup.fragments;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.boha.monitor.library.dto.TaskDTO;
import com.boha.monitor.library.dto.TaskTypeDTO;
import com.boha.monitor.setup.R;
import com.boha.monitor.setup.adapters.TaskDataAdapter;
import com.yqritc.recyclerviewflexibledivider.HorizontalDividerItemDecoration;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link TaskListener} interface
 * to handle interaction events.
 * Use the {@link TaskListFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TaskListFragment extends Fragment {

    private TaskListener mListener;
    private TaskTypeDTO taskType;
    private RecyclerView recyclerView;
    private TextView txtHeader, txtCount;
    private View view;
    private TaskDataAdapter adapter;
    private Context ctx;


    public static TaskListFragment newInstance(TaskTypeDTO taskType) {
        TaskListFragment fragment = new TaskListFragment();
        Bundle args = new Bundle();
        args.putSerializable("taskType", taskType);
        fragment.setArguments(args);
        return fragment;
    }

    public TaskListFragment() {
    }

    public void setTaskType(TaskTypeDTO taskType) {
        this.taskType = taskType;
        setList();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ctx = getActivity();
        if (getArguments() != null) {
            taskType = (TaskTypeDTO) getArguments().getSerializable("taskType");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        this.inflater = inflater;
        view = inflater.inflate(R.layout.fragment_list, container, false);
        setFields();

        return view;
    }

    LayoutInflater inflater;
    TextView txtTasks, txtSubTasks;
    LinearLayout emptyLayout;

    private void setFields() {
        emptyLayout = (LinearLayout) view.findViewById(R.id.FCL_emptyLayout);
        View counts = inflater.inflate(R.layout.tasks_subtasks_counts, null);
        txtTasks = (TextView) counts.findViewById(R.id.COUNTS_taskCount);
        txtSubTasks = (TextView) counts.findViewById(R.id.COUNTS_subtaskCount);
        emptyLayout.addView(counts);

        recyclerView = (RecyclerView) view.findViewById(R.id.FCL_list);
        txtCount = (TextView) view.findViewById(R.id.FCL_count);
        txtHeader = (TextView) view.findViewById(R.id.FCL_title);
        txtCount.setVisibility(View.GONE);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(ctx,
                LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setHasFixedSize(true);
        recyclerView.setRecyclerListener(new RecyclerView.RecyclerListener() {
            @Override
            public void onViewRecycled(RecyclerView.ViewHolder holder) {
            }
        });
        recyclerView.addItemDecoration(
                new HorizontalDividerItemDecoration.Builder(getActivity())
                        .color(R.color.green_100)
                        .sizeResId(R.dimen.mon_divider)
                        .marginResId(R.dimen.mon_divider, R.dimen.mon_divider)
                        .build());
    }

    private void setList() {
        adapter = new TaskDataAdapter(taskType.getTaskList(), ctx, new TaskDataAdapter.TaskListener() {
            @Override
            public void onTaskClicked(TaskDTO task) {

            }

            @Override
            public void onIconDeleteClicked(TaskDTO task, int position) {

            }

            @Override
            public void onIconEditClicked(TaskDTO task, int position) {

            }
        });
        recyclerView.setAdapter(adapter);
        txtHeader.setText(taskType.getTaskTypeName());
        txtCount.setText("" + taskType.getTaskList().size());
        int tasks = 0, subTasks = 0;
        if (taskType.getTaskList() != null) {
            tasks += taskType.getTaskList().size();
            for (TaskDTO x : taskType.getTaskList()) {
                if (x.getSubTaskList() != null) {
                    subTasks += x.getSubTaskList().size();
                }
            }

        }
        txtTasks.setText("" + tasks);
        txtSubTasks.setText("" + subTasks);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (TaskListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.getLocalClassName()
                    + " must implement TaskListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface TaskListener {
        void onTaskClicked(TaskDTO task);

        void onSubTaskCountClicked(TaskDTO task);

        void onIconDeleteClicked(TaskDTO task, int position);

        void onIconEditClicked(TaskDTO task, int position);

        void setBusy(boolean busy);
    }
}

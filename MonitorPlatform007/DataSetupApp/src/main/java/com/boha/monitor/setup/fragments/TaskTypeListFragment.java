package com.boha.monitor.setup.fragments;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.boha.monitor.library.dto.CompanyDTO;
import com.boha.monitor.library.dto.ProgrammeDTO;
import com.boha.monitor.library.dto.TaskDTO;
import com.boha.monitor.library.dto.TaskTypeDTO;
import com.boha.monitor.setup.R;
import com.boha.monitor.setup.adapters.TaskTypeDataAdapter;
import com.yqritc.recyclerviewflexibledivider.HorizontalDividerItemDecoration;


public class TaskTypeListFragment extends Fragment {

    private TaskTypeDataAdapter.TaskTypeListener mListener;
    private ProgrammeDTO programme;
    private RecyclerView recyclerView;
    private TextView txtHeader, txtCount;
    private View view;
    private TaskTypeDataAdapter adapter;
    private Context ctx;


    public static TaskTypeListFragment newInstance(CompanyDTO company) {
        TaskTypeListFragment fragment = new TaskTypeListFragment();
        Bundle args = new Bundle();
        args.putSerializable("programme", company);
        fragment.setArguments(args);
        return fragment;
    }

    public TaskTypeListFragment() {
    }

    public void setProgramme(ProgrammeDTO programme) {
        this.programme = programme;
        setList();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ctx = getActivity();
        if (getArguments() != null) {
            programme = (ProgrammeDTO) getArguments().getSerializable("programme");
        } else {
            if (savedInstanceState != null) {
                programme = (ProgrammeDTO)savedInstanceState.getSerializable("programme");
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        this.inflater = inflater;
        view = inflater.inflate(R.layout.fragment_list, container, false);
        setFields();
        if (savedInstanceState != null) {
            programme = (ProgrammeDTO)savedInstanceState.getSerializable("programme");
        }

        return view;
    }
    LayoutInflater inflater;
    TextView txtTasks, txtSubTasks;
    LinearLayout emptyLayout;
    private void setFields() {
        emptyLayout = (LinearLayout)view.findViewById(R.id.FCL_emptyLayout);
        View counts = inflater.inflate(R.layout.tasks_subtasks_counts, null);
        txtTasks = (TextView)counts.findViewById(R.id.COUNTS_taskCount);
        txtSubTasks = (TextView)counts.findViewById(R.id.COUNTS_subtaskCount);
        emptyLayout.addView(counts);

        recyclerView = (RecyclerView)view.findViewById(R.id.FCL_list);
        txtCount = (TextView)view.findViewById(R.id.FCL_count);
        txtHeader = (TextView)view.findViewById(R.id.FCL_title);

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
        adapter = new TaskTypeDataAdapter(programme.getTaskTypeList(), ctx, new TaskTypeDataAdapter.TaskTypeListener() {
            @Override
            public void onTaskTypeClicked(TaskTypeDTO taskType) {
                mListener.onTaskTypeClicked(taskType);
            }

            @Override
            public void onTaskCountClicked(TaskTypeDTO taskType) {
                mListener.onTaskCountClicked(taskType);
            }

            @Override
            public void onIconDeleteClicked(TaskTypeDTO taskType, int position) {
                mListener.onIconDeleteClicked(taskType,position);
            }

            @Override
            public void onIconEditClicked(TaskTypeDTO taskType, int position) {
                mListener.onIconEditClicked(taskType,position);
            }

            @Override
            public void setBusy(boolean busy) {

            }
        });
        recyclerView.setAdapter(adapter);
        txtHeader.setText(programme.getProgrammeName());
        txtCount.setText("" + programme.getTaskTypeList().size());
        int tasks = 0, subTasks = 0;
        for (TaskTypeDTO dto: programme.getTaskTypeList()) {
            if (dto.getTaskList() != null) {
                tasks += dto.getTaskList().size();
                for (TaskDTO x: dto.getTaskList()) {
                    if (x.getSubTaskList() != null) {
                        subTasks += x.getSubTaskList().size();
                    }
                }
            }
        }
        txtTasks.setText("" + tasks);
        txtSubTasks.setText("" + subTasks);
    }
    @Override
    public void onSaveInstanceState(Bundle b) {
        b.putSerializable("programme", programme);
        super.onSaveInstanceState(b);
        Log.d("TaskTypeListFragment","onSaveInstanceState programme saved");
    }
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (TaskTypeDataAdapter.TaskTypeListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.getLocalClassName()
                    + " must implement TaskTypeListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }
}

package com.boha.monitor.library.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.boha.monitor.library.activities.MonApp;
import com.boha.monitor.library.adapters.TaskAdapter;
import com.boha.monitor.library.dto.ProjectDTO;
import com.boha.monitor.library.dto.ProjectTaskDTO;
import com.boha.monitor.library.dto.RequestDTO;
import com.boha.monitor.library.dto.ResponseDTO;
import com.boha.monitor.library.dto.TaskDTO;
import com.boha.monitor.library.util.OKHttpException;
import com.boha.monitor.library.util.OKUtil;
import com.boha.monitor.library.util.SharedUtil;
import com.boha.monitor.library.util.SimpleDividerItemDecoration;
import com.boha.monitor.library.util.Snappy;
import com.boha.monitor.library.util.Util;
import com.boha.platform.library.R;

import java.util.ArrayList;
import java.util.List;

import jp.wasabeef.recyclerview.animators.FadeInAnimator;

public class ProjectTaskFragment extends Fragment {


    RecyclerView mRecyclerView;
    TextView txtTitle, txtCount, txtTotal;
    ImageView addIcon;
    EditText editName;
    View view;
    ProjectDTO project;
    List<ProjectDTO> projectList;
    List<TaskDTO> taskList;
    List<ProjectTaskDTO> projectTaskList = new ArrayList<>();
    MonApp app;
    int type;

    public static final int
            ADD_NEW_PROJECT = 1,
            ASSIGN_TASKS_TO_ONE_PROJECT = 3;

    public ProjectTaskFragment() {
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
        view = inflater.inflate(R.layout.fragment_project_task, container, false);
        setFields();
        return view;
    }

    private void setFields() {
        txtCount = (TextView) view.findViewById(R.id.FPT_count);
        txtTitle = (TextView) view.findViewById(R.id.FPT_title);
        txtTotal = (TextView) view.findViewById(R.id.FPT_count2);
        editName = (EditText) view.findViewById(R.id.FPT_editProjectName);
        addIcon = (ImageView) view.findViewById(R.id.FPT_addIcon);
        checkBox = (CheckBox) view.findViewById(R.id.FPT_chkBoxAll);

        mRecyclerView = (RecyclerView) view.findViewById(R.id.FPT_recycler);
        LinearLayoutManager llm = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setItemAnimator(new FadeInAnimator());
        mRecyclerView.addItemDecoration(new SimpleDividerItemDecoration(getActivity()));

        mRecyclerView.setHasFixedSize(false);
        mRecyclerView.setLayoutManager(llm);


        txtTitle.setText("");
        checkBox.setVisibility(View.GONE);
        updateFields();
        addIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (type) {
                    case ADD_NEW_PROJECT:
                        sendNewProjectData();
                        break;

                    case ASSIGN_TASKS_TO_ONE_PROJECT:
                        sendProjectTasksData();
                        break;
                }
            }
        });
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    for (TaskDTO t : taskList) {
                        t.setSelected(Boolean.TRUE);
                    }
                    txtCount.setText("" + taskList.size());
                } else {
                    for (TaskDTO t : taskList) {
                        t.setSelected(Boolean.FALSE);
                    }
                    txtCount.setText("00");
                }

                adapter.notifyDataSetChanged();
            }
        });
    }

    private void updateFields() {
        try {
            switch (type) {
                case ADD_NEW_PROJECT:
                    break;
                case ASSIGN_TASKS_TO_ONE_PROJECT:
                    addIcon.setVisibility(View.VISIBLE);
                    editName.setVisibility(View.GONE);
                    getTasks();
                    break;
            }
        } catch (Exception e) {
        }
    }

    CheckBox checkBox;

    private void sendProjectTasksData() {

        List<ProjectTaskDTO> ptList = new ArrayList<>();
        for (TaskDTO task : taskList) {
            ProjectTaskDTO pt = new ProjectTaskDTO();
            pt.setProjectID(project.getProjectID());
            pt.setTask(task);
            pt.setLatitude(project.getLatitude());
            pt.setLongitude(project.getLongitude());
            ptList.add(pt);

        }
        for (ProjectTaskDTO pt: ptList) {
            for (ProjectTaskDTO old: projectTaskList) {
                if (old.getTask().getTaskID().intValue() ==
                        pt.getTask().getTaskID().intValue()) {
                   pt.setProjectTaskID(old.getProjectTaskID());
                }
            }
        }
        RequestDTO w = new RequestDTO(RequestDTO.ADD_PROJECT_TASKS);
        w.setProjectTaskList(ptList);


        OKUtil okUtil = new OKUtil();
        try {
            okUtil.sendPOSTRequest(getActivity(), w, new OKUtil.OKListener() {
                @Override
                public void onResponse(ResponseDTO response) {
                    final List<ProjectTaskDTO> list = response.getProjectTaskList();
                    Snappy.getProject(app, project.getProjectID(), new Snappy.SnappyProjectListener() {
                        @Override
                        public void onProjectFound(ProjectDTO p) {
                            project = p;
                            project.setProjectTaskList(list);
                            List<ProjectDTO> mList = new ArrayList<>();
                            mList.add(project);
                            Snappy.writeProjectList(app, mList, new Snappy.SnappyWriteListener() {
                                @Override
                                public void onDataWritten() {
                                    Log.w(LOG,"Yup.Yup. Project updated in cache with new projectTasks");
                                }

                                @Override
                                public void onError(String message) {

                                }
                            });
                        }

                        @Override
                        public void onError() {

                        }
                    });
                }

                @Override
                public void onError(String message) {

                }
            });
        } catch (OKHttpException e) {
            e.printStackTrace();
        }
    }

    private void sendNewProjectData() {

        if (editName.getText().toString().isEmpty()) {
            Util.showErrorToast(getActivity(), "Please enter Project name");
            return;
        }
        addIcon.setVisibility(View.GONE);
        ProjectDTO p = new ProjectDTO();
        p.setCompanyID(SharedUtil.getCompany(getActivity()).getCompanyID());
        p.setProjectName(editName.getText().toString());
        p.setActiveFlag(Boolean.TRUE);

        RequestDTO w = new RequestDTO(RequestDTO.ADD_PROJECTS);
        w.getProjectList().add(p);
        OKUtil okUtil = new OKUtil();
        try {
            okUtil.sendGETRequest(getActivity(), w, new OKUtil.OKListener() {
                @Override
                public void onResponse(final ResponseDTO response) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            project = response.getProjectList().get(0);
                            txtTitle.setText(project.getProjectName());
                            type = ASSIGN_TASKS_TO_ONE_PROJECT;
                            addIcon.setVisibility(View.VISIBLE);
                            hideKeyboard();
                            editName.setVisibility(View.GONE);
                            getTasks();

                        }
                    });

                }

                @Override
                public void onError(final String message) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            addIcon.setVisibility(View.VISIBLE);
                            Util.showErrorToast(getActivity(), message);
                        }
                    });

                }
            });
        } catch (OKHttpException e) {
            e.printStackTrace();
        }
    }

    private void getTasks() {
        Snappy.getTaskList(app, new Snappy.SnappyReadListener() {
            @Override
            public void onDataRead(final ResponseDTO response) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        taskList = response.getTaskList();
                        checkBox.setVisibility(View.VISIBLE);
                        setTaskList();
                    }
                });

            }

            @Override
            public void onError(String message) {

            }
        });
    }

    void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getActivity()
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(editName.getWindowToken(), 0);
    }

    TaskAdapter adapter;

    private void setTaskList() {
        switch (type) {
            case ADD_NEW_PROJECT:
                adapter = new TaskAdapter(taskList, 0, getContext(), new TaskAdapter.TaskListener() {
                    @Override
                    public void onTaskClicked(TaskDTO task) {
                        int count = 0;
                        for (TaskDTO t: taskList) {
                            if (t.isSelected()) {
                                count++;
                            }
                        }
                        txtCount.setText("" + count);
                        mListener.onTasksAssigned(projectTaskList);
                    }
                });
                break;

            case ASSIGN_TASKS_TO_ONE_PROJECT:
                adapter = new TaskAdapter(taskList, true, getContext(), new TaskAdapter.TaskListener() {
                    @Override
                    public void onTaskClicked(TaskDTO task) {
                        int count = 0;
                        for (TaskDTO t: taskList) {
                            if (t.isSelected()) {
                                count++;
                            }
                        }
                        txtCount.setText("" + count);
                        mListener.onTasksAssigned(projectTaskList);
                    }
                });
                break;
        }
        txtTotal.setText("" + taskList.size());
        matchUp();
        mRecyclerView.setAdapter(adapter);

    }

    private void matchUp() {
        int count = 0;
        for (ProjectTaskDTO p : project.getProjectTaskList()) {
            for (TaskDTO t : taskList) {
                if (p.getTask().getTaskID().intValue() == t.getTaskID().intValue()) {
                    t.setSelected(Boolean.TRUE);
                    count++;
                }
            }
        }
        txtCount.setText("" + count);
        if (count == taskList.size()) {
            checkBox.setChecked(true);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof ProjectTaskListener) {
            mListener = (ProjectTaskListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement ProjectTaskListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    ProjectTaskListener mListener;

    public interface ProjectTaskListener {
        void onTasksAssigned(List<ProjectTaskDTO> projectTaskList);
    }

    public void setListener(ProjectTaskListener mListener) {
        this.mListener = mListener;
    }

    public void setProject(final ProjectDTO p) {
        if (p == null) {
            return;
        }
        Snappy.getProject(app, p.getProjectID(), new Snappy.SnappyProjectListener() {
            @Override
            public void onProjectFound(final ProjectDTO p) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        project = p;
                        getTasks();
                        txtTitle.setText(project.getProjectName());
                        editName.setVisibility(View.GONE);
                        projectTaskList = project.getProjectTaskList();
                    }
                });
            }

            @Override
            public void onError() {

            }
        });

    }

    public void setApp(MonApp app) {
        this.app = app;
    }

    public void setType(int type) {
        this.type = type;
        updateFields();
        if (type == ASSIGN_TASKS_TO_ONE_PROJECT) {
            getTasks();
        }
    }

    static final String LOG = ProjectTaskFragment.class.getSimpleName();
}

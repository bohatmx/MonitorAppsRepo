package com.boha.monitor.library.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.boha.monitor.library.adapters.TaskStatusTypeAdapter;
import com.boha.monitor.library.dto.MonitorDTO;
import com.boha.monitor.library.dto.ProjectDTO;
import com.boha.monitor.library.dto.ProjectTaskDTO;
import com.boha.monitor.library.dto.ProjectTaskStatusDTO;
import com.boha.monitor.library.dto.RequestDTO;
import com.boha.monitor.library.dto.RequestList;
import com.boha.monitor.library.dto.ResponseDTO;
import com.boha.monitor.library.dto.StaffDTO;
import com.boha.monitor.library.dto.TaskStatusTypeDTO;
import com.boha.monitor.library.util.CacheUtil;
import com.boha.monitor.library.util.NetUtil;
import com.boha.monitor.library.util.RequestCacheUtil;
import com.boha.monitor.library.util.SharedUtil;
import com.boha.monitor.library.util.Util;
import com.boha.monitor.library.util.WebCheck;
import com.boha.platform.library.R;
import com.yqritc.recyclerviewflexibledivider.HorizontalDividerItemDecoration;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import jp.wasabeef.recyclerview.animators.SlideInLeftAnimator;


public class TaskStatusUpdateFragment extends Fragment implements PageFragment {

    public interface TaskStatusUpdateListener {
        void onCameraRequested(ProjectTaskDTO projectTask);
        void onStatusReturned(ProjectTaskStatusDTO projectTaskStatus);
        void setBusy(boolean busy);
    }
    ProjectDTO project;
    private View view, actionView;
    private TextView txtTaskName, txtStatusType, txtResult;
    private ProjectTaskDTO projectTask;
    private Button  btnSubmit;
    private RecyclerView mRecyclerView;
    private ImageView imgCamera, iconClose;

    static final String LOG = TaskStatusUpdateFragment.class.getSimpleName();

    public void setProjectTask(ProjectTaskDTO projectTask) {
        this.projectTask = projectTask;
        Log.i(LOG, "++ projectTask has been set");
        if (view != null) {
            txtTaskName.setText(projectTask.getTask().getTaskName());
        } else {
            Log.e(LOG, "$%#$## WTF?");
        }

    }

    public static TaskStatusUpdateFragment newInstance(ProjectTaskDTO projectTask) {
        TaskStatusUpdateFragment fragment = new TaskStatusUpdateFragment();
        Bundle args = new Bundle();
        args.putSerializable("projectTask", projectTask);
        fragment.setArguments(args);
        return fragment;
    }

    public TaskStatusUpdateFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(LOG, "++ onCreate");
        if (getArguments() != null) {
            projectTask = (ProjectTaskDTO) getArguments().getSerializable("projectTask");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_task_status_edit, container, false);
        actionView = view.findViewById(R.id.TSE_actionLayout);
        actionView.setVisibility(View.GONE);
        Log.i(LOG, "++ onCreateView");
        setFields();
        getStatusTypes();
        return view;
    }

    List<TaskStatusTypeDTO> taskStatusTypeList;

    private void getStatusTypes() {
        CacheUtil.getCachedData(getActivity(), CacheUtil.CACHE_DATA, new CacheUtil.CacheUtilListener() {
            @Override
            public void onFileDataDeserialized(ResponseDTO response) {
                if (response.getTaskStatusTypeList() != null) {
                    taskStatusTypeList = response.getTaskStatusTypeList();
                    setList();
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


    TaskStatusTypeAdapter adapter;
    private void setList() {
        txtTaskName.setText(projectTask.getTask().getTaskName());
        adapter = new TaskStatusTypeAdapter(taskStatusTypeList, darkColor, getActivity(), new TaskStatusTypeAdapter.TaskStatusTypeListener() {
            @Override
            public void onTaskStatusTypeClicked(TaskStatusTypeDTO tst) {
                txtStatusType.setText(tst.getTaskStatusTypeName());
                taskStatusType = tst;
                txtStatusType.setText(taskStatusType.getTaskStatusTypeName());
                switch (taskStatusType.getStatusColor()) {
                    case TaskStatusTypeDTO.STATUS_COLOR_RED:
                        btnSubmit.setBackgroundColor(getResources().getColor(R.color.red_800));

                        break;
                    case TaskStatusTypeDTO.STATUS_COLOR_AMBER:
                        btnSubmit.setBackgroundColor(getResources().getColor(R.color.amber_800));
                        break;
                    case TaskStatusTypeDTO.STATUS_COLOR_GREEN:
                        btnSubmit.setBackgroundColor(getResources().getColor(R.color.green_800));
                        break;
                }

                Util.expand(actionView,1000, null);


            }
        });
        mRecyclerView.setAdapter(adapter);
    }

    private void setFields() {

        mRecyclerView = (RecyclerView) view.findViewById(R.id.recycler);
        imgCamera = (ImageView)view.findViewById(R.id.TSE_icon);
        iconClose = (ImageView)view.findViewById(R.id.TSE_closeIcon);
        mRecyclerView.addItemDecoration(new HorizontalDividerItemDecoration.Builder(getActivity())
                .color(getResources().getColor(R.color.blue_gray_500))
                .sizeResId(R.dimen.mon_divider)
                .marginResId(R.dimen.mon_padding, R.dimen.mon_padding)
                .build());
        mRecyclerView.setItemAnimator(new SlideInLeftAnimator());
        LinearLayoutManager llm = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(llm);

        mRecyclerView.setItemAnimator(new DefaultItemAnimator());

        txtTaskName = (TextView) view.findViewById(R.id.TSE_taskName);
        txtStatusType = (TextView) view.findViewById(R.id.TSE_statusType);
        txtTime = (TextView) view.findViewById(R.id.TSE_time);
        txtResult = (TextView) view.findViewById(R.id.TSE_result);

        btnSubmit = (Button) view.findViewById(R.id.btnRed);
        btnSubmit.setBackgroundColor(getResources().getColor(R.color.blue_gray_400));
        btnSubmit.setText("Submit Task Status");
        txtStatusType.setText("");
        txtTime.setText("");
        txtResult.setText("");

        iconClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Util.collapse(actionView, 1000, null);
            }
        });
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Util.flashOnce(btnSubmit, 300, new Util.UtilAnimationListener() {
                    @Override
                    public void onAnimationEnded() {
                        AlertDialog.Builder diag = new AlertDialog.Builder(getActivity());
                        diag.setTitle("Task Status Update")
                                .setMessage("You are about to update the status os this task:\n\n"
                                        + projectTask.getTask().getTaskName() + "\n\nDo you want to continue?")
                                .setPositiveButton(getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        processRequest();
                                    }
                                })
                                .setNegativeButton(getResources().getString(R.string.no), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                    }
                                })
                                .show();

                    }
                });
            }
        });

        imgCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Util.flashOnce(imgCamera, 300, new Util.UtilAnimationListener() {
                    @Override
                    public void onAnimationEnded() {
                        mListener.onCameraRequested(projectTask);
                    }
                });
            }
        });
    }

    TaskStatusTypeDTO taskStatusType;

    private TextView txtTime;
    static final SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
    private void processRequest() {

        btnSubmit.setEnabled(false);
        ProjectTaskStatusDTO projectTaskStatus = new ProjectTaskStatusDTO();
        MonitorDTO m = SharedUtil.getMonitor(getActivity());
        if (m != null) {
            projectTaskStatus.setMonitorID(m.getMonitorID());
        } else {
            StaffDTO s = SharedUtil.getCompanyStaff(getActivity());
            if (s != null) {
               projectTaskStatus.setStaffID(s.getStaffID());
            }
        }

        projectTaskStatus.setStatusDate(new Date().getTime());

        ProjectTaskDTO pt = new ProjectTaskDTO();
        pt.setProjectID(this.projectTask.getProjectID());
        pt.setProjectTaskID(this.projectTask.getProjectTaskID());

        TaskStatusTypeDTO r = new TaskStatusTypeDTO();
        r.setTaskStatusTypeID(taskStatusType.getTaskStatusTypeID());
        projectTaskStatus.setTaskStatusType(r);
        projectTaskStatus.setStatusDate(new Date().getTime());
        projectTaskStatus.setProjectTask(pt);
        //
        final RequestDTO request = new RequestDTO(RequestDTO.ADD_PROJECT_TASK_STATUS);
        request.setProjectTaskStatus(projectTaskStatus);

        if (WebCheck.checkNetworkAvailability(getActivity(),true).isNetworkUnavailable()) {
            saveRequestInCache(request);
            btnSubmit.setEnabled(true);
            return;
        }

        NetUtil.sendRequest(getActivity(), request, new NetUtil.NetUtilListener() {
            @Override
            public void onResponse(final ResponseDTO response) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        btnSubmit.setEnabled(false);
                        if (response.getProjectTaskStatusList() != null && !response.getProjectTaskStatusList().isEmpty()) {
                            returnedStatus = response.getProjectTaskStatusList().get(0);
                            mListener.onStatusReturned(returnedStatus);
                            Snackbar.make(mRecyclerView, "The task status update has been sent", Snackbar.LENGTH_LONG).show();
                            txtTime.setText(sdf.format(new Date()));
                            txtResult.setText("Status updated: " + projectTask.getTask().getTaskName());
                            mListener.onCameraRequested(projectTask);
                        }
                    }
                });

            }

            @Override
            public void onError(final String message) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        saveRequestInCache(request);
                        Util.showErrorToast(getActivity(), message);
                    }
                });

            }

            @Override
            public void onWebSocketClose() {

            }
        });


    }

    private ProjectTaskStatusDTO returnedStatus;
    private void saveRequestInCache(RequestDTO request) {
        RequestCacheUtil.addRequest(getActivity(), request, new RequestCacheUtil.RequestCacheListener() {
            @Override
            public void onError(String message) {
                Log.e(LOG,message);
            }

            @Override
            public void onRequestAdded() {
                Snackbar.make(mRecyclerView,"The task status update has been saved",Snackbar.LENGTH_LONG).show();
                mListener.onCameraRequested(projectTask);
            }

            @Override
            public void onRequestsRetrieved(RequestList requestList) {

            }
        });
    }
    TaskStatusUpdateListener mListener;
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        Log.d(LOG, "++ onAttach");
        try {
            mListener = (TaskStatusUpdateListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement TaskStatusUpdateListener");
        }
    }

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
}

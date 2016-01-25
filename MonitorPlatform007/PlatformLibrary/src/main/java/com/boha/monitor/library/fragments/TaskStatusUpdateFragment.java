package com.boha.monitor.library.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.RequiresPermission;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.boha.monitor.library.adapters.TaskStatusTypeAdapter;
import com.boha.monitor.library.dto.MonitorDTO;
import com.boha.monitor.library.dto.PhotoUploadDTO;
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
import com.boha.monitor.library.util.SpacesItemDecoration;
import com.boha.monitor.library.util.Util;
import com.boha.monitor.library.util.WebCheck;
import com.boha.platform.library.R;
import com.squareup.picasso.Picasso;
import com.yqritc.recyclerviewflexibledivider.HorizontalDividerItemDecoration;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import jp.wasabeef.recyclerview.animators.SlideInLeftAnimator;


public class TaskStatusUpdateFragment extends Fragment implements PageFragment {

    public interface TaskStatusUpdateListener {
        void onStatusCameraRequested(ProjectTaskDTO projectTask, ProjectTaskStatusDTO projectTaskStatus);

        void onProjectTaskCameraRequested(ProjectTaskDTO projectTask);

        void onStatusComplete(ProjectTaskDTO projectTask, ProjectTaskStatusDTO projectTaskStatus);

        void onCancelStatusUpdate(ProjectTaskDTO projectTask);

        void setBusy(boolean busy);
    }

    private View view, actionView;
    private TextView txtTaskName, txtStatusType, txtResult;
    private ProjectTaskDTO projectTask;
    private Button btnSubmit, btnDone;
    private RecyclerView mRecyclerView;
    private ImageView iconClose, hero, iconCancel;
    private FloatingActionButton fab;
    private LinearLayout photoContainer;
    private HorizontalScrollView scrollView;
    List<TaskStatusTypeDTO> taskStatusTypeList;
    LayoutInflater inflater;


    static final String LOG = TaskStatusUpdateFragment.class.getSimpleName();

    public void setProjectTask(ProjectTaskDTO projectTask) {
        this.projectTask = projectTask;
        if (view != null) {
            txtTaskName.setText(projectTask.getTask().getTaskName());
        }

    }

    public static TaskStatusUpdateFragment newInstance(ProjectTaskDTO projectTask,
                                                       int type) {
        TaskStatusUpdateFragment fragment = new TaskStatusUpdateFragment();
        Bundle args = new Bundle();
        args.putSerializable("projectTask", projectTask);
        args.putInt("type", type);
        fragment.setArguments(args);
        return fragment;
    }

    public TaskStatusUpdateFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(LOG, "++ onCreate ......");
        if (getArguments() != null) {
            projectTask = (ProjectTaskDTO) getArguments().getSerializable("projectTask");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.i(LOG, "########## onCreateView");
        view = inflater.inflate(R.layout.fragment_task_status_edit, container, false);
        actionView = view.findViewById(R.id.TSE_actionLayout);
        this.inflater = inflater;

        actionView.setVisibility(View.GONE);
        setFields();
        getStatusTypes();


        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.i(LOG, "####### onResume");


    }


    private void getStatusTypes() {
        Log.d(LOG, "----- getStatusTypes");
        if (SharedUtil.getMonitor(getActivity()) != null) {
            CacheUtil.getCachedMonitorProjects(getActivity(), new CacheUtil.CacheUtilListener() {
                @Override
                public void onFileDataDeserialized(ResponseDTO response) {
                    if (response.getTaskStatusTypeList() != null && !response.getTaskStatusTypeList().isEmpty()) {
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
        if (SharedUtil.getCompanyStaff(getActivity()) != null) {
            CacheUtil.getCachedStaffData(getActivity(), new CacheUtil.CacheUtilListener() {
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
    }

    TaskStatusTypeAdapter adapter;

    private void setList() {
        Log.d(LOG, "+++++++ setList");
        txtTaskName.setText(projectTask.getTask().getTaskName());

        adapter = new TaskStatusTypeAdapter(taskStatusTypeList, darkColor, getActivity(), new TaskStatusTypeAdapter.TaskStatusTypeListener() {
            @Override
            public void onTaskStatusTypeClicked(TaskStatusTypeDTO tst) {
                txtStatusType.setText(tst.getTaskStatusTypeName());
                taskStatusType = tst;
                txtStatusType.setText(taskStatusType.getTaskStatusTypeName());
                switch (taskStatusType.getStatusColor()) {
                    case TaskStatusTypeDTO.STATUS_COLOR_RED:
                        btnSubmit.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.red_800));
                        break;
                    case TaskStatusTypeDTO.STATUS_COLOR_AMBER:
                        btnSubmit.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.amber_800));
                        break;
                    case TaskStatusTypeDTO.STATUS_COLOR_GREEN:
                        btnSubmit.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.green_800));
                        break;
                }

                Util.expand(actionView, 1000, null);


            }
        });

        mRecyclerView.setAdapter(adapter);
    }

    private void setFields() {
        scrollView = (HorizontalScrollView) view.findViewById(R.id.TSE_scroll);
        photoContainer = (LinearLayout) view.findViewById(R.id.TSE_photoContainer);
        scrollView.setVisibility(View.GONE);
        fab = (FloatingActionButton) view.findViewById(R.id.fab);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.recycler);
        iconClose = (ImageView) view.findViewById(R.id.TSE_closeIcon);
        iconCancel = (ImageView) view.findViewById(R.id.TSE_iconCancel);
        hero = (ImageView) view.findViewById(R.id.TSE_backdrop);
        hero.setImageDrawable(Util.getRandomBackgroundImage(getActivity()));

        mRecyclerView.addItemDecoration(new HorizontalDividerItemDecoration.Builder(getActivity())
                .color(ContextCompat.getColor(getActivity(), R.color.grey))
                .sizeResId(R.dimen.mon_padding)
                .marginResId(R.dimen.mon_padding, R.dimen.mon_padding)
                .build());

        mRecyclerView.setItemAnimator(new SlideInLeftAnimator());
        GridLayoutManager glm = new GridLayoutManager(getActivity(), 2, LinearLayoutManager.VERTICAL, false);
        int spacingInPixels = getResources().getDimensionPixelSize(R.dimen.mon_divider);
        mRecyclerView.addItemDecoration(new SpacesItemDecoration(spacingInPixels));
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(glm);

        mRecyclerView.setItemAnimator(new DefaultItemAnimator());

        txtTaskName = (TextView) view.findViewById(R.id.TSE_taskName);
        txtStatusType = (TextView) view.findViewById(R.id.TSE_statusType);
        txtTime = (TextView) view.findViewById(R.id.TSE_time);
        txtResult = (TextView) view.findViewById(R.id.TSE_result);

        btnSubmit = (Button) view.findViewById(R.id.btnRed);
        btnDone = (Button) view.findViewById(R.id.TSE_btnDone);

        btnSubmit.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.blue_gray_400));
        btnSubmit.setText("Submit Status");
        txtStatusType.setText("");
        txtTime.setText("");
        txtResult.setText("");

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.onProjectTaskCameraRequested(projectTask);
            }
        });
        iconCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.onCancelStatusUpdate(projectTask);
            }
        });
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
                        processRequest();
                    }
                });
            }
        });

        scrollView.setVisibility(View.GONE);
        btnDone.setVisibility(View.GONE);
        btnDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cleanup();
            }
        });

    }

    private void cleanup() {
        photoContainer.removeAllViews();
        Util.collapse(actionView, 1000, null);
        btnDone.setVisibility(View.GONE);
        scrollView.setVisibility(View.GONE);
        mListener.onStatusComplete(projectTask, projectTaskStatus);


    }

    public void setListener(TaskStatusUpdateListener mListener) {
        this.mListener = mListener;
    }

    TaskStatusTypeDTO taskStatusType;
    ProjectTaskStatusDTO projectTaskStatus;

    private TextView txtTime;
    static final SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");

    @RequiresPermission
    private void processRequest() {

        btnSubmit.setEnabled(false);
        projectTaskStatus = new ProjectTaskStatusDTO();
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
        projectTaskStatus.setTaskStatusType(taskStatusType);
        projectTaskStatus.setStatusDate(new Date().getTime());
        projectTaskStatus.setDateUpdated(new Date().getTime());
        projectTaskStatus.setProjectTask(pt);
        //
        final RequestDTO request = new RequestDTO(RequestDTO.ADD_PROJECT_TASK_STATUS);
        request.setProjectTaskStatus(projectTaskStatus);

        if (WebCheck.checkNetworkAvailability(getActivity()).isNetworkUnavailable()) {
            saveRequestInCache(request);
            btnSubmit.setEnabled(true);
            return;
        }

        mListener.setBusy(true);
        request.setZipResponse(false);
        NetUtil.sendRequest(getActivity(), request, new NetUtil.NetUtilListener() {
            @Override
            public void onResponse(final ResponseDTO response) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mListener.setBusy(false);
                        btnSubmit.setEnabled(true);
                        if (response.getProjectTaskStatusList() != null
                                && !response.getProjectTaskStatusList().isEmpty()) {
                            returnedStatus = response.getProjectTaskStatusList().get(0);
                            Snackbar.make(mRecyclerView, "The status update has been sent", Snackbar.LENGTH_LONG).show();
                            txtTime.setText(sdf.format(new Date()));
                            txtResult.setText("Status updated: " + projectTask.getTask().getTaskName());

                            Util.collapse(actionView, 1000, null);
                            showCameraDialog();
                        }
                    }
                });

            }

            @Override
            public void onError(final String message) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mListener.setBusy(false);
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


    private void showCameraDialog() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                AlertDialog.Builder diag = new AlertDialog.Builder(getActivity());
                diag.setTitle("Task Status Photo")
                        .setMessage("Do you want to take pictures for this update?\n\n"
                                + projectTask.getTask().getTaskName())
                        .setPositiveButton(getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mListener.onProjectTaskCameraRequested(projectTask);
                            }
                        })
                        .setNegativeButton(getResources().getString(R.string.no), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mListener.onStatusComplete(projectTask,returnedStatus);
                            }
                        })
                        .show();
            }
        });

    }

    private List<PhotoUploadDTO> photoUploadList;

    public void onPictureTaken(boolean isTaken) {
        if (isTaken) {
            Snackbar.make(scrollView,"Photo has been taken and saved",Snackbar.LENGTH_LONG).show();
        } else {
            Util.showErrorToast(getActivity(),"Photo has not been taken. Please try again");
        }
    }
    public void displayPhotos(List<PhotoUploadDTO> list) {
        Log.i(LOG, "## photos Taken: " + list.size());
        if (photoUploadList == null) {
            photoUploadList = new ArrayList<>();
        }
        photoUploadList.addAll(list);
        scrollView.setVisibility(View.VISIBLE);
        btnDone.setVisibility(View.VISIBLE);
        for (PhotoUploadDTO p : photoUploadList) {
            View view = inflater.inflate(R.layout.small_image, null);
            ImageView imageView = (ImageView) view.findViewById(R.id.image);
            File file = new File(p.getThumbFilePath());
            if (file.exists()) {
                Picasso.with(getActivity()).load(file).fit().into(imageView);
                photoContainer.addView(view);
            }
        }
        Util.collapse(actionView, 300, null);
        Util.expand(photoContainer, 1000, null);

    }

    public void onNoPhotoTaken() {
        Log.i(LOG, "## onNoPhotoTaken, photos Taken: 0");
        Util.showErrorToast(getActivity(), "No photos were taken for this update");
        scrollView.setVisibility(View.VISIBLE);
        btnDone.setVisibility(View.VISIBLE);
        Util.collapse(actionView, 300, null);
        Util.collapse(photoContainer, 1000, null);

    }

    private ProjectTaskStatusDTO returnedStatus;

    private void saveRequestInCache(RequestDTO request) {
        RequestCacheUtil.addRequest(getActivity(), request, new RequestCacheUtil.RequestCacheListener() {
            @Override
            public void onError(String message) {
                Log.e(LOG, message);
            }

            @Override
            public void onRequestAdded() {
                Snackbar.make(mRecyclerView, "The task status update has been saved", Snackbar.LENGTH_LONG).show();
                showCameraDialog();
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

    int primaryColor, darkColor;

    @Override
    public void setThemeColors(int primaryColor, int darkColor) {
        this.primaryColor = primaryColor;
        this.darkColor = darkColor;
    }
}

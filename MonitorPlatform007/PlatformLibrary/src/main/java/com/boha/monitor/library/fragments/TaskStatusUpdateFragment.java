package com.boha.monitor.library.fragments;

import android.app.Activity;
import android.content.Intent;
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

import com.boha.monitor.library.activities.MonApp;
import com.boha.monitor.library.adapters.TaskStatusTypeAdapter;
import com.boha.monitor.library.dto.MonitorDTO;
import com.boha.monitor.library.dto.PhotoUploadDTO;
import com.boha.monitor.library.dto.ProjectDTO;
import com.boha.monitor.library.dto.ProjectTaskDTO;
import com.boha.monitor.library.dto.ProjectTaskStatusDTO;
import com.boha.monitor.library.dto.RequestDTO;
import com.boha.monitor.library.dto.ResponseDTO;
import com.boha.monitor.library.dto.StaffDTO;
import com.boha.monitor.library.dto.TaskStatusTypeDTO;
import com.boha.monitor.library.services.RequestIntentService;
import com.boha.monitor.library.util.SharedUtil;
import com.boha.monitor.library.util.Snappy;
import com.boha.monitor.library.util.Util;
import com.boha.platform.library.R;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class TaskStatusUpdateFragment extends Fragment implements PageFragment {

    public interface TaskStatusUpdateListener {
        void onStatusCameraRequested(ProjectTaskDTO projectTask, ProjectTaskStatusDTO projectTaskStatus);

        void onProjectTaskCameraRequested(ProjectTaskDTO projectTask);

        void onStatusComplete(ProjectDTO project);

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

    MonApp monApp;

    public MonApp getMonApp() {
        return monApp;
    }

    public void setMonApp(MonApp monApp) {
        this.monApp = monApp;
    }
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
        args.putInt("editType", type);
        fragment.setArguments(args);
        return fragment;
    }

    public TaskStatusUpdateFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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

        Snappy.getTaskStatusTypeList(monApp, new Snappy.SnappyReadListener() {
            @Override
            public void onDataRead(final ResponseDTO response) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            taskStatusTypeList = response.getTaskStatusTypeList();
                            setList();
                        }
                    });
                }
            }

            @Override
            public void onError(String message) {

            }
        });

    }

    TaskStatusTypeAdapter adapter;

    private void setList() {
//        Log.d(LOG, "+++++++ setList");
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

                Util.expand(actionView, 500, null);


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


        GridLayoutManager glm = new GridLayoutManager(getActivity(), 2, LinearLayoutManager.VERTICAL, false);
        int spacingInPixels = getResources().getDimensionPixelSize(R.dimen.mon_divider);
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
        Util.expand(fab,1000,null);
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
                        btnSubmit.setEnabled(false);
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
        projectTaskStatus.setProjectTaskID(pt.getProjectTaskID());
        //
        final RequestDTO request = new RequestDTO(RequestDTO.ADD_PROJECT_TASK_STATUS);
        request.setProjectTaskStatus(projectTaskStatus);
        request.setRequestDate(new Date().getTime());
        Snappy.cacheRequest(monApp, request, new Snappy.SnappyWriteListener() {
            @Override
            public void onDataWritten() {
                returnedStatus = projectTaskStatus;
                projectTask.getProjectTaskStatusList().add(0, projectTaskStatus);
                updateProjectInCache(projectTaskStatus);
                if (getActivity() != null) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            txtTime.setText(sdf.format(new Date()));
                            txtResult.setText("Status updated: " + projectTask.getTask().getTaskName());

                            Util.collapse(actionView, 1000, null);
                            Util.expand(fab, 1000, null);
                            Intent m = new Intent(getContext(), RequestIntentService.class);
                            getActivity().startService(m);
                        }
                    });

                }
            }

            @Override
            public void onError(String message) {

            }
        });


    }

    private void updateProjectInCache(final ProjectTaskStatusDTO status) {
        status.setDateUpdated(new Date().getTime());
        Snappy.getProject(monApp, projectTask.getProjectID(), new Snappy.SnappyProjectListener() {
            @Override
            public void onProjectFound(final ProjectDTO project) {
                List<ProjectTaskDTO> ptList = new ArrayList<>();
                for (ProjectTaskDTO pt : project.getProjectTaskList()) {
                    if (pt.getProjectTaskID().intValue() == projectTask.getProjectTaskID()) {
                        projectTask.setStatusCount(projectTask.getStatusCount() + 1);
                        projectTask.setLastStatus(status);
                        ptList.add(projectTask);
                    } else {
                        ptList.add(pt);
                    }
                }
                project.setProjectTaskList(ptList);
                project.setStatusCount(project.getStatusCount() + 1);
                project.setLastStatus(status);
                List<ProjectDTO> pList = new ArrayList<>();
                pList.add(project);
                Snappy.writeProjectList(monApp, pList, new Snappy.SnappyWriteListener() {
                    @Override
                    public void onDataWritten() {
                        Log.e(LOG, "Project updated with projectTask that contains new status: " +
                                project.getProjectName() + " - " + projectTask.getTask().getTaskName());
                        if (mListener != null) {
                            mListener.onStatusComplete(project);
                        }

                    }

                    @Override
                    public void onError(String message) {
                        Log.e(LOG,message);
//                        Util.showErrorToast(getActivity(),"Unable to get project from cache");
                    }
                });
            }

            @Override
            public void onError() {

            }
        });
    }

//    private void showCameraDialog(final ProjectDTO p) {
//        AlertDialog.Builder d = new AlertDialog.Builder(getContext());
//
//
//        d.setTitle("Status Camera")
//                .setMessage("Do you want to take pictures for this task:\n"
//                        + projectTask.getTask().getTaskName() + "?")
//                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        mListener.onProjectTaskCameraRequested(projectTask);
//                    }
//                })
//                .setNegativeButton("No", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        mListener.onStatusComplete(p);
//                    }
//                })
//                .show();
//    }

    private List<PhotoUploadDTO> photoUploadList;

    public void onPictureTaken(boolean isTaken) {
        if (isTaken) {
            Snackbar.make(scrollView, "Photo has been taken and saved", Snackbar.LENGTH_LONG).show();
        } else {
            Util.showErrorToast(getActivity(), "Photo has not been taken. Please try again");
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

    TaskStatusUpdateListener mListener;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
//        Log.d(LOG, "++ onAttach");
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

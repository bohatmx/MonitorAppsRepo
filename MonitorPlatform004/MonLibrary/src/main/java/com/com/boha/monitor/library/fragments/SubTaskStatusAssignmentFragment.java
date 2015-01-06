package com.com.boha.monitor.library.fragments;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListPopupWindow;
import android.widget.ListView;
import android.widget.TextView;

import com.boha.monitor.library.R;
import com.com.boha.monitor.library.adapters.SubTaskStatusAdapter;
import com.com.boha.monitor.library.adapters.TaskStatusAdapter;
import com.com.boha.monitor.library.dto.ProjectSiteDTO;
import com.com.boha.monitor.library.dto.ProjectSiteTaskDTO;
import com.com.boha.monitor.library.dto.ProjectSiteTaskStatusDTO;
import com.com.boha.monitor.library.dto.SubTaskDTO;
import com.com.boha.monitor.library.dto.SubTaskStatusDTO;
import com.com.boha.monitor.library.dto.TaskStatusDTO;
import com.com.boha.monitor.library.dto.transfer.RequestDTO;
import com.com.boha.monitor.library.dto.transfer.ResponseDTO;
import com.com.boha.monitor.library.services.RequestCache;
import com.com.boha.monitor.library.util.CacheUtil;
import com.com.boha.monitor.library.util.ErrorUtil;
import com.com.boha.monitor.library.util.RequestCacheUtil;
import com.com.boha.monitor.library.util.SharedUtil;
import com.com.boha.monitor.library.util.Statics;
import com.com.boha.monitor.library.util.Util;
import com.com.boha.monitor.library.util.WebCheck;
import com.com.boha.monitor.library.util.WebCheckResult;
import com.com.boha.monitor.library.util.WebSocketUtil;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Fragment that manages the SubTask status updates
 */
public class SubTaskStatusAssignmentFragment extends Fragment {

    public interface SubTaskStatusAssignmentListener {
        public void onSubTaskStatusCompleted(SubTaskStatusDTO subTaskStatus);
        public void onMainStatusCompleted(ProjectSiteTaskStatusDTO status);
    }
    private SubTaskStatusAssignmentListener listener;
    public SubTaskStatusAssignmentFragment() {
        // Required empty public constructor
    }

    LayoutInflater inflater;
    Context ctx;
    View view;
    TextView txtTaskName, txtCount, txtTitle,
    traffCount, traffRed,traffGreen,traffYellow;
    ListView mListView;
    View handle;
    ProjectSiteTaskDTO task;
    SubTaskStatusAdapter adapter;
    List<SubTaskDTO> subTaskList;
    SubTaskDTO subTask;
    int type;
    static final int TASK = 1, SUBTASK = 2;
    static final String LOG = SubTaskStatusAssignmentFragment.class.getSimpleName();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.w(LOG, "########## onCreateView");
        this.inflater = inflater;
        view = inflater.inflate(R.layout.fragment_assign_site_subtasks, container, false);
        ctx = getActivity();
        txtTaskName = (TextView) view.findViewById(R.id.SUBSTAT_taskName);
        txtCount = (TextView) view.findViewById(R.id.SUBSTAT_number);
        txtTitle = (TextView) view.findViewById(R.id.SUBSTAT_title2);

        traffCount = (TextView) view.findViewById(R.id.TRAFF_count);
        traffGreen = (TextView) view.findViewById(R.id.TRAFF_green);
        traffYellow = (TextView) view.findViewById(R.id.TRAFF_yellow);
        traffRed = (TextView) view.findViewById(R.id.TRAFF_red);


        Statics.setRobotoFontLight(ctx, txtTaskName);
        Statics.setRobotoFontLight(ctx, txtTitle);

        mListView = (ListView) view.findViewById(R.id.SUBSTAT_list);
        handle = view.findViewById(R.id.SUBSTAT_handle);
        txtTaskName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                type = TASK;
                showPopup();
            }
        });
        txtCount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                type = TASK;
                showPopup();
            }
        });
        getCachedData();
        return view;
    }

    public void processMainStatus() {
        type = TASK;
        showPopup();
    }
    ProjectSiteDTO projectSite;

    public void setProjectSiteTask(ProjectSiteDTO site, ProjectSiteTaskDTO task) {
        this.task = task;
        this.projectSite = site;
        subTaskList = new ArrayList<>();
        for (ProjectSiteTaskDTO t : site.getProjectSiteTaskList()) {
            if (task.getTask().getTaskID().intValue() == t.getTask().getTaskID()) {
                subTaskList.addAll(t.getTask().getSubTaskList());
            }
        }
        txtTaskName.setText(task.getTask().getTaskName());

        setList();
    }

    List<SubTaskStatusDTO> subTaskStatusList;

    private void setList() {
        //subTaskStatusList = projectSiteTaskStatus.getSubTaskStatusList();
        if (subTaskStatusList == null) {
            subTaskStatusList = new ArrayList<>();
        }
        //build out list
        for (SubTaskDTO subTask : subTaskList) {
            boolean found = false;
            for (SubTaskStatusDTO subStatus : subTaskStatusList) {
                if (subTask.getSubTaskID().intValue() == subStatus.getSubTaskID().intValue()) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                SubTaskStatusDTO ss = new SubTaskStatusDTO();
                ss.setSubTaskID(subTask.getSubTaskID());
                ss.setSubTaskName(subTask.getSubTaskName());
//                ss.setProjectSiteTaskStatusID(projectSiteTaskStatus.getProjectSiteTaskStatusID());
                subTaskStatusList.add(ss);
            }

        }
        View v = inflater.inflate(R.layout.hero_image, null);
        if (mListView.getHeaderViewsCount() == 0) {
            ImageView image = (ImageView) v.findViewById(R.id.HERO_image);
            TextView caption = (TextView) v.findViewById(R.id.HERO_caption);
            image.setImageDrawable(Util.getRandomHeroImage(ctx));
            caption.setText(ctx.getString(R.string.subtask_status));
            mListView.addHeaderView(v);
        }
        adapter = new SubTaskStatusAdapter(ctx, R.layout.subtask_status_item, subTaskStatusList);
        mListView.setAdapter(adapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                lastIndex = position - 1;
                subTask = subTaskList.get(position - 1);
                type = SUBTASK;
                showPopup();
            }
        });
        mListView.setSelection(lastIndex);
    }

    int lastIndex;

    private void showPopup() {
        View v = null;
        actionsWindow = new ListPopupWindow(getActivity());
        switch (type) {
            case TASK:
                actionsWindow.setPromptView(v = Util.getHeroView(ctx, task.getTask().getTaskName()));
                actionsWindow.setAdapter(new TaskStatusAdapter(ctx,
                        R.layout.task_status_item_small, taskStatusList, true));
                break;
            case SUBTASK:
                actionsWindow.setPromptView(Util.getHeroView(ctx, subTask.getSubTaskName()));
                actionsWindow.setAdapter(new TaskStatusAdapter(ctx,
                        R.layout.task_status_item_small, taskStatusList, false));
                break;
        }
        actionsWindow.setPromptPosition(ListPopupWindow.POSITION_PROMPT_ABOVE);
        actionsWindow.setAnchorView(handle);
        actionsWindow.setWidth(Util.getPopupWidth(getActivity()));
        actionsWindow.setModal(true);
        actionsWindow.setHorizontalOffset(Util.getPopupHorizontalOffset(getActivity()));
        actionsWindow.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                taskStatus = taskStatusList.get(position);
                switch (type) {
                    case TASK:
                        sendTaskStatus();
                        break;
                    case SUBTASK:
                        sendSubTaskStatus();
                        break;
                }
                actionsWindow.dismiss();
            }
        });
        actionsWindow.show();
    }

    SubTaskStatusDTO subTaskStatus;
    int statusCount, greenCount, redCount, yellowCount;

    private void sendTaskStatus() {
        Log.w(LOG, "############## sending taskStatus");
        RequestDTO w = new RequestDTO(RequestDTO.ADD_PROJECT_SITE_TASK_STATUS);
        final ProjectSiteTaskStatusDTO s = new ProjectSiteTaskStatusDTO();
        s.setProjectSiteTaskID(task.getProjectSiteTaskID());
        s.setCompanyStaffID(SharedUtil.getCompanyStaff(ctx).getCompanyStaffID());
        s.setTaskStatus(taskStatus);
        s.setStatusDate(new Date());
        w.setProjectSiteTaskStatus(s);

        WebCheckResult wcr = WebCheck.checkNetworkAvailability(ctx);
        if (!wcr.isWifiConnected()) {
            RequestCacheUtil.addRequest(ctx, w, new CacheUtil.CacheRequestListener() {
                @Override
                public void onDataCached() {
                    switch (s.getTaskStatus().getStatusColor()) {
                        case TaskStatusDTO.STATUS_COLOR_GREEN:
                            txtCount.setBackground(ctx.getResources()
                                    .getDrawable(R.drawable.xgreen_oval));
                            break;
                        case TaskStatusDTO.STATUS_COLOR_YELLOW:
                            txtCount.setBackground(ctx.getResources()
                                    .getDrawable(R.drawable.xorange_oval));
                            break;
                        case TaskStatusDTO.STATUS_COLOR_RED:
                            txtCount.setBackground(ctx.getResources()
                                    .getDrawable(R.drawable.xred_oval));
                            break;
                        default:
                            break;
                    }

                    mListView.setEnabled(true);
                    ObjectAnimator an = ObjectAnimator.ofFloat(mListView, "alpha", 0f, 1f);
                    an.setDuration(1000);
                    an.start();
                }

                @Override
                public void onRequestCacheReturned(RequestCache cache) {

                }

                @Override
                public void onError() {

                }
            });
        } else {
            WebSocketUtil.sendRequest(ctx, Statics.COMPANY_ENDPOINT, w, new WebSocketUtil.WebSocketListener() {
                @Override
                public void onMessage(final ResponseDTO response) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (!ErrorUtil.checkServerError(ctx, response)) {
                                return;
                            }
                            switch (s.getTaskStatus().getStatusColor()) {
                                case TaskStatusDTO.STATUS_COLOR_GREEN:
                                    txtCount.setBackground(ctx.getResources()
                                            .getDrawable(R.drawable.xgreen_oval));
                                    break;
                                case TaskStatusDTO.STATUS_COLOR_YELLOW:
                                    txtCount.setBackground(ctx.getResources()
                                            .getDrawable(R.drawable.xorange_oval));
                                    break;
                                case TaskStatusDTO.STATUS_COLOR_RED:
                                    txtCount.setBackground(ctx.getResources()
                                            .getDrawable(R.drawable.xred_oval));
                                    break;
                                default:
                                    break;
                            }
                            mListView.setEnabled(true);
                            ObjectAnimator an = ObjectAnimator.ofFloat(mListView, "alpha", 0f, 1f);
                            an.setDuration(1000);
                            an.start();
                        }
                    });
                }

                @Override
                public void onClose() {

                }

                @Override
                public void onError(final String message) {
                    Log.e(LOG, "---- ERROR websocket - " + message);
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Util.showErrorToast(ctx, message);
                        }
                    });
                }
            });
        }
        listener.onMainStatusCompleted(s);
    }

    private void sendSubTaskStatus() {
        Log.w(LOG, "##### sending subtaskStatus to cloud");
        RequestDTO w = new RequestDTO(RequestDTO.ADD_SUBTASK_STATUS);
        final SubTaskStatusDTO s = new SubTaskStatusDTO();
        s.setSubTaskID(subTask.getSubTaskID());
        s.setCompanyStaffID(SharedUtil.getCompanyStaff(ctx).getCompanyStaffID());
        s.setTaskStatus(taskStatus);
        s.setProjectSiteTaskID(task.getProjectSiteTaskID());
        w.setSubTaskStatus(s);



        statusCount++;
        traffCount.setText("" + statusCount);
        switch (taskStatus.getStatusColor()) {
            case TaskStatusDTO.STATUS_COLOR_GREEN:
                greenCount++;
                break;
            case TaskStatusDTO.STATUS_COLOR_RED:
                redCount++;
                break;
            case TaskStatusDTO.STATUS_COLOR_YELLOW:
                yellowCount++;
                break;
        }
        traffRed.setText(""+redCount);
        traffGreen.setText("" + greenCount);
        traffYellow.setText("" + yellowCount);

        WebCheckResult wcr = WebCheck.checkNetworkAvailability(ctx);
        if (!wcr.isWifiConnected()) {
            RequestCacheUtil.addRequest(ctx, w, new CacheUtil.CacheRequestListener() {
                @Override
                public void onDataCached() {
                    for (SubTaskStatusDTO ss : subTaskStatusList) {
                        if (s.getSubTaskID().intValue() == ss.getSubTaskID().intValue()) {
                            ss.setTaskStatus(taskStatus);
                            ss.setStatusDate(new Date());
                            ss.setStaffName(SharedUtil.getCompanyStaff(ctx).getFullName());
                            adapter.notifyDataSetChanged();
                            break;
                        }
                    }
                }

                @Override
                public void onRequestCacheReturned(RequestCache cache) {

                }

                @Override
                public void onError() {

                }
            });
        } else {
            WebSocketUtil.sendRequest(ctx, Statics.COMPANY_ENDPOINT, w, new WebSocketUtil.WebSocketListener() {
                @Override
                public void onMessage(final ResponseDTO response) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (!ErrorUtil.checkServerError(ctx, response)) {
                                return;
                            }
                            subTaskStatus = response.getSubTaskStatusList().get(0);
                            for (SubTaskStatusDTO ss : subTaskStatusList) {
                                if (subTaskStatus.getSubTaskID().intValue() == ss.getSubTaskID().intValue()) {
                                    ss.setTaskStatus(subTaskStatus.getTaskStatus());
                                    ss.setStatusDate(subTaskStatus.getStatusDate());
                                    ss.setStaffName(subTaskStatus.getStaffName());
                                    adapter.notifyDataSetChanged();
                                    break;
                                }
                            }

                        }
                    });
                }

                @Override
                public void onClose() {

                }

                @Override
                public void onError(final String message) {
                    Log.e(LOG, "---- ERROR websocket - " + message);
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Util.showErrorToast(ctx, message);
                        }
                    });
                }
            });
        }
        listener.onSubTaskStatusCompleted(s);
    }

    ListPopupWindow actionsWindow;
    List<TaskStatusDTO> taskStatusList;
    TaskStatusDTO taskStatus;

    private void getCachedData() {

        CacheUtil.getCachedData(ctx, CacheUtil.CACHE_DATA, new CacheUtil.CacheUtilListener() {
            @Override
            public void onFileDataDeserialized(ResponseDTO response) {
                if (response != null) {
                    taskStatusList = response.getCompany().getTaskStatusList();

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

    @Override
    public void onAttach(Activity a) {
        super.onAttach(a);
        if (a instanceof SubTaskStatusAssignmentListener) {
            listener = (SubTaskStatusAssignmentListener)a;
        } else {
            throw new UnsupportedOperationException("Host activity " + a.getLocalClassName() + " must implement SubTaskStatusAssignmentListener");
        }
        Log.w(LOG, "Fragment hosted by " + a.getLocalClassName());
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

}

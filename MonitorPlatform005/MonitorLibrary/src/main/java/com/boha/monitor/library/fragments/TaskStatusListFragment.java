package com.boha.monitor.library.fragments;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.TextView;

import com.boha.monitor.library.R;
import com.boha.monitor.library.adapters.TaskStatusAdapter;
import com.boha.monitor.library.dto.ProjectDTO;
import com.boha.monitor.library.dto.TaskStatusDTO;
import com.boha.monitor.library.dto.transfer.RequestDTO;
import com.boha.monitor.library.dto.transfer.ResponseDTO;
import com.boha.monitor.library.util.ErrorUtil;
import com.boha.monitor.library.util.NetUtil;
import com.boha.monitor.library.util.SharedUtil;
import com.boha.monitor.library.util.Util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.boha.monitor.library.util.Util.showErrorToast;

/**
 * A fragment representing a taskStatusList of Items.
 * <project/>
 * Large screen devices (such as tablets) are supported by replacing the ListView
 * with a GridView.
 * <project/>
 * Activities containing this fragment MUST implement the ProjectSiteListListener
 * interface.
 */
public class TaskStatusListFragment extends Fragment implements PageFragment {

    private AbsListView mListView;
    int action;
    static final int ACTION_NEW = 1, ACTION_UPDATE = 2;


    public TaskStatusListFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    Context ctx;
    TextView txtName, txtColor;
    View fab, editLayout, view, heroView;
    EditText editName;
    RadioButton radioRed, radioAmber, radioGreen;
    Button btnSave;
    ImageView iconDelete, fabIcon;
    ProgressBar progressBar;


    @Override
    public View onCreateView( LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
         view = inflater.inflate(R.layout.fragment_task_status_list, container, false);
        ctx = getActivity();
        Bundle b = getArguments();
        if (b != null) {
            ResponseDTO r = (ResponseDTO) b.getSerializable("response");
            taskStatusList = r.getCompany().getTaskStatusList();
        }
        setFields();
        setList();

        return view;
    }


    private void setList() {
        adapter = new TaskStatusAdapter(ctx, R.layout.task_status_item, taskStatusList, true);
        mListView.setAdapter(adapter);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                taskStatus = taskStatusList.get(position);
                action = ACTION_UPDATE;
                iconDelete.setVisibility(View.VISIBLE);
                if (editLayout.getVisibility() == View.GONE) {
                    editName.setText(taskStatus.getTaskStatusName());
                    switch (taskStatus.getStatusColor()) {
                        case TaskStatusDTO.STATUS_COLOR_AMBER:
                            radioAmber.setChecked(true);
                            break;
                        case TaskStatusDTO.STATUS_COLOR_RED:
                            radioRed.setChecked(true);
                            break;
                        case TaskStatusDTO.STATUS_COLOR_GREEN:
                            radioGreen.setChecked(true);
                            break;
                    }
                    Util.expand(editLayout, 500, null);
                } else {
                    Util.collapse(editLayout, 500, null);
                }
            }
        });
    }
    private void setFields() {
        heroView = view.findViewById(R.id.TASK_STAT_top);
        mListView = (AbsListView) view.findViewById(R.id.TASK_STAT_list);
        txtName = (TextView) view.findViewById(R.id.TASK_STAT_title);
        progressBar = (ProgressBar) view.findViewById(R.id.TASK_STAT_progress);
        progressBar.setVisibility(View.GONE);
        txtColor = (TextView) view.findViewById(R.id.EDD_color);
        fab = view.findViewById(R.id.FAB);
        editLayout = view.findViewById(R.id.TASK_STAT_editor);
        iconDelete = (ImageView)view.findViewById(R.id.EDD_imgDelete);
        fabIcon = (ImageView)view.findViewById(R.id.FAB_icon);
        iconDelete.setVisibility(View.GONE);
        editLayout.setVisibility(View.GONE);
        editName = (EditText) view.findViewById(R.id.EDD_edit);
        radioAmber = (RadioButton) view.findViewById(R.id.EDD_radioYellow);
        radioGreen = (RadioButton) view.findViewById(R.id.EDD_radioGreen);
        radioRed = (RadioButton) view.findViewById(R.id.EDD_radioRed);
        btnSave = (Button) view.findViewById(R.id.EDD_btnChange);
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Util.flashOnce(btnSave, 300, new Util.UtilAnimationListener() {
                    @Override
                    public void onAnimationEnded() {
                        sendData();
                    }
                });
            }
        });
        radioAmber.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    txtColor.setText("A");
                    txtColor.setBackground(ContextCompat.getDrawable(ctx, R.drawable.xamber_oval_large));
                    Util.flashOnce(txtColor,200,null);
                }
            }
        });
        radioRed.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    txtColor.setText("R");
                    txtColor.setBackground(ContextCompat.getDrawable(ctx, R.drawable.xred_oval_large));
                    Util.flashOnce(txtColor,200,null);
                }
            }
        });
        radioGreen.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    txtColor.setBackground(ContextCompat.getDrawable(ctx, R.drawable.xgreen_oval_large));
                    txtColor.setText("G");
                    Util.flashOnce(txtColor,200,null);
                }
            }
        });
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Util.flashOnce(fab, 200, new Util.UtilAnimationListener() {
                    @Override
                    public void onAnimationEnded() {
                        action = ACTION_NEW;
                        iconDelete.setVisibility(View.GONE);
                        editName.setText("");
                        radioGreen.setChecked(false);
                        radioAmber.setChecked(false);
                        radioRed.setChecked(false);
                        txtColor.setBackground(ContextCompat.getDrawable(ctx, R.drawable.xgrey_oval));
                        txtColor.setText("X");
                        if (editLayout.getVisibility() == View.GONE) {
                            Util.expand(editLayout, 500, null);
                            fabIcon.setImageDrawable(ContextCompat.getDrawable(ctx, R.drawable.ic_action_overflow));
                        } else {
                            Util.collapse(editLayout, 500, null);
                            fabIcon.setImageDrawable(ContextCompat.getDrawable(ctx, R.drawable.ic_action_new));
                        }
                    }
                });
            }
        });
    }
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    TaskStatusDTO taskStatus;

    private void sendData() {
        RequestDTO w = new RequestDTO();
        if (editName.getText().toString().isEmpty()) {
            Util.showToast(ctx, ctx.getString(R.string.enter_name));
            return;
        }
        Short color = null;
        if (project == null) {
            if (!radioRed.isChecked() && !radioGreen.isChecked() && !radioAmber.isChecked()) {
                Util.showToast(ctx, ctx.getString(R.string.select_status_color));
                return;
            }
            if (radioGreen.isChecked()) color = (short) TaskStatusDTO.STATUS_COLOR_GREEN;
            if (radioAmber.isChecked()) color = (short) TaskStatusDTO.STATUS_COLOR_AMBER;
            if (radioRed.isChecked()) color = (short) TaskStatusDTO.STATUS_COLOR_RED;
        }

        switch (action) {
            case ACTION_NEW:
                taskStatus = new TaskStatusDTO();
                w.setRequestType(RequestDTO.ADD_COMPANY_TASK_STATUS);
                taskStatus.setCompanyID(SharedUtil.getCompany(ctx).getCompanyID());
                taskStatus.setTaskStatusName(editName.getText().toString());
                taskStatus.setStatusColor(color);
                w.setTaskStatus(taskStatus);
                break;
            case ACTION_UPDATE:
                if (taskStatus != null) {
                    w.setRequestType(RequestDTO.UPDATE_COMPANY_TASK_STATUS);
                    taskStatus.setStatusColor(color);
                    taskStatus.setTaskStatusName(editName.getText().toString());
                    w.setTaskStatus(taskStatus);
                }

                break;
        }

        progressBar.setVisibility(View.VISIBLE);
        NetUtil.sendRequest(ctx, w, new NetUtil.NetUtilListener() {
            @Override
            public void onResponse( final ResponseDTO response) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progressBar.setVisibility(View.GONE);
                            if (!ErrorUtil.checkServerError(ctx, response)) {
                                return;
                            }
                            addTaskStatus(response.getTaskStatusList().get(0));
                            Util.collapse(editLayout,500,null);
                            fabIcon.setImageDrawable(ContextCompat.getDrawable(ctx, R.drawable.ic_action_new));
                        }
                    });
                }
            }

            @Override
            public void onError(final String message) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setVisibility(View.GONE);
                        showErrorToast(ctx, message);

                    }
                });
            }

            @Override
            public void onWebSocketClose() {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setVisibility(View.GONE);
                    }
                });
            }
        });

    }

    public void setEmptyText(CharSequence emptyText) {
        View emptyView = mListView.getEmptyView();

        if (emptyText instanceof TextView) {
            ((TextView) emptyView).setText(emptyText);
        }
    }

    @Override
    public void animateHeroHeight() {
        Util.fadeIn(heroView,500);
        Util.rotateViewWithDelay(getActivity(),fab,500,1000, null);
    }

    public void addTaskStatus(TaskStatusDTO taskStatus) {
        if (taskStatusList == null) {
            taskStatusList = new ArrayList<>();
        }
        taskStatusList.add(taskStatus);
        Collections.sort(taskStatusList);
        adapter.notifyDataSetChanged();


    }

    ProjectDTO project;
    List<TaskStatusDTO> taskStatusList;
    TaskStatusAdapter adapter;
}
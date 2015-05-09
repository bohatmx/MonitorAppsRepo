package com.boha.monitor.library.fragments;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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
import com.boha.monitor.library.adapters.ProjectStatusTypeAdapter;
import com.boha.monitor.library.dto.ProjectStatusTypeDTO;
import com.boha.monitor.library.dto.TaskStatusDTO;
import com.boha.monitor.library.dto.transfer.RequestDTO;
import com.boha.monitor.library.dto.transfer.ResponseDTO;
import com.boha.monitor.library.util.ErrorUtil;
import com.boha.monitor.library.util.SharedUtil;
import com.boha.monitor.library.util.Statics;
import com.boha.monitor.library.util.Util;
import com.boha.monitor.library.util.WebSocketUtil;



import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ProjectStatusTypeListFragment extends Fragment implements PageFragment {

    private AbsListView mListView;

    public ProjectStatusTypeListFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    Context ctx;
    TextView txtColor, txtName;
    View fab, view, editLayout, heroView;
    ImageView fabIcon, iconDelete;
    EditText editName;
    int action;
    RadioButton radioAmber, radioGreen, radioRed;
    ProgressBar progressBar;
    Button btnSave;

    static final int ACTION_NEW = 1, ACTION_UPDATE = 2;

    @Override
    public View onCreateView( LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_project_status_type_list, container, false);
        ctx = getActivity();
        Bundle b = getArguments();
        if (b != null) {
            ResponseDTO r = (ResponseDTO) b.getSerializable("response");
            projectStatusTypeList = r.getCompany().getProjectStatusTypeList();
        }

        setFields();
        setList();
        return view;
    }

    private void setList() {
        mListView = (AbsListView) view.findViewById(R.id.FTST_list);
        adapter = new ProjectStatusTypeAdapter(ctx, R.layout.project_status_type_item, projectStatusTypeList);
        mListView.setAdapter(adapter);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                projectStatusType = projectStatusTypeList.get(position);
                action = ACTION_UPDATE;
                iconDelete.setVisibility(View.VISIBLE);
                if (editLayout.getVisibility() == View.GONE) {
                    editName.setText(projectStatusType.getProjectStatusName());
                    switch (projectStatusType.getStatusColor()) {
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
        heroView = view.findViewById(R.id.FTST_top);
        mListView = (AbsListView) view.findViewById(R.id.FTST_list);
        txtName = (TextView) view.findViewById(R.id.FTST_title);
        progressBar = (ProgressBar) view.findViewById(R.id.FTST_progress);
        progressBar.setVisibility(View.GONE);
        txtColor = (TextView) view.findViewById(R.id.EDD_color);
        fab = view.findViewById(R.id.FAB);
        editLayout = view.findViewById(R.id.FTST_layoutEditor);
        iconDelete = (ImageView) view.findViewById(R.id.EDD_imgDelete);
        fabIcon = (ImageView) view.findViewById(R.id.FAB_icon);
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
                    txtColor.setBackground(ctx.getResources().getDrawable(R.drawable.xamber_oval_large));
                    Util.flashOnce(txtColor, 200, null);
                }
            }
        });
        radioRed.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    txtColor.setText("R");
                    txtColor.setBackground(ctx.getResources().getDrawable(R.drawable.xred_oval_large));
                    Util.flashOnce(txtColor, 200, null);
                }
            }
        });
        radioGreen.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    txtColor.setBackground(ctx.getResources().getDrawable(R.drawable.xgreen_oval_large));
                    txtColor.setText("G");
                    Util.flashOnce(txtColor, 200, null);
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
                        txtColor.setBackground(ctx.getResources().getDrawable(R.drawable.xgrey_oval));
                        txtColor.setText("X");
                        if (editLayout.getVisibility() == View.GONE) {
                            Util.expand(editLayout, 500, null);
                            fabIcon.setImageDrawable(ctx.getResources().getDrawable(R.drawable.ic_action_overflow));
                        } else {
                            Util.collapse(editLayout, 500, null);
                            fabIcon.setImageDrawable(ctx.getResources().getDrawable(R.drawable.ic_action_new));
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



    ProjectStatusTypeDTO projectStatusType;

    public void setEmptyText(CharSequence emptyText) {
        View emptyView = mListView.getEmptyView();

        if (emptyText instanceof TextView) {
            ((TextView) emptyView).setText(emptyText);
        }
    }

    @Override
    public void animateHeroHeight() {
        Util.fadeIn(heroView);
        Util.rotateViewWithDelay(getActivity(),fab,500,1000, null);
    }

    public void addProjectStatusType(ProjectStatusTypeDTO status) {
        if (projectStatusTypeList == null) {
            projectStatusTypeList = new ArrayList<>();
        }
        projectStatusTypeList.add(status);
        Collections.sort(projectStatusTypeList);
        adapter.notifyDataSetChanged();


    }

    private void sendData() {
        RequestDTO w = new RequestDTO();
        if (editName.getText().toString().isEmpty()) {
            Util.showToast(ctx, ctx.getString(R.string.enter_name));
            return;
        }
        Short color = null;
        if (!radioRed.isChecked() && !radioGreen.isChecked() && !radioAmber.isChecked()) {
            Util.showToast(ctx, ctx.getString(R.string.select_status_color));
            return;
        }
        if (radioGreen.isChecked()) color = (short) TaskStatusDTO.STATUS_COLOR_GREEN;
        if (radioAmber.isChecked()) color = (short) TaskStatusDTO.STATUS_COLOR_AMBER;
        if (radioRed.isChecked()) color = (short) TaskStatusDTO.STATUS_COLOR_RED;


        switch (action) {
            case ACTION_NEW:
                projectStatusType = new ProjectStatusTypeDTO();
                w.setRequestType(RequestDTO.ADD_COMPANY_PROJECT_STATUS_TYPE);
                projectStatusType.setCompanyID(SharedUtil.getCompany(ctx).getCompanyID());
                projectStatusType.setProjectStatusName(editName.getText().toString());
                projectStatusType.setStatusColor(color);
                w.setProjectStatusType(projectStatusType);
                break;
            case ACTION_UPDATE:
                if (projectStatusType != null) {
                    w.setRequestType(RequestDTO.UPDATE_COMPANY_PROJECT_STATUS_TYPE);
                    projectStatusType.setStatusColor(color);
                    projectStatusType.setProjectStatusName(editName.getText().toString());
                    w.setProjectStatusType(projectStatusType);
                }

                break;
        }

        progressBar.setVisibility(View.VISIBLE);
        WebSocketUtil.sendRequest(ctx, Statics.COMPANY_ENDPOINT, w, new WebSocketUtil.WebSocketListener() {
            @Override
            public void onMessage( final ResponseDTO response) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progressBar.setVisibility(View.GONE);
                            if (!ErrorUtil.checkServerError(ctx, response)) {
                                return;
                            }
                            addProjectStatusType(response.getProjectStatusTypeList().get(0));
                            Util.collapse(editLayout, 500, null);
                            fabIcon.setImageDrawable(ctx.getResources().getDrawable(R.drawable.ic_action_new));
                        }
                    });
                }

            }

            @Override
            public void onClose() {

            }

            @Override
            public void onError(final String message) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setVisibility(View.GONE);
                        Util.showErrorToast(ctx, message);
                    }
                });
            }
        });

    }


    List<ProjectStatusTypeDTO> projectStatusTypeList;
    ProjectStatusTypeAdapter adapter;
}

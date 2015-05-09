package com.boha.monitor.library.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.boha.monitor.library.R;
import com.boha.monitor.library.adapters.TaskAdapter;
import com.boha.monitor.library.dto.CompanyDTO;
import com.boha.monitor.library.dto.ProjectDTO;
import com.boha.monitor.library.dto.TaskDTO;
import com.boha.monitor.library.dto.transfer.RequestDTO;
import com.boha.monitor.library.dto.transfer.ResponseDTO;
import com.boha.monitor.library.util.CacheUtil;
import com.boha.monitor.library.util.ErrorUtil;
import com.boha.monitor.library.util.NetUtil;
import com.boha.monitor.library.util.SharedUtil;
import com.boha.monitor.library.util.Util;




import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A fragment representing a taskStatusList of Items.
 * <project/>
 * Large screen devices (such as tablets) are supported by replacing the ListView
 * with a GridView.
 * <project/>
 * Activities containing this fragment MUST implement the ProjectSiteListListener
 * interface.
 */
public class TaskListFragment extends Fragment implements PageFragment {

    private AbsListView mListView;
    private ListAdapter mAdapter;

    public TaskListFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    Context ctx;
    TextView txtName;
    EditText editTaskName;
    Button btnSave;
    EditText sequence;
    View view, topView, fab;
    View editLayout;
    List<String> list;
    TextView txtTitle;
    ImageView fabIcon;


    @Override
    public View onCreateView( LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_task_list, container, false);
        ctx = getActivity();
        fab = view.findViewById(R.id.FAB);
        setFields();
        Bundle bundle = getArguments();
        if (bundle != null) {
            ResponseDTO r = (ResponseDTO)bundle.getSerializable("response");
            taskList = r.getCompany().getTaskList();
            setList();
        } else {
            getTaskData();
        }
        return view;
    }


    public void openEditPanel() {
        Util.expand(editLayout, 500,null);
        fabIcon.setImageDrawable(ctx.getResources()
                .getDrawable(R.drawable.ic_action_overflow));
    }

    public void closeEditPanel() {
        Util.collapse(editLayout,500,null);
        fabIcon.setImageDrawable(ctx.getResources()
                .getDrawable(R.drawable.ic_action_new));
    }

    int action;

    private void setFields() {
        topView = view.findViewById(R.id.FTL_top);
        editLayout = view.findViewById(R.id.FTL_editLayout);
        txtTitle = (TextView) view.findViewById(R.id.FTL_title);
        editTaskName = (EditText) view.findViewById(R.id.TE_editTaskName);
        sequence = (EditText) view.findViewById(R.id.TE_sequence);
        btnSave = (Button) view.findViewById(R.id.TE_btnSave);
        fabIcon = (ImageView) view.findViewById(R.id.FAB_icon);
        editLayout.setVisibility(View.GONE);
//        Statics.setRobotoFontLight(ctx, txtTitle);

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Util.flashOnce(btnSave, 100, new Util.UtilAnimationListener() {
                    @Override
                    public void onAnimationEnded() {
                        switch (action) {
                            case TaskDTO.ACTION_ADD:
                                registerTask();
                                break;
                            case TaskDTO.ACTION_UPDATE:
                                updateTask();
                                break;
                            default:
                                action = TaskDTO.ACTION_ADD;
                                registerTask();
                                break;
                        }
                    }
                });

            }
        });
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Util.flashOnce(fab, 300, new Util.UtilAnimationListener() {
                    @Override
                    public void onAnimationEnded() {
                        if (editLayout.getVisibility() == View.GONE) {
                            openEditPanel();
                            fabIcon.setImageDrawable(ctx.getResources().getDrawable(R.drawable.ic_action_overflow));
                        } else {
                            closeEditPanel();
                            fabIcon.setImageDrawable(ctx.getResources().getDrawable(R.drawable.ic_action_new));
                        }
                    }
                });
            }
        });
    }

    private void registerTask() {
        Log.w(LOG, "## registerTask");
        task = new TaskDTO();
        CompanyDTO c = new CompanyDTO();
        c.setCompanyID(SharedUtil.getCompany(ctx).getCompanyID());
        task.setCompanyID(c.getCompanyID());
        if (editTaskName.getText().toString().isEmpty()) {
            Util.showToast(ctx, ctx.getResources().getString(R.string.enter_task_name));
            return;
        }

        if (sequence.getText().toString().isEmpty()) {
            Util.showToast(ctx, ctx.getString(R.string.enter_task_number));
            return;
        }

        task.setTaskName(editTaskName.getText().toString());
        task.setTaskNumber(Integer.parseInt(sequence.getText().toString()));

        RequestDTO w = new RequestDTO();
        w.setRequestType(RequestDTO.ADD_COMPANY_TASK);
        w.setTask(task);

        NetUtil.sendRequest(ctx,w,new NetUtil.NetUtilListener() {
            @Override
            public void onResponse( final ResponseDTO response) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (!ErrorUtil.checkServerError(ctx, response)) {
                            return;
                        }
                        task = response.getTaskList().get(0);
                        taskList.add(0, task);
                        adapter.notifyDataSetChanged();
                        closeEditPanel();
                    }
                });
            }

            @Override
            public void onError(final String message) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Util.showErrorToast(ctx,message);
                    }
                });
            }

            @Override
            public void onWebSocketClose() {

            }
        });


    }

    private void updateTask() {
        if (editTaskName.getText().toString().isEmpty()) {
            Util.showToast(ctx, ctx.getString(R.string.enter_taskname));
            return;
        }
        TaskDTO t = new TaskDTO();
        t.setTaskID(task.getTaskID());
        t.setTaskName(editTaskName.getText().toString());
        t.setTaskNumber(Integer.parseInt(sequence.getText().toString()));

        RequestDTO w = new RequestDTO(RequestDTO.UPDATE_COMPANY_TASK);
        w.setTask(t);

        NetUtil.sendRequest(ctx,w,new NetUtil.NetUtilListener() {
            @Override
            public void onResponse(ResponseDTO response) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        adapter.notifyDataSetChanged();
                        closeEditPanel();
                    }
                });
            }

            @Override
            public void onError(final String message) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Util.showErrorToast(ctx,message);
                    }
                });
            }

            @Override
            public void onWebSocketClose() {

            }
        });
    }

    private void deleteTask() {

    }

    private void setList() {
        mListView = (AbsListView) view.findViewById(R.id.FTL_list);
        adapter = new TaskAdapter(ctx, R.layout.task_list_item, taskList);
        mListView.setAdapter(adapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                task = taskList.get(position);
                action = TaskDTO.ACTION_UPDATE;
                editTaskName.setText(task.getTaskName());

                if (task.getTaskNumber() != null) {
                    sequence.setText("" + task.getTaskNumber());
                } else {
                    sequence.setText("0");
                }
                openEditPanel();

            }
        });
        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Log.e(LOG, "***** onItemLongClick position: " + position);
                AlertDialog.Builder b = new AlertDialog.Builder(ctx);
                b.setTitle("Move task")
                        .setMessage("Move this task up or down")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        })
                        .show();
                return true;
            }
        });

    }

    private void getTaskData() {
        CacheUtil.getCachedData(ctx, CacheUtil.CACHE_DATA, new CacheUtil.CacheUtilListener() {
            @Override
            public void onFileDataDeserialized( ResponseDTO response) {
                if (response != null) {
                    if (response.getCompany() != null) {
                        if (response.getCompany().getTaskList() != null) {
                            taskList = response.getCompany().getTaskList();
                            setList();
                        } else {
                            Log.e(LOG, "######## no company tasks found");
                        }
                    }
                }
                getRemoteData();
            }

            @Override
            public void onDataCached() {

            }

            @Override
            public void onError() {

            }
        });
    }

    private void getRemoteData() {
        //TODO - refresh task data from server
        RequestDTO w = new RequestDTO();
        w.setRequestType(RequestDTO.GET_ALL_PROJECT_IMAGES);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

    }

    @Override
    public void onDetach() {
        super.onDetach();
    }


    TaskDTO task;

    /**
     * The default content for this Fragment has a TextView that is shown when
     * the taskStatusList is empty. If you would like to change the text, call this method
     * to supply the text it should use.
     */
    public void setEmptyText(CharSequence emptyText) {
        View emptyView = mListView.getEmptyView();

        if (emptyText instanceof TextView) {
            ((TextView) emptyView).setText(emptyText);
        }
    }

    @Override
    public void animateHeroHeight() {
        Util.fadeIn(topView);
        Util.rotateViewWithDelay(getActivity(),fab,500,1000, null);
    }

    public void addTask(TaskDTO task) {
        if (taskList == null) {
            taskList = new ArrayList<>();
        }
        taskList.add(task);
        Collections.sort(taskList);
        adapter.notifyDataSetChanged();


    }

    ProjectDTO project;
    List<TaskDTO> taskList;
    TaskAdapter adapter;
    static final String LOG = TaskListFragment.class.getSimpleName();
}

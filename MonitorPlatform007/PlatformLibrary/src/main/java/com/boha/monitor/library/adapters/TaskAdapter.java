package com.boha.monitor.library.adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.boha.monitor.library.dto.TaskDTO;
import com.boha.monitor.library.util.Util;
import com.boha.platform.library.R;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

/**
 * Created by aubreyM on 14/12/17.
 */
public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    public interface TaskListener {
        void onTaskClicked(TaskDTO task);
    }

    private TaskListener mListener;
    private List<TaskDTO> taskList;
    private Context ctx;
    int darkColor;
    boolean isSelectionList;

    public TaskAdapter(List<TaskDTO> taskList, int darkColor,
                       Context context, TaskListener listener) {
        this.taskList = taskList;
        this.ctx = context;
        this.mListener = listener;
        this.darkColor = darkColor;
    }

    public TaskAdapter(List<TaskDTO> taskList, boolean isSelectionList,
                       Context context, TaskListener listener) {
        this.taskList = taskList;
        this.ctx = context;
        this.mListener = listener;
        this.isSelectionList = isSelectionList;
    }


    @Override
    public TaskViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.task_type_item, parent, false);
        return new TaskViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final TaskViewHolder holder, final int position) {

        final TaskDTO p = taskList.get(position);
        holder.txtTaskName.setText(p.getTaskName());
        holder.txtNumber.setText("" + (position + 1));
        holder.chkBox.setVisibility(View.GONE);
        holder.chkBox.setEnabled(false);
        if (isSelectionList) {
            holder.chkBox.setVisibility(View.VISIBLE);
            if (p.isSelected()) {
                holder.txtTaskName.setTextColor(Color.BLACK);
                holder.chkBox.setChecked(true);
            } else {
                holder.txtTaskName.setTextColor(Color.GRAY);
                holder.chkBox.setChecked(false);
            }

        }


        holder.txtTaskName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Util.flashOnce(holder.nameView, 300, new Util.UtilAnimationListener() {
                    @Override
                    public void onAnimationEnded() {
                        if (isSelectionList) {
                            if (p.isSelected()) {
                                holder.chkBox.setChecked(false);
                                p.setSelected(Boolean.FALSE);
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                    holder.taskView.setBackgroundColor(
                                            ctx.getResources().getColor(R.color.white,ctx.getTheme()));
                                } else {
                                    holder.taskView.setBackgroundColor(Color.BLACK);
                                }
                            } else {
                                holder.chkBox.setChecked(true);
                                p.setSelected(Boolean.TRUE);
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                    holder.taskView.setBackgroundColor(
                                            ctx.getResources().getColor(R.color.beige_pale,ctx.getTheme()));
                                } else {
                                    holder.taskView.setBackgroundColor(Color.LTGRAY);
                                }
                            }
                        }

                        mListener.onTaskClicked(p);

                    }
                });
            }
        });


    }

    @Override
    public int getItemCount() {
        return taskList == null ? 0 : taskList.size();
    }

    static final Locale loc = Locale.getDefault();
    static final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", loc);
    static final DecimalFormat df = new DecimalFormat("###,###,###,###");

    public class TaskViewHolder extends RecyclerView.ViewHolder {
        protected TextView txtTaskName, txtNumber;
        protected View nameView, taskView;
        protected CheckBox chkBox;


        public TaskViewHolder(View itemView) {
            super(itemView);
            txtTaskName = (TextView) itemView.findViewById(R.id.TSKT_taskName);
            txtNumber = (TextView) itemView.findViewById(R.id.TSKT_number);
            chkBox = (CheckBox) itemView.findViewById(R.id.TSKT_chkBox);
            taskView = itemView.findViewById(R.id.TSKT_taskCard);
            nameView = itemView.findViewById(R.id.TSKT_top);
        }

    }

    static final String LOG = TaskAdapter.class.getSimpleName();
}

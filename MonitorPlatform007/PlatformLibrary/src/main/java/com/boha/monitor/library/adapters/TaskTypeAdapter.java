package com.boha.monitor.library.adapters;

import android.content.Context;
import android.graphics.PorterDuff;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.boha.monitor.library.dto.TaskTypeDTO;
import com.boha.monitor.library.util.Util;
import com.boha.platform.library.R;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

/**
 * Created by aubreyM on 14/12/17.
 */
public class TaskTypeAdapter extends RecyclerView.Adapter<TaskTypeAdapter.TaskTypeViewHolder> {

    public interface TaskListener {
        void onTaskTypeNameClicked(TaskTypeDTO task);
    }

    private TaskListener mListener;
    private List<TaskTypeDTO> taskTypeList;
    private Context ctx;
    int darkColor;

    public TaskTypeAdapter(List<TaskTypeDTO> taskTypeList, int darkColor,
                           Context context, TaskListener listener) {
        this.taskTypeList = taskTypeList;
        this.ctx = context;
        this.mListener = listener;
        this.darkColor = darkColor;
        Log.e("TaskAdapter","### darkColor: " + darkColor);
    }


    @Override
    public TaskTypeViewHolder onCreateViewHolder( ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.task_type_item_grid, parent, false);
        return new TaskTypeViewHolder(v);
    }

    @Override
    public void onBindViewHolder( final TaskTypeViewHolder holder, final int position) {

        final TaskTypeDTO p = taskTypeList.get(position);
        holder.txtTaskTypeName.setText(p.getTaskTypeName());
        holder.txtNumber.setText("" + (position + 1));
        if (darkColor != 0) {
            holder.txtTaskTypeName.setTextColor(darkColor);
            holder.txtNumber.getBackground().setColorFilter(darkColor, PorterDuff.Mode.SRC_IN);
        }
        holder.nameView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Util.flashOnce(holder.nameView, 300, new Util.UtilAnimationListener() {
                    @Override
                    public void onAnimationEnded() {
                        mListener.onTaskTypeNameClicked(p);
                    }
                });
            }
        });


    }

    @Override
    public int getItemCount() {
        return taskTypeList == null ? 0 : taskTypeList.size();
    }

    static final Locale loc = Locale.getDefault();
    static final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", loc);
    static final DecimalFormat df = new DecimalFormat("###,###,###,###");

    public class TaskTypeViewHolder extends RecyclerView.ViewHolder  {
        protected TextView txtTaskTypeName, txtNumber;
        protected View nameView;


        public TaskTypeViewHolder(View itemView) {
            super(itemView);
            txtTaskTypeName = (TextView) itemView.findViewById(R.id.TSKT_taskTypeName);
            txtNumber = (TextView) itemView.findViewById(R.id.TSKT_number);
            nameView = itemView.findViewById(R.id.TSKT_top);
        }

    }

    static final String LOG = TaskTypeAdapter.class.getSimpleName();
}

package com.boha.monitor.library.adapters;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.boha.monitor.library.dto.TaskStatusTypeDTO;
import com.boha.monitor.library.util.Util;
import com.boha.platform.library.R;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

/**
 * Created by aubreyM on 14/12/17.
 */
public class TaskStatusTypeAdapter extends RecyclerView.Adapter<TaskStatusTypeAdapter.TaskStatusTypeViewHolder> {

    public interface TaskStatusTypeListener {
        void onTaskStatusTypeClicked(TaskStatusTypeDTO task);
    }

    private TaskStatusTypeListener mListener;
    private List<TaskStatusTypeDTO> taskStatusTypeList;
    private Context ctx;
    int darkColor;

    public TaskStatusTypeAdapter(List<TaskStatusTypeDTO> taskStatusTypeList, int darkColor,
                                 Context context, TaskStatusTypeListener listener) {
        this.taskStatusTypeList = taskStatusTypeList;
        this.ctx = context;
        this.mListener = listener;
        this.darkColor = darkColor;
        Log.e("TaskAdapter","### darkColor: " + darkColor);
    }


    @Override
    public TaskStatusTypeViewHolder onCreateViewHolder( ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.task_status_type_item, parent, false);
        return new TaskStatusTypeViewHolder(v);
    }

    @Override
    public void onBindViewHolder( final TaskStatusTypeViewHolder holder, final int position) {

        final TaskStatusTypeDTO p = taskStatusTypeList.get(position);
        holder.txtTaskStatusType.setText(p.getTaskStatusTypeName());
        switch (p.getStatusColor()) {
            case TaskStatusTypeDTO.STATUS_COLOR_RED:
                holder.txtColor.setBackground(ContextCompat.getDrawable(ctx,R.drawable.xred_oval_small));
                holder.txtColor.setText("R");
                break;
            case TaskStatusTypeDTO.STATUS_COLOR_AMBER:
                holder.txtColor.setBackground(ContextCompat.getDrawable(ctx, R.drawable.xamber_oval_small));
                holder.txtColor.setText("A");
                break;
            case TaskStatusTypeDTO.STATUS_COLOR_GREEN:
                holder.txtColor.setBackground(ContextCompat.getDrawable(ctx, R.drawable.xgreen_oval_small));
                holder.txtColor.setText("G");
                break;
        }
        holder.layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Util.flashOnce(holder.txtColor, 300, new Util.UtilAnimationListener() {
                    @Override
                    public void onAnimationEnded() {
                        mListener.onTaskStatusTypeClicked(p);
                    }
                });
            }
        });

    }

    @Override
    public int getItemCount() {
        return taskStatusTypeList == null ? 0 : taskStatusTypeList.size();
    }

    static final Locale loc = Locale.getDefault();
    static final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", loc);
    static final DecimalFormat df = new DecimalFormat("###,###,###,###");

    public class TaskStatusTypeViewHolder extends RecyclerView.ViewHolder  {
        protected TextView txtTaskStatusType, txtColor;
        protected View layout;


        public TaskStatusTypeViewHolder(View itemView) {
            super(itemView);

            txtColor = (TextView) itemView.findViewById(R.id.STI_color);
            txtTaskStatusType = (TextView) itemView.findViewById(R.id.STI_statusType);
            layout =  itemView.findViewById(R.id.STI_layout);

        }

    }

    static final String LOG = TaskStatusTypeAdapter.class.getSimpleName();
}

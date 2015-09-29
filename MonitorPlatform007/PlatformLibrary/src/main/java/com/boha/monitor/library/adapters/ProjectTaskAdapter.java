package com.boha.monitor.library.adapters;

import android.content.Context;
import android.graphics.PorterDuff;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.boha.monitor.library.dto.ProjectTaskDTO;
import com.boha.monitor.library.dto.ProjectTaskStatusDTO;
import com.boha.monitor.library.dto.TaskStatusTypeDTO;
import com.boha.monitor.library.util.Util;
import com.boha.platform.library.R;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by aubreyM on 14/12/17.
 */
public class ProjectTaskAdapter extends RecyclerView.Adapter<ProjectTaskAdapter.TaskViewHolder> {

    public interface TaskListener {
        void onTaskNameClicked(ProjectTaskDTO task, int position);
    }

    private TaskListener mListener;
    private List<ProjectTaskDTO> projectTaskList;
    private Context ctx;
    int darkColor;

    public ProjectTaskAdapter(List<ProjectTaskDTO> projectTaskList, int darkColor,
                              Context context, TaskListener listener) {
        this.projectTaskList = projectTaskList;
        this.ctx = context;
        this.mListener = listener;
        this.darkColor = darkColor;
        Log.e("TaskAdapter", "### darkColor: " + darkColor);
    }


    @Override
    public TaskViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.task_item, parent, false);
        return new TaskViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final TaskViewHolder holder, final int position) {

        final ProjectTaskDTO p = projectTaskList.get(position);

        holder.txtStatusColor.setBackground(ContextCompat.getDrawable(ctx,R.drawable.xgrey_oval_small));
        holder.txtStatusColor.setHeight(20);
        holder.txtStatusColor.setWidth(20);

        if (p.getProjectTaskStatusList() != null && !p.getProjectTaskStatusList().isEmpty()) {
            if (p.getProjectTaskStatusList().get(0) == null) {
                Log.e("TaskAdapter", "--- p.getProjectTaskStatusList().get(0) is NULL");
            } else {
                if (p.getProjectTaskStatusList().get(0).getDateUpdated() == null) {
                    p.getProjectTaskStatusList().get(0).setDateUpdated(new Date().getTime());
                }
                holder.txtLastDate.setText(sdf.format(new Date(p.getProjectTaskStatusList().get(0).getDateUpdated())));
                ProjectTaskStatusDTO m = p.getProjectTaskStatusList().get(0);
                holder.txtStatusName.setText(m.getTaskStatusType().getTaskStatusTypeName());
                switch (m.getTaskStatusType().getStatusColor()) {
                    case TaskStatusTypeDTO.STATUS_COLOR_RED:
                        holder.txtStatusColor.setBackground(ContextCompat.getDrawable(ctx,R.drawable.xred_oval_small));
                        break;
                    case TaskStatusTypeDTO.STATUS_COLOR_AMBER:
                        holder.txtStatusColor.setBackground(ContextCompat.getDrawable(ctx,R.drawable.xamber_oval_small));
                        break;
                    case TaskStatusTypeDTO.STATUS_COLOR_GREEN:
                        holder.txtStatusColor.setBackground(ContextCompat.getDrawable(ctx,R.drawable.xgreen_oval_small));
                        break;
                }
            }
            holder.txtStatusCount.setText(df.format(p.getProjectTaskStatusList().size()));
        } else {
            holder.txtLastDate.setText("No Status Date");
            holder.txtStatusName.setText("No Status");
            holder.txtStatusCount.setText("0");
        }
        holder.txtTaskName.setText(p.getTask().getTaskName());
        if (p.getPhotoUploadList() != null && !p.getPhotoUploadList().isEmpty()) {
            holder.txtPhotos.setText(df.format(p.getPhotoUploadList().size()));
        } else {
            holder.txtPhotos.setText("0");
        }

        holder.image.setColorFilter(darkColor, PorterDuff.Mode.SRC_IN);


        holder.nameView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Util.flashOnce(holder.nameView, 300, new Util.UtilAnimationListener() {
                    @Override
                    public void onAnimationEnded() {
                        mListener.onTaskNameClicked(p, position);
                    }
                });
            }
        });

    }

    @Override
    public int getItemCount() {
        return projectTaskList == null ? 0 : projectTaskList.size();
    }

    static final Locale loc = Locale.getDefault();
    static final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", loc);
    static final DecimalFormat df = new DecimalFormat("###,###,###,###");

    public class TaskViewHolder extends RecyclerView.ViewHolder {
        protected ImageView image;
        protected CardView card;
        protected TextView txtTaskName, txtStatusCount, txtStatusColor,
                txtStatusName,txtLastDate, txtPhotos;
        protected View nameView;


        public TaskViewHolder(View itemView) {
            super(itemView);
            card = (CardView) itemView.findViewById(R.id.card);
            image = (ImageView) itemView.findViewById(R.id.TSK_icon);
            txtTaskName = (TextView) itemView.findViewById(R.id.TSK_taskName);
            txtPhotos = (TextView) itemView.findViewById(R.id.TSK_photoCount);
            txtStatusCount = (TextView) itemView.findViewById(R.id.TSK_statusCount);
            txtStatusColor = (TextView) itemView.findViewById(R.id.TSK_statusColor);
            txtStatusName = (TextView) itemView.findViewById(R.id.TSK_label3a);
            txtLastDate = (TextView) itemView.findViewById(R.id.TSK_date);
            nameView = itemView.findViewById(R.id.TSK_top);
        }

    }

    static final String LOG = ProjectTaskAdapter.class.getSimpleName();
}

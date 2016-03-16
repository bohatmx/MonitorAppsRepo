package com.boha.monitor.library.adapters;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.boha.monitor.library.dto.ProjectTaskStatusDTO;
import com.boha.monitor.library.dto.TaskStatusTypeDTO;
import com.boha.monitor.library.util.Statics;
import com.boha.platform.library.R;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

/**
 * Created by aubreyM on 14/12/17.
 */
public class StatusAdapter extends RecyclerView.Adapter<StatusAdapter.StatusViewHolder> {


    private List<ProjectTaskStatusDTO> statusList;
    private Context ctx;
    public static final int THUMB = 1, FULL_IMAGE = 2;

    public StatusAdapter(List<ProjectTaskStatusDTO> statusList,
                         Context context) {
        this.statusList = statusList;
        this.ctx = context;
    }


    @Override
    public StatusViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new StatusViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.status_report_card, parent, false));
    }

    @Override
    public void onBindViewHolder(final StatusViewHolder holder, final int position) {

        final ProjectTaskStatusDTO p = statusList.get(position);
        holder.txtTaskName.setText(p.getTaskName());

        holder.txtDate.setText(sdf.format(p.getStatusDate()));
        holder.txtStatus.setText(p.getTaskStatusType().getTaskStatusTypeName());
        if (p.getStaffName() != null) {
            holder.txtStaff.setText(p.getStaffName());
        }
        switch (p.getTaskStatusType().getStatusColor()) {
            case TaskStatusTypeDTO.STATUS_COLOR_GREEN:
                holder.txtColor.setBackground(ContextCompat.getDrawable(ctx, R.drawable.xgreen_oval_small));
                holder.txtColor.setText("G");
                break;
            case TaskStatusTypeDTO.STATUS_COLOR_RED:
                holder.txtColor.setBackground(ContextCompat.getDrawable(ctx, R.drawable.xred_oval_small));
                holder.txtColor.setText("R");
                break;
            case TaskStatusTypeDTO.STATUS_COLOR_AMBER:
                holder.txtColor.setBackground(ContextCompat.getDrawable(ctx, R.drawable.xorange_oval_small));
                holder.txtColor.setText("A");
                break;
        }

        Statics.setRobotoFontLight(ctx, holder.txtTaskName);
        Statics.setRobotoFontLight(ctx, holder.txtStaff);

    }


    @Override
    public int getItemCount() {
        return statusList == null ? 0 : statusList.size();
    }

    static final Locale loc = Locale.getDefault();
    static final SimpleDateFormat sdf = new SimpleDateFormat("EEEE, dd MMMM yyyy HH:mm", loc);

    public class StatusViewHolder extends RecyclerView.ViewHolder {
        protected TextView txtTaskName, txtStatus,txtDate,txtColor, txtStaff;
        protected int position;


        public StatusViewHolder(View itemView) {
            super(itemView);

            txtColor = (TextView) itemView.findViewById(R.id.SRC_txtColor);
            txtTaskName = (TextView) itemView.findViewById(R.id.SRC_txtTask);
            txtStaff = (TextView) itemView.findViewById(R.id.SRC_txtStaff);
            txtStatus = (TextView) itemView.findViewById(R.id.SRC_txtStatus);
            txtDate = (TextView) itemView.findViewById(R.id.SRC_txtDate);


        }

    }

    static final String LOG = StatusAdapter.class.getSimpleName();
}

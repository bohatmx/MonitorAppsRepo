package com.boha.monitor.library.adapters;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.boha.monitor.library.R;
import com.boha.monitor.library.dto.ProjectSiteTaskStatusDTO;
import com.boha.monitor.library.dto.TaskStatusDTO;
import com.boha.monitor.library.util.Statics;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class StatusReportAdapter extends ArrayAdapter<ProjectSiteTaskStatusDTO> {


    private final LayoutInflater mInflater;
    private final int mLayoutRes;
    private List<ProjectSiteTaskStatusDTO> mList;
    private Context ctx;
    boolean isProjectSite;

   public StatusReportAdapter( Context context, int textViewResourceId,
                              List<ProjectSiteTaskStatusDTO> list, boolean isProjectSite) {
        super(context, textViewResourceId, list);
        this.mLayoutRes = textViewResourceId;
       this.isProjectSite = isProjectSite;
        mList = list;
        ctx = context;
        this.mInflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }


    View view;


    static class ViewHolderItem {
        TextView txtTaskName, txtStatus,txtDate;
        TextView txtColor, txtStaff;
    }


    @Override
    public View getView(int position,  View convertView, ViewGroup parent) {
        final ViewHolderItem item;
        if (convertView == null) {
            convertView = mInflater.inflate(mLayoutRes, null);
            item = new ViewHolderItem();
            item.txtTaskName = (TextView) convertView
                    .findViewById(R.id.SRC_txtTask);

            item.txtStatus = (TextView) convertView
                    .findViewById(R.id.SRC_txtStatus);
            item.txtDate = (TextView) convertView
                    .findViewById(R.id.SRC_txtDate);
            item.txtColor = (TextView) convertView
                    .findViewById(R.id.SRC_txtColor);

            item.txtStaff = (TextView) convertView
                    .findViewById(R.id.SRC_txtStaff);

            convertView.setTag(item);
        } else {
            item = (ViewHolderItem) convertView.getTag();
        }

        final ProjectSiteTaskStatusDTO p = mList.get(position);
        if (isProjectSite) {
            item.txtTaskName.setText(p.getTask().getTaskName());
        } else {
            item.txtTaskName.setText(p.getProjectSiteName() + " - " + p.getTask().getTaskName());
        }
        item.txtDate.setText(sdf.format(p.getStatusDate()));
        item.txtStatus.setText(p.getTaskStatus().getTaskStatusName());
        if (p.getStaffName() != null) {
            item.txtStaff.setText(p.getStaffName());
        }
        switch (p.getTaskStatus().getStatusColor()) {
            case TaskStatusDTO.STATUS_COLOR_GREEN:
                item.txtColor.setBackground(ContextCompat.getDrawable(ctx, R.drawable.xgreen_oval_small));
                item.txtColor.setText("G");
                break;
            case TaskStatusDTO.STATUS_COLOR_RED:
                item.txtColor.setBackground(ContextCompat.getDrawable(ctx, R.drawable.xred_oval_small));
                item.txtColor.setText("R");
                break;
            case TaskStatusDTO.STATUS_COLOR_AMBER:
                item.txtColor.setBackground(ContextCompat.getDrawable(ctx, R.drawable.xorange_oval_small));
                item.txtColor.setText("A");
                break;
        }

        Statics.setRobotoFontLight(ctx, item.txtTaskName);
        Statics.setRobotoFontLight(ctx, item.txtStaff);

        return (convertView);
    }


    static final Locale x = Locale.getDefault();
    static final SimpleDateFormat sdf = new SimpleDateFormat("EEEE, dd MMMM yyyy HH:mm", x);
    static final DecimalFormat df = new DecimalFormat("###,###,##0.0");
}

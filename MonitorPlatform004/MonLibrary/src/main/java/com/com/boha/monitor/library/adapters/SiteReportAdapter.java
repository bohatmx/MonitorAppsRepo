package com.com.boha.monitor.library.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.boha.monitor.library.R;
import com.com.boha.monitor.library.dto.ProjectSiteTaskDTO;
import com.com.boha.monitor.library.dto.ProjectSiteTaskStatusDTO;
import com.com.boha.monitor.library.dto.TaskStatusDTO;
import com.com.boha.monitor.library.util.Statics;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

/**
 * Created by aubreyM on 14/12/16.
 */
public class SiteReportAdapter extends ArrayAdapter<ProjectSiteTaskDTO> {

    public SiteReportAdapter(Context context, int textViewResourceId,
                             List<ProjectSiteTaskDTO> list) {
        super(context, textViewResourceId, list);
        this.mLayoutRes = textViewResourceId;
        mList = list;
        ctx = context;
        this.mInflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }
    View view;


    static class ViewHolderItem {
        TextView txtLastStatus, txtTask, txtColor, txtCount,
        txtGreen, txtRed, txtYellow, txtDate, txtStaff, txtPhotos;

    }
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final ViewHolderItem item;
        if (convertView == null) {
            item = new ViewHolderItem();
            convertView = mInflater.inflate(mLayoutRes, null);
            item.txtLastStatus = (TextView) convertView
                    .findViewById(R.id.RPT_ITEM_status);
            item.txtTask = (TextView) convertView
                    .findViewById(R.id.RPT_ITEM_task);
            item.txtColor = (TextView) convertView
                    .findViewById(R.id.RPT_ITEM_color);
            item.txtCount = (TextView) convertView
                    .findViewById(R.id.RPT_ITEM_statusCount);
            item.txtGreen = (TextView) convertView
                    .findViewById(R.id.TRAFF_green);
            item.txtRed = (TextView) convertView
                    .findViewById(R.id.TRAFF_red);
            item.txtYellow = (TextView) convertView
                    .findViewById(R.id.TRAFF_yellow);
            item.txtDate = (TextView) convertView
                    .findViewById(R.id.RPT_ITEM_date);
            item.txtStaff = (TextView) convertView
                    .findViewById(R.id.RPT_ITEM_staff);
            item.txtPhotos = (TextView) convertView
                    .findViewById(R.id.RPT_ITEM_photoCount);
            convertView.setTag(item);
        } else {
            item = (ViewHolderItem)convertView.getTag();
        }
        final ProjectSiteTaskDTO p = mList.get(position);
        item.txtTask.setText(p.getTask().getTaskName());
        if (p.getPhotoUploadList() != null) {
            item.txtPhotos.setText("" + p.getPhotoUploadList().size());
        } else {
            item.txtPhotos.setText("0");
        }

        int countRed = 0, countGreen = 0, countYellow = 0;
        for (ProjectSiteTaskStatusDTO ss: p.getProjectSiteTaskStatusList()) {
            int color = ss.getTaskStatus().getStatusColor();
            switch (color) {
                case TaskStatusDTO.STATUS_COLOR_GREEN:
                    countGreen++;
                    break;
                case TaskStatusDTO.STATUS_COLOR_AMBER:
                    countYellow++;
                    break;
                case TaskStatusDTO.STATUS_COLOR_RED:
                    countRed++;
                    break;
            }
        }
        item.txtGreen.setText("" + countGreen);
        item.txtYellow.setText("" + countYellow);
        item.txtRed.setText("" + countRed);
        item.txtCount.setText("" + p.getProjectSiteTaskStatusList().size());

        if (!p.getProjectSiteTaskStatusList().isEmpty()) {
            ProjectSiteTaskStatusDTO status = p.getProjectSiteTaskStatusList().get(0);
            item.txtLastStatus.setText(status.getTaskStatus().getTaskStatusName());
            item.txtDate.setText(sdf.format(status.getStatusDate()));
            item.txtStaff.setText(status.getStaffName());
            int color = status.getTaskStatus().getStatusColor();
            switch (color)  {
                case TaskStatusDTO.STATUS_COLOR_GREEN:
                    item.txtColor.setBackground(ctx.getResources().getDrawable(R.drawable.xgreen_oval));
                    item.txtColor.setText(ctx.getString(R.string.green));
                    break;
                case TaskStatusDTO.STATUS_COLOR_AMBER:
                    item.txtColor.setBackground(ctx.getResources().getDrawable(R.drawable.xorange_oval));
                    item.txtColor.setText(ctx.getString(R.string.yellow));
                    break;
                case TaskStatusDTO.STATUS_COLOR_RED:
                    item.txtColor.setBackground(ctx.getResources().getDrawable(R.drawable.xred_oval));
                    item.txtColor.setText(ctx.getString(R.string.red));
                    break;
            }
        } else {
            item.txtLastStatus.setText(ctx.getString(R.string.not_avail));
            item.txtDate.setText(ctx.getString(R.string.not_avail));
            item.txtColor.setBackground(ctx.getResources().getDrawable(R.drawable.xgrey_oval));
        }
        Statics.setRobotoFontLight(ctx, item.txtDate);
        Statics.setRobotoFontLight(ctx,item.txtLastStatus);
        Statics.setRobotoFontLight(ctx,item.txtTask);
        Statics.setRobotoFontLight(ctx,item.txtGreen);
        Statics.setRobotoFontLight(ctx,item.txtYellow);
        Statics.setRobotoFontLight(ctx,item.txtRed);
        return convertView;
    }

    private final LayoutInflater mInflater;
    private final int mLayoutRes;
    private List<ProjectSiteTaskDTO> mList;
    private Context ctx;
    static final Locale x = Locale.getDefault();
    static final SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy HH:mm", x);
    static final DecimalFormat df = new DecimalFormat("###,###,##0.00");


}

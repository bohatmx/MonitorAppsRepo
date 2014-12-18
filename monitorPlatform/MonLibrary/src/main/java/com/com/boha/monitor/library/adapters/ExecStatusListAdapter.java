package com.com.boha.monitor.library.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.boha.monitor.library.R;
import com.com.boha.monitor.library.dto.ProjectSiteTaskStatusDTO;
import com.com.boha.monitor.library.dto.TaskStatusDTO;
import com.com.boha.monitor.library.util.Statics;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class ExecStatusListAdapter extends ArrayAdapter<ProjectSiteTaskStatusDTO> {


    private final LayoutInflater mInflater;
    private final int mLayoutRes;
    private List<ProjectSiteTaskStatusDTO> mList;
    private Context ctx;
    static final String LOG = ExecStatusListAdapter.class.getSimpleName();

    public ExecStatusListAdapter(Context context, int textViewResourceId,
                                 List<ProjectSiteTaskStatusDTO> list) {
        super(context, textViewResourceId, list);
        this.mLayoutRes = textViewResourceId;
        mList = list;
        ctx = context;
        this.mInflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }


    View view;


    static class ViewHolderItem {
        TextView txtTask, txtStatus, txtDate, txtStaff, txtPhotoCount,
                txtColor, txtSite, txtProject;
        View topLayout;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return getCustomView(position, convertView, parent);
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return getCustomView(position, convertView, parent);
    }

    public View getCustomView(int position, View convertView, ViewGroup parent) {
        ViewHolderItem item;
        if (convertView == null) {
            convertView = mInflater.inflate(mLayoutRes, null);
            item = new ViewHolderItem();
            item.txtTask = (TextView) convertView
                    .findViewById(R.id.QS_task);
            item.topLayout = convertView.findViewById(R.id.QS_topTop);
            item.txtSite = (TextView) convertView
                    .findViewById(R.id.QS_site);
            item.txtProject = (TextView) convertView
                    .findViewById(R.id.QS_projectName);

            item.txtStatus = (TextView) convertView
                    .findViewById(R.id.QS_status);
            item.txtDate = (TextView) convertView
                    .findViewById(R.id.QS_statusDate);
            item.txtColor = (TextView) convertView
                    .findViewById(R.id.QS_color);

            convertView.setTag(item);
        } else {
            item = (ViewHolderItem) convertView.getTag();
        }

        final ProjectSiteTaskStatusDTO p = mList.get(position);
        item.txtTask.setText(p.getTask().getTaskName());
        item.txtStatus.setText(p.getTaskStatus().getTaskStatusName());
        item.txtDate.setText(sdf.format(p.getStatusDate()));
        item.txtSite.setText(p.getProjectSiteName());
        item.txtProject.setText(p.getProjectName());
//
//        if (position == 0) {
//            item.topLayout.setVisibility(View.VISIBLE);
//            item.txtProject.setVisibility(View.VISIBLE);
//        } else {
//            item.topLayout.setVisibility(View.GONE);
//            item.txtProject.setVisibility(View.GONE);
//        }


        int color = p.getTaskStatus().getStatusColor();

        switch (color) {
            case TaskStatusDTO.STATUS_COLOR_GREEN:
                item.txtColor.setBackground(ctx.getResources().getDrawable(R.drawable.xgreen_oval_small));
                break;
            case TaskStatusDTO.STATUS_COLOR_RED:
                item.txtColor.setBackground(ctx.getResources().getDrawable(R.drawable.xred_oval_small));
                break;
            case TaskStatusDTO.STATUS_COLOR_YELLOW:
                item.txtColor.setBackground(ctx.getResources().getDrawable(R.drawable.xorange_oval_small));
                break;
        }
        Statics.setRobotoFontLight(ctx, item.txtTask);
        Statics.setRobotoFontLight(ctx, item.txtStatus);
        Statics.setRobotoFontLight(ctx, item.txtDate);
        return (convertView);
    }

    static final Locale loc = Locale.getDefault();
    static final SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMMM yyyy HH:mm", loc);
}

package com.boha.monitor.library.util;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.boha.monitor.library.R;
import com.boha.monitor.library.dto.ProjectSiteTaskStatusDTO;
import com.boha.monitor.library.dto.TaskStatusDTO;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

/**
 * Created by aubreyM on 15/01/10.
 */
public class StatusWall {

    public View getStatusWall( Context ctx,  List<ProjectSiteTaskStatusDTO> statusList) {
        LayoutInflater i = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = i.inflate(R.layout.fragment_site_status_list,null);
        LinearLayout lay = (LinearLayout) v.findViewById(R.id.WALL_layout);
        lay.removeAllViews();
        for (ProjectSiteTaskStatusDTO status: statusList) {
            TextView txt = (TextView) i.inflate(R.layout.status_text, null);
            switch (status.getTaskStatus().getStatusColor()) {
                case TaskStatusDTO.STATUS_COLOR_AMBER:
                    txt.setBackground(ctx.getResources().getDrawable(R.drawable.xorange_oval_small));
                    break;
                case TaskStatusDTO.STATUS_COLOR_RED:
                    txt.setBackground(ctx.getResources().getDrawable(R.drawable.xred_oval_small));
                    break;
                case TaskStatusDTO.STATUS_COLOR_GREEN:
                    txt.setBackground(ctx.getResources().getDrawable(R.drawable.xgreen_oval_small));
                    break;
            }
            txt.setText(sdf.format(status.getStatusDate()));
            lay.addView(txt);
        }

        return v;
    }

    static final Locale loc = Locale.getDefault();
    static final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM", loc);
}

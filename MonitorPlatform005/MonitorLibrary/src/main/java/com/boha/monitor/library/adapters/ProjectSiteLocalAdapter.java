package com.boha.monitor.library.adapters;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.boha.monitor.library.R;
import com.boha.monitor.library.dto.ProjectSiteDTO;
import com.boha.monitor.library.dto.TaskStatusDTO;
import com.boha.monitor.library.util.WebCheckResult;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class ProjectSiteLocalAdapter extends ArrayAdapter<ProjectSiteDTO> implements SiteAdapterInterface {


    private final LayoutInflater mInflater;
    private final int mLayoutRes;
    private List<ProjectSiteDTO> mList;
    private Context ctx;
    private WebCheckResult wcr;

    public interface ProjectSiteListener {
        public void onProjectSiteClicked(ProjectSiteDTO site, int index);
    }

    ProjectSiteListener listener;

    public ProjectSiteLocalAdapter( Context context, int textViewResourceId,
                                   List<ProjectSiteDTO> list,
                                   WebCheckResult wcr,
                                   ProjectSiteListener listener) {
        super(context, textViewResourceId, list);
        this.mLayoutRes = textViewResourceId;
        mList = list;
        this.wcr = wcr;
        this.listener = listener;
        ctx = context;
        this.mInflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    View view;


    static class ViewHolderItem {
        TextView txtName, txtLastStatus, txtTaskName;
        TextView txtTaskCount, txtStatusCount;
        TextView txtNumber, txtDate, txtBen, txtAccuracy;
        ImageView imgConfirmed;
        View statLayout1, statLayout2;
    }


    @Override
    public View getView(final int position,  View convertView, ViewGroup parent) {
        final ViewHolderItem item;
        if (convertView == null) {
            convertView = mInflater.inflate(mLayoutRes, null);
            item = new ViewHolderItem();

            item.txtName = (TextView) convertView
                    .findViewById(R.id.SITE_txtName);
            item.txtNumber = (TextView) convertView
                    .findViewById(R.id.SITE_number);
            item.txtBen = (TextView) convertView
                    .findViewById(R.id.SITE_txtBeneficiary);
            item.txtTaskCount = (TextView) convertView
                    .findViewById(R.id.SITE_txtPictureCount);
            item.txtTaskName = (TextView) convertView
                    .findViewById(R.id.SITE_lastTask);
            item.statLayout1 = convertView.findViewById(R.id.SITE_bottom);
            item.statLayout2 = convertView.findViewById(R.id.SITE_layoutStatus);
            item.txtStatusCount = (TextView) convertView
                    .findViewById(R.id.SITE_txtStatusCount);
            item.txtDate = (TextView) convertView
                    .findViewById(R.id.SITE_lastStatusDate);
            item.txtLastStatus = (TextView) convertView
                    .findViewById(R.id.SITE_lastStatus);
            item.txtAccuracy = (TextView) convertView
                    .findViewById(R.id.SITE_accuracy);

            item.imgConfirmed = (ImageView) convertView.findViewById(R.id.SITE_confirmed);
            convertView.setTag(item);
        } else {
            item = (ViewHolderItem) convertView.getTag();
        }

        final ProjectSiteDTO p = mList.get(position);
        item.txtName.setText(p.getProjectSiteName());
        item.txtNumber.setText("" + (position + 1));

        if (p.getAccuracy() == null) {
            item.txtAccuracy.setVisibility(View.GONE);
        } else {
            item.txtAccuracy.setVisibility(View.VISIBLE);
            item.txtAccuracy.setText(df.format(p.getAccuracy()));
        }
        if (p.getStatusCount() != null)
            item.txtStatusCount.setText("" + p.getStatusCount());
        else
            item.txtStatusCount.setText("0");

        if (p.getBeneficiary() != null) {
            item.txtBen.setText(p.getBeneficiary().getFullName());
            item.txtBen.setVisibility(View.VISIBLE);
        } else {
            item.txtBen.setVisibility(View.GONE);
        }
        if (p.getLocationConfirmed() == null) {
            item.imgConfirmed.setVisibility(View.GONE);
        } else {
            item.imgConfirmed.setVisibility(View.VISIBLE);
        }

        if (p.getLastStatus() != null) {
            item.txtLastStatus.setText(p.getLastStatus().getTaskStatus().getTaskStatusName());
            item.txtTaskName.setText(p.getLastStatus().getTask().getTaskName());
            if (p.getLastStatus().getStatusDate() == null) {
                item.txtDate.setText("Date not available");
            } else {
                item.txtDate.setText(sdf.format(p.getLastStatus().getStatusDate()));
            }

            item.statLayout1.setVisibility(View.VISIBLE);
            item.statLayout2.setVisibility(View.VISIBLE);
            switch (p.getLastStatus().getTaskStatus().getStatusColor()) {
                case TaskStatusDTO.STATUS_COLOR_GREEN:
                    item.txtStatusCount.setBackground(ContextCompat.getDrawable(ctx, R.drawable.xgreen_oval));
                    item.txtTaskCount.setBackground(ContextCompat.getDrawable(ctx, R.drawable.xgreen_oval_small));
                    break;
                case TaskStatusDTO.STATUS_COLOR_AMBER:
                    item.txtStatusCount.setBackground(ContextCompat.getDrawable(ctx, R.drawable.xorange_oval));
                    item.txtTaskCount.setBackground(ContextCompat.getDrawable(ctx, R.drawable.xorange_oval_small));
                    break;
                case TaskStatusDTO.STATUS_COLOR_RED:
                    item.txtStatusCount.setBackground(ContextCompat.getDrawable(ctx, R.drawable.xred_oval));
                    item.txtTaskCount.setBackground(ContextCompat.getDrawable(ctx, R.drawable.xred_oval_small));
                    break;
                default:
                    item.txtStatusCount.setBackground(ContextCompat.getDrawable(ctx, R.drawable.xgrey_oval));
                    item.txtTaskCount.setBackground(ContextCompat.getDrawable(ctx, R.drawable.xgrey_oval_small));
                    break;
            }

        } else {
            item.statLayout1.setVisibility(View.GONE);
            item.statLayout2.setVisibility(View.GONE);
            item.txtStatusCount.setBackground(ContextCompat.getDrawable(ctx, R.drawable.xgrey_oval));
            item.txtTaskCount.setBackground(ContextCompat.getDrawable(ctx, R.drawable.xgrey_oval_small));
        }

        if (p.getProjectSiteTaskList() == null) {
            item.txtTaskCount.setText("0");
        } else {
            item.txtTaskCount.setText("" + p.getProjectSiteTaskList().size());
        }

        if (p.getLastStatus() != null) {
            if (p.getLastStatus().getStatusDate() == null) {
                item.txtDate.setText("Date not available");
            } else {
                item.txtDate.setText(sdf.format(p.getLastStatus().getStatusDate()));
                item.txtDate.setVisibility(View.VISIBLE);
            }
        }

        item.txtName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onProjectSiteClicked(p, position);
            }
        });
        item.txtStatusCount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onProjectSiteClicked(p, position);
            }
        });
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onProjectSiteClicked(p, position);
            }
        });

        return (convertView);
    }


    static final Locale x = Locale.getDefault();
    static final SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy HH:mm", x);
    static final DecimalFormat df = new DecimalFormat("###,###,##0.00");
}

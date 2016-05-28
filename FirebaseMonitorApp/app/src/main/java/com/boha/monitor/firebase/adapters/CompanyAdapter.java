package com.boha.monitor.firebase.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.boha.monitor.firebase.R;
import com.boha.monitor.firebase.dto.MonitorCompanyDTO;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by aubreyM on 14/12/17.
 */
public class CompanyAdapter extends RecyclerView.Adapter<CompanyAdapter.TaskViewHolder> {

    public interface CompanyListener {
        void onAddProjectRequired(MonitorCompanyDTO company);
        void onAddUserRequired(MonitorCompanyDTO company);
    }

    private CompanyListener mListener;
    private List<MonitorCompanyDTO> companyList;
    private Context ctx;
    boolean isSelectionList;

    public CompanyAdapter(List<MonitorCompanyDTO> companyList, int darkColor,
                          Context context, CompanyListener listener) {
        this.companyList = companyList;
        this.ctx = context;
        this.mListener = listener;
    }

    public CompanyAdapter(List<MonitorCompanyDTO> companyList,
                          Context context, CompanyListener listener) {
        this.companyList = companyList;
        this.ctx = context;
        this.mListener = listener;
    }


    @Override
    public TaskViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.company_item, parent, false);
        return new TaskViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final TaskViewHolder holder, final int position) {

        final MonitorCompanyDTO p = companyList.get(position);
        holder.company.setText(p.getCompanyName());
        holder.date.setText(sdf.format(new Date(p.getDateRegistered())));

        holder.addProjects.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onAddProjectRequired(p);
            }
        });
        holder.addUsers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onAddUserRequired(p);
            }
        });



    }

    @Override
    public int getItemCount() {
        return companyList == null ? 0 : companyList.size();
    }

    static final Locale loc = Locale.getDefault();
    static final SimpleDateFormat sdf = new SimpleDateFormat("EEEE, dd/MMMM/yyyy HH:mm", loc);
    static final DecimalFormat df = new DecimalFormat("###,###,###,###");

    public class TaskViewHolder extends RecyclerView.ViewHolder {
        protected TextView company, date;
        protected ImageView addProjects, addUsers;


        public TaskViewHolder(View itemView) {
            super(itemView);
            company = (TextView) itemView.findViewById(R.id.name);
            date = (TextView) itemView.findViewById(R.id.date);
            addProjects = (ImageView) itemView.findViewById(R.id.addProjects);
            addUsers = (ImageView) itemView.findViewById(R.id.addUsers);
        }

    }

    static final String LOG = CompanyAdapter.class.getSimpleName();
}

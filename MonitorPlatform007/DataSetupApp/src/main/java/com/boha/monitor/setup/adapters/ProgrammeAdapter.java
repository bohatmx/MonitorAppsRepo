package com.boha.monitor.setup.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.boha.monitor.library.dto.ProgrammeDTO;
import com.boha.monitor.library.dto.ProjectDTO;
import com.boha.monitor.library.dto.ResponseDTO;
import com.boha.monitor.setup.R;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

/**
 * Created by aubreyM on 15/12/17.
 */
public class ProgrammeAdapter extends RecyclerView.Adapter<ProgrammeAdapter.ProgrammeViewHolder> {

    public interface ProgrammeListener {
        void onProgrammeClicked(ProgrammeDTO programme);

        void onProjectCountClicked(ProgrammeDTO programme);

        void onTaskTypeCountClicked(ProgrammeDTO programme);

        void onTaskImportRequested(ProgrammeDTO programme);

        void onProjectImportRequested(ProgrammeDTO programme);

        void onIconDeleteClicked(ProgrammeDTO programme, int position);

        void onIconEditClicked(ProgrammeDTO programme, int position);

        void onCompanyDataRefreshed(ResponseDTO response, Integer companyID);

        void setBusy(boolean busy);
    }

    private ProgrammeListener listener;
    private List<ProgrammeDTO> programmeList;
    private Context ctx;

    public ProgrammeAdapter(List<ProgrammeDTO> programmes,
                            Context context, ProgrammeListener listener) {
        this.programmeList = programmes;
        this.ctx = context;
        this.listener = listener;
    }


    @Override
    public ProgrammeViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ProgrammeViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.programme_item, parent, false));

    }

    @Override
    public void onBindViewHolder(final ProgrammeViewHolder vh, final int position) {

        final ProgrammeDTO p = programmeList.get(position);
        vh.number.setText("" + (position + 1));
        vh.name.setText(p.getProgrammeName());
        vh.position = position;
        if (p.getProjectList() != null)
            vh.projectCount.setText("" + p.getProjectList().size());
        else
            vh.projectCount.setText("0");
        if (p.getTaskTypeList() != null)
            vh.taskTypeCount.setText("" + p.getTaskTypeList().size());
        else
            vh.taskTypeCount.setText("0");

        int mCount = 0, sCount = 0;
        for (ProjectDTO c : p.getProjectList()) {
            if (c.getMonitorList() != null)
                mCount += c.getMonitorList().size();
            if (c.getStaffList() != null)
                sCount += c.getStaffList().size();
        }
        vh.monCount.setText("" + mCount);
        vh.staffCount.setText("" + sCount);

        setListener(vh.name, p);
        setListener(vh.number, p);


        if (p.getProjectList().isEmpty()) {
            vh.iconDelete.setVisibility(View.VISIBLE);
            vh.iconDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onIconDeleteClicked(p, position);
                }
            });
        } else {
            vh.iconDelete.setVisibility(View.GONE);
        }
        vh.iconEDit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onIconEditClicked(p, position);
            }
        });
        vh.projectCountLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onProjectCountClicked(p);
            }
        });
        vh.taskTypeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onTaskTypeCountClicked(p);
            }
        });
        vh.iconImportTasks.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onTaskImportRequested(p);
            }
        });
        vh.iconImportProjects.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onProjectImportRequested(p);
            }
        });

    }

    private void setListener(View view, final ProgrammeDTO dto) {
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onProgrammeClicked(dto);
            }
        });
    }

    public int getItemCount() {
        return programmeList == null ? 0 : programmeList.size();
    }

    static final Locale loc = Locale.getDefault();
    static final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", loc);

    public class ProgrammeViewHolder extends RecyclerView.ViewHolder {
        protected ImageView iconDelete, iconEDit, iconImportProjects, iconImportTasks;
        protected TextView name, number, projectCount, monCount, staffCount, taskTypeCount;
        protected int position;
        protected View taskTypeLayout, projectCountLayout;


        public ProgrammeViewHolder(View itemView) {
            super(itemView);
            iconImportProjects = (ImageView) itemView.findViewById(R.id.PROG_importProjects);
            iconImportTasks = (ImageView) itemView.findViewById(R.id.PROG_importTasks);
            iconDelete = (ImageView) itemView.findViewById(R.id.PROG_delete);
            iconEDit = (ImageView) itemView.findViewById(R.id.PROG_edit);
            name = (TextView) itemView.findViewById(R.id.PROG_name);
            number = (TextView) itemView.findViewById(R.id.PROG_number);
            projectCount = (TextView) itemView.findViewById(R.id.PROG_projCount);
            monCount = (TextView) itemView.findViewById(R.id.PROG_monCount);
            staffCount = (TextView) itemView.findViewById(R.id.PROG_staffCount);
            taskTypeCount = (TextView) itemView.findViewById(R.id.PROG_taskTypeCount);
            taskTypeLayout = itemView.findViewById(R.id.PROG_taskLayout);
            projectCountLayout = itemView.findViewById(R.id.PROG_projectCountLayout);
        }

    }

    static final String LOG = ProgrammeAdapter.class.getSimpleName();
}

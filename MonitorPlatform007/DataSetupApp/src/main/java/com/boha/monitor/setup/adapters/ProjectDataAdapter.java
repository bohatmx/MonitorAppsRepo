package com.boha.monitor.setup.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.boha.monitor.library.dto.ProjectDTO;
import com.boha.monitor.library.util.Util;
import com.boha.monitor.setup.R;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

/**
 * Created by aubreyM on 15/12/17.
 */
public class ProjectDataAdapter extends RecyclerView.Adapter<ProjectDataAdapter.ProjectViewHolder> {

    public interface ProjectListener {
        void onProjectClicked(ProjectDTO project);

        void onTaskCountClicked(ProjectDTO project);

        void onStaffCount(ProjectDTO project);

        void onMonitorCount(ProjectDTO project);

        void onIconGetLocationClicked(ProjectDTO project);

        void onIconDeleteClicked(ProjectDTO project, int position);

        void onIconEditClicked(ProjectDTO project, int position);

        void setBusy(boolean busy);
    }

    private ProjectListener listener;
    private List<ProjectDTO> projectList;
    private Context ctx;

    public ProjectDataAdapter(List<ProjectDTO> projects,
                              Context context, ProjectListener listener) {
        this.projectList = projects;
        this.ctx = context;
        this.listener = listener;
    }


    @Override
    public ProjectViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ProjectViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.project_item_card, parent, false));

    }

    @Override
    public void onBindViewHolder(final ProjectViewHolder vh, final int position) {

        final ProjectDTO p = projectList.get(position);
        vh.number.setText("" + (position + 1));
        vh.name.setText(p.getProjectName());
        vh.position = position;
        if (p.getProjectTaskList() != null)
            vh.taskCount.setText("" + p.getProjectTaskList().size());
        else vh.taskCount.setText("0");
        if (p.getStaffList() != null)
            vh.staffCount.setText("" + p.getStaffList().size());
        else vh.staffCount.setText("0");
        if (p.getMonitorList() != null)
            vh.monCount.setText("" + p.getMonitorList().size());
        else vh.monCount.setText("0");

        if (p.getCityName() != null) {
            vh.city.setVisibility(View.VISIBLE);
            String cityName = p.getCityName();
            if (p.getMunicipalityName() != null) {
                cityName += " (" + p.getMunicipalityName() + ")";
            }
            vh.city.setText(cityName);
        } else {
            vh.city.setVisibility(View.GONE);
        }


        setListener(vh.name, p);
        setListener(vh.number, p);

        vh.taskCount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Util.flashOnce(vh.taskCount, 300, new Util.UtilAnimationListener() {
                    @Override
                    public void onAnimationEnded() {
                        listener.onTaskCountClicked(p);
                    }
                });
            }
        });
        vh.staffCount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Util.flashOnce(vh.staffCount, 300, new Util.UtilAnimationListener() {
                    @Override
                    public void onAnimationEnded() {
                        listener.onStaffCount(p);
                    }
                });
            }
        });
        vh.monCount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Util.flashOnce(vh.monCount, 300, new Util.UtilAnimationListener() {
                    @Override
                    public void onAnimationEnded() {
                        listener.onMonitorCount(p);
                    }
                });
            }
        });
        vh.iconGetLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Util.flashOnce(vh.iconGetLocation, 300, new Util.UtilAnimationListener() {
                    @Override
                    public void onAnimationEnded() {
                        listener.onIconGetLocationClicked(p);
                    }
                });
            }
        });

        if (p.getProjectTaskList().isEmpty()) {
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
        if (p.getLatitude() != null) {
            vh.locNotConfirmed.setVisibility(View.GONE);
            vh.locConfirmedLayout.setVisibility(View.VISIBLE);
        } else {
            vh.locNotConfirmed.setVisibility(View.VISIBLE);
            vh.locConfirmedLayout.setVisibility(View.GONE);
        }

    }

    private void setListener(View view, final ProjectDTO dto) {
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onProjectClicked(dto);
            }
        });
    }

    public int getItemCount() {
        return projectList == null ? 0 : projectList.size();
    }

    static final Locale loc = Locale.getDefault();
    static final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", loc);

    public class ProjectViewHolder extends RecyclerView.ViewHolder {
        protected ImageView iconDelete, iconEDit,
                iconNotConfirmed, iconConfirmed, iconGetLocation;
        protected TextView name, number, taskCount, monCount, staffCount, city;
        protected int position;
        protected View locConfirmedLayout, locNotConfirmed;


        public ProjectViewHolder(View itemView) {
            super(itemView);
            iconGetLocation = (ImageView) itemView.findViewById(R.id.PROJ_location);
            iconDelete = (ImageView) itemView.findViewById(R.id.PROJ_delete);
            iconEDit = (ImageView) itemView.findViewById(R.id.PROJ_edit);
            iconNotConfirmed = (ImageView) itemView.findViewById(R.id.PROJ_locNotConfirmed);
            iconConfirmed = (ImageView) itemView.findViewById(R.id.PROJ_locConfirmed);
            name = (TextView) itemView.findViewById(R.id.PROJ_name);
            number = (TextView) itemView.findViewById(R.id.PROJ_number);
            taskCount = (TextView) itemView.findViewById(R.id.PROJ_taskCount);
            monCount = (TextView) itemView.findViewById(R.id.PROJ_monCount);
            staffCount = (TextView) itemView.findViewById(R.id.PROJ_staffCount);
            city = (TextView) itemView.findViewById(R.id.PROJ_cityName);
            locConfirmedLayout = itemView.findViewById(R.id.PROJ_locationConfirmedlayout);
            locNotConfirmed = itemView.findViewById(R.id.PROJ_locationNotConfirmedlayout);
        }

    }

    static final String LOG = ProjectDataAdapter.class.getSimpleName();
}

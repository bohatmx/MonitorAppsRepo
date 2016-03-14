package com.boha.monitor.library.adapters;

import android.content.Context;
import android.graphics.PorterDuff;
import android.location.Location;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.boha.monitor.library.dto.ProjectDTO;
import com.boha.monitor.library.fragments.ProjectListFragment;
import com.boha.monitor.library.util.SharedUtil;
import com.boha.monitor.library.util.Statics;
import com.boha.platform.library.R;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by aubreyM on 14/12/17.
 */
public class ProjectAdapter extends RecyclerView.Adapter<ProjectAdapter.ProjectViewHolder> {


    private ProjectListFragment.ProjectListFragmentListener listener;
    private List<ProjectDTO> projectList;
    private Context ctx;
    private int darkColor;
    private double latitude, longitude;
    static final int HEADER = 1, ITEM = 2;

    public ProjectAdapter(List<ProjectDTO> projectList,
                          Context context, int darkColor,
                          double latitude, double longitude,
                          ProjectListFragment.ProjectListFragmentListener listener) {
        this.projectList = projectList;
        this.ctx = context;
        this.listener = listener;
        this.darkColor = darkColor;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    @Override
    public ProjectViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {


        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.project_item, parent, false);
        return new ProjectViewHolder(v);


    }


    @Override
    public void onBindViewHolder(final ProjectViewHolder pvh, final int position) {


        pvh.txtNumber.setText("" + (position));
        pvh.txtTasks.setText("0");
        pvh.txtLastDate.setText("");
        pvh.txtPhotos.setText("0");
        pvh.txtMuni.setText("");
        pvh.txtStatusCount.setText("0");
        pvh.txtVideos.setText("0");
        final ProjectDTO project = projectList.get(position);
        pvh.txtProjectName.setText(project.getProjectName());
        Statics.setRobotoFontLight(ctx,pvh.txtProjectName);
//        Log.d("ProjectAdapter", "project: " + project.getProjectName()
//        + " photos: " + project.getPhotoCount() + " statusCount: "
//                + project.getStatusCount() + " videoCount: " + project.getVideoCount()
//                + " projectID: " + project.getProjectID());

        pvh.imageLayout.setVisibility(View.GONE);
        pvh.image.setVisibility(View.GONE);

        pvh.txtProjectName.setText(project.getProjectName());
        if (project.getCityName() != null) {
            pvh.txtCity.setText(project.getCityName());
        }
        if (project.getMunicipalityName() != null) {
            pvh.txtMuni.setText(project.getMunicipalityName());
        }
        pvh.txtNumber.setText("" + (position + 1));
        if (project.getLastStatus() != null) {
            pvh.txtLastDate.setText(sdf.format(new Date(project.getLastStatus().getStatusDate())));
        } else {
            pvh.txtLastDate.setText("No Status Date");
        }
        pvh.txtPhotos.setText(df.format(project.getPhotoCount()));
        pvh.txtStatusCount.setText(df.format(project.getStatusCount()));
        pvh.txtTasks.setText(df.format(project.getProjectTaskCount()));

        pvh.txtStaff.setText(df.format(project.getStaffCount()));
        pvh.txtMonitors.setText(df.format(project.getMonitorCount()));
        pvh.txtVideos.setText(df.format(project.getVideoCount()));
        setPlaceNames(project, pvh);

        pvh.txtVideos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onVideoPlayListRequired(project);
            }
        });
        pvh.txtStatusCount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onStatusReportRequired(project);
            }
        });
        pvh.txtPhotos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (project.getPhotoCount() > 0) {
                    listener.onGalleryRequired(project);
                }
            }
        });

        pvh.iconCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onCameraRequired(project);
            }
        });
        pvh.iconDirections.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onDirectionsRequired(project);
            }
        });
        pvh.iconDoStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onStatusUpdateRequired(project);
            }
        });
        pvh.iconLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onLocationRequired(project);
            }
        });
        pvh.iconAddTasks.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onProjectTasksRequired(project);
            }
        });

        if (darkColor != 0) {
            pvh.iconCamera.setColorFilter(darkColor, PorterDuff.Mode.SRC_IN);
            pvh.iconDirections.setColorFilter(darkColor, PorterDuff.Mode.SRC_IN);
            pvh.iconDoStatus.setColorFilter(darkColor, PorterDuff.Mode.SRC_IN);
            pvh.iconLocation.setColorFilter(darkColor, PorterDuff.Mode.SRC_IN);
//                pvh.txtProjectName.setTextColor(darkColor);
        }
        if (project.getLatitude() == null) {
            pvh.iconCamera.setEnabled(false);
            pvh.iconDirections.setEnabled(false);
            pvh.iconDoStatus.setEnabled(false);
            pvh.iconCamera.setAlpha(0.2f);
            pvh.iconDirections.setAlpha(0.2f);
            pvh.iconDoStatus.setAlpha(0.2f);
        } else {
            pvh.iconCamera.setEnabled(true);
            pvh.iconDirections.setEnabled(true);
            pvh.iconDoStatus.setEnabled(true);
            pvh.iconCamera.setAlpha(1.0f);
            pvh.iconDirections.setAlpha(1.0f);
            pvh.iconDoStatus.setAlpha(1.0f);
            boolean isNear = amNearProject(project);
            if (!isNear) {
                pvh.iconCamera.setEnabled(false);
                pvh.iconDoStatus.setEnabled(false);
                pvh.iconDoStatus.setAlpha(0.2f);
                pvh.iconCamera.setAlpha(0.2f);
            }
        }
        if (SharedUtil.getMonitor(ctx) != null) {
            pvh.iconAddTasks.setVisibility(View.GONE);
        }

        if (project.getDetailsOpen() == Boolean.TRUE) {
            pvh.details.setVisibility(View.VISIBLE);
            pvh.actions.setVisibility(View.VISIBLE);
        } else {
            pvh.details.setVisibility(View.GONE);
            pvh.actions.setVisibility(View.GONE);
        }
        pvh.txtProjectName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (project.getDetailsOpen() == Boolean.FALSE) {
                    project.setDetailsOpen(Boolean.TRUE);
                    pvh.details.setVisibility(View.VISIBLE);
                    pvh.actions.setVisibility(View.VISIBLE);
                } else {
                    project.setDetailsOpen(Boolean.FALSE);
                    pvh.details.setVisibility(View.GONE);
                    pvh.actions.setVisibility(View.GONE);
                }
            }
        });
    }

    public static final double RADIUS_IN_METRES = 500;
    private boolean amNearProject(ProjectDTO p) {
        if (p.getLatitude() == null) {
            return false;
        }
        boolean amNear = false;
        Location here = new Location("");
        here.setLatitude(latitude);
        here.setLongitude(longitude);

        Location there = new Location("");
        there.setLatitude(p.getLatitude());
        there.setLongitude(p.getLongitude());

        double dist = here.distanceTo(there);
        if (dist <= RADIUS_IN_METRES) {
            amNear = true;
        }

        Log.d("ProjectAdapter","Distance away from project, as the crow flies: " + df2.format(dist) + " metres");

        return amNear;
    }
    static final DecimalFormat df2 = new DecimalFormat("###,###,###,##0.00");
    private void setPlaceNames(ProjectDTO project, ProjectViewHolder pvh) {
        pvh.txtMuni.setVisibility(View.GONE);
        pvh.txtCity.setVisibility(View.GONE);

        if (project.getCityName() != null) {
            pvh.txtCity.setVisibility(View.VISIBLE);
            pvh.txtCity.setText(project.getCityName());
        }
        if (project.getMunicipalityName() != null) {
            pvh.txtMuni.setVisibility(View.VISIBLE);
            pvh.txtMuni.setText(project.getMunicipalityName());
            Statics.setRobotoFontBold(ctx, pvh.txtMuni);
        }

    }

    @Override
    public int getItemCount() {
        return projectList == null ? 0 : projectList.size() + 1;
    }

    static final Locale loc = Locale.getDefault();
    static final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", loc);
    static final DecimalFormat df = new DecimalFormat("###,###,###,###");

    public class ProjectViewHolder extends RecyclerView.ViewHolder {
        protected ImageView image;
        protected TextView txtProjectName, txtStatusCount, txtLastDate,
                txtStaff, txtMonitors, txtVideos,
                txtPhotos, txtTasks, txtCity, txtMuni, txtNumber, txtCaption;
        protected ImageView
                iconCamera, iconDirections, iconDoStatus,
                iconLocation, iconAddTasks;
        protected View imageLayout, details, actions;


        public ProjectViewHolder(View itemView) {

            super(itemView);
            actions = itemView.findViewById(R.id.PI_top);
            details = itemView.findViewById(R.id.PI_details);
            imageLayout = itemView.findViewById(R.id.PI_imageLayout);
            image = (ImageView) itemView.findViewById(R.id.PI_photo);
            iconAddTasks = (ImageView) itemView.findViewById(R.id.PI_addTaskIcon);
            txtProjectName = (TextView) itemView.findViewById(R.id.PI_projectName);
            txtPhotos = (TextView) itemView.findViewById(R.id.PI_photoCount);
            txtStatusCount = (TextView) itemView.findViewById(R.id.PI_statusCount);
            txtLastDate = (TextView) itemView.findViewById(R.id.PI_lastStatusDate);
            txtTasks = (TextView) itemView.findViewById(R.id.PI_taskCount);
            txtCity = (TextView) itemView.findViewById(R.id.PI_cityName);
            txtMuni = (TextView) itemView.findViewById(R.id.PI_muniName);
            txtNumber = (TextView) itemView.findViewById(R.id.PI_number);
            txtCaption = (TextView) itemView.findViewById(R.id.PI_caption);
            txtStaff = (TextView) itemView.findViewById(R.id.PI_staffCount);
            txtMonitors = (TextView) itemView.findViewById(R.id.PI_monitorCount);
            txtVideos = (TextView) itemView.findViewById(R.id.PI_videoCount);

            iconCamera = (ImageView) itemView.findViewById(R.id.PA_camera);
            iconDirections = (ImageView) itemView.findViewById(R.id.PA_directions);
            iconDoStatus = (ImageView) itemView.findViewById(R.id.PA_doStatus);
            iconLocation = (ImageView) itemView.findViewById(R.id.PA_locations);
        }

    }


    static final String LOG = ProjectAdapter.class.getSimpleName();
}

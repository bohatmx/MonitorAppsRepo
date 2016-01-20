package com.boha.monitor.library.adapters;

import android.content.Context;
import android.graphics.PorterDuff;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.boha.monitor.library.dto.PhotoUploadDTO;
import com.boha.monitor.library.dto.ProjectDTO;
import com.boha.monitor.library.dto.ProjectTaskDTO;
import com.boha.monitor.library.dto.ProjectTaskStatusDTO;
import com.boha.monitor.library.fragments.ProjectListFragment;
import com.boha.monitor.library.util.SharedUtil;
import com.boha.monitor.library.util.Statics;
import com.boha.monitor.library.util.Util;
import com.boha.platform.library.R;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by aubreyM on 14/12/17.
 */
public class ProjectAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {


    private ProjectListFragment.ProjectListFragmentListener listener;
    private List<ProjectDTO> projectList;
    private Context ctx;
    private int darkColor;
    static final int HEADER = 1, ITEM = 2;

    public ProjectAdapter(List<ProjectDTO> projectList,
                          Context context, int darkColor, ProjectListFragment.ProjectListFragmentListener listener) {
        this.projectList = projectList;
        this.ctx = context;
        this.listener = listener;
        this.darkColor = darkColor;
    }


    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return HEADER;
        } else {
            return ITEM;
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        if (viewType == HEADER) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.project_list_header, parent, false);
            return new HeaderViewHolder(v);
        } else {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.project_item, parent, false);
            return new ProjectViewHolder(v);
        }

    }


    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {

        if (holder instanceof HeaderViewHolder) {
            final ProjectDTO p = projectList.get(0);
            final HeaderViewHolder hvh = (HeaderViewHolder) holder;
            if (p.getProgrammeName() != null && !p.getProgrammeName().isEmpty()) {
                hvh.txtProgramme.setText(p.getProgrammeName());
            } else {
                hvh.txtProgramme.setText(SharedUtil.getCompany(ctx).getCompanyName());
            }
            hvh.txtCount.setText("" + projectList.size());
            hvh.image.setImageDrawable(Util.getRandomBackgroundImage(ctx));
            Statics.setRobotoFontLight(ctx, hvh.txtProgramme);
        }

        if (holder instanceof ProjectViewHolder) {
            final ProjectDTO project = projectList.get(position - 1);
            final ProjectViewHolder pvh = (ProjectViewHolder) holder;
            pvh.imageLayout.setVisibility(View.GONE);
            pvh.image.setVisibility(View.GONE);

            pvh.txtProjectName.setText(project.getProjectName());
            pvh.txtNumber.setText("" + (position));
            if (project.getProjectTaskList() != null) {
                pvh.txtTasks.setText("" + project.getProjectTaskList().size());

            } else {
                pvh.txtTasks.setText("0");
            }
            List<PhotoUploadDTO> photoList = new ArrayList<>();
            int photoCount = 0, statusCount = 0;
            if (project.getPhotoUploadList() != null) {
                photoCount = project.getPhotoUploadList().size();
                photoList.addAll(project.getPhotoUploadList());
            }

            List<ProjectTaskStatusDTO> statusList = new ArrayList<>();
            for (ProjectTaskDTO task : project.getProjectTaskList()) {
                if (task.getPhotoUploadList() != null) {
                    photoCount += task.getPhotoUploadList().size();
                    photoList.addAll(task.getPhotoUploadList());
                }
                if (task.getProjectTaskStatusList() != null) {
                    statusCount += task.getProjectTaskStatusList().size();
                    statusList.addAll(task.getProjectTaskStatusList());
                }
            }
            Collections.sort(statusList);
            Collections.sort(photoList);
            if (!statusList.isEmpty()) {
                pvh.txtLastDate.setText(sdf.format(new Date(statusList.get(0).getDateUpdated())));
            } else {
                pvh.txtLastDate.setText("No Status Date");
            }
            pvh.txtPhotos.setText(df.format(photoCount));
            pvh.txtStatusCount.setText(df.format(statusCount));
            setPlaceNames(project, pvh);

            pvh.txtStatusCount.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Util.flashOnce(pvh.txtStatusCount, 300, new Util.UtilAnimationListener() {
                        @Override
                        public void onAnimationEnded() {
                            listener.onStatusReportRequired(project);
                        }
                    });
                }
            });
            pvh.txtPhotos.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Util.flashOnce(pvh.txtPhotos, 300, new Util.UtilAnimationListener() {
                        @Override
                        public void onAnimationEnded() {
                            listener.onGalleryRequired(project);
                        }
                    });
                }
            });

            pvh.iconCamera.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Util.flashOnce(pvh.iconCamera, 300, new Util.UtilAnimationListener() {
                        @Override
                        public void onAnimationEnded() {
                            listener.onCameraRequired(project);
                        }
                    });
                }
            });
            pvh.iconDirections.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Util.flashOnce(pvh.iconDirections, 300, new Util.UtilAnimationListener() {
                        @Override
                        public void onAnimationEnded() {
                            listener.onDirectionsRequired(project);
                        }
                    });
                }
            });
            pvh.iconDoStatus.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Util.flashOnce(pvh.iconDoStatus, 300, new Util.UtilAnimationListener() {
                        @Override
                        public void onAnimationEnded() {
                            listener.onStatusUpdateRequired(project);
                        }
                    });
                }
            });
            pvh.iconLocation.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Util.flashOnce(pvh.iconLocation, 300, new Util.UtilAnimationListener() {
                        @Override
                        public void onAnimationEnded() {
                            listener.onLocationRequired(project);
                        }
                    });
                }
            });

            if (darkColor != 0) {
                pvh.iconCamera.setColorFilter(darkColor, PorterDuff.Mode.SRC_IN);
                pvh.iconDirections.setColorFilter(darkColor, PorterDuff.Mode.SRC_IN);
                pvh.iconDoStatus.setColorFilter(darkColor, PorterDuff.Mode.SRC_IN);
                pvh.iconLocation.setColorFilter(darkColor, PorterDuff.Mode.SRC_IN);
                pvh.txtProjectName.setTextColor(darkColor);
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
            }

        }


    }

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
            Statics.setRobotoFontLight(ctx, pvh.txtMuni);
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
                txtPhotos, txtTasks, txtCity, txtMuni, txtNumber, txtCaption;
        protected ImageView
                iconCamera, iconDirections, iconDoStatus,
                iconLocation;
        protected View imageLayout;


        public ProjectViewHolder(View itemView) {

            super(itemView);
            imageLayout = itemView.findViewById(R.id.PI_imageLayout);
            image = (ImageView) itemView.findViewById(R.id.PI_photo);
            txtProjectName = (TextView) itemView.findViewById(R.id.PI_projectName);
            txtPhotos = (TextView) itemView.findViewById(R.id.PI_photoCount);
            txtStatusCount = (TextView) itemView.findViewById(R.id.PI_statusCount);
            txtLastDate = (TextView) itemView.findViewById(R.id.PI_lastStatusDate);
            txtTasks = (TextView) itemView.findViewById(R.id.PI_taskCount);
            txtCity = (TextView) itemView.findViewById(R.id.PI_cityName);
            txtMuni = (TextView) itemView.findViewById(R.id.PI_muniName);
            txtNumber = (TextView) itemView.findViewById(R.id.PI_number);
            txtCaption = (TextView) itemView.findViewById(R.id.PI_caption);

            iconCamera = (ImageView) itemView.findViewById(R.id.PA_camera);
            iconDirections = (ImageView) itemView.findViewById(R.id.PA_directions);
            iconDoStatus = (ImageView) itemView.findViewById(R.id.PA_doStatus);
            iconLocation = (ImageView) itemView.findViewById(R.id.PA_locations);
        }

    }

    public class HeaderViewHolder extends RecyclerView.ViewHolder {
        protected ImageView image;
        protected TextView txtProgramme, txtCount;


        public HeaderViewHolder(View itemView) {
            super(itemView);
            image = (ImageView) itemView.findViewById(R.id.PRH_image);
            txtProgramme = (TextView) itemView.findViewById(R.id.PRH_programme);
            txtCount = (TextView) itemView.findViewById(R.id.PRH_count);

        }

    }


    static final String LOG = ProjectAdapter.class.getSimpleName();
}

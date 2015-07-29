package com.boha.monitor.library.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.boha.monitor.library.dto.ProjectDTO;
import com.boha.monitor.library.dto.ProjectTaskDTO;
import com.boha.monitor.library.fragments.ProjectListFragment;
import com.boha.monitor.library.util.Util;
import com.boha.platform.library.R;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
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
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.project_item_with_shadow, parent, false);
            return new ProjectViewHolder(v);
        }

    }


    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {


        if (holder instanceof HeaderViewHolder) {
            final ProjectDTO p = projectList.get(0);
            final HeaderViewHolder hvh = (HeaderViewHolder) holder;
            hvh.txtProgramme.setText(p.getProgrammeName());
            hvh.txtCount.setText("" + projectList.size());
            hvh.image.setImageDrawable(Util.getRandomHeroImage(ctx));
        }
        if (holder instanceof ProjectViewHolder) {
            final ProjectDTO project = projectList.get(position - 1);
            final ProjectViewHolder pvh = (ProjectViewHolder) holder;
            if (project.getLastStatus() != null) {
                pvh.txtLastDate.setText(sdf.format(new Date(project.getLastStatus().getStatusDate())));
            } else {
                pvh.txtLastDate.setText("No Status Date");
            }
            pvh.txtProjectName.setText(project.getProjectName());
            if (project.getProjectTaskList() != null) {
                pvh.txtTasks.setText("" + project.getProjectTaskList().size());
            } else {
                pvh.txtTasks.setText("0");
            }
            int photoCount = 0, statusCount = 0;
            if (project.getPhotoUploadList() != null) {
                photoCount = project.getPhotoUploadList().size();
            }
            for (ProjectTaskDTO task : project.getProjectTaskList()) {
                if (task.getPhotoUploadList() != null)
                    photoCount += task.getPhotoUploadList().size();
                if (task.getProjectTaskStatusList() != null) {
                    statusCount += task.getProjectTaskStatusList().size();
                }
            }
            pvh.txtPhotos.setText(df.format(photoCount));
            pvh.txtStatusCount.setText(df.format(statusCount));


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
            pvh.iconGallery.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Util.flashOnce(pvh.iconGallery, 300, new Util.UtilAnimationListener() {
                        @Override
                        public void onAnimationEnded() {
                            listener.onGalleryRequired(project);
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
            pvh.iconMap.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Util.flashOnce(pvh.iconMap, 300, new Util.UtilAnimationListener() {
                        @Override
                        public void onAnimationEnded() {
                            listener.onMapRequired(project);
                        }
                    });
                }
            });
            pvh.iconMessaging.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Util.flashOnce(pvh.iconMessaging, 300, new Util.UtilAnimationListener() {
                        @Override
                        public void onAnimationEnded() {
                            listener.onMessagingRequired(project);
                        }
                    });
                }
            });

            if (project.getPhotoUploadList() == null || project.getPhotoUploadList().isEmpty()) {
                pvh.image.setVisibility(View.GONE);
            } else {
                pvh.image.setVisibility(View.VISIBLE);
                String url = project.getPhotoUploadList().get(0).getUri();
                Log.w(LOG, "## photo url: " + url);
                ImageLoader.getInstance().displayImage(url, pvh.image, new ImageLoadingListener() {
                    @Override
                    public void onLoadingStarted(String s, View view) {

                    }

                    @Override
                    public void onLoadingFailed(String s, View view, FailReason failReason) {
                        pvh.image.setImageDrawable(ContextCompat.getDrawable(ctx, R.drawable.under_construction));
                    }

                    @Override
                    public void onLoadingComplete(String s, View view, Bitmap bitmap) {

                    }

                    @Override
                    public void onLoadingCancelled(String s, View view) {

                    }
                });

            }
            if (darkColor != 0) {
                pvh.iconCamera.setColorFilter(darkColor, PorterDuff.Mode.SRC_IN);
                pvh.iconDirections.setColorFilter(darkColor, PorterDuff.Mode.SRC_IN);
                pvh.iconDoStatus.setColorFilter(darkColor, PorterDuff.Mode.SRC_IN);
                pvh.iconGallery.setColorFilter(darkColor, PorterDuff.Mode.SRC_IN);
                pvh.iconLocation.setColorFilter(darkColor, PorterDuff.Mode.SRC_IN);
                pvh.iconMap.setColorFilter(darkColor, PorterDuff.Mode.SRC_IN);
                pvh.iconMessaging.setColorFilter(darkColor, PorterDuff.Mode.SRC_IN);
                pvh.iconStatusRpt.setColorFilter(darkColor, PorterDuff.Mode.SRC_IN);
            }

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
        protected TextView txtProjectName, txtStatusCount, txtLastDate, txtPhotos, txtTasks;
        protected ImageView
                iconCamera, iconDirections, iconDoStatus,
                iconMap, iconGallery, iconLocation, iconStatusRpt,
                iconMessaging;
        protected int imageIndex;


        public ProjectViewHolder(View itemView) {
            super(itemView);
            image = (ImageView) itemView.findViewById(R.id.PI_photo);
            txtProjectName = (TextView) itemView.findViewById(R.id.PI_projectName);
            txtPhotos = (TextView) itemView.findViewById(R.id.PI_photoCount);
            txtStatusCount = (TextView) itemView.findViewById(R.id.PI_statusCount);
            txtLastDate = (TextView) itemView.findViewById(R.id.PI_lastStatusDate);
            txtTasks = (TextView) itemView.findViewById(R.id.PI_taskCount);

            iconCamera = (ImageView) itemView.findViewById(R.id.PA_camera);
            iconDirections = (ImageView) itemView.findViewById(R.id.PA_directions);

            iconDoStatus = (ImageView) itemView.findViewById(R.id.PA_doStatus);
            iconGallery = (ImageView) itemView.findViewById(R.id.PA_gallery);
            iconLocation = (ImageView) itemView.findViewById(R.id.PA_locations);

            iconMap = (ImageView) itemView.findViewById(R.id.PA_map);
            iconMessaging = (ImageView) itemView.findViewById(R.id.PA_messaging);
            iconStatusRpt = (ImageView) itemView.findViewById(R.id.PA_statusReport);
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

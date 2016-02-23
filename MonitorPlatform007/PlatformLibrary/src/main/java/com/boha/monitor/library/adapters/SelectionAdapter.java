package com.boha.monitor.library.adapters;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.boha.monitor.library.dto.ProjectDTO;
import com.boha.platform.library.R;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;


/**
 * Created by aubreyM on 14/12/17.
 */
public class SelectionAdapter extends RecyclerView.Adapter<SelectionAdapter.ProjectViewHolder> {

    public interface ProjectSelectionListener {
        void onProjectAdded(ProjectDTO project);
        void onProjectRemoved(ProjectDTO project);
    }

    private ProjectSelectionListener listener;
    private List<ProjectDTO> projectList;
    int darkColor;
    Context ctx;

    public SelectionAdapter(List<ProjectDTO> projectList, int darkColor,
                             ProjectSelectionListener listener) {
        this.projectList = projectList;
        this.listener = listener;
        this.darkColor = darkColor;
    }


    @Override
    public ProjectViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.project_selection_item, parent, false);
        return new ProjectViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ProjectViewHolder pvh, final int position) {

        final ProjectDTO p = projectList.get(position);
        pvh.txtProjectName.setText(p.getProjectName());
        pvh.txtCity.setVisibility(View.GONE);
        pvh.chkBox.setEnabled(false);

        if (p.getSelected() == Boolean.TRUE) {
            pvh.txtProjectName.setTextColor(Color.BLACK);
            pvh.chkBox.setChecked(true);
        } else {
            pvh.txtProjectName.setTextColor(Color.GRAY);
            pvh.chkBox.setChecked(false);
        }
        pvh.txtProjectName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (p.getSelected() == Boolean.TRUE) {
                    p.setSelected(Boolean.FALSE);
                    pvh.txtProjectName.setTextColor(Color.GRAY);
                    pvh.chkBox.setChecked(false);
                    listener.onProjectRemoved(p);
                } else {
                    p.setSelected(Boolean.TRUE);
                    pvh.txtProjectName.setTextColor(Color.BLACK);
                    pvh.chkBox.setChecked(true);
                    listener.onProjectAdded(p);
                }

            }
        });
        pvh.chkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                if (isChecked) {
//                    p.setSelected(Boolean.TRUE);
//                    pvh.txtProjectName.setTextColor(Color.BLUE);
//                    listener.onProjectAdded(p);
//                } else {
//                    p.setSelected(Boolean.FALSE);
//                    pvh.txtProjectName.setTextColor(Color.BLACK);
//                    listener.onProjectRemoved(p);
//                }
            }
        });

        setPlaceNames(p,pvh);
    }

    private void setToAdd(ImageView imgAdd, ImageView imgDelete, CheckBox chkBox) {
        chkBox.setSelected(false);
        imgDelete.setVisibility(View.GONE);
        imgAdd.setVisibility(View.VISIBLE);
    }
    private void setToDelete(ImageView imgAdd, ImageView imgDelete, CheckBox chkBox) {
        chkBox.setSelected(true);
        imgDelete.setVisibility(View.VISIBLE);
        imgAdd.setVisibility(View.GONE);
    }
    private void setPlaceNames(ProjectDTO project, ProjectViewHolder pvh) {
        pvh.txtCity.setVisibility(View.GONE);
        StringBuilder sb = new StringBuilder();

        if (project.getCityName() != null) {
            pvh.txtCity.setVisibility(View.VISIBLE);
            sb.append(project.getCityName());

        }
        if (project.getMunicipalityName() != null) {
            sb.append(", ").append(project.getMunicipalityName());
        }
        if (sb.toString().length() > 0) {
            pvh.txtCity.setText(sb.toString());
        } else {
            pvh.txtCity.setVisibility(View.GONE);
        }
    }


    @Override
    public int getItemCount() {
        return projectList == null ? 0 : projectList.size();
    }

    static final Locale loc = Locale.getDefault();
    static final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", loc);
    static final DecimalFormat df = new DecimalFormat("###,###,###,###");

    public class ProjectViewHolder extends RecyclerView.ViewHolder  {
        protected TextView txtProjectName, txtCity;
        protected CheckBox chkBox;


        public ProjectViewHolder(View itemView) {

            super(itemView);
            txtProjectName = (TextView) itemView.findViewById(R.id.projectName);
            txtCity = (TextView) itemView.findViewById(R.id.city);
            chkBox = (CheckBox) itemView.findViewById(R.id.checkBox);

        }

    }

    static final String LOG = SelectionAdapter.class.getSimpleName();
}

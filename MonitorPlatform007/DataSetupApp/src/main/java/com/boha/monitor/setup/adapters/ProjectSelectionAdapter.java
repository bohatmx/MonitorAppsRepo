package com.boha.monitor.setup.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.boha.monitor.library.dto.ProjectDTO;
import com.boha.monitor.setup.R;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

/**
 * Created by aubreyM on 15/12/17.
 */
public class ProjectSelectionAdapter extends RecyclerView.Adapter<ProjectSelectionAdapter.ProjectViewHolder> {


    private List<ProjectDTO> projectList;
    private Context ctx;

    public ProjectSelectionAdapter(List<ProjectDTO> projectList,
                                   Context context) {
        this.projectList = projectList;
        this.ctx = context;
    }


    @Override
    public ProjectViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ProjectViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.project_select_item, parent, false));

    }

    @Override
    public void onBindViewHolder(final ProjectViewHolder vh, final int position) {

        final ProjectDTO p = projectList.get(position);
        vh.number.setText("" + (position + 1));
        vh.projectName.setText(p.getProjectName());
        vh.position = position;


        if (p.getCityName() != null) {
            vh.city.setVisibility(View.VISIBLE);
            vh.city.setText(p.getCityName());
        } else {
            vh.city.setVisibility(View.GONE);
        }
        if (p.getMunicipalityName() != null) {
            vh.municipality.setVisibility(View.VISIBLE);
            vh.municipality.setText(p.getMunicipalityName());
        } else {
            vh.municipality.setVisibility(View.GONE);
        }

        if (p.getSelected()) {
            vh.checkBox.setChecked(true);
        } else {
            vh.checkBox.setChecked(false);
        }

        vh.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                p.setSelected(isChecked);
            }
        });

    }

    public int getItemCount() {
        return projectList == null ? 0 : projectList.size();
    }

    static final Locale loc = Locale.getDefault();
    static final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", loc);

    public class ProjectViewHolder extends RecyclerView.ViewHolder {
        protected CheckBox checkBox;
        protected TextView projectName, number, municipality, city;
        protected int position;
        protected View topLayout;


        public ProjectViewHolder(View itemView) {
            super(itemView);
            checkBox = (CheckBox) itemView.findViewById(R.id.PSI_checkBox);

            number = (TextView) itemView.findViewById(R.id.PSI_number);
            projectName = (TextView) itemView.findViewById(R.id.PSI_projectName);
            municipality = (TextView) itemView.findViewById(R.id.PSI_municipality);
            city = (TextView) itemView.findViewById(R.id.PSI_city);
            topLayout = itemView.findViewById(R.id.PSI_top);
        }

    }

    static final String LOG = ProjectSelectionAdapter.class.getSimpleName();
}

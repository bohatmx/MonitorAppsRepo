package com.boha.monitor.firebase.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.boha.monitor.firebase.R;
import com.boha.monitor.firebase.data.CityDTO;

import java.util.List;

/**
 * Created by aubreyM on 14/12/17.
 */
public class CityAdapter extends RecyclerView.Adapter<CityAdapter.CityViewHolder> {

    public interface CityListener {
        void onAddProjects(CityDTO city);
    }

    private CityListener mListener;
    private List<CityDTO> cities;

    public CityAdapter(List<CityDTO> cities, CityListener listener) {
        this.cities = cities;
        this.mListener = listener;
    }

    @Override
    public CityViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.place_item, parent, false);
        return new CityViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final CityViewHolder holder, final int position) {

        final CityDTO p = cities.get(position);
        holder.city.setText(p.getCityName());

        holder.city.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onAddProjects(p);
            }
        });
        holder.addProjects.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onAddProjects(p);
            }
        });




    }

    @Override
    public int getItemCount() {
        return cities == null ? 0 : cities.size();
    }

    public class CityViewHolder extends RecyclerView.ViewHolder {
        protected TextView city, label;
        protected ImageView addProjects;


        public CityViewHolder(View itemView) {
            super(itemView);
            city = (TextView) itemView.findViewById(R.id.name);
            label = (TextView) itemView.findViewById(R.id.label);
            label.setText("Add Projects");
            addProjects = (ImageView) itemView.findViewById(R.id.addIcon);
        }

    }

    static final String LOG = CityAdapter.class.getSimpleName();
}

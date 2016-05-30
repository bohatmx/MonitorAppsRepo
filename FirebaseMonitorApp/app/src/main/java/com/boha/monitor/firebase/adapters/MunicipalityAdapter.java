package com.boha.monitor.firebase.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.boha.monitor.firebase.R;
import com.boha.monitor.firebase.dto.MunicipalityDTO;

import java.util.List;

/**
 * Created by aubreyM on 14/12/17.
 */
public class MunicipalityAdapter extends RecyclerView.Adapter<MunicipalityAdapter.MunicipalityViewHolder> {

    public interface MunicipalityListener {
        void onAddCity(MunicipalityDTO municipality);
    }

    private MunicipalityListener mListener;
    private List<MunicipalityDTO> provinces;

    public MunicipalityAdapter(List<MunicipalityDTO> provinces, MunicipalityListener listener) {
        this.provinces = provinces;
        this.mListener = listener;
    }

    @Override
    public MunicipalityViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.place_item, parent, false);
        return new MunicipalityViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final MunicipalityViewHolder holder, final int position) {

        final MunicipalityDTO p = provinces.get(position);
        holder.municipalityName.setText(p.getMunicipalityName());

        holder.municipalityName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onAddCity(p);
            }
        });
        holder.addCity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onAddCity(p);
            }
        });




    }

    @Override
    public int getItemCount() {
        return provinces == null ? 0 : provinces.size();
    }

    public class MunicipalityViewHolder extends RecyclerView.ViewHolder {
        protected TextView municipalityName, label;
        protected ImageView addCity;


        public MunicipalityViewHolder(View itemView) {
            super(itemView);
            municipalityName = (TextView) itemView.findViewById(R.id.name);
            label = (TextView) itemView.findViewById(R.id.label);
            label.setText("Add Cities");
            addCity = (ImageView) itemView.findViewById(R.id.addIcon);
        }

    }

    static final String LOG = MunicipalityAdapter.class.getSimpleName();
}

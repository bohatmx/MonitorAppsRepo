package com.boha.monitor.firebase.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.boha.monitor.firebase.R;
import com.boha.monitor.library.data.ProvinceDTO;

import java.util.List;

/**
 * Created by aubreyM on 14/12/17.
 */
public class ProvinceAdapter extends RecyclerView.Adapter<ProvinceAdapter.ProvinceViewHolder> {

    public interface ProvinceListener {
        void onAddMunicipality(ProvinceDTO province);
    }

    private ProvinceListener mListener;
    private List<ProvinceDTO> provinces;

    public ProvinceAdapter(List<ProvinceDTO> provinces, ProvinceListener listener) {
        this.provinces = provinces;
        this.mListener = listener;
    }

    @Override
    public ProvinceViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.place_item, parent, false);
        return new ProvinceViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ProvinceViewHolder holder, final int position) {

        final ProvinceDTO p = provinces.get(position);
        holder.provinceName.setText(p.getProvinceName());

        holder.provinceName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onAddMunicipality(p);
            }
        });
        holder.addMuni.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onAddMunicipality(p);
            }
        });




    }

    @Override
    public int getItemCount() {
        return provinces == null ? 0 : provinces.size();
    }

    public class ProvinceViewHolder extends RecyclerView.ViewHolder {
        protected TextView provinceName, label;
        protected ImageView addMuni;


        public ProvinceViewHolder(View itemView) {
            super(itemView);
            provinceName = (TextView) itemView.findViewById(R.id.name);
            label = (TextView) itemView.findViewById(R.id.label);
            label.setText("Add Municipalities");
            addMuni = (ImageView) itemView.findViewById(R.id.addIcon);
        }

    }

    static final String LOG = ProvinceAdapter.class.getSimpleName();
}

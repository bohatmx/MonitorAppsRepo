package com.boha.monitor.library.fragments;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.boha.monitor.library.dto.GcmDeviceDTO;
import com.boha.platform.library.R;

import java.util.List;

public class GcmDeviceListAdapter extends RecyclerView.Adapter<GcmDeviceListAdapter.ViewHolder> {

    private final List<GcmDeviceDTO> mDeviceList;
    private final GcmDeviceFragment.GcmDeviceListener mListener;

    public GcmDeviceListAdapter(List<GcmDeviceDTO> deviceList, GcmDeviceFragment.GcmDeviceListener listener) {
        mDeviceList = deviceList;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_gcmdevice, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final GcmDeviceDTO dev = mDeviceList.get(position);
        holder.mModel.setText(dev.getModel());
        holder.mManufacturer.setText(dev.getManufacturer());
        if (dev.getStaff() != null) {
            holder.mPerson.setText(dev.getStaff().getFullName());
        }
        if (dev.getMonitor() != null) {
            holder.mPerson.setText(dev.getMonitor().getFullName());
        }

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    mListener.onDeviceClicked(dev);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mDeviceList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public  TextView mModel,mManufacturer,mPerson;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mModel = (TextView) view.findViewById(R.id.DEV_model);
            mManufacturer = (TextView) view.findViewById(R.id.DEV_manufacturer);
            mPerson = (TextView) view.findViewById(R.id.DEV_person);
        }


    }
}

package com.boha.monitor.library.adapters;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.boha.monitor.library.dto.LocationTrackerDTO;
import com.boha.monitor.library.util.Statics;
import com.boha.platform.library.R;
import com.squareup.picasso.Picasso;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by aubreyM on 14/12/17.
 */
public class LocationTrackerListAdapter extends RecyclerView.Adapter<LocationTrackerListAdapter.LocationTrackerViewHolder> {

    public interface LocationTrackerListListener {
        void onClicked(LocationTrackerDTO tracker);
    }

    private LocationTrackerListListener mListener;
    private List<LocationTrackerDTO> locationTrackerList;
    private Context ctx;
    int darkColor;

    public LocationTrackerListAdapter(List<LocationTrackerDTO> locationTrackerList, int darkColor,
                                      Context context, LocationTrackerListListener listener) {
        this.locationTrackerList = locationTrackerList;
        this.ctx = context;
        this.mListener = listener;
        this.darkColor = darkColor;
    }


    @Override
    public LocationTrackerViewHolder onCreateViewHolder( ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.location_tracker_item, parent, false);
        return new LocationTrackerViewHolder(v);
    }

    @Override
    public void onBindViewHolder( final LocationTrackerViewHolder holder, final int position) {

        final LocationTrackerDTO p = locationTrackerList.get(position);
        StringBuilder sb = new StringBuilder();
        if (p.getGcmDevice() != null) {
            sb.append(p.getGcmDevice().getManufacturer()).append(" ")
                    .append(p.getGcmDevice().getModel());
            holder.txtDevice.setText(sb.toString());
        } else {
            holder.txtDevice.setText("No Device Info");
        }
        if (p.getStaffName() != null) {
            holder.txtSubtitle.setText(p.getStaffName());
            holder.txtType.setText("Supervisor");
        }
        if (p.getMonitorName() != null) {
            holder.txtSubtitle.setText(p.getMonitorName());
            holder.txtType.setText("Monitor");
        }
        holder.txtDate.setText(sdf.format(new Date(p.getDateTracked())));
        if (p.getPhoto() != null) {
            holder.photo.setAlpha(1.0f);
            Picasso.with(ctx).load(p.getPhoto().getSecureUrl()).into(holder.photo);

        } else {
            holder.photo.setImageDrawable(ContextCompat.getDrawable(ctx, R.drawable.boy));
            holder.photo.setAlpha(0.3f);
        }

        holder.txtDevice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onClicked(p);
            }
        });
        holder.txtSubtitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onClicked(p);
            }
        });

        holder.photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onClicked(p);
            }
        });

        Statics.setRobotoFontLight(ctx, holder.txtDevice);


    }

    @Override
    public int getItemCount() {
        return locationTrackerList == null ? 0 : locationTrackerList.size();
    }

    static final Locale loc = Locale.getDefault();
    static final SimpleDateFormat sdf = new SimpleDateFormat("EEEE dd MMMM yyyy HH:mm", loc);
    static final DecimalFormat df = new DecimalFormat("###,###,###,###");

    public class LocationTrackerViewHolder extends RecyclerView.ViewHolder  {
        protected TextView txtDevice, txtType;
        protected CircleImageView photo;
        protected TextView txtSubtitle, txtDate;


        public LocationTrackerViewHolder(View itemView) {
            super(itemView);

            txtType = (TextView) itemView
                    .findViewById(R.id.LT_type);
            txtDevice = (TextView) itemView
                    .findViewById(R.id.LT_txtDevice);
            txtSubtitle = (TextView) itemView
                    .findViewById(R.id.LT_txtSubtitle);

            txtDate = (TextView) itemView
                    .findViewById(R.id.LT_txtDate);


            photo = (CircleImageView) itemView
                    .findViewById(R.id.LT_image);

        }

    }

    static final String LOG = LocationTrackerListAdapter.class.getSimpleName();
}

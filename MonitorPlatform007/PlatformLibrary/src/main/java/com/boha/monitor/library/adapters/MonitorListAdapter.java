package com.boha.monitor.library.adapters;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import com.boha.monitor.library.dto.MonitorDTO;
import com.boha.monitor.library.dto.PhotoUploadDTO;
import com.boha.monitor.library.util.Statics;
import com.boha.platform.library.R;
import com.squareup.picasso.Picasso;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by aubreyM on 14/12/17.
 */
public class MonitorListAdapter extends RecyclerView.Adapter<MonitorListAdapter.MonitorViewHolder> {

    public interface MonitorListener {
        void onMonitorNameClicked(MonitorDTO monitor);
        void onHighDefPhoto(PhotoUploadDTO photo, Integer monitorID);
    }

    private MonitorListener mListener;
    private List<MonitorDTO> monitorList;
    private Context ctx;
    int darkColor;

    public MonitorListAdapter(List<MonitorDTO> monitorList, int darkColor,
                              Context context, MonitorListener listener) {
        this.monitorList = monitorList;
        this.ctx = context;
        this.mListener = listener;
        this.darkColor = darkColor;
    }


    @Override
    public MonitorViewHolder onCreateViewHolder( ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.monitor_card, parent, false);
        return new MonitorViewHolder(v);
    }

    @Override
    public void onBindViewHolder( final MonitorViewHolder holder, final int position) {

        final MonitorDTO p = monitorList.get(position);
        holder.txtName.setText(p.getFullName());
        holder.statusCount.setText("" + p.getStatusCount());
        holder.projectCount.setText("" + p.getProjectCount());
        holder.photoCount.setText("" + p.getPhotoCount());

        holder.photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!p.getPhotoUploadList().isEmpty()) {
                    mListener.onHighDefPhoto(p.getPhotoUploadList().get(0),p.getMonitorID());
                }
            }
        });
        holder.txtName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onMonitorNameClicked(p);
            }
        });
        Statics.setRobotoFontLight(ctx, holder.txtName);

        if (p.getPhotoUploadList().isEmpty()) {
            holder.photo.setImageDrawable(ContextCompat.getDrawable(ctx,R.drawable.boy));
            holder.photo.setAlpha(0.3f);
        } else {
            holder.photo.setAlpha(1.0f);
            Picasso.with(ctx)
                    .load(p.getPhotoUploadList().get(0).getSecureUrl())
                    .into(holder.photo);
        }


    }

    @Override
    public int getItemCount() {
        return monitorList == null ? 0 : monitorList.size();
    }

    static final Locale loc = Locale.getDefault();
    static final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", loc);
    static final DecimalFormat df = new DecimalFormat("###,###,###,###");

    public class MonitorViewHolder extends RecyclerView.ViewHolder  {
        protected TextView txtName;
        protected CircleImageView photo;
        protected TextView statusCount, projectCount, photoCount;
        protected CheckBox checkBox;


        public MonitorViewHolder(View itemView) {
            super(itemView);

            txtName = (TextView) itemView
                    .findViewById(R.id.MON_txtName);
            statusCount = (TextView) itemView
                    .findViewById(R.id.MON_txtCountStatus);
            photoCount = (TextView) itemView
                    .findViewById(R.id.MON_txtCountPhotos);
            projectCount = (TextView) itemView
                    .findViewById(R.id.MON_txtCountProjects);


            photo = (CircleImageView) itemView
                    .findViewById(R.id.MON_imagex);
            checkBox = (CheckBox) itemView
                    .findViewById(R.id.MON_checkBox);

        }

    }

    static final String LOG = MonitorListAdapter.class.getSimpleName();
}

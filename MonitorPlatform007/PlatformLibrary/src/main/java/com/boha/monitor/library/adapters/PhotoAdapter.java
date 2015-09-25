package com.boha.monitor.library.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.boha.monitor.library.dto.PhotoUploadDTO;
import com.boha.monitor.library.dto.TaskStatusTypeDTO;
import com.boha.platform.library.R;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

/**
 * Created by aubreyM on 14/12/17.
 */
public class PhotoAdapter extends RecyclerView.Adapter<PhotoAdapter.PhotoViewHolder> {

    public interface PictureListener {
        public void onPictureClicked(PhotoUploadDTO photoUpload,int position);
    }

    private PictureListener listener;
    private List<PhotoUploadDTO> photoList;
    private Context ctx;
    private int imageType;
    public static final int THUMB = 1, FULL_IMAGE = 2;

    public PhotoAdapter(List<PhotoUploadDTO> photos,
                        int imageType,
                        Context context, PictureListener listener) {
        this.photoList = photos;
        this.ctx = context;
        this.imageType = imageType;
        this.listener = listener;
    }


    @Override
    public PhotoViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (imageType) {
            case FULL_IMAGE:
                return new PhotoViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.photo_item, parent, false));
            case THUMB:
                return new PhotoViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.photo_item_thumb, parent, false));
        }
        return null;
    }

    @Override
    public void onBindViewHolder(final PhotoViewHolder holder, final int position) {

        final PhotoUploadDTO p = photoList.get(position);
        final int num = photoList.size() - (position);

        holder.caption.setVisibility(View.GONE);
        holder.date.setText(sdf.format(p.getDateTaken()));
        holder.position = position;

        holder.image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                listener.onPictureClicked(p, position);

            }
        });

        if (p.getThumbFilePath() == null) {
            setRemoteImage(holder.image, p);
        } else {
            setLocalImage(holder.image, p);
        }
        hideColors(holder);
        if (p.getProjectTaskStatus() != null) {
            if (p.getProjectTaskStatus().getTaskStatusType() != null) {
                if (p.getProjectTaskStatus().getTaskStatusType().getStatusColor() == null) {
                    p.getProjectTaskStatus().getTaskStatusType().setStatusColor(
                            Short.parseShort("" + TaskStatusTypeDTO.STATUS_COLOR_AMBER));
                }
            }
            try {
                switch (p.getProjectTaskStatus().getTaskStatusType().getStatusColor()) {
                    case TaskStatusTypeDTO.STATUS_COLOR_AMBER:
                        holder.numberRed.setVisibility(View.GONE);
                        holder.numberAmber.setVisibility(View.VISIBLE);
                        holder.numberAmber.setText("" + num);
                        holder.numberGreen.setVisibility(View.GONE);
                        break;
                    case TaskStatusTypeDTO.STATUS_COLOR_RED:
                        holder.numberRed.setVisibility(View.VISIBLE);
                        holder.numberRed.setText("" + num);
                        holder.numberAmber.setVisibility(View.GONE);
                        holder.numberGreen.setVisibility(View.GONE);
                        break;
                    case TaskStatusTypeDTO.STATUS_COLOR_GREEN:
                        holder.numberRed.setVisibility(View.GONE);
                        holder.numberAmber.setVisibility(View.GONE);
                        holder.numberGreen.setVisibility(View.VISIBLE);
                        holder.numberGreen.setText("" + num);
                        break;
                    default:
                        holder.numberRed.setVisibility(View.GONE);
                        holder.numberAmber.setVisibility(View.VISIBLE);
                        holder.numberAmber.setText("" + num);
                        holder.numberGreen.setVisibility(View.GONE);
                        break;
                }
            } catch (Exception e) {
                holder.numberNone.setVisibility(View.VISIBLE);
                holder.numberNone.setText("" + num);
            }
        } else {
            holder.numberNone.setVisibility(View.VISIBLE);
            holder.numberNone.setText("" + num);
        }


    }

    private void hideColors(PhotoViewHolder holder) {
        holder.numberRed.setVisibility(View.GONE);
        holder.numberAmber.setVisibility(View.GONE);
        holder.numberGreen.setVisibility(View.GONE);
        holder.numberNone.setVisibility(View.GONE);
    }

    private void setLocalImage(final ImageView image, PhotoUploadDTO p) {
        File file = new File(p.getThumbFilePath());
        Picasso.with(ctx)
                .load(file)
                .fit()
                .into(image);
    }

    private void setRemoteImage(final ImageView image, PhotoUploadDTO p) {
        Picasso.with(ctx).load(p.getUri())
                .placeholder(R.drawable.back13)
                .fit()
                .into(image);
    }

    @Override
    public int getItemCount() {
        return photoList == null ? 0 : photoList.size();
    }

    static final Locale loc = Locale.getDefault();
    static final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", loc);

    public class PhotoViewHolder extends RecyclerView.ViewHolder {
        protected ImageView image;
        protected TextView caption, caption2, numberRed,numberGreen, numberAmber, numberNone, date;
        protected int position;


        public PhotoViewHolder(View itemView) {
            super(itemView);
            image = (ImageView) itemView.findViewById(R.id.PHOTO_image);
            caption = (TextView) itemView.findViewById(R.id.PHOTO_caption);
            caption2 = (TextView) itemView.findViewById(R.id.PHOTO_caption);
            numberRed = (TextView) itemView.findViewById(R.id.PHOTO_numberRed);
            numberAmber = (TextView) itemView.findViewById(R.id.PHOTO_numberAmber);
            numberGreen = (TextView) itemView.findViewById(R.id.PHOTO_numberGreen);
            numberNone = (TextView) itemView.findViewById(R.id.PHOTO_numberNone);
            date = (TextView) itemView.findViewById(R.id.PHOTO_date);
        }

    }

    static final String LOG = PhotoAdapter.class.getSimpleName();
}

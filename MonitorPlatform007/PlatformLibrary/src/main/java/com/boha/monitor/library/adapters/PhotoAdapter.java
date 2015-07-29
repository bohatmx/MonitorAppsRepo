package com.boha.monitor.library.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.boha.monitor.library.dto.PhotoUploadDTO;
import com.boha.platform.library.R;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

/**
 * Created by aubreyM on 14/12/17.
 */
public class PhotoAdapter extends RecyclerView.Adapter<PhotoAdapter.PhotoViewHolder> {

    public interface PictureListener {
        public void onPictureClicked(int position);
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
        holder.number.setText("" + num);
        holder.caption.setVisibility(View.GONE);
        holder.date.setText(sdf.format(p.getDateTaken()));
        holder.position = position;

        holder.image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                listener.onPictureClicked(position);

            }
        });

        if (p.getThumbFilePath() == null) {
            setRemoteImage(holder.image, p);
        } else {
            setLocalImage(holder.image,p);
        }


    }
    private void setLocalImage(final ImageView image, PhotoUploadDTO p) {
        File file = new File(p.getThumbFilePath());
//        Log.w(LOG, "## photo path: " + file.getAbsolutePath());
        ImageLoader.getInstance().displayImage(Uri.fromFile(file).toString(), image, new ImageLoadingListener() {
            @Override
            public void onLoadingStarted(String s, View view) {

            }

            @Override
            public void onLoadingFailed(String s, View view, FailReason failReason) {
                image.setImageDrawable(ContextCompat.getDrawable(ctx, R.drawable.under_construction));
            }

            @Override
            public void onLoadingComplete(String s, View view, Bitmap bitmap) {

            }

            @Override
            public void onLoadingCancelled(String s, View view) {

            }
        });
    }
    private void setRemoteImage(final ImageView image, PhotoUploadDTO p) {
        String url = p.getUri();
        Log.w(LOG, "## photo url: " + url);
        ImageLoader.getInstance().displayImage(url, image, new ImageLoadingListener() {
            @Override
            public void onLoadingStarted(String s, View view) {

            }

            @Override
            public void onLoadingFailed(String s, View view, FailReason failReason) {
                image.setImageDrawable(ContextCompat.getDrawable(ctx, R.drawable.under_construction));
            }

            @Override
            public void onLoadingComplete(String s, View view, Bitmap bitmap) {

            }

            @Override
            public void onLoadingCancelled(String s, View view) {

            }
        });
    }
    @Override
    public int getItemCount() {
        return photoList == null ? 0 : photoList.size();
    }

    static final Locale loc = Locale.getDefault();
    static final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", loc);

    public class PhotoViewHolder extends RecyclerView.ViewHolder {
        protected ImageView image;
        protected TextView caption, number, date;
        protected int position;


        public PhotoViewHolder(View itemView) {
            super(itemView);
            image = (ImageView) itemView.findViewById(R.id.PHOTO_image);
            caption = (TextView) itemView.findViewById(R.id.PHOTO_caption);
            number = (TextView) itemView.findViewById(R.id.PHOTO_number);
            date = (TextView) itemView.findViewById(R.id.PHOTO_date);
        }

    }

    static final String LOG = PhotoAdapter.class.getSimpleName();
}
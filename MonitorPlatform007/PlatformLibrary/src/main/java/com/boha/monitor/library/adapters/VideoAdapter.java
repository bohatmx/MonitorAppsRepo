package com.boha.monitor.library.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.boha.monitor.library.dto.VideoUploadDTO;
import com.boha.monitor.library.util.Statics;
import com.boha.platform.library.R;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubeThumbnailLoader;
import com.google.android.youtube.player.YouTubeThumbnailView;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

/**
 * Created by aubreyM on 14/12/17.
 */
public class VideoAdapter extends RecyclerView.Adapter<VideoAdapter.VideoViewHolder> {

    public interface VideoListener {
        public void onVideoClicked(VideoUploadDTO videoUpload, int position);
    }

    private VideoListener listener;
    private List<VideoUploadDTO> videoList;
    private Context ctx;
    public static final int THUMB = 1, FULL_IMAGE = 2;

    public VideoAdapter(List<VideoUploadDTO> photos,
                        Context context, VideoListener listener) {
        this.videoList = photos;
        this.ctx = context;
        this.listener = listener;
    }


    @Override
    public VideoViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new VideoViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.video_item, parent, false));
    }

    @Override
    public void onBindViewHolder(final VideoViewHolder holder, final int position) {

        final VideoUploadDTO p = videoList.get(position);
        holder.subTitle.setText(sdf.format(p.getDateTaken()));
        holder.projectName.setText(p.getProjectName());
        holder.position = position;


        holder.image.initialize(p.getYouTubeID(), new YouTubeThumbnailView.OnInitializedListener() {
            @Override
            public void onInitializationSuccess(YouTubeThumbnailView youTubeThumbnailView,
                                                final YouTubeThumbnailLoader loader) {
                loader.setOnThumbnailLoadedListener(new YouTubeThumbnailLoader.OnThumbnailLoadedListener() {
                    @Override
                    public void onThumbnailLoaded(YouTubeThumbnailView view, String s) {
                        holder.image.setImageDrawable(view.getDrawable());
                        loader.release();
                    }

                    @Override
                    public void onThumbnailError(YouTubeThumbnailView youTubeThumbnailView, YouTubeThumbnailLoader.ErrorReason errorReason) {

                    }
                });
                loader.setVideo(p.getYouTubeID());
            }

            @Override
            public void onInitializationFailure(YouTubeThumbnailView youTubeThumbnailView, YouTubeInitializationResult youTubeInitializationResult) {

            }
        });
        holder.main.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onVideoClicked(p, position);

            }
        });

        Statics.setRobotoFontRegular(ctx,holder.projectName);

    }


    @Override
    public int getItemCount() {
        return videoList == null ? 0 : videoList.size();
    }

    static final Locale loc = Locale.getDefault();
    static final SimpleDateFormat sdf = new SimpleDateFormat("EEEE, dd MMMM yyyy HH:mm", loc);

    public class VideoViewHolder extends RecyclerView.ViewHolder {
        protected YouTubeThumbnailView image;
        protected TextView projectName, subTitle;
        protected int position;
        protected View main;


        public VideoViewHolder(View itemView) {
            super(itemView);
            main = itemView.findViewById(R.id.main);
            image = (YouTubeThumbnailView) itemView.findViewById(R.id.icon);
            projectName = (TextView) itemView.findViewById(R.id.title);
            subTitle = (TextView) itemView.findViewById(R.id.subtitle);


        }

    }

    static final String LOG = VideoAdapter.class.getSimpleName();
}

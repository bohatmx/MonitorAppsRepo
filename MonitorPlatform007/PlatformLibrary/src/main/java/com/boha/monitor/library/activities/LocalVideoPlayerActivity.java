package com.boha.monitor.library.activities;

import android.app.Activity;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.VideoView;

import com.boha.monitor.library.dto.VideoUploadDTO;
import com.boha.platform.library.R;

import java.io.File;

public class LocalVideoPlayerActivity extends Activity {

    VideoView mVideoView;
    Chronometer chronometer;
    VideoUploadDTO  videoUpload;
    ImageView replay;
    View btnLayout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_local_video_player);
        videoUpload = (VideoUploadDTO)getIntent().getSerializableExtra("video");


        setFields();

    }

    private void setFields() {
        mVideoView = (VideoView)findViewById(R.id.videoView);
        chronometer = (Chronometer)findViewById(R.id.chronometer);
        btnLayout = findViewById(R.id.btnLayout);
        replay = (ImageView)findViewById(R.id.replay);


        File file = new File(videoUpload.getFilePath());
        Uri uri = Uri.fromFile(file);

        mVideoView.setMediaController(new MediaController(this));
        mVideoView.setVideoURI(uri);
        mVideoView.requestFocus();
        chronometer.start();
        mVideoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                chronometer.stop();
            }
        });
        replay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chronometer.start();
                mVideoView.start();

            }
        });

        mVideoView.start();

    }

}

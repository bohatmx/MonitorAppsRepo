package com.boha.monitor.library.activities;

import android.media.AudioManager;
import android.os.Bundle;
import android.media.MediaPlayer;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import com.boha.monitor.library.dto.VideoUploadDTO;
import com.boha.platform.library.R;

public class VideoPlayerActivity extends AppCompatActivity implements
        SurfaceHolder.Callback, MediaPlayer.OnPreparedListener{
    private MediaPlayer mediaPlayer;
    private SurfaceHolder vidHolder;
    private SurfaceView vidSurface;
    String vidAddress = "https://archive.org/download/ksnn_compilation_master_the_internet/ksnn_compilation_master_the_internet_512kb.mp4";
    private VideoUploadDTO videoUpload;
    static final String LOG = VideoPlayerActivity.class.getSimpleName();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_player);
        videoUpload = (VideoUploadDTO)getIntent().getSerializableExtra("video");
        if (videoUpload == null) {
            videoUpload = new VideoUploadDTO();
            videoUpload.setSecureUrl("https://res.cloudinary.com/bohatmx/video/upload/v1457655655/eazqs9fop5ero4dymcpe.mp4");
        }
        setFields();
        vidSurface = (SurfaceView) findViewById(R.id.surfView);
        vidHolder = vidSurface.getHolder();
        vidHolder.addCallback(this);
    }
    public void setFields(){

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.w(LOG,"..... surfaceCreated");
        try {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDisplay(vidHolder);
            mediaPlayer.setDataSource(videoUpload.getSecureUrl());
            mediaPlayer.prepare();
            mediaPlayer.setOnPreparedListener(this);
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

            mediaPlayer.setOnBufferingUpdateListener(new MediaPlayer.OnBufferingUpdateListener() {
                @Override
                public void onBufferingUpdate(MediaPlayer mp, int percent) {
                    //Log.w(LOG, "onBufferingUpdate, percent: " + percent);
                }
            });
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        Log.w(LOG,"..... onPrepared");
        mediaPlayer.start();
    }
}

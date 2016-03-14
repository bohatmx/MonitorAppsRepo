package com.boha.monitor.library.activities;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.os.Bundle;
import android.app.Activity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.boha.monitor.library.adapters.VideoAdapter;
import com.boha.monitor.library.dto.MonitorDTO;
import com.boha.monitor.library.dto.ResponseDTO;
import com.boha.monitor.library.dto.StaffDTO;
import com.boha.monitor.library.dto.VideoUploadDTO;
import com.boha.monitor.library.fragments.ProjectTaskListFragment;
import com.boha.monitor.library.util.MonLog;
import com.boha.monitor.library.util.SharedUtil;
import com.boha.monitor.library.util.SimpleDividerItemDecoration;
import com.boha.monitor.library.util.ThemeChooser;
import com.boha.monitor.library.util.Util;
import com.boha.platform.library.R;
import com.google.android.youtube.player.YouTubeBaseActivity;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerFragment;

import java.util.ArrayList;
import java.util.List;

public class YouTubePlayerActivity extends YouTubeBaseActivity
    implements YouTubePlayer.OnInitializedListener{

    YouTubePlayerFragment youTubePlayerFragment;
    YouTubePlayer youTubePlayer;
    Context ctx;
    List<VideoUploadDTO> videoList;
    List<String> stringList = new ArrayList<>();
    RecyclerView recycler;
    VideoAdapter adapter;

    static final String LOG = YouTubePlayerActivity.class.getSimpleName();
    public static final String
            STAFF_DEBUG_API_KEY = "AIzaSyBncjb7-oqexK0fKJdfy365tVJUNLkxRkE",
            STAFF_PROD_API_KEY = "AIzaSyDLKxbwrZO0kvsUWARNsm598QHXu0_ATX4",
            MONITOR_DEBUG_API_KEY = "AIzaSyCZ6MSdEkJLRcUpz1KnGU6hM1Fd1Gl9GXA",
            MONITOR_PROD_API_KEY = "AIzaSyCzkBfMREH3my1bgrXw5TcDew7sZpbLKFI";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeChooser.setTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_you_tube_player);
        ctx = getApplicationContext();

        ResponseDTO r = (ResponseDTO)getIntent().getSerializableExtra("videoList");
        if (r != null) {
            videoList = r.getVideoUploadList();
        }

        youTubePlayerFragment = (YouTubePlayerFragment)getFragmentManager()
                .findFragmentById(R.id.youtubeplayerfragment);

        recycler = (RecyclerView)findViewById(R.id.recycler);
        if (recycler != null) {
            LinearLayoutManager llm = new LinearLayoutManager(getApplicationContext());
            recycler.setLayoutManager(llm);
            recycler.addItemDecoration(new SimpleDividerItemDecoration(getApplicationContext()));
        } else {

        }
        youTubePlayerFragment.initialize(getAPIKey(),this);
        if (savedInstanceState != null) {
            MonLog.e(ctx,LOG,"savedInstanceState is NOT null, getting saved video list");
            r = (ResponseDTO)savedInstanceState.getSerializable("list");
            videoList = r.getVideoUploadList();
            position = savedInstanceState.getInt("position",0);
        }

    }

    private void setList() {
        if (videoList == null || videoList.isEmpty()) {
            return;
        }
        adapter = new VideoAdapter(videoList, getApplicationContext(), new VideoAdapter.VideoListener() {
            @Override
            public void onVideoClicked(VideoUploadDTO videoUpload, int index) {
                youTubePlayer.cueVideo(videoUpload.getYouTubeID());
                position = index;
            }
        });
        recycler.setAdapter(adapter);
        youTubePlayer.cueVideo(videoList.get(position).getYouTubeID());
    }

    int position;
    @Override
    public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer ytPlayer, boolean wasRestored) {
        MonLog.w(ctx,LOG, "onInitializationSuccess, wasRestored:  " + wasRestored);

        this.youTubePlayer = ytPlayer;
        youTubePlayer.setPlayerStateChangeListener(new MyPlayerStateChangeListener());
        youTubePlayer.setPlaybackEventListener(new MyPlaybackEventListener());

        setList();
        if (!wasRestored) {

        }
    }

    @Override
    public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult result) {

        MonLog.w(ctx,LOG, "onInitializationFailure: result.isUserRecoverableError: "
                + result.isUserRecoverableError());
        if (result.isUserRecoverableError()) {
            result.getErrorDialog(this,2).show();
            return;
        }
    }

    private String getAPIKey() {
        boolean isDebuggable = 0 != (ctx.getApplicationInfo().flags
                &= ApplicationInfo.FLAG_DEBUGGABLE);
        StaffDTO staff = SharedUtil.getCompanyStaff(ctx);
        MonitorDTO mon = SharedUtil.getMonitor(ctx);
        if (staff != null) {
            if (isDebuggable) {
                return STAFF_DEBUG_API_KEY;
            } else {
                return STAFF_PROD_API_KEY;
            }
        }
        if (mon != null) {
            if (isDebuggable) {
                return MONITOR_DEBUG_API_KEY;
            } else {
                return MONITOR_PROD_API_KEY;
            }
        }


        return null;
    }

    private final class MyPlayerStateChangeListener implements YouTubePlayer.PlayerStateChangeListener {


        @Override
        public void onAdStarted() {
            MonLog.w(ctx,LOG, "********* onAdStarted ");
        }

        @Override
        public void onError(
                com.google.android.youtube.player.YouTubePlayer.ErrorReason reason) {
            MonLog.w(ctx,LOG, "********* onError: " + reason.toString());
        }

        @Override
        public void onLoaded(String arg0) {
            MonLog.w(ctx,LOG, "********* onLoaded: " + arg0);
        }

        @Override
        public void onLoading() {
            MonLog.w(ctx,LOG, "********* onLoading ");
        }

        @Override
        public void onVideoEnded() {
            MonLog.w(ctx,LOG, "********* onVideoEnded ");
        }

        @Override
        public void onVideoStarted() {
            MonLog.w(ctx,LOG, "********* onVideoStarted ");
        }

    }

    private final class MyPlaybackEventListener implements YouTubePlayer.PlaybackEventListener {


        @Override
        public void onBuffering(boolean arg0) {
            MonLog.w(ctx,LOG, "onBuffering: " + arg0);
        }

        @Override
        public void onPaused() {
            MonLog.w(ctx,LOG, "%%%%%% onPaused ");
        }

        @Override
        public void onPlaying() {
            MonLog.w(ctx,LOG, "%%%%%% onPlaying ");
        }

        @Override
        public void onSeekTo(int arg0) {
            MonLog.w(ctx,LOG, "%%%%%% onSeekTo ");
        }

        @Override
        public void onStopped() {
            MonLog.w(ctx,LOG, "%%%%%% onStopped ");
        }

    }


    @Override
    public void onSaveInstanceState(Bundle b) {
        ResponseDTO w = new ResponseDTO();
        w.setVideoUploadList(videoList);
        b.putSerializable("list",w);
        b.putInt("position",position);
        super.onSaveInstanceState(b);
    }
    @Override
    public void onStop() {
        super.onStop();
        try {
            youTubePlayer.release();
            Log.e(LOG,"youTubePlayer has been released");
        } catch (Exception e) {}
    }
}

package com.boha.monitor.library.activities;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.Spinner;
import android.widget.VideoView;

import com.boha.monitor.library.dto.ProjectDTO;
import com.boha.monitor.library.dto.ResponseDTO;
import com.boha.monitor.library.dto.VideoUploadDTO;
import com.boha.monitor.library.util.Snappy;
import com.boha.monitor.library.util.Util;
import com.boha.platform.library.R;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class LocalVideoPlayerActivity extends Activity {

    VideoView mVideoView;
    Chronometer chronometer;
    VideoUploadDTO videoUpload;
    ImageView replay;
    View btnLayout;
    Context ctx;
    List<File> fileList;
    List<ProjectDTO> projectList;
    List<String> nameList;
    Spinner spinner;
    ImageView playIcon;
    View spinnerLayout;
    static final String LOG = LocalVideoPlayerActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ctx = getApplicationContext();
        setContentView(R.layout.activity_local_video_player);
        videoUpload = (VideoUploadDTO) getIntent().getSerializableExtra("video");
        mVideoView = (VideoView) findViewById(R.id.videoView);
        chronometer = (Chronometer) findViewById(R.id.chronometer);
        spinner = (Spinner)findViewById(R.id.spinner);
        playIcon = (ImageView)findViewById(R.id.play);
        btnLayout = findViewById(R.id.btnLayout);
        replay = (ImageView) findViewById(R.id.replay);
        spinnerLayout = findViewById(R.id.spinnerLayout);

        playIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedFile != null) {
                    playVideo(selectedFile);
                }
            }
        });

        if (videoUpload != null) {
            spinnerLayout.setVisibility(View.GONE);
            File file = new File(videoUpload.getFilePath());
            playVideo(file);
            return;
        }
        spinnerLayout.setVisibility(View.VISIBLE);
        getFiles();

    }

    private void playVideo(File file) {
        Uri uri = Uri.fromFile(file);

        mVideoView.setMediaController(new MediaController(this));
        mVideoView.setVideoURI(uri);
        mVideoView.requestFocus();
        chronometer.setBase(SystemClock.elapsedRealtime());
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
                chronometer.setBase(SystemClock.elapsedRealtime());
                chronometer.start();
                mVideoView.start();

            }
        });

        mVideoView.start();

    }

    private void getFiles() {
        if (ContextCompat.checkSelfPermission(ctx,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            Log.d(LOG, "WRITE_EXTERNAL_STORAGE permission not granted yet");
            return;
        }

        File root;
        if (Util.hasStorage(true)) {
            Log.i(LOG, "###### get file from getExternalStoragePublicDirectory");
            root = Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_PICTURES);
        } else {
            Log.i(LOG, "###### get file from getDataDirectory");
            root = Environment.getDataDirectory();
        }
        File dir = new File(root, "monitor");
        fileList = new ArrayList<>();
        File[] files = dir.listFiles();
        for (File f: files) {
            fileList.add(f);
        }
        nameList = new ArrayList<>();
        Snappy.getProjectList((MonApp) getApplication(), new Snappy.SnappyReadListener() {
            @Override
            public void onDataRead(ResponseDTO response) {
                projectList = response.getProjectList();

                for (File f: fileList) {
                    if (f.length() == 0) {
                        Log.e(LOG,"--------------------------- empty file");
                        continue;
                    }
                    ProjectDTO p = findProject(f);
                    if (p != null)
                        nameList.add(findProject(f).getProjectName());
                    else
                        nameList.add(f.getName());
                }
                loadSpinner();

            }

            @Override
            public void onError(String message) {

            }
        });
    }
    File selectedFile;
    private void loadSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                getApplicationContext(),android.R.layout.simple_spinner_item,nameList);
        nameList.add(0,"Please select Video");
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    selectedFile = null;
                    return;
                }
                selectedFile = fileList.get(position - 1);
                playVideo(selectedFile);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }
    private ProjectDTO findProject(File f) {
        int i = f.getName().indexOf("-");
        String id = f.getName().substring(0,i);
        try {
            Integer projectID = Integer.parseInt(id);
            for (ProjectDTO p : projectList) {
                if (p.getProjectID().intValue() == projectID.intValue()) {
                    return p;
                }
            }
        } catch (NumberFormatException e) {
            return null;
        }
        return null;
    }
}

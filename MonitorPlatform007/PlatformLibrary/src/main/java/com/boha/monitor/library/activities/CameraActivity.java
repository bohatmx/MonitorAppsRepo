

package com.boha.monitor.library.activities;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.boha.monitor.library.dto.ProjectDTO;
import com.boha.monitor.library.dto.VideoUploadDTO;
import com.boha.monitor.library.services.YouTubeService;
import com.boha.monitor.library.util.MonLog;
import com.boha.monitor.library.util.SharedUtil;
import com.boha.monitor.library.util.ThemeChooser;
import com.boha.monitor.library.util.Util;
import com.boha.platform.library.R;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;

import java.io.File;

public class CameraActivity extends AppCompatActivity implements Camera2VideoFragment.CameraListener {
    Camera2VideoFragment videoFragment;
    Context ctx;
    Account account;
    AccountManager accountManager;
    ProjectDTO project;
    private final String SCOPE = "https://www.googleapis.com/auth/youtube";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeChooser.setTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        ctx = getApplicationContext();
        project = (ProjectDTO) getIntent().getSerializableExtra("project");
        if (null == savedInstanceState) {
            videoFragment = Camera2VideoFragment.newInstance(project);
            //todo remove project instance
            videoFragment.setMonApp((MonApp) getApplication());
            getFragmentManager().beginTransaction()
                    .replace(R.id.container, videoFragment)
                    .commit();
        }
        chooseAccount();
    }

    private Account chooseAccount() {
        accountManager = AccountManager.get(ctx);
        Account[] accounts = accountManager.getAccountsByType("com.google");
        if (accounts.length > 0) {
            account = accounts[0];
            MonLog.w(ctx, LOG, "##### account to be used: " + account.name);
            accountManager.getAuthToken(account, "oauth2:" + SCOPE, null, this,
                    new OnTokenAcquired(), null);
            return accounts[0];
        } else {
            Util.showErrorToast(ctx, "No Google account found on the device. Cannot continue.");
        }
        return null;
    }


    private static final int AUTHORIZATION_CODE = 1993;
    private String authToken;
    static final String LOG = CameraActivity.class.getSimpleName();
    GoogleCredential credential;

    private void getCreds() throws Exception {
        Log.d(LOG, "##### getCreds for YouTube");
        credential = new GoogleCredential().setAccessToken(authToken);
        final YouTube youtube = new YouTube.Builder(new NetHttpTransport(),
                new JacksonFactory(), credential)
                .setApplicationName("com.boha.Monitor.App").build();

    }

    @Override
    public void onUploadRequired(VideoUploadDTO video, File file) {
        MonLog.w(getApplicationContext(),LOG,"------- onUploadRequired - file length: " + file.length());
        if (isBound) {
            youTubeService.uploadVideo(video, file, new YouTubeService.YouTubeListener() {
                @Override
                public void onVideoUploaded() {
                    MonLog.i(getApplicationContext(),LOG,"Yeahhh! Video gone to YouTube");
                }

                @Override
                public void onError() {

                }
            });
        } else {

        }
    }

    private class OnTokenAcquired implements AccountManagerCallback<Bundle> {

        @Override
        public void run(AccountManagerFuture<Bundle> result) {
            try {
                Bundle bundle = result.getResult();
                Intent launch = (Intent) bundle.get(AccountManager.KEY_INTENT);
                if (launch != null) {
                    startActivityForResult(launch, AUTHORIZATION_CODE);
                } else {
                    Log.w(LOG, "+++++++++++++++++ Token has been acquired");
                    authToken = bundle
                            .getString(AccountManager.KEY_AUTHTOKEN);

                    SharedUtil.saveAuthToken(getApplicationContext(), authToken);
                    getCreds();
                }

            } catch (Exception e) {
                Util.showErrorToast(getApplicationContext(), "Unable to get YouTube authorisation token");
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        MonLog.w(getApplicationContext(),LOG,"onStart - binding to YouTubeService");
        Intent intent = new Intent(this, YouTubeService.class);
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (isBound) {
            MonLog.w(getApplicationContext(),LOG,"onDestroy - unbinding from YouTubeService");
            unbindService(serviceConnection);
            isBound = false;
        }
    }

    YouTubeService youTubeService;
    boolean isBound = false;

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MonLog.w(getApplicationContext(),LOG,"onServiceConnected - YouTubeService bound");
            YouTubeService.MyBinder binder = (YouTubeService.MyBinder) service;
            youTubeService = binder.getService();
            isBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            MonLog.w(getApplicationContext(),LOG,"onServiceDisconnected - YouTubeService");
            youTubeService = null;
            isBound = false;
        }
    };

}

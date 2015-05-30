package com.boha.monitor.library.services;

import android.app.IntentService;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.boha.monitor.dto.ProjectDTO;
import com.boha.monitor.dto.RequestDTO;
import com.boha.monitor.dto.ResponseDTO;
import com.boha.monitor.library.util.NetUtil;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class StatusSyncService extends IntentService {

    public StatusSyncService() {
        super("StatusSyncService");
    }

    private final IBinder mBinder = new LocalBinder();
    @Override
    protected void onHandleIntent( Intent intent) {
        Log.e(LOG,"StatusSyncService onHandleIntent ...");

        if (intent != null) {
//            project = (ProjectDTO)intent.getSerializableExtra("project");
//            projectSiteList = project.getProjectSiteList();
//            if (projectSiteList == null || projectSiteList.isEmpty()) {
//                Log.d(LOG,"***** No sites defined for project, quitting");
//                return;
//            } else {
//                Log.w(LOG,"*** Starting status sync for sites: " + projectSiteList.size());
//            }
            //
            index = 0;
            start = System.currentTimeMillis();
            controlRequests();

        }
    }

    long start, end;
    private void controlRequests() {
//        if (index == projectSiteList.size()) {
//            end = System.currentTimeMillis();
//            Log.e(LOG, "########## StatusSyncService completed, elapsed: "
//                    + Util.getElapsed(start, end) + " seconds, sites sync'd: " + index);
//            statusSyncServiceListener.onStatusSyncComplete("" + index + " sync'd." );
//            return;
//        }
//        if (index < projectSiteList.size()) {
////            getSiteStatus(projectSiteList.get(index));
//        }
    }
    private void getProjectStatus( ProjectDTO site) {
        RequestDTO w = new RequestDTO(RequestDTO.GET_PROJECT_STATUS);
        w.setProjectID(site.getProjectID());
        NetUtil.sendRequest(getApplicationContext(), w, new NetUtil.NetUtilListener() {
            @Override
            public void onResponse(final ResponseDTO response) {

                if (response.getStatusCode() == 0) {
                    final ProjectDTO ss = response.getProjectList().get(0);
                    Log.i(LOG, "*** onMessage GET_PROJECT_STATUS status: " + response.getStatusCode() + " site: " + ss.getProjectName());
//                    CacheUtil.cache.....
                    index++;
                    controlRequests();
                }
            }

            @Override
            public void onError(final String message) {
                Log.e(LOG, "--- ERROR - " + message);
            }

            @Override
            public void onWebSocketClose() {

            }
        });

    }
    int index;
    ProjectDTO project;
    StatusSyncServiceListener statusSyncServiceListener;
    static final String LOG = StatusSyncService.class.getSimpleName();
    public class LocalBinder extends Binder {

        public StatusSyncService getService() {
            return StatusSyncService.this;
        }
    }


    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public int getSitesCompleted() {
        return index;
    }
    public void startSyncService(Intent intent, StatusSyncServiceListener listener) {
        statusSyncServiceListener = listener;
        onHandleIntent(intent);
    }

    public interface StatusSyncServiceListener {
        public void onStatusSyncComplete(String message);
    }
}

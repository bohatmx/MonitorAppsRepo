package com.com.boha.monitor.library.services;

import android.app.IntentService;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.com.boha.monitor.library.dto.ProjectDTO;
import com.com.boha.monitor.library.dto.ProjectSiteDTO;
import com.com.boha.monitor.library.dto.transfer.RequestDTO;
import com.com.boha.monitor.library.dto.transfer.ResponseDTO;
import com.com.boha.monitor.library.util.CacheUtil;
import com.com.boha.monitor.library.util.Statics;
import com.com.boha.monitor.library.util.Util;
import com.com.boha.monitor.library.util.WebSocketUtil;

import java.util.List;

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
    protected void onHandleIntent(Intent intent) {
        Log.e(LOG,"StatusSyncService onHandleIntent ...");

        if (intent != null) {
            project = (ProjectDTO)intent.getSerializableExtra("project");
            projectSiteList = project.getProjectSiteList();
            if (projectSiteList == null || projectSiteList.isEmpty()) {
                Log.d(LOG,"***** No sites defined for project, quitting");
                return;
            } else {
                Log.w(LOG,"*** Starting status sync for sites: " + projectSiteList.size());
            }
            //
            index = 0;
            start = System.currentTimeMillis();
            controlRequests();

        }
    }

    long start, end;
    private void controlRequests() {
        if (index == projectSiteList.size()) {
            end = System.currentTimeMillis();
            Log.e(LOG, "########## StatusSyncService completed, elapsed: " + Util.getElapsed(start, end) + " seconds");
            return;
        }
        if (index < projectSiteList.size()) {
            getSiteStatus(projectSiteList.get(index));
        }
    }
    private void getSiteStatus(ProjectSiteDTO site) {
        RequestDTO w = new RequestDTO(RequestDTO.GET_SITE_STATUS);
        w.setProjectSiteID(site.getProjectSiteID());

        WebSocketUtil.sendRequest(getApplicationContext(), Statics.COMPANY_ENDPOINT, w, new WebSocketUtil.WebSocketListener() {
            @Override
            public void onMessage(ResponseDTO response) {
                if (response.getStatusCode() == 0) {
                    CacheUtil.cacheSiteData(getApplicationContext(),response.getProjectSiteList().get(0), null);
                    index++;
                    controlRequests();
                }
            }

            @Override
            public void onClose() {

            }

            @Override
            public void onError(String message) {

            }
        });
    }
    int index;
    ProjectDTO project;
    List<ProjectSiteDTO> projectSiteList;
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
    public void startSyncService(Intent intent) {
        onHandleIntent(intent);
    }

}

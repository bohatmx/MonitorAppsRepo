package com.boha.monitor.library.services;

import android.app.IntentService;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.boha.monitor.library.dto.MonitorDTO;
import com.boha.monitor.library.dto.ProjectDTO;
import com.boha.monitor.library.dto.ResponseDTO;
import com.boha.monitor.library.dto.StaffDTO;
import com.boha.monitor.library.dto.TaskStatusTypeDTO;
import com.boha.monitor.library.util.CacheUtil;

import java.util.List;

import hugo.weaving.DebugLog;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class CachingService extends IntentService {

    public CachingService() {
        super("CachingService");
    }

    static final String LOG = CachingService.class.getSimpleName();

    @DebugLog
    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            ResponseDTO response = (ResponseDTO) intent.getSerializableExtra("response");
            if (response.getProjectList() != null) {
                startCachingProjects(response,null);
            }
            if (response.getStaffList() != null) {
                startCachingStaff(response,null);
            }
            if (response.getMonitorList() != null) {
                startCachingMonitors(response,null);
            }
            if (response.getTaskStatusTypeList() != null) {
                startCachingTaskStatusTypes(response,null);
            }
        } else {
            if (cachingListener instanceof CacheProjectsListener) {
                CacheProjectsListener listener = (CacheProjectsListener)cachingListener;
                startCachingProjects(res, listener);
            }
            if (cachingListener instanceof CacheStaffListener) {
                CacheStaffListener listener = (CacheStaffListener)cachingListener;
                startCachingStaff(res, listener);
            }
            if (cachingListener instanceof CacheMonitorListener) {
                CacheMonitorListener listener = (CacheMonitorListener)cachingListener;
                startCachingMonitors(res, listener);
            }
            if (cachingListener instanceof CacheTaskStatusListener) {
                CacheTaskStatusListener listener = (CacheTaskStatusListener)cachingListener;
                startCachingTaskStatusTypes(res, listener);
            }
        }

    }
    CachingListener cachingListener;
    ResponseDTO res;
    public void doCache(ResponseDTO response, CachingListener listener) {
        cachingListener = listener;
        res = response;
        onHandleIntent(null);
    }
    @DebugLog
    private void startCachingProjects(ResponseDTO response, final CacheProjectsListener listener) {
        if (response.getProjectList() != null) {
            CacheUtil.cacheProjectList(response.getProjectList(), new CacheUtil.CacheUtilListener() {
                @Override
                public void onFileDataDeserialized(ResponseDTO response) {

                }

                @Override
                public void onDataCached() {
                    Log.w(LOG, "cacheProjectList onDataCached");
                    if (listener != null) {
                        CacheUtil.getCachedProjectsLite(new CacheUtil.ProjectListener() {
                            @Override
                            public void onProjectReturned(ProjectDTO project) {

                            }

                            @Override
                            public void onProjectsReturned(List<ProjectDTO> list) {
                                listener.onCachingProjectsDone(list);
                            }

                            @Override
                            public void onCacheCleared(int count) {

                            }
                        });
                    }
                }

                @Override
                public void onError() {

                }
            });
        }
    }
    @DebugLog
    private void startCachingMonitors(ResponseDTO response, final CacheMonitorListener listener) {
        if (response.getMonitorList() != null) {
            CacheUtil.cacheMonitorList(response, new CacheUtil.CacheUtilListener() {
                @Override
                public void onFileDataDeserialized(ResponseDTO response) {

                }

                @Override
                public void onDataCached() {

                    Log.w(LOG, "updateMonitorList, onDataCached");
                    if (listener != null) {
                        CacheUtil.getCachedMonitorList(new CacheUtil.CacheUtilListener() {
                            @Override
                            public void onFileDataDeserialized(ResponseDTO response) {
                                listener.onCachingMonitorsDone(response.getMonitorList());
                            }

                            @Override
                            public void onDataCached() {

                            }

                            @Override
                            public void onError() {

                            }
                        });
                    }
                }

                @Override
                public void onError() {

                }
            });
        }
    }
    @DebugLog
    private void startCachingStaff(ResponseDTO response, final CacheStaffListener listener) {

        if (response.getStaffList() != null) {
            CacheUtil.cacheStaffList(response, new CacheUtil.CacheUtilListener() {
                @Override
                public void onFileDataDeserialized(ResponseDTO response) {

                }

                @Override
                public void onDataCached() {
                    Log.w(LOG, "updateStaffList");
                    if (listener != null) {
                        CacheUtil.getCachedStaffList(getApplicationContext(), new CacheUtil.CacheUtilListener() {
                            @Override
                            public void onFileDataDeserialized(ResponseDTO response) {
                                listener.onCachingStaffListDone(response.getStaffList());
                            }

                            @Override
                            public void onDataCached() {

                            }

                            @Override
                            public void onError() {

                            }
                        });
                    }
                }

                @Override
                public void onError() {

                }
            });
        }


    }

    private void startCachingTaskStatusTypes(ResponseDTO response, final CacheTaskStatusListener listener) {
        if (response.getTaskStatusTypeList() != null) {
            CacheUtil.cacheTaskStatusTypes(response, new CacheUtil.CacheUtilListener() {
                @Override
                public void onFileDataDeserialized(ResponseDTO response) {

                }

                @Override
                public void onDataCached() {

                    Log.w(LOG, "cacheTaskStatusTypes onDataCached");
                    if (listener != null) {
                        CacheUtil.getCachedTaskStatusTypes(new CacheUtil.CacheUtilListener() {
                            @Override
                            public void onFileDataDeserialized(ResponseDTO response) {
                                listener.onCachingStatusTypesDone(response.getTaskStatusTypeList());
                            }

                            @Override
                            public void onDataCached() {

                            }

                            @Override
                            public void onError() {

                            }
                        });

                    }
                }

                @Override
                public void onError() {

                }
            });
        }
    }

    public interface CachingListener {

    }
    public interface CacheProjectsListener extends CachingListener {
        void onCachingProjectsDone(List<ProjectDTO> list);


    }
    public interface CacheStaffListener extends CachingListener {
        void onCachingStaffListDone(List<StaffDTO> list);

    }
    public interface CacheMonitorListener extends CachingListener {
        void onCachingMonitorsDone(List<MonitorDTO> list);
    }
    public interface CacheTaskStatusListener extends CachingListener {
        void onCachingStatusTypesDone(List<TaskStatusTypeDTO> list);

    }
    public class LocalBinder extends Binder {

        public CachingService getService() {
            return CachingService.this;
        }

    }


    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    private final IBinder mBinder = new LocalBinder();
}

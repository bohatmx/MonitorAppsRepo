package com.boha.monitor.library.util;

import android.os.AsyncTask;
import android.util.Log;

import com.boha.monitor.library.activities.MonApp;
import com.boha.monitor.library.dto.LocationTrackerDTO;
import com.boha.monitor.library.dto.MonitorDTO;
import com.boha.monitor.library.dto.PhotoUploadDTO;
import com.boha.monitor.library.dto.ProjectDTO;
import com.boha.monitor.library.dto.RequestDTO;
import com.boha.monitor.library.dto.ResponseDTO;
import com.boha.monitor.library.dto.StaffDTO;
import com.boha.monitor.library.dto.TaskDTO;
import com.boha.monitor.library.dto.TaskStatusTypeDTO;
import com.boha.monitor.library.dto.VideoUploadDTO;
import com.google.gson.Gson;
import com.snappydb.DB;
import com.snappydb.SnappydbException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Created by aubreymalabie on 2/18/16.
 */
public class Snappy {


    public interface SnappyWriteListener {
        void onDataWritten();

        void onError(String message);
    }

    public interface SnappyDeleteListener {
        void onDataDeleted();

        void onError(String message);
    }

    public interface SnappyReadListener {
        void onDataRead(ResponseDTO response);

        void onError(String message);
    }

    public interface SnappyProjectListener {
        void onProjectFound(ProjectDTO project);

        void onError();
    }

    static final Gson gson = new Gson();
    public static final String PROJECT = "project", REQUEST = "request",
            PROJECT_LITE = "projectlite", STAFF = "staffs", TASK_STATUS_TYPES = "taskstatustypes",
            TASKS = "tasks", PHOTO_FOR_UPLOAD = "photoForUpload",
            PHOTO = "photo",
            MONITOR = "monitors", LOG = Snappy.class.getSimpleName(), DB_NAME = "monDatabase";

    static SnappyWriteListener writeListener;
    static final int CACHE_REQUEST = 1, GET_REQUESTS = 2, DELETE_REQUEST = 3, DELETE_REQUESTS = 4;
    static RequestDTO request;
    static ResponseDTO response = new ResponseDTO();


    public static void writeProjectList(final MonApp monApp, final List<ProjectDTO> list, final SnappyWriteListener listener) {
        Snappy.monApp = monApp;
        writeListener = listener;
        getDatabase(monApp);
        new WriteProjectTask().execute(list);

    }

    static class WriteProjectTask extends AsyncTask<List<ProjectDTO>, Void, Integer> {

        @Override
        protected Integer doInBackground(List<ProjectDTO>... params) {
            List<ProjectDTO> list = params[0];
            try {
                if (!snappydb.isOpen()) {
                    snappydb = monApp.getSnappyDB();
                }
                for (ProjectDTO p : list) {
                    snappydb.put(PROJECT + p.getProjectID(), p);
                    ProjectDTO d = new ProjectDTO();
                    d.setProjectID(p.getProjectID());
                    d.setProjectName(p.getProjectName());
                    d.setCityName(p.getCityName());
                    d.setMunicipalityName(p.getMunicipalityName());
                    d.setProgrammeName(p.getProgrammeName());
                    d.setLatitude(p.getLatitude());
                    d.setLongitude(p.getLongitude());
                    d.setLocationConfirmed(p.getLocationConfirmed());
                    d.setStatusCount(p.getStatusCount());
                    d.setPhotoCount(p.getPhotoCount());
                    d.setMonitorCount(p.getMonitorCount());
                    d.setStaffCount(p.getStaffCount());
                    d.setVideoCount(p.getVideoCount());
                    d.setProjectTaskCount(p.getProjectTaskCount());
                    d.setLastStatus(p.getLastStatus());
                    snappydb.put(PROJECT_LITE + p.getProjectID(), d);
//                    android.util.Log.w(LOG, "Project written: " + p.getProjectName()
//                            + " tasks: " + p.getProjectTaskList().size());
                }
                android.util.Log.d(LOG, "Projects written: " + list.size());

            } catch (Exception e) {
                Log.e(LOG, "Failed WriteProjectTask", e);
                return 9;
            }
            return 0;
        }

        @Override
        protected void onPostExecute(Integer result) {
            if (result == 0) {
                writeListener.onDataWritten();
            } else {
                writeListener.onError("Failed to write project");
            }
        }
    }

    static SnappyReadListener readListener;

    public static void getProjectList(final MonApp ctx, final SnappyReadListener listener) {
        getDatabase(ctx);
        readListener = listener;
        new PTask().execute();

    }

    private static class PTask extends AsyncTask<Void, Void, List<ProjectDTO>> {

        @Override
        protected List<ProjectDTO> doInBackground(Void... params) {
            List<ProjectDTO> pList = null;
            try {
                if (!snappydb.isOpen()) {
                    snappydb = monApp.getSnappyDB();
                }
                String[] keys = snappydb.findKeys(PROJECT_LITE);
                pList = new ArrayList<>(keys.length);
                for (String key : keys) {
                    ProjectDTO dto = snappydb.getObject(key, ProjectDTO.class);
                    pList.add(dto);
                }
            } catch (SnappydbException e) {
                Log.e(LOG, "Crapped out gettting projects: ", e);
                return null;
            }
            return pList;
        }

        @Override
        protected void onPostExecute(List<ProjectDTO> list) {
            if (list == null) {
                readListener.onError("Failed to get projects: ");
                return;
            }
            ResponseDTO w = new ResponseDTO();
            w.setProjectList(list);
            readListener.onDataRead(w);
            android.util.Log.d(LOG, "Read projects from Snappy: " + w.getProjectList().size());


        }

    }

    public static void writeStaffList(final MonApp ctx, final List<StaffDTO> list, final SnappyWriteListener listener) {
        getDatabase(ctx);
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                ResponseDTO r = new ResponseDTO();
                r.setStaffList(list);
                try {
                    //DB snappydb = getDatabase(ctx);
                    String json = gson.toJson(r);
                    snappydb.put(STAFF, json);
//                    snappydb.close();
                    android.util.Log.d(LOG, "Staff written: " + list.size());
                    listener.onDataWritten();

                } catch (SnappydbException e) {
                    listener.onError(e.getMessage());
                }
            }
        });


    }

    public static void getStaffList(final MonApp ctx, final SnappyReadListener listener) {
        getDatabase(ctx);
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                ResponseDTO r = new ResponseDTO();
                r.setStaffList(new ArrayList<StaffDTO>());
                try {
                    //DB snappydb = getDatabase(ctx);
                    String json = snappydb.get(STAFF);
                    r = gson.fromJson(json, ResponseDTO.class);
//                    snappydb.close();
                    android.util.Log.d(LOG, "Staff read: " + r.getStaffList().size());
                    listener.onDataRead(r);

                } catch (SnappydbException e) {
                    listener.onError(e.getMessage());
                }
            }
        });

    }

    public static void addStaff(final MonApp ctx, final StaffDTO staff,
                                final SnappyWriteListener listener) {

        getStaffList(ctx, new SnappyReadListener() {
            @Override
            public void onDataRead(ResponseDTO response) {
                if (response.getStaffList() == null) {
                    response.setStaffList(new ArrayList<StaffDTO>());
                }
                response.getStaffList().add(staff);
                writeStaffList(ctx, response.getStaffList(), listener);
            }

            @Override
            public void onError(String message) {

            }
        });

    }

    public static void addMonitor(final MonApp ctx, final MonitorDTO monitor,
                                  final SnappyWriteListener listener) {

        getMonitorList(ctx, new SnappyReadListener() {
            @Override
            public void onDataRead(ResponseDTO response) {
                if (response.getMonitorList() == null) {
                    response.setMonitorList(new ArrayList<MonitorDTO>());
                }
                response.getMonitorList().add(monitor);
                writeMonitorList(ctx, response.getMonitorList(), listener);
            }

            @Override
            public void onError(String message) {

            }
        });

    }

    public static void writeMonitorList(final MonApp ctx, final List<MonitorDTO> list, final SnappyWriteListener listener) {
        getDatabase(ctx);
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                ResponseDTO r = new ResponseDTO();
                r.setMonitorList(list);
                try {
                    //DB snappydb = getDatabase(ctx);
                    String json = gson.toJson(r);
                    snappydb.put(MONITOR, json);
//                    snappydb.close();
                    android.util.Log.d(LOG, "Monitor written: " + list.size());
                    listener.onDataWritten();

                } catch (SnappydbException e) {
                    listener.onError(e.getMessage());
                }
            }
        });


    }

    static final int ADD = 1, DELETE = 2, LIST = 3;

    static class GetMonitorsTask extends AsyncTask<Void, Void, ResponseDTO> {

        @Override
        protected ResponseDTO doInBackground(Void... params) {
            ResponseDTO r = new ResponseDTO();

            try {
                //DB snappydb = getDatabase(monApp);
                String json = snappydb.get(MONITOR);
                r = gson.fromJson(json, ResponseDTO.class);
//                snappydb.close();
                android.util.Log.d(LOG, "Monitors read: " + r.getMonitorList().size());

            } catch (SnappydbException e) {
                return null;
            }


            return r;
        }

        @Override
        protected void onPostExecute(ResponseDTO p) {
            if (readListener != null) {
                if (p != null) {
                    readListener.onDataRead(p);
                } else {
                    readListener.onError("Failed to get requests");
                }
            }

        }
    }

    static DB snappydb;

    static class ProjectTask extends AsyncTask<Integer, Void, ProjectDTO> {

        @Override
        protected ProjectDTO doInBackground(Integer... params) {
            Integer projectID = params[0];
            ProjectDTO project = null;
            try {
                if (!snappydb.isOpen()) {
                    snappydb = monApp.getSnappyDB();
                }
                project = snappydb.get(PROJECT + projectID, ProjectDTO.class);

            } catch (Exception e) {
                android.util.Log.e(LOG, "Failed ProjectTask", e);
            }
            return project;
        }

        @Override
        protected void onPostExecute(ProjectDTO p) {
            if (p != null)
                snappyProjectListener.onProjectFound(p);
            else
                snappyProjectListener.onError();
        }
    }

    static SnappyProjectListener snappyProjectListener;
    static MonApp monApp;

    public static void getProject(final MonApp ctx, final Integer projectID, final SnappyProjectListener listener) {
        snappyProjectListener = listener;
        monApp = ctx;
        getDatabase(ctx);
        new ProjectTask().execute(projectID);

    }


    public static void getMonitorList(final MonApp ctx, final SnappyReadListener listener) {
        readListener = listener;
        monApp = ctx;
        getDatabase(ctx);
        new GetMonitorsTask().execute();
    }

    public interface PhotoListener {
        void onPhotoAdded();

        void onPhotosFound(List<PhotoUploadDTO> list);

        void onError(String message);
    }

    public static void getPhotoListByProject(final MonApp ctx, final Integer projectID, final PhotoListener listener) {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                ResponseDTO r = new ResponseDTO();
                r.setPhotoUploadList(new ArrayList<PhotoUploadDTO>());
                try {
                    //DB snappydb = getDatabase(ctx);
                    String[] keys = snappydb.findKeys("project-" + projectID);
                    for (String key : keys) {
                        String json = snappydb.get(key);
                        PhotoUploadDTO p = gson.fromJson(json, PhotoUploadDTO.class);
                        r.getPhotoUploadList().add(p);
                    }
                    //snappydb.close();
                    android.util.Log.d(LOG, "Photos read: " + r.getPhotoUploadList().size());
                    listener.onPhotosFound(r.getPhotoUploadList());

                } catch (SnappydbException e) {
                    listener.onError(e.getMessage());
                }
            }
        });

    }

    public static void getPhotoListByProjectTask(final MonApp ctx, final Integer projectTaskID, final PhotoListener listener) {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                ResponseDTO r = new ResponseDTO();
                r.setPhotoUploadList(new ArrayList<PhotoUploadDTO>());
                try {
                    //DB snappydb = getDatabase(ctx);
                    String[] keys = snappydb.findKeys("projectTask-" + projectTaskID);
                    for (String key : keys) {
                        String json = snappydb.get(key);
                        PhotoUploadDTO p = gson.fromJson(json, PhotoUploadDTO.class);
                        r.getPhotoUploadList().add(p);
                    }
//                    snappydb.close();
                    android.util.Log.d(LOG, "Photos read: " + r.getPhotoUploadList().size());
                    listener.onPhotosFound(r.getPhotoUploadList());

                } catch (SnappydbException e) {
                    listener.onError(e.getMessage());
                }
            }
        });

    }

    public static void getMonitorProfilePhotoList(final MonApp ctx, final Integer monitorID, final PhotoListener listener) {
        getDatabase(ctx);
        getMonitorPhotoList(ctx, monitorID, new PhotoListener() {
            @Override
            public void onPhotoAdded() {

            }

            @Override
            public void onPhotosFound(List<PhotoUploadDTO> list) {
                List<PhotoUploadDTO> xList = new ArrayList<PhotoUploadDTO>();
                for (PhotoUploadDTO p : list) {
                    if (p.getPictureType() == PhotoUploadDTO.MONITOR_IMAGE) {
                        xList.add(p);
                    }

                }
                listener.onPhotosFound(xList);
            }

            @Override
            public void onError(String message) {

            }
        });
    }

    public static void getStaffProfilePhotoList(final MonApp ctx, final Integer staffID, final PhotoListener listener) {
        getDatabase(ctx);
        getStaffPhotoList(ctx, staffID, new PhotoListener() {
            @Override
            public void onPhotoAdded() {

            }

            @Override
            public void onPhotosFound(List<PhotoUploadDTO> list) {
                List<PhotoUploadDTO> xList = new ArrayList<>();
                for (PhotoUploadDTO p : list) {
                    if (p.getPictureType() == PhotoUploadDTO.STAFF_IMAGE) {
                        xList.add(p);
                    }

                }
                listener.onPhotosFound(xList);
            }

            @Override
            public void onError(String message) {

            }
        });
    }

    public static void getMonitorPhotoList(final MonApp ctx, final Integer monitorID, final PhotoListener listener) {
        getDatabase(ctx);
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                ResponseDTO r = new ResponseDTO();
                r.setPhotoUploadList(new ArrayList<PhotoUploadDTO>());
                try {
                    String[] keys = snappydb.findKeys("monitor");
                    for (String key : keys) {
                        String json = snappydb.get(key);
                        PhotoUploadDTO p = gson.fromJson(json, PhotoUploadDTO.class);
                        if (p.getMonitorID() != null) {
                            if (p.getMonitorID().intValue() == monitorID.intValue()) {
                                r.getPhotoUploadList().add(p);
                            }
                        }

                    }
                    android.util.Log.d(LOG, "Photos read: " + r.getPhotoUploadList().size());
                    listener.onPhotosFound(r.getPhotoUploadList());

                } catch (SnappydbException e) {
                    listener.onError(e.getMessage());
                }
            }
        });

    }

    public static void getStaffPhotoList(final MonApp ctx, final Integer staffID, final PhotoListener listener) {
        getDatabase(ctx);
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                ResponseDTO r = new ResponseDTO();
                r.setPhotoUploadList(new ArrayList<PhotoUploadDTO>());
                try {
                    //DB snappydb = getDatabase(ctx);
                    String[] keys = snappydb.findKeys("staff");
                    for (String key : keys) {
                        String json = snappydb.get(key);
                        PhotoUploadDTO p = gson.fromJson(json, PhotoUploadDTO.class);
                        if (p.getStaffID() != null) {
                            if (p.getStaffID().intValue() == staffID.intValue()) {
                                r.getPhotoUploadList().add(p);
                            }
                        }

                    }
//                    snappydb.close();
                    android.util.Log.d(LOG, "Photos read: " + r.getPhotoUploadList().size());
                    listener.onPhotosFound(r.getPhotoUploadList());

                } catch (SnappydbException e) {
                    listener.onError(e.getMessage());
                }
            }
        });

    }

    public static void getMonitorPhotoList(final MonApp ctx, final PhotoListener listener) {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                ResponseDTO r = new ResponseDTO();
                r.setPhotoUploadList(new ArrayList<PhotoUploadDTO>());
                try {
                    String[] keys = snappydb.findKeys("monitor");
                    for (String key : keys) {
                        String json = snappydb.get(key);
                        PhotoUploadDTO p = gson.fromJson(json, PhotoUploadDTO.class);
                        r.getPhotoUploadList().add(p);
                    }
                    android.util.Log.d(LOG, "Photos read: " + r.getPhotoUploadList().size());
                    listener.onPhotosFound(r.getPhotoUploadList());

                } catch (SnappydbException e) {
                    listener.onError(e.getMessage());
                }
            }
        });

    }

    public static void getStaffPhotoList(final MonApp ctx, final PhotoListener listener) {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                ResponseDTO r = new ResponseDTO();
                r.setPhotoUploadList(new ArrayList<PhotoUploadDTO>());
                try {
                    String[] keys = snappydb.findKeys("staff");
                    for (String key : keys) {
                        String json = snappydb.get(key);
                        PhotoUploadDTO p = gson.fromJson(json, PhotoUploadDTO.class);
                        r.getPhotoUploadList().add(p);
                    }
                    android.util.Log.d(LOG, "Photos read: " + r.getPhotoUploadList().size());
                    listener.onPhotosFound(r.getPhotoUploadList());

                } catch (SnappydbException e) {
                    listener.onError(e.getMessage());
                }
            }
        });

    }

    public static void writePhotoList(final MonApp app, final List<PhotoUploadDTO> list, final PhotoListener listener) {
        getDatabase(app);
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                ResponseDTO r = new ResponseDTO();
                StringBuilder sb = new StringBuilder();
                try {
                    for (PhotoUploadDTO p : list) {
                        snappydb.put(PHOTO + p.getDateTaken(), p);
                    }

                    android.util.Log.d(LOG, "** uploaded photo metadata written to snappy: " + list.size());
                    listener.onPhotoAdded();

                } catch (SnappydbException e) {
                    listener.onError(e.getMessage());
                }
            }
        });


    }

    public static void writeTaskStatusTypeList(final MonApp ctx, final List<TaskStatusTypeDTO> list, final SnappyWriteListener listener) {
        getDatabase(ctx);
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                ResponseDTO r = new ResponseDTO();
                r.setTaskStatusTypeList(list);
                try {
                    String json = gson.toJson(r);
                    snappydb.put(TASK_STATUS_TYPES, json);
                    android.util.Log.d(LOG, "TaskStatusTypes written: " + list.size());
                    listener.onDataWritten();

                } catch (SnappydbException e) {
                    listener.onError(e.getMessage());
                }
            }
        });


    }

    public static void getTaskStatusTypeList(final MonApp ctx, final SnappyReadListener listener) {
        getDatabase(ctx);
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                ResponseDTO r;
                try {
                    String json = snappydb.get(TASK_STATUS_TYPES);
                    r = gson.fromJson(json, ResponseDTO.class);
                    android.util.Log.d(LOG, "TaskStatusTypes read: " + r.getTaskStatusTypeList().size());
                    listener.onDataRead(r);

                } catch (SnappydbException e) {
                    listener.onError(e.getMessage());
                }
            }
        });

    }

    public static void writeTaskList(final MonApp ctx, final List<TaskDTO> list, final SnappyWriteListener listener) {
        getDatabase(ctx);
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                ResponseDTO r = new ResponseDTO();
                r.setTaskList(list);
                try {
                    String json = gson.toJson(r);
                    snappydb.put(TASKS, json);
                    android.util.Log.d(LOG, "Tasks written: " + list.size());
                    listener.onDataWritten();

                } catch (SnappydbException e) {
                    listener.onError(e.getMessage());
                }
            }
        });


    }

    public static void getTaskList(final MonApp ctx, final SnappyReadListener listener) {
        getDatabase(ctx);
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                ResponseDTO r;
                try {
                    String json = snappydb.get(TASKS);
                    r = gson.fromJson(json, ResponseDTO.class);
                    android.util.Log.d(LOG, "Tasks read: " + r.getTaskList().size());
                    listener.onDataRead(r);

                } catch (SnappydbException e) {
                    listener.onError(e.getMessage());
                }
            }
        });

    }

    static RequestTask requestTask;

    public static void cacheRequest(MonApp ctx, RequestDTO r, SnappyWriteListener listener) {
        monApp = ctx;
        writeListener = listener;
        request = r;
        getDatabase(ctx);
        requestTask = new RequestTask();
        requestTask.execute(CACHE_REQUEST);
    }

    public static void getRequests(MonApp ctx, SnappyReadListener listener) {
        android.util.Log.e(LOG, "SnappyForRequests getRequests");
        monApp = ctx;
        readListener = listener;
        getDatabase(ctx);
        new RequestTask().execute(GET_REQUESTS);
    }

    static List<RequestDTO> requestList;

    public static void updateRequestDates(final MonApp ctx, final List<RequestDTO> list) {
        monApp = ctx;
        requestList = list;
        getDatabase(ctx);
        new RequestTask().execute(DELETE_REQUESTS);

    }

    static SnappyDeleteListener deleteListener;

    public static void deleteRequest(MonApp ctx, RequestDTO w, SnappyDeleteListener listener) {
        monApp = ctx;
        getDatabase(ctx);
        deleteListener = listener;
        request = w;
        new RequestTask().execute(DELETE_REQUEST);
    }

    static class RequestTask extends AsyncTask<Integer, Void, Integer> {

        @Override
        protected Integer doInBackground(Integer... params) {
            Integer type = params[0];
            Log.w("SnappyForRequests", "doInBackground, type: " + type);
            try {
                switch (type) {
                    case CACHE_REQUEST:
                        synchronized (snappydb) {
                            String json = gson.toJson(request);
                            snappydb.put(REQUEST + request.getRequestDate(), json);
                            String[] keys = snappydb.findKeys(REQUEST);
                            android.util.Log.e(LOG, "..........Request cached on Snappy, " +
                                    " totalRequests: " + keys.length);
                        }

                        break;
                    case GET_REQUESTS:
                        synchronized (snappydb) {
                            if (!snappydb.isOpen()) {
                                snappydb = monApp.getSnappyDB();
                            }
                            String[] keys = snappydb.findKeys(REQUEST);
                            response = new ResponseDTO();
                            for (String key : keys) {
                                String json = snappydb.get(key);
                                RequestDTO dto = gson.fromJson(json, RequestDTO.class);
                                if (dto.getDateUploaded() == null)
                                    response.getRequestList().add(dto);
                            }
                            android.util.Log.d(LOG, "............................." +
                                    "Read requests with null dateUploaded: " + response.getRequestList().size());
                        }
                        break;

                    case DELETE_REQUEST:
                        synchronized (snappydb) {
                            snappydb.del(REQUEST + request.getRequestDate());
                            android.util.Log.d(LOG, "Request deleted on Snappy, requestType: "
                                    + request.getRequestType());
                        }

                        break;
                    case DELETE_REQUESTS:
                        if (!snappydb.isOpen()) {
                            snappydb = monApp.getSnappyDB();
                        }
                        synchronized (snappydb) {
                            try {
                                for (RequestDTO w : requestList) {
                                    String json = snappydb.get(REQUEST + w.getRequestDate());
                                    RequestDTO req = gson.fromJson(json, RequestDTO.class);
                                    snappydb.del(REQUEST + w.getRequestDate());
                                    String[] keys = snappydb.findKeys(REQUEST);
                                    android.util.Log.w(LOG, "*** Request deleted on Snappy, requestType: " + req.getRequestType() + " " +
                                            new Date(req.getRequestDate()).toString()
                                            + " requests remaining: " + keys.length);
                                }
                            } catch (Exception e) {
                                Log.e(LOG, e.getMessage());
                            }
                            String[] keys = snappydb.findKeys(REQUEST);
                            android.util.Log.w(LOG, "after deleteLookups, requests: " + keys.length);
                        }
                        break;
                }

            } catch (Exception e) {
                Log.w(LOG, "Failed to work", e);
                response = null;
                return 9;
            }
            return 0;
        }

        @Override
        protected void onPostExecute(Integer p) {
            Log.w("SnappyForRequests", "onPostExecute, result: " + p);
            if (writeListener != null) {
                if (p.intValue() == 0) {
                    writeListener.onDataWritten();
                } else {
                    writeListener.onError("Failed to write");
                }
            }
            if (deleteListener != null) {
                if (p.intValue() == 0) {
                    deleteListener.onDataDeleted();
                } else {
                    deleteListener.onError("Failed to deleteLookups");
                }
            }
            if (readListener != null) {
                if (p.intValue() == 0) {
                    readListener.onDataRead(response);
                } else {
                    readListener.onError("Could not read data from Snappy");
                }
            }

        }


    }

    private static void deleteLookups(final MonApp app) {
        try {
            getDatabase(app);
            String[] keys = snappydb.findKeys(STAFF);
            for (String key : keys) {
                snappydb.del(key);
            }

            keys = snappydb.findKeys(MONITOR);
            for (String key : keys) {
                snappydb.del(key);
            }
            keys = snappydb.findKeys(TASK_STATUS_TYPES);
            for (String key : keys) {
                snappydb.del(key);
            }
            keys = snappydb.findKeys(TASKS);
            for (String key : keys) {
                snappydb.del(key);
            }
            Log.e(LOG, "*********** Snappy cached lookups removed prior to refresh ........ ");
        } catch (Exception e) {

        }
    }

    private static void deleteProjects(final MonApp app) {
        try {
            getDatabase(app);
            String[] keys = snappydb.findKeys(PROJECT);
            for (String key : keys) {
                snappydb.del(key);
            }
            keys = snappydb.findKeys(PROJECT_LITE);
            for (String key : keys) {
                snappydb.del(key);
            }
            Log.e(LOG, "*********** Snappy cached projects removed prior to refresh ........ ");
        } catch (Exception e) {

        }
    }

    public static void cacheProjects(final MonApp ctx,
                                     final ResponseDTO response,
                                     final SnappyWriteListener listener) {

        Log.w(LOG, "################### caching on Snappy for: projects......");
        Collections.sort(response.getProjectList());
        try {
            deleteProjects(ctx);
            writeProjectList(ctx, response.getProjectList(), new SnappyWriteListener() {
                @Override
                public void onDataWritten() {
                    Log.d(LOG, "Yeaaaah!! " + response.getProjectList().size()
                            + " Projects written to SnappyDB. Caching is complete");
                    listener.onDataWritten();
                }

                @Override
                public void onError(String message) {
                    listener.onError(message);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void cacheLookups(final MonApp ctx,
                                    final ResponseDTO response,
                                    final SnappyWriteListener listener) {

        Log.w(LOG, "################### caching on Snappy for: status types, tasks, staff and monitors......");
        try {
            deleteLookups(ctx);
            writeStaffList(ctx, response.getStaffList(), new SnappyWriteListener() {
                @Override
                public void onDataWritten() {
                    writeMonitorList(ctx, response.getMonitorList(), new SnappyWriteListener() {
                        @Override
                        public void onDataWritten() {
                            writeTaskStatusTypeList(ctx, response.getTaskStatusTypeList(), new SnappyWriteListener() {
                                @Override
                                public void onDataWritten() {
                                    writeTaskList(ctx, response.getTaskList(), new SnappyWriteListener() {
                                        @Override
                                        public void onDataWritten() {
                                            Log.d(LOG, "Yeaaaah!! Data written to SnappyDB. Caching is complete");
                                            listener.onDataWritten();
                                        }

                                        @Override
                                        public void onError(String message) {
                                            listener.onError(message);
                                        }
                                    });
                                }

                                @Override
                                public void onError(String message) {
                                    listener.onError(message);
                                }
                            });
                        }

                        @Override
                        public void onError(String message) {
                            listener.onError(message);
                        }
                    });
                }

                @Override
                public void onError(String message) {
                    listener.onError(message);
                }
            });


        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void cachePhotoForUpload(MonApp app, PhotoUploadDTO photo, SnappyWriteListener listener) {
        writeListener = listener;
        getDatabase(app);
        new PhotoUploadTask().execute(photo);
    }

    static class PhotoUploadTask extends AsyncTask<PhotoUploadDTO, Void, Integer> {

        @Override
        protected Integer doInBackground(PhotoUploadDTO... params) {
            PhotoUploadDTO photo = params[0];
            try {
                snappydb.put(PHOTO_FOR_UPLOAD + photo.getDateTaken(), photo);
            } catch (SnappydbException e) {

                return 9;
            }
            return 0;
        }

        @Override
        protected void onPostExecute(Integer result) {
            if (result == 0) {
                writeListener.onDataWritten();
            } else {
                writeListener.onError("Failed to work photo cache");
            }
        }
    }

    public static void getPhotosForUpload(MonApp app, SnappyReadListener listener) {
        readListener = listener;
        getDatabase(app);
        new GetPhotosForUploadTask().execute();
    }

    static class GetPhotosForUploadTask extends AsyncTask<Void, Void, List<PhotoUploadDTO>> {

        @Override
        protected List<PhotoUploadDTO> doInBackground(Void... params) {
            List<PhotoUploadDTO> photoList = new ArrayList<>();
            try {
                String[] keys = snappydb.findKeys(PHOTO_FOR_UPLOAD);
                for (String key : keys) {
                    PhotoUploadDTO p = snappydb.getObject(key, PhotoUploadDTO.class);
                    photoList.add(p);
                }

            } catch (SnappydbException e) {

                return null;
            }
            return photoList;
        }

        @Override
        protected void onPostExecute(List<PhotoUploadDTO> result) {
            if (result == null) {
                readListener.onError("Failed to get photos from cache");
            } else {
                ResponseDTO resp = new ResponseDTO();
                resp.setPhotoUploadList(result);
                readListener.onDataRead(resp);
            }
        }
    }

    public static void deletePhotoUploaded(MonApp app, PhotoUploadDTO photo, SnappyDeleteListener listener) {
        deleteListener = listener;
        getDatabase(app);
        new DeletePhotoUploadedTask().execute(photo);
    }

    static class DeletePhotoUploadedTask extends AsyncTask<PhotoUploadDTO, Void, Integer> {

        @Override
        protected Integer doInBackground(PhotoUploadDTO... params) {
            PhotoUploadDTO photo = params[0];
            try {
                String[] keys = snappydb.findKeys(PHOTO_FOR_UPLOAD + photo.getDateTaken());
                for (String key : keys) {
                    snappydb.del(key);
                }

            } catch (SnappydbException e) {

                return 9;
            }
            return 0;
        }

        @Override
        protected void onPostExecute(Integer result) {
            if (result != 0) {
                deleteListener.onError("Failed to get photos from cache");
            } else {
                deleteListener.onDataDeleted();
            }
        }
    }

    private static void getDatabase(MonApp app) {
        try {
            if (snappydb == null || !snappydb.isOpen()) {
                snappydb = app.getSnappyDB();
            }
        } catch (SnappydbException e) {
            e.printStackTrace();
        }
    }

    public static void saveCompanyTrackerList(MonApp app, List<LocationTrackerDTO> list,
                                              SnappyWriteListener listener) {
        writeListener = listener;
        getDatabase(app);

        new SaveLocationTrackerTask().execute(list);


    }

    public static void getCompanyTrackerList(MonApp app, SnappyReadListener listener) {
        readListener = listener;
        getDatabase(app);

        new GetLocationTrackerTask().execute();
    }


    static class GetLocationTrackerTask extends AsyncTask<Void, Void, List<LocationTrackerDTO>> {

        @Override
        protected List<LocationTrackerDTO> doInBackground(Void... params) {
            List<LocationTrackerDTO> list = new ArrayList<>();

            try {
                ResponseDTO w = snappydb.getObject(LOCATION_TRACK, ResponseDTO.class);
                list = w.getLocationTrackerList();
            } catch (SnappydbException e) {
                Log.e(LOG, e.getMessage());
            }
            Log.w(LOG, "--- trackers found in cache: " + list.size());
            return list;
        }

        @Override
        protected void onPostExecute(List<LocationTrackerDTO> list) {
            ResponseDTO w = new ResponseDTO();
            w.setLocationTrackerList(list);
            readListener.onDataRead(w);
        }
    }

    static final String LOCATION_TRACK = "LOCATION_TRACK";

    static class SaveLocationTrackerTask extends AsyncTask<List<LocationTrackerDTO>, Void, Integer> {

        @Override
        protected Integer doInBackground(List<LocationTrackerDTO>... params) {
            List<LocationTrackerDTO> list = params[0];
            ResponseDTO w = new ResponseDTO();
            w.setLocationTrackerList(list);
            try {
                snappydb.put(LOCATION_TRACK, w);
                Log.w(LOG, "--- trackers saved in cache: " + list.size());
            } catch (SnappydbException e) {
                Log.e(LOG, e.getMessage(), e);
                return 9;
            }
            return 0;
        }

        @Override
        protected void onPostExecute(Integer result) {
            if (result == 0) {
                writeListener.onDataWritten();
            } else {
                writeListener.onError("Unable to write LocationTrackers");
            }
        }
    }


    static VideoListener videoListener;

    public static void addVideo(MonApp app, VideoUploadDTO video, int type, VideoListener listener) {
        videoListener = listener;
        getDatabase(app);
        VideoData vd = new VideoData();
        vd.videoUpload = video;
        vd.operationType = type;
        new VideoTask().execute(vd);
    }

    public static void getVideoList(MonApp app, VideoListener listener) {
        videoListener = listener;
        getDatabase(app);
        VideoData vd = new VideoData();
        vd.operationType = GET_VIDEO_LIST_FOR_UPLOAD;
        new VideoTask().execute(vd);
    }

    public static void deleteVideo(MonApp app, VideoUploadDTO video, VideoListener listener) {
        videoListener = listener;
        getDatabase(app);
        VideoData vd = new VideoData();
        vd.dateTaken = video.getDateTaken();
        vd.operationType = DELETE_VIDEO;
        new VideoTask().execute(vd);
    }

    static class VideoTask extends AsyncTask<VideoData, Void, VideoData> {

        @Override
        protected VideoData doInBackground(VideoData... params) {
            VideoData data = params[0];
            switch (data.operationType) {
                case ADD_VIDEO_FOR_UPLOAD:
                    try {
                        snappydb.put(VIDEO_FOR_UPLOAD + data.videoUpload.getDateTaken(), data.videoUpload);
                        Log.w(LOG, "------------- Video cached. Awaiting uploadToYouTube .....");
                    } catch (SnappydbException e) {
                        data.isError = true;
                    }
                    break;
                case ADD_UPLOADED_VIDEO:
                    try {
                        snappydb.put(UPLOADED_VIDEO + data.videoUpload.getDateTaken(), data.videoUpload);
                        Log.w(LOG, "Uploaded Video cached. +++++++++++");
                    } catch (SnappydbException e) {
                        data.isError = true;
                    }
                    break;
                case GET_VIDEO:
                    try {
                        VideoUploadDTO video = snappydb.getObject(VIDEO_FOR_UPLOAD + data.dateTaken, VideoUploadDTO.class);
                        data.videoUpload = video;
                        Log.w(LOG, "Video retrieved. " + data.videoUpload.getDateTaken());
                    } catch (SnappydbException e) {
                        data.isError = true;
                    }
                    break;
                case DELETE_VIDEO:
                    try {
                        snappydb.del(VIDEO_FOR_UPLOAD + data.dateTaken);
                        Log.w(LOG, "Video deleted. -------------: " + new Date(data.dateTaken));
                        String[] keys = snappydb.findKeys(VIDEO_FOR_UPLOAD);
                        Log.e(LOG, "after delete, videos on disk: " + keys.length);
                    } catch (SnappydbException e) {
                        data.isError = true;
                    }
                    break;
                case GET_VIDEO_LIST_FOR_UPLOAD:
                    try {
                        String[] keys = snappydb.findKeys(VIDEO_FOR_UPLOAD);
                        for (String key : keys) {
                            Log.e(LOG, "GET_VIDEO_LIST_FOR_UPLOAD, key: " + key);
                            VideoUploadDTO dto = snappydb.getObject(key, VideoUploadDTO.class);
                            data.videoUploadList.add(dto);
                        }
                        Log.w(LOG, "Videos Listed from Snappy: " + data.videoUploadList.size());

                    } catch (Exception e) {
                        data.isError = true;
                    }
            }
            return data;
        }

        @Override
        protected void onPostExecute(VideoData data) {
            if (data.isError) {
                videoListener.onError();
                return;
            }
            if (videoListener == null) {
                return;
            }
            switch (data.operationType) {
                case ADD_VIDEO_FOR_UPLOAD:
                    videoListener.onVideoAdded();
                    break;
                case ADD_UPLOADED_VIDEO:
                    videoListener.onVideoAdded();
                    break;
                case DELETE_VIDEO:
                    videoListener.onVideoDeleted();
                    break;
                case GET_VIDEO_LIST_FOR_UPLOAD:
                    videoListener.onVideosListed(data.videoUploadList);
                    break;
                case GET_VIDEO:
                    data.videoUploadList.add(data.videoUpload);
                    videoListener.onVideosListed(data.videoUploadList);
                    break;
            }
        }
    }

    public interface VideoListener {
        void onVideoAdded();

        void onVideoDeleted();

        void onVideosListed(List<VideoUploadDTO> list);

        void onError();
    }

    public static final int ADD_VIDEO_FOR_UPLOAD = 1, GET_VIDEO = 2,
            DELETE_VIDEO = 3, GET_VIDEO_LIST_FOR_UPLOAD = 4, ADD_UPLOADED_VIDEO = 5;
    public static final String VIDEO_FOR_UPLOAD = "videoForUpload", UPLOADED_VIDEO = "uploadedVideo";

    static class VideoData {
        VideoUploadDTO videoUpload;
        List<VideoUploadDTO> videoUploadList = new ArrayList<>();
        Long dateTaken;
        int operationType;
        boolean isError;
    }
}

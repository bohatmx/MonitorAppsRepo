package com.boha.monitor.library.util;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.*;

import com.boha.monitor.library.dto.MonitorDTO;
import com.boha.monitor.library.dto.PhotoUploadDTO;
import com.boha.monitor.library.dto.ProjectDTO;
import com.boha.monitor.library.dto.ResponseDTO;
import com.boha.monitor.library.dto.StaffDTO;
import com.boha.monitor.library.dto.TaskStatusTypeDTO;
import com.google.gson.Gson;
import com.snappydb.DB;
import com.snappydb.DBFactory;
import com.snappydb.SnappydbException;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by aubreymalabie on 2/18/16.
 */
public class Snappy {


    public interface SnappyWriteListener {
        void onDataWritten();

        void onError(String message);
    }

    public interface SnappyReadListener {
        void onDataRead(ResponseDTO response);

        void onError(String message);
    }

    public interface SnappyProjectListener {
        void onProjectFound(ProjectDTO project);
    }

    static final Gson gson = new Gson();
    static final String PROJECT = "project", PROJECT_LITE = "projectlite", STAFF = "staffs", TASK_STATUS_TYPES = "taskstatustypes",
            MONITOR = "monitors", LOG = Snappy.class.getSimpleName(), DB_NAME = "monDatabase";


    public static void writeProjectList(final Context ctx, final List<ProjectDTO> list, final SnappyWriteListener listener) {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    List<ProjectDTO> liteList = new ArrayList<ProjectDTO>();
                    DB snappydb = getDatabase(ctx);
                    for (ProjectDTO p : list) {
                        String json = gson.toJson(p);
                        snappydb.put(PROJECT + p.getProjectID(), json);
                        ProjectDTO d = new ProjectDTO();
                        d.setProjectID(p.getProjectID());
                        d.setProjectName(p.getProjectName());
                        d.setLatitude(p.getLatitude());
                        d.setLongitude(p.getLongitude());
                        d.setLocationConfirmed(p.getLocationConfirmed());
                        liteList.add(d);
                    }
                    ResponseDTO xx = new ResponseDTO();
                    xx.setProjectList(liteList);
                    String json2 = gson.toJson(xx);
                    snappydb.put(PROJECT_LITE, json2);
                    snappydb.close();
                    android.util.Log.d(LOG, "Projects written: " + list.size());
                    android.util.Log.d(LOG, "Projects Lite written: " + liteList.size());

                    listener.onDataWritten();

                } catch (SnappydbException e) {
                    listener.onError(e.getMessage());
                }
            }
        });


    }

    public static void getProjectList(final Context ctx, final SnappyReadListener listener) {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    DB snappydb = getDatabase(ctx);
                    String json = snappydb.get(PROJECT_LITE);
                    ResponseDTO r = gson.fromJson(json, ResponseDTO.class);
                    snappydb.close();
                    android.util.Log.d(LOG, "Read projects: " + r.getProjectList().size());
                    listener.onDataRead(r);

                } catch (SnappydbException e) {
                    listener.onError(e.getMessage());
                }

            }
        });

    }

    public static void writeStaffList(final Context ctx, final List<StaffDTO> list, final SnappyWriteListener listener) {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                ResponseDTO r = new ResponseDTO();
                r.setStaffList(list);
                try {
                    DB snappydb = getDatabase(ctx);
                    String json = gson.toJson(r);
                    snappydb.put(STAFF, json);
                    snappydb.close();
                    android.util.Log.d(LOG, "Staff written: " + list.size());
                    listener.onDataWritten();

                } catch (SnappydbException e) {
                    listener.onError(e.getMessage());
                }
            }
        });


    }

    public static void getStaffList(final Context ctx, final SnappyReadListener listener) {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                ResponseDTO r = new ResponseDTO();
                r.setStaffList(new ArrayList<StaffDTO>());
                try {
                    DB snappydb = getDatabase(ctx);
                    String json = snappydb.get(STAFF);
                    r = gson.fromJson(json, ResponseDTO.class);
                    snappydb.close();
                    android.util.Log.d(LOG, "Staff read: " + r.getStaffList().size());
                    listener.onDataRead(r);

                } catch (SnappydbException e) {
                    listener.onError(e.getMessage());
                }
            }
        });

    }

    public static void addStaff(final Context ctx, final StaffDTO staff,
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

    public static void addMonitor(final Context ctx, final MonitorDTO monitor,
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

    public static void writeMonitorList(final Context ctx, final List<MonitorDTO> list, final SnappyWriteListener listener) {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                ResponseDTO r = new ResponseDTO();
                r.setMonitorList(list);
                try {
                    DB snappydb = getDatabase(ctx);
                    String json = gson.toJson(r);
                    snappydb.put(MONITOR, json);
                    snappydb.close();
                    android.util.Log.d(LOG, "Monitor written: " + list.size());
                    listener.onDataWritten();

                } catch (SnappydbException e) {
                    listener.onError(e.getMessage());
                }
            }
        });


    }

    static class MyTask extends AsyncTask<Integer, Void, ProjectDTO> {

        @Override
        protected ProjectDTO doInBackground(Integer... params) {
            Integer projectID = params[0];
            ProjectDTO project = null;
            try {
                DB snappydb = getDatabase(context);
                String json = snappydb.get(PROJECT + projectID);
                project = gson.fromJson(json, ProjectDTO.class);
                snappydb.close();
//                android.util.Log.d(LOG, "Project found: " + project.getProjectName());


            } catch (SnappydbException e) {
            }
            return project;
        }

        @Override
        protected void onPostExecute(ProjectDTO p) {
            snappyProjectListener.onProjectFound(p);
        }
    }

    static SnappyProjectListener snappyProjectListener;
    static Context context;

    public static void getProject(final Context ctx, final Integer projectID, final SnappyProjectListener listener) {
        snappyProjectListener = listener;
        context = ctx;
        new MyTask().execute(projectID);

    }


    public static void getMonitorList(final Context ctx, final SnappyReadListener listener) {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                ResponseDTO r;
                try {
                    DB snappydb = getDatabase(ctx);
                    String json = snappydb.get(MONITOR);
                    r = gson.fromJson(json, ResponseDTO.class);
                    snappydb.close();
                    android.util.Log.d(LOG, "Monitors read: " + r.getMonitorList().size());
                    listener.onDataRead(r);

                } catch (SnappydbException e) {
                    listener.onError(e.getMessage());
                }
            }
        });

    }

    public interface PhotoListener {
        void onPhotoAdded();

        void onPhotosFound(List<PhotoUploadDTO> list);

        void onError(String message);
    }

    public static void getPhotoListByProject(final Context ctx, final Integer projectID, final PhotoListener listener) {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                ResponseDTO r = new ResponseDTO();
                r.setPhotoUploadList(new ArrayList<PhotoUploadDTO>());
                try {
                    DB snappydb = getDatabase(ctx);
                    String[] keys = snappydb.findKeys("project-" + projectID);
                    for (String key : keys) {
                        String json = snappydb.get(key);
                        PhotoUploadDTO p = gson.fromJson(json, PhotoUploadDTO.class);
                        r.getPhotoUploadList().add(p);
                    }
                    snappydb.close();
                    android.util.Log.d(LOG, "Photos read: " + r.getPhotoUploadList().size());
                    listener.onPhotosFound(r.getPhotoUploadList());

                } catch (SnappydbException e) {
                    listener.onError(e.getMessage());
                }
            }
        });

    }
    public static void getPhotoListByProjectTask(final Context ctx, final Integer projectTaskID, final PhotoListener listener) {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                ResponseDTO r = new ResponseDTO();
                r.setPhotoUploadList(new ArrayList<PhotoUploadDTO>());
                try {
                    DB snappydb = getDatabase(ctx);
                    String[] keys = snappydb.findKeys("projectTask-" + projectTaskID);
                    for (String key : keys) {
                        String json = snappydb.get(key);
                        PhotoUploadDTO p = gson.fromJson(json, PhotoUploadDTO.class);
                        r.getPhotoUploadList().add(p);
                    }
                    snappydb.close();
                    android.util.Log.d(LOG, "Photos read: " + r.getPhotoUploadList().size());
                    listener.onPhotosFound(r.getPhotoUploadList());

                } catch (SnappydbException e) {
                    listener.onError(e.getMessage());
                }
            }
        });

    }
    public static void getMonitorProfilePhotoList(final Context ctx, final Integer monitorID, final PhotoListener listener) {

        getMonitorPhotoList(ctx, monitorID, new PhotoListener() {
            @Override
            public void onPhotoAdded() {

            }

            @Override
            public void onPhotosFound(List<PhotoUploadDTO> list) {
                List<PhotoUploadDTO> xList = new ArrayList<PhotoUploadDTO>();
                for (PhotoUploadDTO p: list) {
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
    public static void getStaffProfilePhotoList(final Context ctx, final Integer staffID, final PhotoListener listener) {

        getStaffPhotoList(ctx, staffID, new PhotoListener() {
            @Override
            public void onPhotoAdded() {

            }

            @Override
            public void onPhotosFound(List<PhotoUploadDTO> list) {
                List<PhotoUploadDTO> xList = new ArrayList<>();
                for (PhotoUploadDTO p: list) {
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
    public static void getMonitorPhotoList(final Context ctx, final Integer monitorID, final PhotoListener listener) {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                ResponseDTO r = new ResponseDTO();
                r.setPhotoUploadList(new ArrayList<PhotoUploadDTO>());
                try {
                    DB snappydb = getDatabase(ctx);
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
                    snappydb.close();
                    android.util.Log.d(LOG, "Photos read: " + r.getPhotoUploadList().size());
                    listener.onPhotosFound(r.getPhotoUploadList());

                } catch (SnappydbException e) {
                    listener.onError(e.getMessage());
                }
            }
        });

    }
    public static void getStaffPhotoList(final Context ctx, final Integer staffID, final PhotoListener listener) {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                ResponseDTO r = new ResponseDTO();
                r.setPhotoUploadList(new ArrayList<PhotoUploadDTO>());
                try {
                    DB snappydb = getDatabase(ctx);
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
                    snappydb.close();
                    android.util.Log.d(LOG, "Photos read: " + r.getPhotoUploadList().size());
                    listener.onPhotosFound(r.getPhotoUploadList());

                } catch (SnappydbException e) {
                    listener.onError(e.getMessage());
                }
            }
        });

    }
    public static void getMonitorPhotoList(final Context ctx,  final PhotoListener listener) {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                ResponseDTO r = new ResponseDTO();
                r.setPhotoUploadList(new ArrayList<PhotoUploadDTO>());
                try {
                    DB snappydb = getDatabase(ctx);
                    String[] keys = snappydb.findKeys("monitor");
                    for (String key : keys) {
                        String json = snappydb.get(key);
                        PhotoUploadDTO p = gson.fromJson(json, PhotoUploadDTO.class);
                        r.getPhotoUploadList().add(p);
                    }
                    snappydb.close();
                    android.util.Log.d(LOG, "Photos read: " + r.getPhotoUploadList().size());
                    listener.onPhotosFound(r.getPhotoUploadList());

                } catch (SnappydbException e) {
                    listener.onError(e.getMessage());
                }
            }
        });

    }
    public static void getStaffPhotoList(final Context ctx,  final PhotoListener listener) {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                ResponseDTO r = new ResponseDTO();
                r.setPhotoUploadList(new ArrayList<PhotoUploadDTO>());
                try {
                    DB snappydb = getDatabase(ctx);
                    String[] keys = snappydb.findKeys("staff");
                    for (String key : keys) {
                        String json = snappydb.get(key);
                        PhotoUploadDTO p = gson.fromJson(json, PhotoUploadDTO.class);
                        r.getPhotoUploadList().add(p);
                    }
                    snappydb.close();
                    android.util.Log.d(LOG, "Photos read: " + r.getPhotoUploadList().size());
                    listener.onPhotosFound(r.getPhotoUploadList());

                } catch (SnappydbException e) {
                    listener.onError(e.getMessage());
                }
            }
        });

    }

    public static void writePhotoList(final Context ctx, final List<PhotoUploadDTO> list, final PhotoListener listener) {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                ResponseDTO r = new ResponseDTO();
                StringBuilder sb = new StringBuilder();
                try {
                    DB snappydb = getDatabase(ctx);
                    for (PhotoUploadDTO p : list) {

                        if (p.getProjectID() != null) {
                            sb.append("project-");
                            sb.append(p.getProjectID());
                        }
                        if (p.getProjectTaskID() != null) {
                            sb.append("projectTask-").append(p.getProjectTaskID());
                        }
                        if (p.getMonitorID() != null) {
                            sb.append("monitor").append(p.getMonitorID());
                        }
                        if (p.getStaffID() != null) {
                            sb.append("staff").append(p.getStaffID());
                        }
                        sb.append("-").append(System.currentTimeMillis());

                        String json = gson.toJson(p);
                        snappydb.put(sb.toString(), json);
                    }

                    snappydb.close();
                    android.util.Log.d(LOG, "** uploaded photo metadata written to snappy: " + list.size());
                    listener.onPhotoAdded();

                } catch (SnappydbException e) {
                    listener.onError(e.getMessage());
                }
            }
        });


    }

    public static void writeTaskStatusTypeList(final Context ctx, final List<TaskStatusTypeDTO> list, final SnappyWriteListener listener) {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                ResponseDTO r = new ResponseDTO();
                r.setTaskStatusTypeList(list);
                try {
                    DB snappydb = getDatabase(ctx);
                    String json = gson.toJson(r);
                    snappydb.put(TASK_STATUS_TYPES, json);
                    snappydb.close();
                    android.util.Log.d(LOG, "TaskStatusTypes written: " + list.size());
                    listener.onDataWritten();

                } catch (SnappydbException e) {
                    listener.onError(e.getMessage());
                }
            }
        });


    }

    public static void getTaskStatusTypeList(final Context ctx, final SnappyReadListener listener) {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                ResponseDTO r;
                try {
                    DB snappydb = getDatabase(ctx);
                    String json = snappydb.get(TASK_STATUS_TYPES);
                    r = gson.fromJson(json, ResponseDTO.class);
                    snappydb.close();
                    android.util.Log.d(LOG, "TaskStatusTypes read: " + r.getTaskStatusTypeList().size());
                    listener.onDataRead(r);

                } catch (SnappydbException e) {
                    listener.onError(e.getMessage());
                }
            }
        });

    }

    private static DB getDatabase(Context ctx) {
        if (ctx == null) {
            android.util.Log.e(LOG,"############### Snappy! Context is null. WTF????");
            return null;
        }
        try {
            return DBFactory.open(ctx, DB_NAME);
        } catch (SnappydbException e) {
            e.printStackTrace();
        }
        return null;
    }
}

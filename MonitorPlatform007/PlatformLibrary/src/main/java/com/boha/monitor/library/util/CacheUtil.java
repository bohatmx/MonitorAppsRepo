package com.boha.monitor.library.util;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.boha.monitor.library.dto.LocationTrackerDTO;
import com.boha.monitor.library.dto.ResponseDTO;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * Created by aubreyM on 2014/06/30.
 */
public class CacheUtil {

    public interface CacheUtilListener {
        public void onFileDataDeserialized(ResponseDTO response);

        public void onDataCached();

        public void onError();
    }


    static CacheUtilListener utilListener;
    public static final int CACHE_DATA = 1, CACHE_COUNTRIES = 3, CACHE_SITE = 7,
            CACHE_PROJECT = 5, CACHE_REQUEST = 6, CACHE_PROJECT_STATUS = 4,
            CACHE_TRACKER = 8, CACHE_CHAT = 9, CACHE_MONITOR_PROJECTS = 10, CACHE_TASK_STATUS = 11, CACHE_COMPANY = 12;
    static int dataType;
    static Integer projectID;
    static ResponseDTO response;
    static Integer projectSiteID;
    static SessionPhoto sessionPhoto;
    static Context ctx;
    static final String JSON_DATA = "data.json", JSON_COUNTRIES = "countries.json", JSON_COMPANY_DATA = "company_data",
            JSON_PROJECT_DATA = "project_data", JSON_PROJECT_STATUS = "project_status", JSON_MON_PROJECTS = "monprojects.json",
            JSON_REQUEST = "requestCache.json", JSON_SITE = "site", JSON_TRACKER = "tracker.json", JSON_CHAT = "chat", JSON_STATUS = "status";


    public static void cacheData(Context context, ResponseDTO r, int type, CacheUtilListener cacheUtilListener) {
        dataType = type;
        response = r;
        utilListener = cacheUtilListener;
        ctx = context;
        new CacheTask().execute();
    }

    static Integer companyID;
    public static void cacheCompanyData(Context context, ResponseDTO r, Integer cID, CacheUtilListener cacheUtilListener) {
        dataType = CACHE_COMPANY;
        response = r;
        utilListener = cacheUtilListener;
        companyID = cID;
        ctx = context;
        new CacheTask().execute();
    }

    public static void cacheProjectData(Context context, ResponseDTO r, Integer pID, CacheUtilListener cacheUtilListener) {
        dataType = CACHE_PROJECT;
        response = r;
        utilListener = cacheUtilListener;
        projectID = pID;
        ctx = context;
        new CacheTask().execute();
    }

    public static void cacheMonitorProjects(Context context, ResponseDTO r, CacheUtilListener cacheUtilListener) {
        dataType = CACHE_MONITOR_PROJECTS;
        response = r;
        utilListener = cacheUtilListener;
        ctx = context;
        new CacheTask().execute();
    }

    public static void cacheProjectChats(Context context, ResponseDTO r, Integer pID, CacheUtilListener cacheUtilListener) {
        dataType = CACHE_CHAT;
        response = r;
        utilListener = cacheUtilListener;
        projectID = pID;
        ctx = context;
        new CacheTask().execute();
    }

    public static void cacheProjectStatus(Context context, ResponseDTO r, Integer pID, CacheUtilListener cacheUtilListener) {
        dataType = CACHE_PROJECT_STATUS;
        response = r;
        utilListener = cacheUtilListener;
        projectID = pID;
        ctx = context;
        new CacheTask().execute();
    }


    public static void getCachedData(Context context, int type, CacheUtilListener cacheUtilListener) {
        dataType = type;
        utilListener = cacheUtilListener;
        ctx = context;
        new CacheRetrieveTask().execute();
    }

    public static void getCachedTaskStatusData(Context context, CacheUtilListener cacheUtilListener) {
        dataType = CACHE_TASK_STATUS;
        utilListener = cacheUtilListener;
        ctx = context;
        new CacheRetrieveTask().execute();
    }


    public static void cacheTrackerData(final Context context, final ResponseDTO r, final CacheUtilListener cacheUtilListener) {

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                String json = gson.toJson(r);
                FileOutputStream outputStream = null;
                try {
                    outputStream = context.openFileOutput(JSON_TRACKER, Context.MODE_PRIVATE);
                    outputStream.write(json.getBytes());
                    outputStream.close();
                    File file = context.getFileStreamPath(JSON_TRACKER);
                    if (file != null) {
                        Log.e(LOG, "Tracker cache written, path: " + file.getAbsolutePath() +
                                " - length: " + file.length());
                    }
                    if (cacheUtilListener != null)
                        cacheUtilListener.onDataCached();
                } catch (IOException e) {
                    Log.e(LOG, "Cache failed", e);
                    if (cacheUtilListener != null)
                        cacheUtilListener.onError();
                }

            }
        });
        thread.start();
    }

    public static void getCachedTrackerData(final Context context, final CacheUtilListener cacheUtilListener) {

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                FileInputStream stream = null;
                try {
                    stream = context.openFileInput(JSON_TRACKER);
                    String json = getStringFromInputStream(stream);
                    ResponseDTO response = gson.fromJson(json, ResponseDTO.class);
                    cacheUtilListener.onFileDataDeserialized(response);
                } catch (IOException e) {
                    Log.e(LOG, "## cache not found, starting new cache");
                    ResponseDTO response = new ResponseDTO();
                    response.setLocationTrackerList(new ArrayList<LocationTrackerDTO>());
                    cacheUtilListener.onFileDataDeserialized(response);
                }

            }
        });
        thread.start();
    }

    public static void getCachedMonitorProjects(Context context, CacheUtilListener cacheUtilListener) {
        dataType = CACHE_MONITOR_PROJECTS;
        utilListener = cacheUtilListener;
        ctx = context;
        new CacheRetrieveTask().execute();
    }

    public static void getCachedProjectData(Context context, Integer id, CacheUtilListener cacheUtilListener) {
        Log.d(LOG, "################ getting cached project data ..................");
        dataType = CACHE_PROJECT;
        utilListener = cacheUtilListener;
        ctx = context;
        projectID = id;
        new CacheRetrieveTask().execute();
    }

    public static void getCachedCompanyData(Context context, Integer id, CacheUtilListener cacheUtilListener) {
        Log.d(LOG, "################ getting cached project data ..................");
        dataType = CACHE_COMPANY;
        utilListener = cacheUtilListener;
        ctx = context;
        companyID = id;
        new CacheRetrieveTask().execute();
    }

    public static void getCachedProjectChats(Context context, Integer id, CacheUtilListener cacheUtilListener) {
        dataType = CACHE_CHAT;
        utilListener = cacheUtilListener;
        ctx = context;
        projectID = id;
        new CacheRetrieveTask().execute();
    }

    public static void getCachedProjectStatus(Context context, Integer id, CacheUtilListener cacheUtilListener) {
        Log.d(LOG, "################ getting cached project status ..................");
        dataType = CACHE_PROJECT_STATUS;
        utilListener = cacheUtilListener;
        ctx = context;
        projectID = id;
        new CacheRetrieveTask().execute();
    }


    static class CacheTask extends AsyncTask<Void, Void, Integer> {


        @Override
        protected Integer doInBackground(Void... voids) {
            String json = null;
            File file = null;
            FileOutputStream outputStream;
            try {
                switch (dataType) {

                    case CACHE_TASK_STATUS:
                        json = gson.toJson(response);
                        outputStream = ctx.openFileOutput(JSON_STATUS, Context.MODE_PRIVATE);
                        write(outputStream, json);
                        file = ctx.getFileStreamPath(JSON_STATUS);
                        if (file != null) {
                            Log.e(LOG, "ProjectTaskStatus cache written, path: " + file.getAbsolutePath() +
                                    " - length: " + file.length());
                        }
                        break;

                    case CACHE_TRACKER:
                        json = gson.toJson(response);
                        outputStream = ctx.openFileOutput(JSON_TRACKER, Context.MODE_PRIVATE);
                        write(outputStream, json);
                        file = ctx.getFileStreamPath(JSON_TRACKER);
                        if (file != null) {
                            Log.e(LOG, "Tracker cache written, path: " + file.getAbsolutePath() +
                                    " - length: " + file.length());
                        }
                        break;
                    case CACHE_MONITOR_PROJECTS:
                        json = gson.toJson(response);
                        outputStream = ctx.openFileOutput(JSON_MON_PROJECTS, Context.MODE_PRIVATE);
                        write(outputStream, json);
                        file = ctx.getFileStreamPath(JSON_MON_PROJECTS);
                        if (file != null) {
                            Log.e(LOG, "Monitor projects cache written, path: " + file.getAbsolutePath() +
                                    " - length: " + file.length());
                        }
                        break;
                    case CACHE_COMPANY:
                        json = gson.toJson(response);
                        outputStream = ctx.openFileOutput(JSON_COMPANY_DATA + companyID + ".json", Context.MODE_PRIVATE);
                        write(outputStream, json);
                        file = ctx.getFileStreamPath(JSON_COMPANY_DATA + companyID + ".json");
                        if (file != null) {
                            Log.e(LOG, "Company cache written, path: " + file.getAbsolutePath() +
                                    " - length: " + file.length());
                        }
                        break;
                    case CACHE_PROJECT:
                        json = gson.toJson(response);
                        outputStream = ctx.openFileOutput(JSON_PROJECT_DATA + projectID + ".json", Context.MODE_PRIVATE);
                        write(outputStream, json);
                        file = ctx.getFileStreamPath(JSON_PROJECT_DATA + projectID + ".json");
                        if (file != null) {
                            Log.e(LOG, "Project cache written, path: " + file.getAbsolutePath() +
                                    " - length: " + file.length());
                        }
                        break;
                    case CACHE_PROJECT_STATUS:
                        json = gson.toJson(response);
                        outputStream = ctx.openFileOutput(JSON_PROJECT_STATUS + projectID + ".json", Context.MODE_PRIVATE);
                        write(outputStream, json);
                        file = ctx.getFileStreamPath(JSON_PROJECT_STATUS + projectID + ".json");
                        if (file != null) {
                            Log.e(LOG, "Project status cache written, path: " + file.getAbsolutePath() +
                                    " - length: " + file.length());
                        }
                        break;


                    case CACHE_DATA:
                        json = gson.toJson(response);
                        outputStream = ctx.openFileOutput(JSON_DATA, Context.MODE_PRIVATE);
                        write(outputStream, json);
                        file = ctx.getFileStreamPath(JSON_DATA);
                        if (file != null) {
                            Log.e(LOG, "Data cache written, path: " + file.getAbsolutePath() +
                                    " - length: " + file.length());
                        }
                        break;
                    case CACHE_CHAT:
                        json = gson.toJson(response);
                        outputStream = ctx.openFileOutput(JSON_CHAT + projectID + ".json", Context.MODE_PRIVATE);
                        write(outputStream, json);
                        file = ctx.getFileStreamPath(JSON_CHAT + projectID + ".json");
                        if (file != null) {
                            Log.e(LOG, "Data cache written, path: " + file.getAbsolutePath() +
                                    " - length: " + file.length());
                        }
                        break;
                    case CACHE_COUNTRIES:
                        json = gson.toJson(response);
                        outputStream = ctx.openFileOutput(JSON_COUNTRIES, Context.MODE_PRIVATE);
                        write(outputStream, json);
                        file = ctx.getFileStreamPath(JSON_COUNTRIES);
                        if (file != null) {
                            Log.e(LOG, "Country cache written, path: " + file.getAbsolutePath() +
                                    " - length: " + file.length());
                        }
                        break;

                    default:
                        Log.e(LOG, "######### NOTHING done ...");
                        break;

                }

            } catch (IOException e) {
                Log.e(LOG, "Failed to cache data", e);
                return 9;
            }
            return 0;
        }

        private void write(FileOutputStream outputStream, String json) throws IOException {
            outputStream.write(json.getBytes());
            outputStream.close();
        }

        @Override
        protected void onPostExecute(Integer v) {
            if (utilListener != null) {
                if (v > 0) {
                    utilListener.onError();
                } else
                    utilListener.onDataCached();
            }

        }
    }

    static class CacheRetrieveTask extends AsyncTask<Void, Void, ResponseDTO> {

        private ResponseDTO getData(FileInputStream stream) throws IOException {
            String json = getStringFromInputStream(stream);
            ResponseDTO response = gson.fromJson(json, ResponseDTO.class);
            return response;
        }

        @Override
        protected ResponseDTO doInBackground(Void... voids) {
            ResponseDTO response = new ResponseDTO();
            FileInputStream stream;
            try {
                switch (dataType) {

                    case CACHE_COMPANY:
                        stream = ctx.openFileInput(JSON_COMPANY_DATA + companyID + ".json");
                        response = getData(stream);
                        Log.i(LOG, "++ company cache retrieved");
                        break;
                    case CACHE_PROJECT:
                        stream = ctx.openFileInput(JSON_PROJECT_DATA + projectID + ".json");
                        response = getData(stream);
                        Log.i(LOG, "++ project cache retrieved");
                        break;
                    case CACHE_CHAT:
                        stream = ctx.openFileInput(JSON_CHAT + projectID + ".json");
                        response = getData(stream);
                        Log.i(LOG, "++ project cache retrieved");
                        break;
                    case CACHE_PROJECT_STATUS:
                        stream = ctx.openFileInput(JSON_PROJECT_STATUS + projectID + ".json");
                        response = getData(stream);
                        Log.i(LOG, "++ project status cache retrieved");
                        break;
                    case CACHE_TASK_STATUS:
                        stream = ctx.openFileInput(JSON_STATUS);
                        response = getData(stream);
                        Log.i(LOG, "++ projectTaskStatus cache retrieved");
                        break;
                    case CACHE_REQUEST:
                        stream = ctx.openFileInput(JSON_REQUEST);
                        response = getData(stream);
                        Log.i(LOG, "++ request cache retrieved");
                        break;
                    case CACHE_TRACKER:
                        stream = ctx.openFileInput(JSON_TRACKER);
                        response = getData(stream);
                        Log.i(LOG, "++ tracker cache retrieved");
                        break;
                    case CACHE_MONITOR_PROJECTS:
                        stream = ctx.openFileInput(JSON_MON_PROJECTS);
                        response = getData(stream);
                        Log.i(LOG, "++ monitor projects cache retrieved");
                        break;

                    case CACHE_DATA:
                        stream = ctx.openFileInput(JSON_DATA);
                        response = getData(stream);
                        Log.i(LOG, "++ company data cache retrieved");
                        break;

                    case CACHE_COUNTRIES:
                        stream = ctx.openFileInput(JSON_COUNTRIES);
                        response = getData(stream);
                        Log.i(LOG, "++ country cache retrieved");
                        break;

                }
                response.setStatusCode(0);

            } catch (FileNotFoundException e) {
                Log.d(LOG, "#### cache file not found - returning a new response object, type = " + dataType);

            } catch (IOException e) {
                Log.v(LOG, "------------ Failed to retrieve cache", e);
            }

            return response;
        }

        @Override
        protected void onPostExecute(ResponseDTO v) {
            if (utilListener == null) return;
            utilListener.onFileDataDeserialized(v);


        }
    }

    private static String getStringFromInputStream(InputStream is) throws IOException {

        BufferedReader br = null;
        StringBuilder sb = new StringBuilder();
        String line;
        try {
            br = new BufferedReader(new InputStreamReader(is));
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

        } finally {
            if (br != null) {
                br.close();
            }
        }
        String json = sb.toString();
        return json;

    }

    static final String LOG = CacheUtil.class.getSimpleName();
    static final Gson gson = new Gson();


}

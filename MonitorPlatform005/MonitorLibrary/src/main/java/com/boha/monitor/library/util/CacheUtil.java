package com.boha.monitor.library.util;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.boha.monitor.library.dto.ProjectSiteDTO;
import com.boha.monitor.library.dto.transfer.ResponseDTO;
import com.boha.monitor.library.services.RequestCache;
import com.google.gson.Gson;




import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Date;

/**
 * Created by aubreyM on 2014/06/30.
 */
public class CacheUtil {

    public interface CacheUtilListener {
        public void onFileDataDeserialized(ResponseDTO response);

        public void onDataCached();

        public void onError();
    }

    public interface CacheRequestListener {
        public void onDataCached();

        public void onRequestCacheReturned(RequestCache cache);

        public void onError();
    }

    public interface CacheSiteListener {
        public void onSiteReturnedFromCache(ProjectSiteDTO site);

        public void onDataCached();

        public void onError();
    }

    static CacheUtilListener utilListener;
    static CacheRequestListener cacheListener;
    static CacheSiteListener siteListener;
    public static final int CACHE_DATA = 1, CACHE_COUNTRIES = 3, CACHE_SITE = 7,
            CACHE_PROJECT = 5, CACHE_REQUEST = 6, CACHE_PROJECT_STATUS = 4,
            CACHE_TRACKER = 8, CACHE_CHAT = 9;
    static int dataType;
    static Integer projectID;
    static ResponseDTO response;
    static ProjectSiteDTO projectSite;
    static Integer projectSiteID;
    static SessionPhoto sessionPhoto;
    static Context ctx;
    static RequestCache requestCache;
    static final String JSON_DATA = "data.json", JSON_COUNTRIES = "countries.json",
            JSON_PROJECT_DATA = "project_data", JSON_PROJECT_STATUS = "project_status",
            JSON_REQUEST = "requestCache.json", JSON_SITE = "site", JSON_TRACKER = "tracker.json", JSON_CHAT = "chat";


    public static void cacheRequest(Context context, RequestCache cache, CacheRequestListener listener) {
        requestCache = cache;
        dataType = CACHE_REQUEST;
        cacheListener = listener;
        ctx = context;
        new CacheTask().execute();
    }

    public static void cacheData(Context context, ResponseDTO r, int type, CacheUtilListener cacheUtilListener) {
        dataType = type;
        response = r;
        response.setLastCacheDate(new Date());
        utilListener = cacheUtilListener;
        ctx = context;
        new CacheTask().execute();
    }

    public static void cacheProjectData(Context context, ResponseDTO r, Integer pID, CacheUtilListener cacheUtilListener) {
        dataType = CACHE_PROJECT;
        response = r;
        response.setLastCacheDate(new Date());
        utilListener = cacheUtilListener;
        projectID = pID;
        ctx = context;
        new CacheTask().execute();
    }
    public static void cacheProjectChats(Context context, ResponseDTO r, Integer pID, CacheUtilListener cacheUtilListener) {
        dataType = CACHE_CHAT;
        response = r;
        response.setLastCacheDate(new Date());
        utilListener = cacheUtilListener;
        projectID = pID;
        ctx = context;
        new CacheTask().execute();
    }

    public static void cacheProjectStatus(Context context, ResponseDTO r, Integer pID, CacheUtilListener cacheUtilListener) {
        dataType = CACHE_PROJECT_STATUS;
        response = r;
        response.setLastCacheDate(new Date());
        utilListener = cacheUtilListener;
        projectID = pID;
        ctx = context;
        new CacheTask().execute();
    }

    public static void cacheTrackerData(Context context, ResponseDTO r, CacheUtilListener cacheUtilListener) {
        dataType = CACHE_TRACKER;
        response = r;
        response.setLastCacheDate(new Date());
        utilListener = cacheUtilListener;
        ctx = context;
        new CacheTask().execute();
    }

    public static void cacheSiteData(Context context,  ProjectSiteDTO r,
                                     CacheSiteListener l) {
        dataType = CACHE_SITE;
        projectSite = r;
        siteListener = l;
        projectSiteID = r.getProjectSiteID();
        ctx = context;
        new CacheTask().execute();
    }


    public static void getCachedData(Context context, int type, CacheUtilListener cacheUtilListener) {
        dataType = type;
        utilListener = cacheUtilListener;
        ctx = context;
        new CacheRetrieveTask().execute();
    }


    public static void getCachedRequests(Context context, CacheRequestListener listener) {
        dataType = CACHE_REQUEST;
        cacheListener = listener;
        ctx = context;
        new CacheRetrieveRequestTask().execute();
    }

    public static void getCachedTrackerData(Context context, CacheUtilListener cacheUtilListener) {
        dataType = CACHE_TRACKER;
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

    public static void getCachedSiteData(Context context, Integer id, CacheSiteListener l) {
        Log.d(LOG, "################ getting cached site data ..................");
        dataType = CACHE_SITE;
        siteListener = l;
        ctx = context;
        projectSiteID = id;
        new CacheRetrieveSiteTask().execute();
    }


    static class CacheTask extends AsyncTask<Void, Void, Integer> {


        @Override
        protected Integer doInBackground(Void... voids) {
            String json = null;
            File file = null;
            FileOutputStream outputStream;
            try {
                switch (dataType) {

                    case CACHE_REQUEST:
                        json = gson.toJson(requestCache);
                        outputStream = ctx.openFileOutput(JSON_REQUEST, Context.MODE_PRIVATE);
                        write(outputStream, json);
                        file = ctx.getFileStreamPath(JSON_REQUEST);
                        if (file != null) {
                            Log.e(LOG, "Request cache written, path: " + file.getAbsolutePath() +
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
                    case CACHE_SITE:
                        json = gson.toJson(projectSite);
                        outputStream = ctx.openFileOutput(JSON_SITE + projectSiteID + ".json", Context.MODE_PRIVATE);
                        write(outputStream, json);
                        file = ctx.getFileStreamPath(JSON_SITE + projectSiteID + ".json");
                        if (file != null) {
                            Log.e(LOG, "Site cache written, path: " + file.getAbsolutePath() +
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

        private void write( FileOutputStream outputStream,  String json) throws IOException {
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
            if (cacheListener != null) {
                if (v > 0) {
                    cacheListener.onError();
                } else {
                    cacheListener.onDataCached();
                }
            }
            if (siteListener != null) {
                if (v > 0) {
                    siteListener.onError();
                } else {
                    siteListener.onDataCached();
                }
            }
        }
    }

    static class CacheRetrieveTask extends AsyncTask<Void, Void, ResponseDTO> {

        private ResponseDTO getData( FileInputStream stream) throws IOException {
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

    static class CacheRetrieveSiteTask extends AsyncTask<Void, Void, ProjectSiteDTO> {

        private ProjectSiteDTO getData( FileInputStream stream) throws IOException {
            String json = getStringFromInputStream(stream);
            ProjectSiteDTO response = gson.fromJson(json, ProjectSiteDTO.class);
            return response;
        }


        @Override
        protected ProjectSiteDTO doInBackground(Void... voids) {
            ProjectSiteDTO site = null;
            FileInputStream stream;
            try {

                stream = ctx.openFileInput(JSON_SITE + projectSiteID + ".json");
                site = getData(stream);
                Log.i(LOG, "++ site cache retrieved");

            } catch (FileNotFoundException e) {
                Log.d(LOG, "## site cache file not found. not initialised yet. no problem");


            } catch (IOException e) {
                Log.v(LOG, "-- Failed to retrieve cache", e);
            }

            return site;
        }

        @Override
        protected void onPostExecute( ProjectSiteDTO result) {
            if (siteListener == null) return;
            if (result != null) {
                siteListener.onSiteReturnedFromCache(result);
            } else {
                Log.e(LOG, "-- No cache, util returns null site object");
                siteListener.onError();
            }

        }
    }

    static class CacheRetrieveRequestTask extends AsyncTask<Void, Void, RequestCache> {

        private RequestCache getData( FileInputStream stream) throws IOException {
            String json = getStringFromInputStream(stream);
            RequestCache cache = gson.fromJson(json, RequestCache.class);
            return cache;
        }


        @Override
        protected RequestCache doInBackground(Void... voids) {
            RequestCache cache = null;
            FileInputStream stream;
            try {
                stream = ctx.openFileInput(JSON_REQUEST);
                cache = getData(stream);
                Log.i(LOG, "++ request cache retrieved");
            } catch (FileNotFoundException e) {
                Log.d(LOG, "## cache file not found. not initialised yet. no problem, creating new cache");
                cache = new RequestCache();

            } catch (IOException e) {
                Log.v(LOG, "-- Failed to retrieve cache", e);
            }

            return cache;
        }

        @Override
        protected void onPostExecute( RequestCache v) {
            if (cacheListener == null) return;
            if (v != null) {
                cacheListener.onRequestCacheReturned(v);
            } else {
                Log.e(LOG, "------ No cache, util returns null response object");
                cacheListener.onError();
            }

        }
    }



    private static String getStringFromInputStream( InputStream is) throws IOException {

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

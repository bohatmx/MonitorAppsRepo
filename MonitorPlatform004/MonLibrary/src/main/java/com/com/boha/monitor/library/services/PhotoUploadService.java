package com.com.boha.monitor.library.services;

import android.app.IntentService;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.com.boha.monitor.library.dto.transfer.PhotoUploadDTO;
import com.com.boha.monitor.library.dto.transfer.ResponseDTO;
import com.com.boha.monitor.library.util.CacheUtil;
import com.com.boha.monitor.library.util.PhotoCache;
import com.com.boha.monitor.library.util.PictureUtil;
import com.com.boha.monitor.library.util.Util;
import com.com.boha.monitor.library.util.WebCheck;
import com.com.boha.monitor.library.util.WebCheckResult;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 */
public class PhotoUploadService extends IntentService {

    public PhotoUploadService() {
        super("PhotoUploadService");
    }
    public void uploadCachedPhotos() {

        Log.d(LOG, "#### uploadCachedPhotos, getting cached photos - will start uploads if wifi is up");
        CacheUtil.getCachedData(getApplicationContext(), CacheUtil.CACHE_PHOTOS, new CacheUtil.CacheUtilListener() {
            @Override
            public void onFileDataDeserialized(ResponseDTO response) {
                Log.e(LOG, "##### cached photo list returned: " + response.getPhotoCache().getPhotoUploadList().size());
                list = response.getPhotoCache().getPhotoUploadList();
                if (list.isEmpty()) {
                    Log.w(LOG,"--- no cached photos for download");
                    return;
                }
                getLog(response.getPhotoCache());
                webCheckResult = WebCheck.checkNetworkAvailability(getApplicationContext());
                if (!webCheckResult.isWifiConnected()) {
                    Log.e(LOG,"--- uploadCachedPhotos exiting. no wifi connected");
                    return;
                }
                onHandleIntent(null);
            }

            @Override
            public void onDataCached() {

            }

            @Override
            public void onError() {

            }
        });
    }

    private static void getLog(PhotoCache cache) {
        StringBuilder sb = new StringBuilder();
        sb.append("## Photos currently in the cache: ")
                .append(cache.getPhotoUploadList().size()).append("\n");
        for (PhotoUploadDTO p : cache.getPhotoUploadList()) {
            sb.append("+++ ").append(p.getDateTaken().toString()).append(" lat: ").append(p.getLatitude());
            sb.append(" lng: ").append(p.getLatitude()).append(" acc: ").append(p.getAccuracy());
            if (p.getDateThumbUploaded() != null)
                sb.append(" ").append(sdf.format(p.getDateThumbUploaded())).append("\n");
            else
                sb.append(" NOT UPLOADED\n");

        }
        Log.w(LOG, sb.toString());
    }


    static final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
    @Override
    protected void onHandleIntent(Intent intent) {
        Log.w(LOG,"## onHandleIntent");
        if (list == null) {
            uploadCachedPhotos();
            return;
        }
        webCheckResult = WebCheck.checkNetworkAvailability(getApplicationContext());
        if (webCheckResult.isWifiConnected()) {
            controlThumbUploads();
        }


    }

    static List<PhotoUploadDTO> list;
    int index;
    WebCheckResult webCheckResult;

    private void controlThumbUploads() {
        if (index < list.size()) {
            if (list.get(index).getDateThumbUploaded() == null) {
                executeThumbUpload(list.get(index));
            } else {
                index++;
                controlThumbUploads();
                return;
            }
        }
        if (index == list.size()) {
            PhotoCache pc = new PhotoCache();
            for (PhotoUploadDTO photo: list) {
                if (photo.getDateThumbUploaded() == null) {
                    pc.getPhotoUploadList().add(photo);
                }
            }
            Log.w(LOG,"&&& cleaning cache right up! photos still pending: " + pc.getPhotoUploadList().size());
            ResponseDTO r = new ResponseDTO();
            r.setPhotoCache(pc);

            CacheUtil.cacheData(getApplicationContext(),r,CacheUtil.CACHE_PHOTOS, new CacheUtil.CacheUtilListener() {
                @Override
                public void onFileDataDeserialized(ResponseDTO response) {

                }

                @Override
                public void onDataCached() {
                    Log.i(LOG, "## cleaned up photo cache OK");
                }

                @Override
                public void onError() {

                }
            });
        }


    }

    public static List<PhotoUploadDTO> getPhotoList() {
        return list;
    }

    public int getIndex() {
        return index;
    }

    private void controlFullPictureUploads() {

        if (index < list.size()) {
            if (list.get(index).getDateFullPictureUploaded() == null) {
                executeFullPictureUpload(list.get(index));
            } else {
                index++;
                controlFullPictureUploads();
            }
        }
        //
        Log.w(LOG, "*** check and remove photos uploaded from cache");
        List<PhotoUploadDTO> pendingList = new ArrayList<>();
        for (PhotoUploadDTO dto : list) {
            if (dto.getDateThumbUploaded() == null || dto.getDateFullPictureUploaded() == null) {
                pendingList.add(dto);
            }
        }
        list = pendingList;
        saveCache();

    }

    private void executeThumbUpload(final PhotoUploadDTO dto) {
        Log.d(LOG, "** executeThumbUpload, projectSiteID: " + dto.getProjectSiteID());
        dto.setFullPicture(false);
        if (dto.getPictureType() == 0) dto.setPictureType(PhotoUploadDTO.PROJECT_IMAGE);
        final long start = System.currentTimeMillis();
        PictureUtil.uploadImage(dto, false, getApplicationContext(), new PhotoUploadDTO.PhotoUploadedListener() {
            @Override
            public void onPhotoUploaded() {
                long end = System.currentTimeMillis();
                Log.i(LOG, "---- thumbnail uploaded, elapsed: " + Util.getElapsed(start,end) + " seconds");
                dto.setDateThumbUploaded(new Date());
                saveCache();
                index++;
                controlThumbUploads();
            }

            @Override
            public void onPhotoUploadFailed() {
                Log.e(LOG, "------<< onPhotoUploadFailed - check and tell someone");
            }
        });
    }

    private void executeFullPictureUpload(final PhotoUploadDTO dto) {
        final long start = System.currentTimeMillis();
        Log.d(LOG, "*** executeFullPictureUpload, path: " + dto.getImageFilePath());
        dto.setFullPicture(true);
        PictureUtil.uploadImage(dto, true, getApplicationContext(), new PhotoUploadDTO.PhotoUploadedListener() {
            @Override
            public void onPhotoUploaded() {
                long end = System.currentTimeMillis();
                Log.i(LOG, "---- full picture uploaded, elapsed: " + (end - start) + " ms");
                dto.setDateFullPictureUploaded(new Date());
                saveCache();
                index++;
                controlFullPictureUploads();
            }

            @Override
            public void onPhotoUploadFailed() {
                Log.e(LOG, "------<< onPhotoUploadFailed - check and tell someone");
            }
        });
    }

    private void saveCache() {
        Log.d(LOG, "*** saveCache starting............");
        ResponseDTO r = new ResponseDTO();
        PhotoCache pc = new PhotoCache();
        pc.setPhotoUploadList(list);
        r.setPhotoCache(pc);
        CacheUtil.cacheData(getApplicationContext(), r, CacheUtil.CACHE_PHOTOS, new CacheUtil.CacheUtilListener() {
            @Override
            public void onFileDataDeserialized(ResponseDTO response) {

            }

            @Override
            public void onDataCached() {

            }

            @Override
            public void onError() {

            }
        });
    }

    static final String LOG = PhotoUploadService.class.getSimpleName();

    public class LocalBinder extends Binder {
        public PhotoUploadService getService() {
            return PhotoUploadService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    private final IBinder mBinder = new LocalBinder();

}

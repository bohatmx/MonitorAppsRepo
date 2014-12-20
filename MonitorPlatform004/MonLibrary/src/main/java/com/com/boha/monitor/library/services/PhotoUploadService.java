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
import com.com.boha.monitor.library.util.WebCheck;
import com.com.boha.monitor.library.util.WebCheckResult;

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
        webCheckResult = WebCheck.checkNetworkAvailability(getApplicationContext());
        if (!webCheckResult.isWifiConnected()) {
            Log.w(LOG,"## uploadCachedPhotos exiting. no wifi connected");
            return;
        }
        Log.e(LOG, "#### uploadCachedPhotos, getting cached photos - will start uploads");
        CacheUtil.getCachedData(getApplicationContext(), CacheUtil.CACHE_PHOTOS, new CacheUtil.CacheUtilListener() {
            @Override
            public void onFileDataDeserialized(ResponseDTO response) {
                Log.e(LOG, "##### cached photo list returned: " + response.getPhotoCache().getPhotoUploadList().size());
                getLog(response.getPhotoCache());
                list = response.getPhotoCache().getPhotoUploadList();
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
        sb.append("Photos currently in the cache: ")
                .append(cache.getPhotoUploadList().size()).append("\n");
        for (PhotoUploadDTO p : cache.getPhotoUploadList()) {
            sb.append("+++ ").append(p.getDateTaken().toString()).append(" lat: ").append(p.getLatitude());
            sb.append(" lng: ").append(p.getLatitude()).append(" acc: ").append(p.getAccuracy()).append("\n");
        }
        Log.d(LOG, sb.toString());
    }


    @Override
    protected void onHandleIntent(Intent intent) {
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
            }
        }
        if (index == list.size()) {
            index = 0;
            controlFullPictureUploads();
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
        Log.d(LOG, "*** executeThumbUpload, file: " + dto.getThumbFilePath());
        dto.setFullPicture(false);
        if (dto.getPictureType() == 0) dto.setPictureType(PhotoUploadDTO.PROJECT_IMAGE);
        final long start = System.currentTimeMillis();
        PictureUtil.uploadImage(dto, false, getApplicationContext(), new PhotoUploadDTO.PhotoUploadedListener() {
            @Override
            public void onPhotoUploaded() {
                long end = System.currentTimeMillis();
                Log.i(LOG, "---- thumbnail uploaded, elapsed: " + (end - start) + " ms");
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

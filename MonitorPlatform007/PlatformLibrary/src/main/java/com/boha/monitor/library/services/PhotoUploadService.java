package com.boha.monitor.library.services;

import android.app.IntentService;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.boha.monitor.library.activities.MonApp;
import com.boha.monitor.library.dto.PhotoUploadDTO;
import com.boha.monitor.library.dto.ProjectDTO;
import com.boha.monitor.library.dto.ResponseDTO;
import com.boha.monitor.library.util.CDNUploader;
import com.boha.monitor.library.util.Snappy;
import com.boha.monitor.library.util.WebCheck;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages the uploading of photos from a list held in cache. Uploads each photo
 * and notifies any activity bound to it on completion. Otherwise these cached photos
 * are uploaded in a silent process not visible to the user.
 * <p/>
 * It may be started by a startService call or may be bound to an activity via the
 * IBinder interface.
 * <p/>
 * Entry points: onHandleIntent, uploadCachedVideos
 */
public class PhotoUploadService extends IntentService {

    public PhotoUploadService() {
        super("PhotoUploadService");
    }

    List<PhotoUploadDTO> uploadedList = new ArrayList<>();
    public static final String BROADCAST_ACTION =
            "com.boha.monitor.PHOTO.UPLOADED";

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(LOG, "## PhotoUploadService onHandleIntent .... starting service");
        if (WebCheck.checkNetworkAvailability(getApplicationContext()).isNetworkUnavailable()) {
            Log.e(LOG, "--- No Network: boolean = isNetworkUnavailable");
            return;
        }
        Snappy.getPhotosForUpload((MonApp) getApplication(), new Snappy.SnappyReadListener() {
            @Override
            public void onDataRead(ResponseDTO response) {
                if (response.getPhotoUploadList().isEmpty()) {
                    Log.w(LOG,"++++ no photos found for upload, quittin ...");
                    return;
                } else {
                    Log.w(LOG,"++++ getting ready to upload photos: " +
                    response.getPhotoUploadList().size());
                }
                uploadedList = new ArrayList<>();
                list = response.getPhotoUploadList();
                index = 0;
                controlUploads();
            }

            @Override
            public void onError(String message) {

            }
        });

    }

    List<PhotoUploadDTO> list;
    int index;
    public static final String PHOTO_UPLOADED = "photoUploaded";

    private void controlUploads() {
        if (index < list.size()) {
            if (list.get(index).getDateUploaded() == null) {
                executeUpload(list.get(index));
            } else {
                index++;
                controlUploads();
            }

        } else {
            updateProject();
        }

    }


    private void executeUpload(final PhotoUploadDTO dto) {

        final MonApp monApp = (MonApp) getApplication();
        CDNUploader.uploadFile(getApplicationContext(), dto, new CDNUploader.CDNUploaderListener() {
            @Override
            public void onFileUploaded(final PhotoUploadDTO photo) {
                uploadedList.add(dto);
                List<PhotoUploadDTO> list = new ArrayList<PhotoUploadDTO>();
                list.add(photo);
                Snappy.writePhotoList(monApp, list, new Snappy.PhotoListener() {
                    @Override
                    public void onPhotoAdded() {
                        Log.d(LOG,"uploaded photo added to snappy");
                        Snappy.deletePhotoUploaded((MonApp) getApplication(),
                                photo, new Snappy.SnappyDeleteListener() {
                            @Override
                            public void onDataDeleted() {

                            }

                            @Override
                            public void onError(String message) {

                            }
                        });
                    }

                    @Override
                    public void onPhotosFound(List<PhotoUploadDTO> list) {

                    }

                    @Override
                    public void onError(String message) {

                    }
                });
                index++;
                controlUploads();
            }

            @Override
            public void onError(String message) {
                Log.e(LOG, message);
                failedUploads.add(dto);
                index++;
                controlUploads();
            }
        });

    }

    private void updateProject() {
        if (uploadedList.isEmpty()) {
            return;
        }
        if (uploadedList.get(0).getProjectID() != null) {
            Snappy.getProject((MonApp) getApplication(),
                    uploadedList.get(0).getProjectID(),
                    new Snappy.SnappyProjectListener() {
                @Override
                public void onProjectFound(ProjectDTO project) {
                    project.getPhotoUploadList().addAll(0,uploadedList);
                    project.setPhotoCount(project.getPhotoUploadList().size());

                    List<ProjectDTO> list = new ArrayList<>();
                    list.add(project);
                    Snappy.writeProjectList((MonApp) getApplication(), list, new Snappy.SnappyWriteListener() {
                        @Override
                        public void onDataWritten() {
                            Log.w(LOG,"******* PhotoUploadService complete, photos uploaded: " +
                                    uploadedList.size() + " - Broadcasting successful uploadToYouTube ...");

                            Intent m = new Intent(BROADCAST_ACTION);
                            m.putExtra(PHOTO_UPLOADED,true);
                            LocalBroadcastManager.getInstance(getApplicationContext())
                                    .sendBroadcast(m);
                        }

                        @Override
                        public void onError(String message) {

                        }
                    });
                }

                @Override
                public void onError() {

                }
            });
        }
    }

    List<PhotoUploadDTO> failedUploads = new ArrayList<>();
    static final String LOG = PhotoUploadService.class.getSimpleName();

}

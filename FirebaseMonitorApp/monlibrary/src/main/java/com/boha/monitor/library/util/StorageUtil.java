package com.boha.monitor.library.util;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;

import com.boha.monitor.library.data.MonitorDTO;
import com.boha.monitor.library.data.PhotoUploadDTO;
import com.boha.monitor.library.data.ProjectDTO;
import com.boha.monitor.library.data.StaffDTO;
import com.boha.monitor.library.data.UserDTO;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.crash.FirebaseCrash;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Random;

/**
 * Created by aubreymalabie on 5/25/16.
 */
public class StorageUtil {

    public interface StorageListener {
        void onUploaded(String key);

        void onError(String message);
    }

    static final String STORAGE_URL = "gs://supervisor-m30-backend.appspot.com/",
            TAG = StorageUtil.class.getSimpleName();

    static FirebaseStorage storage;

    public static void uploadUserPhoto(final UserDTO user, final PhotoUploadDTO p, final StorageListener listener) {
        if (storage == null) {
            storage = FirebaseStorage.getInstance();
        }
        Log.d(TAG, "uploadUserPhoto: starting photo upload to Firebase Storage");
        StorageReference ref = storage.getReferenceFromUrl(STORAGE_URL);
        StorageReference imagesRef = ref
                .child(p.getCompanyID())
                .child(DataUtil.USER_PHOTOS)
                .child(user.getUserID())
                .child(DataUtil.PHOTOS)
                .child("" + p.getDateTaken());

        File file = new File(p.getFilePath());
        try {
            UploadTask task = imagesRef.putStream(new FileInputStream(file));
            task.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Handle unsuccessful uploads
                    Log.e(TAG, "onFailure: ", exception);
                    if (listener != null)
                        listener.onError("Unable to upload the photo");
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                    Log.w(TAG, "onSuccess: photo uploaded, url: " + taskSnapshot.getDownloadUrl().toString());
                    Log.d(TAG, "onSuccess: taskSnapshot: " + taskSnapshot);
                    final Uri downloadUrl = taskSnapshot.getDownloadUrl();
                    p.setUrl(downloadUrl.toString());

                    addUserPhotoToDatabase(p, new StorageListener() {
                        @Override
                        public void onUploaded(String key) {
                            listener.onUploaded(key);
                        }

                        @Override
                        public void onError(String message) {

                        }
                    });
                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                    Log.e(TAG, "onProgress: " + taskSnapshot.getBytesTransferred() + " of " + taskSnapshot.getTotalByteCount());
                }
            });
        } catch (FileNotFoundException e) {
            if (listener != null)
                listener.onError(e.getMessage());
        }
    }
    public static void uploadProjectPhoto(final PhotoUploadDTO p, final StorageListener listener) {
        if (storage == null) {
            storage = FirebaseStorage.getInstance();
        }
        Log.d(TAG, "uploadProjectPhoto: starting photo upload to Firebase Storage");
        StorageReference ref = storage.getReferenceFromUrl(STORAGE_URL);
        StorageReference imagesRef = ref.child(p.getCompanyID())
                .child(DataUtil.PROJECT_PHOTOS)
                .child(p.getProjectID())
                .child(DataUtil.PHOTOS)
                .child("" + p.getDateTaken());

        File file = new File(p.getFilePath());
        try {
            UploadTask task = imagesRef.putStream(new FileInputStream(file));
            task.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Handle unsuccessful uploads
                    Log.e(TAG, "onFailure: ", exception);
                    if (listener != null)
                        listener.onError("Unable to upload the photo");
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                    Log.w(TAG, "onSuccess: photo uploaded, url: " + taskSnapshot.getDownloadUrl().toString());
                    Log.d(TAG, "onSuccess: taskSnapshot: " + taskSnapshot);
                    Uri downloadUrl = taskSnapshot.getDownloadUrl();
                    p.setUrl(downloadUrl.toString());
                    addProjectPhotoToDatabase(p, new StorageListener() {
                        @Override
                        public void onUploaded(String key) {
                            if (listener != null)
                                listener.onUploaded(key);
                        }

                        @Override
                        public void onError(String message) {
                            if (listener != null)
                                listener.onError(message);
                        }
                    });

                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                    Log.e(TAG, "onProgress: " + taskSnapshot.getBytesTransferred() + " of " + taskSnapshot.getTotalByteCount());
                }
            });
        } catch (FileNotFoundException e) {
            if (listener != null)
                listener.onError(e.getMessage());
        }
    }

    public static void addUserPhotoToDatabase(final PhotoUploadDTO p, final StorageListener listener) {
        final FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference ref = db.getReference(DataUtil.MONITOR_DB)
                .child(DataUtil.USERS)
                .child(p.getUserID())
                .child(DataUtil.PHOTOS);

        ref.push().setValue(p, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                if (databaseError == null) {
                    Log.w(TAG, "onComplete: monitor photo added to database");
                    FirebaseCrash.log("Monitor Photo added to database: " + databaseReference.getKey() + "\n"
                            + p.getUrl());
                    if (listener != null)
                        listener.onUploaded(databaseReference.getKey());
                } else {
                    Log.e(TAG, "onComplete: error" + databaseError.toString());
                    FirebaseCrash.log("Error adding monitor photo to database: " + databaseError.toString());
                    if (listener != null)
                        listener.onError("Unable to add monitor photo to database");
                }
            }
        });
    }

    public static void addProjectPhotoToDatabase(final PhotoUploadDTO p, final StorageListener listener) {
        final FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference ref = db.getReference(DataUtil.MONITOR_DB)
                .child(DataUtil.COMPANIES)
                .child(p.getCompanyID())
                .child(DataUtil.PROJECTS)
                .child(p.getProjectID())
                .child(DataUtil.PHOTOS);

        ref.push().setValue(p, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                if (databaseError == null) {
                    Log.w(TAG, "onComplete: photo added to database");
                    FirebaseCrash.log("Photo added to database: " + databaseReference.getKey() + "\n"
                            + p.getUrl());
                    if (listener != null)
                        listener.onUploaded(databaseReference.getKey());
                    p.setPhotoUploadID(databaseReference.getKey());
                    addProjectPhotoToGeofire(p, null);
                } else {
                    Log.e(TAG, "onComplete: error" + databaseError.toString());
                    FirebaseCrash.log("Error adding photo to database: " + databaseError.toString());
                    if (listener != null)
                        listener.onError("Unable to add user photo to database");
                }
            }
        });
    }

    static FirebaseDatabase db;
    static final String GEOFIRE_URL = "https://supervisor-m30-backend.firebaseio.com/_geofire",
            AT = "@";

    static Random random = new Random(System.currentTimeMillis());

    public static void addProjectPhotoToGeofire(final PhotoUploadDTO p, final StorageListener listener) {
        if (db == null)
            db = FirebaseDatabase.getInstance();
        GeoFire geo = new GeoFire(new Firebase(GEOFIRE_URL));
        StringBuilder sb = new StringBuilder();
        sb.append(p.getCompanyID());
        sb.append(AT).append(p.getProjectID());
        sb.append(AT).append(stripBlanks(p.getProjectName()));
        sb.append(AT).append(System.currentTimeMillis());
        sb.append(AT).append(random.nextInt(10000));
        String key = sb.toString();

        geo.setLocation(key, new GeoLocation(p.getLatitude(), p.getLongitude()),
                new GeoFire.CompletionListener() {
                    @Override
                    public void onComplete(String key, FirebaseError error) {
                        if (error == null) {
                            Log.e(TAG, "********* Geofireon - Complete: geofire location saved: " + key);
                            if (listener != null)
                                listener.onUploaded(key);
                        } else {
                            if (listener != null)
                                listener.onError("Unable to add project to Geofire");
                        }

                    }
                });
    }

    public static void addProjectToGeofire(final ProjectDTO p, final StorageListener listener) {
        if (db == null)
            db = FirebaseDatabase.getInstance();
        GeoFire geo = new GeoFire(new Firebase(GEOFIRE_URL));
        StringBuilder sb = new StringBuilder();
        sb.append(p.getCompanyID());
        sb.append(AT).append(p.getProjectID());
        sb.append(AT).append(stripBlanks(p.getProjectName()));
        String key = sb.toString();

        geo.setLocation(key, new GeoLocation(p.getLatitude(), p.getLongitude()),
                new GeoFire.CompletionListener() {
                    @Override
                    public void onComplete(String key, FirebaseError error) {
                        if (error == null) {
                            Log.e(TAG, "********* Geofireon - Complete: geofire location saved: " + key);
                            if (listener != null)
                                listener.onUploaded(key);
                        } else {
                            if (listener != null)
                                listener.onError("Unable to add project to Geofire");
                        }

                    }
                });
    }
    public static void addMonitorToGeofire(final MonitorDTO p,
                                           double latitude, double longitude,
                                           final StorageListener listener) {
        if (db == null)
            db = FirebaseDatabase.getInstance();

        GeoFire geo = new GeoFire(new Firebase(GEOFIRE_URL));
        StringBuilder sb = new StringBuilder();
        sb.append(p.getCompanyID());
        sb.append(AT).append(p.getMonitorID());
        sb.append(AT).append(stripBlanks(p.getFullName()));
        sb.append(AT).append(System.currentTimeMillis());
        sb.append(AT).append("MONITOR");
        String key = sb.toString();

        geo.setLocation(key, new GeoLocation(latitude, longitude),
                new GeoFire.CompletionListener() {
                    @Override
                    public void onComplete(String key, FirebaseError error) {
                        if (error == null) {
                            Log.e(TAG, "********* Geofireon - Complete: geofire location saved: " + key);
                            if (listener != null)
                                listener.onUploaded(key);
                        } else {
                            if (listener != null)
                                listener.onError("Unable to add project to Geofire");
                        }

                    }
                });
    }
    public static void addStaffToGeofire(final StaffDTO p,
                                           double latitude, double longitude,
                                           final StorageListener listener) {
        if (db == null)
            db = FirebaseDatabase.getInstance();

        GeoFire geo = new GeoFire(new Firebase(GEOFIRE_URL));
        StringBuilder sb = new StringBuilder();
        sb.append(p.getCompanyID());
        sb.append(AT).append(p.getStaffID());
        sb.append(AT).append(stripBlanks(p.getFirstName())).append(stripBlanks(p.getLastName()));
        sb.append(AT).append(System.currentTimeMillis());
        sb.append(AT).append("STAFF");
        String key = sb.toString();

        geo.setLocation(key, new GeoLocation(latitude, longitude),
                new GeoFire.CompletionListener() {
                    @Override
                    public void onComplete(String key, FirebaseError error) {
                        if (error == null) {
                            Log.e(TAG, "********* Geofireon - Complete: geofire location saved: " + key);
                            if (listener != null)
                                listener.onUploaded(key);
                        } else {
                            if (listener != null)
                                listener.onError("Unable to add project to Geofire");
                        }

                    }
                });
    }

    private static String stripBlanks(String s) {
        return s.replaceAll("\\s+","");
    }


}

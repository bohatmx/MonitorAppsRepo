package com.boha.monitor.firebase.util;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;

import com.boha.monitor.firebase.dto.MonitorDTO;
import com.boha.monitor.firebase.dto.PhotoUploadDTO;
import com.boha.monitor.firebase.dto.ProjectDTO;
import com.boha.monitor.firebase.dto.StaffDTO;
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

    public static void uploadProjectPhoto(final PhotoUploadDTO p, final StorageListener listener) {
        if (storage == null) {
            storage = FirebaseStorage.getInstance();
        }
        Log.d(TAG, "uploadProjectPhoto: starting photo upload to Firebase Storage");
        StorageReference ref = storage.getReferenceFromUrl(STORAGE_URL);
        StorageReference imagesRef = ref.child(p.getCompanyID()).child("photos").child(p.getProjectID());

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
                    DataUtil.addProjectPhoto(p, new DataUtil.DataAddedListener() {
                        @Override
                        public void onResponse(String key) {
                            addProjectPhotoToGeofire(p, null);
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
    public static void addMonitorPhotoToDatabase(final PhotoUploadDTO p, final StorageListener listener) {
        final FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference ref = db.getReference(DataUtil.MONITOR_DB)
                .child(DataUtil.COMPANIES).child(p.getCompanyID())
                .child("monitors")
                .child(p.getMonitorID());

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
    public static void addStaffPhotoToDatabase(final PhotoUploadDTO p, final StorageListener listener) {
        final FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference ref = db.getReference(DataUtil.MONITOR_DB)
                .child(DataUtil.COMPANIES).child(p.getCompanyID())
                .child("staff")
                .child(p.getStaffID());

        ref.push().setValue(p, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                if (databaseError == null) {
                    Log.w(TAG, "onComplete: staff photo added to database");
                    FirebaseCrash.log("Staff Photo added to database: " + databaseReference.getKey() + "\n"
                            + p.getUrl());
                    if (listener != null)
                        listener.onUploaded(databaseReference.getKey());
                } else {
                    Log.e(TAG, "onComplete: error" + databaseError.toString());
                    FirebaseCrash.log("Error adding staff photo to database: " + databaseError.toString());
                    if (listener != null)
                        listener.onError("Unable to add staff photo to database");
                }
            }
        });
    }
    public static void addProjectPhotoToDatabasex(final PhotoUploadDTO p, final StorageListener listener) {
        final FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference ref = db.getReference(DataUtil.MONITOR_DB)
                .child(DataUtil.COMPANIES).child(p.getCompanyID())
                .child(p.getProjectID())
                .child("photos");

        ref.push().setValue(p, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                if (databaseError == null) {
                    Log.w(TAG, "onComplete: photo added to database");
                    FirebaseCrash.log("Photo added to database: " + databaseReference.getKey() + "\n"
                            + p.getUrl());
                    if (listener != null)
                        listener.onUploaded(databaseReference.getKey());
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
        sb.append(AT).append(stripBlanks(p.getFirstName())).append(stripBlanks(p.getLastName()));
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

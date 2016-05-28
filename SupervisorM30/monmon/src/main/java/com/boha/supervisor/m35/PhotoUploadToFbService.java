package com.boha.supervisor.m35;

import android.app.IntentService;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class PhotoUploadToFbService extends IntentService {
    static final String TAG = PhotoUploadToFbService.class.getSimpleName();
    public PhotoUploadToFbService() {
        super("PhotoUploadToFbService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {

        }
    }
    private void uploadFromUri(Uri fileUri) {
        Log.d(TAG, "uploadFromUri:src:" + fileUri.toString());
        StorageReference mStorageRef = FirebaseStorage.getInstance().getReference();
        // [START get_child_ref]
        // Get a reference to store file at photos/<FILENAME>.jpg
        final StorageReference photoRef = mStorageRef.child("photos")
                .child(fileUri.getLastPathSegment());
        // [END get_child_ref]

        // Upload file to Firebase Storage
        // [START_EXCLUDE]
        // [END_EXCLUDE]
        Log.d(TAG, "uploadFromUri:dst:" + photoRef.getPath());
//        photoRef.putFile(fileUri)
//                .addOnSuccessListener(this, new OnSuccessListener<UploadTask.TaskSnapshot>() {
//                    @Override
//                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
//                        // Upload succeeded
//                        Log.d(TAG, "uploadFromUri:onSuccess");
//
//                        // Get the public download URL
//                        Uri mDownloadUrl = taskSnapshot.getMetadata().getDownloadUrl();
//
//
//                    }
//                })
//                .addOnFailureListener(this, new OnFailureListener() {
//                    @Override
//                    public void onFailure(@NonNull Exception exception) {
//                        // Upload failed
//                        Log.w(TAG, "uploadFromUri:onFailure", exception);
//
//                        mDownloadUrl = null;
//
//                        // [START_EXCLUDE]
//                        hideProgressDialog();
//                        Toast.makeText(MainActivity.this, "Error: upload failed",
//                                Toast.LENGTH_SHORT).show();
//                        updateUI(mAuth.getCurrentUser());
//                        // [END_EXCLUDE]
//                    }
//                });
    }
}

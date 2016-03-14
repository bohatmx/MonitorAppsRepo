package com.boha.monitor.library.tasks;

import android.content.Intent;
import android.util.Log;

import com.boha.monitor.library.services.PhotoUploadService;
import com.google.android.gms.gcm.GcmTaskService;
import com.google.android.gms.gcm.TaskParams;

public class PhotoTaskService extends GcmTaskService {


    @Override
    public int onRunTask(TaskParams taskParams) {
        Log.w("PhotoTaskService", "&&&&&&&& onRunTask");
        Intent m = new Intent(getApplicationContext(), PhotoUploadService.class);
        getApplicationContext().startService(m);
        return 0;
    }
}

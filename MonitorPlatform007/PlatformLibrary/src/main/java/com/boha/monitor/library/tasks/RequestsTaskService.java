package com.boha.monitor.library.tasks;

import android.content.Intent;
import android.util.Log;

import com.boha.monitor.library.services.RequestIntentService;
import com.google.android.gms.gcm.GcmTaskService;
import com.google.android.gms.gcm.TaskParams;

/**
 * Created by aubreymalabie on 3/14/16.
 */
public class RequestsTaskService extends GcmTaskService {
    @Override
    public int onRunTask(TaskParams taskParams) {
        Log.i("RequestsTaskService","%%%%%%%%%%% onRunTask");
        Intent m = new Intent(getApplicationContext(), RequestIntentService.class);
        getApplicationContext().startService(m);
        return 0;
    }
}

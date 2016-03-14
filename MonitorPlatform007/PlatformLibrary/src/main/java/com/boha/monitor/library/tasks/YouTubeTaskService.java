package com.boha.monitor.library.tasks;

import android.content.Intent;
import android.util.Log;

import com.boha.monitor.library.services.YouTubeService;
import com.google.android.gms.gcm.GcmTaskService;
import com.google.android.gms.gcm.TaskParams;

/**
 * Created by aubreymalabie on 3/14/16.
 */
public class YouTubeTaskService extends GcmTaskService {
    @Override
    public int onRunTask(TaskParams taskParams) {
        Log.d("YouTubeTaskService","&&&&&&&&---------- onRunTask");
        Intent m = new Intent(getApplicationContext(), YouTubeService.class);
        getApplicationContext().startService(m);
        return 0;
    }
}

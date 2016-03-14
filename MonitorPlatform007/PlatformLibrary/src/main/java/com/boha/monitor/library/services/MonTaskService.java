package com.boha.monitor.library.services;

import android.app.IntentService;
import android.util.Log;

import com.google.android.gms.gcm.GcmNetworkManager;
import com.google.android.gms.gcm.GcmTaskService;
import com.google.android.gms.gcm.TaskParams;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class MonTaskService extends GcmTaskService {


    @Override
    public int onRunTask(TaskParams taskParams) {
        Log.e(LOG,"## ### ###### onRunTask taskParams: " + taskParams.toString());


        return GcmNetworkManager.RESULT_SUCCESS;
    }


    static final String LOG = MonTaskService.class.getSimpleName();
}

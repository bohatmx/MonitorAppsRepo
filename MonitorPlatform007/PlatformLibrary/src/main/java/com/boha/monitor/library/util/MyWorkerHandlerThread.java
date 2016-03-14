package com.boha.monitor.library.util;

import android.os.Handler;
import android.os.HandlerThread;

/**
 * Created by aubreymalabie on 3/13/16.
 */
public class MyWorkerHandlerThread extends HandlerThread {

    public MyWorkerHandlerThread(String name, int priority) {
        super(name, priority);
    }

    public MyWorkerHandlerThread(String name) {
        super(name);
    }
    private Handler mWorkerHandler;

    public void postTask(Runnable task){
        mWorkerHandler.post(task);
    }

    public void prepareHandler(){
        mWorkerHandler = new Handler(getLooper());
    }
}

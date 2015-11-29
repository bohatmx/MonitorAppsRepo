package com.boha.monitor.library.toolbox;

import android.content.Context;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;


/**
 * Utility class that creates Volley RequestQueues and ImageLoaders
 * 
 * @author AubreyM
 * 
 */
public class BohaVolley {
    private static RequestQueue mRequestQueue;

    private BohaVolley() {
    }


    /**
     * Set up Volley Networking; create RequestQueue and ImageLoader
     * @param context
     */
    public static RequestQueue initialize( Context context) {
    	//Log.e(TAG, "initializing Volley Networking ...");
        mRequestQueue = Volley.newRequestQueue(context, new OkHttpStack());

        return mRequestQueue;
    }

    public static RequestQueue getRequestQueue( Context context) {
        if (mRequestQueue == null) {
            initialize(context);
        } 
        return mRequestQueue;
    }

    static final String LOG = "BohaVolley";
	

	
}

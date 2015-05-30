package com.boha.monitor.library.services;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Cache to hold all offline requests pending upload to cloud server
 * Created by aubreyM on 14/11/29.
 */
public class RequestCache implements Serializable {

    private  List<RequestCacheEntry> requestCacheEntryList = new ArrayList<>();


    public  List<RequestCacheEntry> getRequestCacheEntryList() {
        return requestCacheEntryList;
    }

    public  void setRequestCacheEntryList(List<RequestCacheEntry> requestCacheEntryList) {
        this.requestCacheEntryList = requestCacheEntryList;
    }


}

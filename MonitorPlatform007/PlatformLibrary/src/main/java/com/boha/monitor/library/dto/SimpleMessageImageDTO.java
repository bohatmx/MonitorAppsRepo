package com.boha.monitor.library.dto;

import java.io.Serializable;

/**
 * Created by aubreyM on 15/11/23.
 */
public class SimpleMessageImageDTO implements Serializable {
    private Integer simpleMessageImageID;
    private String url;
    private String secureUrl;
    private Long dateAdded;
    private Integer simpleMessageID;

    public Integer getSimpleMessageImageID() {
        return simpleMessageImageID;
    }

    public void setSimpleMessageImageID(Integer simpleMessageImageID) {
        this.simpleMessageImageID = simpleMessageImageID;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getSecureUrl() {
        return secureUrl;
    }

    public void setSecureUrl(String secureUrl) {
        this.secureUrl = secureUrl;
    }

    public Long getDateAdded() {
        return dateAdded;
    }

    public void setDateAdded(Long dateAdded) {
        this.dateAdded = dateAdded;
    }

    public Integer getSimpleMessageID() {
        return simpleMessageID;
    }

    public void setSimpleMessageID(Integer simpleMessageID) {
        this.simpleMessageID = simpleMessageID;
    }
}

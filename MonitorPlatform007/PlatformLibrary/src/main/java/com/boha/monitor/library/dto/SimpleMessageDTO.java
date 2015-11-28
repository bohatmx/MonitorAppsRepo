package com.boha.monitor.library.dto;

import java.io.Serializable;
import java.util.List;

/**
 * Created by aubreyM on 15/09/13.
 */
public class SimpleMessageDTO implements Serializable, Comparable<SimpleMessageDTO>{
    private Integer simpleMessageID;
    private String message, monitorName, staffName, url;
    private Long messageDate, dateReceived;
    private Boolean locationRequest;
    private Integer monitorID;
    private Integer companyID;
    private Integer staffID;
    private Integer projectID;
    private LocationTrackerDTO locationTracker;
    private List<SimpleMessageDestinationDTO> simpleMessageDestinationList;
    private List<SimpleMessageImageDTO> simpleMessageImageList;


    public List<SimpleMessageDestinationDTO> getSimpleMessageDestinationList() {
        return simpleMessageDestinationList;
    }

    public void setSimpleMessageDestinationList(List<SimpleMessageDestinationDTO> simpleMessageDestinationList) {
        this.simpleMessageDestinationList = simpleMessageDestinationList;
    }

    public Long getDateReceived() {
        return dateReceived;
    }

    public void setDateReceived(Long dateReceived) {
        this.dateReceived = dateReceived;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public List<SimpleMessageImageDTO> getSimpleMessageImageList() {
        return simpleMessageImageList;
    }

    public void setSimpleMessageImageList(List<SimpleMessageImageDTO> simpleMessageImageList) {
        this.simpleMessageImageList = simpleMessageImageList;
    }

    public Integer getSimpleMessageID() {
        return simpleMessageID;
    }

    public void setSimpleMessageID(Integer simpleMessageID) {
        this.simpleMessageID = simpleMessageID;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMonitorName() {
        return monitorName;
    }

    public void setMonitorName(String monitorName) {
        this.monitorName = monitorName;
    }

    public String getStaffName() {
        return staffName;
    }

    public void setStaffName(String staffName) {
        this.staffName = staffName;
    }

    public Long getMessageDate() {
        return messageDate;
    }

    public void setMessageDate(Long messageDate) {
        this.messageDate = messageDate;
    }

    public Boolean getLocationRequest() {
        return locationRequest;
    }

    public void setLocationRequest(Boolean locationRequest) {
        this.locationRequest = locationRequest;
    }

    public Integer getMonitorID() {
        return monitorID;
    }

    public void setMonitorID(Integer monitorID) {
        this.monitorID = monitorID;
    }

    public Integer getCompanyID() {
        return companyID;
    }

    public void setCompanyID(Integer companyID) {
        this.companyID = companyID;
    }

    public Integer getStaffID() {
        return staffID;
    }

    public void setStaffID(Integer staffID) {
        this.staffID = staffID;
    }

    public Integer getProjectID() {
        return projectID;
    }

    public void setProjectID(Integer projectID) {
        this.projectID = projectID;
    }

    public LocationTrackerDTO getLocationTracker() {
        return locationTracker;
    }

    public void setLocationTracker(LocationTrackerDTO locationTracker) {
        this.locationTracker = locationTracker;
    }

    @Override
    public int compareTo(SimpleMessageDTO msg) {

        if (this.messageDate == null || msg.messageDate == null) {
            return 0;
        }
        if (this.messageDate.longValue() > msg.messageDate.longValue()) {
            return - 1;
        }
        if (this.messageDate.longValue() < msg.messageDate.longValue()) {
            return 1;
        }
        return 0;
    }
}

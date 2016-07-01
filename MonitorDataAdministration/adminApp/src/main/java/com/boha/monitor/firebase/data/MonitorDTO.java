/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.boha.monitor.firebase.data;

import java.io.Serializable;

/**
 *
 * @author aubreyM
 */
public class MonitorDTO implements Serializable, Person, Comparable<MonitorDTO> {
    private static final long serialVersionUID = 1L;
    private String monitorID, companyID, projectID, userID;
    private long activeFlag, statusCount,
            photoCount,projectCount;
    private String fullName;
    private boolean selected;
    private long dateAssigned;

    public MonitorDTO() {
    }

    public long getlongAssigned() {
        return dateAssigned;
    }

    public void setlongAssigned(long dateAssigned) {
        this.dateAssigned = dateAssigned;
    }

    public String getMonitorID() {
        return monitorID;
    }

    public void setMonitorID(String monitorID) {
        this.monitorID = monitorID;
    }

    public String getCompanyID() {
        return companyID;
    }

    public void setCompanyID(String companyID) {
        this.companyID = companyID;
    }

    public String getProjectID() {
        return projectID;
    }

    public void setProjectID(String projectID) {
        this.projectID = projectID;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public long getActiveFlag() {
        return activeFlag;
    }

    public void setActiveFlag(long activeFlag) {
        this.activeFlag = activeFlag;
    }

    public long getStatusCount() {
        return statusCount;
    }

    public void setStatusCount(long statusCount) {
        this.statusCount = statusCount;
    }

    public long getPhotoCount() {
        return photoCount;
    }

    public void setPhotoCount(long photoCount) {
        this.photoCount = photoCount;
    }

    public long getProjectCount() {
        return projectCount;
    }

    public void setProjectCount(long projectCount) {
        this.projectCount = projectCount;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    @Override
    public int compareTo(MonitorDTO m) {

        return fullName.compareTo(m.fullName);
    }
}

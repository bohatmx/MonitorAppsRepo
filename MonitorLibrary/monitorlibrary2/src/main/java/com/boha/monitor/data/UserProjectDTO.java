package com.boha.monitor.data;

import java.io.Serializable;

/**
 * Created by aubreymalabie on 6/1/16.
 */
public class UserProjectDTO implements Serializable {

    String projectID, projectName;
    long dateAssigned, dateUpdated;
    boolean active;

    public String getProjectID() {
        return projectID;
    }

    public void setProjectID(String projectID) {
        this.projectID = projectID;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public long getDateAssigned() {
        return dateAssigned;
    }

    public void setDateAssigned(long dateAssigned) {
        this.dateAssigned = dateAssigned;
    }

    public long getDateUpdated() {
        return dateUpdated;
    }

    public void setDateUpdated(long dateUpdated) {
        this.dateUpdated = dateUpdated;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}

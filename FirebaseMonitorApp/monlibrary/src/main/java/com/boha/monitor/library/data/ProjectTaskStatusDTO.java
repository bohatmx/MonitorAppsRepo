/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.boha.monitor.library.data;

import java.io.Serializable;

/**
 *
 * @author aubreyM
 */
public class ProjectTaskStatusDTO implements Serializable, Comparable<ProjectTaskStatusDTO> {
    private static final long serialVersionUID = 1L;
    private String projectTaskStatusID;
    private long statusDate;
    private long dateUpdated;
    private String projectTaskID, companyID, projectID;
    private String taskStatusType;
    private int color;
    private String staffID;
    private String monitorID;
    private String staffName, monitorName, taskName;

    public ProjectTaskStatusDTO() {
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }



    public String getStaffName() {
        return staffName;
    }

    public void setStaffName(String staffName) {
        this.staffName = staffName;
    }

    public String getMonitorName() {
        return monitorName;
    }

    public void setMonitorName(String monitorName) {
        this.monitorName = monitorName;
    }


    public long getStatusDate() {
        return statusDate;
    }

    public void setStatusDate(long statusDate) {
        this.statusDate = statusDate;
    }

    public long getDateUpdated() {
        return dateUpdated;
    }

    public void setDateUpdated(long dateUpdated) {
        this.dateUpdated = dateUpdated;
    }

    public String getProjectTaskStatusID() {
        return projectTaskStatusID;
    }

    public void setProjectTaskStatusID(String projectTaskStatusID) {
        this.projectTaskStatusID = projectTaskStatusID;
    }

    public String getProjectTaskID() {
        return projectTaskID;
    }

    public void setProjectTaskID(String projectTaskID) {
        this.projectTaskID = projectTaskID;
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

    public String getTaskStatusType() {
        return taskStatusType;
    }

    public void setTaskStatusType(String taskStatusType) {
        this.taskStatusType = taskStatusType;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public String getStaffID() {
        return staffID;
    }

    public void setStaffID(String staffID) {
        this.staffID = staffID;
    }

    public String getMonitorID() {
        return monitorID;
    }

    public void setMonitorID(String monitorID) {
        this.monitorID = monitorID;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (projectTaskStatusID != null ? projectTaskStatusID.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof ProjectTaskStatusDTO)) {
            return false;
        }
        ProjectTaskStatusDTO other = (ProjectTaskStatusDTO) object;
        if ((this.projectTaskStatusID == null && other.projectTaskStatusID != null) || (this.projectTaskStatusID != null && !this.projectTaskStatusID.equals(other.projectTaskStatusID))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.boha.monitor.data.ProjectTaskStatus[ projectTaskStatusID=" + projectTaskStatusID + " ]";
    }

    /**
     * Compares this object to the specified object to determine their relative
     * order.
     *
     * @param another the object to compare to this instance.
     * @return a negative integer if this instance is less than {@code another};
     * a positive integer if this instance is greater than
     * {@code another}; 0 if this instance has the same order as
     * {@code another}.
     * @throws ClassCastException if {@code another} cannot be converted into something
     *                            comparable to {@code this} instance.
     */
    @Override
    public int compareTo(ProjectTaskStatusDTO another) {
        if (this.dateUpdated < another.dateUpdated) {
            return 1;
        }
        if (this.dateUpdated > another.dateUpdated) {
            return -1;
        }
        return 0;
    }
}

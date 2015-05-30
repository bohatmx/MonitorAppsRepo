/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.boha.monitor.dto;

import java.io.Serializable;

/**
 *
 * @author aubreyM
 */
public class ProjectTaskStatusDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    private Integer projectTaskStatusID;
    private Long statusDate;
    private Long dateUpdated;
    private ProjectTaskDTO projectTask;
    private TaskStatusTypeDTO taskStatusType;
    private Integer staffID;
    private Integer monitorID;

    public ProjectTaskStatusDTO() {
    }

    public Integer getProjectTaskStatusID() {
        return projectTaskStatusID;
    }

    public void setProjectTaskStatusID(Integer projectTaskStatusID) {
        this.projectTaskStatusID = projectTaskStatusID;
    }



    public ProjectTaskDTO getProjectTask() {
        return projectTask;
    }

    public void setProjectTask(ProjectTaskDTO projectTask) {
        this.projectTask = projectTask;
    }

    public TaskStatusTypeDTO getTaskStatusType() {
        return taskStatusType;
    }

    public void setTaskStatusType(TaskStatusTypeDTO taskStatusType) {
        this.taskStatusType = taskStatusType;
    }

    public Long getStatusDate() {
        return statusDate;
    }

    public void setStatusDate(Long statusDate) {
        this.statusDate = statusDate;
    }

    public Long getDateUpdated() {
        return dateUpdated;
    }

    public void setDateUpdated(Long dateUpdated) {
        this.dateUpdated = dateUpdated;
    }

    public Integer getStaffID() {
        return staffID;
    }

    public void setStaffID(Integer staffID) {
        this.staffID = staffID;
    }

    public Integer getMonitorID() {
        return monitorID;
    }

    public void setMonitorID(Integer monitorID) {
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
    
}

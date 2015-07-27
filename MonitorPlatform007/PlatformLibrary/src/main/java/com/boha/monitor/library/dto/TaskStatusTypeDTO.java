/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.boha.monitor.library.dto;

import java.io.Serializable;
import java.util.List;

/**
 * @author aubreyM
 */
public class TaskStatusTypeDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    private Integer taskStatusTypeID;
    private String taskStatusTypeName;
    private Short statusColor;
    private List<ProjectTaskStatusDTO> projectTaskStatusList;
    private Integer companyID;
    public static final int
            STATUS_COLOR_RED = 1,
            STATUS_COLOR_AMBER = 2,
            STATUS_COLOR_GREEN = 3;

    public TaskStatusTypeDTO() {
    }

    public Integer getCompanyID() {
        return companyID;
    }

    public void setCompanyID(Integer companyID) {
        this.companyID = companyID;
    }


    public Integer getTaskStatusTypeID() {
        return taskStatusTypeID;
    }

    public void setTaskStatusTypeID(Integer taskStatusTypeID) {
        this.taskStatusTypeID = taskStatusTypeID;
    }

    public String getTaskStatusTypeName() {
        return taskStatusTypeName;
    }

    public void setTaskStatusTypeName(String taskStatusTypeName) {
        this.taskStatusTypeName = taskStatusTypeName;
    }

    public Short getStatusColor() {
        return statusColor;
    }

    public void setStatusColor(Short statusColor) {
        this.statusColor = statusColor;
    }

    public List<ProjectTaskStatusDTO> getProjectTaskStatusList() {
        return projectTaskStatusList;
    }

    public void setProjectTaskStatusList(List<ProjectTaskStatusDTO> projectTaskStatusList) {
        this.projectTaskStatusList = projectTaskStatusList;
    }


    @Override
    public int hashCode() {
        int hash = 0;
        hash += (taskStatusTypeID != null ? taskStatusTypeID.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof TaskStatusTypeDTO)) {
            return false;
        }
        TaskStatusTypeDTO other = (TaskStatusTypeDTO) object;
        if ((this.taskStatusTypeID == null && other.taskStatusTypeID != null) || (this.taskStatusTypeID != null && !this.taskStatusTypeID.equals(other.taskStatusTypeID))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.boha.monitor.data.TaskStatusType[ taskStatusTypeID=" + taskStatusTypeID + " ]";
    }

}

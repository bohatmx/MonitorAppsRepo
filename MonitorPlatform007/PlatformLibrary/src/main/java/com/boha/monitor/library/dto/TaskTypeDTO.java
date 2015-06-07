/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.boha.monitor.library.dto;

import java.io.Serializable;
import java.util.List;

/**
 *
 * @author aubreyM
 */
public class TaskTypeDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    private Integer taskTypeID;
    private String taskTypeName;
    private Integer companyID;
    private List<TaskDTO> taskList;
    

    public TaskTypeDTO() {
    }


    public Integer getTaskTypeID() {
        return taskTypeID;
    }

    public void setTaskTypeID(Integer taskTypeID) {
        this.taskTypeID = taskTypeID;
    }

    public String getTaskTypeName() {
        return taskTypeName;
    }

    public void setTaskTypeName(String taskTypeName) {
        this.taskTypeName = taskTypeName;
    }

    public Integer getCompanyID() {
        return companyID;
    }

    public void setCompanyID(Integer companyID) {
        this.companyID = companyID;
    }


    public List<TaskDTO> getTaskList() {
        return taskList;
    }

    public void setTaskList(List<TaskDTO> taskList) {
        this.taskList = taskList;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (taskTypeID != null ? taskTypeID.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof TaskTypeDTO)) {
            return false;
        }
        TaskTypeDTO other = (TaskTypeDTO) object;
        if ((this.taskTypeID == null && other.taskTypeID != null) || (this.taskTypeID != null && !this.taskTypeID.equals(other.taskTypeID))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.boha.monitor.data.TaskType[ taskTypeID=" + taskTypeID + " ]";
    }
    
}

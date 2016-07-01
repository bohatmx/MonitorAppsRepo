/*
 * To change this license header, choose License Headers in ProjectDTO Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.boha.monitor.firebase.data;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author aubreyM
 */
public class ProjectTaskDTO implements Serializable {

    private static final long serialVersionUID = 1L;
    private String projectTaskID, projectID, companyID;
    private Long dateRegistered;
    private Integer statusCount, photoCount;
    private String taskName;
    private String projectName;
    private Map<String, Object> photoUploadList = new HashMap();
    private Map<String,Object> projectTaskStatusList = new HashMap<>();

    public ProjectTaskDTO() {
    }

    public String getCompanyID() {
        return companyID;
    }

    public void setCompanyID(String companyID) {
        this.companyID = companyID;
    }

    public Integer getStatusCount() {
        return statusCount;
    }

    public void setStatusCount(Integer statusCount) {
        this.statusCount = statusCount;
    }

    public Integer getPhotoCount() {
        return photoCount;
    }

    public void setPhotoCount(Integer photoCount) {
        this.photoCount = photoCount;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public Long getDateRegistered() {
        return dateRegistered;
    }

    public void setDateRegistered(Long dateRegistered) {
        this.dateRegistered = dateRegistered;
    }

    public String getProjectTaskID() {
        return projectTaskID;
    }

    public void setProjectTaskID(String projectTaskID) {
        this.projectTaskID = projectTaskID;
    }

    public String getProjectID() {
        return projectID;
    }

    public void setProjectID(String projectID) {
        this.projectID = projectID;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public Map<String, Object> getPhotoUploadList() {
        return photoUploadList;
    }

    public void setPhotoUploadList(Map<String, Object> photoUploadList) {
        this.photoUploadList = photoUploadList;
    }

    public Map<String, Object> getProjectTaskStatusList() {
        return projectTaskStatusList;
    }

    public void setProjectTaskStatusList(Map<String, Object> projectTaskStatusList) {
        this.projectTaskStatusList = projectTaskStatusList;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (projectTaskID != null ? projectTaskID.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof ProjectTaskDTO)) {
            return false;
        }
        ProjectTaskDTO other = (ProjectTaskDTO) object;
        if ((this.projectTaskID == null && other.projectTaskID != null) || (this.projectTaskID != null && !this.projectTaskID.equals(other.projectTaskID))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.boha.monitor.data.ProjectTask[ projectTaskID=" + projectTaskID + " ]";
    }


}

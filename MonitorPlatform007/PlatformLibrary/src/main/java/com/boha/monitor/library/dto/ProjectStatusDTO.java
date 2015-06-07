/*
 * To change this license header, choose License Headers in ProjectDTO Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.boha.monitor.library.dto;

import java.io.Serializable;

/**
 *
 * @author aubreyM
 */
public class ProjectStatusDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    private Integer projectStatusID;
    private Long statusDate;
    private Long dateUpdated;
    private Integer projectID;
    private ProjectStatusTypeDTO projectStatusType;
    private Integer staffID;

    public ProjectStatusDTO() {
    }

    public ProjectStatusDTO(Integer projectStatusID) {
        this.projectStatusID = projectStatusID;
    }

    public Integer getProjectStatusID() {
        return projectStatusID;
    }

    public void setProjectStatusID(Integer projectStatusID) {
        this.projectStatusID = projectStatusID;
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

    public Integer getProjectID() {
        return projectID;
    }

    public void setProjectID(Integer projectID) {
        this.projectID = projectID;
    }

    public ProjectStatusTypeDTO getProjectStatusType() {
        return projectStatusType;
    }

    public void setProjectStatusType(ProjectStatusTypeDTO projectStatusType) {
        this.projectStatusType = projectStatusType;
    }

    public Integer getStaffID() {
        return staffID;
    }

    public void setStaffID(Integer staffID) {
        this.staffID = staffID;
    }

   
    @Override
    public int hashCode() {
        int hash = 0;
        hash += (projectStatusID != null ? projectStatusID.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof ProjectStatusDTO)) {
            return false;
        }
        ProjectStatusDTO other = (ProjectStatusDTO) object;
        if ((this.projectStatusID == null && other.projectStatusID != null) || (this.projectStatusID != null && !this.projectStatusID.equals(other.projectStatusID))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.boha.monitor.data.ProjectStatus[ projectStatusID=" + projectStatusID + " ]";
    }
    
}

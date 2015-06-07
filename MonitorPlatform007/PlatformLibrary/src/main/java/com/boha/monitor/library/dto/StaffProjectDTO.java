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
public class StaffProjectDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    private Integer staffProjectID;
    private Long dateAssigned;
    private Boolean activeFlag;
    private Integer staffID;
    private Integer projectID;
    private String projectName;

    public StaffProjectDTO() {
    }

    public Integer getStaffProjectID() {
        return staffProjectID;
    }

    public void setStaffProjectID(Integer staffProjectID) {
        this.staffProjectID = staffProjectID;
    }



    public Boolean getActiveFlag() {
        return activeFlag;
    }

    public void setActiveFlag(Boolean activeFlag) {
        this.activeFlag = activeFlag;
    }

    public Long getDateAssigned() {
        return dateAssigned;
    }

    public void setDateAssigned(Long dateAssigned) {
        this.dateAssigned = dateAssigned;
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

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

   
   

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (staffProjectID != null ? staffProjectID.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof StaffProjectDTO)) {
            return false;
        }
        StaffProjectDTO other = (StaffProjectDTO) object;
        if ((this.staffProjectID == null && other.staffProjectID != null) || (this.staffProjectID != null && !this.staffProjectID.equals(other.staffProjectID))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.boha.monitor.data.StaffProject[ staffProjectID=" + staffProjectID + " ]";
    }
    
}

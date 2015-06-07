/*
 * To change this license header, choose License Headers in ProjectDTO Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.boha.monitor.library.dto;

import java.io.Serializable;
import java.util.Date;

/**
 *
 * @author aubreyM
 */
public class MonitorProjectDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    private Integer monitorProjectID;
    private Date dateAssigned;
    private Boolean activeFlag;
    private Integer monitorID, projectID;

    public MonitorProjectDTO() {
    }


    public Integer getMonitorProjectID() {
        return monitorProjectID;
    }

    public void setMonitorProjectID(Integer monitorProjectID) {
        this.monitorProjectID = monitorProjectID;
    }

    public Date getDateAssigned() {
        return dateAssigned;
    }

    public void setDateAssigned(Date dateAssigned) {
        this.dateAssigned = dateAssigned;
    }

    public Boolean getActiveFlag() {
        return activeFlag;
    }

    public void setActiveFlag(Boolean activeFlag) {
        this.activeFlag = activeFlag;
    }

    public Integer getMonitorID() {
        return monitorID;
    }

    public void setMonitorID(Integer monitorID) {
        this.monitorID = monitorID;
    }

    public Integer getProjectID() {
        return projectID;
    }

    public void setProjectID(Integer projectID) {
        this.projectID = projectID;
    }


    @Override
    public int hashCode() {
        int hash = 0;
        hash += (monitorProjectID != null ? monitorProjectID.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof MonitorProjectDTO)) {
            return false;
        }
        MonitorProjectDTO other = (MonitorProjectDTO) object;
        if ((this.monitorProjectID == null && other.monitorProjectID != null) || (this.monitorProjectID != null && !this.monitorProjectID.equals(other.monitorProjectID))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.boha.monitor.data.MonitorProject[ monitorProjectID=" + monitorProjectID + " ]";
    }
    
}

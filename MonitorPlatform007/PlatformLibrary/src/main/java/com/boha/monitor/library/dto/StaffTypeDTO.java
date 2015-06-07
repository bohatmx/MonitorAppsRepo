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
public class StaffTypeDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    private Integer staffTypeID;
    private String staffTypeName;
    private List<StaffDTO> staffList;

    public StaffTypeDTO() {
    }

    public Integer getStaffTypeID() {
        return staffTypeID;
    }

    public void setStaffTypeID(Integer staffTypeID) {
        this.staffTypeID = staffTypeID;
    }

    public String getStaffTypeName() {
        return staffTypeName;
    }

    public void setStaffTypeName(String staffTypeName) {
        this.staffTypeName = staffTypeName;
    }

    public List<StaffDTO> getStaffList() {
        return staffList;
    }

    public void setStaffList(List<StaffDTO> staffList) {
        this.staffList = staffList;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (staffTypeID != null ? staffTypeID.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof StaffTypeDTO)) {
            return false;
        }
        StaffTypeDTO other = (StaffTypeDTO) object;
        if ((this.staffTypeID == null && other.staffTypeID != null) || (this.staffTypeID != null && !this.staffTypeID.equals(other.staffTypeID))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.boha.monitor.data.StaffType[ staffTypeID=" + staffTypeID + " ]";
    }
    
}

/*
 * To change this license header, choose License Headers in ProjectDTO Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.boha.monitor.library.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author aubreyM
 */public class CompanyDTO implements Serializable {
   private static final long serialVersionUID = 1L;
    private Integer companyID;
    private String companyName;
    private String address, email, cellphone;

    private List<ProjectStatusTypeDTO> projectStatusTypeList;
    private List<StaffDTO> staffList;
    private List<StaffTypeDTO> staffTypeList;
    private List<TaskStatusTypeDTO> taskStatusTypeList;
    private List<MonitorDTO> monitorList;
    private List<PortfolioDTO> portfolioList;

    public Integer getCompanyID() {
        return companyID;
    }

    public void setCompanyID(Integer companyID) {
        this.companyID = companyID;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCellphone() {
        return cellphone;
    }

    public void setCellphone(String cellphone) {
        this.cellphone = cellphone;
    }

    public List<ProjectStatusTypeDTO> getProjectStatusTypeList() {
        return projectStatusTypeList;
    }

    public void setProjectStatusTypeList(List<ProjectStatusTypeDTO> projectStatusTypeList) {
        this.projectStatusTypeList = projectStatusTypeList;
    }

    public List<StaffDTO> getStaffList() {
        if (staffList == null) {
            staffList = new ArrayList<>();
        }
        return staffList;
    }

    public void setStaffList(List<StaffDTO> staffList) {
        this.staffList = staffList;
    }

    public List<StaffTypeDTO> getStaffTypeList() {
        if (staffTypeList == null) {
            staffTypeList = new ArrayList<>();
        }
        return staffTypeList;
    }

    public void setStaffTypeList(List<StaffTypeDTO> staffTypeList) {
        this.staffTypeList = staffTypeList;
    }

    public List<TaskStatusTypeDTO> getTaskStatusTypeList() {
        if (taskStatusTypeList == null) {
            taskStatusTypeList = new ArrayList<>();
        }
        return taskStatusTypeList;
    }

    public void setTaskStatusTypeList(List<TaskStatusTypeDTO> taskStatusTypeList) {
        this.taskStatusTypeList = taskStatusTypeList;
    }

    public List<MonitorDTO> getMonitorList() {
        if (monitorList == null) {
            monitorList = new ArrayList<>();
        }
        return monitorList;
    }

    public void setMonitorList(List<MonitorDTO> monitorList) {
        this.monitorList = monitorList;
    }

    public List<PortfolioDTO> getPortfolioList() {
        if (portfolioList == null) {
            portfolioList = new ArrayList<>();
        }
        return portfolioList;
    }

    public void setPortfolioList(List<PortfolioDTO> portfolioList) {
        this.portfolioList = portfolioList;
    }
}

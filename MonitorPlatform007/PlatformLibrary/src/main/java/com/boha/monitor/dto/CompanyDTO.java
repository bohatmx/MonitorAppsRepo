/*
 * To change this license header, choose License Headers in ProjectDTO Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.boha.monitor.dto;

import java.io.Serializable;
import java.util.List;

/**
 *
 * @author aubreyM
 */public class CompanyDTO implements Serializable {
   private static final long serialVersionUID = 1L;
    private Integer companyID, countryID;
    private String companyName;
    private String address, email, cellphone;

    private List<ProjectDTO> projectList;
    private List<ProjectStatusTypeDTO> projectStatusTypeList;
    private List<StaffDTO> staffList;
    private List<ProjectTaskDTO> projectTaskList;
    private List<ProjectTaskStatusDTO> projectTaskStatusList;
    private List<TaskDTO> taskList;
    private List<TaskTypeDTO> taskTypeList;
    private List<StaffTypeDTO> staffTypeList;
    private List<TaskStatusTypeDTO> taskStatusTypeList;
    private List<MonitorDTO> monitorList;
    private List<PhotoUploadDTO> photoUploadList;
    private List<PortfolioDTO> portfolioList;
    private List<GcmDeviceDTO> gcmDeviceList;
    private List<StaffProjectDTO> staffProjectlist;
    
    public CompanyDTO() {
    }


    public Integer getCompanyID() {
        return companyID;
    }

    public void setCompanyID(Integer companyID) {
        this.companyID = companyID;
    }

    public Integer getCountryID() {
        return countryID;
    }

    public void setCountryID(Integer countryID) {
        this.countryID = countryID;
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

    public List<ProjectDTO> getProjectList() {
        return projectList;
    }

    public void setProjectList(List<ProjectDTO> projectList) {
        this.projectList = projectList;
    }

    public List<ProjectStatusTypeDTO> getProjectStatusTypeList() {
        return projectStatusTypeList;
    }

    public void setProjectStatusTypeList(List<ProjectStatusTypeDTO> projectStatusTypeList) {
        this.projectStatusTypeList = projectStatusTypeList;
    }

    public List<StaffDTO> getStaffList() {
        return staffList;
    }

    public void setStaffList(List<StaffDTO> staffList) {
        this.staffList = staffList;
    }

    public List<ProjectTaskDTO> getProjectTaskList() {
        return projectTaskList;
    }

    public void setProjectTaskList(List<ProjectTaskDTO> projectTaskList) {
        this.projectTaskList = projectTaskList;
    }

    public List<ProjectTaskStatusDTO> getProjectTaskStatusList() {
        return projectTaskStatusList;
    }

    public void setProjectTaskStatusList(List<ProjectTaskStatusDTO> projectTaskStatusList) {
        this.projectTaskStatusList = projectTaskStatusList;
    }

    public List<TaskDTO> getTaskList() {
        return taskList;
    }

    public void setTaskList(List<TaskDTO> taskList) {
        this.taskList = taskList;
    }

    public List<TaskTypeDTO> getTaskTypeList() {
        return taskTypeList;
    }

    public void setTaskTypeList(List<TaskTypeDTO> taskTypeList) {
        this.taskTypeList = taskTypeList;
    }

    public List<StaffTypeDTO> getStaffTypeList() {
        return staffTypeList;
    }

    public void setStaffTypeList(List<StaffTypeDTO> staffTypeList) {
        this.staffTypeList = staffTypeList;
    }

    public List<TaskStatusTypeDTO> getTaskStatusTypeList() {
        return taskStatusTypeList;
    }

    public void setTaskStatusTypeList(List<TaskStatusTypeDTO> taskStatusTypeList) {
        this.taskStatusTypeList = taskStatusTypeList;
    }

    public List<MonitorDTO> getMonitorList() {
        return monitorList;
    }

    public void setMonitorList(List<MonitorDTO> monitorList) {
        this.monitorList = monitorList;
    }

    public List<PhotoUploadDTO> getPhotoUploadList() {
        return photoUploadList;
    }

    public void setPhotoUploadList(List<PhotoUploadDTO> photoUploadList) {
        this.photoUploadList = photoUploadList;
    }

    public List<PortfolioDTO> getPortfolioList() {
        return portfolioList;
    }

    public void setPortfolioList(List<PortfolioDTO> portfolioList) {
        this.portfolioList = portfolioList;
    }

    public List<GcmDeviceDTO> getGcmDeviceList() {
        return gcmDeviceList;
    }

    public void setGcmDeviceList(List<GcmDeviceDTO> gcmDeviceList) {
        this.gcmDeviceList = gcmDeviceList;
    }

    public List<StaffProjectDTO> getStaffProjectlist() {
        return staffProjectlist;
    }

    public void setStaffProjectlist(List<StaffProjectDTO> staffProjectlist) {
        this.staffProjectlist = staffProjectlist;
    }
    
}

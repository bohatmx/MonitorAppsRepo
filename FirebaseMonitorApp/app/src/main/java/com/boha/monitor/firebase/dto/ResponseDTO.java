/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.boha.monitor.firebase.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author aubreyM
 */
public class ResponseDTO implements Serializable{
    private static final long serialVersionUID = 1L;

    private int statusCode = 0, statusCount,
            goodCount, badCount, gcmSuccess, gcmFailure;
    private String message = "Request is KOOL!",
            sessionID, GCMRegistrationID, fileString;
    private Double elapsedRequestTimeInSeconds;
    private Long startDate, endDate;
    private String log;
    private StaffDTO staff;
    private MonitorDTO monitor;
    private List<RequestDTO> requestList = new ArrayList<>();
    private List<SimpleMessageDTO> simpleMessageList = new ArrayList<>();
    private List<GcmDeviceDTO> gcmDeviceList = new ArrayList<>();
    private List<LocationTrackerDTO> locationTrackerList = new ArrayList<>();
    private List<String> taskImageFileNameList = new ArrayList<>();
    private List<StaffProjectDTO> staffProjectList = new ArrayList<>();
    private List<String> siteImageFileNameList = new ArrayList<>();
    private List<ProjectStatusTypeDTO> projectStatusTypeList = new ArrayList<>();
    private List<ProjectDTO> projectList = new ArrayList<>();
    private List<MonitorProjectDTO> monitorProjectList = new ArrayList<>();
    private List<ErrorStoreDTO> errorStoreList = new ArrayList<>();
    private List<ErrorStoreAndroidDTO> errorStoreAndroidList = new ArrayList<>();
    private List<TaskDTO> taskList = new ArrayList<>();
    private List<ProjectTaskStatusDTO> projectTaskStatusList = new ArrayList<>();
    private List<TaskStatusTypeDTO> taskStatusTypeList = new ArrayList<>();
    private List<ProjectTaskDTO> projectTaskList = new ArrayList<>();
    private List<PhotoUploadDTO> photoUploadList = new ArrayList<>();
    private List<TaskTypeDTO> taskTypeList = new ArrayList<>();
    private List<PortfolioDTO> portfolioList = new ArrayList<>();
    private List<SubTaskDTO> subTaskList = new ArrayList<>();
    private List<CompanyDTO> companyList = new ArrayList<>();
    private List<VideoUploadDTO> videoUploadList = new ArrayList<>();
    //
    private CompanyDTO company;
    private List<MonitorDTO> monitorList = new ArrayList<>();
    private List<StaffDTO> staffList = new ArrayList<>();
    private ProjectTaskStatusDTO lastStatus;

    public List<VideoUploadDTO> getVideoUploadList() {
        return videoUploadList;
    }

    public void setVideoUploadList(List<VideoUploadDTO> videoUploadList) {
        this.videoUploadList = videoUploadList;
    }

    public List<MonitorProjectDTO> getMonitorProjectList() {
        return monitorProjectList;
    }

    public void setMonitorProjectList(List<MonitorProjectDTO> monitorProjectList) {
        this.monitorProjectList = monitorProjectList;
    }

    public StaffDTO getStaff() {
        return staff;
    }

    public void setStaff(StaffDTO staff) {
        this.staff = staff;
    }

    public MonitorDTO getMonitor() {
        return monitor;
    }

    public void setMonitor(MonitorDTO monitor) {
        this.monitor = monitor;
    }

    public List<SubTaskDTO> getSubTaskList() {
        if (subTaskList == null) {
            subTaskList = new ArrayList<>();
        }
        return subTaskList;
    }

    public void setSubTaskList(List<SubTaskDTO> subTaskList) {
        this.subTaskList = subTaskList;
    }

    public List<PortfolioDTO> getPortfolioList() {
        if (portfolioList == null) {
            portfolioList = new ArrayList<>();
        }
        return portfolioList;
    }

    public List<GcmDeviceDTO> getGcmDeviceList() {
        return gcmDeviceList;
    }

    public void setGcmDeviceList(List<GcmDeviceDTO> gcmDeviceList) {
        this.gcmDeviceList = gcmDeviceList;
    }

    public void setPortfolioList(List<PortfolioDTO> portfolioList) {
        this.portfolioList = portfolioList;
    }

    public List<RequestDTO> getRequestList() {
        if (requestList == null) {
            requestList = new ArrayList<>();
        }
        return requestList;
    }

    public void setRequestList(List<RequestDTO> requestList) {
        this.requestList = requestList;
    }

    public List<CompanyDTO> getCompanyList() {
        if (companyList == null) {
            companyList = new ArrayList<>();
        }
        return companyList;
    }

    public void setCompanyList(List<CompanyDTO> companyList) {
        this.companyList = companyList;
    }

    public Long getStartDate() {
        return startDate;
    }

    public void setStartDate(Long startDate) {
        this.startDate = startDate;
    }

    public Long getEndDate() {
        return endDate;
    }

    public void setEndDate(Long endDate) {
        this.endDate = endDate;
    }

    public List<TaskTypeDTO> getTaskTypeList() {
        if (taskTypeList == null) {
            taskTypeList = new ArrayList<>();
        }
        return taskTypeList;
    }

    public void setTaskTypeList(List<TaskTypeDTO> taskTypeList) {
        this.taskTypeList = taskTypeList;
    }

    public List<PhotoUploadDTO> getPhotoUploadList() {
        if (photoUploadList == null) {
            photoUploadList = new ArrayList<>();
        }
        return photoUploadList;
    }

    public void setPhotoUploadList(List<PhotoUploadDTO> photoUploadList) {

        this.photoUploadList = photoUploadList;
    }

    public List<ProjectTaskDTO> getProjectTaskList() {
        if (projectTaskList == null) {
            projectTaskList = new ArrayList<>();
        }
        return projectTaskList;
    }

    public void setProjectTaskList(List<ProjectTaskDTO> projectTaskList) {
        this.projectTaskList = projectTaskList;
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

    public List<ProjectTaskStatusDTO> getProjectTaskStatusList() {
        if (projectStatusTypeList == null) {
            projectStatusTypeList = new ArrayList<>();
        }
        return projectTaskStatusList;
    }

    public void setProjectTaskStatusList(List<ProjectTaskStatusDTO> projectTaskStatusList) {
        this.projectTaskStatusList = projectTaskStatusList;
    }

    public List<SimpleMessageDTO> getSimpleMessageList() {
        return simpleMessageList;
    }

    public void setSimpleMessageList(List<SimpleMessageDTO> simpleMessageList) {
        this.simpleMessageList = simpleMessageList;
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

    public List<StaffDTO> getStaffList() {
        if (staffList == null) {
            staffList = new ArrayList<>();
        }
        return staffList;
    }

    public void setStaffList(List<StaffDTO> staffList) {
        this.staffList = staffList;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public int getStatusCount() {
        return statusCount;
    }

    public void setStatusCount(int statusCount) {
        this.statusCount = statusCount;
    }

    public ProjectTaskStatusDTO getLastStatus() {
        return lastStatus;
    }

    public void setLastStatus(ProjectTaskStatusDTO lastStatus) {
        this.lastStatus = lastStatus;
    }

    public int getGoodCount() {
        return goodCount;
    }

    public void setGoodCount(int goodCount) {
        this.goodCount = goodCount;
    }

    public int getBadCount() {
        return badCount;
    }

    public void setBadCount(int badCount) {
        this.badCount = badCount;
    }

    public int getGcmSuccess() {
        return gcmSuccess;
    }

    public void setGcmSuccess(int gcmSuccess) {
        this.gcmSuccess = gcmSuccess;
    }

    public int getGcmFailure() {
        return gcmFailure;
    }

    public void setGcmFailure(int gcmFailure) {
        this.gcmFailure = gcmFailure;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSessionID() {
        return sessionID;
    }

    public void setSessionID(String sessionID) {
        this.sessionID = sessionID;
    }

    public String getGCMRegistrationID() {
        return GCMRegistrationID;
    }

    public void setGCMRegistrationID(String GCMRegistrationID) {
        this.GCMRegistrationID = GCMRegistrationID;
    }

    public String getFileString() {
        return fileString;
    }

    public void setFileString(String fileString) {
        this.fileString = fileString;
    }

    public Double getElapsedRequestTimeInSeconds() {
        return elapsedRequestTimeInSeconds;
    }

    public void setElapsedRequestTimeInSeconds(Double elapsedRequestTimeInSeconds) {
        this.elapsedRequestTimeInSeconds = elapsedRequestTimeInSeconds;
    }

    public String getLog() {
        return log;
    }

    public void setLog(String log) {
        this.log = log;
    }

    public List<LocationTrackerDTO> getLocationTrackerList() {
        if (locationTrackerList == null) {
            locationTrackerList = new ArrayList<>();
        }
        return locationTrackerList;
    }

    public void setLocationTrackerList(List<LocationTrackerDTO> locationTrackerList) {
        this.locationTrackerList = locationTrackerList;
    }

    public List<String> getTaskImageFileNameList() {
        return taskImageFileNameList;
    }

    public void setTaskImageFileNameList(List<String> taskImageFileNameList) {
        this.taskImageFileNameList = taskImageFileNameList;
    }


    public List<StaffProjectDTO> getStaffProjectList() {
        if (staffProjectList == null) {
            staffProjectList = new ArrayList<>();
        }
        return staffProjectList;
    }

    public void setStaffProjectList(List<StaffProjectDTO> staffProjectList) {
        this.staffProjectList = staffProjectList;
    }

    public List<String> getSiteImageFileNameList() {
        return siteImageFileNameList;
    }

    public void setSiteImageFileNameList(List<String> siteImageFileNameList) {
        this.siteImageFileNameList = siteImageFileNameList;
    }

    public List<ProjectStatusTypeDTO> getProjectStatusTypeList() {
        if (projectStatusTypeList == null) {
            projectStatusTypeList = new ArrayList<>();
        }
        return projectStatusTypeList;
    }

    public void setProjectStatusTypeList(List<ProjectStatusTypeDTO> projectStatusTypeList) {
        this.projectStatusTypeList = projectStatusTypeList;
    }

    public List<ProjectDTO> getProjectList() {
        if (projectList == null) {
            projectList = new ArrayList<>();
        }
        return projectList;
    }

    public void setProjectList(List<ProjectDTO> projectList) {
        this.projectList = projectList;
    }

    public List<ErrorStoreDTO> getErrorStoreList() {
        if (errorStoreList == null) {
            errorStoreList = new ArrayList<>();
        }
        return errorStoreList;
    }

    public void setErrorStoreList(List<ErrorStoreDTO> errorStoreList) {
        this.errorStoreList = errorStoreList;
    }

    public List<ErrorStoreAndroidDTO> getErrorStoreAndroidList() {
        if (errorStoreAndroidList == null) {
            errorStoreAndroidList = new ArrayList<>();
        }
        return errorStoreAndroidList;
    }

    public void setErrorStoreAndroidList(List<ErrorStoreAndroidDTO> errorStoreAndroidList) {
        this.errorStoreAndroidList = errorStoreAndroidList;
    }

    public List<TaskDTO> getTaskList() {
        if (taskList == null) {
            taskList = new ArrayList<>();
        }
        return taskList;
    }

    public void setTaskList(List<TaskDTO> taskList) {
        this.taskList = taskList;
    }

    public CompanyDTO getCompany() {
        return company;
    }

    public void setCompany(CompanyDTO company) {
        this.company = company;
    }

}

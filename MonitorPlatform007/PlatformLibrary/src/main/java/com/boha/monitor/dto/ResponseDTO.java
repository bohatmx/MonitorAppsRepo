/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.boha.monitor.dto;

import java.util.List;

/**
 *
 * @author aubreyM
 */
public class ResponseDTO {

    private Integer statusCode = 0, statusCount,
            goodCount, badCount, gcmSuccess, gcmFailure;
    private String message = "Request is KOOL!",
            sessionID, GCMRegistrationID, fileString;
    private Double elapsedRequestTimeInSeconds;
    private String log;
    private ChatDTO chat;
    private List<ChatDTO> chatList;
    private List<LocationTrackerDTO> locationTrackerList;
    private List<String> taskImageFileNameList;
    private List<ChatMessageDTO> chatMessageList;
    private List<StaffProjectDTO> staffProjectList;
    private List<String> siteImageFileNameList;
    private List<ProjectStatusTypeDTO> projectStatusTypeList;
    private List<ProjectDTO> projectList;
    private List<ErrorStoreDTO> errorStoreList;
    private List<ErrorStoreAndroidDTO> errorStoreAndroidList;
    private List<TaskDTO> taskList;
    private List<ProjectTaskStatusDTO> projectTaskStatusList;
    private List<TaskStatusTypeDTO> taskStatusTypeList;
    private List<ProjectTaskDTO> projectTaskList;
    private List<PhotoUploadDTO> photoUploadList;
    private List<TaskTypeDTO> taskTypeList;
    //
    private CompanyDTO company;
    private List<MonitorDTO> monitorList;
    private List<StaffDTO> staffList;
    private ProjectTaskStatusDTO lastStatus;

    public List<TaskTypeDTO> getTaskTypeList() {
        return taskTypeList;
    }

    public void setTaskTypeList(List<TaskTypeDTO> taskTypeList) {
        this.taskTypeList = taskTypeList;
    }

    public List<PhotoUploadDTO> getPhotoUploadList() {
        return photoUploadList;
    }

    public void setPhotoUploadList(List<PhotoUploadDTO> photoUploadList) {
        this.photoUploadList = photoUploadList;
    }

    public List<ProjectTaskDTO> getProjectTaskList() {
        return projectTaskList;
    }

    public void setProjectTaskList(List<ProjectTaskDTO> projectTaskList) {
        this.projectTaskList = projectTaskList;
    }

    public List<TaskStatusTypeDTO> getTaskStatusTypeList() {
        return taskStatusTypeList;
    }

    public void setTaskStatusTypeList(List<TaskStatusTypeDTO> taskStatusTypeList) {
        this.taskStatusTypeList = taskStatusTypeList;
    }

    public List<ProjectTaskStatusDTO> getProjectTaskStatusList() {
        return projectTaskStatusList;
    }

    public void setProjectTaskStatusList(List<ProjectTaskStatusDTO> projectTaskStatusList) {
        this.projectTaskStatusList = projectTaskStatusList;
    }

    public List<MonitorDTO> getMonitorList() {
        return monitorList;
    }

    public void setMonitorList(List<MonitorDTO> monitorList) {
        this.monitorList = monitorList;
    }

    public List<StaffDTO> getStaffList() {
        return staffList;
    }

    public void setStaffList(List<StaffDTO> staffList) {
        this.staffList = staffList;
    }

    public Integer getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(Integer statusCode) {
        this.statusCode = statusCode;
    }

    public Integer getStatusCount() {
        return statusCount;
    }

    public void setStatusCount(Integer statusCount) {
        this.statusCount = statusCount;
    }

    public ProjectTaskStatusDTO getLastStatus() {
        return lastStatus;
    }

    public void setLastStatus(ProjectTaskStatusDTO lastStatus) {
        this.lastStatus = lastStatus;
    }

    public Integer getGoodCount() {
        return goodCount;
    }

    public void setGoodCount(Integer goodCount) {
        this.goodCount = goodCount;
    }

    public Integer getBadCount() {
        return badCount;
    }

    public void setBadCount(Integer badCount) {
        this.badCount = badCount;
    }

    public Integer getGcmSuccess() {
        return gcmSuccess;
    }

    public void setGcmSuccess(Integer gcmSuccess) {
        this.gcmSuccess = gcmSuccess;
    }

    public Integer getGcmFailure() {
        return gcmFailure;
    }

    public void setGcmFailure(Integer gcmFailure) {
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

    public ChatDTO getChat() {
        return chat;
    }

    public void setChat(ChatDTO chat) {
        this.chat = chat;
    }

    public List<ChatDTO> getChatList() {
        return chatList;
    }

    public void setChatList(List<ChatDTO> chatList) {
        this.chatList = chatList;
    }

    public List<LocationTrackerDTO> getLocationTrackerList() {
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

    public List<ChatMessageDTO> getChatMessageList() {
        return chatMessageList;
    }

    public void setChatMessageList(List<ChatMessageDTO> chatMessageList) {
        this.chatMessageList = chatMessageList;
    }

    public List<StaffProjectDTO> getStaffProjectList() {
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
        return projectStatusTypeList;
    }

    public void setProjectStatusTypeList(List<ProjectStatusTypeDTO> projectStatusTypeList) {
        this.projectStatusTypeList = projectStatusTypeList;
    }

    public List<ProjectDTO> getProjectList() {
        return projectList;
    }

    public void setProjectList(List<ProjectDTO> projectList) {
        this.projectList = projectList;
    }

    public List<ErrorStoreDTO> getErrorStoreList() {
        return errorStoreList;
    }

    public void setErrorStoreList(List<ErrorStoreDTO> errorStoreList) {
        this.errorStoreList = errorStoreList;
    }

    public List<ErrorStoreAndroidDTO> getErrorStoreAndroidList() {
        return errorStoreAndroidList;
    }

    public void setErrorStoreAndroidList(List<ErrorStoreAndroidDTO> errorStoreAndroidList) {
        this.errorStoreAndroidList = errorStoreAndroidList;
    }

    public List<TaskDTO> getTaskList() {
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

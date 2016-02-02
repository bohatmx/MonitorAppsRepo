/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.boha.monitor.library.dto;


import java.io.Serializable;
import java.util.List;

/**
 * @author aubreyM
 */
public class RequestDTO implements Serializable {

    public RequestDTO() {
    }

    public RequestDTO(int requestType) {
        this.requestType = requestType;
    }

    private boolean rideWebSocket = false;
    private boolean zipResponse = false;

    private Integer requestType, companyID, staffID,
            projectID, projectTaskID, loginType, monitorID, taskTypeID;
    private Boolean responseRequested;
    private Integer numberOfDays;

    private String email, pin, gcmRegistrationID;
    private Long startDate, endDate;
    private Double latitude, longitude;
    private Float accuracy;
    private SimpleMessageDTO simpleMessage;
    private CompanyDTO company;
    private StaffDTO staff;
    private TaskTypeDTO taskType;
    private ProjectDTO project;
    private GcmDeviceDTO gcmDevice;
    private ChatMessageDTO chatMessage;
    private ProjectTaskDTO projectTask;
    private VideoUploadDTO videoUpload;
    private LocationTrackerDTO locationTracker;

    private List<TaskDTO> taskList;
    private List<StaffProjectDTO> staffProjectList;
    private List<MonitorProjectDTO> monitorProjectList;
    private List<PortfolioDTO> portfolioList;
    private List<ProgrammeDTO> programmeList;
    private List<ProjectDTO> projectList;
    private List<TaskTypeDTO> taskTypeList;
    private List<TaskStatusTypeDTO> taskStatusTypeList;
    private List<ProjectStatusTypeDTO> projectStatusTypeList;
    private List<StaffDTO> staffList;
    private List<MonitorDTO> monitorList;
    private List<ProjectTaskDTO> projectTaskList;

    private TaskDTO task;
    private TaskStatusTypeDTO taskStatus;
    private ChatDTO chat;

    private ProjectTaskStatusDTO projectTaskStatus;
    private ProjectStatusTypeDTO projectStatusType;
    private PhotoUploadDTO photoUpload;
    private List<LocationTrackerDTO> locationTrackerList;
    private List<ChatMemberDTO> chatMemberList;
    private List<CompanyDTO> companyList;

    //register actors
    public static final int
            REGISTER_COMPANY = 1,
            IMPORT_TASK_INFO = 2,
            IMPORT_PROJECT_INFO = 3,
            NOTIFY_SUPERVISOR_NO_PROJECTS = 30;
    //add stuff
    public static final int
            ADD_PROJECT_DIARY_RECORD = 12,
            ADD_PROJECT_TASK_STATUS = 13,
            ADD_PROJECT_STATUS = 13,
            ADD_DEVICE = 17,
            ADD_PHOTO = 18,
            ADD_VIDEO = 19;
    //get stuff
    public static final int
            GET_COMPANY_LIST = 100,
            GET_PROJECT_DATA = 101,
            GET_PROJECT_SITE_DATA = 102,
            GET_SITE_IMAGE_FILENAMES = 103,
            GET_TASK_IMAGE_FILENAMES = 104,
            GET_COMPANY_STAFF = 105,
            GET_TASK_STATUS_LIST = 106,
            GET_COMPANY_STAFF_TYPE_LIST = 107,
            GET_COMPANY_DATA = 108,
            GET_COUNTRY_LIST = 109,
            GET_PROJECT_IMAGES = 110,
            GET_ALL_PROJECT_IMAGES = 113,
            GET_TASK_IMAGES = 112,
            GET_CONTRACTOR_CLAIMS_BY_PROJECT = 114,
            GET_CONTRACTOR_CLAIMS_BY_COMPANY = 115,
            GET_PROJECT_STATUS = 116,
            GET_COMPANY_STATUS_IN_PERIOD = 117,
            GET_PROJECT_STATUS_IN_PERIOD = 118,
            GET_SITE_STATUS_IN_PERIOD = 119,
            GET_PROJECT_SITES = 120,
            GET_ERROR_REPORTS = 121,
            GET_LOCATION_TRACK_BY_STAFF = 122,
            GET_LOCATION_TRACK_BY_STAFF_IN_PERIOD = 123,
            GET_LOCATION_TRACK_BY_COMPANY_IN_PERIOD = 124,
            GET_STAFF_DATA = 125,
            GET_CHATS_BY_PROJECT = 126,
            GET_CHATS_BY_PROJECT_AND_STAFF = 127,
            GET_MESSAGES_BY_PROJECT = 128,
            GET_MONITOR_PROJECTS = 129,
            GET_PORTFOLIO_LIST = 130,
            GET_MONITOR_PHOTOS = 131,
            GET_LOCATION_TRACK_BY_MONITOR_IN_PERIOD = 132,
            GET_STAFF_PHOTOS = 133,
            GET_MONITOR_SUMMARY = 134,
            GET_PROJECT_STATUS_PHOTOS = 135,
            GET_COMPANY_DEVICES = 136,
            GET_LOCATION_TRACK_BY_DEVICE = 137;

    //
    public static final int
            LOGIN_STAFF = 200,
            LOGIN_STAFF_DATA_SETUP = 201,
            LOGIN_MONITOR = 202,
            SEND_GCM_REGISTRATION = 204,
            UPDATE_GCM_REGISTRATION = 205;
    //
    public static final int ADD_COMPANY_TASK = 301,
            ADD_COMPANY_TASK_STATUS = 302,
            ADD_COMPANY_PROJECT_STATUS_TYPE = 303,
            ADD_COMPANY_CHECKPOINT = 304,
            ADD_TASK_STATUS_TYPE = 305,

    CONFIRM_LOCATION = 310,
            SYNC_CACHED_REQUESTS = 313,

    ADD_PORTFOLOIOS = 314,
            ADD_PROGRAMMES = 315,
            ADD_PROJECTS = 316,
            ADD_TASKS = 317,
            ADD_TASK_TYPES = 318,
            ADD_PROJECT_STATUS_TYPES = 319,
            ADD_TASK_STATUS_TYPES = 320,
            ADD_MONITOR_PROJECTS = 321,
            ADD_STAFF_PROJECTS = 322,
            ADD_LOCATION_TRACKERS = 323,


    ADD_CHAT = 324,
            ADD_CHAT_MEMBERS = 325,
            ADD_PROJECT_TASKS = 326,
            ADD_STAFF = 327,
            ADD_MONITORS = 328,
            ADD_SUB_TASKS = 329,
            ADD_LOCATION_TRACK = 330;

    //updates
    public static final int
            UPDATE_COMPANY_TASK = 401,
            UPDATE_COMPANY_TASK_STATUS = 402,
            UPDATE_COMPANY_PROJECT_STATUS_TYPE = 403,
            UPDATE_COMPANY_CHECKPOINT = 404,
            UPDATE_PROJECT = 405,
            UPDATE_COMPANY_STAFF = 407,
            RESET_STAFF_PIN = 408,
            UPDATE_STAFF_PROJECTS = 409,
            UPDATE_MONITOR = 410,
            UPDATE_STAFF_DEVICE = 411,
            UPDATE_MONITOR_DEVICE = 412;

    //reports
    public static final int REPORT_PROJECT = 601,
            REPORT_SITE = 602,
            GET_PROJECT_STATUS_LIST = 603,
            GET_PROJECT_SITE_STATUS_LIST = 604,
            GET_EXEC_COMPANY_DATA = 605,
            DELETE_SITE_IMAGES = 606,
            DELETE_PROJECT_IMAGES = 607;

    //chat
    public static final int
            REQUEST_CHAT = 700,
            REQUEST_LOCATION = 701,
            SEND_CHAT_MESSAGE = 702,
            SEND_LOCATION = 703,
            SEND_SIMPLE_MESSAGE = 704;

    public static final String COMPANY_DIR = "company";
    public static final String STAFF_DIR = "staff";
    public static final String PROJECT_DIR = "project";
    public static final String TASK_DIR = "task";
    //


    public SimpleMessageDTO getSimpleMessage() {
        return simpleMessage;
    }

    public void setSimpleMessage(SimpleMessageDTO simpleMessage) {
        this.simpleMessage = simpleMessage;
    }

    public boolean isZipResponse() {
        return zipResponse;
    }

    public void setZipResponse(boolean zipResponse) {
        this.zipResponse = zipResponse;
    }

    public boolean getRideWebSocket() {
        return rideWebSocket;
    }



    public Integer getNumberOfDays() {
        return numberOfDays;
    }

    public void setNumberOfDays(Integer numberOfDays) {
        this.numberOfDays = numberOfDays;
    }

    public LocationTrackerDTO getLocationTracker() {
        return locationTracker;
    }

    public void setLocationTracker(LocationTrackerDTO locationTracker) {
        this.locationTracker = locationTracker;
    }

    public VideoUploadDTO getVideoUpload() {
        return videoUpload;
    }

    public void setVideoUpload(VideoUploadDTO videoUpload) {
        this.videoUpload = videoUpload;
    }

    public boolean isRideWebSocket() {
        return rideWebSocket;
    }

    public void setRideWebSocket(boolean rideWebSocket) {
        this.rideWebSocket = rideWebSocket;
    }

    public Integer getRequestType() {
        return requestType;
    }

    public void setRequestType(Integer requestType) {
        this.requestType = requestType;
    }

    public Integer getCompanyID() {
        return companyID;
    }

    public void setCompanyID(Integer companyID) {
        this.companyID = companyID;
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

    public TaskTypeDTO getTaskType() {
        return taskType;
    }

    public void setTaskType(TaskTypeDTO taskType) {
        this.taskType = taskType;
    }

    public Integer getProjectTaskID() {
        return projectTaskID;
    }

    public void setProjectTaskID(Integer projectTaskID) {
        this.projectTaskID = projectTaskID;
    }

    public Integer getLoginType() {
        return loginType;
    }

    public void setLoginType(Integer loginType) {
        this.loginType = loginType;
    }

    public Integer getMonitorID() {
        return monitorID;
    }

    public void setMonitorID(Integer monitorID) {
        this.monitorID = monitorID;
    }

    public List<CompanyDTO> getCompanyList() {
        return companyList;
    }

    public void setCompanyList(List<CompanyDTO> companyList) {
        this.companyList = companyList;
    }

    public Integer getTaskTypeID() {
        return taskTypeID;
    }

    public void setTaskTypeID(Integer taskTypeID) {
        this.taskTypeID = taskTypeID;
    }

    public boolean isResponseRequested() {
        return responseRequested;
    }

    public void setResponseRequested(boolean responseRequested) {
        this.responseRequested = responseRequested;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPin() {
        return pin;
    }

    public void setPin(String pin) {
        this.pin = pin;
    }

    public String getGcmRegistrationID() {
        return gcmRegistrationID;
    }

    public void setGcmRegistrationID(String gcmRegistrationID) {
        this.gcmRegistrationID = gcmRegistrationID;
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

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Float getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(Float accuracy) {
        this.accuracy = accuracy;
    }

    public CompanyDTO getCompany() {
        return company;
    }

    public void setCompany(CompanyDTO company) {
        this.company = company;
    }

    public StaffDTO getStaff() {
        return staff;
    }

    public void setStaff(StaffDTO staff) {
        this.staff = staff;
    }

    public ProjectDTO getProject() {
        return project;
    }

    public void setProject(ProjectDTO project) {
        this.project = project;
    }

    public GcmDeviceDTO getGcmDevice() {
        return gcmDevice;
    }

    public void setGcmDevice(GcmDeviceDTO gcmDevice) {
        this.gcmDevice = gcmDevice;
    }

    public ChatMessageDTO getChatMessage() {
        return chatMessage;
    }

    public void setChatMessage(ChatMessageDTO chatMessage) {
        this.chatMessage = chatMessage;
    }

    public ProjectTaskDTO getProjectTask() {
        return projectTask;
    }

    public void setProjectTask(ProjectTaskDTO projectTask) {
        this.projectTask = projectTask;
    }

    public List<TaskDTO> getTaskList() {
        return taskList;
    }

    public void setTaskList(List<TaskDTO> taskList) {
        this.taskList = taskList;
    }

    public List<StaffProjectDTO> getStaffProjectList() {
        return staffProjectList;
    }

    public void setStaffProjectList(List<StaffProjectDTO> staffProjectList) {
        this.staffProjectList = staffProjectList;
    }

    public List<MonitorProjectDTO> getMonitorProjectList() {
        return monitorProjectList;
    }

    public void setMonitorProjectList(List<MonitorProjectDTO> monitorProjectList) {
        this.monitorProjectList = monitorProjectList;
    }

    public List<PortfolioDTO> getPortfolioList() {
        return portfolioList;
    }

    public void setPortfolioList(List<PortfolioDTO> portfolioList) {
        this.portfolioList = portfolioList;
    }

    public List<ProgrammeDTO> getProgrammeList() {
        return programmeList;
    }

    public void setProgrammeList(List<ProgrammeDTO> programmeList) {
        this.programmeList = programmeList;
    }

    public List<ProjectDTO> getProjectList() {
        return projectList;
    }

    public void setProjectList(List<ProjectDTO> projectList) {
        this.projectList = projectList;
    }

    public List<TaskTypeDTO> getTaskTypeList() {
        return taskTypeList;
    }

    public void setTaskTypeList(List<TaskTypeDTO> taskTypeList) {
        this.taskTypeList = taskTypeList;
    }

    public List<TaskStatusTypeDTO> getTaskStatusTypeList() {
        return taskStatusTypeList;
    }

    public void setTaskStatusTypeList(List<TaskStatusTypeDTO> taskStatusTypeList) {
        this.taskStatusTypeList = taskStatusTypeList;
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

    public List<MonitorDTO> getMonitorList() {
        return monitorList;
    }

    public void setMonitorList(List<MonitorDTO> monitorList) {
        this.monitorList = monitorList;
    }

    public List<ProjectTaskDTO> getProjectTaskList() {
        return projectTaskList;
    }

    public void setProjectTaskList(List<ProjectTaskDTO> projectTaskList) {
        this.projectTaskList = projectTaskList;
    }

    public TaskDTO getTask() {
        return task;
    }

    public void setTask(TaskDTO task) {
        this.task = task;
    }

    public TaskStatusTypeDTO getTaskStatus() {
        return taskStatus;
    }

    public void setTaskStatus(TaskStatusTypeDTO taskStatus) {
        this.taskStatus = taskStatus;
    }

    public ChatDTO getChat() {
        return chat;
    }

    public void setChat(ChatDTO chat) {
        this.chat = chat;
    }

    public ProjectTaskStatusDTO getProjectTaskStatus() {
        return projectTaskStatus;
    }

    public void setProjectTaskStatus(ProjectTaskStatusDTO projectTaskStatus) {
        this.projectTaskStatus = projectTaskStatus;
    }

    public ProjectStatusTypeDTO getProjectStatusType() {
        return projectStatusType;
    }

    public void setProjectStatusType(ProjectStatusTypeDTO projectStatusType) {
        this.projectStatusType = projectStatusType;
    }

    public PhotoUploadDTO getPhotoUpload() {
        return photoUpload;
    }

    public void setPhotoUpload(PhotoUploadDTO photoUpload) {
        this.photoUpload = photoUpload;
    }

    public List<LocationTrackerDTO> getLocationTrackerList() {
        return locationTrackerList;
    }

    public void setLocationTrackerList(List<LocationTrackerDTO> locationTrackerList) {
        this.locationTrackerList = locationTrackerList;
    }

    public List<ChatMemberDTO> getChatMemberList() {
        return chatMemberList;
    }

    public void setChatMemberList(List<ChatMemberDTO> chatMemberList) {
        this.chatMemberList = chatMemberList;
    }
}

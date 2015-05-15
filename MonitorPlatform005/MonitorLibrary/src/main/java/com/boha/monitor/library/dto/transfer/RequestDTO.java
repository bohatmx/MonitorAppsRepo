/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.boha.monitor.library.dto.transfer;


import com.boha.monitor.library.dto.BankDTO;
import com.boha.monitor.library.dto.BankDetailDTO;
import com.boha.monitor.library.dto.BeneficiaryDTO;
import com.boha.monitor.library.dto.ChatDTO;
import com.boha.monitor.library.dto.ChatMemberDTO;
import com.boha.monitor.library.dto.ChatMessageDTO;
import com.boha.monitor.library.dto.CityDTO;
import com.boha.monitor.library.dto.ClientDTO;
import com.boha.monitor.library.dto.CompanyDTO;
import com.boha.monitor.library.dto.CompanyStaffDTO;
import com.boha.monitor.library.dto.ContractorClaimDTO;
import com.boha.monitor.library.dto.ContractorClaimSiteDTO;
import com.boha.monitor.library.dto.EngineerDTO;
import com.boha.monitor.library.dto.GcmDeviceDTO;
import com.boha.monitor.library.dto.InvoiceDTO;
import com.boha.monitor.library.dto.InvoiceItemDTO;
import com.boha.monitor.library.dto.LocationTrackerDTO;
import com.boha.monitor.library.dto.ProjectDTO;
import com.boha.monitor.library.dto.ProjectDiaryRecordDTO;
import com.boha.monitor.library.dto.ProjectSiteDTO;
import com.boha.monitor.library.dto.ProjectSiteTaskDTO;
import com.boha.monitor.library.dto.ProjectSiteTaskStatusDTO;
import com.boha.monitor.library.dto.ProjectStatusTypeDTO;
import com.boha.monitor.library.dto.SubTaskDTO;
import com.boha.monitor.library.dto.SubTaskStatusDTO;
import com.boha.monitor.library.dto.TaskDTO;
import com.boha.monitor.library.dto.TaskPriceDTO;
import com.boha.monitor.library.dto.TaskStatusDTO;
import com.boha.monitor.library.dto.TownshipDTO;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @author aubreyM
 */
public class RequestDTO implements Serializable {
    public RequestDTO(Integer requestType) {
        this.requestType = requestType;
        System.out.println("requestType in constructor: " + requestType);
    }

    public RequestDTO() {
    }

    private Integer requestType, companyID, companyStaffID, projectID,
            projectSiteID, projectSiteTaskID,loginType,
            countryID, contractorClaimID, invoiceID,
            beneficiaryID, engineerID;
    private Boolean responseRequested,
            useHttp = Boolean.TRUE, rideWebSocket = Boolean.FALSE;
    private SubTaskStatusDTO subTaskStatus;
    private Long startDate, endDate;
    private Float accuracy;
    private String email, pin, gcmRegistrationID;
    private Double latitude, longitude;
    private CompanyDTO company;
    private SubTaskDTO subTask;
    private TaskPriceDTO taskPrice;
    private CompanyStaffDTO companyStaff;
    private ProjectDTO project;
    private EngineerDTO engineer;
    private CityDTO city;
    private TownshipDTO township;
    private ProjectSiteDTO projectSite;
    private GcmDeviceDTO gcmDevice;
    private BankDTO bank;
    private BankDetailDTO bankDetail;
    private ContractorClaimDTO contractorClaim;
    private ContractorClaimSiteDTO contractorClaimSite;
    private InvoiceDTO invoice;
    private InvoiceItemDTO invoiceItem;

    private TaskDTO task;
    private TaskStatusDTO taskStatus;
    private ClientDTO client;
    private ChatDTO chat;
    private ChatMessageDTO chatMessage;

    private ProjectSiteTaskDTO projectSiteTask;
    private ProjectDiaryRecordDTO projectDiaryRecord;
    private ProjectSiteTaskStatusDTO projectSiteTaskStatus;
    private ProjectStatusTypeDTO projectStatusType;
    private BeneficiaryDTO beneficiary;
    private List<BeneficiaryDTO> beneficiaryList;
    private List<PhotoUploadDTO> photoUploadList;
    private RequestList requestList;
    private List<LocationTrackerDTO> locationTrackerList;
    private List<ChatMemberDTO> chatMemberList;

    public ChatDTO getChat() {
        return chat;
    }

    public void setChat(ChatDTO chat) {
        this.chat = chat;
    }

    public ChatMessageDTO getChatMessage() {
        return chatMessage;
    }

    public void setChatMessage(ChatMessageDTO chatMessage) {
        this.chatMessage = chatMessage;
    }

    public List<PhotoUploadDTO> getPhotoUploadList() {
        return photoUploadList;
    }

    public void setPhotoUploadList(List<PhotoUploadDTO> photoUploadList) {
        this.photoUploadList = photoUploadList;
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

    public Boolean getResponseRequested() {
        return responseRequested;
    }

    public void setResponseRequested(Boolean responseRequested) {
        this.responseRequested = responseRequested;
    }

    public Boolean getRideWebSocket() {
        return rideWebSocket;
    }

    public void setRideWebSocket(Boolean rideWebSocket) {
        this.rideWebSocket = rideWebSocket;
    }

    //register actors
    public static final int
            REGISTER_COMPANY = 1,
            REGISTER_COMPANY_STAFF = 2,
            REGISTER_PROJECT = 3,
            REGISTER_PROJECT_SITE = 4,
            REGISTER_PROJECT_SITE_STAFF = 5,
            REGISTER_CLIENT = 6,
            REGISTER_BENEFICIARY = 7,
            REGISTER_ENGINEER = 8,
            IMPORT_BENEFICIARIES = 9,
            ADD_LOCATION_TRACKERS = 10,
            ADD_STAFF_PROJECTS = 21,
            ADD_CHAT = 22,
            ADD_CHAT_MEMBERS = 23;

    //add stuff
    public static final int
            ADD_PROJECT_SITE_TASK = 11,
            ADD_PROJECT_DIARY_RECORD = 12,
            ADD_PROJECT_SITE_TASK_STATUS = 13,
            ADD_PROJECT_STATUS_TYPE = 14,
            ADD_DEVICE = 17,
            CONNECT_BENEFICIARY_TO_SITE = 18,
            CONNECT_ENGINEER_TO_PROJECT = 19;
    //get stuff
    public static final int
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
            GET_SITE_IMAGES = 111,
            GET_TASK_IMAGES = 112,
            GET_CONTRACTOR_CLAIMS_BY_PROJECT = 114,
            GET_CONTRACTOR_CLAIMS_BY_COMPANY = 115,
            GET_SITE_STATUS = 116,
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
            GET_MESSAGES_BY_PROJECT = 128;
    //login's
    public static final int
            LOGIN = 200,
            SEND_GCM_REGISTRATION = 204;
    //lookups
    public static final int
            ADD_COMPANY_TASK = 301,
            ADD_COMPANY_TASK_STATUS = 302,
            ADD_COMPANY_PROJECT_STATUS_TYPE = 303,
            ADD_COMPANY_CHECKPOINT = 304,
            ADD_CITY = 305,
            ADD_TOWNSHIP = 306,
            ADD_SITE_TASK = 307,
            ADD_BANK_DETAILS = 308,
            ADD_BANK = 309,
            CONFIRM_LOCATION = 310,
            ADD_SUB_TASK = 311,
            ADD_SUBTASK_STATUS = 312,
            SYNC_CACHED_REQUESTS = 313;

    //updates
    public static final int
            UPDATE_COMPANY_TASK = 401,
            UPDATE_COMPANY_TASK_STATUS = 402,
            UPDATE_COMPANY_PROJECT_STATUS_TYPE = 403,
            UPDATE_COMPANY_CHECKPOINT = 404,
            UPDATE_PROJECT = 405,
            UPDATE_PROJECT_SITE = 406,
            UPDATE_COMPANY_STAFF = 407,
            RESET_STAFF_PIN = 408;
    //invoice * claim
    public static final int
            ADD_INVOICE = 501,
            ADD_INVOICE_ITEM = 502,
            REMOVE_INVOICE_ITEM = 503,
            CREATE_INVOICE_PDF = 504,
            REMOVE_INVOICE = 505,
            REMOVE_CONTRACTOR_CLAIM = 507,
            REMOVE_CONTRACTOR_CLAIM_SITE = 508,
            GET_PROJECT_INVOICES = 506,
            ADD_CONTRACTOR_CLAIM = 511,
            GENERATE_CONTRACTOR_CLAIM_PDF = 513,
            GENERATE_INVOICE_PDF = 514,
            ADD_CONTRACTOR_CLAIM_SITE = 512;
    //reports
    public static final int
            REPORT_PROJECT = 601,
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
            SEND_CHAT_MESSAGE = 702;

    public static final String COMPANY_DIR = "company";
    public static final String COMPANY_STAFF_DIR = "companyStaff";
    public static final String PROJECT_DIR = "project";
    public static final String PROJECT_SITE_DIR = "projectsite";
    public static final String TASK_DIR = "task";

    //


    public Boolean getUseHttp() {
        return useHttp;
    }

    public void setUseHttp(Boolean useHttp) {
        this.useHttp = useHttp;
    }

    public RequestList getRequestList() {
        return requestList;
    }

    public void setRequestList(RequestList requestList) {
        this.requestList = requestList;
    }

    public boolean isResponseRequested() {
        return responseRequested;
    }

    public void setResponseRequested(boolean responseRequested) {
        this.responseRequested = responseRequested;
    }

    public Float getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(Float accuracy) {
        this.accuracy = accuracy;
    }

    public List<BeneficiaryDTO> getBeneficiaryList() {
        return beneficiaryList;
    }

    public void setBeneficiaryList(List<BeneficiaryDTO> beneficiaryList) {
        this.beneficiaryList = beneficiaryList;
    }

    public SubTaskStatusDTO getSubTaskStatus() {
        return subTaskStatus;
    }

    public void setSubTaskStatus(SubTaskStatusDTO subTaskStatus) {
        this.subTaskStatus = subTaskStatus;
    }

    public TaskPriceDTO getTaskPrice() {
        return taskPrice;
    }

    public void setTaskPrice(TaskPriceDTO taskPrice) {
        this.taskPrice = taskPrice;
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

    public SubTaskDTO getSubTask() {
        return subTask;
    }

    public void setSubTask(SubTaskDTO subTask) {
        this.subTask = subTask;
    }

    public EngineerDTO getEngineer() {
        return engineer;
    }

    public void setEngineer(EngineerDTO engineer) {
        this.engineer = engineer;
    }

    public Integer getBeneficiaryID() {
        return beneficiaryID;
    }

    public void setBeneficiaryID(Integer beneficiaryID) {
        this.beneficiaryID = beneficiaryID;
    }

    public Integer getEngineerID() {
        return engineerID;
    }

    public void setEngineerID(Integer engineerID) {
        this.engineerID = engineerID;
    }

    public Integer getContractorClaimID() {
        return contractorClaimID;
    }

    public void setContractorClaimID(Integer contractorClaimID) {
        this.contractorClaimID = contractorClaimID;
    }

    public Integer getInvoiceID() {
        return invoiceID;
    }

    public void setInvoiceID(Integer invoiceID) {
        this.invoiceID = invoiceID;
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

    public Integer getCompanyStaffID() {
        return companyStaffID;
    }

    public void setCompanyStaffID(Integer companyStaffID) {
        this.companyStaffID = companyStaffID;
    }

    public Integer getProjectID() {
        return projectID;
    }

    public void setProjectID(Integer projectID) {
        this.projectID = projectID;
    }

    public Integer getProjectSiteID() {
        return projectSiteID;
    }

    public void setProjectSiteID(Integer projectSiteID) {
        this.projectSiteID = projectSiteID;
    }

    public Integer getProjectSiteTaskID() {
        return projectSiteTaskID;
    }

    public void setProjectSiteTaskID(Integer projectSiteTaskID) {
        this.projectSiteTaskID = projectSiteTaskID;
    }

    public Integer getLoginType() {
        return loginType;
    }

    public void setLoginType(Integer loginType) {
        this.loginType = loginType;
    }

    public Integer getCountryID() {
        return countryID;
    }

    public void setCountryID(Integer countryID) {
        this.countryID = countryID;
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

    public CompanyDTO getCompany() {
        return company;
    }

    public void setCompany(CompanyDTO company) {
        this.company = company;
    }

    public CompanyStaffDTO getCompanyStaff() {
        return companyStaff;
    }

    public void setCompanyStaff(CompanyStaffDTO companyStaff) {
        this.companyStaff = companyStaff;
    }

    public ProjectDTO getProject() {
        return project;
    }

    public void setProject(ProjectDTO project) {
        this.project = project;
    }

    public CityDTO getCity() {
        return city;
    }

    public void setCity(CityDTO city) {
        this.city = city;
    }

    public TownshipDTO getTownship() {
        return township;
    }

    public void setTownship(TownshipDTO township) {
        this.township = township;
    }

    public ProjectSiteDTO getProjectSite() {
        return projectSite;
    }

    public void setProjectSite(ProjectSiteDTO projectSite) {
        this.projectSite = projectSite;
    }

    public GcmDeviceDTO getGcmDevice() {
        return gcmDevice;
    }

    public void setGcmDevice(GcmDeviceDTO gcmDevice) {
        this.gcmDevice = gcmDevice;
    }

    public BankDTO getBank() {
        return bank;
    }

    public void setBank(BankDTO bank) {
        this.bank = bank;
    }

    public BankDetailDTO getBankDetail() {
        return bankDetail;
    }

    public void setBankDetail(BankDetailDTO bankDetail) {
        this.bankDetail = bankDetail;
    }

    public ContractorClaimDTO getContractorClaim() {
        return contractorClaim;
    }

    public void setContractorClaim(ContractorClaimDTO contractorClaim) {
        this.contractorClaim = contractorClaim;
    }

    public ContractorClaimSiteDTO getContractorClaimSite() {
        return contractorClaimSite;
    }

    public void setContractorClaimSite(ContractorClaimSiteDTO contractorClaimSite) {
        this.contractorClaimSite = contractorClaimSite;
    }

    public InvoiceDTO getInvoice() {
        return invoice;
    }

    public void setInvoice(InvoiceDTO invoice) {
        this.invoice = invoice;
    }

    public InvoiceItemDTO getInvoiceItem() {
        return invoiceItem;
    }

    public void setInvoiceItem(InvoiceItemDTO invoiceItem) {
        this.invoiceItem = invoiceItem;
    }

    public TaskDTO getTask() {
        return task;
    }

    public void setTask(TaskDTO task) {
        this.task = task;
    }

    public TaskStatusDTO getTaskStatus() {
        return taskStatus;
    }

    public void setTaskStatus(TaskStatusDTO taskStatus) {
        this.taskStatus = taskStatus;
    }

    public ClientDTO getClient() {
        return client;
    }

    public void setClient(ClientDTO client) {
        this.client = client;
    }

    public ProjectSiteTaskDTO getProjectSiteTask() {
        return projectSiteTask;
    }

    public void setProjectSiteTask(ProjectSiteTaskDTO projectSiteTask) {
        this.projectSiteTask = projectSiteTask;
    }

    public ProjectDiaryRecordDTO getProjectDiaryRecord() {
        return projectDiaryRecord;
    }

    public void setProjectDiaryRecord(ProjectDiaryRecordDTO projectDiaryRecord) {
        this.projectDiaryRecord = projectDiaryRecord;
    }

    public ProjectSiteTaskStatusDTO getProjectSiteTaskStatus() {
        return projectSiteTaskStatus;
    }

    public void setProjectSiteTaskStatus(ProjectSiteTaskStatusDTO projectSiteTaskStatus) {
        this.projectSiteTaskStatus = projectSiteTaskStatus;
    }

    public ProjectStatusTypeDTO getProjectStatusType() {
        return projectStatusType;
    }

    public void setProjectStatusType(ProjectStatusTypeDTO projectStatusType) {
        this.projectStatusType = projectStatusType;
    }

    public BeneficiaryDTO getBeneficiary() {
        return beneficiary;
    }

    public void setBeneficiary(BeneficiaryDTO beneficiary) {
        this.beneficiary = beneficiary;
    }
}

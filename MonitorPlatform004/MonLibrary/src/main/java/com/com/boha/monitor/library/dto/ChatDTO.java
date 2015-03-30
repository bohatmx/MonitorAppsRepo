package com.com.boha.monitor.library.dto;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * Created by aubreyM on 15/03/28.
 */
public class ChatDTO implements Serializable {
    private Integer chatID,projectID, projectSiteID;
    private Date dateStarted;
    private String message;
    private List<ChatMemberDTO> chatMemberList;
    private CompanyStaffDTO companyStaff;

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

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Integer getChatID() {
        return chatID;
    }

    public void setChatID(Integer chatID) {
        this.chatID = chatID;
    }

    public Date getDateStarted() {
        return dateStarted;
    }

    public void setDateStarted(Date dateStarted) {
        this.dateStarted = dateStarted;
    }

    public List<ChatMemberDTO> getChatMemberList() {
        return chatMemberList;
    }

    public void setChatMemberList(List<ChatMemberDTO> chatMemberList) {
        this.chatMemberList = chatMemberList;
    }

    public CompanyStaffDTO getCompanyStaff() {
        return companyStaff;
    }

    public void setCompanyStaff(CompanyStaffDTO companyStaff) {
        this.companyStaff = companyStaff;
    }
}

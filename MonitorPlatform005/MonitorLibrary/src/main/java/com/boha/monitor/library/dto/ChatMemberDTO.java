package com.boha.monitor.library.dto;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by aubreyM on 15/03/28.
 */
public class ChatMemberDTO implements Serializable{
    private Integer chatMemberID;
    private Date dateJoined;
    private CompanyStaffDTO companyStaff, chatOwner;
    private Integer chatID;


    public Integer getChatMemberID() {
        return chatMemberID;
    }

    public CompanyStaffDTO getChatOwner() {
        return chatOwner;
    }

    public void setChatOwner(CompanyStaffDTO chatOwner) {
        this.chatOwner = chatOwner;
    }

    public void setChatMemberID(Integer chatMemberID) {
        this.chatMemberID = chatMemberID;
    }

    public Date getDateJoined() {
        return dateJoined;
    }

    public void setDateJoined(Date dateJoined) {
        this.dateJoined = dateJoined;
    }

    public CompanyStaffDTO getCompanyStaff() {
        return companyStaff;
    }

    public void setCompanyStaff(CompanyStaffDTO companyStaff) {
        this.companyStaff = companyStaff;
    }

    public Integer getChatID() {
        return chatID;
    }

    public void setChatID(Integer chatID) {
        this.chatID = chatID;
    }
}

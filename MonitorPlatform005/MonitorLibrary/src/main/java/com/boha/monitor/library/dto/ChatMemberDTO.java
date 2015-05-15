package com.boha.monitor.library.dto;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by aubreyM on 15/03/28.
 */
public class ChatMemberDTO implements Serializable{
    private Integer chatMemberID;
    private Long dateJoined;
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

    public Long getDateJoined() {
        return dateJoined;
    }

    public void setDateJoined(Long dateJoined) {
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

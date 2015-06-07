/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.boha.monitor.library.dto;

import java.io.Serializable;

/**
 *
 * @author aubreyM
 */
public class ChatMemberDTO implements Serializable {
 private static final long serialVersionUID = 1L;
    private Integer chatMemberID;
    private Long dateJoined;
    private StaffDTO staff, chatOwner;
    private MonitorDTO monitor;
    private Integer chatID;

    public ChatMemberDTO() {
    }


    public Integer getChatMemberID() {
        return chatMemberID;
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

    public StaffDTO getStaff() {
        return staff;
    }

    public void setStaff(StaffDTO staff) {
        this.staff = staff;
    }

    public StaffDTO getChatOwner() {
        return chatOwner;
    }

    public void setChatOwner(StaffDTO chatOwner) {
        this.chatOwner = chatOwner;
    }

    public MonitorDTO getMonitor() {
        return monitor;
    }

    public void setMonitor(MonitorDTO monitor) {
        this.monitor = monitor;
    }

    public Integer getChatID() {
        return chatID;
    }

    public void setChatID(Integer chatID) {
        this.chatID = chatID;
    }
   
}

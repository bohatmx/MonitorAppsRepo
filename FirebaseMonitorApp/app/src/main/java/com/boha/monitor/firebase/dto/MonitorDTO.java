/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.boha.monitor.firebase.dto;

import java.io.Serializable;

/**
 *
 * @author aubreyM
 */
public class MonitorDTO implements Serializable, Person, Comparable<MonitorDTO> {
    private static final long serialVersionUID = 1L;
    private String monitorID, companyID, projectID, userID;
    private Integer activeFlag, statusCount = 0,
            profilePhotoCount = 0,photoCount = 0,projectCount = 0;
    private String firstName;
    private String lastName;
    private String email, companyName, IDNumber, address;
    private String cellphone, pin;
    private Long appInvitationDate;
    private Short gender;

    private Boolean selected = Boolean.FALSE;

    public MonitorDTO() {
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public Integer getProjectCount() {
        if (projectCount == null) {
            projectCount = 0;
        }
        return projectCount;
    }

    public String getProjectID() {
        return projectID;
    }

    public void setProjectID(String projectID) {
        this.projectID = projectID;
    }

    public Integer getProfilePhotoCount() {
        return profilePhotoCount;
    }

    public void setProfilePhotoCount(Integer profilePhotoCount) {
        this.profilePhotoCount = profilePhotoCount;
    }

    public void setProjectCount(Integer projectCount) {
        this.projectCount = projectCount;
    }

    public Integer getStatusCount() {
        if (statusCount == null) {
            statusCount = 0;
        }
        return statusCount;
    }

    public void setStatusCount(Integer statusCount) {
        this.statusCount = statusCount;
    }

    public Integer getPhotoCount() {
        if (photoCount == null) {
            photoCount = 0;
        }
        return photoCount;
    }

    public void setPhotoCount(Integer photoCount) {
        this.photoCount = photoCount;
    }

    public Boolean getSelected() {
        return selected;
    }

    public Short getGender() {
        return gender;
    }

    public Boolean isSelected() {
        return selected;
    }

    public void setSelected(Boolean selected) {
        this.selected = selected;
    }

    public void setGender(Short gender) {
        this.gender = gender;
    }

    public String getIDNumber() {
        return IDNumber;
    }

    public void setIDNumber(String IDNumber) {
        this.IDNumber = IDNumber;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }

    public String getMonitorID() {
        return monitorID;
    }

    public void setMonitorID(String monitorID) {
        this.monitorID = monitorID;
    }

    public Integer getActiveFlag() {
        return activeFlag;
    }

    public void setActiveFlag(Integer activeFlag) {
        this.activeFlag = activeFlag;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getCellphone() {
        return cellphone;
    }

    public void setCellphone(String cellphone) {
        this.cellphone = cellphone;
    }

    public String getPin() {
        return pin;
    }

    public void setPin(String pin) {
        this.pin = pin;
    }

    public Long getAppInvitationDate() {
        return appInvitationDate;
    }

    public void setAppInvitationDate(Long appInvitationDate) {
        this.appInvitationDate = appInvitationDate;
    }

    public String getCompanyID() {
        return companyID;
    }

    public void setCompanyID(String companyID) {
        this.companyID = companyID;
    }


    @Override
    public int compareTo(MonitorDTO m) {
        String name1 = this.getLastName() + this.getFirstName();
        String name2 = m.getLastName() + m.getFirstName();
        return name1.compareTo(name2);
    }
}

package com.boha.monitor.firebase.dto;

import java.util.HashMap;

/**
 * Created by aubreymalabie on 5/22/16.
 */

public class MonitorCompanyDTO {

    private String companyName, email, companyID, address;
    private HashMap<String, Object> projects;
    private long dateRegistered;

    public  MonitorCompanyDTO() {}

    public HashMap<String, Object> getProjects() {
        return projects;
    }

    public void setProjects(HashMap<String, Object> projects) {
        this.projects = projects;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCompanyID() {
        return companyID;
    }

    public void setCompanyID(String companyID) {
        this.companyID = companyID;
    }

    public long getDateRegistered() {
        return dateRegistered;
    }

    public void setDateRegistered(long dateRegistered) {
        this.dateRegistered = dateRegistered;
    }
}

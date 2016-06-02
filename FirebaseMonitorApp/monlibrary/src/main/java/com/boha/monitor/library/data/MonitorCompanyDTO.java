package com.boha.monitor.library.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by aubreymalabie on 5/22/16.
 */

public class MonitorCompanyDTO implements Serializable{

    private String companyName, email, companyID, address;
    private HashMap<String, Object> companyProjects;
    private HashMap<String, ProjectDTO> projects;
    private HashMap<String, UserDTO> users;
    private long dateRegistered;

    public  MonitorCompanyDTO() {}

    public HashMap<String, Object> getCompanyProjects() {
        return companyProjects;
    }

    public void setCompanyProjects(HashMap<String, Object> companyProjects) {
        this.companyProjects = companyProjects;
    }

    public HashMap<String, ProjectDTO> getProjects() {
        return projects;
    }

    public List<ProjectDTO> getProjectList() {
        List<ProjectDTO> list = new ArrayList<>();
        if (projects != null) {
            for (ProjectDTO p : projects.values()) {
                list.add(p);
            }
        }
        return list;
    }
    public void setProjects(HashMap<String, ProjectDTO> projects) {
        this.projects = projects;
        getProjectList();
    }

    public HashMap<String, UserDTO> getUsers() {
        return users;
    }

    public void setUsers(HashMap<String, UserDTO> users) {
        this.users = users;
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

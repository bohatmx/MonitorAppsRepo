/*
 * To change this license header, choose License Headers in ProjectDTO Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.boha.monitor.library.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author aubreyM
 */
public class ProjectDTO implements Serializable, Comparable<ProjectDTO> {


    private static final long serialVersionUID = 1L;
    private String projectID = "NOT CREATED YET", cityID,companyID;
    private int
            statusCount = 0, photoCount = 0, projectTaskCount = 0,
            monitorCount = 0, staffCount = 0, videoCount = 0;
    private String projectName;
    private double latitude;
    private double longitude;
    private float accuracy;
    private boolean activeFlag;
    private boolean locationConfirmed;
    private String address,cityName, municipalityName;
    private String description;
    private HashMap<String, MonitorDTO> monitors;
    private HashMap<String, PhotoUploadDTO> photos;
    private List<PhotoUploadDTO> photoList;
    private List<MonitorDTO> monitorList;

    public List<PhotoUploadDTO> getPhotoList() {
        if (photoList == null) {
            photoList = new ArrayList<>();
        }
        return photoList;
    }

    private boolean selected;
    private long dateUploaded, dateRegistered;

    public ProjectDTO() {
    }

    public List<MonitorDTO> getMonitorList() {
        if (monitorList == null) {
            monitorList = new ArrayList<>();
        }
        return monitorList;
    }

    public void setMonitorList(List<MonitorDTO> monitorList) {
        this.monitorList = monitorList;
    }


    public HashMap<String, MonitorDTO> getMonitors() {
        return monitors;
    }

    public void setMonitors(HashMap<String, MonitorDTO> monitors) {
        monitorList = new ArrayList<>();
        if (monitors != null) {
            for (MonitorDTO m: monitors.values()) {
                monitorList.add(m);
            }
        }
        this.monitors = monitors;
    }

    public void setPhotoList(List<PhotoUploadDTO> photoList) {
        this.photoList = photoList;
    }

    public HashMap<String, PhotoUploadDTO> getPhotos() {
        return photos;
    }

    public void setPhotos(HashMap<String, PhotoUploadDTO> photos) {
        photoList = new ArrayList<>();
        if (photos != null) {
            for (PhotoUploadDTO m: photos.values()) {
                photoList.add(m);
            }
        }
        this.photos = photos;
    }

    public String getProjectID() {
        return projectID;
    }

    public void setProjectID(String projectID) {
        this.projectID = projectID;
    }

    public String getCityID() {
        return cityID;
    }

    public void setCityID(String cityID) {
        this.cityID = cityID;
    }

    public String getCompanyID() {
        return companyID;
    }

    public void setCompanyID(String companyID) {
        this.companyID = companyID;
    }

    public int getStatusCount() {
        return statusCount;
    }

    public void setStatusCount(int statusCount) {
        this.statusCount = statusCount;
    }

    public int getPhotoCount() {
        return photoCount;
    }

    public void setPhotoCount(int photoCount) {
        this.photoCount = photoCount;
    }

    public int getProjectTaskCount() {
        return projectTaskCount;
    }

    public void setProjectTaskCount(int projectTaskCount) {
        this.projectTaskCount = projectTaskCount;
    }

    public int getMonitorCount() {
        return monitorCount;
    }

    public void setMonitorCount(int monitorCount) {
        this.monitorCount = monitorCount;
    }

    public int getStaffCount() {
        return staffCount;
    }

    public void setStaffCount(int staffCount) {
        this.staffCount = staffCount;
    }

    public int getVideoCount() {
        return videoCount;
    }

    public void setVideoCount(int videoCount) {
        this.videoCount = videoCount;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public float getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(float accuracy) {
        this.accuracy = accuracy;
    }

    public boolean getActiveFlag() {
        return activeFlag;
    }

    public void setActiveFlag(boolean activeFlag) {
        this.activeFlag = activeFlag;
    }

    public boolean getLocationConfirmed() {
        return locationConfirmed;
    }

    public void setLocationConfirmed(boolean locationConfirmed) {
        this.locationConfirmed = locationConfirmed;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public String getMunicipalityName() {
        return municipalityName;
    }

    public void setMunicipalityName(String municipalityName) {
        this.municipalityName = municipalityName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public long getDateUploaded() {
        return dateUploaded;
    }

    public void setDateUploaded(long dateUploaded) {
        this.dateUploaded = dateUploaded;
    }

    public long getDateRegistered() {
        return dateRegistered;
    }

    public void setDateRegistered(long dateRegistered) {
        this.dateRegistered = dateRegistered;
    }

    @Override
    public String toString() {
        return "com.boha.monitor.data.Project[ projectID=" + projectID + " ]";
    }


    @Override
    public int compareTo(ProjectDTO another) {
        return this.projectName.compareTo(another.projectName);
    }
}

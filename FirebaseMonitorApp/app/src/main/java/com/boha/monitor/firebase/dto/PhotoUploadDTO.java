/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.boha.monitor.firebase.dto;
import java.io.Serializable;
import java.util.Date;

/**
 *
 * @author aubreyM
 */
public class PhotoUploadDTO implements Serializable, Comparable<PhotoUploadDTO> {
    /**
     * Compares this object to the specified object to determine their relative
     * order.
     *
     * @param another the object to compare to this instance.
     * @return a negative integer if this instance is less than {@code another};
     * a positive integer if this instance is greater than
     * {@code another}; 0 if this instance has the same order as
     * {@code another}.
     * @throws ClassCastException if {@code another} cannot be converted into something
     *                            comparable to {@code this} instance.
     */
    @Override
    public int compareTo(PhotoUploadDTO another) {

        Date thisDate = new Date(this.dateTaken.longValue());
        Date anotherDate = new Date(another.dateTaken.longValue());

        if (thisDate.after(anotherDate)) {
            return -1;
        }
        if (thisDate.before(anotherDate)) {
            return 1;
        }
        return 0;
    }

    private static final long serialVersionUID = 1L;
    private String photoUploadID = "x";
    private Long dateTaken = Long.parseLong("0");
    private Double latitude = Double.parseDouble("0.0");
    private Double longitude = Double.parseDouble("0.0");
    private Float accuracy = Float.parseFloat("0.0");
    private String  projectName = "", taskName,  monitorName = "";
    private Long dateUploaded = Long.parseLong("0");
    private String filePath;
    private String companyID = "x";
    private String projectID = "x";
    private String projectTaskID = "x";
    private String userID  = "x", monitorID = "", staffID = "";

    private String url = "url", bucketName = "x";
    private Boolean marked = Boolean.FALSE;
    private Integer sharedCount = 0;


    public PhotoUploadDTO() {
    }

    public String getMonitorID() {
        return monitorID;
    }

    public void setMonitorID(String monitorID) {
        this.monitorID = monitorID;
    }

    public String getStaffID() {
        return staffID;
    }

    public void setStaffID(String staffID) {
        this.staffID = staffID;
    }

    public String getBucketName() {
        return bucketName;
    }

    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }

    public String getPhotoUploadID() {
        return photoUploadID;
    }

    public void setPhotoUploadID(String photoUploadID) {
        this.photoUploadID = photoUploadID;
    }

    public Long getDateTaken() {
        return dateTaken;
    }

    public void setDateTaken(Long dateTaken) {
        this.dateTaken = dateTaken;
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

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public String getMonitorName() {
        return monitorName;
    }

    public void setMonitorName(String monitorName) {
        this.monitorName = monitorName;
    }

    public Long getDateUploaded() {
        return dateUploaded;
    }

    public void setDateUploaded(Long dateUploaded) {
        this.dateUploaded = dateUploaded;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getCompanyID() {
        return companyID;
    }

    public void setCompanyID(String companyID) {
        this.companyID = companyID;
    }

    public String getProjectID() {
        return projectID;
    }

    public void setProjectID(String projectID) {
        this.projectID = projectID;
    }

    public String getProjectTaskID() {
        return projectTaskID;
    }

    public void setProjectTaskID(String projectTaskID) {
        this.projectTaskID = projectTaskID;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Boolean getMarked() {
        return marked;
    }

    public void setMarked(Boolean marked) {
        this.marked = marked;
    }

    public Integer getSharedCount() {
        return sharedCount;
    }

    public void setSharedCount(Integer sharedCount) {
        this.sharedCount = sharedCount;
    }
}

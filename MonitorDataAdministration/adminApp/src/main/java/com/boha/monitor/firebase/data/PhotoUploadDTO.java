/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.boha.monitor.firebase.data;
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

        Date thisDate = new Date(this.dateTaken);
        Date anotherDate = new Date(another.dateTaken);

        if (thisDate.after(anotherDate)) {
            return -1;
        }
        if (thisDate.before(anotherDate)) {
            return 1;
        }
        return 0;
    }

    private static final long serialVersionUID = 1L;
    private String photoUploadID;
    private long dateTaken;
    private double latitude;
    private double longitude;
    private float accuracy;
    private String  projectName, taskName,  monitorName;
    private long dateUploaded;
    private String filePath;
    private String companyID;
    private String projectID;
    private String projectTaskID;
    private String userID, monitorID, staffID;

    private String url, bucketName;
    private boolean marked = false;
    private int sharedCount = 0;


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

    public long getDateTaken() {
        return dateTaken;
    }

    public void setDateTaken(long dateTaken) {
        this.dateTaken = dateTaken;
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

    public long getDateUploaded() {
        return dateUploaded;
    }

    public void setDateUploaded(long dateUploaded) {
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

    public boolean getMarked() {
        return marked;
    }

    public void setMarked(boolean marked) {
        this.marked = marked;
    }

    public int getSharedCount() {
        return sharedCount;
    }

    public void setSharedCount(int sharedCount) {
        this.sharedCount = sharedCount;
    }
}

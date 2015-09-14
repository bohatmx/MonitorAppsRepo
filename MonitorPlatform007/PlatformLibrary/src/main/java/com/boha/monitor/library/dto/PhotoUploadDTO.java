/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.boha.monitor.library.dto;
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

    public interface PhotoUploadedListener {
        void onPhotoUploaded();
        void onPhotoUploadFailed();
    }
    private Integer monitorID;
    private static final long serialVersionUID = 1L;
    private Integer photoUploadID;
    private int pictureType;
    private Long dateTaken;
    private Double latitude;
    private Double longitude;
    private Float accuracy;
    private String uri, projectName, taskName, staffName, monitorName;
    private Integer thumbFlag;
    private Long dateUploaded;
    private String thumbFilePath;
    private Integer staffPictureID;
    private Integer companyID;
    private Integer projectID;
    private Integer projectTaskID;
    private Integer staffID ;

    private String secureUrl;
    private String eTag;
    private String signature;
    private Integer width;
    private Integer height;
    private Integer bytes;
    private ProjectTaskStatusDTO projectTaskStatus;

    public PhotoUploadDTO() {
    }
    public static final int TASK_IMAGE = 2, PROJECT_IMAGE = 3, STAFF_IMAGE = 4, MONITOR_IMAGE = 5;
    private boolean isFullPicture, isStaffPicture;
    public PhotoUploadDTO(Integer photoUploadID) {
        this.photoUploadID = photoUploadID;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public ProjectTaskStatusDTO getProjectTaskStatus() {
        return projectTaskStatus;
    }

    public void setProjectTaskStatus(ProjectTaskStatusDTO projectTaskStatus) {
        this.projectTaskStatus = projectTaskStatus;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public String getStaffName() {
        return staffName;
    }

    public void setStaffName(String staffName) {
        this.staffName = staffName;
    }

    public String getMonitorName() {
        return monitorName;
    }

    public void setMonitorName(String monitorName) {
        this.monitorName = monitorName;
    }

    public String getSecureUrl() {
        return secureUrl;
    }

    public void setSecureUrl(String secureUrl) {
        this.secureUrl = secureUrl;
    }

    public String geteTag() {
        return eTag;
    }

    public void seteTag(String eTag) {
        this.eTag = eTag;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public Integer getWidth() {
        return width;
    }

    public void setWidth(Integer width) {
        this.width = width;
    }

    public Integer getHeight() {
        return height;
    }

    public void setHeight(Integer height) {
        this.height = height;
    }

    public Integer getBytes() {
        return bytes;
    }

    public void setBytes(Integer bytes) {
        this.bytes = bytes;
    }

    public boolean isIsFullPicture() {
        return isFullPicture;
    }

    public void setIsFullPicture(boolean isFullPicture) {
        this.isFullPicture = isFullPicture;
    }

    public boolean isIsStaffPicture() {
        return isStaffPicture;
    }

    public void setIsStaffPicture(boolean isStaffPicture) {
        this.isStaffPicture = isStaffPicture;
    }

    public Long getDateTaken() {
        return dateTaken;
    }

    public void setDateTaken(Long dateTaken) {
        this.dateTaken = dateTaken;
    }

    public Long getDateUploaded() {
        return dateUploaded;
    }

    public void setDateUploaded(Long dateUploaded) {
        this.dateUploaded = dateUploaded;
    }

    public Integer getStaffPictureID() {
        return staffPictureID;
    }

    public void setStaffPictureID(Integer staffPictureID) {
        this.staffPictureID = staffPictureID;
    }

    public Integer getCompanyID() {
        return companyID;
    }

    public void setCompanyID(Integer companyID) {
        this.companyID = companyID;
    }

    public Integer getProjectID() {
        return projectID;
    }

    public void setProjectID(Integer projectID) {
        this.projectID = projectID;
    }

    public Integer getProjectTaskID() {
        return projectTaskID;
    }

    public void setProjectTaskID(Integer projectTaskID) {
        this.projectTaskID = projectTaskID;
    }

    public Integer getStaffID() {
        return staffID;
    }

    public void setStaffID(Integer staffID) {
        this.staffID = staffID;
    }
    
    

    public Integer getPhotoUploadID() {
        return photoUploadID;
    }

    public void setPhotoUploadID(Integer photoUploadID) {
        this.photoUploadID = photoUploadID;
    }

   

    public int getPictureType() {
        return pictureType;
    }

    public void setPictureType(int pictureType) {
        this.pictureType = pictureType;
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

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public Integer getThumbFlag() {
        return thumbFlag;
    }

    public void setThumbFlag(Integer thumbFlag) {
        this.thumbFlag = thumbFlag;
    }

 

    public String getThumbFilePath() {
        return thumbFilePath;
    }

    public void setThumbFilePath(String thumbFilePath) {
        this.thumbFilePath = thumbFilePath;
    }

   
    @Override
    public int hashCode() {
        int hash = 0;
        hash += (photoUploadID != null ? photoUploadID.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof PhotoUploadDTO)) {
            return false;
        }
        PhotoUploadDTO other = (PhotoUploadDTO) object;
        if ((this.photoUploadID == null && other.photoUploadID != null) || (this.photoUploadID != null && !this.photoUploadID.equals(other.photoUploadID))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.boha.monitor.data.PhotoUpload[ photoUploadID=" + photoUploadID + " ]";
    }

    public Integer getMonitorID() {
        return monitorID;
    }

    public void setMonitorID(Integer monitorID) {
        this.monitorID = monitorID;
    }
}

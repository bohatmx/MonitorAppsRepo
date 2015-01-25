/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.com.boha.monitor.library.dto.transfer;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 *
 * @author aubreyM
 */
public class PhotoUploadDTO implements Serializable{

    public interface PhotoUploadedListener {
        public void onPhotoUploaded();
        public void onPhotoUploadFailed();
    }
    public static final int SITE_IMAGE = 1, TASK_IMAGE = 2, PROJECT_IMAGE = 3, STAFF_IMAGE = 4;
    private Integer index,companyID, projectID, projectSiteID,
            photoUploadID,
            projectSiteTaskID, pictureType, companyStaffID, thumbFlag;
    private List<String> tags;
    private Float accuracy;
    private String projectName, projectSiteName, projectSiteTask, uri;
    private Boolean isFullPicture,isStaffPicture, selected;
    private String imageFilePath;
    private Double latitude, longitude;
    private Long time;
    private String thumbFilePath;

    public Integer getPhotoUploadID() {
        return photoUploadID;
    }

    public void setPhotoUploadID(Integer photoUploadID) {
        this.photoUploadID = photoUploadID;
    }

    public Boolean getSelected() {
        return selected;
    }

    public Double getLatitude() {
        return latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public Long getTime() {
        return time;
    }

    public Boolean isSelected() {
        return selected;
    }

    public void setSelected(Boolean selected) {
        this.selected = selected;
    }

    public Boolean getIsFullPicture() {
        return isFullPicture;
    }

    public void setIsFullPicture(Boolean isFullPicture) {
        this.isFullPicture = isFullPicture;
    }

    public Boolean getIsStaffPicture() {
        return isStaffPicture;
    }

    public void setIsStaffPicture(Boolean isStaffPicture) {
        this.isStaffPicture = isStaffPicture;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public void setTime(Long time) {
        this.time = time;
    }

    public boolean isStaffPicture() {
        return isStaffPicture;
    }

    public void setStaffPicture(boolean isStaffPicture) {
        this.isStaffPicture = isStaffPicture;
    }

    public Float getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(Float accuracy) {
        this.accuracy = accuracy;
    }

    private Date dateThumbUploaded, dateFullPictureUploaded, dateTaken, dateUploaded;

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getProjectSiteName() {
        return projectSiteName;
    }

    public void setProjectSiteName(String projectSiteName) {
        this.projectSiteName = projectSiteName;
    }

    public String getProjectSiteTask() {
        return projectSiteTask;
    }

    public void setProjectSiteTask(String projectSiteTask) {
        this.projectSiteTask = projectSiteTask;
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

    public Date getDateUploaded() {
        return dateUploaded;
    }

    public void setDateUploaded(Date dateUploaded) {
        this.dateUploaded = dateUploaded;
    }


    public Integer getIndex() {
        return index;
    }

    public void setIndex(Integer index) {
        this.index = index;
    }

    public Integer getCompanyStaffID() {
        return companyStaffID;
    }

    public void setCompanyStaffID(Integer companyStaffID) {
        this.companyStaffID = companyStaffID;
    }

    public String getImageFilePath() {
        return imageFilePath;
    }

    public void setImageFilePath(String imageFilePath) {
        this.imageFilePath = imageFilePath;
    }

    public String getThumbFilePath() {
        return thumbFilePath;
    }

    public void setThumbFilePath(String thumbFilePath) {
        this.thumbFilePath = thumbFilePath;
    }

    public Date getDateThumbUploaded() {
        return dateThumbUploaded;
    }

    public void setDateThumbUploaded(Date dateThumbUploaded) {
        this.dateThumbUploaded = dateThumbUploaded;
    }

    public Date getDateFullPictureUploaded() {
        return dateFullPictureUploaded;
    }

    public void setDateFullPictureUploaded(Date dateFullPictureUploaded) {
        this.dateFullPictureUploaded = dateFullPictureUploaded;
    }

    public Date getDateTaken() {
        return dateTaken;
    }

    public void setDateTaken(Date dateTaken) {
        this.dateTaken = dateTaken;
    }

    public Integer getPictureType() {
        return pictureType;
    }

    public void setPictureType(Integer pictureType) {
        this.pictureType = pictureType;
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

    public Integer getProjectSiteID() {
        return projectSiteID;
    }

    public void setProjectSiteID(Integer projectSiteID) {
        this.projectSiteID = projectSiteID;
    }

    public Integer getProjectSiteTaskID() {
        return projectSiteTaskID;
    }

    public void setProjectSiteTaskID(Integer projectSiteTaskID) {
        this.projectSiteTaskID = projectSiteTaskID;
    }

    

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }
   

}

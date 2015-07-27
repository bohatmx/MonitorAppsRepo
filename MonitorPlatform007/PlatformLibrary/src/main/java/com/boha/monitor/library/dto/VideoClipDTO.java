package com.boha.monitor.library.dto;



import java.io.Serializable;

/**
 * Created by aubreyM on 2014/04/24.
 */
public class VideoClipDTO implements Serializable, Comparable<VideoClipDTO> {

    private long videoDate, length, dateUploaded;
    private String uriString,
            comment,
            filePath,
            youTubeID,
            projectName, taskNamw;
    private Integer projectTaskID, projectID, videoClipID;
    private int sortType;

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getTaskNamw() {
        return taskNamw;
    }

    public void setTaskNamw(String taskNamw) {
        this.taskNamw = taskNamw;
    }

    public Integer getProjectTaskID() {
        return projectTaskID;
    }

    public void setProjectTaskID(Integer projectTaskID) {
        this.projectTaskID = projectTaskID;
    }

    public Integer getProjectID() {
        return projectID;
    }

    public void setProjectID(Integer projectID) {
        this.projectID = projectID;
    }

    public void setVideoClipID(Integer videoClipID) {
        this.videoClipID = videoClipID;
    }

    public long getDateUploaded() {
        return dateUploaded;
    }

    public void setDateUploaded(long dateUploaded) {
        this.dateUploaded = dateUploaded;
    }

    public long getVideoDate() {
        return videoDate;
    }

    public void setVideoDate(long videoDate) {
        this.videoDate = videoDate;
    }

    public String getYouTubeID() {
        return youTubeID;
    }

    public void setYouTubeID(String youTubeID) {
        this.youTubeID = youTubeID;
    }

    public int getVideoClipID() {
        return videoClipID;
    }

    public void setVideoClipID(int videoClipID) {
        this.videoClipID = videoClipID;
    }


    public int getSortType() {
        return sortType;
    }

    public void setSortType(int sortType) {
        this.sortType = sortType;
    }

    public long getLength() {
        return length;
    }

    public void setLength(long length) {
        this.length = length;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }


    public String getUriString() {
        return uriString;
    }

    public void setUriString(String uriString) {
        this.uriString = uriString;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    @Override
    public int compareTo( VideoClipDTO v) {

        switch (sortType) {
            case 0:
                if (videoDate == v.videoDate) return 0;
                if (videoDate < v.videoDate) return 1;
                if (videoDate > v.videoDate) return -1;
                break;
            case SORT_BY_DATE_DESC:
                if (videoDate == v.videoDate) return 0;
                if (videoDate < v.videoDate) return 1;
                if (videoDate > v.videoDate) return -1;
                break;
            case SORT_BY_DATE_ASC:
                if (videoDate == v.videoDate) return 0;
                if (videoDate < v.videoDate) return -1;
                if (videoDate > v.videoDate) return 1;
                break;

        }
        return 0;
    }

    public static  final int SORT_BY_DATE_DESC = 1, SORT_BY_DATE_ASC = 2;


}

package com.boha.monitor.library.dto;

import java.io.Serializable;

/**
 * Created by aubreyM on 15/07/09.
 */
public class SubTaskDTO implements Serializable {
    private Integer subTaskID;
    private String subTaskName;
    private Integer subTaskNumber;
    private String description;
    private Integer taskID;

    public Integer getSubTaskID() {
        return subTaskID;
    }

    public void setSubTaskID(Integer subTaskID) {
        this.subTaskID = subTaskID;
    }

    public String getSubTaskName() {
        return subTaskName;
    }

    public void setSubTaskName(String subTaskName) {
        this.subTaskName = subTaskName;
    }

    public Integer getSubTaskNumber() {
        return subTaskNumber;
    }

    public void setSubTaskNumber(Integer subTaskNumber) {
        this.subTaskNumber = subTaskNumber;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getTaskID() {
        return taskID;
    }

    public void setTaskID(Integer taskID) {
        this.taskID = taskID;
    }
}

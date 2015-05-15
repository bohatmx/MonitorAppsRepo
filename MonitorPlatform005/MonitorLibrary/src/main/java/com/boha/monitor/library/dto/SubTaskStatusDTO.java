package com.boha.monitor.library.dto;

/**
 * Created by aubreyM on 14/12/03.
 */



import java.io.Serializable;
import java.util.Date;

/**
 *
 * @author aubreyM
 */
public class SubTaskStatusDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    private Integer subTaskStatusID;
    private Long statusDate;
    private Long dateUpdated;
    private TaskStatusDTO taskStatus;
    private Integer subTaskID,taskID, projectSiteTaskID;
    private Integer companyStaffID;
    private String staffName, subTaskName;

    public Integer getProjectSiteTaskID() {
        return projectSiteTaskID;
    }

    public void setProjectSiteTaskID(Integer projectSiteTaskID) {
        this.projectSiteTaskID = projectSiteTaskID;
    }

    public Integer getTaskID() {
        return taskID;
    }

    public void setTaskID(Integer taskID) {
        this.taskID = taskID;
    }

    public SubTaskStatusDTO() {
    }

    public String getSubTaskName() {
        return subTaskName;
    }

    public void setSubTaskName(String subTaskName) {
        this.subTaskName = subTaskName;
    }


    public Integer getSubTaskStatusID() {
        return subTaskStatusID;
    }

    public void setSubTaskStatusID(Integer subTaskStatusID) {
        this.subTaskStatusID = subTaskStatusID;
    }

    public Long getStatusDate() {
        return statusDate;
    }

    public void setStatusDate(Long statusDate) {
        this.statusDate = statusDate;
    }

    public Long getDateUpdated() {
        return dateUpdated;
    }

    public void setDateUpdated(Long dateUpdated) {
        this.dateUpdated = dateUpdated;
    }

    public TaskStatusDTO getTaskStatus() {
        return taskStatus;
    }

    public void setTaskStatus(TaskStatusDTO taskStatus) {
        this.taskStatus = taskStatus;
    }

    public Integer getSubTaskID() {
        return subTaskID;
    }

    public void setSubTaskID(Integer subTaskID) {
        this.subTaskID = subTaskID;
    }

    public Integer getCompanyStaffID() {
        return companyStaffID;
    }

    public void setCompanyStaffID(Integer companyStaffID) {
        this.companyStaffID = companyStaffID;
    }

    public String getStaffName() {
        return staffName;
    }

    public void setStaffName(String staffName) {
        this.staffName = staffName;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (subTaskStatusID != null ? subTaskStatusID.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof SubTaskStatusDTO)) {
            return false;
        }
        SubTaskStatusDTO other = (SubTaskStatusDTO) object;
        if ((this.subTaskStatusID == null && other.subTaskStatusID != null) || (this.subTaskStatusID != null && !this.subTaskStatusID.equals(other.subTaskStatusID))) {
            return false;
        }
        return true;
    }


    @Override
    public String toString() {
        return "com.boha.monitor.data.SubTaskStatus[ subTaskStatusID=" + subTaskStatusID + " ]";
    }

}

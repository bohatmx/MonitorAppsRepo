package com.boha.monitor.library.dto;

import java.io.Serializable;

/**
 * Created by aubreyM on 15/11/23.
 */
public class SimpleMessageDestinationDTO implements Serializable {
    private Integer simpleMessageDestinationID;
    private Integer monitorID;
    private Integer simpleMessageID;
    private Integer staffID;

    public Integer getSimpleMessageDestinationID() {
        return simpleMessageDestinationID;
    }

    public void setSimpleMessageDestinationID(Integer simpleMessageDestinationID) {
        this.simpleMessageDestinationID = simpleMessageDestinationID;
    }

    public Integer getMonitorID() {
        return monitorID;
    }

    public void setMonitorID(Integer monitorID) {
        this.monitorID = monitorID;
    }

    public Integer getSimpleMessageID() {
        return simpleMessageID;
    }

    public void setSimpleMessageID(Integer simpleMessageID) {
        this.simpleMessageID = simpleMessageID;
    }

    public Integer getStaffID() {
        return staffID;
    }

    public void setStaffID(Integer staffID) {
        this.staffID = staffID;
    }
}

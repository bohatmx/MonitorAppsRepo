/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.boha.monitor.dto;

import java.io.Serializable;
import java.util.List;

/**
 * Created by aubreyM on 14/12/13.
 */
public class RequestList implements Serializable {
    private List<RequestDTO> requests;
    private boolean rideWebSocket;

    public boolean isRideWebSocket() {
        return rideWebSocket;
    }

    public void setRideWebSocket(boolean rideWebSocket) {
        this.rideWebSocket = rideWebSocket;
    }

    public List<RequestDTO> getRequests() {
        return requests;
    }

    public void setRequests(List<RequestDTO> requests) {
        this.requests = requests;
    }
}

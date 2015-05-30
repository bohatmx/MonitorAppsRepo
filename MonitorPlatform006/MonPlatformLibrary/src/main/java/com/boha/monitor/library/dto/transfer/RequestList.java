package com.boha.monitor.library.dto.transfer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by aubreyM on 14/12/13.
 */
public class RequestList implements Serializable {
    private boolean rideWebSocket = true;
    private List<RequestDTO> requests = new ArrayList<>();

    public List<RequestDTO> getRequests() {
        return requests;
    }

    public void setRequests(List<RequestDTO> requests) {
        this.requests = requests;
    }

    public boolean isRideWebSocket() {
        return rideWebSocket;
    }

    public void setRideWebSocket(boolean rideWebSocket) {
        this.rideWebSocket = rideWebSocket;
    }
}

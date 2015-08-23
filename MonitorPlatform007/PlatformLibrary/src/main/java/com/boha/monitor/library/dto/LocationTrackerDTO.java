/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.boha.monitor.library.dto;

import java.io.Serializable;

/**
 *
 * @author aubreyM
 */
public class LocationTrackerDTO implements Serializable {

    private static final long serialVersionUID = 1L;
    private Integer locationTrackerID;
    private int staffID, monitorID;
    private Long dateTracked;
    private double latitude;
    private double longitude;
    private float accuracy;
    private String geocodedAddress, staffName;
    private Long dateAdded;
    private GcmDeviceDTO gcmDevice;

    public LocationTrackerDTO() {
    }

    public GcmDeviceDTO getGcmDevice() {
        return gcmDevice;
    }

    public void setGcmDevice(GcmDeviceDTO gcmDevice) {
        this.gcmDevice = gcmDevice;
    }

    public Integer getLocationTrackerID() {
        return locationTrackerID;
    }

    public void setLocationTrackerID(Integer locationTrackerID) {
        this.locationTrackerID = locationTrackerID;
    }

    public int getStaffID() {
        return staffID;
    }

    public void setStaffID(int staffID) {
        this.staffID = staffID;
    }

    public int getMonitorID() {
        return monitorID;
    }

    public void setMonitorID(int monitorID) {
        this.monitorID = monitorID;
    }

    public Long getDateTracked() {
        return dateTracked;
    }

    public void setDateTracked(Long dateTracked) {
        this.dateTracked = dateTracked;
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

    public float getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(float accuracy) {
        this.accuracy = accuracy;
    }

    public String getGeocodedAddress() {
        return geocodedAddress;
    }

    public void setGeocodedAddress(String geocodedAddress) {
        this.geocodedAddress = geocodedAddress;
    }

    public String getStaffName() {
        return staffName;
    }

    public void setStaffName(String staffName) {
        this.staffName = staffName;
    }

    public Long getDateAdded() {
        return dateAdded;
    }

    public void setDateAdded(Long dateAdded) {
        this.dateAdded = dateAdded;
    }
    
}

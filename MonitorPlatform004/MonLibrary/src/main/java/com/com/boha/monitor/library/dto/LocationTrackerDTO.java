package com.com.boha.monitor.library.dto;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by aubreyM on 15/03/15.
 */
public class LocationTrackerDTO implements Serializable{
    private static final long serialVersionUID = 1L;
    private Integer locationTrackerID;
    private int companyStaffID;
    private Date dateTracked;
    private double latitude;
    private double longitude;
    private float accuracy;

    private String geocodedAddress,staffName;
    private Date dateAdded;

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

    public Date getDateAdded() {
        return dateAdded;
    }

    public void setDateAdded(Date dateAdded) {
        this.dateAdded = dateAdded;
    }

    public Integer getLocationTrackerID() {
        return locationTrackerID;
    }

    public void setLocationTrackerID(Integer locationTrackerID) {
        this.locationTrackerID = locationTrackerID;
    }

    public int getCompanyStaffID() {
        return companyStaffID;
    }

    public void setCompanyStaffID(int companyStaffID) {
        this.companyStaffID = companyStaffID;
    }

    public Date getDateTracked() {
        return dateTracked;
    }

    public void setDateTracked(Date dateTracked) {
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
}

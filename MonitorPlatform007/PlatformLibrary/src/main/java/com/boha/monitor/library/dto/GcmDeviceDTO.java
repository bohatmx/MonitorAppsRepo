/*
 * To change this license header, choose License Headers in ProjectDTO Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.boha.monitor.library.dto;


import java.io.Serializable;
import java.util.List;

/**
 *
 * @author aubreyM
 */
public class GcmDeviceDTO implements Serializable{

    private Integer gcmDeviceID;
    private String registrationID;
    private String manufacturer;
    private String model, app;
    private String product;
    private Integer messageCount;
    private long dateRegistered;
    private String serialNumber, androidVersion;
    private Integer companyID;
    private StaffDTO staff;
    private MonitorDTO monitor;
    private List<LocationTrackerDTO> trackerList;

    public List<LocationTrackerDTO> getTrackerList() {
        return trackerList;
    }

    public void setTrackerList(List<LocationTrackerDTO> trackerList) {
        this.trackerList = trackerList;
    }

    public String getApp() {
        return app;
    }

    public void setApp(String app) {
        this.app = app;
    }

    public Integer getGcmDeviceID() {
        return gcmDeviceID;
    }

    public void setGcmDeviceID(Integer gcmDeviceID) {
        this.gcmDeviceID = gcmDeviceID;
    }

    public String getRegistrationID() {
        return registrationID;
    }

    public void setRegistrationID(String registrationID) {
        this.registrationID = registrationID;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getProduct() {
        return product;
    }

    public void setProduct(String product) {
        this.product = product;
    }

    public Integer getMessageCount() {
        return messageCount;
    }

    public void setMessageCount(Integer messageCount) {
        this.messageCount = messageCount;
    }

    public long getDateRegistered() {
        return dateRegistered;
    }

    public void setDateRegistered(long dateRegistered) {
        this.dateRegistered = dateRegistered;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public String getAndroidVersion() {
        return androidVersion;
    }

    public void setAndroidVersion(String androidVersion) {
        this.androidVersion = androidVersion;
    }

    public Integer getCompanyID() {
        return companyID;
    }

    public void setCompanyID(Integer companyID) {
        this.companyID = companyID;
    }

    public StaffDTO getStaff() {
        return staff;
    }

    public void setStaff(StaffDTO staff) {
        this.staff = staff;
    }

    public MonitorDTO getMonitor() {
        return monitor;
    }

    public void setMonitor(MonitorDTO monitor) {
        this.monitor = monitor;
    }
}

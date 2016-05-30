package com.boha.monitor.firebase.dto;

import java.io.Serializable;

/**
 * Created by aubreyM on 15/07/25.
 */
public class CityDTO implements Serializable {
    private String municipalityID, provinceID;
    private static final long serialVersionUID = 1L;
    private String cityID;
    private String cityName, municipalityName;
    private double latitude;
    private double longitude;

    public String getProvinceID() {
        return provinceID;
    }

    public void setProvinceID(String provinceID) {
        this.provinceID = provinceID;
    }

    public String getMunicipalityName() {
        return municipalityName;
    }

    public void setMunicipalityName(String municipalityName) {
        this.municipalityName = municipalityName;
    }

    public String getMunicipalityID() {
        return municipalityID;
    }

    public void setMunicipalityID(String municipalityID) {
        this.municipalityID = municipalityID;
    }

    public String getCityID() {
        return cityID;
    }

    public void setCityID(String cityID) {
        this.cityID = cityID;
    }

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
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
}

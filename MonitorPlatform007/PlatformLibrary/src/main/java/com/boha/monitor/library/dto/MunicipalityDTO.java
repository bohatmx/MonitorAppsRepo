package com.boha.monitor.library.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by aubreyM on 15/07/25.
 */
public class MunicipalityDTO implements Serializable {

    private static final long serialVersionUID = 1L;
    private List<CityDTO> cityList;
    private Integer municipalityID;
    private String municipalityName;
    private Double latitude;
    private Double longitude;
    private Integer provinceID;

    public List<CityDTO> getCityList() {
        if (cityList == null) {
            cityList = new ArrayList<>();
        }
        return cityList;
    }

    public void setCityList(List<CityDTO> cityList) {
        this.cityList = cityList;
    }

    public Integer getMunicipalityID() {
        return municipalityID;
    }

    public void setMunicipalityID(Integer municipalityID) {
        this.municipalityID = municipalityID;
    }

    public String getMunicipalityName() {
        return municipalityName;
    }

    public void setMunicipalityName(String municipalityName) {
        this.municipalityName = municipalityName;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Integer getProvinceID() {
        return provinceID;
    }

    public void setProvinceID(Integer provinceID) {
        this.provinceID = provinceID;
    }
}

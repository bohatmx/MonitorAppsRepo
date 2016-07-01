package com.boha.monitor.firebase.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by aubreyM on 15/07/25.
 */
public class MunicipalityDTO implements Serializable {

    public MunicipalityDTO() {}
    private static final long serialVersionUID = 1L;
    private List<CityDTO> cityList;
    private HashMap<String, CityDTO> cities;
    private String municipalityID;
    private String municipalityName, provinceName;
    private double latitude;
    private double longitude;
    private String provinceID;

    public List<CityDTO> getCityList() {
        if (cityList == null) {
            cityList = new ArrayList<>();
        }
        return cityList;
    }

    public String getProvinceName() {
        return provinceName;
    }

    public void setProvinceName(String provinceName) {
        this.provinceName = provinceName;
    }

    public HashMap<String, CityDTO> getCities() {
        return cities;
    }

    public void setCities(HashMap<String, CityDTO> cities) {
        this.cities = cities;
    }

    public void setCityList(List<CityDTO> cityList) {
        this.cityList = cityList;
    }

    public String getMunicipalityID() {
        return municipalityID;
    }

    public void setMunicipalityID(String municipalityID) {
        this.municipalityID = municipalityID;
    }

    public String getMunicipalityName() {
        return municipalityName;
    }

    public void setMunicipalityName(String municipalityName) {
        this.municipalityName = municipalityName;
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

    public String getProvinceID() {
        return provinceID;
    }

    public void setProvinceID(String provinceID) {
        this.provinceID = provinceID;
    }
}

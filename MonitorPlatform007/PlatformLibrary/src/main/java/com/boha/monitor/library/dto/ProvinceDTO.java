package com.boha.monitor.library.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by aubreyM on 15/07/25.
 */
public class ProvinceDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    private Integer provinceID;
    private String provinceName;
    private List<MunicipalityDTO> municipalityList;
    private Integer countryID;

    public Integer getProvinceID() {
        return provinceID;
    }

    public void setProvinceID(Integer provinceID) {
        this.provinceID = provinceID;
    }

    public String getProvinceName() {
        return provinceName;
    }

    public void setProvinceName(String provinceName) {
        this.provinceName = provinceName;
    }

    public List<MunicipalityDTO> getMunicipalityList() {
        if (municipalityList == null) {
            municipalityList = new ArrayList<>();
        }
        return municipalityList;
    }

    public void setMunicipalityList(List<MunicipalityDTO> municipalityList) {
        this.municipalityList = municipalityList;
    }

    public Integer getCountryID() {
        return countryID;
    }

    public void setCountryID(Integer countryID) {
        this.countryID = countryID;
    }
}

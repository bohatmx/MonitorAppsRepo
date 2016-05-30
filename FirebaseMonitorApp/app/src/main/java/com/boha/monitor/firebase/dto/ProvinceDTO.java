package com.boha.monitor.firebase.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by aubreyM on 15/07/25.
 */
public class ProvinceDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    private String provinceID;
    private String provinceName;
    private HashMap<String, MunicipalityDTO> municipalities;
    private List<MunicipalityDTO> municipalityList;
    private Integer countryID;

    public HashMap<String, MunicipalityDTO> getMunicipalities() {
        return municipalities;
    }

    public void setMunicipalities(HashMap<String, MunicipalityDTO> municipalities) {
        this.municipalities = municipalities;
    }

    public String getProvinceID() {
        return provinceID;
    }

    public void setProvinceID(String provinceID) {
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

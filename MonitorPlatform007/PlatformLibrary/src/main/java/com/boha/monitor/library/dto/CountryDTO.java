package com.boha.monitor.library.dto;

import java.io.Serializable;
import java.util.List;

/**
 * Created by aubreyM on 15/07/25.
 */
public class CountryDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    private Integer countryID;
    private String countryName;
    private List<ProvinceDTO> provinceList;

    public Integer getCountryID() {
        return countryID;
    }

    public void setCountryID(Integer countryID) {
        this.countryID = countryID;
    }

    public String getCountryName() {
        return countryName;
    }

    public void setCountryName(String countryName) {
        this.countryName = countryName;
    }

    public List<ProvinceDTO> getProvinceList() {
        return provinceList;
    }

    public void setProvinceList(List<ProvinceDTO> provinceList) {
        this.provinceList = provinceList;
    }
}

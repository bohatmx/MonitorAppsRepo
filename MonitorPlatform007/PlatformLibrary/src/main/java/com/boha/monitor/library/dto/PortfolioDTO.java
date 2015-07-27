/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.boha.monitor.library.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author aubreyM
 */
public class PortfolioDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    private Integer portfolioID;
    private String portfolioName;
    private Long dateRegistered;
    private Integer companyID;
    private List<ProgrammeDTO> programmeList;

    public PortfolioDTO() {
    }

    public PortfolioDTO(Integer portfolioID) {
        this.portfolioID = portfolioID;
    }

    public List<ProgrammeDTO> getProgrammeList() {
        if (programmeList == null) {
            programmeList = new ArrayList<>();
        }
        return programmeList;
    }

    public void setProgrammeList(List<ProgrammeDTO> programmeList) {
        this.programmeList = programmeList;
    }

    public Integer getPortfolioID() {
        return portfolioID;
    }

    public void setPortfolioID(Integer portfolioID) {
        this.portfolioID = portfolioID;
    }

    public String getPortfolioName() {
        return portfolioName;
    }

    public void setPortfolioName(String portfolioName) {
        this.portfolioName = portfolioName;
    }

    public Long getDateRegistered() {
        return dateRegistered;
    }

    public void setDateRegistered(Long dateRegistered) {
        this.dateRegistered = dateRegistered;
    }

    public Integer getCompanyID() {
        return companyID;
    }

    public void setCompanyID(Integer companyID) {
        this.companyID = companyID;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (portfolioID != null ? portfolioID.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof PortfolioDTO)) {
            return false;
        }
        PortfolioDTO other = (PortfolioDTO) object;
        if ((this.portfolioID == null && other.portfolioID != null) || (this.portfolioID != null && !this.portfolioID.equals(other.portfolioID))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.boha.monitor.data.Portfolio[ portfolioID=" + portfolioID + " ]";
    }
    
}

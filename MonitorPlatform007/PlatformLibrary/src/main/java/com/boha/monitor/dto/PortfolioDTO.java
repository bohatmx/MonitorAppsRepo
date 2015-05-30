/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.boha.monitor.dto;

import java.io.Serializable;
import java.util.Date;

/**
 *
 * @author aubreyM
 */
public class PortfolioDTO implements Serializable {
    private static final long serialVersionUID = 1L;
  private Integer portfolioID;
    private String portfolioName;
    private Long dateRegistered;
    private CompanyDTO company;

    public PortfolioDTO() {
    }

    public PortfolioDTO(Integer portfolioID) {
        this.portfolioID = portfolioID;
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


    public CompanyDTO getCompany() {
        return company;
    }

    public void setCompany(CompanyDTO company) {
        this.company = company;
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

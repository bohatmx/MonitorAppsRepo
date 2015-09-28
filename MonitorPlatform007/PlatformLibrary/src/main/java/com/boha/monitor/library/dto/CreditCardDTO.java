package com.boha.monitor.library.dto;

import java.io.Serializable;

/**
 * Created by aubreyM on 15/09/26.
 */
public class CreditCardDTO implements Serializable{
    String cardHolder, cardNumber, email, mobileNumber;
    int expiryYear, expiryMonth;
    int budgetPeriod;
    int profileInfoID;

    public String getCardHolder() {
        return cardHolder;
    }

    public void setCardHolder(String cardHolder) {
        this.cardHolder = cardHolder;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }

    public int getExpiryYear() {
        return expiryYear;
    }

    public void setExpiryYear(int expiryYear) {
        this.expiryYear = expiryYear;
    }

    public int getExpiryMonth() {
        return expiryMonth;
    }

    public void setExpiryMonth(int expiryMonth) {
        this.expiryMonth = expiryMonth;
    }

    public int getBudgetPeriod() {
        return budgetPeriod;
    }

    public void setBudgetPeriod(int budgetPeriod) {
        this.budgetPeriod = budgetPeriod;
    }

    public int getProfileInfoID() {
        return profileInfoID;
    }

    public void setProfileInfoID(int profileInfoID) {
        this.profileInfoID = profileInfoID;
    }
}

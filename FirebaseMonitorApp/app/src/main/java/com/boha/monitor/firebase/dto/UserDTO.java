package com.boha.monitor.firebase.dto;

import java.io.Serializable;
import java.util.HashMap;

/**
 * Created by aubreymalabie on 5/21/16.
 */

public class UserDTO implements Serializable {
    public UserDTO() {}


    private String firstName = "", lastName = "", email = "", userID = "", companyID = "", password;
    private HashMap<String, Object> photos;
    private String uri;

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }

    public HashMap<String, Object> getPhotos() {
        return photos;
    }

    public void setPhotos(HashMap<String, Object> photos) {
        this.photos = photos;
    }

    public String getCompanyID() {
        return companyID;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setCompanyID(String companyID) {
        this.companyID = companyID;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }


}

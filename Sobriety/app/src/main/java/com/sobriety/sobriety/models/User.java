package com.sobriety.sobriety.models;

import java.io.Serializable;

/**
 * Created by andre on 8/14/2018.
 */

public class User{
    int idx = 1;
    String name = "";
    String username = "";
    String gender = "";
    String phoneNumber = "";
    String photoUrl = "";
    String cleanDate = "";

    public User(){}

    public void setGender(String gender) {
        this.gender = gender;
    }

    public void setCleanDate(String cleanDate) {
        this.cleanDate = cleanDate;
    }

    public String getGender() {
        return gender;
    }

    public String getCleanDate() {
        return cleanDate;
    }

    public void setIdx(int idx) {
        this.idx = idx;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public int getIdx() {
        return idx;
    }

    public String getName() {
        return name;
    }

    public String getUsername() {
        return username;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }
}

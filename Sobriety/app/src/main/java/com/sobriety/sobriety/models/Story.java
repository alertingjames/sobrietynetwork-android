package com.sobriety.sobriety.models;

import java.io.Serializable;

/**
 * Created by LGH419 on 8/17/2018.
 */

public class Story{
    int idx = 1;
    String title = "";
    int user_id = 1;
    String userName = "";
    String dateTime = "";
    String type = "";
    String url = "";
    String thumbnail = "";

    public Story(){}

    public void setUrl(String url) {
        this.url = url;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public String getUrl() {
        return url;
    }

    public void setIdx(int idx) {
        this.idx = idx;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getIdx() {
        return idx;
    }

    public String getTitle() {
        return title;
    }

    public int getUser_id() {
        return user_id;
    }

    public String getUserName() {
        return userName;
    }

    public String getDateTime() {
        return dateTime;
    }

    public String getType() {
        return type;
    }
}

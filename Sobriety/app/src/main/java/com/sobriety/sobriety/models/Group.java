package com.sobriety.sobriety.models;

import java.util.ArrayList;

/**
 * Created by andre on 8/14/2018.
 */

public class Group {
    int idx = 1;
    int user_id = 1;
    String name = "";
    String code = "";
    String dateTime = "";
    ArrayList<User> users = new ArrayList<>();

    public Group(){}

    public void setIdx(int idx) {
        this.idx = idx;
    }

    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    public void setUsers(ArrayList<User> users) {
        this.users.clear();
        this.users.addAll(users);
    }

    public int getIdx() {
        return idx;
    }

    public int getUser_id() {
        return user_id;
    }

    public String getName() {
        return name;
    }

    public String getCode() {
        return code;
    }

    public String getDateTime() {
        return dateTime;
    }

    public ArrayList<User> getUsers() {
        return users;
    }
}

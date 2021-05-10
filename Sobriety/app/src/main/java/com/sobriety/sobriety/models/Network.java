package com.sobriety.sobriety.models;

import java.util.ArrayList;

/**
 * Created by andre on 8/14/2018.
 */

public class Network {

    int idx = 1;
    int user_id = 1;
    String name = "";
    String photoUrl = "";
    ArrayList<User> users = new ArrayList<>();

    public Network(){}

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setIdx(int idx) {
        this.idx = idx;
    }

    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setUsers(ArrayList<User> users) {
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

    public ArrayList<User> getUsers() {
        return users;
    }
}

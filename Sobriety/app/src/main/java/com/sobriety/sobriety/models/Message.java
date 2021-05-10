package com.sobriety.sobriety.models;

import com.firebase.client.Firebase;

import java.io.Serializable;

/**
 * Created by LGH419 on 8/26/2018.
 */

public class Message{
    int idx = 1;
    String message = "";
    String group_name = "";
    int network_id = 1;
    String call_code = "";
    String option = "";
    int sender_id = 1;
    String sender_phone = "";
    String sender_name = "";
    String sender_photo = "";
    String date_time = "";
    String key = "";
    Firebase firebase = null;

    public Message(){}

    public void setOption(String option) {
        this.option = option;
    }

    public String getOption() {
        return option;
    }

    public void setNetwork_id(int network_id) {
        this.network_id = network_id;
    }

    public int getNetwork_id() {
        return network_id;
    }

    public void setCall_code(String call_code) {
        this.call_code = call_code;
    }

    public String getCall_code() {
        return call_code;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }

    public void setFirebase(Firebase firebase) {
        this.firebase = firebase;
    }

    public Firebase getFirebase() {
        return firebase;
    }

    public void setSender_id(int sender_id) {
        this.sender_id = sender_id;
    }

    public int getSender_id() {
        return sender_id;
    }

    public void setIdx(int idx) {
        this.idx = idx;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setGroup_name(String group_name) {
        this.group_name = group_name;
    }

    public void setSender_phone(String sender_phone) {
        this.sender_phone = sender_phone;
    }

    public void setSender_name(String sender_name) {
        this.sender_name = sender_name;
    }

    public void setSender_photo(String sender_photo) {
        this.sender_photo = sender_photo;
    }

    public void setDate_time(String date_time) {
        this.date_time = date_time;
    }

    public int getIdx() {
        return idx;
    }

    public String getMessage() {
        return message;
    }

    public String getGroup_name() {
        return group_name;
    }

    public String getSender_phone() {
        return sender_phone;
    }

    public String getSender_name() {
        return sender_name;
    }

    public String getSender_photo() {
        return sender_photo;
    }

    public String getDate_time() {
        return date_time;
    }
}

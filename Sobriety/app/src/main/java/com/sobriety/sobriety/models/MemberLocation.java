package com.sobriety.sobriety.models;

/**
 * Created by LGH419 on 8/28/2018.
 */

public class MemberLocation {
    int idx = 1;
    int member_id = 1;
    double lat = 0.0d;
    double lng = 0.0d;

    public MemberLocation(){}

    public void setIdx(int idx) {
        this.idx = idx;
    }

    public void setMember_id(int member_id) {
        this.member_id = member_id;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public int getIdx() {
        return idx;
    }

    public int getMember_id() {
        return member_id;
    }

    public double getLat() {
        return lat;
    }

    public double getLng() {
        return lng;
    }
}

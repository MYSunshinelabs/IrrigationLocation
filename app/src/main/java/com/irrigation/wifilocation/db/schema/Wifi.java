package com.irrigation.wifilocation.db.schema;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

@Entity
public class Wifi {
    private String ssid,password,latitude,longtitude;
    private String tag;
    @PrimaryKey(autoGenerate = true)
    private int id;

    public Wifi(String ssid, String password, String latitude, String longtitude) {
        this.ssid = ssid;
        this.password = password;
        this.latitude = latitude;
        this.longtitude = longtitude;
    }

    public String getSsid() {
        return ssid;
    }

    public void setSsid(String ssid) {
        this.ssid = ssid;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongtitude() {
        return longtitude;
    }

    public void setLongtitude(String longtitude) {
        this.longtitude = longtitude;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }
}

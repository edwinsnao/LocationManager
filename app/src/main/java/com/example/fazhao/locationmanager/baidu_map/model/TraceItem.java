package com.example.fazhao.locationmanager.baidu_map.model;

/**
 * Created by fazhao on 2017/3/6.
 */

import android.os.Bundle;

public class TraceItem {
    private int id;
    private int tag;
    private int step;

    public int getStep() {
        return step;
    }

    public void setStep(int step) {
        this.step = step;
    }

    public int getTag() {
        return tag;
    }

    public void setTag(int tag) {
        this.tag = tag;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }


    private String name;

    public String address;

    private String date;
    private String provider;
    private double latitude;
    private double longitude;
    private  double accuracy;


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }


    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }


    @Override
    public String toString()
    {
        return "TraceItem [id=" + id + ", name=" + name + ", address=" + address +  ", tag=" + tag + ", date=" + date + ", latitude=" + latitude
                + ", longitude=" + longitude + ", provider=" + provider +   ", accuracy=" + accuracy+"]";
    }

}

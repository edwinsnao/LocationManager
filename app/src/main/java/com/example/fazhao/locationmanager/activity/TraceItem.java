package com.example.fazhao.locationmanager.activity;

import android.os.Bundle;

import com.tencent.map.geolocation.TencentLocation;
import com.tencent.map.geolocation.TencentPoi;

import java.util.List;

public class TraceItem implements TencentLocation {
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

    private int level;

    private String name;
   
    public String address;
   
    private String date;
    private String provider;
    private double latitude;
    private double longitude;
    private  double accuracy;


    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    @Override
    public String getNation() {
        return null;
    }

    @Override
    public String getProvince() {
        return null;
    }

    @Override
    public String getCity() {
        return null;
    }

    @Override
    public String getDistrict() {
        return null;
    }

    @Override
    public String getTown() {
        return null;
    }

    @Override
    public String getVillage() {
        return null;
    }

    @Override
    public String getStreet() {
        return null;
    }

    @Override
    public String getStreetNo() {
        return null;
    }

    @Override
    public Integer getAreaStat() {
        return null;
    }

    @Override
    public List<TencentPoi> getPoiList() {
        return null;
    }

    @Override
    public float getBearing() {
        return 0;
    }

    @Override
    public float getSpeed() {
        return 0;
    }

    @Override
    public long getTime() {
        return 0;
    }

    @Override
    public long getElapsedRealtime() {
        return 0;
    }

    @Override
    public int getRssi() {
        return 0;
    }

    @Override
    public double getDirection() {
        return 0;
    }

    @Override
    public String getCityCode() {
        return null;
    }

    @Override
    public int getCoordinateType() {
        return 0;
    }

    @Override
    public int isMockGps() {
        return 0;
    }

    @Override
    public Bundle getExtra() {
        return null;
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

    @Override
    public double getAltitude() {
        return 0;
    }

    @Override
    public float getAccuracy() {
        return 0;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }


    public void setAccuracy(double accuracy) {
        this.accuracy = accuracy;
    }

    @Override
    public String toString()
    {
        return "TraceItem [id=" + id + ", name=" + name + ", address=" + address +  ", tag=" + tag + ", date=" + date + ", latitude=" + latitude
                + ", longitude=" + longitude + ", provider=" + provider +   ", accuracy=" + accuracy+"]";
    }

}

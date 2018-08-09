package com.kamruzzaman.trackme;

import java.util.ArrayList;

public class User {
    String email,userId,name;
    double longitude, latitude;
    ArrayList<User> buddyList, request;

    public User(){

    }

    public User(String userId,String email, String name,double longitude, double latitude) {
        this.userId = userId;
        this.email = email;
        this.longitude = longitude;
        this.latitude = latitude;
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public ArrayList<User> getBuddyList() {
        return buddyList;
    }

    public void setBuddyList(ArrayList<User> buddyList) {
        this.buddyList = buddyList;
    }

    public ArrayList<User> getRequest() {
        return request;
    }

    public void setRequest(ArrayList<User> request) {
        this.request = request;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUserLogo()
    {
        return this.name.substring(0,1);
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    @Override
    public String toString() {
        return "User{" +
                "email='" + email + '\'' +
                ", userId='" + userId + '\'' +
                ", longitude=" + longitude +
                ", latitude=" + latitude +
                ", buddyList=" + buddyList +
                ", request=" + request +
                '}';
    }
}

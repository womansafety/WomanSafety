package com.ws.womansafety.model;


public class UserLatLng {
    public String lat;
    public String lng;

    public UserLatLng(String lat, String lng) {
        this.lat = lat;
        this.lng = lng;
    }

    public UserLatLng() {
    }

    public String getLat() {
        return lat;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public String getLng() {
        return lng;
    }

    public void setLng(String lng) {
        this.lng = lng;
    }
}

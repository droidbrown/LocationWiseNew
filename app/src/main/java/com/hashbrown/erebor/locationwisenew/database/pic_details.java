package com.hashbrown.erebor.locationwisenew.database;

/**
 * Created by Erebor on 19/05/17.
 */

public class pic_details
{
    Double longitude;
    Double latitude;
    String address;
    String date;
    String time;
    String path;
    String co_ordinate_one;
    String co_ordinate_two;
    public pic_details(Double longitude, Double latitude, String address, String date, String time, String path, String co_ordinate_one, String co_ordinate_two) {
        this.longitude = longitude;
        this.latitude = latitude;
        this.address = address;
        this.date = date;
        this.time = time;
        this.path = path;
        this.co_ordinate_one = co_ordinate_one;
        this.co_ordinate_two = co_ordinate_two;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
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

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getCo_ordinate_one() {
        return co_ordinate_one;
    }

    public void setCo_ordinate_one(String co_ordinate_one) {
        this.co_ordinate_one = co_ordinate_one;
    }

    public String getCo_ordinate_two() {
        return co_ordinate_two;
    }

    public void setCo_ordinate_two(String co_ordinate_two) {
        this.co_ordinate_two = co_ordinate_two;
    }
}

package com.saikrishna.succour;

/**
 * Created by saikrishna on 26/02/18.
 */

public class Succour {
    String image;
    int number;
    int zero;
    int first;
    int second;
    String description;
    String location;
    String latitude;
    String longitude;

    public Succour() {

    }

    public Succour(String image, int number, int zero, int first, int second, String description, String location, String latitude, String longitude) {
        this.image = image;
        this.number = number;
        this.zero = zero;
        this.first = first;
        this.second = second;
        this.description = description;
        this.location = location;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getImage() {
        return image;
    }

    public int getZero() {
        return zero;
    }

    public int getNumber() {
        return number;
    }

    public int getFirst() {
        return first;
    }

    public int getSecond() {
        return second;
    }

    public String getDescription() {
        return description;
    }

    public String getLocation() {
        return location;
    }

    public String getLatitude() {
        return latitude;
    }

    public String getLongitude() {
        return longitude;
    }
}

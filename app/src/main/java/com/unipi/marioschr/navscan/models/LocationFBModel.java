package com.unipi.marioschr.navscan.models;

import com.google.firebase.firestore.GeoPoint;

public class LocationFBModel {
    private GeoPoint coords;
    private int points;
    private String name;
    private String location;
    private String description;
    public LocationFBModel() { }

    public GeoPoint getCoords() {
        return coords;
    }

    public void setCoords(GeoPoint coords) {
        this.coords = coords;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

}

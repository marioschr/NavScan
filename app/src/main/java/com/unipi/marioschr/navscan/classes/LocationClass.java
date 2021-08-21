package com.unipi.marioschr.navscan.classes;

import com.google.firebase.firestore.GeoPoint;

public class LocationClass {
    private GeoPoint coords;
    private int points;
    private String name;

    public LocationClass() { }

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
}

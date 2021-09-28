package com.unipi.marioschr.navscan.models;

public class StoreItemFBModel {
    private int cost;
    private String name, description;

    public StoreItemFBModel() {}

    public int getCost() { return cost; }

    public void setCost(int cost) { this.cost = cost; }

    public String getName() { return name; }

    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }

    public void setDescription(String description) { this.description = description; }
}

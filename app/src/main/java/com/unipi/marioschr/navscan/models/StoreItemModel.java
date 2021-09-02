package com.unipi.marioschr.navscan.models;

public class StoreItemModel {
    private int cost;
    private String name, description;

    public StoreItemModel() {}

    public int getCost() { return cost; }

    public void setCost(int cost) { this.cost = cost; }

    public String getName() { return name; }

    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }

    public void setDescription(String description) { this.description = description; }
}

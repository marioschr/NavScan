package com.unipi.marioschr.navscan.models;

public class ClaimedItemModel {
    private String claimedItemCode;
    private String itemID;
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ClaimedItemModel() {}

    public String getClaimedItemCode() {
        return claimedItemCode;
    }

    public void setClaimedItemCode(String claimedItemCode) {
        this.claimedItemCode = claimedItemCode;
    }

    public String getItemID() {
        return itemID;
    }

    public void setItemID(String itemID) {
        this.itemID = itemID;
    }
}

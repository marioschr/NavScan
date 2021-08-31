package com.unipi.marioschr.navscan.models;

public class UserModel {
    private String fullName;
    private String email;
    private int level;
    private float currentLevelXp;
    private float currentLevelMaxXp;
    private String birthday;

    public UserModel() {}

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public float getCurrentLevelXp() {
        return currentLevelXp;
    }

    public void setCurrentLevelXp(float currentLevelXp) {
        this.currentLevelXp = currentLevelXp;
    }

    public float getCurrentLevelMaxXp() {
        return currentLevelMaxXp;
    }

    public void setCurrentLevelMaxXp(float currentLevelMaxXp) {
        this.currentLevelMaxXp = currentLevelMaxXp;
    }

    public String getBirthday() {
        return birthday;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }
}

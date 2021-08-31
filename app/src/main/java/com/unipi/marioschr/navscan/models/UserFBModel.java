package com.unipi.marioschr.navscan.models;

import com.google.firebase.Timestamp;

public class UserFBModel {
    private String fullName;
    private String email;
    private int exp;
    private Timestamp birthday;

    public UserFBModel() {}

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public int getExp() {
        return exp;
    }

    public void setExp(int exp) {
        this.exp = exp;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Timestamp getBirthday() {
        return birthday;
    }

    public void setBirthday(Timestamp birthday) {
        this.birthday = birthday;
    }
}

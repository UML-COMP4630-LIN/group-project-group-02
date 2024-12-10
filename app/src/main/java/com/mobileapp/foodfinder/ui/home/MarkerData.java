package com.mobileapp.foodfinder.ui.home;

public class MarkerData {
    private String name;
    private String address;

    public MarkerData(String name, String address) {
        this.name = name;
        this.address = address;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

}
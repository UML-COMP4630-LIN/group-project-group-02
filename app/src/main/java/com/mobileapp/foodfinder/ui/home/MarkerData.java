package com.mobileapp.foodfinder.ui.home;

public class MarkerData {
    private String name;
    private String address;

    /*
     * brief: Constructor for the MarkerData class. Initializes the name and address of the marker.
     * param: name - The name of the marker.
     *        address - The address associated with the marker.
     * return: None.
     */
    public MarkerData(String name, String address) {
        this.name = name;
        this.address = address;
    }

    /*
     * brief: Retrieves the name of the marker.
     * param: None.
     * return: String - The name of the marker.
     */
    public String getName() {
        return name;
    }

    /*
     * brief: Sets a new name for the marker.
     * param: name - The new name for the marker.
     * return: None.
     */
    public void setName(String name) {
        this.name = name;
    }

    /*
     * brief: Retrieves the address of the marker.
     * param: None.
     * return: String - The address of the marker.
     */
    public String getAddress() {
        return address;
    }
}

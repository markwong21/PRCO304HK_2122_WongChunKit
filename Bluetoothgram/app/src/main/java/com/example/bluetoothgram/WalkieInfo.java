package com.example.bluetoothgram;

public class WalkieInfo {
    private String name;
    private String address;

    public WalkieInfo(String name, String address) {
        this.name = name;
        this.address = address;
    }

    // Return Device name
    public String getName()
    {
        return name;
    }
    // Return Device address
    public String getAddress()
    {
        return address;
    }

    @Override
    public String toString() {
        return getName();
    }
}

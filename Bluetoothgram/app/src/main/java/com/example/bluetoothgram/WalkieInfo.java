package com.example.bluetoothgram;

public class WalkieInfo {
    private String DeviceName;
    private String DeviceAddress;

    public WalkieInfo(String name, String address) {
        this.DeviceName = name;
        this.DeviceAddress = address;
    }

    public String getName()
    {
        return DeviceName;      // return device name
    }

    public String getAddress()
    {
        return DeviceAddress;   // return device MAC address
    }

    @Override
    public String toString() {
        return getName();
    }
}

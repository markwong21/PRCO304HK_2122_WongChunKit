package com.example.bluetoothgram;

// Reference:
// Manoj Sharan Gunasegaran. (2017). gms298/Android-Walkie-Talkie. [online] Available at:
// https://github.com/gms298/Android-Walkie-Talkie [Accessed Date: 15 Apr 2022]

public class WalkieInfo {
    private String DeviceName;
    private String DeviceAddress;

    public WalkieInfo(String name, String address) {
        this.DeviceName = name;
        this.DeviceAddress = address;
    }

    public String getName() {
        // return device name
        return DeviceName;
    }

    public String getAddress() {
        // return device MAC address
        return DeviceAddress;
    }

    @Override
    public String toString() {
        return getName();
    }
}

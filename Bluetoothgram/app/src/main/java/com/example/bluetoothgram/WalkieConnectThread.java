package com.example.bluetoothgram;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.util.UUID;

public class WalkieConnectThread {
    private BluetoothDevice WalkieBluetoothDevice;
    private BluetoothSocket WalkieBluetoothSocket;

    // Establish connection
    @SuppressLint("MissingPermission")
    public boolean connect(BluetoothDevice walkiedevice, UUID UUID) {

        // Get the MAC address
        WalkieBluetoothDevice = walkiedevice;

        try {
            // Create a RFCOMM socket with the UUID
            WalkieBluetoothSocket = WalkieBluetoothDevice.createRfcommSocketToServiceRecord(UUID);
        }
        catch (IOException e) {
            Log.d("CONNECT", "Failed at create RFCOMM");
            return false;
        }

        if (WalkieBluetoothSocket == null) {
            return false;
        }

        try {
            // Try to connect
            WalkieBluetoothSocket.connect();
        } catch(IOException e) {
            Log.d("CONNECT", "Failed at socket connect");
            try {
                WalkieBluetoothSocket.close();
            } catch(IOException close) {
                Log.d("CONNECT", "Failed at socket close");
            }
            // Moved return false out from inner catch, making it return false when connect is unsuccessful.
            // Return value used to determine if intent switch to next screen.
            return false;
        }
        return true;
    }

    // Close connection
    public boolean closeConnect() {
        try {
            WalkieBluetoothSocket.close();
        } catch(IOException e) {
            Log.d("CONNECT", "Failed at socket close");
            return false;
        }
        return true;
    }

    // Returns the bluetooth socket object
    public BluetoothSocket getSocket() {
        return WalkieBluetoothSocket;
    }
}

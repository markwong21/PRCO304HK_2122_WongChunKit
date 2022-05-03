package com.example.bluetoothgram;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.util.UUID;

public class WalkieConnectThread {
    private BluetoothDevice WalkieBluetoothDevice;
    private BluetoothSocket WalkieBluetoothSocket;

    @SuppressLint("MissingPermission")
    public boolean connect(BluetoothDevice walkiedevice, UUID UUID) {   // Buildup connection
        WalkieBluetoothDevice = walkiedevice;                           // Capture the MAC address

        try {
            // Made a new RFCOMM socket with UUID
            WalkieBluetoothSocket = WalkieBluetoothDevice.createRfcommSocketToServiceRecord(UUID);
        }
        catch (IOException e) {
            Log.d("WalkieConnectThread", "Failed to create RFCOMM socket");
            return false;
        }

        if (WalkieBluetoothSocket == null) {
            return false;                           // return false if there is no socket
        }

        try {
            WalkieBluetoothSocket.connect();        // connect by using WalkieBluetoothSocket
        } catch(IOException e) {
            Log.d("WalkieConnectThread", "Failed to connect at socket");
            try {
                WalkieBluetoothSocket.close();
            } catch(IOException close) {
                Log.d("WalkieConnectThread", "Failed to close at socket");
            }
            return false;                           // return false if connect false
        }
        return true;
    }


    public boolean closeConnect() {
        try {
            WalkieBluetoothSocket.close();          // Disconnect
        } catch(IOException e) {
            Log.d("WalkieConnectThread", "Failed to close at socket");
            return false;
        }
        return true;
    }


    public BluetoothSocket getSocket() {
        return WalkieBluetoothSocket;               // Returns the bluetooth socket object
    }
}

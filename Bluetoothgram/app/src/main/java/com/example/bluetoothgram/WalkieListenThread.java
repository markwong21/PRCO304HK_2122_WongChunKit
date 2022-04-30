package com.example.bluetoothgram;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.util.UUID;

public class WalkieListenThread {
    private BluetoothSocket WalkieListenSocket;
    private byte[] buffer;

    @SuppressLint("MissingPermission")
    public boolean ConnectAccept(BluetoothAdapter adapter, UUID ConnectUUID) {
        BluetoothServerSocket WalkieServerSocket = null;
        try {
            WalkieServerSocket = adapter.listenUsingRfcommWithServiceRecord("BTService", ConnectUUID);
        } catch(IOException e) {

        }

        try {
            WalkieListenSocket = WalkieServerSocket.accept();
        } catch (IOException e) {

        }
        if (WalkieListenSocket != null) {
            try {
                WalkieServerSocket.close();
            } catch (IOException e) {

            }
            return true;
        }
        return false;
    }

    // Close connection
    public boolean closeConnect() {
        try {
            WalkieListenSocket.close();
        } catch(IOException e) {
            return false;
        }
        return true;
    }

    // Return socket object
    public BluetoothSocket getSocket() {
        return WalkieListenSocket;
    }
}

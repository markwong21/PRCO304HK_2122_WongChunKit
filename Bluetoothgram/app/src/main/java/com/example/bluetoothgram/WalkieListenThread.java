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
            Log.d("WalkieListenThread", "Fail to use RFCOMM to listen");
        }

        try {
            WalkieListenSocket = WalkieServerSocket.accept();
        } catch (IOException e) {
            Log.d("WalkieListenThread", "Fail to accept connection");
        }
        if (WalkieListenSocket != null) {
            try {
                WalkieServerSocket.close();
            } catch (IOException e) {
                Log.d("WalkieListenThread", "Fail to close socket");
            }
            return true;
        }
        return false;
    }


    public boolean closeConnect() {
        try {
            WalkieListenSocket.close();     // Close connection
        } catch(IOException e) {
            Log.d("WalkieListenThread", "Failed to close socket");
            return false;
        }
        return true;
    }


    public BluetoothSocket getSocket() {
        return WalkieListenSocket;          // Return socket
    }
}

package com.example.bluetoothgram;

// Reference:
// Manoj Sharan Gunasegaran. (2017). gms298/Android-Walkie-Talkie. [online] Available at:
// https://github.com/gms298/Android-Walkie-Talkie [Accessed Date: 15 Apr 2022]

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
            // connect the socket by using the UUID
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
                // close the server socket if there is no WalkieListenSocket
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
            // Close connection
            WalkieListenSocket.close();
        } catch(IOException e) {
            Log.d("WalkieListenThread", "Failed to close socket");
            return false;
        }
        return true;
    }


    public BluetoothSocket getSocket() {
        // Return socket
        return WalkieListenSocket;
    }
}

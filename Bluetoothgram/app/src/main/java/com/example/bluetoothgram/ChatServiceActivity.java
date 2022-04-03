package com.example.bluetoothgram;

import static android.media.session.PlaybackState.STATE_NONE;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Handler;

import java.io.IOException;
import java.util.UUID;

public class ChatServiceActivity {
    private Context context;
    private Handler handler;

    private ConnectThread connectThread;

    private static final UUID APP_UUID=UUID.fromString(
            "00001101-0000-1000-8000-00805F9B34FB");

    // define the state
    public static final int STATE_NONE = 0;
    public static final int STATE_LISTEN = 1;
    public static final int STATE_CONNECTING = 2;
    public static final int STATE_CONNECTED = 3;

    // the current state of ChatServiceActivity
    private int CurrentState;

    // create constructor read the context and handler
    public ChatServiceActivity(Context context, Handler handler){
        this.context=context;
        this.handler=handler;

        // initialize the current state
        CurrentState=STATE_NONE;
    }

    private synchronized void setState(int state){
        CurrentState = state;
        // send the status back to the handler for reflecting the status to MainActivity
        // pass the current status minus 1 send to target MainActivity
        handler.obtainMessage(MainActivity.MESSAGE_STATE_CHANGE,state,-1)
                .sendToTarget();
    }

    public synchronized int getState(){
        return CurrentState;
    }

    private synchronized void start() {

    }

    public synchronized void stop() {

    }

    // create a thread to handle all connectivity
    private class ConnectThread extends Thread {
        private final BluetoothSocket Socket;
        private final BluetoothDevice Device;

        @SuppressLint("MissingPermission")
        public ConnectThread(BluetoothDevice device){
            // Use a temporary object that is later assigned to Socket,
            // because socket is final
            Device=device;
            BluetoothSocket tmp = null;
            // Get a BluetoothSocket to connect with the given BluetoothDevice
            try{
                // MY_UUID is the app's UUID string, also used by the server code
                tmp = device.createRfcommSocketToServiceRecord(APP_UUID);
            }catch (IOException e){}
            Socket = tmp;
        }
    }
}

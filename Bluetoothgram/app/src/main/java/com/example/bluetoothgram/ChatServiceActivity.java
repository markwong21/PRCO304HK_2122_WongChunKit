package com.example.bluetoothgram;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.net.Socket;
import java.util.UUID;

public class ChatServiceActivity {
    private Context context;
    private Handler handler;

    private final BluetoothAdapter adapter;
    private ConnectThread CurrentConnectThread;
    //private ConnectedThread CurrentConnectedThread;
    private AcceptThread CurrentAcceptThread;

    private final String NAME = "ChatServiceActivity";
    // this UUID is generated from uuidgenerator.net:
    // https://www.uuidgenerator.net/
    private static final UUID APP_UUID=UUID.fromString(
            "9f507539-bdfd-45ab-921d-c844f878489f");

    // define the state
    public static final int STATE_NONE = 0;
    public static final int STATE_LISTEN = 1;
    public static final int STATE_CONNECTING = 2;
    public static final int STATE_CONNECTED = 3;

    // the current state of ChatServiceActivity
    private int CurrentState;

    // create constructor read the context and handler
    public ChatServiceActivity(Context context, Handler handler){
        adapter=BluetoothAdapter.getDefaultAdapter();
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


    public synchronized void start(){
        // check if there is any instance of connecting
        if(CurrentConnectThread !=null){
            // cancel the ConnectThread to make sure restarting fresh
            CurrentConnectThread.cancel();
            CurrentConnectThread=null;
        }

        // start a new instance of AcceptThread
        if (CurrentAcceptThread==null){
            CurrentAcceptThread=new AcceptThread();
            CurrentAcceptThread.start();
        }
        setState(STATE_LISTEN);
    }


    // Cancel the thread under the state Connecting and Connected
    // Then run a new CurrentConnectThread
    public synchronized void connect(BluetoothDevice device){
        if(CurrentState == STATE_CONNECTED){
            if(CurrentConnectThread !=null){
                CurrentConnectThread.cancel();
                CurrentConnectThread=null;
            }
        }

        CurrentConnectThread = new ConnectThread(device);
        CurrentConnectThread.start();
        setState(STATE_CONNECTING);
    }


    // stop all thread and set the state to null
    // use this stop before destroy the MainActivity
    public synchronized void stop(){
        if(CurrentConnectThread !=null){
            CurrentConnectThread.cancel();
            CurrentConnectThread=null;
        }

        if(CurrentAcceptThread !=null){
            CurrentAcceptThread.cancel();
            CurrentAcceptThread=null;
        }
        setState(STATE_NONE);
    }


    // Create a new thread to accept connection
    private class AcceptThread extends Thread {
        private final BluetoothServerSocket ServerSocket;

        @SuppressLint("MissingPermission")
        public AcceptThread() {
            BluetoothServerSocket tmp = null;
            try {
                tmp = adapter.listenUsingRfcommWithServiceRecord(NAME, APP_UUID);
            } catch (IOException e) {
            }

            // setup the server of it is done
            ServerSocket = tmp;
        }


        public void run() {
            // connect this service socket
            BluetoothSocket socket= null;

            while(CurrentState != STATE_CONNECTED){
                try{
                    socket = ServerSocket.accept();
                }catch (IOException e) {
                    break;
                }
                // close if it is successful
                if(socket != null){
                    connect(socket.getRemoteDevice());
                    try{
                        // close the server socket
                        ServerSocket.close();
                    }catch (IOException e){}
                }
            }
        }


        // close the server socket
        public void cancel(){
            try{
                ServerSocket.close();
            }catch (IOException e){}
        }
    }

    // create a thread to handle all connectivity
    private class ConnectThread extends Thread {
        private final BluetoothSocket Socket;
        private final BluetoothDevice Device;

        @SuppressLint("MissingPermission")
        public ConnectThread(BluetoothDevice device) {
            // Use a temporary object that is later assigned to Socket,
            // because socket is final
            Device = device;
            BluetoothSocket tmp = null;
            // Get a BluetoothSocket to connect with the given BluetoothDevice
            try {
                // MY_UUID is the app's UUID string, also used by the server code
                tmp = device.createRfcommSocketToServiceRecord(APP_UUID);
            } catch (IOException e) {
                Log.e("Connect->Constructor", e.toString());
            }
            Socket = tmp;
        }

        @SuppressLint("MissingPermission")
        public void run() {
            // Cancel discovery because it will slow down the connection
            adapter.cancelDiscovery();
            try {
                // Connect the device through the socket. This will block
                // until it succeeds or throws an exception
                Socket.connect();
            } catch (IOException e) {
                connectionFailed();
                // Unable to connect; close the socket and get out
                try {
                    Socket.close();
                } catch (IOException e2) {
                    Log.e("Connect->CloseSocket", e.toString());
                }

                //ChatService.this.start();
                return;
            }
            synchronized (ChatServiceActivity.this) {
                CurrentConnectThread = null;
            }
            connect(Device);
        }

        public void cancel() {
           /* try{
                mmSocket.close();
            }catch (IOException e){}*/
        }
    }


        // set the state to STATE_LISTEN when connection is failed
        private void connectionFailed(){
            setState(STATE_LISTEN);

            // send a message to handler to show the state in the MaiActivity
            Message msg = handler.obtainMessage(MainActivity.MESSAGE_TOAST);

            // create a bundle to pass data to message
            Bundle bundle=new Bundle();
            bundle.putString(MainActivity.TOAST,"Cannot connect to device");
            msg.setData(bundle);

            // pass the message to handler
            handler.sendMessage(msg);

            // restart the ChatServiceActivity to listen again
            ChatServiceActivity.this.start();
        }

}

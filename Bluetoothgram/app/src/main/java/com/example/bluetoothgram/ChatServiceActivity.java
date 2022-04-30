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
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.UUID;


public class ChatServiceActivity {
    private Context context;
    private Handler handler;

    private final BluetoothAdapter adapter;
    private ConnectThread CurrentConnectThread;
    private AcceptThread CurrentAcceptThread;
    private ConnectedThread CurrentConnectedThread;

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

        if(CurrentConnectedThread !=null){
            CurrentConnectedThread.cancel();
            CurrentConnectedThread=null;
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

        if(CurrentConnectedThread !=null){
            CurrentConnectedThread.cancel();
            CurrentConnectedThread=null;
        }

        if(CurrentAcceptThread != null){
            CurrentAcceptThread.cancel();
            CurrentAcceptThread =null;
        }

        CurrentConnectThread = new ConnectThread(device);
        CurrentConnectThread.start();
        setState(STATE_CONNECTING);
    }


    @SuppressLint("MissingPermission")
    public synchronized void connected(BluetoothSocket socket,
    BluetoothDevice device){
        // cancel the current ConnectThread
            if(CurrentConnectThread !=null){
                CurrentConnectThread.cancel();
                CurrentConnectThread=null;
            }

        // cancel the current ConnectedThread
            if(CurrentConnectedThread !=null){
                CurrentConnectedThread.cancel();
                CurrentConnectedThread=null;
            }

        // cancel the current AcceptThread
            if(CurrentAcceptThread !=null){
                CurrentAcceptThread.cancel();
                CurrentAcceptThread=null;
            }

        // then create a new ConnectThread to manage the current connection
            CurrentConnectedThread=new ConnectedThread(socket);
            CurrentConnectedThread.start();

            // use the handler to inform the UI to connect
            Message msg = handler.obtainMessage(MainActivity.MESSAGE_DEVICE_NAME);
            Bundle bundle = new Bundle();
            bundle.putString(MainActivity.DEVICE_NAME,device.getName());
            msg.setData(bundle);
            handler.sendMessage(msg);
            setState(STATE_CONNECTED);
        }


        // stop all thread and set the state to null
        // use this stop before destroy the MainActivity
        public synchronized void stop(){
            if(CurrentConnectThread !=null){
                CurrentConnectThread.cancel();
                CurrentConnectThread=null;
            }

            if(CurrentConnectedThread !=null){
                CurrentConnectedThread.cancel();
                CurrentConnectedThread=null;
            }

            if(CurrentAcceptThread !=null){
                CurrentAcceptThread.cancel();
                CurrentAcceptThread=null;
            }

            setState(STATE_NONE);
        }


    // using the write method of the CurrentConnectedThread to write the byte under STATE_CONNECTED
    public void write(byte[]out){
        ConnectedThread CT;
        synchronized (this){
            if(CurrentState != STATE_CONNECTED)
                return;
            CT = CurrentConnectedThread;
        }
        CT.write(out);
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
                BluetoothSocket socket = null;

                while (CurrentState != STATE_CONNECTED) {
                    try {
                        socket = ServerSocket.accept();
                    } catch (IOException e) {
                        break;
                    }
                    // close if it is successful
                    if (socket != null) {
                        connected(socket, socket.getRemoteDevice());
                        try {
                            // close the server socket
                            ServerSocket.close();
                        } catch (IOException e) {
                        }
                    }
                }
            }


            // close the server socket
            public void cancel() {
                try {
                    ServerSocket.close();
                } catch (IOException e) {
                }
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
                connected(Socket,Device);
            }

            public void cancel() {}
        }

        // create a thread to handle sending and receiving message
        // the thread will be run after both side of Bluetooth connected
        private class ConnectedThread extends Thread {
            private final BluetoothSocket BlueSocket;
            private final InputStream InStream;
            private final OutputStream OutStream;

            public ConnectedThread(BluetoothSocket socket) {
                BlueSocket = socket;
                InputStream tmpIn = null;
                OutputStream tmpOut = null;

                try {
                    // use the temp objects to get the input and output streams, stream will be final
                    tmpIn = BlueSocket.getInputStream();
                    tmpOut = BlueSocket.getOutputStream();
                } catch (IOException e) {
                }

                InStream = tmpIn;
                OutStream = tmpOut;
            }

        public void run() {
            byte[] buffer = new byte[1024];
            int bytes;
            while (true) {
                try {
                    // listen the incoming stream
                    bytes = InStream.read(buffer);

                    // send the message to MainActivity
                    // pass to handler, pass the buffer and send to target
                    handler.obtainMessage(MainActivity.MESSAGE_READ, bytes, -1, buffer).sendToTarget();
                } catch (IOException e) {
                    // handle the break connection
                    connectionLost();
                    break;
                }
            }
        }

        // bring the buffer send to the device
        public void write(byte[]buffer){
            try{
                OutStream.write(buffer);
            }catch (IOException e){
                Log.d("MainActivity","Send Fail");
            }
        // notify the MainActivity after message sent success
            handler.obtainMessage(MainActivity.MESSAGE_WRITE,buffer).sendToTarget();
        }

            public void cancel(){
                try{
                    BlueSocket.close();
                }catch (IOException e){}
            }

    }


    // Change the state to STATE_LISTEN when connection suspend
    private void connectionLost(){
        setState(STATE_LISTEN);

    // create a message send to MainActivity to inform the direction lost
        Message msg = handler.obtainMessage(MainActivity.MESSAGE_TOAST);
        Bundle bundle=new Bundle();
        bundle.putString(MainActivity.TOAST,"Connection suspend");
        msg.setData(bundle);
    // pass the message to the handler
        handler.sendMessage(msg);

    // restore the ChatServiceActivity
        ChatServiceActivity.this.start();
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
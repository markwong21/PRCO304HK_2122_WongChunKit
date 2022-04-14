package com.example.bluetoothgram;

import static com.example.bluetoothgram.ChatServiceActivity.STATE_NONE;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    // define the message type
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;

    private Context context;                                    // create context for toast
    private BluetoothAdapter bluetoothAdapter;                  // Bluetooth Adapter view for switching Bluetooth

    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";
    private String ConnectedDeviceName = null;
    private ChatServiceActivity ChatService = null;

    // add request code here
    private static final int LOCATION_PERMISSION_REQUEST = 101;        // create a location permission request
    private static final int CHOSE_DEVICE = 102;


    // for chat message use
    private ListView ChatView;
    private EditText EditMessage;
    private Button SendButton;
    private ArrayAdapter<String> ChatArrayAdapter;             // adapter for listview
    private StringBuffer OutputStringBuffer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        context = this;

        int MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION = 1;
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION);

        // set the bluetooth adapter
        bluetoothAdapter=BluetoothAdapter.getDefaultAdapter();
        ChatService = new ChatServiceActivity(context, handler);

         ChatSetup();
    }

    private void ChatSetup(){
        ChatView=(ListView)findViewById(R.id.list);
        EditMessage=(EditText)findViewById(R.id.message);
        SendButton = (Button)findViewById(R.id.button_send);

        ChatArrayAdapter=new ArrayAdapter<String>(this, R.layout.paired_device_list);
        ChatView.setAdapter(ChatArrayAdapter);

        SendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            // send the message string when click SendButton
            public void onClick(View v) {
                String message = EditMessage.getText().toString();
                sendMessage(message);
            }
        });
        ChatService = new ChatServiceActivity(this,handler);
        OutputStringBuffer=new StringBuffer("");
    }


    private void sendMessage(String message){
        if(ChatService.getState() != ChatService.STATE_CONNECTED){
            // show error message to user when it is not connect to other device
            Toast.makeText(context, "Not Connected",Toast.LENGTH_SHORT).show();
            return;
        }

        if(message.length() > 0){
            // check the length of message string before send the message
            byte[] send=message.getBytes();
            ChatService.write(send);

            // clean the text in edittext after sent message
            OutputStringBuffer.setLength(0);
            EditMessage.setText(OutputStringBuffer);
        }
    }


    @Override
    // Create the Option Menu
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_activity_main, menu);
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    // It will be called when option menu selected
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            // When user select "Add Device" button, call chickBluetoothStatus()
            case R.id.menu_add_devices:
                checkBluetoothStatus();
                return true;

            // When user select "Open Bluetooth" button, call switchBluetooth()
            case R.id.menu_switch_bluetooth:
                switchBluetooth();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    // Before list the device, the app need to make sure Bluetooth is open
    public void checkBluetoothStatus(){
        if (!bluetoothAdapter.isEnabled()) {
            Toast.makeText(context, "Please open Bluetooth before search device", Toast.LENGTH_SHORT).show();
        } else {
            // check the permission after Bluetooth on
            checkPermissions();
        }
    }


    @SuppressLint("MissingPermission")
    private void switchBluetooth () {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // If the device does not have Bluetooth
        if (bluetoothAdapter == null) {
            Toast.makeText(context, "This device does not have Bluetooth", Toast.LENGTH_SHORT).show();
        }

        // Open Bluetooth and make it discoverable if the device does not open Bluetooth
        if (!bluetoothAdapter.isEnabled()) {
            bluetoothAdapter.enable();
            Toast.makeText(context, "Bluetooth opened", Toast.LENGTH_SHORT).show();

            // make the device visible
            Intent discoveryIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);

            // make the device visible in 60 seconds
            discoveryIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 60);
            startActivity(discoveryIntent);

            // Close the Bluetooth if the device is already open Bluetooth
        } else if (bluetoothAdapter.isEnabled()) {
            bluetoothAdapter.disable();
            Toast.makeText(context, "Bluetooth off", Toast.LENGTH_SHORT).show();
        }
    }


    private void checkPermissions() {
        // Check whether ACCESS_FINE_LOCATION is granted
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            // Request permission if it was not granted
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST);
        } else {
            Intent intent = new Intent(context, DeviceListActivity.class);
            startActivityForResult(intent, CHOSE_DEVICE);
        }
    }


    // handle activity result
    @SuppressLint("MissingSuperCall")
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            // if the request equal to
            case CHOSE_DEVICE:
                if (resultCode == Activity.RESULT_OK) {
                    // get the address
                    String deviceaddress = data.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
                    BluetoothDevice device = bluetoothAdapter.getRemoteDevice(deviceaddress);
                    ChatService.connect(device);
                }
                break;
        }
    }


    @Override
    // Response to handle the request permission result
    public void onRequestPermissionsResult ( int requestCode, @NonNull String[] permissions,
                                             @NonNull int[] grantResults){

        // If the permission is granted, move to the next activity
        if (requestCode == LOCATION_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Intent intent = new Intent(context, DeviceListActivity.class);
                startActivityForResult(intent, CHOSE_DEVICE);
            } else {
                // Show the dialog to request permission if it is not granted
                new AlertDialog.Builder(context)
                        .setCancelable(false)
                        .setMessage("Please grant the permission!")
                        .setPositiveButton("Grant", (dialogInterface, i) -> {
                            // Go to checkPermissions() if user select "Grant"
                            checkPermissions();
                        })
                        .setNegativeButton("Deny", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // Close the dialog if user select "Deny"
                                MainActivity.this.finish();
                            }
                        }).show();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }


    // Create a function to reflect state
    private void setState(CharSequence subTitle) {
        getSupportActionBar().setSubtitle(subTitle);
    }


    @SuppressLint("HandlerLeak")
    // handle all the message coming from the service
    private final Handler handler=new Handler() {
        @Override
        public void handleMessage(Message msg) {
            // tell the type of message
            switch (msg.what) {
                case MESSAGE_STATE_CHANGE:
                    switch (msg.arg1){
                        // show the state in the system UI
                        case ChatServiceActivity.STATE_NONE:
                            setState("Not Connected");
                            break;
                        case ChatServiceActivity.STATE_LISTEN:
                            setState("Not Connected");
                            break;
                        case ChatServiceActivity.STATE_CONNECTING:
                            setState("Connecting, please wait");
                            break;
                        case ChatServiceActivity.STATE_CONNECTED:
                            setState("Connected to: " + ConnectedDeviceName);
                            break;
                    }
                    break;
                case MESSAGE_WRITE:
                    byte[]writeBuf =(byte[])msg.obj;
                    // convert to the string and pass the buffer here
                    String writeMessage=new String(writeBuf);
                    // add to the adapter
                    ChatArrayAdapter.add("Me： " + writeMessage);
                    break;
                case MESSAGE_READ:
                    // store the input buffer
                    byte[]readBuf =(byte[])msg.obj;
                    // convert the buffer to string
                    String readMessage=new String(readBuf,0,msg.arg1);
                    // add the string to adapter
                    ChatArrayAdapter.add(ConnectedDeviceName+": " +readMessage);
                    break;
                case MESSAGE_DEVICE_NAME:
                    ConnectedDeviceName=msg.getData().getString(DEVICE_NAME);
                    // create a toast to show the connected device name
                    Toast.makeText(context, ConnectedDeviceName, Toast.LENGTH_SHORT).show();
                    break;
                case MESSAGE_TOAST:
                    if(obtainMessage()!=null && obtainMessage().getData().getString(TOAST)!=null){
                        Toast.makeText(context, obtainMessage().getData().getString(TOAST), Toast.LENGTH_SHORT).show();
                    }
                    break;
            }
        }
    };


    @Override
    public synchronized void onDestroy () {
        super.onDestroy();
        // call stop method in ChatService if ChatService is exist
        if (ChatService != null)
            ChatService.stop();
    }

}
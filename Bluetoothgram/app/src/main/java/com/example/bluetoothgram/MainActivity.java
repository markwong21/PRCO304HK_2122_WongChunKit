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
    private final int LOCATION_PERMISSION_REQUEST = 101;        // create a location permission request
    private final int CHOSE_DEVICE = 102;

    //
    private final int REQUEST_CONNECT_DEVICE = 1;
    public static final int REQUEST_ENABLE_BT = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        context = this;

        int MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION = 1;
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION);
    }

    //
    @SuppressLint("HandlerLeak")
    // handle all the message coming from the service
    private final Handler Handler=new Handler() {
        @Override
        public void handleMessage(Message msg) {
            // tell the type of message
            switch (msg.what) {
                case MESSAGE_STATE_CHANGE:
                    switch (msg.arg1){
                        case ChatServiceActivity.STATE_NONE:
                            setState("Not Connected");
                            break;
                        case ChatServiceActivity.STATE_LISTEN:
                            setState("Not Connected");
                            break;
                        case ChatServiceActivity.STATE_CONNECTING:
                            setState("Connecting...");
                            break;
                        case ChatServiceActivity.STATE_CONNECTED:
                            setState("Connected: " + ConnectedDeviceName);
                            break;
                    }
                    break;
                case MESSAGE_WRITE:
                    break;
                case MESSAGE_READ:
                    break;
                case MESSAGE_DEVICE_NAME:
                    ConnectedDeviceName=msg.getData().getString(DEVICE_NAME);
                    Toast.makeText(context, ConnectedDeviceName, Toast.LENGTH_SHORT).show();
                    break;
                case MESSAGE_TOAST:
                    Toast.makeText(context, obtainMessage().getData().getString(TOAST), Toast.LENGTH_SHORT).show();
            }
        }
    };

    // Create a function to reflect state
    private void setState(CharSequence subTitle) {
        getSupportActionBar().setSubtitle(subTitle);
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
                chickBluetoothStatus();
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
    public void chickBluetoothStatus(){
        if (!bluetoothAdapter.isEnabled()) {
            Toast.makeText(context, "Please open Bluetooth before search device", Toast.LENGTH_SHORT).show();
        } else {
            // check the permission after Bluetooth on
            checkPermissions();
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
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        switch (requestCode){
            // if the request equal to
            case REQUEST_CONNECT_DEVICE:
                if(resultCode== Activity.RESULT_OK){
                    // get the address
                    data.getStringExtra("deviceAddress");
//                   BluetoothDevice device=BluetoothAdapter.getRemoteDevice(deviceaddress);
//                   mChatService.connect(device);
//               }
//                break;

                // if the request code equal to
//            case REQUEST_ENABLE_BT:
//                if(resultCode == Activity.RESULT_OK){
//                    setupChat();
//                }else {
//                    Toast.makeText(this, "bt_not_enable_leaving",
//                            Toast.LENGTH_SHORT).show();
//                    finish();
                }
        }
    }

    @Override
    // Response to handle the request permission result
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

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

    @SuppressLint("MissingPermission")
    private void switchBluetooth() {
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
}
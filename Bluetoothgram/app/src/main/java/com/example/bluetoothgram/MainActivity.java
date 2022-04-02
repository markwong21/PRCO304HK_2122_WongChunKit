package com.example.bluetoothgram;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.view.Menu;
import android.view.MenuItem;

import android.os.Bundle;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    private Context context;                                    // create context for toast
    private BluetoothAdapter bluetoothAdapter;                  // Bluetooth Adapter view for switching Bluetooth

    // add request code here
    private final int LOCATION_PERMISSION_REQUEST = 101;        // create a location permission request

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        context = this;
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

            // When user select "Add Device" button
            case R.id.menu_add_devices:
                Toast.makeText(context, "Clicked add Device", Toast.LENGTH_SHORT).show();
                checkPermissions();
                return true;

            // When user select "Open Bluetooth" button
            case R.id.menu_switch_bluetooth:
                switchBluetooth();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void checkPermissions() {
        // Check whether ACCESS_FINE_LOCATION is granted
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            // Request permission if it was not granted
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST);
        } else {
        Intent intent = new Intent(context, DeviceListActivity.class);
        startActivity(intent);
        }
    }

    @Override
    // Response to handle the request permission result
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        // If the permission is granted, move to the next activity
        if (requestCode == LOCATION_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Intent intent = new Intent(context, DeviceListActivity.class);
                startActivity(intent);
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
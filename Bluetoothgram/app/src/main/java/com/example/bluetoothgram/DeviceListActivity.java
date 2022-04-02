package com.example.bluetoothgram;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.Set;

public class DeviceListActivity extends AppCompatActivity {
    private ListView PairedList, AvailableList;                 // instance of two listview
    private ProgressBar Scanning;

    private ArrayAdapter<String> PairedListAdapter, AvailableListAdapter;
    private Context context;                                    // create context for toast
    private BluetoothAdapter bluetoothAdapter;                  // Bluetooth Adapter view for switching Bluetooth

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_list);
        context = this;

        init();
    }

    @SuppressLint("MissingPermission")
    private void init() {
        // initialize the listview and adapter
        PairedList = findViewById(R.id.paired_list);
        AvailableList = findViewById(R.id.available_list);

        PairedListAdapter = new ArrayAdapter<String>(context, R.layout.available_device_list);
        AvailableListAdapter = new ArrayAdapter<String>(context, R.layout.paired_device_list);

        // set adapter in the list
        PairedList.setAdapter(PairedListAdapter);
        AvailableList.setAdapter(AvailableListAdapter);

        // initialize the BluetoothAdapter, and return all paired devices
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        @SuppressLint("MissingPermission")
        Set<BluetoothDevice> PairedDevice = bluetoothAdapter.getBondedDevices();

        // show the paired device if there is at least one item
        if (PairedDevice != null && PairedDevice.size() >= 1) {
            for (BluetoothDevice device : PairedDevice) {
                PairedListAdapter.add(device.getName() + "\n" + device.getAddress());
            }
        }
    }
}
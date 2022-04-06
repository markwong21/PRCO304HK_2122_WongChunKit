package com.example.bluetoothgram;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Set;

public class DeviceListActivity extends AppCompatActivity {
    public static String EXTRA_DEVICE_ADDRESS="device_address";
    private BluetoothAdapter bluetoothAdapter;                  // Bluetooth Adapter view for switching Bluetooth
    private ListView PairedList, AvailableList;                 // instance of two listview
    private ProgressBar Scanning;

    private ArrayAdapter<String> PairedListAdapter, AvailableListAdapter;
    private Context context;                                    // create context for toast

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_list);
        context = this;

        int MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION = 1;
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION);

        init();
    }


    @SuppressLint("MissingPermission")
    private void init() {
        // initialize the listview and adapter
        PairedList = findViewById(R.id.paired_list);
        AvailableList = findViewById(R.id.available_list);
        Scanning = findViewById(R.id.progress_scaning);

        PairedListAdapter = new ArrayAdapter<String>(context, R.layout.available_device_list);
        AvailableListAdapter = new ArrayAdapter<String>(context, R.layout.paired_device_list);

        // set adapter in the list
        PairedList.setAdapter(PairedListAdapter);
        AvailableList.setAdapter(AvailableListAdapter);

        PairedList.setOnItemClickListener(DeviceClickListener);
        AvailableList.setOnItemClickListener(DeviceClickListener);

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

        //register receiver
        IntentFilter intentFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(DeviceDetecter, intentFilter);
        IntentFilter intentFilter1 = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(DeviceDetecter, intentFilter1);
    }


    @Override
    // Create the Option Menu
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_device_list, menu);
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    // It will be called when option menu selected
    public boolean onOptionsItemSelected(MenuItem item) {
        // When user select "Scan Device" button
        switch (item.getItemId()) {
            case R.id.menu_scan_devices:
                scanNewDevices();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    @SuppressLint("MissingPermission")
    private void scanNewDevices() {
        if (!bluetoothAdapter.isEnabled()) {
            Toast.makeText(context, "Please open Bluetooth before scan", Toast.LENGTH_SHORT).show();
        } else {
            // make it visible
            Scanning.setVisibility(View.VISIBLE);

            //remove all current available device in the list to update the list
            AvailableListAdapter.clear();
            Toast.makeText(context, "Scanning", Toast.LENGTH_SHORT).show();

            // if the user click scan button again, the discover function should be cancel first to avoid the loop
            if (bluetoothAdapter.isDiscovering()) {
                bluetoothAdapter.cancelDiscovery();
            }

            bluetoothAdapter.startDiscovery();
        }
    }


    private AdapterView.OnItemClickListener DeviceClickListener=new AdapterView.OnItemClickListener() {
        @SuppressLint("MissingPermission")
        @Override
        public void onItemClick(AdapterView<?> adapterView,
                                View view, int i, long l) {
            bluetoothAdapter.cancelDiscovery();

            // need the info from the list item
            // get the entire detail from the item, include the name and address
            String deviceinfo=((TextView) view).getText().toString();
            // only address is needed, so the length of deviceinfo minus 17
            String deviceaddress=deviceinfo.substring(deviceinfo.length()-17);
            Intent intent =new Intent();
            intent.putExtra(EXTRA_DEVICE_ADDRESS,deviceaddress);

            // pass the intent
            setResult(Activity.RESULT_OK,intent);
            finish();
        }
    };


    // listen the incoming device
    private final BroadcastReceiver DeviceDetecter = new BroadcastReceiver() {
        @SuppressLint("MissingPermission")
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            // check the current action
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                // check whether the device is paired
                if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                    // add to available list if it is not paired
                    AvailableListAdapter.add(device.getName() + "\n" + device.getAddress());
                }
                // if the scanning is finished
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                // hide the progress bar if the scanning is finished
                Scanning.setVisibility(View.GONE);

                // if no available device found
                if (AvailableListAdapter.getCount() == 0) {
                    Toast.makeText(context, "No new devices", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, "Select a new device to chat", Toast.LENGTH_SHORT).show();
                }
            }
        }
    };
}
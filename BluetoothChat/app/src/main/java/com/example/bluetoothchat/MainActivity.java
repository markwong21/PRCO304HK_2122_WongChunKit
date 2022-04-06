package com.example.bluetoothchat;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    Button buttonON, buttonOFF, buttonSHOW, buttonDISCOVER, buttonENABLE;
    BluetoothAdapter myBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    ListView listView;
    ArrayList<String> stringArrayList=new ArrayList<String>();
    ArrayAdapter<String> arrayAdapter;

    Intent btEnablingIntent;
    int requestCodeForEnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        buttonON=(Button) findViewById(R.id.btON);
        buttonOFF=(Button) findViewById(R.id.btOFF);
        buttonSHOW=(Button) findViewById(R.id.btSHOW);
        buttonDISCOVER=(Button) findViewById(R.id.btDISCOVER);
        buttonENABLE=(Button) findViewById(R.id.btENABLE);
        listView=(ListView) findViewById(R.id.scanlistview);

        myBluetoothAdapter=BluetoothAdapter.getDefaultAdapter();
        btEnablingIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        requestCodeForEnable = 1;

        // 006 : Discover Devices - 2/2 : Android studio bluetooth communication
        buttonDISCOVER.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("MissingPermission")
            @Override
            public void onClick(View view) {
                myBluetoothAdapter.startDiscovery();
            }
        });

        IntentFilter intentFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(myReceiver, intentFilter);

        arrayAdapter=new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1,stringArrayList);
        listView.setAdapter(arrayAdapter);

        // 007 : Enabling discoverability : Android studio bluetooth communication
        buttonENABLE.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("MissingPermission")
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                // set the discover duration to 60 second
                intent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 60);
                startActivity(intent);
            }
        });
        

        bluetoothONMethod();
        bluetoothOFFMethod();
        ListDevices();

    }

    // 006 : Discover Devices - 2/2 : Android studio bluetooth communication
    BroadcastReceiver myReceiver= new BroadcastReceiver() {
        @SuppressLint("MissingPermission")
        @Override
        public void onReceive(Context context, Intent intent) {
            String action =intent.getAction();
            // when new device discover
            if(BluetoothDevice.ACTION_FOUND.equals(action)){
                BluetoothDevice device=intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // the device name will be added in the list view
                stringArrayList.add(device.getName());
                // notify the arrayAdapter if stringArrayList change
                arrayAdapter.notifyDataSetChanged();
            }
        }
    };

    // 002 : Enable Bluetooth on Device 2/2 : Android studio bluetooth communication
    private void bluetoothOFFMethod(){
        // When buttonOFF is selected
        buttonOFF.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("MissingPermission")
            @Override
            public void onClick(View view) {
                if(myBluetoothAdapter.isEnabled()){
                    // disable Bluetooth if it is enable
                    myBluetoothAdapter.disable();
                }
            }
        });
    }

    // 002 : Enable Bluetooth on Device 2/2 : Android studio bluetooth communication
    @SuppressLint("MissingSuperCall")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        // check the request code
        if(requestCode==requestCodeForEnable){
            if(resultCode==RESULT_OK){
                Toast.makeText(getApplicationContext(), "Bluetooth is enable", Toast.LENGTH_SHORT).show();
            }else if(requestCode==RESULT_CANCELED){
                Toast.makeText(getApplicationContext(), "Bluetooth enabling cancelled", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // 002 : Enable Bluetooth on Device 2/2 : Android studio bluetooth communication
    private void bluetoothONMethod(){
        // When buttonON is selected
        buttonON.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Verify Bluetooth is support on the device or not
                if(myBluetoothAdapter==null){
                    // if device does not support Bluetooth
                    Toast.makeText(getApplicationContext(), "Bluetooth does not support on this device", Toast.LENGTH_SHORT).show();
                }else{
                    // if Bluetooth is supported but the not enable
                    if(!myBluetoothAdapter.isEnabled()){
                        // enable Bluetooth if it is not enable
                        startActivityForResult(btEnablingIntent, requestCodeForEnable);
                    }
                }
            }
        });
    }

    // 004 : List Paired Devices 2/2 : Android studio bluetooth communication
    private void ListDevices(){
        buttonSHOW.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("MissingPermission")
            @Override
            public void onClick(View view) {
                Set<BluetoothDevice> bt= myBluetoothAdapter.getBondedDevices();
                String[] strings=new String[bt.size()];
                int index =0;

                // if Bluetooth device found
                if(bt.size()>0) {
                    for (BluetoothDevice device : bt) {
                        // add the device name and add to the index
                        strings[index] = device.getName();
                        index++;
                    }
                    ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String> (getApplicationContext(), android.R.layout.simple_list_item_1, strings);
                    // list all device on the listview
                    listView.setAdapter(arrayAdapter);
                }
            }
        });
    }
}
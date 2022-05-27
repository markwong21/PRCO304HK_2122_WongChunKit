package com.example.bluetoothgram;

// Reference:
// Qaifi Khan. (2019). AndroidTutorials/BluetoothChatApp/. [online] Available at:
// https://github.com/qaifikhan/AndroidTutorials/tree/master/BluetoothChatApp [Accessed Date: 21 Mar 2022]
//
// Manoj Sharan Gunasegaran. (2017). gms298/Android-Walkie-Talkie. [online] Available at:
// https://github.com/gms298/Android-Walkie-Talkie [Accessed Date: 15 Apr 2022]
//
// Nikolay Elenkov and Paul Stöhr. (2015). nelenkov/ecdh-kx. [online] Available at:
// https://github.com/nelenkov/ecdh-kx [Accessed Date: 15 May 2022]

import static android.service.controls.ControlsProviderService.TAG;
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
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.Security;
import java.util.UUID;
import java.util.Set;
import java.util.ArrayList;

import javax.crypto.Cipher;
import javax.crypto.KeyAgreement;
import javax.crypto.spec.SecretKeySpec;
import java.util.Random;
import java.security.MessageDigest;


import com.google.android.material.snackbar.Snackbar;

import org.spongycastle.jce.spec.ECParameterSpec;
import org.spongycastle.math.ec.ECCurve;
import org.spongycastle.util.encoders.Hex;


public class MainActivity extends AppCompatActivity {

    // define the message type
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;
    public static final int MESSAGE_READPASSWORD = 6;

    private Context context;                                    // create context for toast
    private BluetoothAdapter bluetoothAdapter;                  // Bluetooth Adapter view for switching Bluetooth
    private BluetoothAdapter walkie_bluetoothAdapter;

    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";
    private String ConnectedDeviceName = null;
    private ChatServiceActivity ChatService = null;

    // add request code here
    private static final int LOCATION_PERMISSION_REQUEST = 101;        // create a location permission request
    private static final int CHOSE_DEVICE = 102;

    // for chat message use
    private ListView ChatView, PairedWalkieListView;
    private EditText EditMessage;
    private Button SendButton, ListenButton, SwitchConnectButton, ListWalkieDeviceButton, TalkButton;
    private ArrayAdapter<String> ChatArrayAdapter;             // adapter for listview
    private ArrayAdapter<String> CyptoArrayAdapter;             // adapter for listview
    private StringBuffer OutputStringBuffer;

    // Requesting permission to RECORD_AUDIO
    private static final int RECORD_AUDIO_PERMISSION_REQUEST = 200;
    private boolean RecordPermission = false;
    private String [] record_permission = {Manifest.permission.RECORD_AUDIO};

    private static final UUID APP_UUID = UUID.fromString("9f507539-bdfd-45ab-921d-c844f878489f");
    private WalkieConversation WalkieAudio;
    private WalkieListenThread walkieListenThread;
    private WalkieConnectThread walkieConnectThread;
    private BluetoothSocket WalkieBluetoothSocket;

    private boolean WalkieListenStatus = false;
    private boolean WalkieConnectStatus = false;

    private ArrayList<WalkieInfo> PairedWalkieList;
    private ArrayAdapter<WalkieInfo> WalkieAdapter;
    private Set<BluetoothDevice> PairedWalkieDevice;

    // AES Password used to encrypt and decrypted
    String Password = "f9049ca7b36b47e28e5e1ab1";
    String AES = "AES";

    private static String TAG = MainActivity.class.getSimpleName();
    private static String KeyPairA_Key = "KeyPair_A";
    private static String KeyPairB_Key = "KeyPair_B";
    private static final String CURVE_NAME = "secp160k1";
    public Cryptography cryptography;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cryptography = Cryptography.getInstance();

        ActivityCompat.requestPermissions(this, record_permission, RECORD_AUDIO_PERMISSION_REQUEST);

        PairedWalkieListView=(ListView)findViewById(R.id.Walkie_list);
        ListenButton = (Button)findViewById(R.id.button_listen);
        SwitchConnectButton = (Button)findViewById(R.id.button_switchConnect);
        ListWalkieDeviceButton = (Button)findViewById(R.id.button_listWalkieDevice);
        TalkButton = (Button)findViewById(R.id.button_talk);


        context = this;

        int MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION = 1;
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION);

        walkieListenThread = new WalkieListenThread();
        walkieConnectThread = new WalkieConnectThread();
        WalkieAudio = new WalkieConversation();

        // Disable microphone button
        TalkButton.setVisibility(TalkButton.GONE);

        // set the bluetooth adapter
        bluetoothAdapter=BluetoothAdapter.getDefaultAdapter();
        walkie_bluetoothAdapter=BluetoothAdapter.getDefaultAdapter();
        ChatService = new ChatServiceActivity(context, handler);

        // when listen button was click
        ListenButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                ListenButton.setEnabled(false);
                ListWalkieDeviceButton.setEnabled(false);
                PairedWalkieListView.setVisibility(View.GONE);

                boolean connectSuccess = walkieListenThread.ConnectAccept(walkie_bluetoothAdapter, APP_UUID);
                Toast.makeText(context, "Wait", Toast.LENGTH_SHORT).show();

                // Toast notification on status.
                if (connectSuccess) {
                    WalkieBluetoothSocket = walkieListenThread.getSocket();
                    WalkieAudio.CreateAudio();
                    WalkieAudio.setSocket(WalkieBluetoothSocket);
                    WalkieAudio.setupStreams();
                    WalkieAudio.startPlay();
                    WalkieListenStatus = true;
                    TalkButton.setVisibility(TalkButton.VISIBLE);
                    Toast.makeText(context, "Success", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, "Fail", Toast.LENGTH_SHORT).show();
                    ListenButton.setEnabled(true);
                    SwitchConnectButton.setEnabled(true);
                }
            }
        });

        ListWalkieDeviceButton.setOnClickListener(new View.OnClickListener(){
            @SuppressLint("MissingPermission")
            @Override
            public void onClick(View arg0) {

                // Handle UI changes
                PairedWalkieListView.setVisibility(View.VISIBLE);
                ListWalkieDeviceButton.setEnabled(false);

                // List to store all paired device information
                PairedWalkieList = new ArrayList<WalkieInfo>();
                PairedWalkieDevice = walkie_bluetoothAdapter.getBondedDevices();

                // Populate list with the paired device information
                if (PairedWalkieDevice.size() > 0) {
                    for (BluetoothDevice device : PairedWalkieDevice) {
                        WalkieInfo newDevice= new WalkieInfo(device.getName(),device.getAddress());
                        PairedWalkieList.add(newDevice);
                    }
                }

                // No devices found
                if (PairedWalkieList.size() == 0) {
                    Toast.makeText(context, "No paired device", Toast.LENGTH_SHORT).show();
                }

                // Populate List view with device information
                WalkieAdapter = new ArrayAdapter<WalkieInfo>(MainActivity.this, android.R.layout.simple_list_item_1, PairedWalkieList);
                PairedWalkieListView.setAdapter(WalkieAdapter);
            }
        });

        // close the walkie connection
        SwitchConnectButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View arg0) {
                boolean disconnectListen = false;
                boolean disconnectConnect = false;

                ListenButton.setEnabled(true);
                ListWalkieDeviceButton.setEnabled(true);
                PairedWalkieListView.setVisibility(View.GONE);

                // Close the bluetooth socket
                if (WalkieListenStatus) {
                    disconnectListen = walkieListenThread.closeConnect();
                    WalkieListenStatus = false;
                }
                if (WalkieConnectStatus) {
                    disconnectConnect = walkieConnectThread.closeConnect();
                    WalkieConnectStatus = false;
                }

                WalkieAudio.destroyProcesses();

                if (disconnectListen || disconnectConnect) {
                    // Handle UI change if disconnect success
                    TalkButton.setVisibility(TalkButton.GONE);
                    ListenButton.setEnabled(true);
                    ListWalkieDeviceButton.setEnabled(true);
                    if(PairedWalkieListView.getVisibility() == View.VISIBLE){
                        PairedWalkieListView.setVisibility(View.GONE);
                    }
                } else {
                    // Do nothing if disconnect not success
                }
            }
        });


        // Attempt to connect when paired device is clicked in ListView
        PairedWalkieListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                // Get the MAC address of the device you want to connect to
                BluetoothDevice device = walkie_bluetoothAdapter.getRemoteDevice(PairedWalkieList.get(position).getAddress());

                // Connect to the device
                boolean connectSuccess = walkieConnectThread.connect(device, APP_UUID);

                // Toast notification on status.
                if (connectSuccess) {
                    // Handle socket objects
                    WalkieBluetoothSocket = walkieConnectThread.getSocket();

                    // Start listening for audio from other device
                    WalkieAudio.CreateAudio();
                    WalkieAudio.setSocket(WalkieBluetoothSocket);
                    WalkieAudio.setupStreams();
                    WalkieAudio.startPlay();

                    // Change status of UI elements
                    PairedWalkieListView.setVisibility(View.GONE);
                    TalkButton.setVisibility(TalkButton.VISIBLE);
                    ListenButton.setEnabled(false);
                    ListWalkieDeviceButton.setEnabled(false);
                    WalkieConnectStatus = true;
                }
            }
            });

        // Tap the button when user need to talk
        TalkButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();
                // Start record voice when user tap the button
                if (action == MotionEvent.ACTION_DOWN ) {
                    WalkieAudio.stopPlay();
                    WalkieAudio.startRecord();

                    // Play the audio when the button is not tapped
                } else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL ) {
                    WalkieAudio.stopRecord();
                    WalkieAudio.startPlay();
                }
                return false;
            }
        });

        ChatSetup();
    }

    private void ChatSetup(){
        ChatView=(ListView)findViewById(R.id.Message_list);
        EditMessage=(EditText)findViewById(R.id.message);
        SendButton = (Button)findViewById(R.id.button_send);


        ChatArrayAdapter=new ArrayAdapter<String>(this, R.layout.paired_device_list);
        ChatView.setAdapter(ChatArrayAdapter);

        SendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            // send the message string when click SendButton
            public void onClick(View v) {
                String message = null;
                try {
                    if(ChatArrayAdapter.getCount()<1){
                        // Send the public key which is not need to encrypt
                        message = EditMessage.getText().toString();
                    } else {
                        // call AES_Encrypt method to encrypt message after click "Send" button
                        message = AES_Encrypt(EditMessage.getText().toString(), Password.toString());
                    }
                } catch (Exception e) {
                    Toast.makeText(context, "Encrypted failed",Toast.LENGTH_SHORT).show();
                }
                sendMessage(message);
            }
        });
        ChatService = new ChatServiceActivity(this,handler);
        OutputStringBuffer=new StringBuffer("");
    }

    private String AES_Encrypt(String TextMessage, String password) throws Exception{
        // Generate secret case by using given password
        SecretKeySpec AES_key = GenKey(password);
        // Use cipher with AES to encrypt
        Cipher cipher = Cipher.getInstance(AES);
        cipher.init(Cipher.ENCRYPT_MODE, AES_key);
        byte[] EncValue = cipher.doFinal(TextMessage.getBytes());
        // Use Base64 to encode message
        String encryptedValue = Base64.encodeToString(EncValue, Base64.DEFAULT);
        return encryptedValue;
    }

    // Generate secret case by using given password
    private SecretKeySpec GenKey(String password) throws Exception{
        // Craete an instance object of SHA-256
        final MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
        // Change the password to byte
        byte[] bytes = password.getBytes("UTF-8");
        messageDigest.update(bytes, 0, bytes.length);
        byte[] key = messageDigest.digest();
        SecretKeySpec secretKeySpec = new SecretKeySpec(key, "AES");
        return secretKeySpec;
    }

    // Call ClearKeys method
    private void CallClearKeys(){
        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(this);
        ClearKeys(prefs);
    }

    //private void CallECDH(){
    //    SharedPreferences prefs = PreferenceManager
    //            .getDefaultSharedPreferences(this);
    //    ECDH(prefs);
    //}

    // Clear all key pair in share preference for store new key pair
    private void ClearKeys(SharedPreferences prefs) {
        SharedPreferences.Editor prefsEditor = prefs.edit();
        prefsEditor.putString(KeyPairA_Key + "_private", null);
        prefsEditor.putString(KeyPairA_Key + "_public", null);
        prefsEditor.putString(KeyPairB_Key + "_private", null);
        prefsEditor.putString(KeyPairB_Key + "_public", null);

        prefsEditor.commit();

        Toast.makeText(MainActivity.this, "Deleted keys.", Toast.LENGTH_LONG)
                .show();
    }

    // Call GenerateKeys method
    private void CallGenerateKeys(){
        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(this);
        GenerateKeys(prefs);
    }

    // Generate the own keypair combined with own public key and own private key
    private void GenerateKeys(SharedPreferences prefs) {
        final SharedPreferences.Editor prefsEditor = prefs.edit();

        new AsyncTask<Void, Void, Boolean>() {

            EC_Parameter ec_param;
            Exception error;

            @Override
            protected void onPreExecute() {
                Toast.makeText(MainActivity.this, "Generating keypair of ECDH", Toast.LENGTH_SHORT).show();
                setProgressBarIndeterminateVisibility(true);
            }

            @Override
            protected Boolean doInBackground(Void... arg0) {
                try {
                    // Call EC_Parameter method and get the parameter
                    ec_param = EC_Parameter.getParams(CURVE_NAME);
                    // Generate the keypair by calling cryptography method
                    KeyPair KeyPair_A = cryptography.GenKeyPairParams(ec_param);
                    // Save the own keypair including public key and private key
                    SaveKeyPair(prefsEditor, KeyPairA_Key, KeyPair_A);
                    return prefsEditor.commit();
                } catch (Exception e) {
                    Log.e(TAG, "Error to generate keys: " + e.getMessage(), error);
                    error = e;
                    return false;
                }
            }

            @Override
            protected void onPostExecute(Boolean result) {
                setProgressBarIndeterminateVisibility(false);
                if (result) {
                    Toast.makeText(MainActivity.this, "Generated and saved keys", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, error == null ? "Error to save keys" : error.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        }.execute();
    }


    // We tried to use ECDH method to generate the common secret by combine the own private key and received public key
    // But cannot read the received public key
    //private void ECDH (final SharedPreferences prefs) {
    //    new AsyncTask<Void, Void, String[]>() {

    //        Exception error;

    //        @Override
    //        protected void onPreExecute() {
    //            Toast.makeText(MainActivity.this,
    //                    "Calculating shared ECDH key...", Toast.LENGTH_SHORT)
    //                    .show();

    //            setProgressBarIndeterminateVisibility(true);
    //        }

    //        @Override
    //        protected String[] doInBackground(Void... arg0) {
    //            try {
    //                KeyPair KeyPair_A = Read_KeyPair(prefs, KeyPairA_Key);
    //                if (KeyPair_A == null) {
    //                    throw new IllegalArgumentException(
    //                            "Key A not found. Generate keys first.");
    //                }

    //                KeyPair KeyPair_B;

    //                byte[] Secret_A = cryptography.ecdh(KeyPair_A.getPrivate(),
    //                        KeyPair_B.getPublic());

    //                return new String[] { Cryptography.hex(Secret_A),
    //                        Cryptography.hex(Secret_B) };
    //            } catch (Exception e) {
    //                Log.e(TAG, "Error doing ECDH: " + e.getMessage(), error);
    //                error = e;

    //                return null;
    //            }
    //        }

    //        @Override
    //        protected void onPostExecute(String[] result) {
    //            setProgressBarIndeterminateVisibility(false);

    //            if (result != null && error == null) {
                    //sharedKeyAText.setText(result[0]);
                    //sharedKeyBText.setText(result[1]);
    //            } else {
    //                Toast.makeText(MainActivity.this, error.getMessage(),
    //                        Toast.LENGTH_LONG).show();
    //            }
    //        }

    //    }.execute();
    //}


    private void SaveKeyPair(SharedPreferences.Editor prefsEditor, String key,
                             KeyPair keypair) {
        // Save public key
        String Public_String = Cryptography.Encode_Base64(keypair.getPublic().getEncoded());
        // Save private key
        String Private_String = Cryptography.Encode_Base64(keypair.getPrivate().getEncoded());

        prefsEditor.putString(key + "_public", Public_String);
        prefsEditor.putString(key + "_private", Private_String);

        // call ExchangePublicKey method
        ExchangePublicKey(Public_String);
    }


    private void ExchangePublicKey(String Public_String){
        if(ChatService.getState() != ChatService.STATE_CONNECTED){
            // show error when the device is not connect to other device
            Toast.makeText(context, "Not Connected",Toast.LENGTH_SHORT).show();
            return;
        }
        if(Public_String.length() > 0){
            byte[] send=Public_String.getBytes();
            // call ChatService to send the public key
            ChatService.write(send);
        }
    }

    private KeyPair Read_KeyPair(SharedPreferences prefs, String key)
            throws Exception {
        // Read the String of public key
        String public_KeyString = prefs.getString(key + "_public", null);
        // Read the String of private key
        String private_KeyString = prefs.getString(key + "_private", null);
        if (public_KeyString == null || private_KeyString == null) {
            return null;
        }
        return cryptography.Read_KeyPair(public_KeyString, private_KeyString);
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

            // When user select "Open Bluetooth" button, call switchBluetooth()
            case R.id.menu_change_lockcode:
                Intent intent = new Intent(MainActivity.this,ChangeLockCodeActivity.class);
                startActivityForResult(intent, 0);

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
                    ChatView=(ListView)findViewById(R.id.Message_list);
                    PairedWalkieListView.setVisibility(View.GONE);

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
        private KeyPair KeyPair_B;

        @Override
        public void handleMessage(Message msg) {
            // tell the type of message
            switch (msg.what) {
                case MESSAGE_STATE_CHANGE:
                    switch (msg.arg1){
                        // show the state in the system UI
                        case ChatServiceActivity.STATE_NONE:
                            CallClearKeys();
                            setState("Not Connected");
                            break;
                        case ChatServiceActivity.STATE_LISTEN:
                            setState("Not Connected");
                            break;
                        case ChatServiceActivity.STATE_CONNECTING:
                            PairedWalkieListView.setVisibility(View.GONE);
                            TalkButton.setVisibility(TalkButton.GONE);
                            setState("Connecting, please wait");
                            break;
                        case ChatServiceActivity.STATE_CONNECTED:
                            PairedWalkieListView.setVisibility(View.GONE);
                            TalkButton.setVisibility(TalkButton.GONE);
                            CallClearKeys();
                            CallGenerateKeys();
                            setState("Connected to: " + ConnectedDeviceName);
                            break;
                    }
                    break;
                case MESSAGE_WRITE:
                    if (ChatArrayAdapter.getCount()<=1){
                        byte[]writeBuf =(byte[])msg.obj;
                        String writeMessage=new String(writeBuf);
                        PairedWalkieListView.setVisibility(View.GONE);
                        TalkButton.setVisibility(TalkButton.GONE);
                        // ChatView.setVisibility(View.VISIBLE);
                        break;
                    } else{
                        byte[]writeBuf =(byte[])msg.obj;
                        // convert to the string and pass the buffer here
                        String writeMessage=new String(writeBuf);
                        PairedWalkieListView.setVisibility(View.GONE);
                        TalkButton.setVisibility(TalkButton.GONE);
                        // add to the adapter
                        ChatArrayAdapter.add("Me： " + writeMessage);
                        ChatView.setVisibility(View.VISIBLE);
                        break;
                    }
                case MESSAGE_READ:
                    // store the input buffer
                    byte[]readBuf =(byte[])msg.obj;
                    // convert the buffer to string
                    String readMessage=new String(readBuf,0,msg.arg1);
                    PairedWalkieListView.setVisibility(View.GONE);
                    TalkButton.setVisibility(TalkButton.GONE);
                    try {
                        if (ChatArrayAdapter.getCount()<=0){
                            ChatArrayAdapter.add(ConnectedDeviceName+": " +readMessage);
                            if (ChatArrayAdapter.getCount()==0){
                                KeyPairB_Key = readMessage;
                                // CallECDH();
                            }
                        } else {
                            // decrypt message
                            SecretKeySpec AES_key = null;
                            // Generate secret case by using given password
                            AES_key = GenKey(Password);
                            // Use cipher with AES to decrypt
                            Cipher cipher = Cipher.getInstance(AES);
                            cipher.init(Cipher.DECRYPT_MODE, AES_key);
                            // Use Base64 to decode message
                            byte[] decodedValue = Base64.decode(readMessage, Base64.DEFAULT);
                            byte[] DecValue = cipher.doFinal(decodedValue);
                            String decryptedValue = new String(DecValue);
                            // add the string to adapter
                            ChatArrayAdapter.add(ConnectedDeviceName+": " +decryptedValue);
                            ChatView.setVisibility(View.VISIBLE);
                        }
                    } catch (Exception e) {
                        Toast.makeText(context, "Decrypted failed",Toast.LENGTH_SHORT).show();
                    }

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
                case MESSAGE_READPASSWORD:
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
package com.example.bluetoothgram;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

public class LoadingActivity extends AppCompatActivity {
    String LockCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);

        // load the lock code
        SharedPreferences PIN = getSharedPreferences("pin", 0);
        LockCode = PIN.getString("LockCode", "");

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (LockCode.equals("")) {
                    // if there is no lock code
                    Intent intent = new Intent(getApplicationContext(), CreateLockCodeActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    // if there is a lock code
                    Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        }, 100);
    }
}
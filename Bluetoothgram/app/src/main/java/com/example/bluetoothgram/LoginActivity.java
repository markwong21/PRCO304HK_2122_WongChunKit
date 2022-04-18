package com.example.bluetoothgram;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class LoginActivity extends AppCompatActivity {
    EditText editText_enterLockCode;
    Button button_Login;

    String LockCode;
    int counter = 5;                // the user have 5 times of chance to login
    CountDownTimer Timer = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // load the lock code
        SharedPreferences PIN = getSharedPreferences("pin", 0);
        LockCode = PIN.getString("LockCode", "");

        editText_enterLockCode = (EditText) findViewById(R.id.enterLockCode);
        button_Login = (Button) findViewById(R.id.button_Login);

        button_Login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String text = editText_enterLockCode.getText().toString();

                // If login success
                if (text.equals(LockCode)) {
                    // Go to the main page
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(intent);
                    // set the number of attempts to 5 times
                    counter=5;
                    finish();
                } else {
                    // Decrease 1 time of login attempts
                    counter--;
                    Toast.makeText(LoginActivity.this, "Invalid login, No of attempts remaining: " + String.valueOf(counter), Toast.LENGTH_SHORT).show();
                    if(counter <= 0){
                        // Disable the login button if login fail in five times in a row
                        button_Login.setEnabled(false);
                        BlockLogin();
                    }
                }
            }

            public void BlockLogin(){
                // Start to count 5 minutes for disable the login button
                Timer = new CountDownTimer(300000, 1000) {
                    public void onTick(long millisUntilFinished) {
                    }
                    public void onFinish() {
                        // enable the login button after finish counting
                        button_Login.setEnabled(true);
                    }
                };
                Toast.makeText(LoginActivity.this, "The login was blocked for 5 minutes", Toast.LENGTH_SHORT).show();
                Timer.start();
            }
        });
    }
}

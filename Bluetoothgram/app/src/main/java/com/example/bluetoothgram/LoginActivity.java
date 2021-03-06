package com.example.bluetoothgram;

// Reference:
// RAdeff. T. (2017). Develop Password protected app in Android Studio. [online] Available at:
// https://www.youtube.com/watch?v=OOclvSIelcI&ab_channel=TihomirRAdeff [Accessed Date: 17 Apr 2022]

import androidx.annotation.NonNull;
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

    String LockCode, StringCounter;
    int counter;
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

        // the user have 5 times of chance to login
        counter = 5;

        button_Login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String text = editText_enterLockCode.getText().toString();

                // If login success
                if (text.equals(LockCode)) {
                    // set the number of attempts to 5 times
                    counter++;

                    // save the number of attempts
                    StringCounter = Integer.toString(counter);
                    SharedPreferences COUNTER = getSharedPreferences("login_counter", 0);
                    SharedPreferences.Editor editor = COUNTER.edit();
                    editor.putString("login_counter",StringCounter);
                    editor.apply();

                    // Go to the main page
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    // Decrease 1 time of login attempts
                    counter--;

                    // save the number of attempts
                    StringCounter = Integer.toString(counter);
                    SharedPreferences COUNTER = getSharedPreferences("login_counter", 0);
                    SharedPreferences.Editor editor = COUNTER.edit();
                    editor.putString("login_counter",StringCounter);
                    editor.apply();

                    Toast.makeText(LoginActivity.this, "Invalid login, No of attempts remaining: " + String.valueOf(counter), Toast.LENGTH_SHORT).show();

                    if(counter <= 0){
                        // lock the login button
                        BlockLogin();
                        Toast.makeText(LoginActivity.this, "The login was blocked for 5 minutes", Toast.LENGTH_SHORT).show();
                        Timer.start();
                    }
                }
            }
        });
    }

    public void BlockLogin(){
        button_Login.setEnabled(false);
        // Start to count 5 minutes for disable the login button
        Timer = new CountDownTimer(300000, 1000) {
            public void onTick(long millisUntilFinished) {
            }
            public void onFinish() {
                // enable the login button after finish counting
                button_Login.setEnabled(true);
                counter=5;

                // save the number of attempts
                StringCounter = Integer.toString(counter);
                SharedPreferences COUNTER = getSharedPreferences("login_counter", 0);
                SharedPreferences.Editor editor = COUNTER.edit();
                editor.putString("login_counter",StringCounter);
                editor.apply();
            }
        };
        Toast.makeText(LoginActivity.this, "The login was blocked for 5 minutes", Toast.LENGTH_SHORT).show();
        Timer.start();
    }
}

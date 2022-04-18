package com.example.bluetoothgram;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class ChangeLockCodeActivity extends AppCompatActivity {
    EditText editText_currentLockCode, editText_newLockCode, editText_confirmNewLockCode;
    Button button_Change;

    String LockCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_lock_code);

        // load the lock code
        SharedPreferences PIN = getSharedPreferences("pin", 0);
        LockCode = PIN.getString("LockCode", "");

        editText_currentLockCode = (EditText) findViewById(R.id.currentLockCode);
        editText_newLockCode = (EditText) findViewById(R.id.newLockCode);
        editText_confirmNewLockCode = (EditText) findViewById(R.id.confirmNewLockCode);
        button_Change = (Button) findViewById(R.id.button_Change);

        button_Change.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String String_currentLockCode = editText_currentLockCode.getText().toString();
                String String_newLockCode = editText_newLockCode.getText().toString();
                String String_confirmNewLockCode = editText_confirmNewLockCode.getText().toString();

                if (!String_currentLockCode.equals(LockCode)) {
                    // the current lock code user enter is wrong
                    Toast.makeText(ChangeLockCodeActivity.this, "Wrong current lock code, please enter again!", Toast.LENGTH_SHORT).show();
                } else {
                    // there is no new lock code
                    if (String_newLockCode.equals("") || String_confirmNewLockCode.equals("")) {
                        Toast.makeText(ChangeLockCodeActivity.this, "No lock code entered, please enter again!", Toast.LENGTH_SHORT).show();
                    } else {
                        // check whether the new lock code is match with confirm lock code
                        if (String_newLockCode.equals(String_confirmNewLockCode)) {
                            // check the length of new lock code
                            if (String_newLockCode.length() >= 6) {
                                // check whether the new lock code contain digit
                                if (String_newLockCode.contains("1") || String_newLockCode.contains("2") || String_newLockCode.contains("3") || String_newLockCode.contains("4") ||
                                        String_newLockCode.contains("5") || String_newLockCode.contains("6") || String_newLockCode.contains("7") || String_newLockCode.contains("8") ||
                                        String_newLockCode.contains("9") || String_newLockCode.contains("0")) {
                                    // check whether the new lock code contain character
                                    if (String_newLockCode.contains("a") || String_newLockCode.contains("b") || String_newLockCode.contains("c") || String_newLockCode.contains("d") ||
                                            String_newLockCode.contains("e") || String_newLockCode.contains("f") || String_newLockCode.contains("g") || String_newLockCode.contains("h") ||
                                            String_newLockCode.contains("i") || String_newLockCode.contains("j") || String_newLockCode.contains("k") || String_newLockCode.contains("l") ||
                                            String_newLockCode.contains("m") || String_newLockCode.contains("n") || String_newLockCode.contains("o") || String_newLockCode.contains("p") ||
                                            String_newLockCode.contains("q") || String_newLockCode.contains("r") || String_newLockCode.contains("s") || String_newLockCode.contains("t") ||
                                            String_newLockCode.contains("u") || String_newLockCode.contains("v") || String_newLockCode.contains("w") || String_newLockCode.contains("x") ||
                                            String_newLockCode.contains("y") || String_newLockCode.contains("z") ||
                                            String_newLockCode.contains("A") || String_newLockCode.contains("B") || String_newLockCode.contains("C") || String_newLockCode.contains("D") ||
                                            String_newLockCode.contains("E") || String_newLockCode.contains("F") || String_newLockCode.contains("G") || String_newLockCode.contains("H") ||
                                            String_newLockCode.contains("I") || String_newLockCode.contains("J") || String_newLockCode.contains("K") || String_newLockCode.contains("L") ||
                                            String_newLockCode.contains("M") || String_newLockCode.contains("N") || String_newLockCode.contains("O") || String_newLockCode.contains("P") ||
                                            String_newLockCode.contains("Q") || String_newLockCode.contains("R") || String_newLockCode.contains("S") || String_newLockCode.contains("T") ||
                                            String_newLockCode.contains("U") || String_newLockCode.contains("V") || String_newLockCode.contains("W") || String_newLockCode.contains("X") ||
                                            String_newLockCode.contains("Y") || String_newLockCode.contains("Z")) {
                                        // save the lock code
                                        SharedPreferences PIN = getSharedPreferences("pin", 0);
                                        SharedPreferences.Editor editor = PIN.edit();
                                        editor.putString("LockCode", String_newLockCode);
                                        Toast.makeText(ChangeLockCodeActivity.this, "Lock code changed", Toast.LENGTH_SHORT).show();
                                        editor.apply();

                                        // go to main page
                                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                        startActivity(intent);
                                        finish();
                                    } else {
                                        Toast.makeText(ChangeLockCodeActivity.this, "The lock code should contain character", Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    Toast.makeText(ChangeLockCodeActivity.this, "The lock code should contain digit", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(ChangeLockCodeActivity.this, "The lock code should be at least 6 digit with character long", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            // if the String_newLockCode is not match with String_confirmNewLockCode
                            Toast.makeText(ChangeLockCodeActivity.this, "Lock code does not match", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
        });
    }
}
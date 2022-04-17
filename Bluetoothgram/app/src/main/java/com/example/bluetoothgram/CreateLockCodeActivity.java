package com.example.bluetoothgram;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class CreateLockCodeActivity extends AppCompatActivity {
    EditText editText_createLockCode, editText_confirmLockCode;
    Button button_Done;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_lock_code);

        editText_createLockCode = (EditText) findViewById(R.id.createLockCode);
        editText_confirmLockCode = (EditText) findViewById(R.id.confirmLockCode);
        button_Done = (Button) findViewById(R.id.button_Done);

        button_Done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String String_createLockCode = editText_createLockCode.getText().toString();
                String String_confirmLockCode = editText_confirmLockCode.getText().toString();

                if (String_createLockCode.equals("") || String_confirmLockCode.equals("")) {
                    // there is no lock code
                    Toast.makeText(CreateLockCodeActivity.this, "No lock code entered, please enter again!", Toast.LENGTH_SHORT).show();
                } else {
                    // check whether the new lock code is match with confirm lock code
                    if (String_createLockCode.equals(String_confirmLockCode)) {
                        // check the length of new lock code
                        if (String_createLockCode.length()>=6) {
                            // check whether the new lock code contain digit
                            if (String_createLockCode.contains("1") || String_createLockCode.contains("2") || String_createLockCode.contains("3") || String_createLockCode.contains("4") ||
                                    String_createLockCode.contains("5") || String_createLockCode.contains("6") || String_createLockCode.contains("7") || String_createLockCode.contains("8") ||
                                    String_createLockCode.contains("9") || String_createLockCode.contains("0") ){
                                // check whether the new lock code contain character
                                if(String_createLockCode.contains("a") || String_createLockCode.contains("b") || String_createLockCode.contains("c") || String_createLockCode.contains("d") ||
                                        String_createLockCode.contains("e") || String_createLockCode.contains("f") || String_createLockCode.contains("g") || String_createLockCode.contains("h") ||
                                        String_createLockCode.contains("i") || String_createLockCode.contains("j") || String_createLockCode.contains("k") || String_createLockCode.contains("l") ||
                                        String_createLockCode.contains("m") || String_createLockCode.contains("n") || String_createLockCode.contains("o") || String_createLockCode.contains("p") ||
                                        String_createLockCode.contains("q") || String_createLockCode.contains("r") || String_createLockCode.contains("s") || String_createLockCode.contains("t") ||
                                        String_createLockCode.contains("u") || String_createLockCode.contains("v") || String_createLockCode.contains("w") || String_createLockCode.contains("x") ||
                                        String_createLockCode.contains("y") || String_createLockCode.contains("z") ||
                                        String_createLockCode.contains("A") || String_createLockCode.contains("B") || String_createLockCode.contains("C") || String_createLockCode.contains("D") ||
                                        String_createLockCode.contains("E") || String_createLockCode.contains("F") || String_createLockCode.contains("G") || String_createLockCode.contains("H") ||
                                        String_createLockCode.contains("I") || String_createLockCode.contains("J") || String_createLockCode.contains("K") || String_createLockCode.contains("L") ||
                                        String_createLockCode.contains("M") || String_createLockCode.contains("N") || String_createLockCode.contains("O") || String_createLockCode.contains("P") ||
                                        String_createLockCode.contains("Q") || String_createLockCode.contains("R") || String_createLockCode.contains("S") || String_createLockCode.contains("T") ||
                                        String_createLockCode.contains("U") || String_createLockCode.contains("V") || String_createLockCode.contains("W") || String_createLockCode.contains("X") ||
                                        String_createLockCode.contains("Y") || String_createLockCode.contains("Z")){
                                    // save the lock code
                                    SharedPreferences PIN = getSharedPreferences("pin", 0);
                                    SharedPreferences.Editor editor = PIN.edit();
                                    editor.putString("LockCode", String_createLockCode);
                                    editor.apply();

                                    // go to main page
                                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                    startActivity(intent);
                                    finish();
                                } else{
                                    Toast.makeText(CreateLockCodeActivity.this, "The lock code should contain character", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(CreateLockCodeActivity.this, "The lock code should contain digit", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(CreateLockCodeActivity.this, "The lock code should be at least 6 digit with character long", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        // if the String_createLockCode is not match with String_confirmLockCode
                        Toast.makeText(CreateLockCodeActivity.this, "Lock code does not match", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }
}
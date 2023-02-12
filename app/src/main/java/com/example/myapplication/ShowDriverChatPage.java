package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class ShowDriverChatPage extends AppCompatActivity {

    TextView mUsername , mPhoneNumber;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_driver_chat_page);

        //textViews
        mUsername = (TextView) findViewById(R.id.username);
        mPhoneNumber = (TextView) findViewById(R.id.phonenumebrnew);

        Intent DriverIntent = getIntent();
        String DriverName = DriverIntent.getStringExtra("DRIVER_NAME");
        String PhoneNumber = DriverIntent.getStringExtra("PHONE_NUMBER");

        mUsername.setText(DriverName);
        mPhoneNumber.setText(PhoneNumber);

    }
}
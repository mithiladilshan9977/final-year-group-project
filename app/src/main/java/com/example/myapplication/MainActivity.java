package com.example.myapplication;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
Button mapbtn , mcustomer , mDriver , submitbtn;

private LinearLayout noInterConnection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mcustomer = findViewById(R.id.customer);
        mDriver = findViewById(R.id.driver);
        noInterConnection= (LinearLayout) findViewById(R.id.nointerconnectionLayout);

       if(!is_Connected()){
           noInterConnection.setVisibility(View.VISIBLE);
           mcustomer.setVisibility(View.GONE);
           mDriver.setVisibility(View.GONE);

           Toast.makeText(getApplicationContext(), "No internet connection !" , Toast.LENGTH_LONG).show();
       }else{
           noInterConnection.setVisibility(View.GONE);
           mcustomer.setVisibility(View.VISIBLE);
           mDriver.setVisibility(View.VISIBLE);


       }


        mDriver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this , DriverLogInActivity.class);
                startActivity(intent);
                finish();
                return;
            }
        });

        mcustomer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this , customerLoginActivity.class);
                startActivity(intent);
                finish();
                return;
            }
        });


    }
Context context;
 private boolean is_Connected(){
     ConnectivityManager connectivityManager = (ConnectivityManager) getApplicationContext().getSystemService(context.CONNECTIVITY_SERVICE);
     return connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isConnectedOrConnecting();



 }

}
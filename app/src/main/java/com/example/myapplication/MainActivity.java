package com.example.myapplication;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

import es.dmoral.toasty.Toasty;

public class MainActivity extends AppCompatActivity {
Button mapbtn    , submitbtn, msound;
ImageButton mcustomer,mDriver ;
private LinearLayout noInterConnection;
private TextView mopenEmail, mcustomerText, moficcerText ;

private ImageView mmainimage;

private FirebaseAuth mAuth;


/////////////

    NotificationHelper notificationHelper;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mcustomer = findViewById(R.id.customer);
        mDriver = findViewById(R.id.driver);
        noInterConnection= (LinearLayout) findViewById(R.id.nointerconnectionLayout);
        mopenEmail = (TextView) findViewById(R.id.openEmail);
        mAuth = FirebaseAuth.getInstance();
        mmainimage = (ImageView) findViewById(R.id.mainimage);
        mcustomerText = (TextView) findViewById(R.id.customertext) ;
        moficcerText = (TextView) findViewById(R.id.officertext) ;
        Button mnotificationbtn = (Button) findViewById(R.id.notificationbtn);

        //////

        notificationHelper = new NotificationHelper(this);

        mnotificationbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
             notificationHelper.sendHighProrityNotification("Notification from ECAS","the body", DriverMapsActivity.class);
            }
        });



        mopenEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_SEND);
                String[] recipient = {"dilshanwickramaaracchi99@gmail.com"};
                intent.putExtra(Intent.EXTRA_EMAIL, recipient);
                intent.putExtra(Intent.EXTRA_SUBJECT,"Functional issue");
                intent.putExtra(Intent.EXTRA_TEXT,"Dear sir, ");
                intent.setType("text/html");
                intent.setPackage("com.google.android.gm");
                startActivity(Intent.createChooser(intent, "Send Email"));

            }
        });

       if(!is_Connected()){
           noInterConnection.setVisibility(View.VISIBLE);
           mcustomer.setVisibility(View.GONE);
           mDriver.setVisibility(View.GONE);
           mmainimage.setVisibility(View.GONE);
           mcustomerText.setVisibility(View.GONE);
           moficcerText.setVisibility(View.GONE);
           mopenEmail.setVisibility(View.GONE);
           Toasty.error(getApplicationContext(), "No internet connection !", Toast.LENGTH_LONG, true).show();

       }else{
           noInterConnection.setVisibility(View.GONE);
           mcustomer.setVisibility(View.VISIBLE);
           mDriver.setVisibility(View.VISIBLE);
           mmainimage.setVisibility(View.VISIBLE);
           mcustomerText.setVisibility(View.VISIBLE);
           moficcerText.setVisibility(View.VISIBLE);
           mopenEmail.setVisibility(View.VISIBLE);


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
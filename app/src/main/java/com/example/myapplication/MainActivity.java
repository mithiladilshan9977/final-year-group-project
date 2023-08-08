package com.example.myapplication;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.google.firebase.auth.FirebaseAuth;

import es.dmoral.toasty.Toasty;

public class MainActivity extends AppCompatActivity {
Button mapbtn    , submitbtn, msound,mgotoimagechat;
ImageButton mcustomer,mDriver ;
private LinearLayout noInterConnection;
private TextView mopenEmail, mcustomerText, moficcerText ;

private ImageView mmainimage;

private LinearLayout gradianColor;

private ImageView mainimage;

private TextView headingtext;

private ImageButton customer;

private ImageButton driver;
private LinearLayout holder;
private TextView customertext;
private TextView officertext;
private TextView openEmail;


private FirebaseAuth mAuth;
String pilicetationName;


/////////////

    NotificationHelper notificationHelper;

    public  static final String SHARED_REF = "shared";
    public  static final String TEXT = "text";





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
        mgotoimagechat = (Button) findViewById(R.id.gotoimagechat);
        gradianColor = (LinearLayout) findViewById(R.id.gradianColor);
        mainimage = (ImageView) findViewById(R.id.mainimage);
        headingtext = (TextView) findViewById(R.id.headingtext);
        customer = (ImageButton) findViewById(R.id.customer);
        driver = (ImageButton) findViewById(R.id.driver);
        holder = (LinearLayout) findViewById(R.id.holder);
        customertext = (TextView) findViewById(R.id.customertext);
        officertext = (TextView) findViewById(R.id.officertext);
        openEmail = (TextView) findViewById(R.id.openEmail);

        YoYo.with(Techniques.FadeInLeft).duration(1500).playOn(mcustomer);
        YoYo.with(Techniques.FadeInRight).duration(1500).playOn(mDriver);
        YoYo.with(Techniques.FadeInUp).duration(1500).playOn(mmainimage);




        //////


        Intent getPoliceStation =  getIntent();
        if (getPoliceStation.hasExtra("policeStationName")){
            pilicetationName= getPoliceStation.getStringExtra("policeStationName");
            Toast.makeText(getApplicationContext(), pilicetationName + "settingsssssssssss", Toast.LENGTH_SHORT).show();
        }

        mgotoimagechat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            Intent intent = new Intent(getApplicationContext(), ImageChatActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
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
           gradianColor.setVisibility(View.GONE);
           mainimage.setVisibility(View.GONE);
           headingtext.setVisibility(View.GONE);
           customer.setVisibility(View.GONE);
           mcustomer.setVisibility(View.GONE);
           mDriver.setVisibility(View.GONE);
           mmainimage.setVisibility(View.GONE);
           driver.setVisibility(View.GONE);
           holder.setVisibility(View.GONE);
           customertext.setVisibility(View.GONE);
           officertext.setVisibility(View.GONE);
           openEmail.setVisibility(View.GONE);
           mcustomerText.setVisibility(View.GONE);
           moficcerText.setVisibility(View.GONE);
           mopenEmail.setVisibility(View.GONE);
           Toasty.error(getApplicationContext(), "No internet connection !", Toast.LENGTH_LONG, true).show();

       }else{
           noInterConnection.setVisibility(View.GONE);
           gradianColor.setVisibility(View.VISIBLE);
           mainimage.setVisibility(View.VISIBLE);
           headingtext.setVisibility(View.VISIBLE);
           customer.setVisibility(View.VISIBLE);
           driver.setVisibility(View.VISIBLE);
           holder.setVisibility(View.VISIBLE);
           customertext.setVisibility(View.VISIBLE);
           officertext.setVisibility(View.VISIBLE);
           openEmail.setVisibility(View.VISIBLE);
           mcustomer.setVisibility(View.VISIBLE);
           mDriver.setVisibility(View.VISIBLE);
           mmainimage.setVisibility(View.VISIBLE);
           mcustomerText.setVisibility(View.VISIBLE);
           moficcerText.setVisibility(View.VISIBLE);
           mopenEmail.setVisibility(View.VISIBLE);


       }
        Intent getPoliceStationName = getIntent();
if(getPoliceStationName.hasExtra("officerPoliceStation"))

{
    String policeStationName = getPoliceStationName.getStringExtra("officerPoliceStation");
    SharedPreferences sharedPreferences = getSharedPreferences(SHARED_REF, MODE_PRIVATE);
    SharedPreferences.Editor editor = sharedPreferences.edit();
    editor.putString(TEXT,policeStationName);
    editor.apply();
}
        mDriver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                    Intent intent = new Intent(MainActivity.this , DriverLogInActivity.class);

                    intent.putExtra("policeStationName" , pilicetationName);
                    Toast.makeText(getApplicationContext(), pilicetationName+"main" ,Toast.LENGTH_SHORT) .show();
                    startActivity(intent);
                    finish();
                    return;


            }
        });
       update();

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

    private void update() {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_REF, MODE_PRIVATE);
          pilicetationName =  sharedPreferences.getString(TEXT , "");
        Toast.makeText(getApplicationContext(), pilicetationName+"11" ,Toast.LENGTH_LONG).show();

    }


    Context context;
 private boolean is_Connected(){
     ConnectivityManager connectivityManager = (ConnectivityManager) getApplicationContext().getSystemService(context.CONNECTIVITY_SERVICE);
     return connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isConnectedOrConnecting();



 }

}
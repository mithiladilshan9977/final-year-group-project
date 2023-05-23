package com.example.myapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Map;

import es.dmoral.toasty.Toasty;

public class DriverLogInActivity extends AppCompatActivity {
 private Button mLogin , mRegistertion , mGoBack ;
    private EditText mEmail , mPassword , mReenterPassword, mNICnumber ,mEmailLogin, mPasswordLogin;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener firebasequthlistener;
    private TextView mVeifiedEmalText;
    TextView mpoliceStationName;
    LinearLayout mregisterLinerLayout, mloginLenerLayout;
    private ImageView mloginimage , msignupimage,mgotosettingpagebtn;
    private Spinner spinner;
    private  String dropDownValue,pilicetationName,policeStationName,pilicetationNamenew,statusOfUser;

    public  static final String SHARED_REF = "shared";
    public  static final String TEXT = "text";


    private DatabaseReference mDriverDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_log_in);
        mLogin = findViewById(R.id.loginButton);
        mRegistertion = findViewById(R.id.registerButton);

        mEmail = findViewById(R.id.email);
        mPassword = findViewById(R.id.password);
        mGoBack = (Button) findViewById(R.id.goback);
        mReenterPassword = (EditText) findViewById(R.id.reenterpassword);
        mEmailLogin = (EditText) findViewById(R.id.emailLogin);
        mPasswordLogin = (EditText) findViewById(R.id.passwordLogin);
        mNICnumber = (EditText) findViewById(R.id.nicnumber);
        mVeifiedEmalText = (TextView) findViewById(R.id.verifiedText);
        mAuth = FirebaseAuth.getInstance();
        mgotosettingpagebtn = findViewById(R.id.gotosettingpagebtn);
        mloginimage = (ImageView) findViewById(R.id.loginimage);
        msignupimage = (ImageView) findViewById(R.id.signupimage);

        mregisterLinerLayout = (LinearLayout) findViewById(R.id.registerLinerLayout);
        mloginLenerLayout = (LinearLayout) findViewById(R.id.loginLenerLayout);

        mregisterLinerLayout.setVisibility(View.VISIBLE);
        mloginLenerLayout.setVisibility(View.GONE);
        mpoliceStationName = findViewById(R.id.policeStationName);

        //images
        mloginimage.setVisibility(View.VISIBLE);
        msignupimage.setVisibility(View.GONE);

        spinner = findViewById(R.id.spinner);

        YoYo.with(Techniques.FadeInUp).duration(1500).playOn(mEmail);
        YoYo.with(Techniques.FadeInUp).duration(2000).playOn(mPassword);
        YoYo.with(Techniques.FadeInUp).duration(2500).playOn(mReenterPassword);



        //police stattions
        String[] policeStations = {"Select your police station","Nittambuwa" , "Mirihana", "Kaluthara" ,"Nittambuwa" , "Mirihana", "Kaluthara","Nittambuwa" , "Mirihana", "Kaluthara", "Mirihana", "Kaluthara"};

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(DriverLogInActivity.this ,R.layout.drop_drop_item,policeStations );
        adapter.setDropDownViewResource(R.layout.drop_drop_item);
        spinner.setAdapter(adapter);

        mgotosettingpagebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
                FirebaseUser currentUser = firebaseAuth.getCurrentUser();

                if (currentUser != null) {
                         Intent goSettings = new Intent(getApplicationContext(), driverSetting.class);
                         startActivity(goSettings);
                } else {
                     Toasty.info(getApplicationContext(),"Create an account first", Toasty.LENGTH_LONG).show();
                     return;
                }
            }
        });
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                  dropDownValue = parent.getItemAtPosition(position).toString();
//                Toast.makeText(getApplicationContext() , dropDownValue +" Police station", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


          Intent getIntent = getIntent();
          String policeName =getIntent.getStringExtra("policeStationName");



        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference().child("Users").child("Driver");



                    if (mAuth.getCurrentUser() != null && mAuth.getCurrentUser().isEmailVerified()  ){

                        Intent intent = new Intent(DriverLogInActivity.this, DriverMapsActivity.class);
                        intent.putExtra("policeStationName", policeName);

                        startActivity(intent);
                    }


        LoadingDialog loadingDialog =new LoadingDialog(DriverLogInActivity.this);
        firebasequthlistener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

            }
        };
        mGoBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
                finish();
                return;
            }
        });

//        update();
        mRegistertion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mregisterLinerLayout.setVisibility(View.VISIBLE);
                mloginLenerLayout.setVisibility(View.GONE);

                mloginimage.setVisibility(View.VISIBLE);
                msignupimage.setVisibility(View.GONE);


                final String email = mEmail.getText().toString();
                final String password = mPassword.getText().toString();
                final String reenterpassword = mReenterPassword.getText().toString();
                final String  nicNumber = mNICnumber.getText().toString();
                final String policeStation = dropDownValue.toString();

                if(email.isEmpty()){
                    mEmail.setError("Please enter Email");
                    mEmail.requestFocus();
                    return;
                }
                if (password.isEmpty()){
                    mPassword.setError("Enter Strong password");
                    mPassword.requestFocus();
                    return;
                }
                if(reenterpassword.isEmpty()){
                    mReenterPassword.setError("Reenter your password");
                    mReenterPassword.requestFocus();
                    return;
                }
                if (password.length() <=5 ){
                    mPassword.setError("Passwod is too short");
                    mPassword.requestFocus();
                    return;
                }
                if (password.length() != reenterpassword.length()){
                    mReenterPassword.setError("Password are not matching");
                    mReenterPassword.requestFocus();
                    return;
                }

                if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                    mEmail.setError("Invalid Email address");
                    mEmail.requestFocus();
                    return;
                }
                if(policeStation.equals("Select your police station"))
                {
                    Toast.makeText(getApplicationContext() , "Select a police station" , Toast.LENGTH_LONG).show();
                    return;

                }
                if(password.length() == reenterpassword.length() && Patterns.EMAIL_ADDRESS.matcher(email).matches()){

                    loadingDialog.stratAlertAnimation();

                }

                mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(DriverLogInActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(!task.isSuccessful()){
                            if(task.getException() instanceof FirebaseAuthUserCollisionException){
                                loadingDialog.stopAlert();
                                Toasty.error(getApplicationContext(), "You are already registered", Toast.LENGTH_LONG, true).show();

                                return;

                            }else{
                                Toasty.error(getApplicationContext(), task.getException().getMessage() , Toast.LENGTH_LONG, true).show();



                            }
                            Toasty.error(getApplicationContext(),"Check your internet connect" , Toast.LENGTH_LONG, true).show();


                        }else{
                            String user_id = mAuth.getCurrentUser().getUid();
                            DatabaseReference currnt_user_db = FirebaseDatabase.getInstance().getReference().child("Users").child("Driver").child(dropDownValue+user_id).child("email");

                            DatabaseReference curretUserNIC = FirebaseDatabase.getInstance().getReference().child("Users").child("Driver").child(dropDownValue+user_id) ;

                            DatabaseReference addStationName = FirebaseDatabase.getInstance().getReference().child("Users").child("Driver").child(user_id) ;
                            addStationName.child("stationLocation").setValue(dropDownValue);



                            final FirebaseUser user = mAuth.getCurrentUser();
                            user.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){
                                        loadingDialog.stopAlert();
                                        Toasty.success(getApplicationContext(),"Email has sent" , Toast.LENGTH_LONG, true).show();

                                        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_REF, MODE_PRIVATE);
                                        SharedPreferences.Editor editor = sharedPreferences.edit();
                                        editor.putString(TEXT,dropDownValue);
                                        editor.apply();

                                        mEmail.setText("");
                                        mPassword.setText("");
                                        mReenterPassword.setText("");
                                        mNICnumber.setText("");

                                        Intent intent = new Intent(DriverLogInActivity.this , driverSetting.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                        intent.putExtra("officerPoliceStation", dropDownValue);
                                        startActivity(intent);



                                    }else{
                                        Toasty.success(getApplicationContext(),"Email Not sent" , Toast.LENGTH_LONG, true).show();



                                    }
                                }
                            });

                            currnt_user_db.setValue(true);
                            curretUserNIC.setValue(true);
                        }
                    }
                });
            }
        });







        Intent getPoliceStationName = getIntent();
        String officerPoliceStationName = getPoliceStationName.getStringExtra("officerPoliceStation");

        mLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mregisterLinerLayout.setVisibility(View.GONE);
                mloginLenerLayout.setVisibility(View.VISIBLE);

                mloginimage.setVisibility(View.GONE);
                msignupimage.setVisibility(View.VISIBLE);

                final String email = mEmailLogin.getText().toString();
                final String passwordnew = mPasswordLogin.getText().toString();
                final FirebaseAuth newauth = FirebaseAuth.getInstance(); ;

                if (email.isEmpty()){
                    mEmailLogin.setError("Please enter your Email");
                    mEmailLogin.requestFocus();
                    return;
                }
                if(passwordnew.isEmpty()){
                    mPasswordLogin.setError("Password is empty");
                    mPasswordLogin.requestFocus();
                    return;
                }
                if(!email.isEmpty() && !passwordnew.isEmpty()){
                    loadingDialog.stratAlertAnimation();
                }

                mAuth.signInWithEmailAndPassword(email, passwordnew).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(!task.isSuccessful()){
                            loadingDialog.stopAlert();
                            Toasty.success(getApplicationContext(),task.getException().getMessage() , Toast.LENGTH_LONG, true).show();


                        }else {
                            if(newauth.getCurrentUser().isEmailVerified()){
                                loadingDialog.stopAlert();

                                SharedPreferences sharedPreferences = getSharedPreferences(SHARED_REF, MODE_PRIVATE);
                                pilicetationNamenew =  sharedPreferences.getString(TEXT , "");


                                DatabaseReference officerdatabase = database.getReference().child("Users").child("Driver").child(pilicetationNamenew+newauth.getCurrentUser().getUid());

                                officerdatabase.addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                                        Map<String, Object> map = (Map<String, Object>) snapshot.getValue();

                                        if(map.get("status") != null){
                                            statusOfUser = map.get("status").toString();
                                            if(statusOfUser.equals("unregistered"))
                                            {
//
                                                Toasty.info(getApplicationContext(),"Permission in not given yet", Toast.LENGTH_LONG, true).show();


                                            }else{


                                                Intent intent = new Intent(getApplicationContext() , DriverMapsActivity.class);
                                                intent.putExtra("policeStationName",pilicetationNamenew);
                                                startActivity(intent);

                                                Toasty.success(getApplicationContext(),pilicetationNamenew+" log in", Toast.LENGTH_LONG, true).show();

                                            }

                                        }

                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });


                            }else{
                                Toasty.info(getApplicationContext(),"Please verify the Email", Toast.LENGTH_LONG, true).show();
                                loadingDialog.stopAlert();



                            }

                        }
                    }
                });
            }
        });
    }

    private void update() {

            SharedPreferences sharedPreferences = getSharedPreferences(SHARED_REF, MODE_PRIVATE);
             pilicetationNamenew =  sharedPreferences.getString(TEXT , "");
             mpoliceStationName.setText(pilicetationNamenew);



    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(firebasequthlistener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mAuth.removeAuthStateListener(firebasequthlistener);
    }
}
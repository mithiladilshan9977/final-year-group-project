package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import es.dmoral.toasty.Toasty;

public class DriverLogInActivity extends AppCompatActivity {
 private Button mLogin , mRegistertion , mGoBack ;
    private EditText mEmail , mPassword , mReenterPassword, mNICnumber ,mEmailLogin, mPasswordLogin;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener firebasequthlistener;
    private TextView mVeifiedEmalText;
    LinearLayout mregisterLinerLayout, mloginLenerLayout;
    private ImageView mloginimage , msignupimage;
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

        mloginimage = (ImageView) findViewById(R.id.loginimage);
        msignupimage = (ImageView) findViewById(R.id.signupimage);

        mregisterLinerLayout = (LinearLayout) findViewById(R.id.registerLinerLayout);
        mloginLenerLayout = (LinearLayout) findViewById(R.id.loginLenerLayout);

        mregisterLinerLayout.setVisibility(View.VISIBLE);
        mloginLenerLayout.setVisibility(View.GONE);

        //images
        mloginimage.setVisibility(View.VISIBLE);
        msignupimage.setVisibility(View.GONE);

        if (mAuth.getCurrentUser() != null && mAuth.getCurrentUser().isEmailVerified()){
            Intent intent = new Intent(DriverLogInActivity.this, DriverMapsActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
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
                            DatabaseReference currnt_user_db = FirebaseDatabase.getInstance().getReference().child("Users").child("Driver").child(user_id).child("email");

                            DatabaseReference curretUserNIC = FirebaseDatabase.getInstance().getReference().child("Users").child("Driver").child(user_id).child("UserNIC").child(nicNumber);
                            final FirebaseUser user = mAuth.getCurrentUser();
                            user.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){
                                        loadingDialog.stopAlert();
                                        Toasty.success(getApplicationContext(),"Email has sent" , Toast.LENGTH_LONG, true).show();


                                        Intent intent = new Intent(DriverLogInActivity.this , MainActivity.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
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
                                Intent intent = new Intent(getApplicationContext() , DriverMapsActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(intent);
                                Toasty.success(getApplicationContext(),"Verified", Toast.LENGTH_LONG, true).show();




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
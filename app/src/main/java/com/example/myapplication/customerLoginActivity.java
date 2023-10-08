package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import javax.swing.Spring;

import es.dmoral.toasty.Toasty;

public class customerLoginActivity extends AppCompatActivity {

    private Button mLogin , mRegistertion , mGoBack;
    private EditText mEmail , mPassword, mReenterPassword, mNICnumber ,mEmailLogin, mPasswordLogin;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener firebasequthlistener;
    private ProgressBar mPrograssBar;

    private ImageView mloginimage , msignupimage;
    LinearLayout mregisterLinerLayout, mloginLenerLayout;
    private boolean checkEmail = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_login);
        mLogin = findViewById(R.id.loginButton);
        mRegistertion = findViewById(R.id.registerButton);

        mEmail = findViewById(R.id.email);
        mPassword = findViewById(R.id.password);
        mGoBack = (Button) findViewById(R.id.goback);
        mReenterPassword = (EditText) findViewById(R.id.reenterpassword);
        mEmailLogin = (EditText) findViewById(R.id.emailLogin);
        mPasswordLogin = (EditText) findViewById(R.id.passwordLogin);
        mNICnumber = (EditText) findViewById(R.id.nicnumber);

        mAuth = FirebaseAuth.getInstance();

        mregisterLinerLayout = (LinearLayout) findViewById(R.id.registerLinerLayout);
        mloginLenerLayout = (LinearLayout) findViewById(R.id.loginLenerLayout);

        mregisterLinerLayout.setVisibility(View.VISIBLE);
        mloginLenerLayout.setVisibility(View.GONE);

        //images
        mloginimage =    findViewById(R.id.loginimage);
        msignupimage =   findViewById(R.id.signupimage);

        mregisterLinerLayout.setVisibility(View.VISIBLE);
        mloginLenerLayout.setVisibility(View.GONE);




        LoadingDialog loadingDialog =new LoadingDialog(customerLoginActivity.this);


        firebasequthlistener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if(user != null &&  mAuth.getCurrentUser().isEmailVerified()){
                    Intent intent = new Intent(getApplicationContext() , customerMapActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                }
            }
        };

        mGoBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(customerLoginActivity.this, MainActivity.class);
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

                // Images
                mloginimage.setVisibility(View.VISIBLE);
                msignupimage.setVisibility(View.GONE);

                final String email = mEmail.getText().toString();
                final String password = mPassword.getText().toString();
                final String reenterpassword = mReenterPassword.getText().toString();
                final String nicNumber = mNICnumber.getText().toString();

                if (email.isEmpty()) {
                    mEmail.setError("Please enter Email");
                    mEmail.requestFocus();
                    return;
                }

                // Check password strength using PasswordStrengthMeter
                if (passwordStrengthMeter.getPasswordStrength() != PasswordStrengthMeter.Strong) {
                    mPassword.setError("Password must be strong (e.g., at least 8 characters, containing uppercase, lowercase, digit, and special character)");
                    mPassword.requestFocus();
                    return;
                }

                if (!password.equals(reenterpassword)) {
                    mReenterPassword.setError("Passwords do not match");
                    mReenterPassword.requestFocus();
                    return;
                }

                if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    mEmail.setError("Invalid Email address");
                    mEmail.requestFocus();
                    return;
                }

                // Continue with registration
                loadingDialog.stratAlertAnimation();
                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(customerLoginActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (!task.isSuccessful()) {
                                    if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                                        loadingDialog.stopAlert();
                                        Toasty.error(getApplicationContext(), "You are already registered", Toast.LENGTH_LONG, true).show();
                                    } else {
                                        Toasty.error(getApplicationContext(), task.getException().getMessage(), Toast.LENGTH_LONG, true).show();
                                    }
                                    Toasty.error(getApplicationContext(), "Check your internet connection", Toast.LENGTH_LONG, true).show();
                                } else {
                                    String user_id = mAuth.getCurrentUser().getUid();
                                    DatabaseReference currentUserDb = FirebaseDatabase.getInstance().getReference().child("Users").child("Customer").child(user_id);
                                    DatabaseReference currentUserNIC = FirebaseDatabase.getInstance().getReference().child("Users").child("Customer").child(user_id).child("UserNIC").child(nicNumber);
                                    final FirebaseUser user = mAuth.getCurrentUser();
                                    user.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                print("yex");
                                                Toasty.success(getApplicationContext(), "Email has been sent", Toast.LENGTH_LONG, true).show();
                                                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                                startActivity(intent);
                                            } else {
                                                print("email is not sent");
                                                Toasty.error(getApplicationContext(), "Email not sent", Toast.LENGTH_LONG, true).show();
                                            }
                                        }
                                    });
                                    currentUserDb.setValue(true);
                                    currentUserNIC.setValue(true);
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

                //images
                mloginimage.setVisibility(View.GONE);
                msignupimage.setVisibility(View.VISIBLE);


                final String email = mEmailLogin.getText().toString();
                final String password = mPasswordLogin.getText().toString();
                final FirebaseAuth newauth = FirebaseAuth.getInstance(); ;

                if (email.isEmpty()){
                    mEmailLogin.setError("Please enter your Email");
                    mEmailLogin.requestFocus();
                    return;
                }
                if(password.isEmpty()){
                    mPasswordLogin.setError("Password is empty");
                    mPasswordLogin.requestFocus();
                    return;
                }
                if(!email.isEmpty() && !password.isEmpty()){
                    loadingDialog.stratAlertAnimation();
                }

                mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(!task.isSuccessful()){

                            loadingDialog.stopAlert();
                            Toasty.error(getApplicationContext(),task.getException().getMessage(), Toast.LENGTH_LONG, true).show();



                        }
                        else{
                            if(newauth.getCurrentUser().isEmailVerified()){

                                loadingDialog.stopAlert();
                                Intent intent = new Intent(getApplicationContext() , customerMapActivity.class);
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
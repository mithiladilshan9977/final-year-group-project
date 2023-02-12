package com.example.myapplication;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;

import java.util.HashMap;
import java.util.Map;

public class driverSetting  extends AppCompatActivity {
    private EditText mnameField, mPhoneField , mCarField;
    private Button mBack, mConfirm;

    private FirebaseAuth mAuth;
    private DatabaseReference mDriverDatabase;
    private  String userid, mName , mPhone , mCar , mservice;
    private ImageView mProfileImage ;
    FirebaseStorage storage;
    Uri imageURI;
    RadioGroup mRadioGroup;
    LoadingDialog loadingDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_setting);

        mnameField = findViewById(R.id.name);
        mPhoneField = findViewById(R.id.phonenumebr);

        mBack = findViewById(R.id.back);
        mConfirm = findViewById(R.id.confirm);
        mCarField = findViewById(R.id.car);
        mProfileImage = findViewById(R.id.profileImage);

        mRadioGroup = findViewById(R.id.radioGroup);
        mRadioGroup.check(R.id.userX);

        mAuth = FirebaseAuth.getInstance();
        userid = mAuth.getCurrentUser().getUid();
        storage = FirebaseStorage.getInstance();
        loadingDialog = new LoadingDialog(driverSetting.this);

        mDriverDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("Driver").child(userid);
        getSaveInfor();

        ActivityResultLauncher<String> mGetContent = registerForActivityResult(new ActivityResultContracts.GetContent(), new ActivityResultCallback<Uri>() {
            @Override
            public void onActivityResult(Uri result) {
                if(result != null){
                    mProfileImage.setImageURI(result);
                    imageURI = result;
                }
            }
        });

        mProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mGetContent.launch("image/*");
            }
        });



        mConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadingDialog.stratAlertAnimation();
                saveUserInformation();

            }
        });


        mBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(driverSetting.this , DriverMapsActivity.class);
                startActivity(intent);
                finish();
                return;
            }
        });

    }



    private void getSaveInfor(){
        mDriverDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot datasnapshot) {
                if(datasnapshot.exists() && datasnapshot.getChildrenCount() > 0){
                    Map<String, Object> map = (Map<String, Object>) datasnapshot.getValue();
                    if(map.get("name") != null){
                        mName = map.get("name").toString();
                        mnameField.setText(mName);
                    }if(map.get("car") != null){
                        mCar = map.get("car").toString();
                        mCarField.setText(mCar);
                    }
                    if(map.get("phone") != null){
                        mPhone = map.get("phone").toString();
                        mPhoneField.setText(mPhone);
                    }
                    if(map.get("service") != null){
                        mservice = map.get("service").toString();
                        switch (mservice){
                            case "Police":
                                mRadioGroup.check(R.id.userX);
                                break;
                            case "Hospitel":
                                mRadioGroup.check(R.id.userblack);
                                break;
                            case "CEB":
                                mRadioGroup.check(R.id.userxl);
                                break;
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void saveUserInformation() {
        mName = mnameField.getText().toString();
        mPhone = mPhoneField.getText().toString();
        mCar = mCarField.getText().toString();


        if(mName.isEmpty()){
            mnameField.setError("Enter username");
            mnameField.requestFocus();
            loadingDialog.stopAlert();
            return;
        }
        if(mPhone.isEmpty()){
            mPhoneField.setError("Enter your phone number");
            mPhoneField.requestFocus();
            loadingDialog.stopAlert();
            return;
        }
        if(mCar.isEmpty()){
            mCarField.setError("Type your description");
            mCarField.requestFocus();
            loadingDialog.stopAlert();
            return;
        }

        int selectid = mRadioGroup.getCheckedRadioButtonId();
        final RadioButton radiobutton = (RadioButton) findViewById(selectid);
        if(radiobutton.getText() == null){
            return;
        }
        mservice = radiobutton.getText().toString();
        Map userInfo = new HashMap();
        userInfo.put("name", mName);
        userInfo.put("phone", mPhone);
        userInfo.put("car", mCar);
        userInfo.put("service", mservice);
        mDriverDatabase.updateChildren(userInfo);
        Intent intent = new Intent(driverSetting.this, DriverMapsActivity.class);
        startActivity(intent);
    }
}
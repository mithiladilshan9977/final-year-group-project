package com.example.myapplication;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Map;

import es.dmoral.toasty.Toasty;

public class customerSettingsActivity extends AppCompatActivity {
 private EditText mnameField, mPhoneField;
    private Button mBack, mConfirm;

    private FirebaseAuth mAuth;
    private DatabaseReference mCustomerDatabase;
    private  String userid, mName , mPhone , mProfile;
    private ImageView mProfileImage ;
    FirebaseStorage storage;
    Uri imageURI;
    LoadingDialog loadingDialog;
    FirebaseUser user;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_settings);

        mnameField = findViewById(R.id.name);
        mPhoneField = findViewById(R.id.phonenumebr);

        mBack = findViewById(R.id.back);
        mConfirm = findViewById(R.id.confirm);

        mProfileImage = findViewById(R.id.profileImage);
        user = FirebaseAuth.getInstance().getCurrentUser();
          loadingDialog = new LoadingDialog(customerSettingsActivity.this);

         mAuth = FirebaseAuth.getInstance();
         userid = mAuth.getCurrentUser().getUid();
          storage = FirebaseStorage.getInstance();

         mCustomerDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("Customer").child(userid);
        getSaveInfor();

         String newname = "mithila dilshan";
        if(user != null && mProfileImage != null){
            UserProfileChangeRequest profile = new UserProfileChangeRequest.Builder().setDisplayName(newname).setPhotoUri(Uri.parse(String.valueOf(imageURI))).build();

            user.updateProfile(profile).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){



                        }else {


                        }
                }
            });
        }
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
                AlertDialog.Builder builder = new AlertDialog.Builder(customerSettingsActivity.this);
                builder.setTitle("Choose option");

                String[] options = {"Open camera" , "Open Gallery"};
                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0){
                            try {
                                Intent intent = new Intent();
                                intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
                                startActivity(intent);
                            }catch (Exception e){
                                Toasty.error(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG, true).show();


                            }

                        }
                        if (which == 1){
                            mGetContent.launch("image/*");

                        }
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });







        mConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadingDialog.stratAlertAnimation();
                uploadImage();
                 saveUserInformation();

            }
        });


        mBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                return;
            }
        });

    }


    private void uploadImage() {


        if (imageURI != null){
            StorageReference reference = storage.getReference().child("profile_image").child(userid);

            reference.putFile(imageURI).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toasty.error(getApplicationContext(),"Image failed to upload", Toast.LENGTH_LONG, true).show();



                    finish();
                    return;
                }
            });
            reference.putFile(imageURI).continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }


                    return reference.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        //start from here
                        Uri downloadUri = task.getResult();
                        Map newImage = new HashMap();
                        newImage.put("profileImageUrl" , downloadUri.toString());
                        mCustomerDatabase.updateChildren(newImage);
                        Toasty.success(getApplicationContext(),"Image uploaded", Toast.LENGTH_LONG, true).show();

                         finish();
                        return;
                    } else {

                    }
                }
            });
        }else   {
            finish();
        }
    }


    private void getSaveInfor(){
        mCustomerDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot datasnapshot) {
         if(datasnapshot.exists() && datasnapshot.getChildrenCount() > 0){
             Map<String, Object> map = (Map<String, Object>) datasnapshot.getValue();
            if(map.get("name") != null){
                mName = map.get("name").toString();
                mnameField.setText(mName);
            }
             if(map.get("phone") != null){
                 mPhone = map.get("phone").toString();
                 mPhoneField.setText(mPhone);
             }

             if(map.get("profileImageUrl") != null){
                 mProfile = map.get("profileImageUrl").toString();

                 Glide.with(getApplicationContext()).load(mProfile).into(mProfileImage);
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

           Map userInfo = new HashMap();
           userInfo.put("name", mName);
           userInfo.put("phone", mPhone);
           mCustomerDatabase.updateChildren(userInfo).addOnCompleteListener(new OnCompleteListener() {
               @Override
               public void onComplete(@NonNull Task task) {
                   if (task.isSuccessful()){
                       Toasty.success(getApplicationContext(),"Updated", Toast.LENGTH_LONG, true).show();

                     loadingDialog.stopAlert();
                   }
               }
           });
          finish();
    }
}
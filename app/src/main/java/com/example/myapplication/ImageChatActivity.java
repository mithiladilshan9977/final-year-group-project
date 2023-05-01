package com.example.myapplication;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

import java.util.ArrayList;
import java.util.List;

import es.dmoral.toasty.Toasty;

public class ImageChatActivity extends AppCompatActivity {
private Button mopengallary,msendimage;
    Uri imageURI,downloadUri;
    DatabaseReference db,mCustomerDatabase,mDriverDatabase;
    String uid,DriverFoundId;

    StorageReference reference;
    FirebaseStorage storage;
    ImageView mProfileImage;
    FirebaseUser user;
    FirebaseAuth auth;

    RecycleViewAdapter adapter;
    ArrayList<Message> list;
    private List<String> imageList;
    ImageViewAdapter imageViewAdapter;
    RecyclerView recyclerView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_chat);

        mopengallary = findViewById(R.id.opengallary);
        msendimage = findViewById(R.id.sendimage);
        DriverFoundId="KBG6TSwPmqe4ZAIuTfaBTKEdVRr2";
        mProfileImage = (ImageView) findViewById(R.id.profileImage);
        storage = FirebaseStorage.getInstance();
        db = FirebaseDatabase.getInstance().getReference();
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        uid= user.getUid();
        recyclerView = findViewById(R.id.recycleview);
        list = new ArrayList<>();


        //image load code
        imageViewAdapter = new ImageViewAdapter(this, imageList);
        LinearLayoutManager llmanager = new LinearLayoutManager(this,RecyclerView.VERTICAL,false);
        recyclerView.setLayoutManager(llmanager);
        recyclerView.setAdapter(imageViewAdapter);

        Toast.makeText(getApplicationContext(), DriverFoundId, Toast.LENGTH_LONG).show();
        mDriverDatabase = FirebaseDatabase.getInstance().getReference().child("Messages").child(DriverFoundId).child(uid);

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

        mopengallary.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mGetContent.launch("image/*");

            }
        });

        msendimage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadImage();
            }
        });

    }



    private void uploadImage() {
        if (imageURI != null){

            reference = storage.getReference().child("profile_image_chat").child(uid);

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

                        downloadUri = task.getResult();

                        db.child("Messages").child(DriverFoundId).child("image").push().setValue(new ImageClass(downloadUri.toString())).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                Toasty.success(getApplicationContext(),"Image uploaded", Toast.LENGTH_LONG, true).show();

                            }
                        });


                        finish();
                        return;
                    } else {
                        Toasty.error(getApplicationContext(),"Image error", Toast.LENGTH_LONG, true).show();

                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toasty.success(getApplicationContext(),e.getMessage().toString(), Toast.LENGTH_LONG, true).show();

                }
            });
        }else   {

            finish();
        }
    }


    @Override
    protected void onStart() {
        super.onStart();

        loadImages();
    }
    private void loadImages() {

        db.child("Messages").child(DriverFoundId).child("image").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    if(imageList != null){
                        Toasty.success(getApplicationContext(),"not null", Toast.LENGTH_LONG, true).show();

                        imageList.clear();
                        for (DataSnapshot snap: snapshot.getChildren())
                        {
                            ImageClass imageClass = snap.getValue(ImageClass.class);
                            imageList.add(String.valueOf(imageClass));
                            imageViewAdapter.notifyDataSetChanged();

                        }

                    }else{
                        Toasty.success(getApplicationContext(),"  null", Toast.LENGTH_LONG, true).show();

                    }



                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

}
package com.example.myapplication;

import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class send_image extends AppCompatActivity {

    String uri,receiver_name,sender_id,receiver_uid;
    ImageView imageView;
    Uri imageUri , urii;
    ProgressBar progressBar;
    Button button;

    StorageReference storageReference;
    FirebaseStorage firebaseStorage;
    DatabaseReference rootRef1, rootRef2;

    FirebaseDatabase database = FirebaseDatabase.getInstance();




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_image);


    }
}
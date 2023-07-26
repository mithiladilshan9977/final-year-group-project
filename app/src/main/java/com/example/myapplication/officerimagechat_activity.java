package com.example.myapplication;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.github.chrisbanes.photoview.PhotoView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class officerimagechat_activity extends AppCompatActivity {


    private TextView miamgesendername;
    private ImageView mgoBackArrow;
    private String customerName,customerPhone;


    FloatingActionButton mopengallary,msendimage;
    Uri imageURI,downloadUri;
    DatabaseReference db,mCustomerDatabase,mDriverDatabase;
    String uid,DriverFoundId,driverName,driverPhone;

    StorageReference reference;
    FirebaseStorage storage;

    FirebaseUser user;
    FirebaseAuth auth;

    RecycleViewAdapter adapter;
    ArrayList<ImageClass> list;
    ArrayList<ImageClass> imageUris ;
    private List<String> imageList;
    ImageViewAdapter imageViewAdapter;
    RecyclerView recyclerView;
    private ProgressBar progressBar;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_officerimagechat);


        Intent intent = getIntent();

// Retrieve the extra values using the keys you used while putting them
          customerName = intent.getStringExtra("CUSTOMER_NAME");
          customerPhone = intent.getStringExtra("CUSTOMER_PHONE");
        Toast.makeText(this, customerName, Toast.LENGTH_SHORT).show();

        miamgesendername=findViewById(R.id.iamgesendername);
        miamgesendername.setText(customerName + " images");
        recyclerView = findViewById(R.id.recycleview);

        mgoBackArrow = findViewById(R.id.goBackArrow);
        db = FirebaseDatabase.getInstance().getReference();

        mgoBackArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent1 = new Intent(getApplicationContext(), OfficerChatActivity.class);
                intent1.putExtra("CUSTOMER_NAME", customerName);
                intent1.putExtra("CUSTOMER_PHONE", customerPhone);
                startActivity(intent1);
            }
        });

        imageList = new ArrayList<>(); // Initialize the imageList
        imageViewAdapter = new ImageViewAdapter(this, imageList);
        LinearLayoutManager llmanager = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        recyclerView.setLayoutManager(llmanager);
        recyclerView.setAdapter(imageViewAdapter);

        imageViewAdapter.setOnImageClickListener(new ImageViewAdapter.OnImageClickListener() {
            @Override
            public void onImageClick(String imageUri) {
                // Handle the click event to display the enlarged image
                showEnlargedImage(imageUri);
            }
        });




    }

    private void showEnlargedImage(String imageUri) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_enlarged_image, null);
        PhotoView imageView = view.findViewById(R.id.enlargedImageView);
        Glide.with(this)
                .load(imageUri)
                .into(imageView);

        builder.setView(view);
        builder.setPositiveButton("Close", null); // Add any button to close the dialog if needed
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    protected void onStart() {
        super.onStart();

        loadImages();
    }
    private void loadImages() {



        db.child("Messages").child("KBG6TSwPmqe4ZAIuTfaBTKEdVRr2").child("image").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){


                    List<String> imageUris = new ArrayList<>();

                    for (DataSnapshot snap : snapshot.getChildren()) {
                        ImageClass imageClass = snap.getValue(ImageClass.class);
                        if (imageClass != null) {
                            String imageUri = imageClass.getmProfile();
                            imageUris.add(imageUri);
                        }
                    }

                    imageList.clear(); // Clear the existing imageList
                    imageList.addAll(imageUris); // Update the imageList with the new data

                    // Notify the adapter that the data has changed
                    imageViewAdapter.notifyDataSetChanged();

                }else{
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getApplicationContext(), "Database Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

}
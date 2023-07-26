package com.example.myapplication;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.github.chrisbanes.photoview.PhotoView;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import es.dmoral.toasty.Toasty;

public class ImageChatActivity extends AppCompatActivity {

    FloatingActionButton mopengallary,msendimage;
    Uri imageURI,downloadUri;
    DatabaseReference db,mCustomerDatabase,mDriverDatabase;
    String uid,DriverFoundId,driverName,driverPhone;

    StorageReference reference;
    FirebaseStorage storage;
    ImageView mProfileImage,mgoBackArrow,movedown;
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
        mgoBackArrow = findViewById(R.id.goBackArrow);
        movedown = findViewById(R.id.movedown);

        //image load code
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



        Intent intent = getIntent();

        // Retrieve the data using the keys you used when adding them to the Intent
          driverName = intent.getStringExtra("DRIVER_NAME");
          driverPhone = intent.getStringExtra("DRIVER_PHONE");
          DriverFoundId = intent.getStringExtra("DRIVER_FOUND_ID");

        mgoBackArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext() , ShowUserChatPage.class);
                intent.putExtra("DRIVER_NAME",driverName);
                intent.putExtra("DRIVER_PHONE",driverPhone);
                intent.putExtra("DRIVER_FOUND_ID",DriverFoundId);

                startActivity(intent);
            }
        });

        movedown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int lastPosition = imageViewAdapter.getItemCount() - 1;
                if (lastPosition >= 0) {
                    recyclerView.smoothScrollToPosition(lastPosition);
                }
            }
        });







        Toast.makeText(getApplicationContext(), "aple ===" +driverName+"  "+driverPhone +"   "+DriverFoundId, Toast.LENGTH_LONG).show();
        mDriverDatabase = FirebaseDatabase.getInstance().getReference().child("Messages").child(DriverFoundId).child(uid);

        String newname = "mithila dilshan";
        mProfileImage.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);

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
                    mProfileImage.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);

                    mProfileImage.setImageURI(result);
                    imageURI = result;
                }
            }
        });

        mopengallary.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                AlertDialog.Builder builder = new AlertDialog.Builder(ImageChatActivity.this);
                builder.setTitle("Choose option");

                String[] options = {"Open camera" , "Open Gallery" };
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
                            mProfileImage.setVisibility(View.VISIBLE);
                            recyclerView.setVisibility(View.GONE);



                        }

                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();








            }
        });

        msendimage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadImage();
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


    private void uploadImage() {
        if (imageURI != null){
            mProfileImage.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);

            ProgressDialog progressDialog = new ProgressDialog(ImageChatActivity.this);
            progressDialog.setMessage("Uploading image...");
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();

            reference = storage.getReference().child("profile_image_chat").child(uid);

            reference.putFile(imageURI).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    progressDialog.dismiss();
                    Toasty.error(getApplicationContext(),"Image failed to upload", Toast.LENGTH_LONG, true).show();



                    finish();
                    return;
                }
            });
            reference.putFile(imageURI).continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        progressDialog.dismiss();
                        throw task.getException();
                    }


                    return reference.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        //start from here
                        progressDialog.dismiss();
                        downloadUri = task.getResult();

                        Calendar calendar = Calendar.getInstance();
                        Date currentDate = calendar.getTime();

                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        String formattedDate = dateFormat.format(currentDate);

                        String originalKey = db.child("Messages").child(DriverFoundId).push().getKey();
                        String modifiedKey =formattedDate+ "imagemessage-" + originalKey;

                        Map<String, Object> messageValues = new HashMap<>();
                        messageValues.put("mProfile", downloadUri.toString());


                        Map<String, Object> childUpdates = new HashMap<>();
                        childUpdates.put("/Messages/"+DriverFoundId+"/" + modifiedKey, messageValues);


                        db.updateChildren(childUpdates).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {

                                Toasty.success(getApplicationContext(),"Image uploaded", Toast.LENGTH_LONG, true).show();
                                int lastPosition = imageViewAdapter.getItemCount() - 1;
                                if (lastPosition >= 0) {
                                    recyclerView.smoothScrollToPosition(lastPosition);
                                }
//                             finish();

                            }
                        });


//                        finish();
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

        progressBar = findViewById(R.id.progressBar); // Initialize the ProgressBar

        progressBar.setVisibility(View.VISIBLE);

        db.child("Messages").child(DriverFoundId).child("image").addValueEventListener(new ValueEventListener() {
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


                    imageViewAdapter.notifyDataSetChanged();
                    progressBar.setVisibility(View.GONE);

                }else{
                    progressBar.setVisibility(View.GONE); // Hide the ProgressBar if there are no images
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressBar.setVisibility(View.GONE); // Hide the ProgressBar on cancellation
                Toast.makeText(getApplicationContext(), "Database Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

}
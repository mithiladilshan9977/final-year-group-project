package com.example.myapplication;

import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputLayout;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import es.dmoral.toasty.Toasty;

public class ShowUserChatPage extends AppCompatActivity {


    RecycleViewAdapter adapter;
    ArrayList<Message> list;
    ArrayList<ImageClass> imageList;
    ImageViewAdapter imageViewAdapter;
    RecyclerView recyclerView;

    TextInputLayout message;
    FloatingActionButton send;
    DatabaseReference db,mCustomerDatabase,mDriverDatabase;
    FirebaseAuth auth;
    FirebaseUser user;

    MediaPlayer mediaPlayer;
    String uid,DriverFoundId;

    private TextView mchatUserName , mchatUserPhone;
    private ImageView mgoback_arrow ,mcam_SendMessage,mProfileImage;



    FirebaseStorage storage;
    Uri imageURI;
    LoadingDialog loadingDialog;

    Button msend_poto;
     String checker = "";
     StorageReference reference;
     Uri downloadUri;


    private static final  int PICK_IMAGE = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_driver_chat_page);

        send = findViewById(R.id.fab_send);
        message = findViewById(R.id.message);
        db = FirebaseDatabase.getInstance().getReference();
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
          uid= user.getUid();
        String uEmail = user.getEmail();
        recyclerView = findViewById(R.id.recycleview);
        list = new ArrayList<>();
        mediaPlayer = MediaPlayer.create(this,R.raw.finalnotificationsound);
        mchatUserName = (TextView) findViewById(R.id.chatUserName);
        mchatUserPhone = (TextView) findViewById(R.id.chatUserPhone);
        mgoback_arrow = (ImageView) findViewById(R.id.goBackArrow);
        msend_poto = (Button) findViewById(R.id.send_poto);
        mcam_SendMessage = (ImageView) findViewById(R.id.cam_SendMessage);
        storage = FirebaseStorage.getInstance();
        mProfileImage = (ImageView) findViewById(R.id.profileImage);
        String timeStamp = new SimpleDateFormat("dd-MM-yy HH:mm a").format(Calendar.getInstance().getTime());

        Intent intent = getIntent();
        String DriverName = intent.getStringExtra("DRIVER_NAME");
        String DriverPhone = intent.getStringExtra("DRIVER_PHONE");
          DriverFoundId = intent.getStringExtra("DRIVER_FOUND_ID");

        mchatUserName.setText(DriverName);
        mchatUserPhone.setText("("+DriverPhone+")");

//        mCustomerDatabase = FirebaseDatabase.getInstance().getReference().child("Messages").child(DriverFoundId).child(uid);

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




        mcam_SendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ShowUserChatPage.this);
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



        msend_poto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadImage();


            };
        });



        mgoback_arrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent1 = new Intent(getApplicationContext(), customerMapActivity.class);
                intent1.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent1);

            }
        });

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                String msg = message.getEditText().getText().toString();
                String dateTime = "10:00:00 P.M";
              db.child("Messages").child(DriverFoundId).child("text").push().setValue(new Message(uEmail, msg, timeStamp )).addOnCompleteListener(new OnCompleteListener<Void>() {
                  @Override
                  public void onComplete(@NonNull Task<Void> task) {
                      message.getEditText().setText("");
                  }
              });
            }
        });
        adapter = new RecycleViewAdapter(this, list);
        LinearLayoutManager llm = new LinearLayoutManager(this,RecyclerView.VERTICAL,false);
        recyclerView.setLayoutManager(llm);
        recyclerView.setAdapter(adapter);


//        imageViewAdapter = new ImageViewAdapter(this, imageList);
//        LinearLayoutManager llmanager = new LinearLayoutManager(this,RecyclerView.VERTICAL,false);
//        recyclerView.setLayoutManager(llmanager);
//        recyclerView.setAdapter(imageViewAdapter);






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

//                        Map newImage = new HashMap();
//                        newImage.put("profileImageUrlChat" , downloadUri.toString());
//                        mDriverDatabase.updateChildren(newImage);

//                        Toasty.success(getApplicationContext(),"Image uploaded", Toast.LENGTH_LONG, true).show();



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


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==101){

            if(resultCode==RESULT_OK){
                Toasty.success(getApplicationContext(),"GPS is enabled", Toast.LENGTH_LONG, true).show();



            }if(resultCode==RESULT_CANCELED){
                Toasty.warning(getApplicationContext(),"GPS is cancelled", Toast.LENGTH_LONG, true).show();



            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        receiveMesages();
        loadImages();
    }

    private void loadImages() {
        db.child("Messages").child(DriverFoundId).child("image").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    if(imageList != null){
                        imageList.clear();
                        for (DataSnapshot snap: snapshot.getChildren())
                        {
                            ImageClass imageClass = snap.getValue(ImageClass.class);
                            imageList.add(imageClass);
                            imageViewAdapter.notifyDataSetChanged();

                        }

                    }



                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void receiveMesages(){
        db.child("Messages").child(DriverFoundId).child("text").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot datasnapshot) {

                list.clear();

                 for (DataSnapshot snap:datasnapshot.getChildren()){
                     mediaPlayer.start();
                     Message message = snap.getValue(Message.class);
                     list.add(message);
                     adapter.notifyDataSetChanged();
                 }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

}
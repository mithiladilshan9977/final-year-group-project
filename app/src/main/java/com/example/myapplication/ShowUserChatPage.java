package com.example.myapplication;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.GestureDetector;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
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

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import es.dmoral.toasty.Toasty;

public class ShowUserChatPage extends AppCompatActivity {

    private RecyclerView recyclerView;
    private MultiItemTypeAdapter adapter;
    private List<Object> list;
    private DatabaseReference db;



    EditText message;
    FloatingActionButton send,sendAudioButton;
    DatabaseReference  mCustomerDatabase,mDriverDatabase;
    FirebaseAuth auth;
    FirebaseUser user;

    MediaPlayer mediaPlayer;
    String uid,DriverFoundId;

    private TextView mchatUserName , mchatUserPhone;
    private ImageView mgoback_arrow ,mcam_SendMessage,mProfileImage;

    private EditText minputmessagefield;



    FirebaseStorage storage;
    Uri imageURI;
    LoadingDialog loadingDialog;

    Button msend_poto;
    String checker = "" ,uEmail;
    StorageReference reference;
    Uri downloadUri;
    private GestureDetector gestureDetector;

    private static final  int PICK_IMAGE = 1;
    private String audioFileName,DriverName,DriverPhone;
    private StorageReference audioStorageReference;
    private DatabaseReference audioDatabaseReference;
    private MediaRecorder mediaRecorder;
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private boolean isRecordingPermissionGranted = false;
    private ImageView recordStatusIcon,mprofileImagee;
    private TextView recordStatusText;
    private ImageButton recordPauseButton;
    private RelativeLayout mshowtheaudiorecoringui,mimageprevisrelativelayout;
    private Button msendimage;
    private boolean imageSet = false;
    private boolean audiorecordingbool = false;






    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_driver_chat_page);
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        uid= user.getUid();
          uEmail = user.getEmail();
        recyclerView = findViewById(R.id.recycleview);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        list = new ArrayList<>();
        adapter = new MultiItemTypeAdapter(list);
        recyclerView.setAdapter(adapter);

        // Initialize Firebase database reference
        db = FirebaseDatabase.getInstance().getReference();

        mediaPlayer = MediaPlayer.create(this,R.raw.finalnotificationsound);
        mchatUserName = (TextView) findViewById(R.id.chatUserName);
        mchatUserPhone = (TextView) findViewById(R.id.chatUserPhone);
        mgoback_arrow = (ImageView) findViewById(R.id.goBackArrow);
        msend_poto = (Button) findViewById(R.id.sendimage);
        mcam_SendMessage = (ImageView) findViewById(R.id.cam_SendMessage);
        storage = FirebaseStorage.getInstance();
        String timeStamp = new SimpleDateFormat("dd-MM-yy HH:mm a").format(Calendar.getInstance().getTime());

        mimageprevisrelativelayout = findViewById(R.id.imageprevisrelativelayout);
        mprofileImagee =findViewById(R.id.profileImagee);
        msendimage = findViewById(R.id.sendimage);



        sendAudioButton = findViewById(R.id.send_audio);
        send = findViewById(R.id.fab_send);
        message = findViewById(R.id.message);

        recordStatusIcon = findViewById(R.id.recordStatusIcon);
        recordStatusText = findViewById(R.id.recordStatusText);
        recordPauseButton = findViewById(R.id.recordPauseButton);
        mshowtheaudiorecoringui = findViewById(R.id.showtheaudiorecoringui);


        mimageprevisrelativelayout.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);

       // Replace "DriverFoundId" with the actual node you want to read messages from


        message.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                boolean isInputEmpty = charSequence.toString().trim().isEmpty();

                if (isInputEmpty) {
                    // If the input field is empty, show the mic icon Button and hide the send button
                    send.setVisibility(View.GONE);
                    sendAudioButton.setVisibility(View.VISIBLE);
                } else {
                    // If the input field has content, show the send button and hide the mic icon Button
                    send.setVisibility(View.VISIBLE);
                    sendAudioButton.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        gestureDetector = new GestureDetector(this, new MyGestureListener());
        sendAudioButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                // Show the popup menu for audio recording
                showAudioRecordingMenu();
                return true;
            }
        });


        Intent intent = getIntent();
        DriverName = intent.getStringExtra("DRIVER_NAME");
        DriverPhone = intent.getStringExtra("DRIVER_PHONE");
        DriverFoundId = intent.getStringExtra("DRIVER_FOUND_ID");

        mchatUserName.setText(DriverName);
        mchatUserPhone.setText("("+DriverPhone+")");


        mDriverDatabase = FirebaseDatabase.getInstance().getReference().child("Messages").child(DriverFoundId).child(uid);





        String newname = "mithila dilshan";
        if(user != null && mprofileImagee != null){
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
                     imageSet = true;
                    mimageprevisrelativelayout.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                    mprofileImagee.setImageURI(result);
                    imageURI = result;
                }
            }
        });




        mcam_SendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                AlertDialog.Builder builder = new AlertDialog.Builder(ShowUserChatPage.this);
                builder.setTitle("Choose option");

                String[] options = {"Open camera" , "Open Gallery" ,"go page"};
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
                            mimageprevisrelativelayout.setVisibility(View.VISIBLE);
                            recyclerView.setVisibility(View.GONE);



                        }if(which == 2){
                            Intent openimagechat = new Intent(getApplicationContext(),ImageChatActivity.class);
                            openimagechat.putExtra("DRIVER_NAME", DriverName);
                            openimagechat.putExtra("DRIVER_PHONE", DriverPhone);
                            openimagechat.putExtra("DRIVER_FOUND_ID", DriverFoundId);
                            startActivity(openimagechat);
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

        adapter.setOnImageClickListener(new ImageViewAdapter.OnImageClickListener() {
            @Override
            public void onImageClick(String imageUri) {
                // Handle the click event to display the enlarged image
                showEnlargedImage(imageUri);
            }
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
                mediaPlayer.start();

                String msg = message.getText().toString();
                if (msg.isEmpty()) {
                    // Show a toast message if the EditText is empty
                    Toast.makeText(ShowUserChatPage.this, "Please enter a message", Toast.LENGTH_SHORT).show();
                    return;
                }
                String dateTime = "10:00:00 P.M";


                android.icu.util.Calendar calendar = android.icu.util.Calendar.getInstance();
                Date currentDate = calendar.getTime();

                android.icu.text.SimpleDateFormat dateFormat = new android.icu.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String formattedDate = dateFormat.format(currentDate);

                String originalKey = db.child("Messages").child(DriverFoundId).push().getKey();
                String modifiedKey =formattedDate+ "textmessage-" + originalKey;

                Map<String, Object> messageValues = new HashMap<>();
                messageValues.put("userEmail", uEmail);
                messageValues.put("message", msg);
                messageValues.put("dateTime", formattedDate);

                Map<String, Object> childUpdates = new HashMap<>();
                childUpdates.put("/Messages/"+DriverFoundId+"/" + modifiedKey, messageValues);

                db.updateChildren(childUpdates).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        message.setText("");


                        int lastPosition = adapter.getItemCount() - 1;
                        if (lastPosition >= 0) {
                            recyclerView.smoothScrollToPosition(lastPosition);
                        }

                    }
                });
            }
        });
    }

    @Override
    public void onBackPressed() {
        if(imageSet){
            new AlertDialog.Builder(this)
                    .setTitle("Confirm Exit")
                    .setMessage("Are you sure you want to go back without saving the image?")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // If the user selects "Yes," go back to the previous screen
                            imageSet = false;
                            ShowUserChatPage.super.onBackPressed();
                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // If the user selects "No," dismiss the dialog and stay on the current screen
                            imageSet = false;

                            dialog.dismiss();
                        }
                    })
                    .show();
        }

       else if(audiorecordingbool){
            new AlertDialog.Builder(this)
                    .setTitle("Confirm Exit")
                    .setMessage("Are you sure you want to go back without saving the audio?")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // If the user selects "Yes," go back to the previous screen
                            ShowUserChatPage.super.onBackPressed();
                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // If the user selects "No," dismiss the dialog and stay on the current screen
                            dialog.dismiss();
                        }
                    })
                    .show();
        }
        else{
            super.onBackPressed();

        }
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
            mimageprevisrelativelayout.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);


            ProgressDialog progressDialog = new ProgressDialog(ShowUserChatPage.this);
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

                        android.icu.util.Calendar calendar = android.icu.util.Calendar.getInstance();
                        Date currentDate = calendar.getTime();

                        android.icu.text.SimpleDateFormat dateFormat = new android.icu.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
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
                                int lastPosition = adapter.getItemCount() - 1;
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

    private void showAudioRecordingMenu() {

        PopupMenu popupMenu = new PopupMenu(this, sendAudioButton);
        popupMenu.getMenuInflater().inflate(R.menu.audio_recording_menu, popupMenu.getMenu());

        // Set a click listener for menu items
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.start_recording:
                        // Start recording audio
                        startRecordingAudio();
                        return true;
                    case R.id.stop_recording:
                        // Stop recording audio
                        stopRecordingAudio();
                        return true;
                    case R.id.gotoaudioactivity:
                        Intent intent = new Intent(getApplicationContext(), customerAudioMessages.class);
                        intent.putExtra("DRIVER_NAME", DriverName);
                        intent.putExtra("DRIVER_PHONE", DriverPhone);
                        intent.putExtra("DRIVER_FOUND_ID", DriverFoundId);
                        startActivity(intent);
                    default:
                        return false;
                }
            }
        });

        // Show the popup menu
        popupMenu.show();


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

    private void stopRecordingAudio() {

        Toast.makeText(this, "Recording audio stopped", Toast.LENGTH_SHORT).show();
        mshowtheaudiorecoringui.setVisibility(View.GONE);
        audiorecordingbool = false;
        if (mediaRecorder != null) {
            mediaRecorder.stop();
            mediaRecorder.release();
            mediaRecorder = null;

            // Upload the recorded audio to Firebase Storage
            Uri audioUri = Uri.fromFile(new File(getAudioFilePath()));





            audioStorageReference.child(audioFileName).putFile(audioUri)
                    .addOnSuccessListener(taskSnapshot -> {
                        Task<Uri> downloadUriTask = taskSnapshot.getStorage().getDownloadUrl();

                        downloadUriTask.addOnSuccessListener(downloadUri -> {
                            String audioUrl = downloadUri.toString();

                            android.icu.util.Calendar calendar = android.icu.util.Calendar.getInstance();
                            Date currentDate = calendar.getTime();

                            android.icu.text.SimpleDateFormat dateFormat = new android.icu.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                            String formattedDate = dateFormat.format(currentDate);

                            String originalKey = db.child("Messages").child(DriverFoundId).push().getKey();
                            String modifiedKey =formattedDate+ "audiomessage-" + originalKey;

                            Map<String, Object> messageValues = new HashMap<>();
                            messageValues.put("audioUrl", audioUrl);


                            Map<String, Object> childUpdates = new HashMap<>();
                            childUpdates.put("/Messages/"+DriverFoundId+"/" + modifiedKey, messageValues);




                            db.updateChildren(childUpdates).addOnCompleteListener(task -> {
                                        if (task.isSuccessful()) {
                                            Toast.makeText(this, "Audio uploaded successfully", Toast.LENGTH_SHORT).show();
                                            int lastPosition = adapter.getItemCount() - 1;
                                            if (lastPosition >= 0) {
                                                recyclerView.smoothScrollToPosition(lastPosition);
                                            }
                                        } else {
                                            Toast.makeText(this, "Failed to upload audio", Toast.LENGTH_SHORT).show();
                                            int lastPosition = adapter.getItemCount() - 1;
                                            if (lastPosition >= 0) {
                                                recyclerView.smoothScrollToPosition(lastPosition);
                                            }
                                        }
                                    });
                        });
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, "Failed to upload audio", Toast.LENGTH_SHORT).show());
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        receiveMessagesFromDatabasee2();


    }



    private void startRecordingAudio() {
        checkRecordAudioPermission();

        if(isRecordingPermissionGranted){
            Toast.makeText(this, "sssssssssstarted", Toast.LENGTH_SHORT).show();

            audiorecordingbool = true;
            // Create a new unique filename for the audio
            audioFileName = "audio_" + System.currentTimeMillis() + ".3gp";

            recordStatusIcon.setImageResource(R.drawable.micicon);
            recordStatusText.setText("Recording...");
            recordPauseButton.setImageResource(R.drawable.cam_send);
            mshowtheaudiorecoringui.setVisibility(View.VISIBLE);
            // Initialize Firebase Storage and Database references
            audioStorageReference = FirebaseStorage.getInstance().getReference().child("audio");





            audioDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Messages").child(DriverFoundId);

            // Initialize MediaRecorder
            mediaRecorder = new MediaRecorder();
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            mediaRecorder.setOutputFile(getAudioFilePath());

            try {
                mediaRecorder.prepare();
                mediaRecorder.start();
            } catch (IOException e) {
                Toast.makeText(this, "Failed to start audio recording", Toast.LENGTH_SHORT).show();
            }
        }
        
    }

    private String getAudioFilePath() {
        File dir = getExternalFilesDir(Environment.DIRECTORY_MUSIC);
        if (dir != null && !dir.exists()) {
            dir.mkdirs();
        }
        return dir.getPath() + File.separator + audioFileName;
    }

    private void checkRecordAudioPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted, request the permission
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    REQUEST_RECORD_AUDIO_PERMISSION);
            Toast.makeText(this, "1111111111111111", Toast.LENGTH_SHORT).show();

        } else {
            // Permission is already granted, start audio recording
            isRecordingPermissionGranted = true;
            Toast.makeText(this, "222222222222222", Toast.LENGTH_SHORT).show();

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission is granted, start audio recording
                isRecordingPermissionGranted = true;
                startRecordingAudio();
            } else {
                // Permission is denied, show a message or take appropriate action
                Toast.makeText(this, "Audio recording permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void receiveMessagesFromDatabasee2() {
        // Assuming the database structure for "Messages" node is as follows:
        // Messages
        //   |- DriverFoundId
        //         |- text
        //         |- photo
        //         |- audio

        // Load text messages
        db.child("Messages").child(DriverFoundId ).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                list.clear();

                for (DataSnapshot snap : dataSnapshot.getChildren()) {
                          String key = snap.getKey();
                          if(key != null){
                              if (key.contains("textmessage-")) {
                                  Message message = snap.getValue(Message.class);
                                  list.add(message);
                              }
                              // Check if the key starts with "imagemessage"
                               else if (key.contains("imagemessage-")) {
                                  ImageClass image = snap.getValue(ImageClass.class);
                                  list.add(image);
                              } else if (key.contains("audiomessage")) {

                                    AudioUri  audioUri = snap.getValue(AudioUri.class);
                                     list.add(audioUri);

                                }
                          }


                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ShowUserChatPage.this, "Failed to load text messages", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private class MyGestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public void onLongPress(MotionEvent e) {
            // Handle the long press action
            showAudioRecordingMenu();
        }
    }
}

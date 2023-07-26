package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class customerAudioMessages extends AppCompatActivity {

    private String driverName,driverPhone,driverFoundId;
    private RecyclerView recyclerView;


    private AudioUriAdapter audioUriAdapter;

    private List<String> audioUriList;

    DatabaseReference db,mCustomerDatabase,mDriverDatabase;
    FirebaseAuth auth;
    FirebaseUser user;
    String uid,DriverFoundId;
    private ImageView mgoBackArrow;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_audio_messages);


        Intent intent = getIntent();
          driverName = intent.getStringExtra("DRIVER_NAME");
          driverPhone = intent.getStringExtra("DRIVER_PHONE");
          driverFoundId = intent.getStringExtra("DRIVER_FOUND_ID");


          recyclerView = findViewById(R.id.audiorecycleview);
          mgoBackArrow = findViewById(R.id.goBackArrow);





        audioUriList = new ArrayList<>();
        audioUriAdapter = new AudioUriAdapter(this, audioUriList);
        LinearLayoutManager llm = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        recyclerView.setLayoutManager(llm);
        recyclerView.setAdapter(audioUriAdapter);

        db = FirebaseDatabase.getInstance().getReference();
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        uid= user.getUid();



        loadAudio();

        mgoBackArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent1 = new Intent(getApplicationContext() ,ShowUserChatPage.class);
                intent1.putExtra("DRIVER_NAME", driverName);
                intent1.putExtra("DRIVER_PHONE", driverPhone);
                intent1.putExtra("DRIVER_FOUND_ID", driverFoundId);
                startActivity(intent1);
            }
        });


    }



    private void loadAudio() {



        db.child("Messages").child(driverFoundId).child("audio").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {



                for (DataSnapshot snap : snapshot.getChildren()) {
                    String audioUrl = snap.getValue(String.class);
                    if (audioUrl != null) {
                        audioUriList.add(audioUrl);
                    }
                }

                audioUriAdapter.notifyDataSetChanged();




            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle onCancelled if needed
            }
        });
    }
}
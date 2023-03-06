package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

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

public class OfficerChatActivity extends AppCompatActivity {

    DatabaseReference db;
    FirebaseAuth auth;
    FirebaseUser user;
    String uid ,customerName;
    RecycleViewAdapter adapter;
    RecyclerView recyclerView;
    ArrayList<Message> list;
    private TextView mcustomerName,mcustomerPhone;
    private ImageView mgoBackArrow;
    Message message;
    NotificationHelper notificationHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_officer_chat);


        db = FirebaseDatabase.getInstance().getReference();
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        uid= user.getUid();
        recyclerView = findViewById(R.id.recycleview);
        list = new ArrayList<>();
        mcustomerName = (TextView) findViewById(R.id.customerName);
        mcustomerPhone = (TextView) findViewById(R.id.customerPhone);
        mgoBackArrow = (ImageView) findViewById(R.id.goBackArrow);

        adapter = new RecycleViewAdapter(this, list);
        LinearLayoutManager llm = new LinearLayoutManager(this,RecyclerView.VERTICAL,false);
        recyclerView.setLayoutManager(llm);
        recyclerView.setAdapter(adapter);

        notificationHelper = new NotificationHelper(this);



        Intent intent = getIntent();
          customerName = intent.getStringExtra("CUSTOMER_NAME");
        String customerPhone = intent.getStringExtra("CUSTOMER_PHONE");
        mcustomerName.setText(customerName);
        mcustomerPhone.setText("("+customerPhone+")");

        mgoBackArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), DriverMapsActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        receiveMesages();
    }
    private void receiveMesages(){
        db.child("Messages").child(uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                list.clear();

                for (DataSnapshot snap:snapshot.getChildren()){
                      message = snap.getValue(Message.class);
                    notificationHelper.sendHighProrityNotification(customerName,message.getMessage(), DriverMapsActivity.class);

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
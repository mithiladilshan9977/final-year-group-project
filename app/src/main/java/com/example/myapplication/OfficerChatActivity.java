package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Map;

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
    FloatingActionButton  mgototheimagechat;

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
        mgototheimagechat = findViewById(R.id.opengallary) ;

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

        mgototheimagechat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent1 = new Intent(getApplicationContext(),officerimagechat_activity.class);
                intent1.putExtra("CUSTOMER_NAME", customerName);
                intent1.putExtra("CUSTOMER_PHONE", customerPhone);

                startActivity(intent1);
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        receiveMesages();
    }
    private void receiveMesages(){

        DatabaseReference getPoliceStation = FirebaseDatabase.getInstance().getReference().child("Users").child("Driver").child(uid);
        getPoliceStation.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot datasnapshot) {
                 if(datasnapshot.exists() && datasnapshot.getChildrenCount() > 0){
                     Map<String, Object> map = (Map<String, Object>) datasnapshot.getValue();
                     if(map.get("stationLocation") != null){
                        String policeStationLocation = map.get("stationLocation").toString();

                         Toast.makeText(getApplicationContext(),policeStationLocation+  "   " + uid + "  kikiki", Toast.LENGTH_LONG).show();

                         db.child("Messages").child(policeStationLocation+uid).child("text").addValueEventListener(new ValueEventListener() {
                                             @Override
                                             public void onDataChange(@NonNull DataSnapshot snapshot) {

                                                 list.clear();

                                                 for (DataSnapshot snap:snapshot.getChildren()){
                                                     message = snap.getValue(Message.class);
                                                     notificationHelper.sendHighProrityNotification(customerName,message.getMessage(), OfficerChatActivity.class);

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
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });



    }
}
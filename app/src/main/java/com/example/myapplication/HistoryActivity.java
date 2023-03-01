package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class HistoryActivity extends AppCompatActivity {
   private RecyclerView mHistoryRecycelView;
   private RecyclerView.Adapter mHistoryAdapter;
   private RecyclerView.LayoutManager mHistoryLayoutManager;
   private ImageView mgoBackArrow , mhistoryimage;
   private String customerOrDriverHere , userid,historyDriverPhone,historyDriverName,historyDriverDisprition;
   private TextView mnoHistoryText;
   private NestedScrollView mnestedScrollView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        mHistoryRecycelView = (RecyclerView) findViewById(R.id.historyRecyCleViewXML);
        mHistoryRecycelView.setNestedScrollingEnabled(false);
        mHistoryRecycelView.setHasFixedSize(true);

        mgoBackArrow = (ImageView) findViewById(R.id.goBackArrow);
        mhistoryimage = (ImageView) findViewById(R.id.historyimage);
        mnoHistoryText = (TextView) findViewById(R.id.noHistoryText);
        mnestedScrollView = (NestedScrollView) findViewById(R.id.nestedScrollView);

        mHistoryLayoutManager = new LinearLayoutManager(HistoryActivity.this);
        mHistoryRecycelView.setLayoutManager(mHistoryLayoutManager);
        mHistoryAdapter =new HistoryAdapter(getdatasethistory() , HistoryActivity.this);
        mHistoryRecycelView.setAdapter(mHistoryAdapter);

        customerOrDriverHere = getIntent().getExtras().getString("customerOrDriver");

        userid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        getUserHistoryIds();

         mgoBackArrow.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 Intent intent = new Intent(getApplicationContext(), customerMapActivity.class);
                 intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                 startActivity(intent);
             }
         });



    }

    private void getUserHistoryIds() {

        DatabaseReference userhistorydatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(customerOrDriverHere).child(userid).child("history");
        userhistorydatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot datasnapshot) {
                         if (datasnapshot.exists()){
                             for (DataSnapshot driverinfor: datasnapshot.getChildren() ){
                                     historyDriverName = driverinfor.child("driverName").getValue().toString();
                                     historyDriverPhone = driverinfor.child("driverPhone").getValue().toString();
                                     historyDriverDisprition = driverinfor.child("driverDiscription").getValue().toString();

//                                 FetchRiferInformation(driverinfor.getKey());
                                 mnoHistoryText.setVisibility(View.GONE);
                                 mhistoryimage.setVisibility(View.GONE);
                                 mnestedScrollView.setVisibility(View.VISIBLE);



                             }
                             HistoryObject obj  =new HistoryObject(historyDriverName, historyDriverPhone,historyDriverDisprition );
                             resultHistory.add(obj);
                             mHistoryAdapter.notifyDataSetChanged();
                         }else{
                             mnoHistoryText.setVisibility(View.VISIBLE);
                             mhistoryimage.setVisibility(View.VISIBLE);
                             mnestedScrollView.setVisibility(View.GONE);


                         }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void FetchRiferInformation(String rideKey) {
        DatabaseReference historydatabase = FirebaseDatabase.getInstance().getReference().child("history").child(rideKey);
        historydatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot datasnapshot) {
                if (datasnapshot.exists()){
                   String rideId = datasnapshot.getKey();
                   Long timeStamp = 0L;
                   for (DataSnapshot child: datasnapshot.getChildren()){
                        if (child.equals("driverName")){
//                              historyDriverName = child.getValue().toString();
                        }
                       if (child.equals("driverPhone")){
//                             historyDriverPhone = child.getValue().toString();
                       }


                       if (child.equals("timeStamp")){
                           timeStamp = Long.valueOf(child.getValue().toString());
                       }
                   }
                   //inside here getDate(timeStamp)


                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private String getDate(Long timeStamp) {
        Calendar cal = Calendar.getInstance(Locale.getDefault());
        cal.setTimeInMillis(timeStamp*1000);
        String date = DateFormat.format("dd-MM-yyyy hh:mm" , cal).toString();
        return date;
    }

    private ArrayList resultHistory =new ArrayList<HistoryObject>();
    private ArrayList<HistoryObject> getdatasethistory() {

        return resultHistory;
    }
}
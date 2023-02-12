package com.example.myapplication;

import android.os.Bundle;
import android.text.format.DateFormat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
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
   private String customerOrDriverHere , userid;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        mHistoryRecycelView = (RecyclerView) findViewById(R.id.historyRecyCleViewXML);
        mHistoryRecycelView.setNestedScrollingEnabled(false);
        mHistoryRecycelView.setHasFixedSize(true);

        mHistoryLayoutManager = new LinearLayoutManager(HistoryActivity.this);
        mHistoryRecycelView.setLayoutManager(mHistoryLayoutManager);
        mHistoryAdapter =new HistoryAdapter(getdatasethistory() , HistoryActivity.this);
        mHistoryRecycelView.setAdapter(mHistoryAdapter);

        customerOrDriverHere = getIntent().getExtras().getString("customerOrDriver");
        userid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        getUserHistoryIds();






    }

    private void getUserHistoryIds() {
        DatabaseReference userhistorydatabase = FirebaseDatabase.getInstance().getReference("Users").child(customerOrDriverHere).child(userid).child("history");
        userhistorydatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot datasnapshot) {
                         if (datasnapshot.exists()){
                             for (DataSnapshot history: datasnapshot.getChildren() ){
                                 FetchRiferInformation(history.getKey());
                             }
                         }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void FetchRiferInformation(String rideKey) {
        DatabaseReference historydatabase = FirebaseDatabase.getInstance().getReference("history").child(rideKey);
        historydatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot datasnapshot) {
                if (datasnapshot.exists()){
                   String rideId = datasnapshot.getKey();
                   Long timeStamp = 0L;
                   for (DataSnapshot child: datasnapshot.getChildren()){
                       if (child.equals("timeStamp")){
                           timeStamp = Long.valueOf(child.getValue().toString());
                       }
                   }
                    HistoryObject obj  =new HistoryObject(rideId, getDate(timeStamp));
                    resultHistory.add(obj);
                    mHistoryAdapter.notifyDataSetChanged();

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
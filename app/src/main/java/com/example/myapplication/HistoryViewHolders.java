package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class HistoryViewHolders  extends RecyclerView.ViewHolder implements View.OnClickListener {

    public TextView rideid;
    public TextView time;
    public HistoryViewHolders(@NonNull View itemView) {
        super(itemView);
        itemView.setOnClickListener(this);

        rideid = (TextView) itemView.findViewById(R.id.rideid);
        time = (TextView) itemView.findViewById(R.id.time);
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent(v.getContext(), historySingelActivity.class);
        Bundle b = new Bundle();
        b.putString("rideId", rideid.getText().toString());
        intent.putExtras(b) ;
        v.getContext().startActivity(intent);
    }
}

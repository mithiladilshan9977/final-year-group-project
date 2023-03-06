package com.example.myapplication;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class RecycleViewAdapter extends RecyclerView.Adapter<RecycleViewAdapter.ViewHolder> {
    private Context context;
    private ArrayList<Message> list;

    public RecycleViewAdapter(Context context, ArrayList<Message> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public RecycleViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view    = LayoutInflater.from(context).inflate(R.layout.message_design, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecycleViewAdapter.ViewHolder holder, int position) {

        holder.username.setText(list.get(position).getUserEmail());
        holder.message.setText(list.get(position).getMessage());
        holder.dateTime.setText(list.get(position).getDateTime());



    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView username, message, dateTime;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            username = itemView.findViewById(R.id.user_email);
            message = itemView.findViewById(R.id.user_message);
            dateTime = itemView.findViewById(R.id.user_message_date_time);
        }
    }
}

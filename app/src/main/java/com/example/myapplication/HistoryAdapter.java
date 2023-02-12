package com.example.myapplication;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryViewHolders> {
    private List<HistoryObject> itemList;
    private Context context;


                    public HistoryAdapter(List<HistoryObject> itemList , Context context){
                        this.itemList = itemList;
                        this.context = context;

                    }

    @Override
    public HistoryViewHolders onCreateViewHolder(ViewGroup parent, int viewType) {

        View layoutview = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_history , null ,false);
         RecyclerView.LayoutParams lp = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
         layoutview.setLayoutParams(lp);
         HistoryViewHolders rcv = new HistoryViewHolders(layoutview);
        return rcv;
    }

    @Override
    public void onBindViewHolder( HistoryViewHolders holder, int position) {
    holder.rideid.setText(itemList.get(position).getRideid());
        holder.time.setText(itemList.get(position).getTime());

    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }
}

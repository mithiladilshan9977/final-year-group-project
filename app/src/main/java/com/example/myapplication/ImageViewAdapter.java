package com.example.myapplication;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class ImageViewAdapter extends RecyclerView.Adapter<ImageViewAdapter.ImageHolder>  {

    private Context newcontext;
    private ArrayList<ImageClass> list;
    ImageView mProfile;



    public ImageViewAdapter(Context newcontext, ArrayList<ImageClass> list) {
        this.newcontext = newcontext;
        this.list = list;
    }




    @NonNull
    @Override
    public ImageViewAdapter.ImageHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view    = LayoutInflater.from(newcontext).inflate(R.layout.image_message_design, parent, false);
        return new ImageHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewAdapter.ImageHolder holder, int position) {

        Glide.with(newcontext.getApplicationContext()).load(list.get(position).getmProfile()).into(mProfile);
    }

    @Override
    public int getItemCount() {
 if (list != null){
     return list.size();
 }else{
     return 0;
 }
    }

    public class ImageHolder extends RecyclerView.ViewHolder {

        public ImageHolder(@NonNull View itemView) {
            super(itemView);
            mProfile = itemView.findViewById(R.id.sent_image);

        }
    }
}
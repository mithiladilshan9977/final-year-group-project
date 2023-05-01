package com.example.myapplication;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.List;

public class ImageViewAdapter extends RecyclerView.Adapter<ImageViewAdapter.ImageHolder>  {


    ImageView mProfile;

    private Context context;
    private List<String> imageUris;




    public ImageViewAdapter( Context context, List<String> imageUris) {
        this.context = context;
        this.imageUris = imageUris;
    }




    @NonNull
    @Override
    public ImageViewAdapter.ImageHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.image_message_design, parent, false);

        return new ImageHolder(view);
    }



    @Override
    public void onBindViewHolder(@NonNull ImageHolder holder, int position) {

        String imageUri = imageUris.get(position);
        Picasso.get().load(imageUri).into(holder.imageView);


    }

    public int getItemCount() {
        if (imageUris != null) {
            int size = imageUris.size();
            return size;
        }else{
            Toast.makeText(context.getApplicationContext(), "null",Toast.LENGTH_LONG).show();

            return 0;
        }

    }


    public class ImageHolder extends RecyclerView.ViewHolder {

        ImageView imageView;

        public ImageHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.sent_image);



        }
    }
}



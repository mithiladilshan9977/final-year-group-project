package com.example.myapplication;

import android.media.MediaPlayer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.io.IOException;
import java.util.List;

public class MultiItemTypeAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_TEXT_MESSAGE = 1;
    private static final int VIEW_TYPE_PHOTO = 2;
    private static final int VIEW_TYPE_AUDIO = 3;
    private MediaPlayer mediaPlayer;
    private int playingPosition = -1;

    private static List<Object> items;

    private static ImageViewAdapter.OnImageClickListener onImageClickListener;

    public void setOnImageClickListener(ImageViewAdapter.OnImageClickListener listener) {
        this.onImageClickListener = listener;
    }


    public interface OnImageClickListener {
        void onImageClick(String imageUri);
    }


    public MultiItemTypeAdapter(List<Object> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        Log.d("hhhhhhhhhhhhhhhhhhhh" , String.valueOf(viewType)) ;
        // Inflate different layouts based on view type
        if (viewType == VIEW_TYPE_TEXT_MESSAGE) {
            View textView = inflater.inflate(R.layout.message_design, parent, false);

            return new TextMessageViewHolder(textView);
        } else if (viewType == VIEW_TYPE_PHOTO) {
            View photoView = inflater.inflate(R.layout.image_message_design, parent, false);

            return new PhotoItemViewHolder(photoView);

        } else if (viewType == VIEW_TYPE_AUDIO) {
            View audioView = inflater.inflate(R.layout.audio_message_item, parent, false);

            return new AudioItemViewHolder(audioView);
        }

        throw new IllegalArgumentException("Invalid view type");

//        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        Object item = items.get(position);

        // Check the view type and bind data accordingly
        if (holder instanceof TextMessageViewHolder && item instanceof Message) {
            TextMessageViewHolder textMessageViewHolder = (TextMessageViewHolder) holder;
            Message textMessage = (Message) item;

            textMessageViewHolder.username.setText(textMessage.getUserEmail());
            textMessageViewHolder.message.setText(textMessage.getMessage());
            textMessageViewHolder.dateTime.setText(textMessage.getDateTime());



        } else if (holder instanceof PhotoItemViewHolder && item instanceof ImageClass) {
            PhotoItemViewHolder photoItemViewHolder = (PhotoItemViewHolder) holder;
            ImageClass photoItem = (ImageClass) item;

            // Bind image data to the views using Glide
            Glide.with(photoItemViewHolder.itemView.getContext())
                    .load(photoItem.getmProfile())
                    .into(photoItemViewHolder.imageView);
            photoItemViewHolder.textView.setText("Image");




        } else if (holder instanceof AudioItemViewHolder && item instanceof AudioUri) {


            AudioItemViewHolder audioItemViewHolder = (AudioItemViewHolder) holder;
            AudioUri audioItem = (AudioUri) item;


            audioItemViewHolder.playaudiofile.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    playAudio(audioItem.getAudioUrl(), audioItemViewHolder);
                }
            });
            audioItemViewHolder.stopaudioicon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    stopAudio();
                }
            });

            // Handle audio playback state
            if (position == playingPosition) {
                audioItemViewHolder.playaudiofile.setVisibility(View.GONE);
                audioItemViewHolder.stopaudioicon.setVisibility(View.VISIBLE);
            } else {
                audioItemViewHolder.playaudiofile.setVisibility(View.VISIBLE);
                audioItemViewHolder.stopaudioicon.setVisibility(View.GONE);
            }





        }


    }

    private void playAudio(String audioUrl, AudioItemViewHolder holder) {

        stopAudio();
        mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(audioUrl);
            mediaPlayer.prepare();
            mediaPlayer.start();
            playingPosition = holder.getAdapterPosition();
            notifyDataSetChanged();

            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    stopAudio();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void stopAudio() {


        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
            playingPosition = -1;
            notifyDataSetChanged();
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }
    @Override
    public int getItemViewType(int position) {
        Object item = items.get(position);
        if (item instanceof Message) {

            return VIEW_TYPE_TEXT_MESSAGE;
        } else if (item instanceof ImageClass) {

            return VIEW_TYPE_PHOTO;
        } else if (item instanceof AudioUri) {

            return VIEW_TYPE_AUDIO;
        }
        return -1;

    }

    private static class TextMessageViewHolder extends RecyclerView.ViewHolder {

        TextView username, message, dateTime;
        public TextMessageViewHolder(@NonNull View itemView) {
            super(itemView);

            username = itemView.findViewById(R.id.user_email);
            message = itemView.findViewById(R.id.user_message);
            dateTime = itemView.findViewById(R.id.user_message_date_time);
        }

    }

    private static class PhotoItemViewHolder extends RecyclerView.ViewHolder {

        ImageView imageView;
        TextView textView;
        ImageViewAdapter imageViewAdapter;

        public PhotoItemViewHolder(@NonNull View itemView) {
            super(itemView);

            imageView = itemView.findViewById(R.id.sent_image);
            textView = itemView.findViewById(R.id.image_text);

            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onImageClickListener != null){
                        int position = getAdapterPosition();
                               if(position != RecyclerView.NO_POSITION){
                                   Object item = items.get(position);
                                   if (item instanceof ImageClass) {
                                       ImageClass imageItem = (ImageClass) item;
                                       String imageUri = imageItem.getmProfile();
                                       onImageClickListener.onImageClick(imageUri);
                                   }
                               }
                    }
                }
            });

        }

    }

    private static class AudioItemViewHolder extends RecyclerView.ViewHolder {
        ImageView playaudiofile, stopaudioicon;
        TextView filesize1,audiotimeduration1;
        public AudioItemViewHolder(@NonNull View itemView) {
            super(itemView);
            filesize1 = itemView.findViewById(R.id.filesize1);
            audiotimeduration1 = itemView.findViewById(R.id.audiotimeduration1);
            playaudiofile = itemView.findViewById(R.id.playaudiofile);
            stopaudioicon = itemView.findViewById(R.id.stopaudiofile);

        }

    }
}

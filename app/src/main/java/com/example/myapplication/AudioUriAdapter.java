package com.example.myapplication;


import android.content.Context;
import android.media.MediaPlayer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class AudioUriAdapter extends RecyclerView.Adapter<AudioUriAdapter.ViewHolder>{

    private Context context;
    private List<String> audioUriList;

    private MediaPlayer mediaPlayer;
    private int playingPosition = -1;

    public AudioUriAdapter(Context context, List<String> audioUriList) {
        this.context = context;
        this.audioUriList = audioUriList;
    }



    @NonNull
    @Override
    public AudioUriAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.audio_message_item, parent, false);
        return new    ViewHolder(view);


    }

    @Override
    public void onBindViewHolder(@NonNull AudioUriAdapter.ViewHolder holder,  final int position) {
        String audioUrl = audioUriList.get(position);

        if (position == playingPosition) {
            holder.playaudiofile.setVisibility(View.GONE);
            holder.stopaudioicon.setVisibility(View.VISIBLE);
        } else {
            holder.playaudiofile.setVisibility(View.VISIBLE);
            holder.stopaudioicon.setVisibility(View.GONE);
        }

        holder.playaudiofile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playAudio(holder.getAdapterPosition(), holder);
            }
        });

        holder.stopaudioicon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopAudio(holder);
            }
        });




    }

    private void stopAudio(ViewHolder holder) {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
            playingPosition = -1;
            notifyDataSetChanged();
        }
    }

    private void playAudio(int position, ViewHolder holder) {

        if (mediaPlayer != null) {
            mediaPlayer.release();
        }

        mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(audioUriList.get(position));
            mediaPlayer.prepare();
            mediaPlayer.start();
            playingPosition = position;
            notifyDataSetChanged();
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    stopAudio(holder);
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private long getFileSize(String audioUrl) {
        File audioFile = new File(audioUrl);
        return audioFile.length();
    }


    public int getItemCount() {
        return audioUriList.size();

    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        Button audioPlayButton; // Button to play audio
        TextView filesize1,audiotimeduration1;
        ImageView playaudiofile,stopaudioicon;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            audioPlayButton = itemView.findViewById(R.id.audio_play_button);
            filesize1 = itemView.findViewById(R.id.filesize1);
            audiotimeduration1 = itemView.findViewById(R.id.audiotimeduration1);
            playaudiofile = itemView.findViewById(R.id.playaudiofile);
            stopaudioicon = itemView.findViewById(R.id.stopaudiofile);


        }
    }
}

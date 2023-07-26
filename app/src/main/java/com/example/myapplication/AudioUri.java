package com.example.myapplication;

public class AudioUri   {

    private String audioUrl;

    public AudioUri() {
        // Required empty constructor for Firebase database
    }

    public AudioUri(String audioUrl) {
        this.audioUrl = audioUrl;
    }

    public void setAudioUrl(String audioUrl) {
        this.audioUrl = audioUrl;
    }
    public String getAudioUrl() {
        return audioUrl;
    }


}

package com.example.myapplication;

public class HistoryObject {
    private String rideid;
    private String time;

    public HistoryObject(String rideid , String time){
        this.rideid = rideid;
        this.time = time;
    }



    public String getRideid(){
        return rideid;
    }

    public void setRideid(String rideid) {
        this.rideid = rideid;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}

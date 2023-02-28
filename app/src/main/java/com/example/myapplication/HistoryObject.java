package com.example.myapplication;

public class HistoryObject {
    private String rideid, phone,discription;
    private String time;

    public HistoryObject(String rideid , String time, String discription){
        this.rideid = rideid;
        this.time = time;
        this.discription = discription;
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

    public String getDiscription() {
        return discription;
    }

    public void setDiscription(String time) {
        this.discription = discription;
    }
}

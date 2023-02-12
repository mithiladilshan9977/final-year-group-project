package com.example.myapplication;

import android.app.Activity;
import android.app.AlertDialog;
import android.view.LayoutInflater;

public class LoadingDialog {
    Activity activity;
    AlertDialog alertDialog;

    public LoadingDialog(Activity myActivity) {
        activity = myActivity;
    }

    public void stratAlertAnimation(){
        AlertDialog.Builder  builder= new AlertDialog.Builder(activity);
        LayoutInflater inflater = activity.getLayoutInflater();
        builder.setView(inflater.inflate(R.layout.custome_doalog, null));
        builder.setCancelable(false);

        alertDialog = builder.create();
        alertDialog.show();

    }
    public void stopAlert(){
        alertDialog.dismiss();
    }
}

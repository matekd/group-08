package com.example.myapplication;

import android.content.Context;
import android.widget.Toast;

public class Toasty {

    private String toastText;
    private Context toastContext;
    private int toastDuration;

    public Toasty(Context toastContext) {
        this.toastText = "";
        this.toastContext = toastContext;
        this.toastDuration = Toast.LENGTH_SHORT;
    }

    public void pleaseConnect() {
        toastText = "Please connect to smart car";
        Toast.makeText(toastContext, toastText, toastDuration);
    }

}

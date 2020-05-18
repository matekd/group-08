package com.example.myapplication;

import android.content.Context;
import android.widget.Toast;

public class Toasty {

    // I couldn't get this class to work with MainActivity. The app crashes.
    // If anyone can spot my mistake, feel free to fix :)

    private String toastText;
    private Context toastContext;
    private int toastDuration;

    public Toasty(Context toastContext) {
        this.toastText = "";
        this.toastContext = toastContext;
        this.toastDuration = Toast.LENGTH_SHORT;
    }

    public void pleaseConnectCar() {
        toastText = "Please connect to smart car";
        Toast.makeText(toastContext, toastText, toastDuration).show();
    }

    public void pleaseConnectHeadset() {
        toastText = "Please connect to devices";
        Toast.makeText(toastContext, toastText, toastDuration).show();
    }

    private void pleaseConnectDevices() {
        String toastString = "Please connect hardware";
        Toast.makeText(toastContext, toastString, Toast.LENGTH_SHORT).show();
    }

}

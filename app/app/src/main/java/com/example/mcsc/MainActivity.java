package com.example.mcsc;

import androidx.appcompat.app.AppCompatActivity;

// import com.neurosky.connection.*; (TODO: Commented out for build to run)
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.net.Uri; //for hyperlink in url
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;


import java.io.IOException;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    TextView myLabel;
    BluetoothAdapter mBluetoothAdapter;
    BluetoothSocket mmSocket;
    BluetoothDevice mmDevice;

    OutputStream mmOutputStream;

    // Buttons for external connections
    Button connectCar = (Button) findViewById(R.id.connectCarBtn);
    Button connectHeadset = (Button) findViewById(R.id.connectHeadsetBtn);

    // Buttons to switch app UI state
    Button partial = (Button) findViewById(R.id.partialBtn);
    Button total = (Button) findViewById(R.id.totalBtn);
    Button manual = (Button) findViewById(R.id.manualBtn);

    // Car control buttons
    ImageButton forward = (ImageButton) findViewById(R.id.forwardBtn);
    ImageButton backward = (ImageButton) findViewById(R.id.backwardBtn);
    ImageButton left = (ImageButton) findViewById(R.id.leftBtn);
    ImageButton right = (ImageButton) findViewById(R.id.rightBtn);

    // EEG layout id for visibility regulation
    RelativeLayout eegView = (RelativeLayout) findViewById(R.id.eegLayout);

    public void hideControls() {
        forward.setVisibility(View.GONE);
        backward.setVisibility(View.GONE);
        left.setVisibility(View.GONE);
        right.setVisibility(View.GONE);
    }

    public void showControls() {
        forward.setVisibility(View.VISIBLE);
        backward.setVisibility(View.VISIBLE);
        left.setVisibility(View.VISIBLE);
        right.setVisibility(View.VISIBLE);
    }

    public void hideEeg() {eegView.setVisibility(View.GONE);}

    public void showEeg() {eegView.setVisibility(View.VISIBLE);}

    public void showTotalPartialBtns() {
        manual.setVisibility(View.GONE);

        total.setVisibility(View.VISIBLE);
        partial.setVisibility(View.VISIBLE);
    }

    public void showTotalManualBtns() {
        partial.setVisibility(View.GONE);

        total.setVisibility(View.VISIBLE);
        manual.setVisibility(View.VISIBLE);
    }

    public void showManualPartialBtns() {
        total.setVisibility(View.GONE);

        manual.setVisibility(View.VISIBLE);
        partial.setVisibility(View.VISIBLE);
    }



    // Method to take user to external github page in browser
    private void goToUrl(View view) {
        String url = "https://github.com/DIT112-V20/group-08";
        Uri uriUrl = Uri.parse(url);
        Intent launchBrowser = new Intent(Intent.ACTION_VIEW, uriUrl);
        startActivity(launchBrowser);
    }
    
    //connect 8 directions button with the car
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Logic for changing app UI states
        partial.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTotalManualBtns();
                showEeg();
                showControls();
            }
        });
        total.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showManualPartialBtns();
                showEeg();
                hideControls();
            }
        });
        manual.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTotalPartialBtns();
                hideEeg();
                showControls();
            }
        });

        // myLabel = (TextView) findViewById(R.id.myLabel); (TODO: Commented out for build to run)
        
        connectCar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    findCarBT();
                    openCarBT();
                } catch (IOException ex) {
                }
            }
        });
        forward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    goForward();
                } catch (IOException ex) {
                }
            }
        });
        left.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    goLeft();
                } catch (IOException ex) {
                }
            }
        });
        right.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    goRight();
                } catch (IOException ex) {
                }
            }
        });
        backward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    goBack();
                } catch (IOException ex) {
                }
            }
        });
    }
    
    //find car bluetooth and respond through text if no bluetooth found.
    void findCarBT() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            myLabel.setText("No bluetooth adapter available");
        }

        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBluetooth, 0);
        }

        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                if (device.getName().equals("Car"))
                {
                    mmDevice = device;
                    myLabel.setText("Bluetooth Device Found");
                    break;
                } else {
                    myLabel.setText("Bluetooth Device NOT Found");
                }
            }
        }

    }
 
    //open car bluetooth 
    void openCarBT() throws IOException {
        UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
        if (mmDevice != null) {
            mmSocket = mmDevice.createRfcommSocketToServiceRecord(uuid);
            mmSocket.connect();
            mmOutputStream = mmSocket.getOutputStream();

        }
    }
    
    // The getBytes() method encodes a given String into a sequence of bytes and returns an array of bytes. 
    // 8 directons proceeding methods
    void goForward() throws IOException {
        String msg = "f";
        mmOutputStream.write(msg.getBytes());
        myLabel.setText("Going Forward!");
    }
    void goBack() throws IOException {
        String msg = "b";
        mmOutputStream.write(msg.getBytes());
        myLabel.setText("Going Backwards!");
    }
    void goLeft() throws IOException {
        String msg = "l";
        mmOutputStream.write(msg.getBytes());
        myLabel.setText("Going Left!");
    }
    void goRight() throws IOException {
        String msg = "r";
        mmOutputStream.write(msg.getBytes());
        myLabel.setText("Going Right!");
    }
}

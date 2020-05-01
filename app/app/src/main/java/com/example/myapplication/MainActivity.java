package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.net.Uri; //for hyperlink in url
import android.bluetooth.BluetoothAdapter;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Button;

import java.io.OutputStream;
import java.util.Set;
import java.io.IOException;
import java.util.UUID;

import com.neurosky.connection.ConnectionStates;
import com.neurosky.connection.TgStreamHandler;
import com.neurosky.connection.TgStreamReader;
import com.neurosky.connection.DataType.MindDataType;

public class MainActivity extends AppCompatActivity {

    static final String TAG = null;
    TextView tv_attention;
    TextView myLabel;
    TgStreamReader tgStreamReader;

    // For bluetooth connections
    Connector Car = new Connector();
    Connector Headset = new Connector();

    boolean eegActive = false;
    boolean carIsConnected = false;
    boolean headsetIsConnected = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tv_attention = findViewById(R.id.tv_attention);

        // Header buttons in content_header.xml
        final Button connectCar = findViewById(R.id.connectCarBtn);
        final Button carConnected = findViewById(R.id.connectedCarBtn);

        final Button connectHeadset = findViewById(R.id.connectHeadsetBtn);
        final Button headsetConnected = findViewById(R.id.connectedHeadsetBtn);

        final Button eegContentBtn = findViewById(R.id.switchToEegBtn);
        final Button joystickContentBtn = findViewById(R.id.switchToJoystickBtn);

        // Activity content id's for changing content in main activity
        final RelativeLayout eegContent = findViewById(R.id.eegContent);
        final RelativeLayout joystickContent = findViewById(R.id.joystickContent);

        // Buttons to control the start and stop of eeg reading in UI, found in content_controls.xml
        final Button controlEeg = findViewById(R.id.controlEegBtn);

        // Smart car control buttons in content_controls.xml
        final ImageButton forward = findViewById(R.id.forwardBtn);
        final ImageButton backward = findViewById(R.id.backwardBtn);
        final ImageButton left = findViewById(R.id.leftBtn);
        final ImageButton right = findViewById(R.id.rightBtn);

        // Click listeners for connecting to external hardware
        connectCar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Car.findBT("Car");
                try {

                    Car.openBT();
                    carIsConnected = true;

                } catch (IOException e) { e.printStackTrace(); }
            }
        });

        if (carIsConnected == true) {
            connectCar.setVisibility(View.GONE);
            carConnected.setVisibility(View.VISIBLE);
        }

        connectHeadset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Headset.findBT("Force Trainer II");
                try {
                    Headset.openBT();
                    headsetIsConnected = true;
                } catch (IOException e) { e.printStackTrace(); }
            }
        });

        if (headsetIsConnected == true) {
            connectHeadset.setVisibility(View.GONE);
            headsetConnected.setVisibility(View.VISIBLE);
        }

        // Click listeners for changing activity content
        joystickContentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                joystickContent.setVisibility(View.VISIBLE);
                eegContent.setVisibility(View.GONE);

                joystickContentBtn.setVisibility(View.GONE);
                eegContentBtn.setVisibility(View.VISIBLE);

            }
        });

        eegContentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                joystickContent.setVisibility(View.GONE);
                eegContent.setVisibility(View.VISIBLE);

                joystickContentBtn.setVisibility(View.VISIBLE);
                eegContentBtn.setVisibility(View.GONE);

            }
        });

        // Click listeners for starting and stopping the eeg reading in the UI

        if (eegActive == false) {
            controlEeg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                controlEeg.setBackground(getDrawable(R.drawable.bg_eegcontrol_stop));
                controlEeg.setText(getString(R.string.stop));

                start();

                }
            });
        }

        // TODO: Bug! The button doesn't switch back from "stop" to "start"?

        if (eegActive == true) {
            controlEeg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                controlEeg.setBackground(getDrawable(R.drawable.bg_eegcontrol_start));
                controlEeg.setText(getString(R.string.start));

                stop();

                }
            });
        }

        // Click listeners for the smart car navigation control buttons
        forward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            try {
                goForward();
            } catch (IOException e) {
                e.printStackTrace();
            }
            }
        });

        left.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            try {
                goLeft();
            } catch (IOException e) {
                e.printStackTrace();
            }
            }
        });

        right.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            try {
                goRight();
            } catch (IOException e) {
                e.printStackTrace();
            }
            }
        });

        backward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            try {
                goBackward();
            } catch (IOException e) {
                e.printStackTrace();
            }
            }
        });
    }

    // Header button methods


    //Starts reading eeg data
    public void start() {

        if (eegActive == false) {
            createStreamReader(Headset.mmDevice);

            tgStreamReader.connectAndStart();

            eegActive = true;
        }
    }

    //Stops reading eeg data
    public void stop() {

        if (eegActive == true) {
            tgStreamReader.stop();
            tgStreamReader.close();//if there is not stop cmd, please call close() or the data will accumulate
            tgStreamReader = null;

            eegActive = false;
        }
    }

    // Handles data recieved from headset
    public TgStreamHandler callback = new TgStreamHandler() {

        @Override
        public void onDataReceived(int datatype, int data, Object obj) { //A sort of constructor
            Message msg = LinkDetectedHandler.obtainMessage();
            msg.what = datatype; //The type of data
            msg.arg1 = data; //The actual data
            msg.obj = obj;
            LinkDetectedHandler.sendMessage(msg);
        }

        @Override
        public void onStatesChanged(int connectionStates) { //Checks if the headset is connected

            switch (connectionStates) {
                case ConnectionStates.STATE_CONNECTED:
                    tgStreamReader.start();
                    break;
            }
        }

        @Override
        public void onRecordFail(int flag) { //not used
        }

        @Override
        public void onChecksumFail(byte[] payload, int length, int checksum) { //not used
        }

        private Handler LinkDetectedHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) { //Method is used to determine what kind of data we want to gather, and what we use the data for
                switch (msg.what) {
                    case MindDataType.CODE_ATTENTION: // Here we establish the data we want to gather
                        Log.d(TAG, "CODE_ATTENTION " + msg.arg1);
                        tv_attention.setText("" + msg.arg1);
                        if (msg.arg1 > 60) {
                            String msgn = "f";
                            try {
                                Car.mmOutputStream.write(msgn.getBytes());
                            } catch (IOException e) {
                            }
                        } else {
                            String msgn = "k";
                            try {
                                Car.mmOutputStream.write(msgn.getBytes());
                            } catch (IOException e) {
                            }
                        }
                        break;
                    default:
                        break;
                }
                super.handleMessage(msg);
            }
        };
    };

    // Go to repository through the link in GitHub shape
    public void goToUrl(View view) {
        String url = "https://github.com/DIT112-V20/group-08";
        Uri uriUrl = Uri.parse(url);
        Intent launchBrowser = new Intent(Intent.ACTION_VIEW, uriUrl);
        startActivity(launchBrowser);
    }

    public TgStreamReader createStreamReader(BluetoothDevice bd) { //here the data reader is being created

        if (tgStreamReader == null) {
            tgStreamReader = new TgStreamReader(bd, callback);
            tgStreamReader.startLog();
        }
        return tgStreamReader;
    }

    // Car control buttons
    void goForward() throws IOException { //Buttons to steer the car
        String msg = "c";
        Car.mmOutputStream.write(msg.getBytes());
    }

    void goLeft() throws IOException {
        String msg = "l";
        Car.mmOutputStream.write(msg.getBytes());
    }

    void goRight() throws IOException {
        String msg = "r";
        Car.mmOutputStream.write(msg.getBytes());
    }

    void goBackward() throws IOException {
        String msg = "b";
        Car.mmOutputStream.write(msg.getBytes());
    }
}





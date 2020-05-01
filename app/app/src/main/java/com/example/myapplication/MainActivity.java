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

    boolean eegActive = false;

    Connector Car = new Connector();
    Connector Headset = new Connector();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final RelativeLayout eegLayout = (RelativeLayout) findViewById(R.id.eegLayout);
        tv_attention = (TextView) findViewById(R.id.tv_attention);

        // Buttons to connect to external hardware, in content_connect.xml
        Button btn_connectcar = (Button) findViewById(R.id.connect_car);
        Button connectBtnH = (Button) findViewById(R.id.connect_headset);

        // Buttons for switching between UI states, found in content_switch.xml
        final Button partial = (Button) findViewById(R.id.partialBtn);
        final Button total = (Button) findViewById(R.id.totalBtn);
        final Button manual = (Button) findViewById(R.id.manualBtn);

        // Buttons to control the start and stop of eeg reading in UI, found in content_controls.xml
        final Button controlEeg = (Button) findViewById(R.id.controlEegBtn);

        // Smart car control buttons in content_controls.xml
        final ImageButton forward = (ImageButton) findViewById(R.id.forwardBtn);
        // TODO: Integrate backward button into controls logic below.
        final ImageButton backward = (ImageButton) findViewById(R.id.backwardBtn);
        final ImageButton left = (ImageButton) findViewById(R.id.leftBtn);
        final ImageButton right = (ImageButton) findViewById(R.id.rightBtn);

        // Click listeners for connecting to external hardware
        btn_connectcar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Car.findBT("Car");
                try {
                    Car.openBT();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        connectBtnH.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Headset.findBT("Force Trainer II");
            }

        });

        // Click listeners for switching between UI states
        partial.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Visibility of buttons that switch UI states
                partial.setVisibility(View.GONE);
                total.setVisibility(View.VISIBLE);
                manual.setVisibility(View.VISIBLE);

                // Visibility for smart car control buttons
                forward.setVisibility(View.VISIBLE);
                backward.setVisibility(View.VISIBLE);
                left.setVisibility(View.VISIBLE);
                right.setVisibility(View.VISIBLE);

                // Visibility of EEG reading in UI
                eegLayout.setVisibility(View.VISIBLE);

                // Visibility of eeg control button
                controlEeg.setVisibility(View.VISIBLE);
            }
        });

        total.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Visibility of buttons that switch UI states
                partial.setVisibility(View.VISIBLE);
                total.setVisibility(View.GONE);
                manual.setVisibility(View.VISIBLE);

                // Visibility for smart car control buttons
                forward.setVisibility(View.GONE);
                backward.setVisibility(View.GONE);
                left.setVisibility(View.GONE);
                right.setVisibility(View.GONE);

                // Visibility of EEG reading in UI
                eegLayout.setVisibility(View.VISIBLE);

                // Visibility of eeg control button
                controlEeg.setVisibility(View.VISIBLE);
            }
        });

        manual.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Visibility of buttons that switch UI states
                partial.setVisibility(View.VISIBLE);
                total.setVisibility(View.VISIBLE);
                manual.setVisibility(View.GONE);

                // Visibility for smart car control buttons
                forward.setVisibility(View.VISIBLE);
                backward.setVisibility(View.VISIBLE);
                left.setVisibility(View.VISIBLE);
                right.setVisibility(View.VISIBLE);

                // Visibility of EEG reading in UI
                eegLayout.setVisibility(View.GONE);

                // Visibility of eeg control button
                controlEeg.setVisibility(View.GONE);

                // Method to stop eeg reading in UI
                controlEeg.setBackground(getDrawable(R.drawable.bg_eegcontrol_start));
                controlEeg.setText(getString(R.string.start));
                stop();
            }
        });

        // Changes size of start/stop Eeg reading button when in total mind control mode
        /* if (total.getVisibility() == View.GONE) {

            ViewGroup.LayoutParams params = controlEeg.getLayoutParams();

            params.width = 150;
            params.height = 150;

            controlEeg.setLayoutParams(params);

        } else {

            ViewGroup.LayoutParams params = controlEeg.getLayoutParams();

            params.width = 100;
            params.height = 100;

            controlEeg.setLayoutParams(params);

        } */


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

    public TgStreamHandler callback = new TgStreamHandler() { //Handles data recieved from headset

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

    //go to webpage through the link in GitHub shape
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





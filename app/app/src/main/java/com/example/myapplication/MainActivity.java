package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Button;
import java.io.IOException;
import com.neurosky.connection.ConnectionStates;
import com.neurosky.connection.TgStreamHandler;
import com.neurosky.connection.TgStreamReader;
import com.neurosky.connection.DataType.MindDataType;

public class MainActivity extends AppCompatActivity {

    static final String TAG = null;
    TextView tv_attention;
    TgStreamReader tgStreamReader;

    // For pulse animation
    private Handler animationHandler;
    private ImageView animOne, animTwo, animThree, animFour;

    // For bluetooth connections
    Connector Car = new Connector();
    Connector Headset = new Connector();

    boolean eegActive = false;
    boolean carIsConnected = false;
    boolean headsetIsConnected = false;

    //for gyroscope sensors
    SensorManager sensorManager;
    Sensor accelerometer;
    SensorEventListener accelerometerEventListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tv_attention = findViewById(R.id.tv_attention);

        initAnimation();

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

        //Used for gyroscope
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        accelerometerEventListener = new SensorEventListener(){
            @Override
            public void onSensorChanged(SensorEvent sensorEvent) {

                int value = (int) sensorEvent.values[0];
                value = (value *11);

                try {
                    Car.mmOutputStream.write(value);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {
                //Not used
            }
        };

        // Click listeners for connecting to external hardware
        connectCar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!carIsConnected) {
                    Car.findBT("Car");
                    try {

                        Car.openBT();
                        carIsConnected = true;
                        connectCar.setVisibility(View.GONE);
                        carConnected.setVisibility(View.VISIBLE);

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                else {
                    //do nothing
                }
            }
        });

        connectHeadset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!headsetIsConnected) {
                    Headset.findBT("Force Trainer II");
                    headsetIsConnected = true;
                    connectHeadset.setVisibility(View.GONE);
                    headsetConnected.setVisibility(View.VISIBLE);
                }
                else{
                    // do nothing
                }
            }
        });

        // Click listeners for changing activity content
        joystickContentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (headsetIsConnected && carIsConnected) {
                    joystickContent.setVisibility(View.VISIBLE);
                    eegContent.setVisibility(View.GONE);

                    joystickContentBtn.setVisibility(View.GONE);
                    eegContentBtn.setVisibility(View.VISIBLE);

                    if (eegActive) {
                        eegActive = false;
                        stopEeg();
                        stopGyro();
                    }
                }
                else {
                    // do nothing
                }
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
            controlEeg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (carIsConnected && headsetIsConnected && !eegActive) {
                        controlEeg.setBackground(getDrawable(R.drawable.bg_eegcontrol_stop));
                        controlEeg.setText(getString(R.string.stop));

                        eegActive = true;

                        startAnimation();
                        startEeg();
                        startGyro();

                    }
                    else if (eegActive) {

                                controlEeg.setBackground(getDrawable(R.drawable.bg_eegcontrol_start));
                                controlEeg.setText(getString(R.string.start));
                                eegActive = false;

                                stopAnimation();
                                stopEeg();
                    }
                }
            });


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

    //stops reading gyroscope data
    public void stopGyro(){
        sensorManager.unregisterListener(accelerometerEventListener);
    }

    //starts reading gyroscope data
    public void startGyro(){
        sensorManager.registerListener(accelerometerEventListener, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    //Starts reading eeg data
    public void startEeg() {
        createStreamReader(Headset.mmDevice);
        tgStreamReader.connectAndStart();
    }

    //Stops reading eeg data
    public void stopEeg() {
        tgStreamReader.stop();
        tgStreamReader.close();//if there is not stop cmd, please call close() or the data will accumulate
        tgStreamReader = null;
    }

    // Handles data recieved from headset
    public TgStreamHandler callback = new TgStreamHandler() {

        //A sort of constructor
        @Override
        public void onDataReceived(int datatype, int data, Object obj) {
            Message msg = LinkDetectedHandler.obtainMessage();
            msg.what = datatype; //The type of data
            msg.arg1 = data; //The actual data
            msg.obj = obj;
            LinkDetectedHandler.sendMessage(msg);
        }

        //Checks if the headset is connected
        @Override
        public void onStatesChanged(int connectionStates) {
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

        // Method is used to determine what kind of data we want to gather, and what we use the data for
        private Handler LinkDetectedHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    // Here we establish the data we want to gather
                    case MindDataType.CODE_ATTENTION:
                        Log.d(TAG, "CODE_ATTENTION " + msg.arg1);
                        tv_attention.setText("" + msg.arg1);
                        if (msg.arg1 > 59) {
                            int msgn = 3;
                            try {
                                Car.mmOutputStream.write(msgn);
                            } catch (IOException e) {
                            }
                        } else {
                            int msgn = 4;
                            try {
                                Car.mmOutputStream.write(msgn);
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

    // For pulse animation
    private void initAnimation() {
        this.animationHandler = new Handler();
        this.animOne = findViewById(R.id.animOne);
        this.animTwo = findViewById(R.id.animTwo);
        this.animThree = findViewById(R.id.animThree);
        this.animFour = findViewById(R.id.animFour);
    }

    private void startAnimation() { this.pulseAnimation.run(); }

    private void stopAnimation() { this.animationHandler.removeCallbacks(pulseAnimation); }

    private Runnable pulseAnimation = new Runnable() {
        @Override
        public void run() {

            animOne.animate().scaleX(4f).scaleY(4f).alpha(0f).setDuration(800).withEndAction(new Runnable() {
                @Override
                public void run() {
                    animOne.setScaleX(1f);
                    animOne.setScaleY(1f);
                    animOne.setAlpha(1f);
                }
            });

            animTwo.animate().scaleX(4f).scaleY(4f).alpha(0f).setDuration(1200).withEndAction(new Runnable() {
                @Override
                public void run() {
                    animTwo.setScaleX(1f);
                    animTwo.setScaleY(1f);
                    animTwo.setAlpha(1f);
                }
            });

            animThree.animate().scaleX(4f).scaleY(4f).alpha(0f).setDuration(1600).withEndAction(new Runnable() {
                @Override
                public void run() {
                    animThree.setScaleX(1f);
                    animThree.setScaleY(1f);
                    animThree.setAlpha(1f);
                }
            });

            animFour.animate().scaleX(4f).scaleY(4f).alpha(0f).setDuration(2000).withEndAction(new Runnable() {
                @Override
                public void run() {
                    animFour.setScaleX(1f);
                    animFour.setScaleY(1f);
                    animFour.setAlpha(1f);
                }
            });

            animationHandler.postDelayed(pulseAnimation, 2500);
        }
    };
}





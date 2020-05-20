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
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Button;
import android.widget.Toast;

import java.io.IOException;

import com.neurosky.connection.ConnectionStates;
import com.neurosky.connection.TgStreamHandler;
import com.neurosky.connection.TgStreamReader;
import com.neurosky.connection.DataType.MindDataType;

public class MainActivity extends AppCompatActivity {

    static final String TAG = null;
    TextView tv_attention;
    TgStreamReader tgStreamReader;

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

        // Pulse animation
        PulseView pulse = findViewById(R.id.pulse);

        int concentration = 1;

        // Fade calculation for case levels
        int minFade = 40;
        int maxFade = 100;
        int transparent = 255;
        int fadeCalc;

        // Duration calculation for case levels
        long maxDuration = 1500L;
        long durationCalc;
        long noDuration = 0L;

        // If concentration is zero, make sure app doesn't crash
        if (concentration == 0) {
            fadeCalc = 0;
            durationCalc = 0;
        } else {
            fadeCalc = minFade + ((maxFade - minFade) / concentration);
            durationCalc = maxDuration / concentration;
        }

        // Switch case for testing purposes, will add a prettier loop later
        switch (concentration) {
            default:
                pulse.setDuration(noDuration);
                pulse.setFade(transparent);
                break;
            case 1:
                pulse.setDuration(maxDuration);
                pulse.setFade(maxFade);
                break;
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
            case 7:
            case 8:
            case 9:
                pulse.setDuration(durationCalc);
                pulse.setFade(fadeCalc);
                break;
            case 10:
                pulse.setDuration(durationCalc);
                pulse.setFade(minFade);
                break;
        }

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

        accelerometerEventListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent sensorEvent) {

                int value = (int) sensorEvent.values[0];
                value = (value * 11);

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
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    CountDownTimer timer = new CountDownTimer(6000, 1000) {
                        @Override
                        public void onTick(long millisUntilFinished) {
                            connectCar.setTextSize(10);
                            connectCar.setText(getString(R.string.connecting));
                        }

                        @Override
                        public void onFinish() {
                            connectCar.setVisibility(View.GONE);
                            carConnected.setVisibility(View.VISIBLE);
                            carIsConnected = true;
                        }
                    }.start();
                }
            }
        });

        connectHeadset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!headsetIsConnected) {
                    Headset.findBT("Force Trainer II");
                    CountDownTimer timer = new CountDownTimer(6000, 1000) {
                        @Override
                        public void onTick(long millisUntilFinished) {
                            connectHeadset.setTextSize(10);
                            connectHeadset.setText("Connecting...");
                        }

                        @Override
                        public void onFinish() {
                            connectHeadset.setVisibility(View.GONE);
                            headsetConnected.setVisibility(View.VISIBLE);
                            headsetIsConnected = true;
                        }
                    }.start();
                }
            }
        });

        // Click listeners for changing activity content
        joystickContentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
                if (!carIsConnected && !headsetIsConnected) {
                    pleaseConnectDevices();
                } else if (!carIsConnected) {
                    pleaseConnectCar();
                } else if (!headsetIsConnected) {
                    pleaseConnectHeadset();
                } else if (carIsConnected && headsetIsConnected && !eegActive) {
                    controlEeg.setBackground(getDrawable(R.drawable.bg_eegcontrol_stop));
                    controlEeg.setText(getString(R.string.stop));
                    eegActive = true;

                    startEeg();
                    startGyro();
                } else {
                    controlEeg.setBackground(getDrawable(R.drawable.bg_eegcontrol_start));
                    controlEeg.setText(getString(R.string.start));
                    eegActive = false;

                    stopEeg();
                    stopGyro();
                }
            }
        });

        // Click listeners for the smart car navigation control buttons
        forward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!carIsConnected) {
                    pleaseConnectCar();
                } else {
                    try {
                        goForward();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        left.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!carIsConnected) {
                    pleaseConnectCar();
                } else {
                    try {
                        goLeft();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        right.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!carIsConnected) {
                    pleaseConnectCar();
                } else {
                    try {
                        goRight();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        backward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!carIsConnected) {
                    pleaseConnectCar();
                } else {
                    try {
                        goBackward();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    // Toast methods because I couldn't get the Toasty class to work
    private void pleaseConnectDevices() {
        String toastString = "Please connect devices";
        Toast.makeText(getApplicationContext(), toastString, Toast.LENGTH_SHORT).show();
    }

    private void pleaseConnectHeadset() {
        String toastString = "Please connect the headset";
        Toast.makeText(getApplicationContext(), toastString, Toast.LENGTH_SHORT).show();
    }

    private void pleaseConnectCar() {
        String toastString = "Please connect to smart car";
        Toast.makeText(getApplicationContext(), toastString, Toast.LENGTH_SHORT).show();
    }

    // Method to stops reading gyroscope data
    public void stopGyro() {
        sensorManager.unregisterListener(accelerometerEventListener);
    }

    // Method to start reading gyroscope data
    public void startGyro() {
        sensorManager.registerListener(accelerometerEventListener, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    // Method to start reading eeg data
    public void startEeg() {
        createStreamReader(Headset.mmDevice);
        tgStreamReader.connectAndStart();
    }

    // Method to stop reading eeg data
    public void stopEeg() {
        tgStreamReader.stop();
        tgStreamReader.close();//if there is not stop cmd, please call close() or the data will accumulate
        tgStreamReader = null;
    }

    // Handles data received from headset
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
                            int msgn = 9; // forward
                            try {
                                Car.mmOutputStream.write(msgn);
                            } catch (IOException e) {
                            }
                        } else {
                            int msgn = 10; // stop
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

    // Method to go to repository in external browser
    public void goToUrl(View view) {
        String url = "https://github.com/DIT112-V20/group-08";
        Uri uriUrl = Uri.parse(url);
        Intent launchBrowser = new Intent(Intent.ACTION_VIEW, uriUrl);
        startActivity(launchBrowser);
    }

    // Method to create data reader
    public TgStreamReader createStreamReader(BluetoothDevice bd) {
        if (tgStreamReader == null) {
            tgStreamReader = new TgStreamReader(bd, callback);
            tgStreamReader.startLog();
        }
        return tgStreamReader;
    }

    // Car control buttons
    void goForward() throws IOException { //Buttons to steer the car
        int msg = 1;
        Car.mmOutputStream.write(msg);
    }

    void goLeft() throws IOException {
        int msg = 8;
        Car.mmOutputStream.write(msg);
    }

    void goRight() throws IOException {
        int msg = 2;
        Car.mmOutputStream.write(msg);
    }

    void goBackward() throws IOException {
        int msg = 5;
        Car.mmOutputStream.write(msg);
    }
}





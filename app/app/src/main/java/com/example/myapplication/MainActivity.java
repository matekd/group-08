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
import android.os.Process;
import android.util.Log;
import android.view.View;
import android.widget.HeaderViewListAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Button;
import android.widget.Toast;

import java.io.IOException;

import com.neurosky.connection.ConnectionStates;
import com.neurosky.connection.TgStreamHandler;
import com.neurosky.connection.TgStreamReader;
import com.neurosky.connection.DataType.MindDataType;

public class MainActivity extends AppCompatActivity implements JoyStick.JoyStickListener {

    static final String TAG = null;
    TgStreamReader tgStreamReader;

    // For bluetooth connections
    Connector Car = new Connector();
    boolean carIsConnected = false;
    Button connectCar;
    Button carConnected;

    Connector Headset = new Connector();
    boolean headsetIsConnected = false;
    Button connectHeadset;
    Button headsetConnected;

    private void initConnection() {
        connectCar = findViewById(R.id.connectCarBtn);
        carConnected = findViewById(R.id.connectedCarBtn);
        connectHeadset = findViewById(R.id.connectHeadsetBtn);
        headsetConnected = findViewById(R.id.connectedHeadsetBtn);
    }

    // UI layout elements
    RelativeLayout eegContent;

    RelativeLayout joystickContent;
    JoyStick joyStick;

    Button eegContentBtn;
    Button joystickContentBtn;

    private void initLayout() {
        eegContentBtn = findViewById(R.id.switchToEegBtn);
        joystickContentBtn = findViewById(R.id.switchToJoystickBtn);

        eegContent = findViewById(R.id.eegContent);
        joystickContent = findViewById(R.id.joystickContent);
        joyStick = findViewById(R.id.joy1);
        joyStick.setBackgroundResource(R.drawable.joystick_trackpad_background);
        joyStick.setListener(this);
    }

    // Eeg reading in app
    boolean eegActive = false;
    Button controlEeg;

    PulseView pulse;
    TextView tv_attention;

    private void initEeg() {
        controlEeg = findViewById(R.id.controlEegBtn);
        pulse = findViewById(R.id.pulse);
        tv_attention = findViewById(R.id.tv_attention);
    }

    // For gyroscope sensors
    SensorManager sensorManager;
    Sensor accelerometer;
    SensorEventListener accelerometerEventListener;

    private void initGyroscope() {
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        assert sensorManager != null;
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initConnection();
        initLayout();
        initEeg();
        initGyroscope();

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
                    connectionHelper("Car");
                    try {
                        Thread.sleep(3000);
                        if (Car.mmSocket.isConnected()==false) {
                            pleaseConnect();
                        } else {
                        CountDownTimer timer = new CountDownTimer(6000, 1000) { // TODO: What does CountDownTimer timer do? It comes up as 'never used' in edit :) Liv, 2020-05-31
                            @Override
                            public void onTick(long millisUntilFinished) {
                                connectCar.setTextSize(9);
                                connectCar.setText(getString(R.string.connecting));
                            }
                            @Override
                            public void onFinish() {
                                connectCar.setVisibility(View.GONE);
                                carConnected.setVisibility(View.VISIBLE);
                                carIsConnected = true;
                            }
                        }.start();
                    }} catch (NullPointerException | InterruptedException e) {
                        e.printStackTrace();
                        pleaseConnect();
                    }
                }
            }});

        connectHeadset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){
                    if (!headsetIsConnected) {
                        connectionHelper("Force Trainer II");
                        try {
                            if (Car.mmOutputStream == null) {
                                pleaseConnect();
                            } else  {
                                CountDownTimer timer = new CountDownTimer(6000, 1000) {
                                @Override
                                public void onTick(long millisUntilFinished) {
                                    connectHeadset.setTextSize(9);
                                    connectHeadset.setText("Connecting...");
                                }

                                @Override
                                public void onFinish() {
                                    connectHeadset.setVisibility(View.GONE);
                                    headsetConnected.setVisibility(View.VISIBLE);
                                    headsetIsConnected = true;
                                }
                            }.start();
                        } }
                        catch (NullPointerException e) {
                            e.printStackTrace();
                            pleaseConnect();
                        }
                    }
                }
        });

        // Click listeners for changing activity content
        joystickContentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Car.mmOutputStream == null) {
                    pleaseConnect();
                } else {
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
                if (!carIsConnected && !headsetIsConnected || !carIsConnected || !headsetIsConnected) {
                    pleaseConnect();
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
    }

    // Toast method, prompting the end-user to check bluetooth connections to hardware
    private void pleaseConnect() {
        String toastString = "Please check Bluetooth connection";
        Toast.makeText(getApplicationContext(), toastString, Toast.LENGTH_SHORT).show();
    }

    //stops reading gyroscope data
    public void stopGyro() {
        sensorManager.unregisterListener(accelerometerEventListener);
    }

    //starts reading gyroscope data
    public void startGyro() {
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
        // If there is not stop cmd, please call close() or the data will accumulate
        tgStreamReader.close();
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
            // TODO: This could be changed to an if-statement. What does connectionStates require to be true? Liv 2020-05-31
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

                        // Changes the animation to reflect eeg
                        showConcentration(msg.arg1);

                        if (msg.arg1 > 59 && msg.arg1 < 101) {
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

    // Go to repository through the link in GitHub shape
    public void goToUrl(View view) {
        String url = "https://github.com/DIT112-V20/group-08";
        Uri uriUrl = Uri.parse(url);
        Intent launchBrowser = new Intent(Intent.ACTION_VIEW, uriUrl);
        startActivity(launchBrowser);
    }

    // Here the data reader is being created
    public TgStreamReader createStreamReader(BluetoothDevice bd) {
        if (tgStreamReader == null) {
            tgStreamReader = new TgStreamReader(bd, callback);
            tgStreamReader.startLog();
        }
        return tgStreamReader;
    }

    // Joystick eight control methods to replace four buttons
    public void onMove(JoyStick joyStick, double angle, double power, int direction) {
        if (!carIsConnected) {
            pleaseConnect();

        } else {
            switch (direction) {
                case JoyStick.DIRECTION_LEFT: {
                    try {
                        Car.mmOutputStream.write(7);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;

                case JoyStick.DIRECTION_RIGHT: {
                    try {
                        Car.mmOutputStream.write(3);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;

                case JoyStick.DIRECTION_UP: {
                    try {
                        Car.mmOutputStream.write(1);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;

                case JoyStick.DIRECTION_DOWN: {
                    try {
                        Car.mmOutputStream.write(5);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;

                case JoyStick.DIRECTION_LEFT_UP: {
                    try {
                        Car.mmOutputStream.write(8);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;

                case JoyStick.DIRECTION_UP_RIGHT: {
                    try {
                        Car.mmOutputStream.write(2);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;

                case JoyStick.DIRECTION_DOWN_LEFT: {
                    try {
                        Car.mmOutputStream.write(6);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;

                case JoyStick.DIRECTION_RIGHT_DOWN: {
                    try {
                        Car.mmOutputStream.write(4);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;

                case JoyStick.DIRECTION_CENTER: {
                    // Stops the car
                    try {
                        Car.mmOutputStream.write(13);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;

                default:
                    break;

            }
        }
    }

    public void onTap() {
    }

    public void onDoubleTap() {
    }

    // Method for animation changes
    public void showConcentration(final int eeg) {
        Runnable animationInteraction = new Runnable() {
            @Override
            public void run() {
                Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);

                // Set concentration level in PulseView
                if (eeg == 0) {
                    pulse.setConcentration(0);
                } else if ((eeg > 0) && (eeg < 100)) {
                    pulse.setConcentration((eeg / 10) + 1);
                }
            }
        };

        Thread animationInteractionThread = new Thread(animationInteraction);
        animationInteractionThread.start();
    }

    public void connectionHelper(final String name) {
        Runnable animationInteraction = new Runnable() {
            @Override
            public void run() {
                Process.setThreadPriority(Process.THREAD_PRIORITY_MORE_FAVORABLE);
                if(name.equals("Car")){
                    Car.findBT(name);
                    try {
                        Car.openBT();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else
                    Headset.findBT(name);
            }
        };

        Thread animationInteractionThread = new Thread(animationInteraction);
        animationInteractionThread.start();
    }
}

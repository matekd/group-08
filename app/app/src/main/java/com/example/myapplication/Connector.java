package com.example.myapplication;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

public class Connector {     //Used to connect the devices via bluetooth

    BluetoothAdapter mBluetoothAdapter;
    BluetoothSocket mmSocket;
    BluetoothDevice mmDevice;
    OutputStream mmOutputStream;

    void findBT(String name){ //You need to specify the name of the bluetooth device
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                if (device.getName().equals(name))
                {
                    mmDevice = device;

                    break;
                } else {

                }
            }
        }
    }

        void openBT() throws IOException {
            UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
            if (mmDevice != null) {
                mmSocket = mmDevice.createRfcommSocketToServiceRecord(uuid);
                mmSocket.connect();
                mmOutputStream = mmSocket.getOutputStream();

            }
        }
}


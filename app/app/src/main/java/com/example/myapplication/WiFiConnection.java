package com.example.myapplication;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Arrays;
import java.util.List;


public class WiFiConnection extends MainActivity implements View.OnClickListener{



    //receive broadcast
    private BroadcastReceiver mConnReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {

            NetworkInfo currentNetworkInfo = intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);

            if (currentNetworkInfo.isConnected()) {
                WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                WifiInfo wifiInfo = wifiManager.getConnectionInfo();

                Toast.makeText(getApplicationContext(), "Broadcast Connected:" +  wifiInfo.getSSID(), Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(getApplicationContext(), "Broadcast Not Connected", Toast.LENGTH_LONG).show();
            }
        }
    };

    //wifi manager finds netwrok
    public boolean ConnectToNetworkWEP( String networkSSID, String password )
    {
        try {
            WifiConfiguration conf = new WifiConfiguration();
            conf.SSID = "\"" + "smartcar" + "\"";   // Please note the quotes. String should contain SSID in quotes
            conf.wepKeys[0] = "123456789";  //WEP password is in hex, we do not need to surround it with quotes.
            conf.wepTxKeyIndex = 0;
            conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);

            WifiManager wifiManager = (WifiManager) this.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            wifiManager.addNetwork(conf);

            List<WifiConfiguration> list = wifiManager.getConfiguredNetworks();
            for( WifiConfiguration i : list ) {
                if(i.SSID != null && i.SSID.equals("\"" + networkSSID + "\"")) {
                    wifiManager.disconnect();
                    wifiManager.enableNetwork(i.networkId, true);
                    wifiManager.reconnect();

                    break;
                }
            }

            //WiFi Connection success, return true
            return true;
        } catch (Exception ex) {
            System.out.println(Arrays.toString(ex.getStackTrace()));
            return false;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.registerReceiver(this.mConnReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        Button connectCarBtn = findViewById(R.id.connectCarBtn);
        connectCarBtn.setOnClickListener(this) ;
    }

            @Override
            public void onClick(View v) {
                // Perform action on click

                WifiManager wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
                if(!wifi.isWifiEnabled()) {
                    wifi.setWifiEnabled(true);
                }

                ConnectToNetworkWEP("smartcar", "123456789");
            }
}


package com.embeddedproject.tokenManager;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        WifiManager wifiMgr = (WifiManager) getSystemService(Context.WIFI_SERVICE);

        if (sharedPreferences.getBoolean("firstrun", true)) {
            Intent intent = new Intent(this, InitialSetupActivity.class);
            startActivity(intent);
            finish();
        } else if(!wifiMgr.isWifiEnabled()){
            Intent intent = new Intent(this, WifiEnableActivity.class);
            startActivity(intent);
            finish();
        }else {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

}
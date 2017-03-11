package com.embeddedproject.tokenManager;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;

public class WifiEnableActivity extends AppCompatActivity {

    WifiManager wifiMgr;
    CoordinatorLayout coordinatorLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi_enable);

        wifiMgr = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.enableWifiCoordinatorLayout);

        Switch enableSwitch = (Switch) findViewById(R.id.enable_wifi_switch);
        enableSwitch.setOnCheckedChangeListener(enableSwitchListner);
        Button nextButton = (Button) findViewById(R.id.enable_wifi_next);
        nextButton.setOnClickListener(nextButtonClick);
    }

    CompoundButton.OnCheckedChangeListener enableSwitchListner = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if(isChecked){
                wifiMgr.setWifiEnabled(true);
            }else{
                wifiMgr.setWifiEnabled(false);
            }
        }
    };

    View.OnClickListener nextButtonClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(wifiMgr.isWifiEnabled()){
                Intent intent = new Intent(WifiEnableActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }else{
                Snackbar.make(coordinatorLayout, "Enable WiFi.", Snackbar.LENGTH_LONG).show();
            }
        }
    };

}

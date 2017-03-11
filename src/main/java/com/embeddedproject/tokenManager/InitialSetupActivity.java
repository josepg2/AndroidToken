package com.embeddedproject.tokenManager;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class InitialSetupActivity extends AppCompatActivity {

    WifiManager wifiMgr;
    CoordinatorLayout coordinatorLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_initial_setup);

        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.initialSetupCoordinatorLayout);
        wifiMgr = (WifiManager) getSystemService(Context.WIFI_SERVICE);

        TextView setInitialIPAddress = (TextView) findViewById(R.id.initial_setup_display_server_ip);
        setInitialIPAddress.setText("192.168.1.150");
        TextView setInitialPortID = (TextView) findViewById(R.id.initial_setup_port_id);
        setInitialPortID.setText("8000");

        Button nextButton = (Button) findViewById(R.id.next_button);
        Button cancelButton = (Button) findViewById(R.id.cancel_button);

        nextButton.setOnClickListener(clickListener);
        cancelButton.setOnClickListener(clickListener);


    }

    View.OnClickListener clickListener = new View.OnClickListener(){

        @Override
        public void onClick(View v) {
            switch(v.getId()) {
                case R.id.next_button:
                    if(checkFormStatus()){

                        if(!wifiMgr.isWifiEnabled()){
                            Intent intent = new Intent(InitialSetupActivity.this, WifiEnableActivity.class);
                            startActivity(intent);
                            finish();
                        }else {
                            Intent intent = new Intent(InitialSetupActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    }
                    // it was the first button
                    break;
                case R.id.cancel_button:
                    finish();
                    break;
            }
        }
    };

    boolean checkFormStatus() {

        EditText retriveText = (EditText) findViewById(R.id.initial_setup_password);
        String password = retriveText.getText().toString();
        retriveText = (EditText) findViewById(R.id.initial_setup_doctor_name);
        String doctor_name = retriveText.getText().toString();
        retriveText = (EditText) findViewById(R.id.initial_setup_doctor_code);
        String doctor_id = retriveText.getText().toString();
        retriveText = (EditText) findViewById(R.id.initial_setup_hospital_name);
        String hostpital_name = retriveText.getText().toString();
        retriveText = (EditText) findViewById(R.id.initial_setup_display_server_ip);
        String ip_address = retriveText.getText().toString();
        retriveText = (EditText) findViewById(R.id.initial_setup_port_id);
        String port_id = retriveText.getText().toString();

        if(!password.equals("blueOcean")){
            Snackbar.make(coordinatorLayout, "Wrong Password.", Snackbar.LENGTH_LONG).show();
            return false;
        }else if(doctor_name.trim().isEmpty()){
            Snackbar.make(coordinatorLayout, "Doctor Name cannot be Empty.", Snackbar.LENGTH_LONG).show();
            return false;
        }else if(doctor_id.trim().isEmpty()){
            Snackbar.make(coordinatorLayout, "Doctor ID cannot be Empty.", Snackbar.LENGTH_LONG).show();
            return false;
        }else if(hostpital_name.trim().isEmpty()){
            Snackbar.make(coordinatorLayout, "Hospital Name cannot be Empty.", Snackbar.LENGTH_LONG).show();
            return false;
        }else if(ip_address.trim().isEmpty()){
            Snackbar.make(coordinatorLayout, "Server IP cannot be Empty.", Snackbar.LENGTH_LONG).show();
            return false;
        }else if(port_id.trim().isEmpty()){
            Snackbar.make(coordinatorLayout, "PORT ID cannot be Empty.", Snackbar.LENGTH_LONG).show();
            return false;
        }else{
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("pref_key_user_name", doctor_name);
            editor.putString("pref_key_user_id", doctor_id);
            editor.putString("pref_key_hospital_name", hostpital_name);
            editor.putString("pref_key_ip_address", ip_address);
            editor.putString("pref_key_port_id", port_id);
            editor.putBoolean("firstrun",false);
            editor.apply();

            return true;
        }
    }
}

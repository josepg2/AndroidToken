package com.embeddedproject.tokenManager;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.Preference;
import android.support.v7.preference.EditTextPreference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.view.MenuItem;

public class MyPreferencesActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTitle("Settings");
        getSupportFragmentManager().beginTransaction().replace(android.R.id.content, new MyPreferenceFragment()).commit();
    }

    public static class MyPreferenceFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener
    {
        @Override
        public void onCreatePreferences(Bundle bundle, String s)
        {
            addPreferencesFromResource(R.xml.app_preferences);
            SharedPreferences sp = getPreferenceScreen().getSharedPreferences();
            EditTextPreference editTextPref;
            String prefs[] = {"pref_key_user_name", "pref_key_user_id", "pref_key_hospital_name", "pref_key_ip_address"};
            String default_prefs[] = {"Enter Doctor Name", "Enter Doctor Code", "Enter Hospital Name","Enter Server IP Address" };
            for (int i = 0; i < prefs.length ; i++){
                editTextPref = (EditTextPreference) findPreference(prefs[i]);
                editTextPref.setSummary(sp.getString(prefs[i], default_prefs[i]));
            }

            EditTextPreference editPortId;
            editPortId  = (EditTextPreference) findPreference("pref_key_port_id");
            editPortId.setSummary(sp.getString("pref_key_port_id", "Enter Server Port ID"));
        }

        @Override
        public void onResume() {
            super.onResume();
            getPreferenceScreen().getSharedPreferences()
                    .registerOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onPause() {
            super.onPause();
            getPreferenceScreen().getSharedPreferences()
                    .unregisterOnSharedPreferenceChangeListener(this);
        }


        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            if (key.equals("pref_key_user_name") ||
                    key.equals("pref_key_user_id") ||
                    key.equals("pref_key_hospital_name") ||
                    key.equals("pref_key_ip_address") ||
                    key.equals("pref_key_port_id")) {
                Preference pref = findPreference(key);
                pref.setSummary(sharedPreferences.getString(key, ""));
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {

        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return true;
    }
}

package com.embeddedproject.tokenManager;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;

import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Iterator;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, Listener {

    final Context c = this;

    CoordinatorLayout coordinatorLayout;
    CollapsingToolbarLayout collapsingToolbarLayout;
    RecyclerView recyclerView;
    DbHelper dbHelper;
    ListAdapter adapter;
    NavigationView navigationView;
    TextView textToUpdate;

    String ip_address = "";
    String port_number = "";
    String doctor_name;
    String doctor_id;
    String hospital_name;
    int lastNonCalledToken = 0;
    boolean clearLastNonCalledToken = false;
    TokenData currentToken = new TokenData(0, true);

    boolean positionDeleteStatus;
    int positionToDelete;
    int positoinToDeteteToken;


    IsSocketFree isSocketFree = new IsSocketFree();
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        if (savedInstanceState != null) {
            currentToken.setTokenNumber(savedInstanceState.getInt("TokenCount"));
        } else {
            SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
            currentToken.setTokenNumber(sharedPref.getInt("TokenCount", currentToken.tokenNumber));
            currentToken.setTokenStatus(sharedPref.getBoolean("TokenStatus", true));
            lastNonCalledToken = sharedPref.getInt("LastNonCalledToken" , 0);
        }

        setContentView(R.layout.activity_main);
        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.main_coordinatior_layout);
        //ToolBar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        AppBarLayout appBarLayout = (AppBarLayout) findViewById(R.id.appBarLayout);
        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            boolean isShow = false;
            int scrollRange = -1;

            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                if (scrollRange == -1) {
                    scrollRange = appBarLayout.getTotalScrollRange();
                }
                if (scrollRange + verticalOffset == 0) {
                    collapsingToolbarLayout.setTitle("Token  " + currentToken.tokenNumber);
                    isShow = true;
                } else if (isShow) {
                    collapsingToolbarLayout.setTitle("");
                    isShow = false;
                }
            }
        });

        textToUpdate = (TextView) findViewById(R.id.token_count);
        textToUpdate.setText(String.valueOf(currentToken.tokenNumber));

        dbHelper = DbHelper.getInstance(getApplicationContext());

        recyclerView = (RecyclerView) findViewById(R.id.rv_tokenlist);
        adapter = new ListAdapter(this, currentToken, dbHelper.getUnAttentedTokens(), isSocketFree);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        RecyclerView.ItemAnimator animator = recyclerView.getItemAnimator();
        if (animator instanceof SimpleItemAnimator) {
            ((SimpleItemAnimator) animator).setSupportsChangeAnimations(false);
        }


        ImageButton countBackButton = (ImageButton) findViewById(R.id.back_button);
        countBackButton.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                //Do stuff here
                if (isSocketFree.isSocketBusy()) {
                    return;
                }
                isSocketFree.blockSocket();
                if (lastNonCalledToken == 0) {
                    showNoUnattentedTokensBehind();
                    isSocketFree.openSocket();
                } else {
                    goToToken(lastNonCalledToken);
                    clearLastNonCalledToken = true;
                }

            }
        });

        ImageButton countForwardButton = (ImageButton) findViewById(R.id.forward_button);
        countForwardButton.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                //Do stuff here
                if (isSocketFree.isSocketBusy()) {
                    return;
                }
                isSocketFree.blockSocket();
                if (currentToken.tokenNumber == 0) {
                    if (!getTokenConnectionStatus()) {
                        return;
                    }
                    new SendPostRequest(c, new TokenData(1, true), "UPDATEZERO")
                            .execute(ip_address + ":" + port_number);
                    return;
                }
                goToToken(currentToken.tokenNumber + 1);
                //new SendPostRequest(c, new TokenData(1, true)).execute();
            }
        });


        //Drawer Layout
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        //Navigation View
        navigationView = (NavigationView) findViewById(R.id.navigation_drawer_bottom);
        navigationView.setNavigationItemSelectedListener(this);

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    @Override
    protected void onResume() {
        super.onResume();

        loadSavedPreferences((NavigationView) findViewById(R.id.navigation_drawer_top));
        getTokenConnectionStatus();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d("lifecycle", "onPause invoked");
        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt("TokenCount", currentToken.tokenNumber);
        editor.putBoolean("TokenStatus", currentToken.tokenStatus);
        editor.putInt("LastNonCalledToken", lastNonCalledToken);
        editor.apply();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("ONDestroy", "Destroyed");
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        Log.d("ONSaveInst", "InsideSavingInstance");
        super.onSaveInstanceState(outState);
        outState.putInt("TokenCount", currentToken.tokenNumber);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        Log.d("ONRESTInst", "InsideRestoreInstance");
        super.onRestoreInstanceState(savedInstanceState);
        currentToken.setTokenNumber(savedInstanceState.getInt("TokenCount"));
        textToUpdate = (TextView) findViewById(R.id.token_count);
        textToUpdate.setText(String.valueOf(currentToken.tokenNumber));
    }

    private void loadSavedPreferences(NavigationView navigationView) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        ip_address = sharedPreferences.getString("pref_key_ip_address", "192.168.1.1");
        port_number = sharedPreferences.getString("pref_key_port_id", "8000");
        doctor_name = sharedPreferences.getString("pref_key_user_name", "Doctor Name");
        doctor_id = sharedPreferences.getString("pref_key_user_id", "Doctor ID");
        hospital_name = sharedPreferences.getString("pref_key_hospital_name", "Hospital Name");
        View headerView = navigationView.getHeaderView(0);
        textToUpdate = (TextView) headerView.findViewById(R.id.drawerDoctorName);
        textToUpdate.setText(doctor_name);
        textToUpdate = (TextView) headerView.findViewById(R.id.drawerHospitalName);
        textToUpdate.setText(hospital_name);

    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_settings) {
            LayoutInflater layoutInflaterAndroid = LayoutInflater.from(c);
            View mView = layoutInflaterAndroid.inflate(R.layout.settings_password, null);
            AlertDialog.Builder alertDialogBuilderUserInput = new AlertDialog.Builder(c, R.style.EnterTokenDialogTheme);
            alertDialogBuilderUserInput.setView(mView);

            final EditText userInputDialogEditText = (EditText) mView.findViewById(R.id.userInputDialog);
            alertDialogBuilderUserInput
                    .setCancelable(false)
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialogBox, int id) {
                            // ToDo get user input here
                            Log.d("UserInput", userInputDialogEditText.getText().toString());
                            if (userInputDialogEditText.getText().toString().equals("blueOcean")) {
                                Intent prefIntent = new Intent(MainActivity.this, MyPreferencesActivity.class);
                                startActivity(prefIntent);
                            } else {
                                Toast.makeText(getApplicationContext(), "Wrong Password", Toast.LENGTH_LONG).show();
                            }
                        }
                    })

                    .setNegativeButton("Cancel",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialogBox, int id) {
                                    dialogBox.cancel();
                                }
                            });

            AlertDialog alertDialogAndroid = alertDialogBuilderUserInput.create();
            alertDialogAndroid.show();

        } else if (id == R.id.nav_reset) {
            currentToken.setTokenNumber(0);
            dbHelper.deleteAll();
            adapter.clearAllTokens();
            textToUpdate = (TextView) findViewById(R.id.token_count);
            textToUpdate.setText(String.valueOf(currentToken.tokenNumber));
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void setListItemToRemove(int position, int tokenNumber){
        positionDeleteStatus = true;
        positionToDelete = position;
        positoinToDeteteToken = tokenNumber;
    }

    @Override
    public void updateTokenStatus(int token, boolean status) {
        dbHelper.updateTokenStatus(token, status);
    }

    @Override
    public void listAllTokens() {
        adapter.updateListOfTokens(dbHelper.getAllTokens());
    }

    @Override
    public void listUnattentedTokens() {
        adapter.updateListOfTokens(dbHelper.getUnAttentedTokens());
    }

    @Override
    public void enterTokenNumber() {

        LayoutInflater layoutInflaterAndroid = LayoutInflater.from(c);
        View mView = layoutInflaterAndroid.inflate(R.layout.enter_token_number, null);
        AlertDialog.Builder alertDialogBuilderUserInput = new AlertDialog.Builder(c, R.style.EnterTokenDialogTheme);
        alertDialogBuilderUserInput.setView(mView);

        final EditText userInputDialogEditText = (EditText) mView.findViewById(R.id.enteredTokenNumber);
        alertDialogBuilderUserInput
                .setCancelable(false)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogBox, int id) {
                        // TODO Tget user input here
                        if (userInputDialogEditText.getText().toString().length() == 0) {
                            dialogBox.cancel();
                        } else {
                            if (isSocketFree.isSocketBusy()) {
                                return;
                            }
                            isSocketFree.blockSocket();
                            int nextTokenNumber = Integer.parseInt(userInputDialogEditText.getText().toString());
                            updateLastNonCalledToken(nextTokenNumber);
                            goToToken(nextTokenNumber);
                        }
                    }
                })

                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialogBox, int id) {
                                dialogBox.cancel();
                            }
                        });

        AlertDialog alertDialogAndroid = alertDialogBuilderUserInput.create();
        alertDialogAndroid.show();

    }

    @Override
    public void updateLastNonCalledToken(int nextTokenNumber){
            if(nextTokenNumber > currentToken.tokenNumber && (nextTokenNumber != (currentToken.tokenNumber+1))){
                lastNonCalledToken = currentToken.tokenNumber+1;
                while (dbHelper.isTokenPresent(lastNonCalledToken))
                    lastNonCalledToken++;
            }
    }

    @Override
    public void updateTokenHeaderAndTitle(TokenData tokenData) {
        currentToken.setTokenNumber(tokenData.tokenNumber);
        currentToken.setTokenStatus(tokenData.tokenStatus);
        if(adapter.isUnattentedSelected()){
            listUnattentedTokens();
        }else{
            listAllTokens();
        }
        textToUpdate = (TextView) findViewById(R.id.token_count);
        textToUpdate.setText(String.valueOf(currentToken.tokenNumber));
    }

    @Override
    public void updateViewAndDb(TokenData tokenData) {
        if (currentToken.tokenNumber == 0) {
            currentToken.setTokenNumber(1);
            currentToken.setTokenStatus(true);
            adapter.notifyItemChanged(0);
            textToUpdate = (TextView) findViewById(R.id.token_count);
            textToUpdate.setText(String.valueOf(currentToken.tokenNumber));
        }
    }

    @Override
    public boolean isTokenPresent(int tokenNumber){
      return dbHelper.isTokenPresent(tokenNumber);
    }

    public void goToToken(int newTokenNumber) {

        if (!getTokenConnectionStatus()) {
            return;
        }

        boolean isNewTokenPresent = dbHelper.isTokenPresent(newTokenNumber);
        boolean isOldTokenPresent = dbHelper.isTokenPresent(currentToken.tokenNumber);

        int oldTokennumber = currentToken.tokenNumber;
        boolean oldTokenStatus = currentToken.tokenStatus;

        TokenData oldTokenData = new TokenData(oldTokennumber, oldTokenStatus);

        if (isNewTokenPresent) {
            boolean tokenStatus = dbHelper.getTokenStatus(newTokenNumber);
            if (!tokenStatus) {
                if(isOldTokenPresent) {
                    new SendPostRequest(c, new TokenData(newTokenNumber, tokenStatus), "UPDATEONLYHEADER")
                            .execute(ip_address + ":" + port_number);
                }else{
                    new SendPostRequest(c, oldTokenData, isOldTokenPresent, new TokenData(newTokenNumber, tokenStatus), "UPDATEHEADERLISTDB")
                            .execute(ip_address + ":" + port_number);
                }
                //updateTokenHeaderAndTitle(new TokenData(newTokenNumber, true));
            } else {
                int nextNonCalledToken = newTokenNumber;
                while (dbHelper.isTokenPresent(nextNonCalledToken))
                    nextNonCalledToken++;
                showTokenPrensentDialog(nextNonCalledToken, newTokenNumber, isOldTokenPresent, oldTokenData);
            }
        } else {
            //update header
            //updatedlist
            new SendPostRequest(c, oldTokenData, isOldTokenPresent, new TokenData(newTokenNumber, true), "UPDATEHEADERLISTDB")
                    .execute(ip_address + ":" + port_number);
            //if(!isOldTokenPresent) {
            //    dbHelper.insertTokenDetail(oldTokenData);
            //    adapter.addElementToTokenList(oldTokenData);
            //}
            //updateTokenHeaderAndTitle(new TokenData(newTokenNumber, true));
        }
    }

    @Override
    public boolean getTokenConnectionStatus() {
        WifiManager wifiMgr = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        if (wifiMgr.isWifiEnabled()) { // Wi-Fi adapter is ON
            WifiInfo wifiInfo = wifiMgr.getConnectionInfo();
            if (wifiInfo.getNetworkId() == -1) {
                Snackbar.make(coordinatorLayout, "Not connected to an access point", Snackbar.LENGTH_LONG).show();
                isSocketFree.openSocket();
                positionDeleteStatus = false;
                return false;
                // Not connected to an access point
            } else {
                //Snackbar.make(coordinatorLayout, "Connected to an access point " + Integer.toString(wifiInfo.getNetworkId()), Snackbar.LENGTH_LONG).show();
                return true;
            }
            // Connected to an access point
        } else {
            Snackbar.make(coordinatorLayout, "Wi-Fi adapter is OFF", Snackbar.LENGTH_LONG).show();
            isSocketFree.openSocket();
            positionDeleteStatus = false;
            return false;
            // Wi-Fi adapter is OFF
        }

    }

    private void showTokenPrensentDialog(final int nextNonCalledToken, final int currentSelectedToken, final boolean isOldTokenPresent, final TokenData oldTokenData ) {
        AlertDialog.Builder builder = new AlertDialog.Builder(c, R.style.EnterTokenDialogTheme);
        builder.setTitle("Token Already Visited");
        builder.setMessage("This Token is already served. Click OK to move to next nonserved token number("
                + Integer.toString(nextNonCalledToken) + ")");

        builder.setCancelable(false);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogBox, int id) {

                new SendPostRequest(c, oldTokenData, isOldTokenPresent, new TokenData(nextNonCalledToken, true), "UPDATEHEADERLISTDB")
                        .execute(ip_address + ":" + port_number);

            }
        });
        builder.setNegativeButton("RECALL", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogBox, int id) {
                new SendPostRequest(c, oldTokenData, isOldTokenPresent, new TokenData(currentSelectedToken, true), "UPDATEHEADERLISTDB")
                        .execute(ip_address + ":" + port_number);

            }
        });


        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void showNoUnattentedTokensBehind() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("All Previous Tokens are Served");
        builder.setMessage("This is message");


        builder.setPositiveButton("OK", null);


        AlertDialog dialog = builder.create();
        dialog.show();
    }

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    public Action getIndexApiAction() {
        Thing object = new Thing.Builder()
                .setName("Main Page") // TODO: Define a title for the content shown.
                // TODO: Make sure this auto-generated URL is correct.
                .setUrl(Uri.parse("http://[ENTER-YOUR-URL-HERE]"))
                .build();
        return new Action.Builder(Action.TYPE_VIEW)
                .setObject(object)
                .setActionStatus(Action.STATUS_TYPE_COMPLETED)
                .build();
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        AppIndex.AppIndexApi.start(client, getIndexApiAction());
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        AppIndex.AppIndexApi.end(client, getIndexApiAction());
        client.disconnect();
    }

    public class SendPostRequest extends AsyncTask<String, Void, String> {

        Listener listener;
        int tokenNumberToUpdate;
        boolean tokenStatusToUpdate;
        TokenData oldTokenData = new TokenData();
        boolean isOldTokenPresent = true;
        String filter;

        SendPostRequest(Context c, TokenData tokenData, String filter) {
            this.listener = (Listener) c;
            this.tokenNumberToUpdate = tokenData.tokenNumber;
            this.tokenStatusToUpdate = tokenData.tokenStatus;
            this.filter = filter;
        }

        SendPostRequest(Context c, TokenData oldTokenData, boolean isOldTokenPresent, TokenData newTokenData, String filter) {
            this.listener = (Listener) c;
            this.tokenNumberToUpdate = newTokenData.tokenNumber;
            this.tokenStatusToUpdate = newTokenData.tokenStatus;
            this.oldTokenData = oldTokenData;
            this.isOldTokenPresent = isOldTokenPresent;
            this.filter = filter;
        }

        @Override
        protected String doInBackground(String... params) {
            return POST(params[0], new TokenData(tokenNumberToUpdate, tokenStatusToUpdate), doctor_name, doctor_id);
        }

        @Override
        protected void onPostExecute(String result) {

            if (!result.equals("SUCCESS")) {
                Snackbar.make(coordinatorLayout, "Error Contacting Server ... Sorry Try Again.", Snackbar.LENGTH_LONG).show();
                isSocketFree.openSocket();
                clearLastNonCalledToken = false;
                return;
            }

            if(clearLastNonCalledToken){
                lastNonCalledToken = 0;
                clearLastNonCalledToken = false;
            }

            if (filter.equals("UPDATEZERO")) {
                listener.updateViewAndDb(new TokenData(tokenNumberToUpdate, tokenStatusToUpdate));
            } else if (filter.equals("UPDATEONLYHEADER")) {
                listener.updateTokenHeaderAndTitle(new TokenData(tokenNumberToUpdate, tokenStatusToUpdate));
            } else if (filter.equals("UPDATEHEADERLISTDB")) {
                if (!isOldTokenPresent) {
                    dbHelper.insertTokenDetail(oldTokenData);
                    adapter.addElementToTokenList(oldTokenData);
                }
                listener.updateTokenHeaderAndTitle(new TokenData(tokenNumberToUpdate, tokenStatusToUpdate));
            }
            //Toast.makeText(getApplicationContext(), result,
            //  Toast.LENGTH_LONG).show();
            isSocketFree.openSocket();
        }
    }

    public static String POST(String url, TokenData tokenData, String doctor_name, String doctor_id) {
        String result = "";
        try {
            URL serverUrl = new URL("http://" + url);

            JSONObject postDataParameters = new JSONObject();
            postDataParameters.put("doctorName", doctor_name);
            postDataParameters.put("doctorID", doctor_id);
            postDataParameters.put("TokenNumber", Integer.toString(tokenData.tokenNumber));


            HttpURLConnection httpURLConnection = (HttpURLConnection) serverUrl.openConnection();
            httpURLConnection.setReadTimeout(3000 /* milliseconds */);
            httpURLConnection.setConnectTimeout(3000 /* milliseconds */);
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setDoInput(true);
            httpURLConnection.setDoOutput(true);

            OutputStream os = httpURLConnection.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
            writer.write(getPostDataString(postDataParameters));
            writer.flush();
            writer.close();
            os.close();

            int responseCode = httpURLConnection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {

                BufferedReader in = new BufferedReader(
                        new InputStreamReader(httpURLConnection.getInputStream()));
                StringBuilder sb = new StringBuilder("");
                String line;

                while ((line = in.readLine()) != null) {

                    sb.append(line);
                    //break;
                }

                in.close();
                return sb.toString();

            } else {
                return "false : " + String.valueOf(responseCode);
            }

            //HttpURLConnection
        } catch (Exception e) {
            e.printStackTrace();
        }


        return result;
    }

    private static String getPostDataString(JSONObject postDataParameters) throws Exception {
        StringBuilder result = new StringBuilder();
        boolean first = true;

        Iterator<String> itr = postDataParameters.keys();

        while (itr.hasNext()) {

            String key = itr.next();
            Object value = postDataParameters.get(key);


            if (first)
                first = false;
            else
                result.append("&");

            result.append(URLEncoder.encode(key, "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(value.toString(), "UTF-8"));

        }

        return result.toString();
    }

}


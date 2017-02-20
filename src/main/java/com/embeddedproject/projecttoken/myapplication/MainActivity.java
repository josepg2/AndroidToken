package com.embeddedproject.projecttoken.myapplication;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
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

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, Listener{

    CollapsingToolbarLayout collapsingToolbarLayout;
    RecyclerView recyclerView;
    DbHelper dbHelper;
    ListAdapter adapter;

    int LastNonCalledToken = 0;

    final Context c = this;
    TextView textToUpdate;
    TokenData currentToken = new TokenData(0, true);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("ONCREATE", "Oncreate");

        if(savedInstanceState != null) {
            currentToken.setTokenNumber(savedInstanceState.getInt("TokenCount"));
        } else{
            SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
            currentToken.setTokenNumber(sharedPref.getInt("TokenCount", currentToken.tokenNumber));
        }

        setContentView(R.layout.activity_main);

        //ToolBar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if(getSupportActionBar() != null){
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        collapsingToolbarLayout = (CollapsingToolbarLayout)findViewById(R.id.collapsing_toolbar);
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
                    collapsingToolbarLayout.setTitle("Token : "+currentToken.tokenNumber);
                    isShow = true;
                } else if(isShow) {
                    collapsingToolbarLayout.setTitle("");
                    isShow = false;
                }
            }
        });

        textToUpdate = (TextView) findViewById(R.id.token_count);
        textToUpdate.setText(Integer.toString(currentToken.tokenNumber));

        dbHelper = DbHelper.getInstance(getApplicationContext());

        recyclerView = (RecyclerView) findViewById(R.id.rv_tokenlist);
        adapter = new ListAdapter(this, currentToken, dbHelper.getUnAttentedTokens());
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        RecyclerView.ItemAnimator animator = recyclerView.getItemAnimator();
        if(animator instanceof SimpleItemAnimator){
            ((SimpleItemAnimator)animator).setSupportsChangeAnimations(false);
        }


        ImageButton countBackButton = (ImageButton) findViewById(R.id.back_button);
        countBackButton.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                //Do stuff here
                if(LastNonCalledToken == 0){
                    showNoUnattentedTokensBehind();
                    return;
                }else{
                    goToToken(LastNonCalledToken);
                    LastNonCalledToken = 0;
                }


            }
        });

        ImageButton countForwardButton = (ImageButton) findViewById(R.id.forward_button);
        countForwardButton.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                //Do stuff here
                if(currentToken.tokenNumber == 0){
                    currentToken.setTokenNumber(1);
                    currentToken.setTokenStatus(true);
                    adapter.notifyItemChanged(0);
                    textToUpdate = (TextView) findViewById(R.id.token_count);
                    textToUpdate.setText(Integer.toString(currentToken.tokenNumber));
                    return;
                }
                goToToken( currentToken.tokenNumber + 1 );
            }
        });


        //Drawer Layout
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        //Navigation View
        NavigationView navigationView = (NavigationView) findViewById(R.id.navigation_drawer_bottom);
        navigationView.setNavigationItemSelectedListener(this);
        loadSavedPreferences((NavigationView) findViewById(R.id.navigation_drawer_top));


    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d("lifecycle","onPause invoked");
        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt("TokenCount", currentToken.tokenNumber);
        editor.putBoolean("TokenStatus", currentToken.tokenStatus);
        editor.commit();
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
    protected void onDestroy(){
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
        textToUpdate.setText(Integer.toString(currentToken.tokenNumber));
    }

    private void loadSavedPreferences(NavigationView navigationView) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String doctor_name = sharedPreferences.getString("pref_key_user_name", "Doctor Name");
        String hospital_name = sharedPreferences.getString("pref_key_hospital_name", "Hospital Name");
        View headerView = navigationView.getHeaderView(0);
        textToUpdate = (TextView) headerView.findViewById(R.id.drawerDoctorName);
        textToUpdate.setText(doctor_name);
        textToUpdate = (TextView) headerView.findViewById(R.id.drawerHospitalName);
        textToUpdate.setText(hospital_name);
    }

  // @Override
    //public boolean onCreateOptionsMenu(Menu menu) {
        //Inflate the menu; this adds items to the action bar if it is present.
     //  getMenuInflater().inflate(R.menu.main, menu);
    //   return true;
    //}

   //@Override
   // public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
  //      int id = item.getItemId();

        //noinspection SimplifiableIfStatement
 //       if (id == R.id.action_settings) {
//            Intent prefIntent1 = new Intent(this, MyPreferencesActivity.class);
  //          startActivity(prefIntent1);
  //          return true;
  //      }
//
  //      return super.onOptionsItemSelected(item);
   // }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_settings) {
            LayoutInflater layoutInflaterAndroid = LayoutInflater.from(c);
            View mView = layoutInflaterAndroid.inflate(R.layout.settings_password, null);
            AlertDialog.Builder alertDialogBuilderUserInput = new AlertDialog.Builder(c);
            alertDialogBuilderUserInput.setView(mView);

            final EditText userInputDialogEditText = (EditText) mView.findViewById(R.id.userInputDialog);
            alertDialogBuilderUserInput
                    .setCancelable(false)
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialogBox, int id) {
                            // ToDo get user input here
                            Log.d("UserInput", userInputDialogEditText.getText().toString());
                            if(userInputDialogEditText.getText().toString().equals("blueOcean")){
                                Intent prefIntent = new Intent(MainActivity.this, MyPreferencesActivity.class);
                                startActivity(prefIntent);
                            }else{
                                Toast.makeText(getApplicationContext(),"Wrong Password",Toast.LENGTH_LONG).show();
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
            textToUpdate.setText(Integer.toString(currentToken.tokenNumber));
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void updateTokenStatus(int token, boolean status) {
        dbHelper.updateTokenStatus(token,status);
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

    }

    @Override
    public void updateTokenHeaderAndTitle(TokenData tokenData) {
        currentToken.setTokenNumber(tokenData.tokenNumber);
        currentToken.setTokenStatus(true);
        adapter.notifyItemChanged(0);
        textToUpdate = (TextView) findViewById(R.id.token_count);
        textToUpdate.setText(Integer.toString(currentToken.tokenNumber));
    }

    public void goToToken(int newTokenNumber){
        boolean isNewTokenPresent = dbHelper.isTokenPresent(newTokenNumber);
        boolean isOldTokenPresent = dbHelper.isTokenPresent(currentToken.tokenNumber);

        int oldTokennumber = currentToken.tokenNumber;
        boolean oldTokenStatus = currentToken.tokenStatus;

        TokenData oldTokenData = new TokenData(oldTokennumber,oldTokenStatus);

        if(isNewTokenPresent){
            boolean tokenStatus = dbHelper.getTokenStatus(newTokenNumber);
            if(!tokenStatus){
                updateTokenHeaderAndTitle(new TokenData(newTokenNumber, true));
            }else{
                showTokenPrensentDialog();
            }
        }else{
            //update header
            //updatedlist
            if(!isOldTokenPresent) {
                dbHelper.insertTokenDetail(oldTokenData);
                adapter.addElementToTokenList(oldTokenData);
            }
            updateTokenHeaderAndTitle(new TokenData(newTokenNumber, true));
        }
    }

    private void showTokenPrensentDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Already Called");
        builder.setMessage("This is message");

        builder.setCancelable(false);
        builder.setPositiveButton("OK", null);
        builder.setNegativeButton("RECALL",null);


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

}

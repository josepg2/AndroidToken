package com.embeddedproject.projecttoken.myapplication;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, Listener {

    CollapsingToolbarLayout collapsingToolbarLayout;
    RecyclerView recyclerView;
    DbHelper dbHelper;
    ListAdapter adapter;

    final Context c = this;
    TextView textToUpdate;
    Integer TokenCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("ONCREATE", "Oncreate");

        if(savedInstanceState != null) {
            TokenCount = savedInstanceState.getInt("TokenCount");
            Log.d("ONCREATE", "SavedNotNull");
            Log.d("ONCREATE", Integer.toString(TokenCount));
        } else{
            SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
            TokenCount = sharedPref.getInt("TokenCount", TokenCount);
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
                    collapsingToolbarLayout.setTitle("Token : "+TokenCount);
                    isShow = true;
                } else if(isShow) {
                    collapsingToolbarLayout.setTitle("");
                    isShow = false;
                }
            }
        });

        textToUpdate = (TextView) findViewById(R.id.token_count);
        textToUpdate.setText(Integer.toString(TokenCount));

        dbHelper = DbHelper.getInstance(getApplicationContext());

        recyclerView = (RecyclerView) findViewById(R.id.rv_tokenlist);
        adapter = new ListAdapter(this, dbHelper.getAllTokens());
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));


        ImageButton countBackButton = (ImageButton) findViewById(R.id.back_button);
        countBackButton.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                //Do stuff here
                textToUpdate = (TextView) findViewById(R.id.token_count);

                if(TokenCount > 1) {
                    TokenCount--;
                }
                textToUpdate.setText(Integer.toString(TokenCount));

                Log.d("ButtonClick", "Count Back Clicked");
            }
        });

        ImageButton countForwardButton = (ImageButton) findViewById(R.id.forward_button);
        countForwardButton.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                //Do stuff here
                textToUpdate = (TextView) findViewById(R.id.token_count);
                textToUpdate.setText(Integer.toString(TokenCount + 1));
                TokenCount++;
                dbHelper.insertTokenDetail(TokenCount, true);
                adapter.addElementToTokenList(TokenCount, true);
                Log.d("ButtonClick", "Count Forward Clicked");
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

    protected void onPause() {
        super.onPause();
        Log.d("lifecycle","onPause invoked");
        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt("TokenCount", TokenCount);
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
        outState.putInt("TokenCount", TokenCount);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        Log.d("ONRESTInst", "InsideRestoreInstance");
        super.onRestoreInstanceState(savedInstanceState);
        TokenCount = savedInstanceState.getInt("TokenCount");
        textToUpdate = (TextView) findViewById(R.id.token_count);
        textToUpdate.setText(Integer.toString(TokenCount));
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
            TokenCount = 0;
            dbHelper.deleteAll();
            adapter.clearAllTokens();
            textToUpdate = (TextView) findViewById(R.id.token_count);
            textToUpdate.setText(Integer.toString(TokenCount));
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void tokenToCall(int tokentocall) {

    }
}

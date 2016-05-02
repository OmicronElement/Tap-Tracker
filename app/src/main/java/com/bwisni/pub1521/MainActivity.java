package com.bwisni.pub1521;

import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;


import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnItemClick;
import butterknife.OnItemLongClick;


public class MainActivity extends AppCompatActivity {

    @Bind(R.id.drinkersListView) ListView drinkersListView;
    @Bind(R.id.fab) FloatingActionButton fab;

    MediaPlayer mediaPlayer;

    private ArrayList<Drinker> drinkersArrayList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ButterKnife.bind(this);

        /*
        drinkersArrayList.add(new Drinker("Jon", 5));
        drinkersArrayList.add(new Drinker("Ken", 5));
        drinkersArrayList.add(new Drinker("Chris", 5));

        saveData();
        */

        loadData();

        DrinkerAdapter listAdapter = new DrinkerAdapter(this, drinkersArrayList);
        drinkersListView.setAdapter(listAdapter);


    }

    // Thanks to Shinan Kozak
    private void saveData() {
        Log.i("JSON", "saveData");
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = sharedPrefs.edit();
        Gson gson = new Gson();

        String json = gson.toJson(drinkersArrayList);

        editor.putString("Pub1521JSON", json);
        editor.commit();
    }

    private void loadData() {
        Log.i("JSON", "loadData");
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        Gson gson = new Gson();
        String json = sharedPrefs.getString("Pub1521JSON", null);
        Type type = new TypeToken<ArrayList<Drinker>>() {}.getType();
        
        drinkersArrayList = gson.fromJson(json, type);

        drinkersListView.invalidateViews();
    }

    @OnItemClick(R.id.drinkersListView) void onItemClick(int position, View view) {
        Drinker d = drinkersArrayList.get(position);


        if(d.getCredits() == 0){
            mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.alarm);
            mediaPlayer.start();
        }
        else{
            mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.beer);
            mediaPlayer.start();

            d.subtractCredit();
        }

        Snackbar.make(view, d.getCredits() + " beers remaining", Snackbar.LENGTH_LONG)
                .setAction("UNDO", null)
                .show();
    }

    @OnItemLongClick(R.id.drinkersListView) boolean onItemLongClick(int position, View view) {
        // Create intent to open up dialogue
        Intent intent = new Intent(getApplicationContext(),
               EditDrinkerActivity.class);

        intent.putExtra("drinkerPosition", position);
        intent.putExtra("drinkerName", drinkersArrayList.get(position).getName());
        intent.putExtra("drinkerCredits", drinkersArrayList.get(position).getCredits());

        startActivityForResult(intent, 1);
        return true;
    }

    // Add a drinker
    @OnClick(R.id.fab) public void onFabClick(View view) {
        // Create intent to open up dialogue
        Intent intent = new Intent(getApplicationContext(),
                AddDrinkerActivity.class);

        startActivityForResult(intent, 0);
    }

    // Returning from a dialogue
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        Log.i("onActivityResult", "called");

        if (requestCode == 0 && resultCode == RESULT_OK && intent != null) {
            Log.i("onActivityResult", "RESULT_OK");
            String name = intent.getStringExtra("name");

            addDrinker(name, 0);
        }

        if (requestCode == 1 && resultCode == RESULT_OK && intent != null) {
            Log.i("onActivityResult", "RESULT_OK");
            int credits = intent.getIntExtra("drinkerCredits", 0);
            int position = intent.getIntExtra("drinkerPosition", -1);

            boolean delete = intent.getBooleanExtra("delete", false);

            if(delete){
                removeDrinker(position);
            }
            else{
                Drinker drinker = drinkersArrayList.get(position);
                drinker.setCredits(credits);
            }

        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStop() {
        super.onStop();

        mediaPlayer.release();
        mediaPlayer = null;

        saveData();
    }

    /*@Override
   protected void onResume() {
        super.onResume();
    }*/

    public void addDrinker(String name, int credits){
        drinkersArrayList.add(new Drinker(name, credits));
        saveData();
        drinkersListView.invalidateViews();
    }

    public void removeDrinker(int position){
        drinkersArrayList.remove(position);
        saveData();
        drinkersListView.invalidateViews();
    }
}

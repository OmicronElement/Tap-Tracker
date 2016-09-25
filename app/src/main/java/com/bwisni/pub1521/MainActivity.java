package com.bwisni.pub1521;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.Toolbar;
import android.widget.ViewSwitcher;

import com.orm.SugarContext;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import be.appfoundry.nfclibrary.activities.NfcActivity;
import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnItemLongClick;


public class MainActivity extends NfcActivity {
    public static final String NDEF_PREFIX = "sms:"+AddDrinkerActivity.SMS_NUMBER+"?body=";
    public static final String ADMIN_NFC_ID = "664de4d0-0586-40aa-9518-a854d2657982";
    public static final int ADD_REQ_CODE = 0;
    public static final int EDIT_REQ_CODE = 1;
    public static final int CONFIRM_REQ_CODE = 2;
    public static final int ADMIN_REQ_CODE = 3;

    @Bind(R.id.drinkersListView) ListView drinkersListView;
    @Bind(R.id.numServedTextView) TextSwitcher numServedTextSwitcher;

    ArrayList<Drinker> drinkersArrayList = new ArrayList<>();

    private long totalServed;
    private boolean admin = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setActionBar(toolbar);

        ButterKnife.bind(this);
        SugarContext.init(getApplicationContext());

        numServedTextSwitcher.setInAnimation(getApplicationContext(),android.support.design.R.anim.abc_slide_in_top);

        numServedTextSwitcher.setFactory(new ViewSwitcher.ViewFactory() {

            public View makeView() {
                TextView myText = new TextView(getApplicationContext());
                myText.setGravity(Gravity.CENTER);

                FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT,
                        Gravity.CENTER);
                myText.setLayoutParams(params);

                myText.setTextSize(56);
                myText.setTextColor(Color.WHITE);
                return myText;
            }
        });

        drinkersListView.bringToFront();

        loadData();
    }

    private void saveData() {
        for (Drinker d : drinkersArrayList){
            d.save();
        }

        // Save totalServed to SharedPrefs
        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putLong("totalServed", totalServed);
        editor.apply();
    }

    private void loadData() {
        drinkersArrayList = new ArrayList<>(Drinker.listAll(Drinker.class));

        printArr();

        DrinkerAdapter listAdapter = new DrinkerAdapter(this, drinkersArrayList);
        drinkersListView.setAdapter(listAdapter);

        drinkersListView.invalidateViews();

        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        totalServed = sharedPref.getLong("totalServed", 30000);

        updateTotalServed();
    }

    private void printArr(){
        for(Drinker d : drinkersArrayList)
            Log.d("ARRAY",d.toString());

        drinkersListView.toString();
    }

    private void updateTotalServed() {
        // Execute after 1 second has passed
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                numServedTextSwitcher.setText(NumberFormat.getNumberInstance(Locale.US).format(totalServed));;
            }
        }, 500);

    }


    private void openConfirmActivity(int position) {
        Intent intent = new Intent(getApplicationContext(),
                ConfirmActivity.class);

        intent.putExtra("drinker", drinkersArrayList.get(position));
        intent.putExtra("drinkerPosition", position);

        startActivityForResult(intent, CONFIRM_REQ_CODE);
    }

    @OnItemLongClick(R.id.drinkersListView) boolean onItemLongClick(int position, View view) {
        // Require six taps to enter admin dialogue

        // Create intent to open up dialogue
        Intent intent = new Intent(getApplicationContext(),
                EditDrinkerActivity.class);


        intent.putExtra("drinker", drinkersArrayList.get(position));
        intent.putExtra("drinkerPosition", position);
        /*intent.putExtra("drinkerName", drinkersArrayList.get(position).name);
        intent.putExtra("drinkerCredits", drinkersArrayList.get(position).credits);*/

        startActivityForResult(intent, EDIT_REQ_CODE);

        return true;
    }

    // Add a drinker
    @OnClick(R.id.fab) public void onFabClick(View view) {
        // Create intent to open up dialogue
        Intent intent = new Intent(getApplicationContext(),
                AddDrinkerActivity.class);

        startActivityForResult(intent, ADD_REQ_CODE);
    }

    // Returning from a dialogue
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        Log.i("onActivityResult", "called");

        // Returning from Add Drinker
        if (requestCode == ADD_REQ_CODE && resultCode == RESULT_OK && intent != null) {
            Log.i("onActivityResult", "RESULT_OK");
            String name = intent.getStringExtra("name");
            String nfcId = intent.getStringExtra("nfcId");

            // Start with 6 credits
            addDrinker(name, 6, nfcId);
        }
        // Returning from Edit Drinker
        if (requestCode == EDIT_REQ_CODE && resultCode == RESULT_OK && intent != null) {
            Log.i("onActivityResult", "RESULT_OK");
            int credits = intent.getIntExtra("drinkerCredits", 0);
            int position = intent.getIntExtra("drinkerPosition", -1);

            boolean delete = intent.getBooleanExtra("delete", false);

            if(delete){
                removeDrinker(position);
            }
            else{
                Drinker drinker = drinkersArrayList.get(position);
                drinker.credits = credits;
            }

        }
        // Returning from Confirm with successful credit use
        if (requestCode == CONFIRM_REQ_CODE && resultCode == RESULT_OK && intent != null) {
            Log.i("onActivityResult", "RESULT_OK");
            int position = intent.getIntExtra("drinkerPosition", -1);

            drinkersArrayList.get(position).subtractCredit();

            totalServed++;
        }
        // Returning from admin password check
        if (requestCode == ADMIN_REQ_CODE && resultCode == RESULT_OK && intent != null) {
            toggleAdminMode();
        }

        saveData();
        updateTotalServed();
    }

    private void toggleAdminMode() {
        if(admin) {
            admin = false;
            drinkersListView.setVisibility(View.INVISIBLE);
        }
        else{
            admin = true;
            drinkersListView.setVisibility(View.VISIBLE);
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

        if (id == R.id.action_admin) {
            checkAdminPassword();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void checkAdminPassword() {
        Intent intent = new Intent(getApplicationContext(),
                PasswordActivity.class);

        startActivityForResult(intent, ADMIN_REQ_CODE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        saveData();
    }

    //Called when NFC Tag has been read
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        /*for (String message : getNfcMessages()){
            Toast.makeText(this,message,Toast.LENGTH_SHORT).show();
        }*/

        List<String> msgs = getNfcMessages();

        //byte[] rawMessage = intent.getByteArrayExtra(NfcAdapter.EXTRA_ID);
        //String id = rawMessage.toString();


        for(String s : msgs){
            //Toast.makeText(this, s, Toast.LENGTH_SHORT).show();

            if(s.equals(NDEF_PREFIX+ADMIN_NFC_ID)) {
                toggleAdminMode();
                break;
            }

            for(Drinker d : drinkersArrayList) {
               String record = NDEF_PREFIX+d.nfcId;
                if(s.equals(record)) {
                   openConfirmActivity(drinkersArrayList.indexOf(d));
                   break;
               }
           }
        }

    }

    public void addDrinker(String name, int credits, String id){
        drinkersArrayList.add(new Drinker(name, credits, id));
        saveData();
        drinkersListView.invalidateViews();
    }

    public void removeDrinker(int position){
        //Remove from arraylist and delete() sugar record
        drinkersArrayList.remove(position).delete();
        saveData();
        drinkersListView.invalidateViews();
    }
}

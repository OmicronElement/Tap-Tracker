package com.bwisni.pub1521;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.orm.SugarContext;

import java.util.ArrayList;
import java.util.List;

import be.appfoundry.nfclibrary.activities.NfcActivity;
import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnItemLongClick;


public class MainActivity extends NfcActivity {

    @Bind(R.id.drinkersListView) ListView drinkersListView;
    @Bind(R.id.numServedTextView) TextView numServedTextView;
    @Bind(R.id.fab) FloatingActionButton fab;

    ArrayList<Drinker> drinkersArrayList = new ArrayList<>();

    long totalServed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setActionBar(toolbar);

        ButterKnife.bind(this);
        SugarContext.init(getApplicationContext());

        Drinker.deleteAll(Drinker.class);
        drinkersArrayList.add(new Drinker("Jon",6,"entest"));
        saveData();

        loadData();
    }

    private void saveData() {
        for (Drinker d : drinkersArrayList){
            d.save();
        }
    }

    private void loadData() {
        drinkersArrayList = new ArrayList<>(Drinker.listAll(Drinker.class));

        printArr();


        DrinkerAdapter listAdapter = new DrinkerAdapter(this, drinkersArrayList);
        drinkersListView.setAdapter(listAdapter);

        drinkersListView.invalidateViews();

        countTotalServed();
        updateTotalServed();
    }
    private void printArr(){
        for(Drinker d : drinkersArrayList)
            Log.d("ARRAY",d.toString());

        drinkersListView.toString();
    }
    private void updateTotalServed() {
        countTotalServed();
        numServedTextView.setText("Over "+totalServed+" served");
    }

    private void countTotalServed() {
        totalServed = 30000;
        for (Drinker d : drinkersArrayList){
            totalServed+=d.totalDrank;
        }
    }


    private void openConfirmActivity(int position) {
        Intent intent = new Intent(getApplicationContext(),
                ConfirmActivity.class);

        intent.putExtra("drinker", drinkersArrayList.get(position));
        intent.putExtra("drinkerPosition", position);

        startActivityForResult(intent, 2);
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

        drinkersListView.setVisibility(View.INVISIBLE);

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

        // Returning from Add Drinker
        if (requestCode == 0 && resultCode == RESULT_OK && intent != null) {
            Log.i("onActivityResult", "RESULT_OK");
            String name = intent.getStringExtra("name");
            String nfcId = intent.getStringExtra("nfcId");

            // Start with 6 credits
            addDrinker(name, 6, nfcId);
        }
        // Returning from Edit Drinker
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
                drinker.credits = credits;
            }

        }
        // Returning from Confirm
        if (requestCode == 2 && resultCode == RESULT_OK && intent != null) {
            Log.i("onActivityResult", "RESULT_OK");
            int position = intent.getIntExtra("drinkerPosition", -1);

            drinkersArrayList.get(position).subtractCredit();

            totalServed++;
        }

        updateTotalServed();
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

        AlertDialog.Builder alert = new AlertDialog.Builder(this);

        alert.setTitle("Administrator Code");

        final EditText input = new EditText(this);
        /*input.setInputType(InputType.TYPE_NUMBER_VARIATION_PASSWORD); //Doesn't work
        input.requestFocus();*/

        alert.setView(input);

        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String value = input.getText().toString();
                // If password is correct, show drinkers list
                if(value.trim().equals("0323")){
                    drinkersListView.setVisibility(View.VISIBLE);
                }
                else{
                    Toast.makeText(MainActivity.this, "Incorrect code.", Toast.LENGTH_LONG).show();
                }

            }
        });

        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
            }
        });

        alert.show();


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
            Toast.makeText(this, s, Toast.LENGTH_SHORT).show();

            for(Drinker d : drinkersArrayList) {
               String record = "sms:1521?body="+d.nfcId;
                if(s.equals(record)) {
                   openConfirmActivity(drinkersArrayList.indexOf(d));
                   break;
               }
           }
        }

    }

    /*@Override
   protected void onResume() {
        super.onResume();
    }*/

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

package com.bwisni.pub1521;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
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
import butterknife.OnItemClick;
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
            Drinker drinkerEntry = Drinker.findById(Drinker.class, d.getId());
            drinkerEntry = d;
            drinkerEntry.save();
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

    @OnItemLongClick(R.id.drinkersListView) boolean onItemLongClick(int position, View view) {
        openConfirmActivity(position);
        return true;
    }

    private void openConfirmActivity(int position) {
        Intent intent = new Intent(getApplicationContext(),
                ConfirmActivity.class);

        intent.putExtra("drinker", drinkersArrayList.get(position));
        intent.putExtra("drinkerPosition", position);

        startActivityForResult(intent, 2);
    }

    int counter = 0;
    @OnItemClick(R.id.drinkersListView) void onItemClick(int position, View view) {
        counter++;
        // Require six taps to enter admin dialogue
        if(counter == 6) {
            // Create intent to open up dialogue
            Intent intent = new Intent(getApplicationContext(),
                    EditDrinkerActivity.class);

            intent.putExtra("drinkerPosition", position);
            intent.putExtra("drinkerName", drinkersArrayList.get(position).name);
            intent.putExtra("drinkerCredits", drinkersArrayList.get(position).credits);

            counter = 0;

            startActivityForResult(intent, 1);
        }
        //return true;
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

            addDrinker(name, 0, "");
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
                drinker.credits = credits;
            }

        }
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

        return super.onOptionsItemSelected(item);
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
               if(s.equals(d.nfcId)) {
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

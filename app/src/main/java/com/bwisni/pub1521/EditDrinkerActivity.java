package com.bwisni.pub1521;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.FormatException;
import android.nfc.NfcAdapter;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.os.Bundle;

import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import be.appfoundry.nfclibrary.exceptions.InsufficientCapacityException;
import be.appfoundry.nfclibrary.exceptions.ReadOnlyTagException;
import be.appfoundry.nfclibrary.exceptions.TagNotPresentException;
import be.appfoundry.nfclibrary.tasks.interfaces.AsyncOperationCallback;
import be.appfoundry.nfclibrary.tasks.interfaces.AsyncUiCallback;
import be.appfoundry.nfclibrary.utilities.async.WriteSmsNfcAsync;
import be.appfoundry.nfclibrary.utilities.interfaces.NfcWriteUtility;
import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnLongClick;

public class EditDrinkerActivity extends AppCompatActivity implements AsyncUiCallback{
    @Bind(R.id.editTextCredit) EditText editTextCredit;
    @Bind(R.id.editNameTextView) EditText nameTextView;
    @Bind(R.id.nfcIdtextView) TextView nfcIdTextView;

    private int position;
    private int credits;
    private String uuid;


    private PendingIntent pendingIntent;
    private IntentFilter[] mIntentFilters;
    private String[][] mTechLists;
    private NfcAdapter mNfcAdapter;

    private void initNfc() {
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        mIntentFilters = new IntentFilter[]{new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED)};
        mTechLists = new String[][]{new String[]{Ndef.class.getName()},
                new String[]{NdefFormatable.class.getName()}};
    }

    AsyncOperationCallback mAsyncOperationCallback = new AsyncOperationCallback() {

        @Override
        public boolean performWrite(NfcWriteUtility writeUtility) throws ReadOnlyTagException, InsufficientCapacityException, TagNotPresentException, FormatException {
            //NdefMessage ndefMessage = new NdefMessage(uuid.getBytes());
            return writeUtility.writeSmsToTagFromIntent(MainActivity.SMS_NUMBER, uuid, getIntent());
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_drinker);
        setTitle("Join the Party");

        ButterKnife.bind(this);
        initNfc();

        Intent intent = getIntent();

        Drinker drinker = (Drinker) intent.getSerializableExtra("drinker");

        position = intent.getIntExtra("drinkerPosition", -1);
        credits = drinker.getCredits();
        String name = drinker.getName();
        String nfcId = drinker.getNfcId();

        nameTextView.setText(name);
        nfcIdTextView.setText(nfcId);

        uuid = nfcId;

        refreshCreditEditText();
    }

    private void refreshCreditEditText() {
        editTextCredit.setText(String.valueOf(credits));
        editTextCredit.setSelection(editTextCredit.getText().length());
        editTextCredit.clearFocus();
    }

    @OnClick({R.id.okButton})
    void editDrinkerDone() {
        credits = Integer.parseInt(editTextCredit.getText().toString());

        // Sanity check
        credits = Math.abs(credits);

        // Send data back to Main activity
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);

        intent.putExtra("drinkerPosition", position);
        intent.putExtra("name", nameTextView.getText().toString());
        intent.putExtra("drinkerCredits", credits);

        setResult(RESULT_OK, intent);
        finish();

    }

    @OnLongClick({R.id.nfcIdtextView})
    boolean delDrinker(View v) {

        Intent intent = new Intent(getApplicationContext(), MainActivity.class);

        intent.putExtra("drinkerPosition", position);
        intent.putExtra("drinkerCredits", credits);

        setResult(RESULT_CANCELED, intent);
        finish();

        return true;
    }

    @OnClick({R.id.oneButton})
    void addOne(Button b) {
        credits += 1;
        refreshCreditEditText();
    }

    @OnClick({R.id.sixButton})
    void addSix(Button b) {
        credits += 6;
        refreshCreditEditText();
    }

    @OnClick({R.id.minusButton})
    void subOne(Button b) {
        credits -= 1;
        refreshCreditEditText();
    }

    // Create new card for this user on scan
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        new WriteSmsNfcAsync(this, mAsyncOperationCallback).executeWriteOperation();

        editDrinkerDone();
    }

    @Override
    public void callbackWithReturnValue(Boolean result) {
        if (result){
            Log.i("NFC", "Wrote UUID to tag:"+uuid);
        }
    }

    @Override
    public void onProgressUpdate(Boolean... booleans) {
        Log.i("NFC", booleans[0] ? "We started writing" : "We could not write!");
    }

    @Override
    public void onError(Exception e) {
        //Toast.makeText(this,"error",Toast.LENGTH_SHORT).show();
        Log.e("NFC",e.getMessage());
    }

    public void onResume(){
        super.onResume();
        if (mNfcAdapter != null) {
            mNfcAdapter.enableForegroundDispatch(this, pendingIntent, mIntentFilters, mTechLists);
        }
    }

    public void onPause(){
        super.onPause();
        if (mNfcAdapter != null)
        {
            mNfcAdapter.disableForegroundDispatch(this);
        }
    }

}

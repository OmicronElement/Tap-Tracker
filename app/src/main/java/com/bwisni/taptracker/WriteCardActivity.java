package com.bwisni.taptracker;

import android.content.Intent;
import android.nfc.FormatException;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import be.appfoundry.nfclibrary.activities.NfcActivity;
import be.appfoundry.nfclibrary.exceptions.InsufficientCapacityException;
import be.appfoundry.nfclibrary.exceptions.ReadOnlyTagException;
import be.appfoundry.nfclibrary.exceptions.TagNotPresentException;
import be.appfoundry.nfclibrary.tasks.interfaces.AsyncOperationCallback;
import be.appfoundry.nfclibrary.tasks.interfaces.AsyncUiCallback;
import be.appfoundry.nfclibrary.utilities.async.WriteSmsNfcAsync;
import be.appfoundry.nfclibrary.utilities.interfaces.NfcWriteUtility;
import butterknife.Bind;
import butterknife.ButterKnife;

public class WriteCardActivity extends NfcActivity implements AsyncUiCallback{
    @Bind(R.id.rewriteNameTextView)
    TextView nameView;
    @Bind(R.id.rewriteSecondaryTextView)
    TextView textView;

    private Drinker drinker;

    AsyncOperationCallback mAsyncOperationCallback = new AsyncOperationCallback() {

        @Override
        public boolean performWrite(NfcWriteUtility writeUtility) throws ReadOnlyTagException, InsufficientCapacityException, TagNotPresentException, FormatException {
            //NdefMessage ndefMessage = new NdefMessage(uuid.getBytes());
            return writeUtility.writeSmsToTagFromIntent(MainActivity.SMS_NUMBER, drinker.getNfcId(), getIntent());
        }

    };

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        new WriteSmsNfcAsync(this, mAsyncOperationCallback).executeWriteOperation();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rewrite_drinker);
        ButterKnife.bind(this);

        Intent intent = getIntent();
        drinker = (Drinker) intent.getSerializableExtra("drinker");

        String name = drinker.getName();
        nameView.setText(name);
    }

    @Override
    public void callbackWithReturnValue(Boolean result) {
        // Finish on successful write
        if(result)
            finish();
    }

    @Override
    public void onProgressUpdate(Boolean... booleans) {
    }

    @Override
    public void onError(Exception e) {
        Toast.makeText(this,e.getMessage(),Toast.LENGTH_SHORT).show();
    }
}

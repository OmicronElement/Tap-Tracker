package com.bwisni.taptracker;

import android.content.Context;
import android.content.Intent;
import android.nfc.FormatException;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import java.util.Objects;
import java.util.UUID;

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

public class AddDrinkerActivity extends NfcActivity implements AsyncUiCallback {
    @Bind(R.id.editTextName)
    EditText editTextName;

    // Generate unique user id
    String uuid = UUID.randomUUID().toString();

    AsyncOperationCallback mAsyncOperationCallback = new AsyncOperationCallback() {

        @Override
        public boolean performWrite(NfcWriteUtility writeUtility) throws ReadOnlyTagException,
                InsufficientCapacityException, TagNotPresentException, FormatException {
            return writeUtility.writeSmsToTagFromIntent(MainActivity.SMS_NUMBER, uuid, getIntent());
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_drinker);

        ButterKnife.bind(this);

        editTextName.requestFocus();
    }

    void okAddDrinker() {
        String name = editTextName.getText().toString();

        if (!name.equals("")) {
            // Send data back to Main activity
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);

            intent.putExtra("name", name);
            intent.putExtra("nfcId", uuid);

            setResult(RESULT_OK, intent);
            hideKeyboard(editTextName);
            finish();
        }
    }

    private void hideKeyboard(View view) {
        // Hide keyboard
        InputMethodManager imm = (InputMethodManager)
                getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        if (!Objects.equals(editTextName.getText().toString(), ""))
            new WriteSmsNfcAsync(this, mAsyncOperationCallback).executeWriteOperation();
    }

    @Override
    public void callbackWithReturnValue(Boolean result) {
        if (result) {
            Log.i("NFC", "Wrote UUID to tag:" + uuid);
            okAddDrinker();
        }
    }

    @Override
    public void onProgressUpdate(Boolean... booleans) {
        Log.i("NFC", booleans[0] ? "We started writing" : "We could not write!");
    }

    @Override
    public void onError(Exception e) {
        //Toast.makeText(this,"error",Toast.LENGTH_SHORT).show();
        //Log.e("NFC", e.getMessage());
    }
}


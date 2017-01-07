package com.bwisni.pub1521;

import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.nfc.FormatException;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.thebluealliance.spectrum.SpectrumDialog;

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
import butterknife.OnClick;
import butterknife.OnLongClick;
//TODO: fix NFC writing
public class EditDrinkerActivity extends AppCompatActivity implements AsyncUiCallback{
    @Bind(R.id.editTextCredit) EditText editTextCredit;
    @Bind(R.id.editNameTextView) EditText nameTextView;
    @Bind(R.id.nfcIdtextView) TextView nfcIdTextView;

    private int position;
    private int credits;
    private String uuid;
    private Drinker drinker;

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

        Intent intent = getIntent();

        drinker = (Drinker) intent.getSerializableExtra("drinker");

        position = intent.getIntExtra("drinkerPosition", -1);
        credits = drinker.getCredits();
        String name = drinker.getName();
        String nfcId = drinker.getNfcId();

        buildIcon();

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
        intent.putExtra("drinkerColor", drinker.getColor());

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

    @OnClick(R.id.edit_user_icon)
        void editColor() {
            showDialog();
    }


    // Create new card for this user on scan
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        new WriteSmsNfcAsync(this, mAsyncOperationCallback).executeWriteOperation();

        editDrinkerDone();
    }

    private void showDialog() {
        new SpectrumDialog.Builder(getApplicationContext())
                .setColors(getMatColors("300"))
                .setSelectedColor(drinker.getColor())
                .setDismissOnColorSelected(true)
                .setOutlineWidth(2)
                .setOnColorSelectedListener(new SpectrumDialog.OnColorSelectedListener() {
                    @Override public void onColorSelected(boolean positiveResult, @ColorInt int color) {
                        if (positiveResult) {
                            drinker.setColor(color);
                            buildIcon();
                        }
                    }
                }).build().show(getSupportFragmentManager(), "colorpicker");
    }

    private void buildIcon() {
        TextDrawable drawable = TextDrawable.builder()
                .beginConfig()
                .bold()
                .endConfig()
                .buildRound(drinker.getShortName(), drinker.getColor());

        ImageView image = (ImageView) findViewById(R.id.edit_user_icon);
        image.setImageDrawable(drawable);
    }

    public int[] getMatColors(String typeColor) {
        // Grab material colors from resources
        int arrayId = getResources().getIdentifier("mdcolor_" + typeColor, "array", getApplicationContext().getPackageName());
        if (arrayId != 0) {
            TypedArray colors = getResources().obtainTypedArray(arrayId);
            int[] materialColors = new int[colors.length()];
            for (int i = 0; i < colors.length(); i++) {
                materialColors[i] = colors.getColor(i, MainActivity.mAccentColor);
            }

            colors.recycle();
            return materialColors;
        }
        else return new int[(Color.BLACK)];
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
}

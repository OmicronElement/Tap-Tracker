package com.bwisni.taptracker;

import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.jrummyapps.android.colorpicker.ColorPickerDialog;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class EditDrinkerActivity extends AppCompatActivity{
    @Bind(R.id.editNameTextView) EditText nameTextView;

    private int position;
    private int credits;
    private Drinker drinker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_drinker);

        ButterKnife.bind(this);

        Intent intent = getIntent();

        drinker = (Drinker) intent.getSerializableExtra("drinker");

        position = intent.getIntExtra("drinkerPosition", -1);
        credits = drinker.getCredits();
        String name = drinker.getName();

        buildIcon();

        nameTextView.setText(name);
    }


    @OnClick({R.id.okButton})
    void editDrinkerDone() {
        // Sanity check
        credits = Math.abs(credits);

        // Send data back to Main activity
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);

        intent.putExtra("drinkerPosition", position);
        intent.putExtra("name", nameTextView.getText().toString());
        intent.putExtra("drinkerCredits", credits);
        intent.putExtra("drinkerColor", drinker.getColor());

        hideKeyboard(this.getCurrentFocus());

        setResult(RESULT_OK, intent);
        finish();
    }

    @OnClick(R.id.edit_user_icon)
        void editColor() {
            showDialog();
    }


    private void showDialog() {
        ColorPickerDialog.newBuilder()
                .setColor(drinker.getColor())
                .setPresets(getMatColors("400"))
                .setShowAlphaSlider(false)
                .show(this);
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

    private void hideKeyboard(View view) {
        // Hide keyboard
        InputMethodManager imm = (InputMethodManager)
                getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
}

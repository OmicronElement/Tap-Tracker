package com.bwisni.pub1521;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnLongClick;

public class EditDrinkerActivity extends Activity {
    @Bind(R.id.editTextCredit) EditText editTextCredit;
    @Bind(R.id.editNameTextView) TextView nameTextView;
    @Bind(R.id.nfcIdtextView) TextView nfcIdTextView;


    int position;
    int credits;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_drinker);
        setTitle("Join the Party");

        ButterKnife.bind(this);

        Intent intent = getIntent();

        Drinker drinker = (Drinker) intent.getSerializableExtra("drinker");

        position = intent.getIntExtra("drinkerPosition", -1);
        credits = drinker.credits;
        String name = drinker.name;
        String nfcId = drinker.nfcId;

        nameTextView.setText(name);
        nfcIdTextView.setText(nfcId);

        refreshCreditEditText();
    }

    private void refreshCreditEditText() {
        editTextCredit.setText(Integer.toString(credits));
        editTextCredit.setSelection(editTextCredit.getText().length());
        editTextCredit.clearFocus();
    }

    @OnClick({R.id.okButton})
    void editDrinker(Button b) {
        credits = Integer.parseInt(editTextCredit.getText().toString());

        // Sanity check
        credits = Math.abs(credits);

        // Send data back to Main activity
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);

        intent.putExtra("drinkerPosition", position);
        intent.putExtra("drinkerCredits", credits);
        intent.putExtra("delete", false);

        setResult(RESULT_OK, intent);
        finish();

    }

    @OnLongClick({R.id.editNameTextView})
    boolean delDrinker(View v) {

        Intent intent = new Intent(getApplicationContext(), MainActivity.class);

        intent.putExtra("drinkerPosition", position);
        intent.putExtra("drinkerCredits", credits);
        intent.putExtra("delete", true);

        setResult(RESULT_OK, intent);
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
}

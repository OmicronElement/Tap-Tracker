package com.bwisni.pub1521;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class EditDrinkerActivity extends AppCompatActivity {
    @Bind(R.id.editTextCredit) EditText editTextCredit;
    @Bind(R.id.editTextPw) EditText editTextPw;
    @Bind(R.id.textView) TextView nameTextView;


    int position;
    int credits;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_drinker);
        setTitle("Beer Here!");

        ButterKnife.bind(this);

        Intent intent = getIntent();

        position = intent.getIntExtra("drinkerPosition", -1);
        credits = intent.getIntExtra("drinkerCredits", 0);
        String name = intent.getStringExtra("drinkerName");

        nameTextView.setText(name);

        refreshCreditEditText();

        editTextPw.requestFocus();
    }

    private void refreshCreditEditText() {
        editTextCredit.setText(Integer.toString(credits));
        editTextCredit.setSelection(editTextCredit.getText().length());
    }

    @OnClick({R.id.okButton})
    void editDrinker(Button b) {
        //String name = editTextName.getText().toString();
        String password = editTextPw.getText().toString().trim();
        credits = Integer.parseInt(editTextCredit.getText().toString());

        Log.i("PASS", password);
        if (password.equals("0323")){
            // Send data back to Main activity
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);

            intent.putExtra("drinkerPosition", position);
            intent.putExtra("drinkerCredits", credits);
            intent.putExtra("delete", false);

            setResult(RESULT_OK, intent);
            finish();
        }
    }

    @OnClick({R.id.delButton})
    void delDrinker(Button b) {

        Intent intent = new Intent(getApplicationContext(), MainActivity.class);

        intent.putExtra("drinkerPosition", position);
        intent.putExtra("drinkerCredits", credits);
        intent.putExtra("delete", true);

        setResult(RESULT_OK, intent);
        finish();

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

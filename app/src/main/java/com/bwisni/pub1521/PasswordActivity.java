package com.bwisni.pub1521;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import butterknife.Bind;
import butterknife.ButterKnife;

public class PasswordActivity extends AppCompatActivity {

    @Bind(R.id.passwordEditText) EditText passwordEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password);
        ButterKnife.bind(this);

        // Open up keyboard
        passwordEditText.requestFocus();
        ((InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE)).toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);


        // Listen for Done button on keyboard and check password when clicked
        passwordEditText.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    onPassDoneClick();
                    return true;
                }
                return false;
            }
        });
    }

    public void onPassDoneClick() {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);

        //Close keyboard
        ((InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE))
                .hideSoftInputFromWindow(passwordEditText.getWindowToken(), 0);

        if(passwordEditText.getText().toString().equals("0323")){
            setResult(RESULT_OK, intent);
            finish();
        }
        else{
            setResult(RESULT_CANCELED, intent);
            Log.d("PASS", "Incorrect password: "+passwordEditText.toString());
            finish();
        }

    }
}

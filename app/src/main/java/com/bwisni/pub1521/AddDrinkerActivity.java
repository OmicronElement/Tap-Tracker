package com.bwisni.pub1521;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class AddDrinkerActivity extends AppCompatActivity {
    @Bind(R.id.editTextName) EditText editTextName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_drinker);
        setTitle("Join The Party");

        ButterKnife.bind(this);

    }

    @OnClick({R.id.okButton})
    void okAddDrinker(Button b){
        String name = editTextName.getText().toString();

        if(!name.equals("")) {
            // Send data back to Main activity
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);

            intent.putExtra("name", name);

            setResult(RESULT_OK, intent);
            finish();
        }
    }
}


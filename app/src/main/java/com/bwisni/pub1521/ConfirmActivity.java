package com.bwisni.pub1521;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ConfirmActivity extends AppCompatActivity {
    @Bind(R.id.drinkerConfirmName) TextView nameTextView;
    @Bind(R.id.drinkerConfirmCredits) TextView creditsTextView;

    MediaPlayer mediaPlayer;
    int credits;
    int position;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ButterKnife.bind(this);

        Intent intent = getIntent();

        position = intent.getIntExtra("drinkerPosition", -1);
        credits = intent.getIntExtra("drinkerCredits", 0);
        String name = intent.getStringExtra("drinkerName");

        nameTextView.setText(name);
        creditsTextView.setText(Integer.toString(credits));
    }

    @OnClick({R.id.pourButton}) void pourDrink(){
        if(credits == 0){
            mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.alarm);
            mediaPlayer.start();
        }
        else {
            mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.beer);
            mediaPlayer.start();

            credits--;

        }

        Intent intent = new Intent(getApplicationContext(), MainActivity.class);

        intent.putExtra("drinkerPosition", position);
        intent.putExtra("drinkerCredits", credits);
        setResult(RESULT_OK, intent);
        finish();

    }

    @Override
    protected void onStop() {
        super.onStop();

        mediaPlayer.release();
        mediaPlayer = null;
    }
}

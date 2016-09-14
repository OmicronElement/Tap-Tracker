package com.bwisni.pub1521;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.Button;
import android.widget.TextView;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ConfirmActivity extends AppCompatActivity {
    @Bind(R.id.drinkerConfirmName) TextView nameTextView;
    @Bind(R.id.drinkerConfirmCredits) TextView creditsTextView;
    @Bind(R.id.pourButton) Button pourButton;

    MediaPlayer mediaPlayer;
    int position;
    Drinker drinker;
    String nfcId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ButterKnife.bind(this);

        Intent intent = getIntent();

        drinker = (Drinker) intent.getSerializableExtra("drinker");
        position = intent.getIntExtra("drinkerPosition", 0);

        String name = drinker.name;
        nfcId = drinker.nfcId;

        nameTextView.setText(name);
        creditsTextView.setText(Integer.toString(drinker.credits));

        // Execute after 1 second has passed
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                pourDrink();
            }
        }, 1000);

    }

    @OnClick({R.id.pourButton}) void pourDrink(){
        pourButton.setEnabled(false);

        if(drinker.credits == 0){
            mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.alarm);
            mediaPlayer.start();
        }
        else {
            mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.beer);
            mediaPlayer.start();

            drinker.subtractCredit();
            creditsTextView.setText(Integer.toString(drinker.credits));
        }
        // Execute after 2 seconds have passed
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                finishActivity();
            }
        }, 2000);


    }

    private void finishActivity() {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);

        intent.putExtra("drinkerPosition", position);
        intent.putExtra("drinker", drinker);
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }
}

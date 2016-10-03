package com.bwisni.pub1521;

import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.Toolbar;
import android.widget.ViewSwitcher;

import be.appfoundry.nfclibrary.activities.NfcActivity;
import butterknife.Bind;
import butterknife.ButterKnife;

public class ConfirmActivity extends NfcActivity {
    @Bind(R.id.drinkerConfirmName) TextView nameTextView;
    @Bind(R.id.drinkerConfirmCredits) TextSwitcher creditsTextView;

    MediaPlayer mediaPlayer;
    int position;
    Drinker drinker;
    String nfcId;

    Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setActionBar(toolbar);
        setTitle("Beer Here!");

        ButterKnife.bind(this);

        Intent intent = getIntent();
        final Context context = getApplicationContext();

        // specify the in/out animations you wish to use
        //creditsTextView.setInAnimation(context, android.support.design.R.anim.abc_fade_in);

        creditsTextView.setFactory(new ViewSwitcher.ViewFactory() {

            public View makeView() {
                TextView myText = new TextView(context);
                myText.setGravity(Gravity.CENTER);

                FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT,
                        Gravity.CENTER);
                myText.setLayoutParams(params);

                myText.setTextSize(112);
                myText.setTextColor(getResources().getColor(R.color.colorAccent));
                return myText;
            }
        });

        drinker = (Drinker) intent.getSerializableExtra("drinker");
        position = intent.getIntExtra("drinkerPosition", 0);
        boolean adminMode = intent.getBooleanExtra("adminMode", false);

        String name = drinker.name;
        nfcId = drinker.nfcId;

        nameTextView.setText(name);
        creditsTextView.setText(Integer.toString(drinker.credits));

        if(adminMode){
            // Execute after 1 second has passed
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                   addCredits();
                }
            }, 1000);
        }
        else {
            // Execute after 1 second has passed
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    pourDrink();
                }
            }, 1000);
        }
    }

    private void addCredits(){
        mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.chaching);
        mediaPlayer.start();

        drinker.setCredits(drinker.getCredits()+6);
        creditsTextView.setOutAnimation(getApplicationContext(),android.support.design.R.anim.abc_slide_out_top);
        creditsTextView.setText(Integer.toString(drinker.credits));

        // Execute after 2 seconds have passed
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                finishActivity();
            }
        }, 2000);

    }

    private void pourDrink(){
        creditsTextView.setOutAnimation(getApplicationContext(),android.support.design.R.anim.abc_slide_out_bottom);

        if(drinker.credits == 0){
            mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.alarm);
            mediaPlayer.start();

            // Execute after 2 seconds have passed
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    finishActivityNoCredits();
                }
            }, 2000);
        }
        else {
            mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.beer);
            mediaPlayer.start();

            drinker.subtractCredit();
            creditsTextView.setText(Integer.toString(drinker.credits));

            // Execute after 2 seconds have passed
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    finishActivity();
                }
            }, 2000);
        }


    }

    private void finishActivity() {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);

        intent.putExtra("drinkerPosition", position);
        intent.putExtra("drinker", drinker);
        setResult(RESULT_OK, intent);
        finish();
    }

    private void finishActivityNoCredits() {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);

        setResult(RESULT_CANCELED, intent);
        finish();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    //Called when NFC Tag has been read
    @Override
    protected void onNewIntent(Intent intent) {
        // Do nothing
    }
}

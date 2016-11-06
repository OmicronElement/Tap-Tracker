package com.bwisni.pub1521;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.amulyakhare.textdrawable.TextDrawable;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import lecho.lib.hellocharts.model.PieChartData;
import lecho.lib.hellocharts.model.SliceValue;
import lecho.lib.hellocharts.view.PieChartView;

public class ConfirmActivity extends AppCompatActivity {
    @Bind(R.id.drinkerConfirmName) TextView nameTextView;
    @Bind(R.id.drinkerConfirmCredits) TextSwitcher creditsTextView;
    @Bind(R.id.userPieChart) PieChartView pieChartView;

    MediaPlayer mediaPlayer;
    int position;
    Drinker drinker;
    String nfcId;
    List<DailyStat> dailyStatList;

    Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm);
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
                myText.setTextColor(MainActivity.mAccentColor);
                return myText;
            }
        });

        drinker = (Drinker) intent.getSerializableExtra("drinker");
        position = intent.getIntExtra("drinkerPosition", 0);
        boolean adminMode = intent.getBooleanExtra("adminMode", false);

        String name = drinker.getName();
        nfcId = drinker.getNfcId();

        drawPieChart();

        nameTextView.setText(name);
        creditsTextView.setText(Integer.toString(drinker.getCredits()));

        TextDrawable drawable = TextDrawable.builder()
                .buildRound(drinker.getShortName(), drinker.getColor());

        ImageView image = (ImageView) findViewById(R.id.user_icon);
        image.setImageDrawable(drawable);

        if(adminMode){
            // Execute after .5 seconds
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                   addCredits();
                }
            }, 500);
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

    private void drawPieChart() {
        dailyStatList = new ArrayList<>(DailyStat.find(DailyStat.class,"NFC_ID = ?", nfcId));

        PieChartData pieChartData = new PieChartData(getPieChartData());

        pieChartData.setHasLabels(true);
        pieChartData.setHasLabelsOnlyForSelected(false);
        pieChartData.setHasLabelsOutside(false);
        pieChartData.setValueLabelsTextColor(Color.BLACK);
        pieChartData.setHasCenterCircle(true);
        pieChartData.setCenterCircleScale(0.425f);
        pieChartData.setValueLabelBackgroundEnabled(false);

        pieChartView.setPieChartData(pieChartData);
    }

    private List<SliceValue> getPieChartData() {
        List<SliceValue> values = new ArrayList<>();

        // Add a slice for each entry in dailyStats
        for (DailyStat ds : dailyStatList) {
            SliceValue sv = new SliceValue(ds.getNumPours(), Color.WHITE);
            String date = MainActivity.getDateString(ds.getDate()).substring(0,5);
            sv.setLabel(date);
            values.add(sv);
        }

        return values;
    }

    @SuppressLint("PrivateResource")
    private void addCredits(){
        playMedia(R.raw.chaching);

        drinker.setCredits(drinker.getCredits()+6);
        creditsTextView.setOutAnimation(getApplicationContext(),android.support.design.R.anim.abc_slide_out_top);
        creditsTextView.setText(Integer.toString(drinker.getCredits()));

        // Execute after 1 second
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                finishActivity();
            }
        }, 1000);

    }

    @SuppressLint("PrivateResource")
    private void pourDrink(){
        creditsTextView.setOutAnimation(getApplicationContext(),android.support.design.R.anim.abc_slide_out_bottom);

        if(drinker.getCredits() == 0){
            playMedia(R.raw.alarm);

            // Execute after 2 seconds have passed
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    finishActivityNoCredits();
                }
            }, 2000);
        }
        else {

            playPourSound();

            drinker.subtractCredit();
            creditsTextView.setText(Integer.toString(drinker.getCredits()));

            // Execute after 2 seconds have passed
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    finishActivity();
                }
            }, 2000);
        }


    }

    private void playPourSound() {
        playMedia(MainActivity.getSound());
    }

    private void playMedia(Uri uri) {
        mediaPlayer = MediaPlayer.create(getApplicationContext(), uri);
        mediaPlayer.start();
    }

    private void playMedia(int r) {
        mediaPlayer = MediaPlayer.create(getApplicationContext(), r);
        mediaPlayer.start();
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

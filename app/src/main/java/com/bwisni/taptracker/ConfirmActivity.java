package com.bwisni.taptracker;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.amulyakhare.textdrawable.TextDrawable;
import com.jrummyapps.android.colorpicker.ColorPickerDialog;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import lecho.lib.hellocharts.model.PieChartData;
import lecho.lib.hellocharts.model.SliceValue;
import lecho.lib.hellocharts.view.PieChartView;

public class ConfirmActivity extends AppCompatActivity {
    private final static int FINISH_DELAY_MILLIS = 3000;

    @Bind(R.id.drinkerConfirmName) TextView nameTextView;
    @Bind(R.id.drinkerConfirmCredits) TextSwitcher creditsTextView;
    @Bind(R.id.userPieChart) PieChartView pieChartView;

    MediaPlayer mediaPlayer;
    int position;
    Drinker drinker;
    String nfcId;
    List<DailyStat> dailyStatList;

    Handler handler = new Handler();
    private Uri pourSound;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm);
        ButterKnife.bind(this);

        Intent intent = getIntent();
        final Context context = getApplicationContext();

        initTextSwitcher(context);


        // Assign values
        drinker = (Drinker) intent.getSerializableExtra("drinker");
        position = intent.getIntExtra("drinkerPosition", 0);
        boolean adminMode = intent.getBooleanExtra("adminMode", false);
        pourSound = Uri.parse(intent.getStringExtra("pourSound"));

        String name = drinker.getName();
        nfcId = drinker.getNfcId();

        // Init drawables
        drawPieChart();
        buildIcon();

        nameTextView.setText(name);
        creditsTextView.setCurrentText(Integer.toString(drinker.getCredits()));

        /*if(adminMode){
 // Execute after .5 seconds
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                   addCredits();
                }
            }, 500);
        }*/

        // Execute after 1 second has passed
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                pourDrink();
            }
        }, 1000);


    }

    private void initTextSwitcher(final Context context) {
        // specify the in/out animations you wish to use
        creditsTextView.setInAnimation(context, android.support.design.R.anim.abc_fade_in);
        creditsTextView.setOutAnimation(getApplicationContext(),android.support.design.R.anim.abc_slide_out_top);

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
    }

    private void buildIcon() {
        TextDrawable drawable = TextDrawable.builder()
                .beginConfig()
                    .bold()
                .endConfig()
                .buildRound(drinker.getShortName(), drinker.getColor());

        ImageView image = (ImageView) findViewById(R.id.user_icon);
        image.setImageDrawable(drawable);
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
        pieChartView.setPieChartData(pieChartData);

        if(drinker.getCredits() != 0) {
            List<SliceValue> values = pieChartData.getValues();
            SliceValue sv;
            if(values.size() == 0){
                sv = new SliceValue().setLabel(" ");
                values = new ArrayList<>();
            } else {
                sv = values.get(values.size() - 1);
            }
            // If we don't already have slice for today, add one and increase it
            String today = MainActivity.getDateString().substring(0,5);
            if (!String.valueOf(sv.getLabelAsChars()).equals(today)) {
                values.add(new SliceValue(0, Color.WHITE));
                sv = values.get(values.size() - 1);
                sv.setLabel(today);
            }
            sv.setTarget(sv.getValue() + 1);
        }

        pieChartView.startDataAnimation(2000);
    }

    private List<SliceValue> getPieChartData() {
        List<SliceValue> values = new ArrayList<>();

        // Add a slice for each entry in dailyStats
        for (DailyStat ds : dailyStatList) {
            SliceValue sv = new SliceValue(ds.getNumPours(), Color.WHITE);
            String date = ds.getDate().substring(0,5);
            sv.setLabel(date);

            // Only display stats from this month
            String statMonth = date.substring(0,2);
            String thisMonth = MainActivity.getDateString().substring(0,2);
            if(statMonth.equals(thisMonth)) {
                values.add(sv);
            }
        }

        return values;
    }

    /*
    @SuppressLint("PrivateResource")
    private void addCredits(){
        playMedia(R.raw.chaching);

        drinker.setCredits(drinker.getCredits()+MainActivity.DEFAULT_CREDITS_REFILL);
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
            }, FINISH_DELAY_MILLIS);
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
            }, FINISH_DELAY_MILLIS);
        }


    }
    */

    private void pourDrink(){
        playPourSound();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                finishActivity();
            }
        }, FINISH_DELAY_MILLIS);
    }

    private void showDialog() {
       ColorPickerDialog.newBuilder()
            .setColor(drinker.getColor())
            .setPresets(getMatColors("400"))
            .show(this);
    }

    private void playPourSound() {
        SharedPreferences sharedPreferences;// Settings Shared Preferences
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean enabled = sharedPreferences.getBoolean("switch_sounds_preference", true);

        if(enabled) {
            playMedia(pourSound);
        }
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

    @OnClick(R.id.user_icon)
    protected void onUserIconClick(View view){
        showDialog();
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

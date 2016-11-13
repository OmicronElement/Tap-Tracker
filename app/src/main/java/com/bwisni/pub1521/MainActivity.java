package com.bwisni.pub1521;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.nfc.NfcAdapter;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TabHost;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.ViewFlipper;
import android.widget.ViewSwitcher;

import com.facebook.stetho.Stetho;
import com.orm.SugarContext;

import java.io.File;
import java.io.IOException;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import be.appfoundry.nfclibrary.utilities.sync.NfcReadUtilityImpl;
import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnItemClick;
import butterknife.OnItemLongClick;
import butterknife.OnLongClick;
import lecho.lib.hellocharts.listener.ComboLineColumnChartOnValueSelectListener;
import lecho.lib.hellocharts.listener.PieChartOnValueSelectListener;
import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.AxisValue;
import lecho.lib.hellocharts.model.Column;
import lecho.lib.hellocharts.model.ColumnChartData;
import lecho.lib.hellocharts.model.ComboLineColumnChartData;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PieChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.model.SliceValue;
import lecho.lib.hellocharts.model.SubcolumnValue;
import lecho.lib.hellocharts.model.Viewport;
import lecho.lib.hellocharts.view.ColumnChartView;
import lecho.lib.hellocharts.view.ComboLineColumnChartView;
import lecho.lib.hellocharts.view.PieChartView;

public class MainActivity extends AppCompatActivity {
    protected static final String SMS_NUMBER = "1521";
    protected static final String NDEF_PREFIX = "sms:" + SMS_NUMBER + "?body=";
    private static final String ADMIN_NFC_ID = "664de4d0-0586-40aa-9518-a854d2657982";
    private static final int ADD_REQ_CODE = 0;
    private static final int EDIT_REQ_CODE = 1;
    private static final int CONFIRM_REQ_CODE = 2;
    private static final int ADMIN_REQ_CODE = 3;
    private static final int SOUND_REQ_CODE = 4;
    private static final int PERMISSIONS_REQ_CODE = 5;
    private static final int CAMERA_REQ_CODE = 6;
    //protected static final int ONE_DAY_MS = (1000 * 60 * 60 * 24);
    protected static final int BEERS_IN_KEG = 165;
    protected static final int KEG_LOW_VALUE = 25;
    protected static final int KEG_BG_COLOR = Color.BLACK;
    protected static final int KEG_LOW_COLOR = Color.RED;
    protected static final int GRAPH_VIEWPORT_DAYS = 7;
    protected static final int GRAPH_VIEWPORT_MAX_Y = 50;
    public static final int DEFAULT_CREDITS_REFILL = 6;
    public static final int SLIDESHOW_INTERVAL = 25000;

    protected static int defaultSoundIndex;
    protected static int mAccentColor;
    protected static int mTextColor;
    protected static int kegColor;

    @Bind(R.id.drinkersListView)
    ListView drinkersListView;
    @Bind(R.id.soundsListView)
    ListView soundsListView;
    @Bind(R.id.numServedTextView)
    TextSwitcher numServedTextSwitcher;
    @Bind(R.id.kegTextView)
    TextView kegTextView;
    @Bind(R.id.kegEditText)
    EditText kegEditText;
    @Bind(R.id.AdminLayout)
    RelativeLayout adminLayout;
    @Bind(R.id.coordinatorLayout)
    CoordinatorLayout coordinatorLayout;
    @Bind(R.id.graph)
    ComboLineColumnChartView graph;
    @Bind(R.id.kegGraph)
    ColumnChartView kegGraph;
    @Bind(R.id.pieChart)
    PieChartView pieChart;
    @Bind(R.id.slideshow)
    ViewFlipper slideshow;

    @Bind(R.id.fab)
    FloatingActionButton fab;

    private PendingIntent pendingIntent;
    private IntentFilter[] mIntentFilters;
    private String[][] mTechLists;
    private NfcAdapter mNfcAdapter;

    private ArrayList<Drinker> drinkersArrayList;
    private Map<String, Drinker> drinkers;
    private Map<String, DailyStat> dailyStats;
    private Map<String, Integer> dailyTotals;
    private static ArrayList<Sound> soundsList;
    private PieChartData pieChartData;
    private ColumnChartData kegGraphData;
    private LineChartData lineChartData;
    private ColumnChartData columnChartData;
    private long totalServed;
    private int kegCounter;
    private boolean adminMode = false;

    private String dateToday;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ButterKnife.bind(this);
        SugarContext.init(getApplicationContext());
        Stetho.initializeWithDefaults(this);

        // Keep device awake
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        checkPermissions();

        loadData();

        initNfc();
        initColors();
        initTextSwitcher();
        initGraph();
        initKegGraph();
        initPieChart();
        initAdminTabs();
        initSounds();
        initSlideshow();
    }


    private void initSlideshow() {
        File root = getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        slideshow.removeAllViews();
        slideshow.invalidate();

        if (root != null) {
            for (final File fileEntry : root.listFiles()) {
                addPicToSlideshow(fileEntry.toString());
            }
        }

        Animation fadeIn = new AlphaAnimation(0, 1);
        fadeIn.setInterpolator(new DecelerateInterpolator());
        fadeIn.setDuration(3000);

        Animation fadeOut = new AlphaAnimation(1, 0);
        fadeOut.setInterpolator(new AccelerateInterpolator());
        fadeOut.setStartOffset(3000);
        fadeOut.setDuration(3000);


        slideshow.setInAnimation(fadeIn);
        slideshow.setOutAnimation(fadeOut);
        slideshow.setFlipInterval(SLIDESHOW_INTERVAL);
        slideshow.startFlipping();
    }

    private void addPicToSlideshow(String photoPath) {
        slideshow.setMeasureAllChildren(true);
        // Get the dimensions of the View
        int targetW = 250;
        int targetH = 140;

        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(photoPath, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        // Determine how much to scale down the image
        int scaleFactor = Math.min(photoW/targetW, photoH/targetH);

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;

        Bitmap bitmap = BitmapFactory.decodeFile(photoPath, bmOptions);
        ImageView IV = new ImageView(this);
        IV.setImageBitmap(bitmap);
        slideshow.addView(IV);
    }


    private void initNfc() {
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        mIntentFilters = new IntentFilter[]{new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED)};
        mTechLists = new String[][]{new String[]{Ndef.class.getName()},
                new String[]{NdefFormatable.class.getName()}};
    }


    @SuppressWarnings("deprecation")
    private void initColors() {
        mAccentColor = getResources().getColor(R.color.colorAccent);
        mTextColor = getResources().getColor(android.R.color.primary_text_dark);
        kegColor = getResources().getColor(android.R.color.tertiary_text_dark);
    }

    private void checkPermissions() {
        String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.NFC, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        ActivityCompat.requestPermissions(this, permissions, PERMISSIONS_REQ_CODE);
    }

    private void initSounds() {
        SoundAdapter adapter = new SoundAdapter(this, soundsList);
        soundsListView.setAdapter(adapter);
        soundsListView.setItemChecked(defaultSoundIndex, true);
    }

    public static Uri getSound() {
        // Play fun sounds randomly, otherwise use default sound
        int oneIn = 3;
        Random rand = new Random();
        int value = rand.nextInt(oneIn);

        Log.i("RNG", "Random generated: " + value);

        int size = soundsList.size();
        int nextRand = rand.nextInt(size);
        if (value == oneIn - 1 && size > 1) {
            while (nextRand == defaultSoundIndex) {
                nextRand = rand.nextInt(size);
            }
            return soundsList.get(nextRand).getUri();
        } else {
            return soundsList.get(defaultSoundIndex).getUri();
        }
    }

    private void initAdminTabs() {
        // Thanks Pacific P. Regmi http://www.viralandroid.com/
        TabHost host = (TabHost) findViewById(R.id.tabHost);
        host.setup();

        //Tab 1
        TabHost.TabSpec spec = host.newTabSpec("Settings");
        spec.setContent(R.id.adminTab1);
        spec.setIndicator("Settings");
        host.addTab(spec);

        //Tab 2
        spec = host.newTabSpec("Users");
        spec.setContent(R.id.adminTab2);
        spec.setIndicator("Users");
        host.addTab(spec);
    }

    @SuppressLint("PrivateResource")
    private void initTextSwitcher() {
        numServedTextSwitcher.setInAnimation(getApplicationContext(),
                android.support.design.R.anim.abc_slide_in_top);
        numServedTextSwitcher.setOutAnimation(getApplicationContext(),
                android.support.design.R.anim.abc_slide_out_bottom);

        numServedTextSwitcher.setFactory(new ViewSwitcher.ViewFactory() {

            public View makeView() {
                TextView myText = new TextView(getApplicationContext());
                myText.setGravity(Gravity.CENTER);

                FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.WRAP_CONTENT,
                        FrameLayout.LayoutParams.WRAP_CONTENT,
                        Gravity.CENTER);
                myText.setLayoutParams(params);

                myText.setTextSize(56);
                myText.setTextColor(mTextColor);
                return myText;
            }
        });

        String format = NumberFormat.getNumberInstance(Locale.US).format(totalServed);
        numServedTextSwitcher.setCurrentText(format);
    }

    private void initGraph() {
        final List<DailyStat> allDailyStats = DailyStat.listAll(DailyStat.class);

        columnChartData = new ColumnChartData(getColumnData(allDailyStats));
        columnChartData.setStacked(false);

        lineChartData = new LineChartData(getLineData(allDailyStats));

        ComboLineColumnChartData comboLineColumnChartData = new ComboLineColumnChartData(columnChartData, lineChartData);

        int x = 0;
        List<AxisValue> axisValues = new ArrayList<>();
        for (String date : dailyTotals.keySet()) {
            AxisValue axisValue = new AxisValue(x);
            axisValue.setLabel(date);
            axisValues.add(axisValue);
            x++;
        }

        Axis axisX = new Axis(axisValues);
        Axis axisY = new Axis().setHasLines(true);
        //axisY.setName("Pours");

        comboLineColumnChartData.setAxisXBottom(axisX);
        comboLineColumnChartData.setAxisYLeft(axisY);
        comboLineColumnChartData.setValueLabelTextSize(20);
        comboLineColumnChartData.setValueLabelBackgroundEnabled(true);
        comboLineColumnChartData.setValueLabelBackgroundAuto(false);
        comboLineColumnChartData.setValueLabelBackgroundColor(Color.parseColor("#00ffffff"));

        graph.setComboLineColumnChartData(comboLineColumnChartData);

        graph.setOnValueTouchListener(new ComboLineColumnChartOnValueSelectListener() {
            @Override
            public void onColumnValueSelected(int columnIndex, int subcolumnIndex, SubcolumnValue value) {
                Set<String> dates = dailyTotals.keySet();
                ArrayList<String> datesList = new ArrayList<>(dates);
                updatePieChart(datesList.get(columnIndex));
            }

            @Override
            public void onPointValueSelected(int lineIndex, int pointIndex, PointValue value) {
                onColumnValueSelected(pointIndex, 0, null);
            }

            @Override
            public void onValueDeselected() {

            }
        });
        // Set viewport to last GRAPH_VIEWPORT_DAYS days
        graph.setHorizontalScrollBarEnabled(true);
        Viewport v = new Viewport(graph.getMaximumViewport());
        v.left = v.right - GRAPH_VIEWPORT_DAYS;
        v.top = GRAPH_VIEWPORT_MAX_Y;
        graph.setCurrentViewport(v);
    }


    private List<PointValue> getGraphDataFromDailyStats(List<DailyStat> allDailyStats) {
        List<PointValue> values = new ArrayList<>();

        generateDailyTotals(allDailyStats);

        int i = 0;
        for (Integer total : dailyTotals.values()) {
            values.add(new PointValue(i, total));
            i++;
        }

        return values;
    }

    private void generateDailyTotals(List<DailyStat> allDailyStats) {
        dailyTotals = new LinkedHashMap<>();

        for (DailyStat ds : allDailyStats) {
            int total = ds.getNumPours();
            if (dailyTotals.containsKey(ds.getDate())) {
                total += dailyTotals.get(ds.getDate());
            }
            dailyTotals.put(ds.getDate(), total);
        }
    }


/*    private ColumnChartData generateStackedColumnData(List<DailyStat> dailyStats) {
        ArrayList<Column> columns = new ArrayList<>();

        generateDailyTotals(dailyStats);

        for (String s : dailyTotals.keySet()) {
            ArrayList<Integer> arrayList = dailyTotals.get(s);
            List<SubcolumnValue> subcolumnValues = new ArrayList<>();
            for (Integer integer : arrayList) {
                SubcolumnValue sc = new SubcolumnValue(integer);
                sc.setColor(mAccentColor);
                subcolumnValues.add(sc);
            }

            Column c = new Column(subcolumnValues);
            c.setHasLabels(false);
            //c.setHasLabelsOnlyForSelected(true);
            columns.add(c);
        }
        ColumnChartData columnChartData = new ColumnChartData(columns);
        columnChartData.setStacked(true);
        return columnChartData;
    }*/

    private ArrayList<Column> getColumnData(List<DailyStat> allDailyStats) {
        ArrayList<Column> columns = new ArrayList<>();

        for (PointValue pv : getGraphDataFromDailyStats(allDailyStats)) {
            ArrayList<SubcolumnValue> subcolumnValues = new ArrayList<>();
            SubcolumnValue sc = new SubcolumnValue(pv.getY());
            sc.setColor(mAccentColor);
            subcolumnValues.add(0, sc);

            Column c = new Column(subcolumnValues);
            c.setHasLabels(false);
            //c.setHasLabelsOnlyForSelected(true);
            columns.add(c);
        }

        return columns;
    }

    private List<Line> getLineData(List<DailyStat> allDailyStats) {
        Line line = new Line(getGraphDataFromDailyStats(allDailyStats));
        line.setColor(Color.WHITE);
        line.setCubic(true);
        line.setHasLines(true);
        line.setHasPoints(true);
        line.setHasLabels(true);

        List<Line> lines = new ArrayList<>();
        lines.add(line);

        return lines;
    }

    private void updateGraph() {
        loadDailyStats(getDateString());

        // Check if we need to add a new point/column
        if(!dailyTotals.containsKey(getDateString())){
            initGraph();
        }else { // Otherwise increase existing one
            Line line = lineChartData.getLines().get(0);
            List<PointValue> pointValues = line.getValues();
            PointValue lastPointValue = pointValues.get(pointValues.size() - 1);
            lastPointValue.setTarget(lastPointValue.getX(), lastPointValue.getY() + 1);

            List<Column> columns = columnChartData.getColumns();
            Column column = columns.get(columns.size() - 1);
            SubcolumnValue lastSubcolumnValue = column.getValues().get(0);
            lastSubcolumnValue.setTarget(lastSubcolumnValue.getValue() + 1);
            graph.startDataAnimation(3000);
        }
    }

    private void initKegGraph() {
        kegGraphData = new ColumnChartData(getKegData());
        kegGraphData.setFillRatio(1);
        kegGraphData.setStacked(true);

        kegGraph.setZoomEnabled(false);
        kegGraph.setBackgroundColor(KEG_BG_COLOR);
        kegGraph.setColumnChartData(kegGraphData);
    }

    private void updateKegGraph() {
        List<Column> columns = kegGraphData.getColumns();
        Column column = columns.get(0);
        SubcolumnValue sc = column.getValues().get(0);
        SubcolumnValue bg = column.getValues().get(1);

        sc.setValue(0);
        bg.setValue(BEERS_IN_KEG);

        sc.setTarget(kegCounter);
        bg.setTarget(BEERS_IN_KEG - kegCounter);

        kegGraph.startDataAnimation(2000);
    }

    private ArrayList<Column> getKegData() {
        ArrayList<Column> columns = new ArrayList<>();

        ArrayList<SubcolumnValue> subcolumnValues = new ArrayList<>();
        SubcolumnValue sc = new SubcolumnValue(kegCounter);
        //Add another subcolumn to bring max Y to BEERS_IN_KEG
        SubcolumnValue bg = new SubcolumnValue(BEERS_IN_KEG - kegCounter);

        kegTextView.setText(String.valueOf(kegCounter));

        bg.setColor(KEG_BG_COLOR);

        if (kegCounter > KEG_LOW_VALUE) {
            sc.setColor(kegColor);
        } else {
            sc.setColor(KEG_LOW_COLOR);
        }

        subcolumnValues.add(0, sc);
        subcolumnValues.add(1, bg);

        Column c = new Column(subcolumnValues);
        c.setHasLabels(false);
        columns.add(c);

        return columns;
    }

    private void initPieChart() {
        pieChartData = new PieChartData(getPieChartData());

        pieChartData.setHasLabels(true);
        pieChartData.setHasLabelsOnlyForSelected(false);
        //pieChartData.setHasLabelsOutside(false);
        pieChartData.setHasCenterCircle(true);
        pieChartData.setCenterCircleScale(0.375f);
        pieChartData.setCenterText2("Today");
        pieChartData.setCenterText1FontSize(13);
        pieChartData.setCenterText1Color(Color.WHITE);
        pieChartData.setCenterText1("Pours");
        pieChartData.setCenterText2FontSize(13);
        pieChartData.setCenterText2Color(Color.WHITE);

        // Create a Snackbar showing number of pours when users slice is touched
        pieChart.setValueTouchEnabled(true);
        pieChart.setOnValueTouchListener(new PieChartOnValueSelectListener() {
            @Override
            public void onValueSelected(int arcIndex, SliceValue value) {
                String name = drinkersArrayList.get(arcIndex).getName();
                String s = name + ": " + String.valueOf((int) value.getValue()) + " pours";
                Snackbar snackbar = Snackbar.make(coordinatorLayout, s, Snackbar.LENGTH_SHORT);
                View view = snackbar.getView();
                CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) view.getLayoutParams();
                params.gravity = Gravity.BOTTOM | Gravity.LEFT;
                view.setLayoutParams(params);
                snackbar.show();
            }

            @Override
            public void onValueDeselected() {

            }
        });

        pieChart.setPieChartData(pieChartData);
    }

    private void updatePieChart(String date) {
        loadDailyStats(date);

        List<SliceValue> values = pieChartData.getValues();
        for (Drinker drinker : drinkersArrayList) {
            SliceValue sv = values.get(drinkersArrayList.indexOf(drinker));
            sv.setColor(drinker.getColor());
            // If we have a daily stat today for a given drinker, set target to new value
            if(dailyStats.containsKey(drinker.getNfcId())) {
                DailyStat dailyStat = dailyStats.get(drinker.getNfcId());
                sv.setLabel(drinker.getShortName());
                sv.setTarget(dailyStat.getNumPours());
            } else { // Otherwise hide their slice
                sv.setLabel("");
                sv.setTarget(0);
            }
        }
        pieChart.startDataAnimation(1500);

        if(!date.equals(getDateString()))
            pieChartData.setCenterText2(date);
        else
            pieChartData.setCenterText2("Today");
    }

    private List<SliceValue> getPieChartData() {
        List<SliceValue> values = new ArrayList<>();

        // Add a slice for each entry in dailyStats
        SliceValue sv;
        for (Drinker d : drinkersArrayList) {
            if(dailyStats.containsKey(d.getNfcId())) {
                DailyStat ds = dailyStats.get(d.getNfcId());
                sv = new SliceValue(ds.getNumPours(), d.getColor());
                sv.setLabel(d.getShortName());
            }
            else {
                sv = new SliceValue(0, d.getColor());
                sv.setLabel("");
            }

            values.add(sv);
        }
        return values;
    }

    private void loadData() {
        // Populate with drinkers saved in database
        drinkersArrayList = new ArrayList<>(Drinker.listAll(Drinker.class));
        drinkers = new HashMap<>();
        for (Drinker d : drinkersArrayList) {
            drinkers.put(d.getNfcId(), d);
        }

        loadDailyStats(getDateString());

        // Retrieve sounds list
        soundsList = new ArrayList<>(Sound.listAll(Sound.class));

        printUsers();

        DrinkerAdapter listAdapter = new DrinkerAdapter(this, drinkersArrayList);
        drinkersListView.setAdapter(listAdapter);

        drinkersListView.invalidateViews();


        // Shared Preferences
        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        totalServed = sharedPref.getLong("totalServed", 32800);
        dateToday = sharedPref.getString("dateToday", "00/00/00");
        kegCounter = sharedPref.getInt("kegCounter", BEERS_IN_KEG - 1);
        defaultSoundIndex = sharedPref.getInt("defaultSoundIndex", 0);

        Log.i("kegCounter", Integer.toString(kegCounter));
    }

    private void loadDailyStats(String date) {
        // Load data for daily pie chart
        List<DailyStat> dsList = DailyStat.find(DailyStat.class, "date = ?", date);
        dailyStats = new HashMap<>();
        for (DailyStat ds : dsList) {
            dailyStats.put(ds.getNfcId(), ds);
        }
    }

    private void saveSharedPrefs() {
        // Save totalServed, kegCounter, soundsList to SharedPrefs
        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putLong("totalServed", totalServed);
        editor.putString("dateToday", dateToday);
        editor.putInt("kegCounter", kegCounter);
        editor.putInt("defaultSoundIndex", defaultSoundIndex);
        editor.apply();
    }

    private void printUsers() {
        for (Drinker d : drinkersArrayList)
            Log.d("loadData", d.toString());
    }

    private void increaseTotalServed() {
        totalServed++;

        // Reduce keg counter
        if (kegCounter > 0)
            kegCounter--;

        updateKegGraph();
        updateTotalServed();
    }

    private void updateTotalServed() {
        // Execute after 1 second has passed
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                String formatted = NumberFormat.getNumberInstance(Locale.US).format(totalServed);
                numServedTextSwitcher.setText(formatted);
            }
        }, 500);
    }

    private void increaseDailyStat(Drinker drinker) {
        increaseTotalServed();

        // Start a new chart if it's a new day
        if (!dateToday.equals(getDateString())) {
            dateToday = getDateString();
            dailyStats = new HashMap<>();
        }

        DailyStat ds;
        if (dailyStats.containsKey(drinker.getNfcId())) {
            ds = dailyStats.get(drinker.getNfcId());
            ds.addPour();
        } else {
            ds = new DailyStat(getDateString(), drinker, 1);
            dailyStats.put(drinker.getNfcId(), ds);
        }
        ds.save();

        updateGraph();
        updatePieChart(dateToday);
    }

    private void openConfirmActivity(int position) {
        Intent intent = new Intent(getApplicationContext(),
                ConfirmActivity.class);

        intent.putExtra("drinker", drinkersArrayList.get(position));
        intent.putExtra("drinkerPosition", position);
        intent.putExtra("adminMode", adminMode);

        startActivityForResult(intent, CONFIRM_REQ_CODE);
    }


    private void toggleAdminMode() {
        if (adminMode) {
            adminMode = false;
            slideToBottom(adminLayout);
            hideKeyboard(adminLayout);
            // Update keg counter
            onAdjustKegClick();
            saveSharedPrefs();
        } else {
            adminMode = true;
            kegEditText.setText(String.valueOf(kegCounter));
            kegEditText.setSelection(kegEditText.getText().length());

            slideToTop(adminLayout);
        }
    }

    private void hideKeyboard(View view) {
        // Hide keyboard
        InputMethodManager imm = (InputMethodManager)
                getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    // View animation functions
    // Thanks to pvllnspk on StackOverflow
    public static void slideToBottom(View view) {
        TranslateAnimation animate = new TranslateAnimation(0, 0, 0, view.getHeight());
        animate.setDuration(500);
        view.startAnimation(animate);
        view.setVisibility(View.GONE);
    }

    public static void slideToTop(View view) {
        TranslateAnimation animate = new TranslateAnimation(0, 0, view.getHeight(), 0);
        animate.setDuration(500);
        view.startAnimation(animate);
        view.setVisibility(View.VISIBLE);
    }

    public void addDrinker(String name, int credits, String id) {
        Drinker drinker = new Drinker(name, credits, id);
        drinkersArrayList.add(drinker);
        drinkers.put(id, drinker);
        drinker.save();
        drinkersListView.invalidateViews();
    }

    public void removeDrinker(int position) {
        //Remove from ArrayList and delete() sugar record
        drinkersArrayList.remove(position).delete();
        drinkersListView.invalidateViews();
    }

    // Returns date in ms
    public static long getDate() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.HOUR, 0);
        return calendar.getTimeInMillis();
    }

    protected static String getDateString() {
        return getDateString(getDate());
    }

    protected static String getDateString(long l) {
        Date date = new Date(l);
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yy", Locale.US);
        return sdf.format(date);
    }

    @OnItemLongClick(R.id.drinkersListView)
    boolean onItemLongClick(int position, View view) {
        // Create intent to open up dialogue
        Intent intent = new Intent(getApplicationContext(),
                EditDrinkerActivity.class);

        intent.putExtra("drinker", drinkersArrayList.get(position));
        intent.putExtra("drinkerPosition", position);

        startActivityForResult(intent, EDIT_REQ_CODE);

        return true;
    }

    // Add a drinker
    @OnClick(R.id.fab)
    public void onFabClick(View view) {
        // Create intent to open up dialogue
        Intent intent = new Intent(getApplicationContext(),
                AddDrinkerActivity.class);

        startActivityForResult(intent, ADD_REQ_CODE);
    }

    @OnClick(R.id.adminExitButton)
    public void onAdminExitClick(View view) {
        toggleAdminMode();
    }

    @OnLongClick(R.id.adjustKegButton)
    public boolean onAdjustKegClick() {
        int newValue = Integer.parseInt(kegEditText.getText().toString());

        if(newValue != kegCounter) {
            if (newValue <= BEERS_IN_KEG && newValue >= 0)
                kegCounter = newValue;
            else
                kegCounter = BEERS_IN_KEG;

            Snackbar.make(coordinatorLayout, "Keg Level Updated", Snackbar.LENGTH_SHORT).show();

            updateKegGraph();
        }

        return true;
    }

    @OnLongClick(R.id.newKegButton)
    public boolean onNewKegClick(View view) {
        kegEditText.setText(String.valueOf(BEERS_IN_KEG - 1));

        return true;
    }

    @OnClick(R.id.selSoundButton)
    public void onSelSoundClick(View arg0) {
        final Uri currentTone = RingtoneManager.getActualDefaultRingtoneUri(MainActivity.this,
                RingtoneManager.TYPE_ALARM);
        Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_NOTIFICATION);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "Select Tone");
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, currentTone);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true);
        startActivityForResult(intent, SOUND_REQ_CODE);
    }

    @OnItemClick(R.id.soundsListView)
    public void onSoundClick(int position) {
        Log.d("Sound", soundsList.get(position).toString());
        playMedia(soundsList.get(position).getUri());
        defaultSoundIndex = position;
    }

    @OnItemLongClick(R.id.soundsListView)
    public boolean onSoundLongClick(int position, View view) {
        soundsList.remove(position).delete();
        initSounds();
        return false;
    }

    @OnClick(R.id.cameraButton)
    public void onCameraClick(View view) {
        startCamera();
    }

    private void playMedia(Uri uri) {
        MediaPlayer mediaPlayer = MediaPlayer.create(this, uri);
        mediaPlayer.start();
    }

    @Override
    public void onBackPressed() {
        if (adminMode)
            toggleAdminMode();
        else
            super.onBackPressed();
    }

    // Returning from a dialogue
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        Log.i("onActivityResult", "called");

        // Returning from Add Drinker
        if (requestCode == ADD_REQ_CODE && resultCode == RESULT_OK && intent != null) {
            Log.i("onActivityResult", "RESULT_OK");
            String name = intent.getStringExtra("name");
            String nfcId = intent.getStringExtra("nfcId");

            // Start with DEFAULT_CREDITS_REFILL credits
            addDrinker(name, DEFAULT_CREDITS_REFILL, nfcId);

            initPieChart();
        }
        // Returning from Edit Drinker
        if (requestCode == EDIT_REQ_CODE && resultCode == RESULT_OK && intent != null) {
            Log.i("onActivityResult", "RESULT_OK");
            int credits = intent.getIntExtra("drinkerCredits", 0);
            String name = intent.getStringExtra("name");
            int position = intent.getIntExtra("drinkerPosition", -1);
            int color =  intent.getIntExtra("drinkerColor", Color.BLACK);

            Drinker drinker = drinkersArrayList.get(position);
            drinker.setName(name);
            drinker.setCredits(credits);
            drinker.setColor(color);
            drinker.save();

            // Invalidate views in case name was changed
            drinkersListView.invalidateViews();
            updatePieChart(getDateString());
        }
        // Returning from Edit Drinker - Delete Drinker
        if (requestCode == EDIT_REQ_CODE && resultCode == RESULT_CANCELED && intent != null) {
            int position = intent.getIntExtra("drinkerPosition", -1);
            removeDrinker(position);
            updatePieChart(getDateString());
        }
        // Returning from Confirm with successful credit use/increase
        if (requestCode == CONFIRM_REQ_CODE && resultCode == RESULT_OK && intent != null) {
            Log.i("onActivityResult", "RESULT_OK");
            int position = intent.getIntExtra("drinkerPosition", -1);
            int color = ((Drinker) intent.getSerializableExtra("drinker")).getColor();

            Drinker drinker = drinkersArrayList.get(position);

            drinker.setColor(color);
            // If admin, add credits, else use one
            if (adminMode) {
                drinker.setCredits(drinker.getCredits() + DEFAULT_CREDITS_REFILL);
            } else {
                drinker.subtractCredit();
                updatePieChart(dateToday);
                increaseDailyStat(drinker);
            }

            drinker.save();
        }
        // Returning from valid adminMode password check
        if (requestCode == ADMIN_REQ_CODE && resultCode == RESULT_OK && intent != null) {
            toggleAdminMode();
        }

        if (requestCode == SOUND_REQ_CODE && resultCode == RESULT_OK && intent != null) {
            Sound newSound = new Sound(getApplicationContext(),
                    (Uri) intent.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI));
            soundsList.add(newSound);
            newSound.save();
            soundsListView.invalidateViews();
        }
        if (requestCode == CAMERA_REQ_CODE && resultCode == RESULT_OK) {
            initSlideshow();
            slideshow.setDisplayedChild(slideshow.getChildCount()-1);
            Log.d("CAMERA", "result_ok");
        }

        saveSharedPrefs();
    }

    // Called when NFC Tag has been read
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        Collection<String> ndefMessages = new NfcReadUtilityImpl().readFromTagWithMap(intent).values();

        boolean newUser = true;
        for (String m : ndefMessages) {
            // Check if admin card
            if (m.equals(NDEF_PREFIX + ADMIN_NFC_ID)) {
                toggleAdminMode();
                newUser = false;
            }
            // Otherwise search for matching drinker
            else for (Drinker d : drinkersArrayList) {
                String record = NDEF_PREFIX + d.getNfcId();
                if (m.equals(record)) {
                    openConfirmActivity(drinkersArrayList.indexOf(d));
                    newUser = false;
                }
            }
        }
        // If we don't find a matching nfcId, open new user dialogue
        if (newUser && adminMode) {
            onFabClick(getCurrentFocus());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_exit) {
            finish();
            return true;
        }

        if (id == R.id.action_admin) {
            checkAdminPassword();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void checkAdminPassword() {
        Intent intent = new Intent(getApplicationContext(),
                PasswordActivity.class);

        startActivityForResult(intent, ADMIN_REQ_CODE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        saveSharedPrefs();
        slideshow.stopFlipping();
    }

    public void onResume() {
        super.onResume();
        if (mNfcAdapter != null) {
            mNfcAdapter.enableForegroundDispatch(this, pendingIntent, mIntentFilters, mTechLists);
        }
    }

    public void onPause() {
        super.onPause();
        if (mNfcAdapter != null) {
            mNfcAdapter.disableForegroundDispatch(this);
        }
    }

    // Thanks tibox stackoverflow.com
    protected int getMatColor(String typeColor)
    {
        int returnColor = Color.BLACK;
        int arrayId = getResources().getIdentifier("mdcolor_" + typeColor, "array", getApplicationContext().getPackageName());

        if (arrayId != 0)
        {
            TypedArray colors = getResources().obtainTypedArray(arrayId);
            int index = (int) (Math.random() * colors.length());
            returnColor = colors.getColor(index, Color.BLACK);
            colors.recycle();
        }
        return returnColor;
    }

    String mCurrentPhotoPath;

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = "file:" + image.getAbsolutePath();
        Log.d("PhotoPath", mCurrentPhotoPath);
        return image;
    }

    private void startCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
                Log.e("Camera", "Error occurred while creating the File");
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.bwisni.pub1521.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                // Correct permissions for older APIs. Thanks limlim stackoverflow.com
                List<ResolveInfo> resInfoList = getApplicationContext().getPackageManager()
                        .queryIntentActivities(takePictureIntent, PackageManager.MATCH_DEFAULT_ONLY);
                for (ResolveInfo resolveInfo : resInfoList) {
                    String packageName = resolveInfo.activityInfo.packageName;
                    getApplicationContext().grantUriPermission(packageName, photoURI,
                            Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                }
                startActivityForResult(takePictureIntent, CAMERA_REQ_CODE);
            }
        }
    }
}
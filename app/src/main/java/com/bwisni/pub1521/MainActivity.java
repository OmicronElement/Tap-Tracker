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
import android.support.design.widget.FloatingActionButton;
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
import android.widget.Toast;
import android.widget.ViewFlipper;
import android.widget.ViewSwitcher;

import com.amulyakhare.textdrawable.util.ColorGenerator;
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

import be.appfoundry.nfclibrary.utilities.sync.NfcReadUtilityImpl;
import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnItemClick;
import butterknife.OnItemLongClick;
import butterknife.OnLongClick;
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
    protected static final int KEG_BG_COLOR = Color.parseColor("#000000");
    protected static final int KEG_COLOR = Color.parseColor("#707070");
    protected static final int KEG_LOW_COLOR = Color.parseColor("#ff0000");
    protected static final int GRAPH_VIEWPORT_DAYS = 7;
    protected static final int GRAPH_VIEWPORT_MAX_Y = 50;

    protected static int defaultSoundIndex = 0;
    protected static int mAccentColor;
    protected static int mTextColor;

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
    @Bind(R.id.graph)
    ComboLineColumnChartView graph;
    @Bind(R.id.kegGraph)
    ColumnChartView kegGraph;
    @Bind(R.id.pieChart)
    PieChartView pieChart;
    @Bind(R.id.slideshow)
    ViewFlipper mViewFlipper;

    @Bind(R.id.fab)
    FloatingActionButton fab;

    private ArrayList<Drinker> drinkersArrayList;
    private Map<String, Drinker> drinkers;
    private Map<String, DailyStat> dailyStats;
    private Map<Long, ArrayList<Integer>> dailyTotals;
    private static ArrayList<Sound> soundsList;
    private PieChartData pieChartData;
    private long totalServed;
    private int kegCounter;
    private boolean adminMode = false;

    private long pieChartDate;

    private PendingIntent pendingIntent;
    private IntentFilter[] mIntentFilters;
    private String[][] mTechLists;
    private NfcAdapter mNfcAdapter;


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

        for(Drinker d : drinkersArrayList){
            ColorGenerator generator = ColorGenerator.MATERIAL;
            d.setColor(generator.getColor(d.getNfcId()));
            d.save();
        }

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

        mViewFlipper.setInAnimation(fadeIn);
        mViewFlipper.setOutAnimation(fadeOut);
        mViewFlipper.setAutoStart(true);
        mViewFlipper.setFlipInterval(25000);
        mViewFlipper.startFlipping();
    }

    private void addPicToSlideshow(String photoPath) {
        // Get the dimensions of the View
        mViewFlipper.setMeasureAllChildren(true);
        int targetW = 250;
        int targetH = 140;

        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
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
        mViewFlipper.addView(IV);
        mViewFlipper.startFlipping();
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
        int oneIn = 5;
        Random rand = new Random();
        int value = rand.nextInt(oneIn);

        Log.i("RNG", "Random generated: " + value);

        int size = soundsList.size();
        if (value == oneIn - 1 && size > 0) {
            value = rand.nextInt(size);
            return soundsList.get(value).getUri();
        } else return soundsList.get(defaultSoundIndex).getUri();
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
        List<DailyStat> dailyStats = DailyStat.listAll(DailyStat.class);
        ComboLineColumnChartData data = new ComboLineColumnChartData
                (generateColumnData(dailyStats), generateLineData(dailyStats));

        List<AxisValue> axisValues = new ArrayList<>();

        int x = 0;
        for (Long date : dailyTotals.keySet()) {
            String dateString = getDateString(date);
            AxisValue axisValue = new AxisValue(x);
            axisValue.setLabel(dateString);
            axisValues.add(axisValue);
            x++;
        }

        Axis axisX = new Axis(axisValues);
        Axis axisY = new Axis().setHasLines(true);
        //axisY.setName("Pours");


        data.setAxisXBottom(axisX);
        data.setAxisYLeft(axisY);
        data.setValueLabelTextSize(20);
        data.setValueLabelBackgroundEnabled(true);
        data.setValueLabelBackgroundAuto(false);
        data.setValueLabelBackgroundColor(Color.parseColor("#00ffffff"));

        graph.setComboLineColumnChartData(data);

        // Set viewport to last GRAPH_VIEWPORT_DAYS days
        graph.setHorizontalScrollBarEnabled(true);
        Viewport v = new Viewport(graph.getMaximumViewport());
        v.left = v.right - GRAPH_VIEWPORT_DAYS;
        v.top = GRAPH_VIEWPORT_MAX_Y;
        graph.setCurrentViewport(v);
    }


    private List<PointValue> getGraphDataFromDailyStats(List<DailyStat> dailyStats) {
        List<PointValue> values = new ArrayList<>();

        generateDailyTotals(dailyStats);

        int i = 0;
        for (ArrayList<Integer> arrayList : dailyTotals.values()) {
            int total = 0;
            for (Integer integer : arrayList) {
                total += integer;
            }
            values.add(new PointValue(i, total));
            i++;
        }

        return values;
    }

    private void generateDailyTotals(List<DailyStat> dailyStats) {
        dailyTotals = new LinkedHashMap<>();

        for (DailyStat ds : dailyStats) {
            ArrayList<Integer> totalPoured = new ArrayList<>();
            if (dailyTotals.containsKey(ds.getDate())) {
                totalPoured = dailyTotals.get(ds.getDate());
                totalPoured.add(ds.getNumPours());
                dailyTotals.put(ds.getDate(), totalPoured);
            } else {
                totalPoured.add(ds.getNumPours());
                dailyTotals.put(ds.getDate(), totalPoured);
            }
        }
    }


    private ColumnChartData generateStackedColumnData(List<DailyStat> dailyStats) {
        ArrayList<Column> columns = new ArrayList<>();

        generateDailyTotals(dailyStats);

        for (Long l : dailyTotals.keySet()) {
            ArrayList<Integer> arrayList = dailyTotals.get(l);
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
    }

    private ColumnChartData generateColumnData(List<DailyStat> dailyStats) {
        ArrayList<Column> columns = new ArrayList<>();

        for (PointValue pv : getGraphDataFromDailyStats(dailyStats)) {
            ArrayList<SubcolumnValue> subcolumnValues = new ArrayList<>();
            SubcolumnValue sc = new SubcolumnValue(pv.getY());
            sc.setColor(mAccentColor);
            subcolumnValues.add(0, sc);

            Column c = new Column(subcolumnValues);
            c.setHasLabels(false);
            //c.setHasLabelsOnlyForSelected(true);
            columns.add(c);
        }
        ColumnChartData columnChartData = new ColumnChartData(columns);
        columnChartData.setStacked(false);
        return columnChartData;
    }

    private LineChartData generateLineData(List<DailyStat> dailyStats) {

        Line line = new Line(getGraphDataFromDailyStats(dailyStats));
        line.setColor(Color.WHITE);
        line.setCubic(true);
        line.setHasLines(true);
        line.setHasPoints(true);
        line.setHasLabels(true);

        List<Line> lines = new ArrayList<>();
        lines.add(line);

        return new LineChartData(lines);
    }

    private void updateGraph() {
        initGraph();
    }

    private void initKegGraph() {
        kegGraph.setZoomEnabled(false);
        kegGraph.setBackgroundColor(KEG_BG_COLOR);

        kegGraph.setColumnChartData(generateKegData());
    }

    private void updateKegGraph() {
        initKegGraph();
    }

    private ColumnChartData generateKegData() {
        ArrayList<Column> columns = new ArrayList<>();
        ColumnChartData columnChartData = new ColumnChartData();

        ArrayList<SubcolumnValue> subcolumnValues = new ArrayList<>();
        SubcolumnValue sc = new SubcolumnValue(kegCounter);
        //Add another subcolumn to bring max Y to BEERS_IN_KEG
        SubcolumnValue bg = new SubcolumnValue(BEERS_IN_KEG - kegCounter);

        kegTextView.setText(String.valueOf(kegCounter));

        bg.setColor(KEG_BG_COLOR);

        if (kegCounter > KEG_LOW_VALUE) {
            sc.setColor(KEG_COLOR);
        } else {
            sc.setColor(KEG_LOW_COLOR);
        }

        subcolumnValues.add(sc);
        subcolumnValues.add(bg);

        Column c = new Column(subcolumnValues);
        c.setHasLabels(false);
        columns.add(c);

        columnChartData.setColumns(columns);
        columnChartData.setFillRatio(1);
        columnChartData.setStacked(true);

        return columnChartData;
    }

    private void initPieChart() {
        pieChartData = new PieChartData(getPieChartData());

        pieChartData.setHasLabels(true);
        pieChartData.setHasLabelsOnlyForSelected(false);
        //pieChartData.setHasLabelsOutside(false);
        pieChartData.setHasCenterCircle(true);
        pieChartData.setCenterCircleScale(0.375f);
        pieChartData.setCenterText1("Today's");
        pieChartData.setCenterText1FontSize(13);
        pieChartData.setCenterText1Color(Color.WHITE);
        pieChartData.setCenterText2("Pours");
        pieChartData.setCenterText2FontSize(13);
        pieChartData.setCenterText2Color(Color.WHITE);

        // Create a Toast showing number of pours when users slice is touched
        pieChart.setValueTouchEnabled(true);
        pieChart.setOnValueTouchListener(new PieChartOnValueSelectListener() {
            @Override
            public void onValueSelected(int arcIndex, SliceValue value) {
                String name = String.valueOf(value.getLabelAsChars());
                String s = name + ": " + String.valueOf((int) value.getValue()) + " pours";
                Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onValueDeselected() {

            }
        });

        pieChart.setPieChartData(pieChartData);
    }

    private void updatePieChart() {
        pieChartData.setValues(getPieChartData());
        pieChart.startDataAnimation();
    }

    private List<SliceValue> getPieChartData() {
        List<SliceValue> values = new ArrayList<>();

        // Add a slice for each entry in dailyStats
        for (DailyStat ds : dailyStats.values()) {
            SliceValue sv = new SliceValue(ds.getNumPours(), KEG_COLOR);

            Drinker drinker = drinkers.get(ds.getNfcId());
            sv.setLabel(drinker.getShortName());
            sv.setColor(drinker.getColor());
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

        // Load data for daily pie chart
        List<DailyStat> dsList = DailyStat.find(DailyStat.class, "date = ?", Long.toString(getDate()));
        dailyStats = new HashMap<>();
        for (DailyStat ds : dsList) {
            dailyStats.put(ds.getNfcId(), ds);
        }

        // Retrieve sounds list
        soundsList = new ArrayList<>(Sound.listAll(Sound.class));

        printUsers();

        DrinkerAdapter listAdapter = new DrinkerAdapter(this, drinkersArrayList);
        drinkersListView.setAdapter(listAdapter);

        drinkersListView.invalidateViews();


        // Shared Preferences
        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        totalServed = sharedPref.getLong("totalServed", 32800);
        pieChartDate = sharedPref.getLong("pieChartDate", getDate());
        kegCounter = sharedPref.getInt("kegCounter", BEERS_IN_KEG - 1);
        defaultSoundIndex = sharedPref.getInt("defaultSoundIndex", defaultSoundIndex);

        Log.i("kegCounter", Integer.toString(kegCounter));
    }

    private void saveSharedPrefs() {
        // Save totalServed, kegCounter, soundsList to SharedPrefs
        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putLong("totalServed", totalServed);
        editor.putLong("pieChartDate", pieChartDate);
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

/*        // Find DatePoint for today and increment
        List<DatePoint> dpList = DatePoint.find(DatePoint.class, "date = ?", Long.toString(getDate()));
        DatePoint dp = dpList.get(0);
        dp.addDrink();
        dp.save();*/

        updateGraph();
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
        // Start a new chart if it's a new day
        if (pieChartDate < getDate()) {
            pieChartDate = getDate();
            dailyStats = new HashMap<>();
        }

        DailyStat ds;
        if (dailyStats.containsKey(drinker.getNfcId())) {
            ds = dailyStats.get(drinker.getNfcId());
            ds.addDrink();
        } else {
            ds = new DailyStat(getDate(), drinker, 1);
            dailyStats.put(drinker.getNfcId(), ds);
        }
        ds.save();

        updateGraph();
        updatePieChart();
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
    public long getDate() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.HOUR, 0);
        return calendar.getTimeInMillis();
    }

    protected static String getDateString(long x) {
        Date date = new Date(x);
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
        /*intent.putExtra("drinkerName", drinkersArrayList.get(position).name);
        intent.putExtra("drinkerCredits", drinkersArrayList.get(position).credits);*/

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

        if (newValue <= BEERS_IN_KEG && newValue >= 0)
            kegCounter = newValue;
        else
            kegCounter = BEERS_IN_KEG;

        updateKegGraph();
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

            // Start with 6 credits
            addDrinker(name, 6, nfcId);
        }
        // Returning from Edit Drinker
        if (requestCode == EDIT_REQ_CODE && resultCode == RESULT_OK && intent != null) {
            Log.i("onActivityResult", "RESULT_OK");
            int credits = intent.getIntExtra("drinkerCredits", 0);
            String name = intent.getStringExtra("name");
            int position = intent.getIntExtra("drinkerPosition", -1);

            Drinker drinker = drinkersArrayList.get(position);
            drinker.setName(name);
            drinker.setCredits(credits);
            drinker.save();

            // Invalidate views in case name was changed
            drinkersListView.invalidateViews();
        }
        // Returning from Edit Drinker - Delete Drinker
        if (requestCode == EDIT_REQ_CODE && resultCode == RESULT_CANCELED && intent != null) {
            int position = intent.getIntExtra("drinkerPosition", -1);
            removeDrinker(position);
        }
        // Returning from Confirm with successful credit use/increase
        if (requestCode == CONFIRM_REQ_CODE && resultCode == RESULT_OK && intent != null) {
            Log.i("onActivityResult", "RESULT_OK");
            int position = intent.getIntExtra("drinkerPosition", -1);

            Drinker drinker = drinkersArrayList.get(position);

            // If admin, add credits, else use one
            if (adminMode) {
                drinker.setCredits(drinker.getCredits() + 6);
            } else {
                drinker.subtractCredit();
                increaseTotalServed();
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
        if (requestCode == CAMERA_REQ_CODE && resultCode == RESULT_OK && intent != null) {
            Bundle extras = intent.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            ImageView imageView = new ImageView(this);
            imageView.setImageBitmap(imageBitmap);
            mViewFlipper.addView(imageView);
            mViewFlipper.startFlipping();
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
        mViewFlipper.stopFlipping();
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


    String mCurrentPhotoPath;

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
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
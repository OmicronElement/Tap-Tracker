package com.bwisni.taptracker;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.nfc.NfcAdapter;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.facebook.stetho.Stetho;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.orm.SugarContext;

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
import java.util.Set;

import be.appfoundry.nfclibrary.utilities.sync.NfcReadUtilityImpl;
import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
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

import static android.support.v7.appcompat.R.id.wrap_content;

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
    private static final int SETTINGS_REQ_CODE = 7;

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
    public static final int DEFAULT_KEG_SIZE = 165;
    protected static int beersInKeg = DEFAULT_KEG_SIZE;

    @Bind(R.id.coordinatorLayout)
    CoordinatorLayout mainLayout;
    @Bind(R.id.bannerTextView)
    TextView bannerTextView;
    @Bind(R.id.kegTextView)
    TextView kegTextView;
    @Bind(R.id.graph)
    ComboLineColumnChartView graph;
    @Bind(R.id.kegGraph)
    ColumnChartView kegGraph;
    @Bind(R.id.pieChart)
    PieChartView pieChart;
    @Bind(R.id.AdminLayout)
    RelativeLayout adminLayout;
    @Bind(R.id.drinkersListView)
    ListView drinkersListView;

    @Bind(R.id.fab)
    FloatingActionButton fab;

    private PendingIntent pendingIntent;
    private IntentFilter[] mIntentFilters;
    private String[][] mTechLists;
    private NfcAdapter mNfcAdapter;

    private ArrayList<Drinker> drinkersArrayList;
    private Map<String, DailyStat> dailyStats;
    private Map<String, Integer> dailyTotals;
    private PieChartData pieChartData;
    private ColumnChartData kegGraphData;
    private LineChartData lineChartData;
    private ColumnChartData columnChartData;
    private long totalServed;
    private boolean adminMode = false;

    private String dateToday;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initAds();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ButterKnife.bind(this);
        registerForContextMenu(drinkersListView);

        SugarContext.init(getApplicationContext());
        Stetho.initializeWithDefaults(this);

        WizardPagerAdapter adapter = new WizardPagerAdapter();
        ViewPager pager = (ViewPager) findViewById(R.id.pager);
        pager.setAdapter(adapter);

        checkPermissions();

        loadData();

        initNfc();
        initColors();
        initBanner();
        initGraph();
        initKegGraph();
        initPieChart();

        checkKeepScreenAwake();
        checkFirstLaunch();
    }

    private void checkKeepScreenAwake() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean keepScreenAwake = sharedPreferences.getBoolean("switch_screen_alive_preference", false);

        if(keepScreenAwake)
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        else
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    private void initAds() {
        MobileAds.initialize(getApplicationContext(), "ca-app-pub-4753048576668871~3512825148");
        AdView mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
    }

    private void checkFirstLaunch() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean firstLaunch = sharedPreferences.getBoolean("first_launch", true);

        if(firstLaunch){
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(R.string.dialog_first_launch)
                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // Dismiss
                        }
                    }).show();

            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("first_launch", false);
            editor.apply();
        }
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
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mAccentColor = sharedPreferences.getInt("graph_color",
                getResources().getColor(R.color.colorAccent));

        mTextColor = getResources().getColor(android.R.color.primary_text_dark);
        kegColor = getResources().getColor(android.R.color.tertiary_text_dark);
    }

    private void checkPermissions() {
        String[] permissions = {Manifest.permission.NFC, Manifest.permission.INTERNET};
        ActivityCompat.requestPermissions(this, permissions, PERMISSIONS_REQ_CODE);
    }

    public Uri getSound() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String uriString = sharedPreferences.getString("sound_preference","android.resource://com.bwisni.taptracker/" + R.raw.pour);
        return Uri.parse(uriString);
    }

    private void initBanner() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String banner = sharedPreferences.getString("banner_text",
                    getResources().getString(R.string.banner_text));
        bannerTextView.setText(banner);
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

        resetGraphViewport();
    }

    private void resetGraphViewport() {
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
            lastPointValue.set(lastPointValue.getX(), lastPointValue.getY() + 1);
            lastPointValue.setLabel(String.valueOf((int) lastPointValue.getY()));

            List<Column> columns = columnChartData.getColumns();
            Column column = columns.get(columns.size() - 1);
            SubcolumnValue lastSubcolumnValue = column.getValues().get(0);
            lastSubcolumnValue.setValue(lastSubcolumnValue.getValue() + 1);

            //graph.startDataAnimation(3000); // animations not jiving with viewport reset
        }

        resetGraphViewport();
    }

    private void initKegGraph() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        beersInKeg = Integer.parseInt(sharedPreferences.getString("keg_size",
                                        Integer.toString(DEFAULT_KEG_SIZE)));

        kegGraphData = new ColumnChartData(getKegData());
        kegGraphData.setFillRatio(1);
        kegGraphData.setStacked(true);

        kegGraph.setZoomEnabled(false);
        kegGraph.setBackgroundColor(KEG_BG_COLOR);
        kegGraph.setColumnChartData(kegGraphData);
    }

    private void updateKegGraph() {
        int kegCounter = getKegCounterPref();

        List<Column> columns = kegGraphData.getColumns();
        Column column = columns.get(0);
        SubcolumnValue sc = column.getValues().get(0);
        SubcolumnValue bg = column.getValues().get(1);

        sc.setValue(0);
        bg.setValue(beersInKeg);

        sc.setTarget(kegCounter);
        bg.setTarget(beersInKeg - kegCounter);

        kegTextView.setText(String.valueOf(kegCounter));

        if (kegCounter > KEG_LOW_VALUE) {
            sc.setColor(kegColor);
        } else {
            sc.setColor(KEG_LOW_COLOR);
        }

        kegGraph.startDataAnimation(2000);
    }

    private ArrayList<Column> getKegData() {
        int kegCounter = getKegCounterPref();
        ArrayList<Column> columns = new ArrayList<>();

        ArrayList<SubcolumnValue> subcolumnValues = new ArrayList<>();
        SubcolumnValue sc = new SubcolumnValue(kegCounter);
        //Add another subcolumn to bring max Y to beersInKeg
        SubcolumnValue bg = new SubcolumnValue(beersInKeg - kegCounter);

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
                Snackbar snackbar = Snackbar.make(mainLayout, s, Snackbar.LENGTH_SHORT);
                View view = snackbar.getView();
                CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) view.getLayoutParams();
                params.gravity = Gravity.BOTTOM | Gravity.LEFT;
                params.leftMargin = params.bottomMargin = (int) getResources().getDimension(R.dimen.activity_vertical_margin);
                params.width = CoordinatorLayout.LayoutParams.WRAP_CONTENT;
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

        loadDailyStats(getDateString());

        printUsers();

        DrinkerAdapter listAdapter = new DrinkerAdapter(this, drinkersArrayList);
        drinkersListView.setAdapter(listAdapter);

        drinkersListView.invalidateViews();

        loadSharedPrefs();
    }

    private void loadDailyStats(String date) {
        // Load data for daily pie chart
        List<DailyStat> dsList = DailyStat.find(DailyStat.class, "date = ?", date);
        dailyStats = new HashMap<>();
        for (DailyStat ds : dsList) {
            dailyStats.put(ds.getNfcId(), ds);
        }
    }

    private void loadSharedPrefs() {
        // MainActivity Shared Preferences
        SharedPreferences sharedPreferences = this.getPreferences(Context.MODE_PRIVATE);
        totalServed = sharedPreferences.getLong("totalServed", 32800);
        dateToday = sharedPreferences.getString("dateToday", "00/00/00");
        defaultSoundIndex = sharedPreferences.getInt("defaultSoundIndex", 0);
    }


    private int getKegCounterPref() {
        SharedPreferences sharedPreferences;// Settings Shared Preferences
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        int kegCounter = Integer.parseInt(sharedPreferences.getString("keg_counter",
                                        Integer.toString(beersInKeg)));
        // Sanity check
        if(kegCounter > beersInKeg) {
            kegCounter = beersInKeg;
            saveKegCounterPref(beersInKeg);
        }

        return kegCounter;
    }

    private void saveSharedPrefs() {
        // Save totalServed, kegCounter, soundsList to SharedPrefs
        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putLong("totalServed", totalServed);
        editor.putString("dateToday", dateToday);
        editor.putInt("defaultSoundIndex", defaultSoundIndex);
        editor.apply();
    }

    private void saveKegCounterPref(int i) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("keg_counter", Integer.toString(i));
        editor.apply();
    }

    private void printUsers() {
        for (Drinker d : drinkersArrayList)
            Log.d("loadData", d.toString());
    }

    private void increaseTotalServed() {
        int kegCounter = getKegCounterPref();
        totalServed++;

        // Reduce keg counter
        if (kegCounter > 0)
            kegCounter--;

        saveKegCounterPref(kegCounter);

        updateKegGraph();
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
        intent.putExtra("pourSound", getSound().toString());

        startActivityForResult(intent, CONFIRM_REQ_CODE);
    }


    private void toggleAdminMode() {
        if (adminMode) {
            adminMode = false;
            slideToBottom(adminLayout);
            hideKeyboard(adminLayout);
            // Update keg counter
            updateKegGraph();
            saveSharedPrefs();
        } else {
            adminMode = true;
            adminLayout.bringToFront();
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

    void editDrinker(int position) {
        // Create intent to open up dialogue
        Intent intent = new Intent(getApplicationContext(),
                EditDrinkerActivity.class);

        intent.putExtra("drinker", drinkersArrayList.get(position));
        intent.putExtra("drinkerPosition", position);

        startActivityForResult(intent, EDIT_REQ_CODE);
    }

    void writeDrinkerCard(int position) {
        // Create intent to open up dialogue
        Intent intent = new Intent(getApplicationContext(),
                WriteCardActivity.class);

        intent.putExtra("drinker", drinkersArrayList.get(position));

        startActivity(intent);
    }

    void writeAdminCard() {
        // Create intent to open up dialogue
        Intent intent = new Intent(getApplicationContext(),
                WriteCardActivity.class);

        intent.putExtra("drinker", new Drinker("Admin", 0, ADMIN_NFC_ID));

        startActivity(intent);
    }


    // Add a drinker
    @OnClick(R.id.fab)
    public void onFabClick() {
        // Create intent to open up dialogue
        Intent intent = new Intent(getApplicationContext(),
                AddDrinkerActivity.class);

        startActivityForResult(intent, ADD_REQ_CODE);
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
            // If admin, add credits, else use one // disabled for now
            if (false) {
                drinker.setCredits(drinker.getCredits() + DEFAULT_CREDITS_REFILL);
            } else {
                drinker.subtractCredit();
                updatePieChart(dateToday);
                increaseDailyStat(drinker);
            }

            drinker.save();


            resetGraphViewport();
        }
        // Returning from valid adminMode password check
        if (requestCode == ADMIN_REQ_CODE && resultCode == RESULT_OK && intent != null) {
            toggleAdminMode();
        }

        /*if (requestCode == SOUND_REQ_CODE && resultCode == RESULT_OK && intent != null) {
            Sound newSound = new Sound(getApplicationContext(),
                    (Uri) intent.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI));
            soundsList.add(newSound);
            newSound.save();
            soundsListView.invalidateViews();
        }*/
        if (requestCode == SETTINGS_REQ_CODE){
            // Re-initialize in case something was changed
            initColors();
            initGraph();
            initBanner();
            initKegGraph();
            checkKeepScreenAwake();
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
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.context_menu, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        switch (item.getItemId()) {
            case R.id.drinker_edit:
                editDrinker(info.position);
                return true;
            case R.id.drinker_newcard:
                writeDrinkerCard(info.position);
                return true;
            case R.id.drinker_delete:
                removeDrinker(info.position);
                return true;
            default:
                return super.onContextItemSelected(item);
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
            toggleAdminMode();
            return true;
        }

        if (id == R.id.action_settings) {
            Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
            startActivityForResult(intent, SETTINGS_REQ_CODE);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStop() {
        super.onStop();
        saveSharedPrefs();
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
}
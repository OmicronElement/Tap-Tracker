package com.bwisni.pub1521;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.TranslateAnimation;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TabHost;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;
import android.widget.ViewSwitcher;

import com.orm.SugarContext;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.SortedMap;
import java.util.TreeMap;

import be.appfoundry.nfclibrary.activities.NfcActivity;
import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnItemClick;
import butterknife.OnItemLongClick;
import butterknife.OnLongClick;
import butterknife.OnTouch;
import lecho.lib.hellocharts.formatter.SimplePieChartValueFormatter;
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


public class MainActivity extends NfcActivity {
    public static final String NDEF_PREFIX = "sms:"+AddDrinkerActivity.SMS_NUMBER+"?body=";
    private static final String ADMIN_NFC_ID = "664de4d0-0586-40aa-9518-a854d2657982";
    private static final int ADD_REQ_CODE = 0;
    private static final int EDIT_REQ_CODE = 1;
    private static final int CONFIRM_REQ_CODE = 2;
    private static final int ADMIN_REQ_CODE = 3;
    public static final int SOUND_REQ_CODE = 4;
    public static final int RESULT_DELETE = -1;
    public static final int ONE_DAY_MS = (1000 * 60 * 60 * 24);
    public static final int BEERS_IN_KEG = 165;
    public static final int KEG_LOW_VALUE = 25;
    private static final int KEG_BG_COLOR = Color.parseColor("#000000");
    private static final int KEG_COLOR = Color.parseColor("#707070");
    private static final int KEG_LOW_COLOR = Color.parseColor("#ff0000");



    private static int mAccentColor;
    private static int mTextColor;

    @Bind(R.id.drinkersListView) ListView drinkersListView;
    @Bind(R.id.soundsListView) ListView soundsListView;
    @Bind(R.id.numServedTextView) TextSwitcher numServedTextSwitcher;
    @Bind(R.id.kegTextView) TextView kegTextView;
    @Bind(R.id.kegEditText) EditText kegEditText;
    @Bind(R.id.AdminLayout) RelativeLayout adminLayout;
    @Bind(R.id.graph) ComboLineColumnChartView graph;
    @Bind(R.id.kegGraph) ColumnChartView kegGraph;
    @Bind(R.id.pieChart) PieChartView pieChart;

    @Bind(R.id.fab) FloatingActionButton fab;

    private ArrayList<Drinker> drinkersArrayList = new ArrayList<>();
    private PieChartData pieChartData;
    private long totalServed;
    private int kegCounter;
    private boolean adminMode = false;
    private long pieChartDate;
    private Map<String, DailyStat> dailyStats = new HashMap<>();


    private static List<Uri> soundsList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setActionBar(toolbar);
        // Keep device awake
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        ButterKnife.bind(this);
        SugarContext.init(getApplicationContext());

        loadData();

        mAccentColor = getResources().getColor(R.color.colorAccent);
        mTextColor = getResources().getColor(android.R.color.primary_text_dark);

        initTextSwitcher();
        initGraph();
        initKegGraph();
        initPieChart();
        initAdminTabs();
        initSounds();
    }

    private void initSounds() {
        ArrayAdapter<Uri> adapter = new ArrayAdapter<>(getApplicationContext(),
                android.R.layout.simple_list_item_1, soundsList);
        soundsListView.setAdapter(adapter);
    }

    public static Uri getSound(){
        // Play fun sounds randomly, otherwise use default sound
        int oneIn = 5;
        Random rand = new Random();
        int value = rand.nextInt(oneIn);

        Log.i("RNG", "Random generated: " + value);

        int size = soundsList.size();
        if(value == oneIn - 1 && size > 0){
            value = rand.nextInt(size);
            return soundsList.get(value);
        }
        else return Uri.parse("android.resource://com.bwisni.pub1521/" + R.raw.beer);
    }

    private void initAdminTabs() {
        // Thanks Pacific P. Regmi http://www.viralandroid.com/
        TabHost host = (TabHost)findViewById(R.id.tabHost);
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
        List<DatePoint> dpList = DatePoint.listAll(DatePoint.class);
        ComboLineColumnChartData data = new ComboLineColumnChartData
                (generateColumnData(dpList), generateLineData(dpList));

        List<AxisValue> axisValues = new ArrayList<>();
        int x = 0;
        for (DatePoint dp : dpList) {
            String dateString = getDateString(dp.date);
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

        // Set viewport to last 7 days
        graph.setHorizontalScrollBarEnabled(true);
        Viewport v = new Viewport(graph.getMaximumViewport());
        v.left = v.right - 7;
        v.top = 50;
        graph.setCurrentViewport(v);
    }

    private List<PointValue> getGraphData(List<DatePoint> dpList) {
        List<PointValue> values = new ArrayList<>();
        int i = 0;
        for (DatePoint dp : dpList) {
            values.add(new PointValue(i, dp.pours));
            i++;
        }

        return values;
    }

    private ColumnChartData generateColumnData(List<DatePoint> dpList) {
        ArrayList<Column> columns = new ArrayList<>();
        for(PointValue pv : getGraphData(dpList)){
            ArrayList<SubcolumnValue> subcolumnValues = new ArrayList<>();
            SubcolumnValue sc =  new SubcolumnValue(pv.getY());
            sc.setColor(mAccentColor);
            subcolumnValues.add(0, sc);

            Column c = new Column(subcolumnValues);
            c.setHasLabels(true);
            //c.setHasLabelsOnlyForSelected(true);
            columns.add(c);
        }

        return new ColumnChartData(columns);
    }

    private LineChartData generateLineData(List<DatePoint> dpList) {
        Line line = new Line(getGraphData(dpList));
        line.setColor(Color.WHITE);
        line.setCubic(true);
        line.setHasLabels(false);
        line.setHasLines(true);
        line.setHasPoints(true);

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
        SubcolumnValue sc =  new SubcolumnValue(kegCounter);
        //Add another subcolumn to bring max Y to BEERS_IN_KEG
        SubcolumnValue bg =  new SubcolumnValue(BEERS_IN_KEG - kegCounter);


        columnChartData.setValueLabelBackgroundAuto(false);

/*
        // Set graph color and label position based on kegCounter value
        if(kegCounter < BEERS_IN_KEG/2){
            bg.setLabel(String.valueOf(kegCounter));
            sc.setLabel("");
            columnChartData.setValueLabelBackgroundColor(KEG_BG_COLOR);
        }
        else{
            sc.setLabel(String.valueOf(kegCounter));
            bg.setLabel("");
            columnChartData.setValueLabelBackgroundColor(KEG_COLOR);
        }
*/

        kegTextView.setText(String.valueOf(kegCounter));

        bg.setColor(KEG_BG_COLOR);

        if(kegCounter > KEG_LOW_VALUE) {
            sc.setColor(KEG_COLOR);
        }
        else {
            sc.setColor(KEG_LOW_COLOR);
        }

        subcolumnValues.add(0, sc);
        subcolumnValues.add(1, bg);

        Column c = new Column(subcolumnValues);
        c.setHasLabels(false);
        columns.add(c);

        columnChartData.setColumns(columns);
        columnChartData.setFillRatio(1);
        columnChartData.setStacked(true);

        return columnChartData;
    }

    private void initPieChart(){
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
                String s = String.valueOf(value.getLabelAsChars()) +": "
                        + String.valueOf((int) value.getValue()) +" pours";
                Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onValueDeselected() {

            }
        });

        pieChart.setPieChartData(pieChartData);
    }

    private void updatePieChart(){
        pieChartData.setValues(getPieChartData());
        pieChart.startDataAnimation();
    }

    private List<SliceValue> getPieChartData() {
        List<SliceValue> values = new ArrayList<>();

        // Add a slice for each entry in dailyStats
        int i = 0;
        for(String key : dailyStats.keySet()){
            DailyStat ds = dailyStats.get(key);
            SliceValue sv = new SliceValue(ds.getNumPours(), KEG_COLOR);
            sv.setLabel(ds.getName());
            values.add(sv);
            i++;
        }

        return values;
    }

    private void loadData() {
        // Populate list with drinkers saved in database
        drinkersArrayList = new ArrayList<>(Drinker.listAll(Drinker.class));

        // Search for existing DatePoint
        List<DatePoint> dpList = DatePoint.find(DatePoint.class, "date = ?", Long.toString(getDate()));
        DatePoint dp;
        // If no DatePoint for today, add one
        try {
            dp = dpList.get(0);
        }
        catch (IndexOutOfBoundsException e){
            dp = new DatePoint(getDate(), 0);
        }
        dp.save();

        printUsers();

        DrinkerAdapter listAdapter = new DrinkerAdapter(this, drinkersArrayList);
        drinkersListView.setAdapter(listAdapter);

        drinkersListView.invalidateViews();

        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        totalServed = sharedPref.getLong("totalServed", 30000);
        kegCounter = sharedPref.getInt("kegCounter", BEERS_IN_KEG - 1);

        Log.i("kegCounter",Integer.toString(kegCounter));
    }

    private void saveData() {
        // Save each drinker in the list to the database
        for (Drinker d : drinkersArrayList){
            d.save();
        }

        // Save totalServed & kegCounter to SharedPrefs
        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putLong("totalServed", totalServed);
        editor.putInt("kegCounter", kegCounter);
        editor.apply();
    }

    private void printUsers(){
        for(Drinker d : drinkersArrayList)
            Log.d("loadData",d.toString());
    }

    private void increaseTotalServed() {
        totalServed++;

        // Reduce keg counter
        if(kegCounter > 0)
            kegCounter--;

        // Find DatePoint for today and increment
        List<DatePoint> dpList = DatePoint.find(DatePoint.class, "date = ?", Long.toString(getDate()));
        DatePoint dp = dpList.get(0);
        dp.addDrink();
        dp.save();

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

    private void increaseDailyStat(String nfcId, String name){
        // Start a new chart if it's a new day
        if(pieChartDate < getDate()){
            pieChartDate = getDate();
            dailyStats = new HashMap<>();
        }

        if(dailyStats.containsKey(nfcId)){
            DailyStat ds = dailyStats.get(nfcId);
            ds.addDrink();
        }
        else
            dailyStats.put(nfcId, new DailyStat(name, 1));

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
        if(adminMode) {
            adminMode = false;
            slideToBottom(adminLayout);
            // Update keg counter if changed
            onAdjustKegClick(new View(this));
        }
        else{
            adminMode = true;
            kegEditText.setText(String.valueOf(kegCounter));
            kegEditText.setSelection(kegEditText.getText().length());
            slideToTop(adminLayout);
        }
    }

    // View animation functions
    // Thanks to pvllnspk on StackOverflow
    public static void slideToBottom(View view){
        TranslateAnimation animate = new TranslateAnimation(0,0,0,view.getHeight());
        animate.setDuration(500);
        view.startAnimation(animate);
        view.setVisibility(View.GONE);
    }

    public static void slideToTop(View view){
        TranslateAnimation animate = new TranslateAnimation(0,0,view.getHeight(),0);
        animate.setDuration(500);
        view.startAnimation(animate);
        view.setVisibility(View.VISIBLE);
    }

    public void addDrinker(String name, int credits, String id){
        Drinker drinker = new Drinker(name, credits, id);
        drinkersArrayList.add(drinker);
        drinker.save();
        drinkersListView.invalidateViews();
    }

    public void removeDrinker(int position){
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

    private String getDateString(long x) {
        Date date = new Date(x);
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yy", Locale.US);
        return sdf.format(date);
    }

    @OnItemLongClick(R.id.drinkersListView) boolean onItemLongClick(int position, View view) {
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
    @OnClick(R.id.fab) public void onFabClick(View view) {
        // Create intent to open up dialogue
        Intent intent = new Intent(getApplicationContext(),
                AddDrinkerActivity.class);

        startActivityForResult(intent, ADD_REQ_CODE);
    }

    @OnClick(R.id.adminExitButton) public void onAdminExitClick(View view) {
        toggleAdminMode();
    }

    @OnLongClick(R.id.adjustKegButton) public  boolean onAdjustKegClick(View view) {
        int newValue = Integer.parseInt(kegEditText.getText().toString());

        if(newValue <= BEERS_IN_KEG && newValue >= 0)
            kegCounter = newValue;
        else
            kegCounter = BEERS_IN_KEG;

        updateKegGraph();
        return true;
    }

    @OnLongClick(R.id.newKegButton) public  boolean onNewKegClick(View view) {
        kegEditText.setText(String.valueOf(BEERS_IN_KEG - 1));

        return true;
    }

    @OnClick(R.id.selSoundButton) public void onSelSoundClick(View arg0) {
            final Uri currentTone= RingtoneManager.getActualDefaultRingtoneUri(MainActivity.this,
                    RingtoneManager.TYPE_ALARM);
            Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_NOTIFICATION);
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "Select Tone");
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, currentTone);
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false);
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true);
            startActivityForResult(intent, SOUND_REQ_CODE);
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
        if (requestCode == EDIT_REQ_CODE && resultCode == RESULT_CANCELED && intent != null){
            int position = intent.getIntExtra("drinkerPosition", -1);
            removeDrinker(position);
        }
        // Returning from Confirm with successful credit use/increase
        if (requestCode == CONFIRM_REQ_CODE && resultCode == RESULT_OK && intent != null) {
            Log.i("onActivityResult", "RESULT_OK");
            int position = intent.getIntExtra("drinkerPosition", -1);

            Drinker drinker = drinkersArrayList.get(position);

            if(adminMode) {
                drinker.setCredits(drinker.getCredits()+6);
            }
            else {
                drinker.subtractCredit();
                increaseTotalServed();
                increaseDailyStat(drinker.getNfcId(), drinker.getName());
            }

            drinker.save();
        }
        // Returning from valid adminMode password check
        if (requestCode == ADMIN_REQ_CODE && resultCode == RESULT_OK && intent != null) {
            toggleAdminMode();
        }

        if (requestCode == SOUND_REQ_CODE && resultCode == RESULT_OK && intent != null) {
            soundsList.add((Uri)intent.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI));
            soundsListView.invalidateViews();
        }
    }

    // Called when NFC Tag has been read
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        List<String> ndefMessages = getNfcMessages();

        boolean newUser = true;
        for(String m : ndefMessages){
            // Check if admin card
            if(m.equals(NDEF_PREFIX+ADMIN_NFC_ID)) {
                toggleAdminMode();
                newUser = false;
            }
            // Otherwise search for matching drinker
            else for(Drinker d : drinkersArrayList) {
               String record = NDEF_PREFIX+d.nfcId;
                if(m.equals(record)) {
                    openConfirmActivity(drinkersArrayList.indexOf(d));
                    newUser = false;
                }
            }
        }
        // If we don't find a matching nfcId, open new user dialogue
        if(newUser && adminMode){
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
        saveData();
    }

}

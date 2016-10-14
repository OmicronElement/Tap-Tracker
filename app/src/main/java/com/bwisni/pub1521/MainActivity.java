package com.bwisni.pub1521;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
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
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
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

import be.appfoundry.nfclibrary.activities.NfcActivity;
import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
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
import lecho.lib.hellocharts.view.ColumnChartView;
import lecho.lib.hellocharts.view.ComboLineColumnChartView;
import lecho.lib.hellocharts.view.PieChartView;


public class MainActivity extends NfcActivity {
    public static final String NDEF_PREFIX = "sms:"+AddDrinkerActivity.SMS_NUMBER+"?body=";
    public static final String ADMIN_NFC_ID = "664de4d0-0586-40aa-9518-a854d2657982";
    public static final int ADD_REQ_CODE = 0;
    public static final int EDIT_REQ_CODE = 1;
    public static final int CONFIRM_REQ_CODE = 2;
    public static final int ADMIN_REQ_CODE = 3;
    public static final int ONE_DAY_MS = (1000 * 60 * 60 * 24);
    public static final int BEERS_IN_KEG = 165;
    public static final int KEG_LOW_VALUE = 25;
    public static final int KEG_BG_COLOR = Color.parseColor("#000000");
    public static final int KEG_COLOR = Color.parseColor("#707070");
    private static final int KEG_LOW_COLOR = Color.parseColor("#ff0000");

    private static int mAccentColor;
    private static int mTextColor;

    @Bind(R.id.drinkersListView) ListView drinkersListView;
    @Bind(R.id.numServedTextView) TextSwitcher numServedTextSwitcher;
    @Bind(R.id.AdminLayout) RelativeLayout adminLayout;
    @Bind(R.id.graph) ComboLineColumnChartView graph;
    @Bind(R.id.kegGraph) ColumnChartView kegGraph;
    @Bind(R.id.pieChart) PieChartView pieChart;

    @Bind(R.id.fab) FloatingActionButton fab;

    ArrayList<Drinker> drinkersArrayList = new ArrayList<>();
    private long totalServed;
    private int kegCounter;
    private boolean adminMode = false;
    private long pieChartDate;
    private Map<String, DailyStat> dailyStats = new HashMap<>();

    private PieChartData pieChartData;

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
                        FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT,
                        Gravity.CENTER);
                myText.setLayoutParams(params);

                myText.setTextSize(56);
                myText.setTextColor(mTextColor);
                return myText;
            }
        });

        numServedTextSwitcher.setCurrentText(NumberFormat.getNumberInstance(Locale.US).format(totalServed));
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
        data.setValueLabelTextSize(25);

        graph.setScrollEnabled(true);
        graph.setComboLineColumnChartData(new ComboLineColumnChartData());
        graph.setComboLineColumnChartData(data);

        slideToTop(graph);
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

        ArrayList<SubcolumnValue> subcolumnValues = new ArrayList<>();
        SubcolumnValue sc =  new SubcolumnValue(kegCounter);
        //Add another subcolumn to bring max Y to BEERS_IN_KEG
        SubcolumnValue bg =  new SubcolumnValue(BEERS_IN_KEG - kegCounter);

        if(kegCounter < BEERS_IN_KEG/2){
            bg.setLabel(String.valueOf(kegCounter));
            sc.setLabel("");
        }
        else{
            sc.setLabel(String.valueOf(kegCounter));
            bg.setLabel("");
        }

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
        c.setHasLabels(true);
        columns.add(c);

        ColumnChartData columnChartData = new ColumnChartData(columns);
        columnChartData.setFillRatio(1);
        columnChartData.setStacked(true);
        columnChartData.setValueLabelTextSize(56);
        columnChartData.setValueLabelBackgroundEnabled(false);

        return columnChartData;
    }

    private void initPieChart(){
        pieChartData = new PieChartData(getPieChartData());

        pieChartData.setHasLabels(true);
        pieChartData.setHasLabelsOnlyForSelected(false);
        pieChartData.setHasLabelsOutside(false);
        pieChartData.setHasCenterCircle(true);
        pieChartData.setCenterCircleScale(0.375f);
        pieChartData.setCenterText1("Today's");
        pieChartData.setCenterText1FontSize(13);
        pieChartData.setCenterText1Color(Color.WHITE);
        pieChartData.setCenterText2("Pours");
        pieChartData.setCenterText2FontSize(13);
        pieChartData.setCenterText2Color(Color.WHITE);

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

        for(String key : dailyStats.keySet()){
            DailyStat ds = dailyStats.get(key);
            SliceValue sv = new SliceValue(ds.getNumPours(), KEG_COLOR);
            sv.setLabel(ds.getName());
            values.add(sv);
        }

        return values;
    }

    private void loadData() {
        drinkersArrayList = new ArrayList<>(Drinker.listAll(Drinker.class));

        printUsers();

        DrinkerAdapter listAdapter = new DrinkerAdapter(this, drinkersArrayList);
        drinkersListView.setAdapter(listAdapter);

        drinkersListView.invalidateViews();

        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        totalServed = sharedPref.getLong("totalServed", 30000);
        kegCounter = sharedPref.getInt("kegCounter", BEERS_IN_KEG - 1);

        Log.d("KC",Integer.toString(kegCounter));
    }

    private void saveData() {
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
            Log.d("ARRAY",d.toString());
    }

    private void increaseTotalServed() {
        totalServed++;

        if(kegCounter > 0)
            kegCounter--;

        List<DatePoint> dpList = DatePoint.find(DatePoint.class, "date = ?", Long.toString(getDate()));

        DatePoint dp;
        try {
            dp = dpList.get(0);
            dp.addDrink();
        }
        catch (IndexOutOfBoundsException e){
            dp = new DatePoint(getDate(), 1);
        }

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
        if(pieChartDate < getDate()){
            pieChartDate = getDate();
            dailyStats = new HashMap<>();
        }
        if(dailyStats.containsKey(nfcId)){
            DailyStat ds = dailyStats.get(nfcId);
            ds.setNumPours(ds.getNumPours()+1);
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
            fab.setVisibility(View.GONE);
        }
        else{
            adminMode = true;
            fab.setVisibility(View.VISIBLE);
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
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy", Locale.US);
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

    @OnLongClick(R.id.adminResetButton) public  boolean onAdminResetClick(View view) {
        kegCounter = BEERS_IN_KEG - 1;
        updateKegGraph();
        Toast.makeText(this, "Keg Level Reset", Toast.LENGTH_SHORT).show();
        return true;
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
            int position = intent.getIntExtra("drinkerPosition", -1);

            boolean delete = intent.getBooleanExtra("delete", false);

            if(delete){
                removeDrinker(position);
            }
            else{
                Drinker drinker = drinkersArrayList.get(position);
                drinker.credits = credits;
                drinker.save();
            }

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
    }

    //Called when NFC Tag has been read
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        /*for (String message : getNfcMessages()){
            Toast.makeText(this,message,Toast.LENGTH_SHORT).show();
        }*/

        List<String> ndefMessages = getNfcMessages();

        //byte[] rawMessage = intent.getByteArrayExtra(NfcAdapter.EXTRA_ID);
        //String id = rawMessage.toString();

        boolean newUser = true;
        for(String m : ndefMessages){
            //Toast.makeText(this, s, Toast.LENGTH_SHORT).show();

            if(m.equals(NDEF_PREFIX+ADMIN_NFC_ID)) {
                toggleAdminMode();
                newUser = false;
            }


            for(Drinker d : drinkersArrayList) {
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
        if (id == R.id.action_settings) {
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

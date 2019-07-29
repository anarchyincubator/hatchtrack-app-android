package com.example.hatchtracksensor;

import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Fragment;
import android.os.Handler;
import android.support.design.widget.TabLayout;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import android.widget.LinearLayout;

import java.util.Locale;
import java.util.TimeZone;
import java.text.ParseException;

import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.ImageView;

import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;



public class PeepSensorDataFragment extends Fragment {
    private final SettingsManager.TemperatureUnits CELSIUS =
            SettingsManager.TemperatureUnits.CELSIUS;
    private final SettingsManager.TemperatureUnits FAHRENHEIT =
            SettingsManager.TemperatureUnits.FAHRENHEIT;
    private String bgHex;
    private int current_tab_index;

    private TextView mTextViewTemperature;
    private TextView mTextViewHumidity;

    private TextView mTextViewMaximumTemperature;
    private TextView mTextViewMaximumHumidity;

    private TextView mTextViewMinimumTemperature;
    private TextView mTextViewMinimumwHumidity;

    private TextView mTextViewSDTemperature;
    private TextView mTextViewSDHumidity;

    private ScrollView mSensorScrollView;

    private PeepUnitManager mPeepUnitManager;
    private SettingsManager mSettingsManager;

    private ImageView mImageViewHistory;

    private TextView mSensorTitleDate;

    private JSONArray json;
    private String table_sensor_data;
    float mTemperatureOffsetCelsius = 0.0f;

    private double mSensorAverageTemperature;
    private double mSensorMinimumTemperature;
    private double mSensorMaximumTemperature;
    private double mSensorSDTemperature;

    private double mSensorAverageHumidity;
    private double mSensorMinimumHumidity;
    private double mSensorMaximumHumidity;
    private double mSensorSDHumidity;

    public String current_hatch_uuid;

    public ArrayList<String> hatchList;

    // Poll the database for new data at the provided time interval.
    private final static int mDbPollInterval = 1000 * 60 * 1; // 1 minute

    private Handler mHandler = new Handler();
    private Runnable mHandlerTask = new Runnable() {
        @Override
        public void run() {
            PeepUnit peep = mPeepUnitManager.getPeepUnitActive();
            Log.i("TIMELINE Runnable:","mHandlerTask");

            PeepSensorDataFragment.SensorUpdateJob sensorUpdateJob = new PeepSensorDataFragment.SensorUpdateJob();
            sensorUpdateJob.execute(peep);

            mHandler.postDelayed(mHandlerTask, mDbPollInterval);
        }
    };

    public PeepSensorDataFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_peep_sensor_data, container,false);

        return rootView;
    }


    @Override
    public void onActivityCreated(Bundle bundle) {
        super.onActivityCreated(bundle);

        current_hatch_uuid = "";

        Log.i("TIMELINE SENSOR_DATA:","onActivityCreated");
        mPeepUnitManager = new PeepUnitManager();
        mSettingsManager = new SettingsManager();
        PeepUnit peep = mPeepUnitManager.getPeepUnitActive();

        mTextViewTemperature = getView().findViewById(R.id.textViewTemperature);
        mTextViewHumidity = getView().findViewById(R.id.textViewHumidity);

        mTextViewMaximumTemperature = getView().findViewById(R.id.textViewMaximumTemperature);
        mTextViewMaximumHumidity = getView().findViewById(R.id.textViewMaximumHumidity);

        mTextViewMinimumTemperature = getView().findViewById(R.id.textViewMinimumTemperature);
        mTextViewMinimumwHumidity = getView().findViewById(R.id.textViewMinimumHumidity);

        mTextViewSDTemperature = getView().findViewById(R.id.textViewSDTemperature);
        mTextViewSDHumidity = getView().findViewById(R.id.textViewSDHumidity);

        mSensorScrollView = getView().findViewById(R.id.SensorScrollView);

        mImageViewHistory = getView().findViewById(R.id.ImageViewHistory);

        mSensorTitleDate = getView().findViewById(R.id.SensorTitleDate);

        current_tab_index = 1;

        TabLayout tabLayout = getView().findViewById(R.id.sensor_data_tabs);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                mSensorScrollView.fullScroll(ScrollView.FOCUS_UP);
                resetAvgTextViews();
                //viewPager.setCurrentItem(tab.getPosition());
                current_tab_index = tab.getPosition();

                if(current_tab_index == 2){
                    current_tab_index = 7;
                }
                if(current_tab_index == 3){
                    current_tab_index = 999;
                }
                if(current_tab_index == 1){
                    current_tab_index = 3;
                }
                if(current_tab_index == 0){
                    current_tab_index = 1;
                }

                startRepeatingTask();
            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                //viewPager.setCurrentItem(tab.getPosition());
                //reloadSensorDataTable();
            }
            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                mSensorScrollView.fullScroll(ScrollView.FOCUS_UP);
                //viewPager.setCurrentItem(tab.getPosition());
                //reloadSensorDataTable();
            }
        });

        mImageViewHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                buildHatchHistoryList();
            }
        });

        startRepeatingTask();
    }

    public void reloadSensorDataTable(){

        PeepUnit peep = mPeepUnitManager.getPeepUnitActive();


        JSONObject json_obj = null;
        String val_time_start = "";
        String val_time_end = "";

        try {
            json_obj = json.getJSONObject(json.length()-1);
            val_time_start = json_obj.getString("time");
            json_obj = json.getJSONObject(0);
            val_time_end = json_obj.getString("time");
        } catch (JSONException e) {
            e.printStackTrace();
        }



        SimpleDateFormat userTimeParse = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ENGLISH);
        Date parse_date_start = null;
        Date parse_date_end = null;
        //userTime.setTimeZone(TimeZone.getDefault());
        try{
            parse_date_start = userTimeParse.parse(val_time_start);
            parse_date_end = userTimeParse.parse(val_time_end);
        } catch (Exception e) {
            e.printStackTrace();
        }




        SimpleDateFormat hourTimeFormat = new SimpleDateFormat("M/d", Locale.ENGLISH);
        //hourTime.setTimeZone(TimeZone.getDefault());
        val_time_start = hourTimeFormat.format(parse_date_start);
        val_time_end = hourTimeFormat.format(parse_date_end);


        String title_date = peep.getName()+"\n "+val_time_start+" - "+val_time_end;
        if(val_time_start.equals(val_time_end))title_date = peep.getName()+"\n "+val_time_end;
        mSensorTitleDate.setText(title_date);


        SettingsManager.TemperatureUnits units = mSettingsManager.getTemperatureUnits();
        float offset = peep.getLastHatch().getTemperatureOffsetCelsius();
        mTemperatureOffsetCelsius = offset;

        for (int index = 0; index < json.length(); index++) {
            try {
                JSONObject curr_object = json.getJSONObject(index);
                String val_temperature  = curr_object.getString("temperature");
                String val_humidity = curr_object.getString("humidity");
                String val_time = curr_object.getString("time");

                mSensorAverageTemperature = mSensorAverageTemperature + Float.parseFloat(val_temperature);
                if(mSensorMinimumTemperature>=Float.parseFloat(val_temperature) || mSensorMinimumTemperature == 0)mSensorMinimumTemperature = Float.parseFloat(val_temperature);
                if(mSensorMaximumTemperature<=Float.parseFloat(val_temperature))mSensorMaximumTemperature = Float.parseFloat(val_temperature);
                //mSensorSDTemperature = mSensorSDTemperature + Float.parseFloat(val_temperature);

                mSensorAverageHumidity = mSensorAverageHumidity + Float.parseFloat(val_humidity);
                if(mSensorMinimumHumidity>=Float.parseFloat(val_humidity) || mSensorMinimumHumidity == 0)mSensorMinimumHumidity = Float.parseFloat(val_humidity);
                if(mSensorMaximumHumidity<=Float.parseFloat(val_humidity))mSensorMaximumHumidity = Float.parseFloat(val_humidity);
                //mSensorSDHumidity = mSensorSDHumidity + Float.parseFloat(val_humidity);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }



        //Log.i("sensor",json.length() + String.valueOf(mSensorAverageTemperature));
        //mSensorAverageTemperature = mSensorAverageTemperature/json.length();
        mSensorAverageTemperature = (mSensorMinimumTemperature + mSensorMaximumTemperature)/2;
        //mSensorAverageHumidity = mSensorAverageHumidity/json.length();
        mSensorAverageHumidity = (mSensorMinimumHumidity + mSensorMaximumHumidity)/2;
        //Log.i("sensor 2",String.valueOf(mSensorAverageTemperature));

        if (FAHRENHEIT == units) {
            mSensorAverageTemperature = (mSensorAverageTemperature * 1.8) + 32.0;
        }

        //Log.i("sensor 3",String.valueOf(mSensorAverageTemperature)+" F");


        float sd_temp = 0.0f;
        float sd_humid = 0.0f;

        float sd_temp_total = 0.0f;
        float sd_humid_total = 0.0f;

        for (int index = 0; index < json.length(); index++) {
            try {
                JSONObject curr_object = json.getJSONObject(index);
                double val_temperature  = Double.valueOf(curr_object.getString("temperature"));
                double val_humidity = Double.valueOf(curr_object.getString("humidity"));

                if (FAHRENHEIT == units) {
                    val_temperature = (val_temperature * 1.8) + 32.0;
                }

                //Log.i("val_humidity",String.valueOf(val_humidity));
                //Log.i("val_temperature",String.valueOf(val_temperature) )   ;

                sd_temp = ((float)val_temperature - (float)mSensorAverageTemperature) * ((float)val_temperature - (float)mSensorAverageTemperature);
                //Log.i("val_temperature SQRD",String.valueOf(sd_temp));

                sd_temp_total = sd_temp_total + sd_temp;

                //Log.i("sd_temp",String.valueOf(sd_temp));
                //Log.i("sd_temp_total",String.valueOf(sd_temp_total));

                sd_humid = ((float)val_humidity - (float)mSensorAverageHumidity) * ((float)val_humidity - (float)mSensorAverageHumidity);
                sd_humid_total = sd_humid_total + sd_humid;

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        //Log.i("00000000",String.valueOf(sd_humid_total) + " - json_length" + json.length());

        //Log.i("12121212",String.valueOf(sd_temp_total));
        sd_temp = sd_temp_total/json.length();
        sd_humid = sd_humid_total/json.length();

        //Log.i("11111111",String.valueOf(sd_temp));

        sd_temp = (float)Math.sqrt(sd_temp);
        sd_humid = (float)Math.sqrt(sd_humid);

        //Log.i("22222222",String.valueOf(sd_temp));


        double t = mSensorAverageTemperature;

        //SET TEMPERATURE AVERAGE
        String fmt = "";
        String fmt2 = "";
        if (CELSIUS == units) {
            fmt = String.format(Locale.US, "%.1f", t) + " ℃";
        }
        else if (FAHRENHEIT == units) {
            //fmt = String.format(Locale.US, "%.1f", (t * 1.8) + 32.0) + " ℉";
            fmt = String.format(Locale.US, "%.1f", t) + " ℉";
        }
        mTextViewTemperature.setText(fmt);

        //t = peepMeasurement.getmHumidity();
       // t = Double.parseDouble(mSensorAverageHumidity);
        t = mSensorAverageHumidity;
        // SET HUMIDITY AVERAGE Convert InfluxDB UTC times to the local time of the user.
        fmt2 = String.format(
                Locale.US, "%.1f",
               t) + " %";
        mTextViewHumidity.setText(fmt2);

        t = mSensorMaximumTemperature;
        if (CELSIUS == units) {
            fmt = String.format(Locale.US, "%.1f", t) + " ℃";
        }
        else if (FAHRENHEIT == units) {
            fmt = String.format(Locale.US, "%.1f", (t * 1.8) + 32.0) + " ℉";
        }
        t = mSensorMaximumHumidity;
        fmt2 = String.format(
                Locale.US, "%.1f",
                t) + " %";
        mTextViewMaximumTemperature.setText(fmt);
        mTextViewMaximumHumidity.setText(fmt2);

        t = mSensorMinimumTemperature;
        if (CELSIUS == units) {
            fmt = String.format(Locale.US, "%.1f", t) + " ℃";
        }
        else if (FAHRENHEIT == units) {
            fmt = String.format(Locale.US, "%.1f", (t * 1.8) + 32.0) + " ℉";
        }
        t = mSensorMinimumHumidity;
        fmt2 = String.format(
                Locale.US, "%.1f",
                t) + " %";
        mTextViewMinimumTemperature.setText(fmt);
        mTextViewMinimumwHumidity.setText(fmt2);

        t = sd_temp;
        if (CELSIUS == units) {
            fmt = String.format(Locale.US, "%.1f", t) + " ℃";
        }
        else if (FAHRENHEIT == units) {
            //fmt = String.format(Locale.US, "%.1f", (t * 1.8) + 32.0) + " ℉";
            fmt = String.format(Locale.US, "%.1f", t) + " ℉";

        }
        t = sd_humid;
        fmt2 = String.format(
                Locale.US, "%.1f",
                t) + " %";
        mTextViewSDTemperature.setText(fmt);
        mTextViewSDHumidity.setText(fmt2);



        TableLayout ll = (TableLayout) getView().findViewById(R.id.tableSensorDataTimeline);

        //RENDER TABLE ROWS
        for (int i = 0; i < json.length(); i++) {
            try {
                JSONObject jsonObject = json.getJSONObject(i);

                //Set table row
                bgHex = "#FFFFFF";
                if (i % 2 == 0) bgHex = "#EFEFEF";

                //Create TableRow
                TableRow tbrow = new TableRow(getActivity().getApplicationContext());
                tbrow.setLayoutParams(new TableLayout.LayoutParams(
                        TableLayout.LayoutParams.MATCH_PARENT,
                        TableLayout.LayoutParams.WRAP_CONTENT, 1.0f));
                tbrow.setBackgroundColor(android.graphics.Color.parseColor(bgHex));
                tbrow.setPadding(10, 10, 10, 10);

                //Create Time TextView - 1).
                TextView tv0 = new TextView(getActivity().getApplicationContext());
                tv0.setLayoutParams(new TableRow.LayoutParams(
                        TableRow.LayoutParams.MATCH_PARENT,
                        TableRow.LayoutParams.WRAP_CONTENT, 0.2f));
                tv0.setGravity(Gravity.CENTER_HORIZONTAL);

                //Create Time TextView - 1).
                TextView tv1 = new TextView(getActivity().getApplicationContext());
                tv1.setLayoutParams(new TableRow.LayoutParams(
                        TableRow.LayoutParams.MATCH_PARENT,
                        TableRow.LayoutParams.WRAP_CONTENT, 0.2f));
                tv1.setGravity(Gravity.CENTER_HORIZONTAL);

                //DateTimeZone.setDefault(DateTimeZone.forID(TimeZone.getDefault().getID()))

                String strDate = jsonObject.getString("time");
                //Log.i("strDate",strDate);
                SimpleDateFormat userTime = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ENGLISH);
                Date parse_date = null;
                //userTime.setTimeZone(TimeZone.getDefault());
                try{
                    parse_date = userTime.parse(strDate);
                    //Log.i("parse_date",parse_date.toString());
                } catch (Exception e) {
                    e.printStackTrace();
                }

                TimeZone tz = TimeZone.getDefault();
                //System.out.println("TimeZone   "+tz.getDisplayName(false, TimeZone.SHORT)+" Timezon id :: " +tz.getID());

                SimpleDateFormat hourTime = new SimpleDateFormat("M/d", Locale.ENGLISH);
                //hourTime.setTimeZone(TimeZone.getDefault());
                String localTime = hourTime.format(parse_date);
                //String text = "Last Updated: " + localTime;

                tv0.setTextSize(16.0f);
                tv0.setText(localTime);
                tv0.setTypeface(tv0.getTypeface(), tv0.getTypeface().BOLD);

                tbrow.addView(tv0);

                hourTime = new SimpleDateFormat("h:mm a",
                        Locale.ENGLISH);
                //hourTime.setTimeZone(TimeZone.getDefault());
                //hourTime.setTimeZone(TimeZone.getTimeZone("PST"));
                localTime = hourTime.format(parse_date);

                tv1.setTextSize(16.0f);
                tv1.setText(localTime);
                tv1.setTypeface(tv1.getTypeface(), tv1.getTypeface().BOLD);

                tbrow.addView(tv1);

                //Create Time TextView - 2).
                TextView tv2 = new TextView(getActivity().getApplicationContext());
                tv2.setLayoutParams(new TableRow.LayoutParams(
                        TableRow.LayoutParams.MATCH_PARENT,
                        TableRow.LayoutParams.WRAP_CONTENT, 0.4f));
                tv2.setGravity(Gravity.CENTER_HORIZONTAL);
                //String temp_string = jsonObject.getString("temperature");
                //tv2.setText(temp_string+" °F");

                String temp_string = jsonObject.getString("temperature");

                //t = Double.valueOf(jsonObject.getString("temperature"));

                t = Double.parseDouble(jsonObject.getString("temperature"));

                fmt = "";
                if (CELSIUS == units) {
                    fmt = String.format(Locale.US, "%.1f", t) + " ℃";
                }
                else if (FAHRENHEIT == units) {
                    fmt = String.format(Locale.US, "%.1f", (t * 1.8) + 32.0) + " ℉";
                }

                tv2.setTextSize(16.0f);
                tv2.setText(fmt);


                tv2.setTypeface(tv1.getTypeface(), tv1.getTypeface().BOLD);

                //If hotter than 102f, text = red
                if(Float.valueOf(temp_string)>102) {
                    tv2.setTextColor(0xffdda3a3);
                }

                //If hotter than 99f, text = blue
                if(Float.valueOf(temp_string)<99) {
                    tv2.setTextColor(0xffb0c4fb);
                }

                tbrow.addView(tv2);

                //Create Time TextView - 3).
                TextView tv3 = new TextView(getActivity().getApplicationContext());
                tv3.setLayoutParams(new TableRow.LayoutParams(
                        TableRow.LayoutParams.MATCH_PARENT,
                        TableRow.LayoutParams.WRAP_CONTENT, 0.4f));
                tv3.setGravity(Gravity.CENTER_HORIZONTAL);

                tv3.setTextSize(16.0f);
                tv3.setText(jsonObject.getString("humidity")+"%");
                tv3.setTypeface(tv1.getTypeface(), tv1.getTypeface().BOLD);
                tbrow.addView(tv3);

                //Add TableRow
                ll.addView(tbrow, new TableLayout.LayoutParams(
                        TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT));
            } catch (JSONException e) {
                //Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
        }
        stopRepeatingTask();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Do something?
    }

    private void clearTable(){
        //GET TABLE
        TableLayout ll = (TableLayout) getView().findViewById(R.id.tableSensorDataTimeline);

        //CLEAR TABLE
        int childCount = ll.getChildCount();
        ll.removeViews(1, childCount - 1);
    }

    private void startRepeatingTask() {
        mHandlerTask.run();
    }

    private void stopRepeatingTask() {
        mHandler.removeCallbacks(mHandlerTask);
    }

    private void resetAvgTextViews(){
        clearTable();
        mTextViewTemperature.setText("---");
        mTextViewHumidity.setText("---");
        mTextViewMaximumTemperature.setText("---");
        mTextViewMaximumHumidity.setText("---");
        mTextViewMinimumTemperature.setText("---");
        mTextViewMinimumwHumidity.setText("---");
        mTextViewSDTemperature.setText("---");
        mTextViewSDHumidity.setText("---");
        mSensorTitleDate.setText("---");
    }

    private class SensorUpdateJob extends AsyncTask<PeepUnit, Void, PeepUnit> {
        @Override
        protected PeepUnit doInBackground(PeepUnit... peeps) {

            PeepUnit peep = peeps[0];

            Log.i("accessToken:","accessToken ");
            String accessToken = RestApi.postUserAuth(
                    peep.getUserEmail(),
                    peep.getUserPassword());

            Log.i("TIMELINE BG:","accessToken: "+accessToken);


            JSONArray timelineJSON = RestApi.getPeepTimeline(
                    accessToken,
                    peep,current_tab_index,current_hatch_uuid);

            json = timelineJSON;
            Log.i("JSON - TABLE ",timelineJSON.toString());

            //peep.setMeasurement(peepMeasurement);

            hatchList = RestApi.getHatchUUIDs(accessToken, peep);
            Collections.reverse(hatchList);
            return peep;
        }

        @Override
        protected void onPostExecute(PeepUnit peep) {

            try {
                //json = timelineJSON;

                if(json.length() != 0) {
                    PeepMeasurement peepMeasurement = peep.getMeasurement();
                    SettingsManager.TemperatureUnits units = mSettingsManager.getTemperatureUnits();
                    String fmt = "";

                    long unixTime = System.currentTimeMillis() / 1000L;


                    // Display Temperature value in the user specified units.
                    double t = peepMeasurement.getTemperature() + mTemperatureOffsetCelsius;
                    if (CELSIUS == units) {
                        fmt = String.format(Locale.US, "%.1f", t) + " ℃";
                    } else if (FAHRENHEIT == units) {
                        fmt = String.format(Locale.US, "%.1f", (t * 1.8) + 32.0) + " ℉";
                    }
                    mTextViewTemperature.setText(fmt);

                    // Convert InfluxDB UTC times to the local time of the user.
                    fmt = String.format(
                            Locale.US, "%.1f",
                            peepMeasurement.getmHumidity()) + " %";
                    mTextViewHumidity.setText(fmt);

                    // User readable time representation.
                    DateFormat userTime = new SimpleDateFormat(
                            "MMM dd, yyyy HH:mm a",
                            Locale.ENGLISH);

                    userTime.setTimeZone(TimeZone.getDefault());
                    Date date = new Date(peepMeasurement.getUnixTimestamp() * 1000);
                    String localTime = userTime.format(date);
                    String text = "Last Updated: " + localTime;

                    Log.i("TIMELINE onpost: ", text + " " + fmt);

                    reloadSensorDataTable();
                }

            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    public void buildHatchHistoryList(){

        String[] mStringArray = new String[hatchList.size()];
        mStringArray = hatchList.toArray(mStringArray);

        final String[] action = mStringArray;
        //final int peepSelect = position;
        //final float length = (float)action.length-1;
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Hatch Data");
        builder.setItems(action, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.i("BUILDER", String.valueOf(which));

                current_hatch_uuid = hatchList.get(which);
                if(String.valueOf(which).equals("0"))current_hatch_uuid = "";
                Log.i("current_hatch_uuid",current_hatch_uuid);
                resetAvgTextViews();
                //startRepeatingTask();
                TabLayout tabLayout = getView().findViewById(R.id.sensor_data_tabs);
                if(tabLayout.getSelectedTabPosition() != 3) {
                    TabLayout.Tab tab = tabLayout.getTabAt(3);
                    tab.select();
                }else{
                    startRepeatingTask();
                }
                if(current_hatch_uuid.equals("")){
                    enableTabs();
                    TabLayout.Tab tab = tabLayout.getTabAt(0);
                    tab.select();
                }else{
                    disabledTabs();
                }
            }
        });
        builder.show();
    }

    public void disabledTabs(){
        TabLayout tabLayout = getView().findViewById(R.id.sensor_data_tabs);

        LinearLayout tabStrip = ((LinearLayout)tabLayout.getChildAt(0));
        tabStrip.setEnabled(false);
        for(int i = 0; i < tabStrip.getChildCount(); i++) {
            if(i<3) {
                tabStrip.getChildAt(i).setClickable(false);
            }
        }
    }

    public void enableTabs(){
        TabLayout tabLayout = getView().findViewById(R.id.sensor_data_tabs);
        LinearLayout tabStrip = ((LinearLayout)tabLayout.getChildAt(0));
        tabStrip.setEnabled(true);
        for(int i = 0; i < tabStrip.getChildCount(); i++) {
            tabStrip.getChildAt(i).setClickable(true);
        }
    }

    public void popupHistorySelector(){

    }
}
package com.example.hatchtracksensor;

import android.app.FragmentTransaction;
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

import java.util.Locale;
import java.util.TimeZone;
import java.text.ParseException;

import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

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

        current_tab_index = 1;

        TabLayout tabLayout = getView().findViewById(R.id.sensor_data_tabs);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                mSensorScrollView.fullScroll(ScrollView.FOCUS_UP);

                //viewPager.setCurrentItem(tab.getPosition());
                current_tab_index = tab.getPosition();
                if(current_tab_index == 0){
                    current_tab_index = 1;
                }
                if(current_tab_index == 2){
                    current_tab_index = 7;
                }
                if(current_tab_index == 3){
                    current_tab_index = 999;
                }
                if(current_tab_index == 1){
                    current_tab_index = 3;
                }
                resetAvgTextViews();
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
        startRepeatingTask();
    }

    public void reloadSensorDataTable(){

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

        mSensorAverageTemperature = mSensorAverageTemperature/json.length();
        mSensorAverageHumidity = mSensorAverageHumidity/json.length();

        PeepUnit peep = mPeepUnitManager.getPeepUnitActive();
        SettingsManager.TemperatureUnits units = mSettingsManager.getTemperatureUnits();
        float offset = peep.getLastHatch().getTemperatureOffsetCelsius();
        //float offset = 0.0f;
        mTemperatureOffsetCelsius = offset;
        // Display Temperature value in the user specified units.

       //GET ACTUAL DATA double t = peepMeasurement.getTemperature() + mTemperatureOffsetCelsius;

        //double t = Double.parseDouble(mSensorAverageTemperature);

        double t = mSensorAverageTemperature;

        //SET TEMPERATURE AVERAGE
        String fmt = "";
        String fmt2 = "";
        if (CELSIUS == units) {
            fmt = String.format(Locale.US, "%.1f", t) + " ℃";
        }
        else if (FAHRENHEIT == units) {
            fmt = String.format(Locale.US, "%.1f", (t * 1.8) + 32.0) + " ℉";
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

        t = -17.7778;
        if (CELSIUS == units) {
            fmt = String.format(Locale.US, "%.1f", t) + " ℃";
        }
        else if (FAHRENHEIT == units) {
            fmt = String.format(Locale.US, "%.1f", (t * 1.8) + 32.0) + " ℉";
        }
        t = 0.0;
        fmt2 = String.format(
                Locale.US, "%.1f",
                t) + " %";
        mTextViewSDTemperature.setText(fmt);
        mTextViewSDHumidity.setText(fmt2);

        //GET TABLE
        TableLayout ll = (TableLayout) getView().findViewById(R.id.tableSensorDataTimeline);

        //CLEAR TABLE
        int childCount = ll.getChildCount();
        ll.removeViews(1, childCount - 1);

        /*
        table_sensor_data = "[{\"time\":\"PLACEHOLDER\",\"temperature\":\"98.0\",\"humidity\":\"59.4\"},{\"time\":\"10:00pm\",\"temperature\":\"98.0\",\"humidity\":\"48.5\"},{\"time\":\"9:00pm\",\"temperature\":\"102.0\",\"humidity\":\"41.6\"},{\"time\":\"8:00pm\",\"temperature\":\"101.0\",\"humidity\":\"48.5\"},{\"time\":\"7:00pm\",\"temperature\":\"98.0\",\"humidity\":\"56.4\"},{\"time\":\"6:00pm\",\"temperature\":\"99.0\",\"humidity\":\"59.4\"},{\"time\":\"5:00pm\",\"temperature\":\"104.0\",\"humidity\":\"52.5\"},{\"time\":\"4:00pm\",\"temperature\":\"99.0\",\"humidity\":\"49.5\"},{\"time\":\"3:00pm\",\"temperature\":\"100.0\",\"humidity\":\"56.4\"},{\"time\":\"2:00pm\",\"temperature\":\"99.0\",\"humidity\":\"50.5\"},{\"time\":\"1:00pm\",\"temperature\":\"99.0\",\"humidity\":\"45.5\"},{\"time\":\"12:00pm\",\"temperature\":\"99.0\",\"humidity\":\"43.6\"},{\"time\":\"11:00am\",\"temperature\":\"100.0\",\"humidity\":\"43.6\"},{\"time\":\"10:00am\",\"temperature\":\"98.0\",\"humidity\":\"42.6\"},{\"time\":\"9:00am\",\"temperature\":\"102.0\",\"humidity\":\"56.4\"},{\"time\":\"8:00am\",\"temperature\":\"104.0\",\"humidity\":\"50.5\"},{\"time\":\"7:00am\",\"temperature\":\"103.0\",\"humidity\":\"57.4\"},{\"time\":\"6:00am\",\"temperature\":\"102.0\",\"humidity\":\"51.5\"},{\"time\":\"5:00am\",\"temperature\":\"100.0\",\"humidity\":\"56.4\"},{\"time\":\"4:00am\",\"temperature\":\"102.0\",\"humidity\":\"49.5\"},{\"time\":\"3:00am\",\"temperature\":\"98.0\",\"humidity\":\"47.5\"},{\"time\":\"2:00am\",\"temperature\":\"101.0\",\"humidity\":\"54.5\"},{\"time\":\"1:00am\",\"temperature\":\"98.0\",\"humidity\":\"39.6\"},{\"time\":\"12:00am\",\"temperature\":\"102.0\",\"humidity\":\"52.5\"}]";
        try {
           json = new JSONArray(table_sensor_data);
        } catch (JSONException e) {
            //Log.e("Error", e.getMessage());
            e.printStackTrace();
        }
        */
        //json = new JSONObject(data);

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
                TextView tv1 = new TextView(getActivity().getApplicationContext());
                tv1.setLayoutParams(new TableRow.LayoutParams(
                        TableRow.LayoutParams.MATCH_PARENT,
                        TableRow.LayoutParams.WRAP_CONTENT, 0.2f));
                tv1.setGravity(Gravity.CENTER_HORIZONTAL);


                String strDate = jsonObject.getString("time");
                SimpleDateFormat userTime = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
                Date parse_date = null;
                userTime.setTimeZone(TimeZone.getDefault());
                try{
                    parse_date= userTime.parse(strDate);
                } catch (Exception e) {
                    e.printStackTrace();
                }


                SimpleDateFormat hourTime = new SimpleDateFormat("M/d - h:mm a");
                hourTime.setTimeZone(TimeZone.getDefault());
                String localTime = hourTime.format(parse_date);
                //String text = "Last Updated: " + localTime;

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

    private void startRepeatingTask() {
        mHandlerTask.run();
    }

    private void stopRepeatingTask() {
        mHandler.removeCallbacks(mHandlerTask);
    }

    private void resetAvgTextViews(){
        mTextViewTemperature.setText("---");
        mTextViewHumidity.setText("---");
        mTextViewMaximumTemperature.setText("---");
        mTextViewMaximumHumidity.setText("---");
        mTextViewMinimumTemperature.setText("---");
        mTextViewMinimumwHumidity.setText("---");
        mTextViewSDTemperature.setText("---");
        mTextViewSDHumidity.setText("---");
    }

    private class SensorUpdateJob extends AsyncTask<PeepUnit, Void, PeepUnit> {
        @Override
        protected PeepUnit doInBackground(PeepUnit... peeps) {

            PeepUnit peep = peeps[0];

            String accessToken = RestApi.postUserAuth(
                    peep.getUserEmail(),
                    peep.getUserPassword());

            Log.i("TIMELINE BG:","accessToken: "+accessToken);


            JSONArray timelineJSON = RestApi.getPeepTimeline(
                    accessToken,
                    peep,current_tab_index);

            json = timelineJSON;
            Log.i("JSON - TABLE ",timelineJSON.toString());

            //peep.setMeasurement(peepMeasurement);

            return peep;
        }

        @Override
        protected void onPostExecute(PeepUnit peep) {
            try {
                //json = timelineJSON;

                PeepMeasurement peepMeasurement = peep.getMeasurement();
                SettingsManager.TemperatureUnits units = mSettingsManager.getTemperatureUnits();
                String fmt = "";

                long unixTime = System.currentTimeMillis() / 1000L;


                // Display Temperature value in the user specified units.
                double t = peepMeasurement.getTemperature() + mTemperatureOffsetCelsius;
                if (CELSIUS == units) {
                    fmt = String.format(Locale.US, "%.1f", t) + " ℃";
                }
                else if (FAHRENHEIT == units) {
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

                Log.i("TIMELINE onpost: ",text+" "+fmt);

                reloadSensorDataTable();

            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
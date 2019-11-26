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
import android.util.Pair;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import java.util.Collections;

import android.widget.LinearLayout;
import android.widget.RelativeLayout;

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
import android.content.SharedPreferences;




public class PeepSensorDataFragment extends Fragment {
    private final SettingsManager.TemperatureUnits CELSIUS =
            SettingsManager.TemperatureUnits.CELSIUS;
    private final SettingsManager.TemperatureUnits FAHRENHEIT =
            SettingsManager.TemperatureUnits.FAHRENHEIT;
    private String bgHex;
    private int current_tab_index;

    private TextView mTextViewTemperature;
    private TextView mTextViewHumidity;

    private TabLayout mTabLayoutSensorData;

    private TextView mTextViewMaximumTemperature;
    private TextView mTextViewMaximumHumidity;

    private TextView mTextViewMinimumTemperature;
    private TextView mTextViewMinimumwHumidity;

    private TextView mTextViewSDTemperature;
    private TextView mTextViewSDHumidity;

    private TextView mTextViewTitleAverage;
    private TextView mTextViewTitleMax;
    private TextView mTextViewTitleMin;
    private TextView mTextViewTitleSD;

    private TextView mTextViewTitleHeader;
    private TextView mTextViewEggCountSpeciesHeader;
    private TextView mTextViewTimeframeHeader;
    private TextView mTextViewPeepNameHeader;

    private TableRow mTrHatchedCount;

    private ScrollView mSensorScrollView;

    private AccountManager mAccountManager;

    private PeepUnitManager mPeepUnitManager;
    private SettingsManager mSettingsManager;

    private List<String> speciesUUIDArray;

    //private ImageView mImageViewHistory;

    //private TextView mSensorTitleDate;

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

    private PeepUnit peep;
    private PeepHatch mPeepHatch;

    private String[] mHatchUUIDArray;
    private String[] mHatchStartEndArray;
    public ArrayList<PeepHatch> peepHatches;

    public String current_hatch_uuid;
    public int currentHatchUUIDIndex;

    public ArrayList<String> hatchUUIDList;

    // Poll the database for new data at the provided time interval.
    private final static int mDbPollInterval = 1000 * 60 * 1; // 1 minute


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

        // Placeholder species UUID array
        speciesUUIDArray = new ArrayList<String>();
        speciesUUIDArray.add("90df88e3-5ed5-4a1f-a689-97dfc097ebf7"); // Chicken
        speciesUUIDArray.add("c0999080-7749-4c9b-ada1-947ec383a845"); // Duck
        // END Placeholder species UUID array

        SharedPreferences preferences = getContext().getSharedPreferences("UserData", getContext().getApplicationContext().MODE_PRIVATE);
        String HatchListSelectedUUID = preferences.getString("selected_hatch_uuid", null);

        Log.i("HatchList","HatchListSelectedUUID: "+HatchListSelectedUUID);

       // current_hatch_uuid = "";
        if(!HatchListSelectedUUID.equals(""))current_hatch_uuid = HatchListSelectedUUID;

        Log.i("TIMELINE SENSOR_DATA:","onActivityCreated");
        mPeepUnitManager = new PeepUnitManager();
        mSettingsManager = new SettingsManager();
        //PeepUnit peep = mPeepUnitManager.getPeepUnitActive();

        if(mPeepUnitManager.mPeepList.size()>0) {
            peep = mPeepUnitManager.getPeepUnitActive();
            Log.i("peep ACTIVE",peep.getUUID());
        }


        peepHatches = new ArrayList<PeepHatch>();

        mTrHatchedCount = getView().findViewById(R.id.tr_hatched_count);

        mTextViewPeepNameHeader = getView().findViewById(R.id.textViewPeepNameHeader);

        mTextViewTitleHeader = getView().findViewById(R.id.textViewTitleHeader);
        mTextViewEggCountSpeciesHeader = getView().findViewById(R.id.textViewEggCountSpeciesHeader);
        mTextViewTimeframeHeader = getView().findViewById(R.id.textViewTimeframeHeader);

        mTextViewTitleAverage = getView().findViewById(R.id.textViewTitleAverage);
        mTextViewTitleMax = getView().findViewById(R.id.textViewTitleMax);
        mTextViewTitleMin = getView().findViewById(R.id.textViewTitleMin);
        mTextViewTitleSD = getView().findViewById(R.id.textViewTitleSD);

        mTextViewTemperature = getView().findViewById(R.id.textViewTemperature);
        mTextViewHumidity = getView().findViewById(R.id.textViewHumidity);

        mTextViewMaximumTemperature = getView().findViewById(R.id.textViewMaximumTemperature);
        mTextViewMaximumHumidity = getView().findViewById(R.id.textViewMaximumHumidity);

        mTextViewMinimumTemperature = getView().findViewById(R.id.textViewMinimumTemperature);
        mTextViewMinimumwHumidity = getView().findViewById(R.id.textViewMinimumHumidity);

        mTextViewSDTemperature = getView().findViewById(R.id.textViewSDTemperature);
        mTextViewSDHumidity = getView().findViewById(R.id.textViewSDHumidity);

        mSensorScrollView = getView().findViewById(R.id.SensorScrollView);

        //mImageViewHistory = getView().findViewById(R.id.ImageViewHistory);

        //mSensorTitleDate = getView().findViewById(R.id.SensorTitleDate);

        current_tab_index = 1;


        mTabLayoutSensorData = getView().findViewById(R.id.sensor_data_tabs);
        mTabLayoutSensorData.setVisibility(View.GONE);
        mTrHatchedCount.setVisibility(View.GONE);

        //DEBUG
        //if(!current_hatch_uuid.equals(peep.getLastHatch().getUUID())){

        //TabLayout tabLayout = getView().findViewById(R.id.tabLayoutSensorData);
        mTabLayoutSensorData.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                mSensorScrollView.fullScroll(ScrollView.FOCUS_UP);
                resetAvgTextViews();
                //viewPager.setCurrentItem(tab.getPosition());
                current_tab_index = tab.getPosition();

                if(current_tab_index == 2){
                    //3 day
                    current_tab_index = 7;
                    mTextViewTitleAverage.setText("Week Average");
                    mTextViewTitleMax.setText("Week Maxmium");
                    mTextViewTitleMin.setText("Week Minimum");
                    mTextViewTitleSD.setText("Avg. Deviation");
                }
                if(current_tab_index == 3){
                    // all
                    current_tab_index = 999;
                    mTextViewTitleAverage.setText("Hatch Average");
                    mTextViewTitleMax.setText("Hatch Maxmium");
                    mTextViewTitleMin.setText("Hatch Minimum");
                    mTextViewTitleSD.setText("Avg. Deviation");
                }
                if(current_tab_index == 1){
                    // week
                    current_tab_index = 3;
                    mTextViewTitleAverage.setText("3 Day Average");
                    mTextViewTitleMax.setText("3 Day Maxmium");
                    mTextViewTitleMin.setText("3 Day Minimum");
                    mTextViewTitleSD.setText("Avg. Deviation");
                }
                if(current_tab_index == 0){
                    //1 day
                    current_tab_index = 1;
                    mTextViewTitleAverage.setText("Today's Average");
                    mTextViewTitleMax.setText("Today's Maxmium");
                    mTextViewTitleMin.setText("Today's Minimum");
                    mTextViewTitleSD.setText("Avg. Deviation");
                }

                getData();
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

        /* mImageViewHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getHatchHistoryData();
            }
        }); */

        mAccountManager = new AccountManager(getContext());

        getData();
    }



    public void reloadSensorDataTable(){

        getActivity().setTitle("");
        //PeepUnit peep = mPeepUnitManager.getPeepUnitActive();

        //String accessToken = RestApi.postUserAuth( peep.getUserEmail(), peep.getUserPassword());

        //mPeepHatch = RestApi.getHatch(accessToken, current_hatch_uuid);

        SimpleDateFormat userTime = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ENGLISH);

        TimeZone tz2 = TimeZone.getDefault();
        TimeZone timezoneID = TimeZone.getTimeZone(tz2.getDisplayName(false, TimeZone.SHORT));


        Date dateStart = new java.util.Date();
        Date dateEnd = new java.util.Date();
        Date dateCurr = new java.util.Date();
        try{
            dateStart = new java.util.Date(mPeepHatch.getStartUnixTimestamp()*1000L);
            dateEnd = new java.util.Date(mPeepHatch.getEndUnixTimestamp()*1000L);
            Log.i("mPeepHatch data",String.valueOf(mPeepHatch.getEndUnixTimestamp()));
        }catch(Exception e){
            e.printStackTrace();
        }
        SimpleDateFormat hourTime = new SimpleDateFormat("M/d", Locale.ENGLISH);
        String localTimeStart = hourTime.format(dateStart);
        String localTimeEnd = hourTime.format(dateEnd);
        if(dateEnd.compareTo(dateCurr)>0)localTimeEnd = "In Progress";
        String startEnd = localTimeStart + " - " + localTimeEnd;

        getActivity().setTitle("Hatch: " + startEnd);

        //String title_date = peep.getName();
        //mSensorTitleDate.setText(title_date);


        SettingsManager.TemperatureUnits units = mSettingsManager.getTemperatureUnits();
        //float offset = peep.getLastHatch().getTemperatureOffsetCelsius();
        float offset = mPeepHatch.getTemperatureOffsetCelsius();
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
                //SimpleDateFormat userTime = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ENGLISH);
                Date parse_date = null;
                userTime.setTimeZone(timezoneID);

                try{
                    parse_date = userTime.parse(strDate);
                    //Log.i("parse_date",parse_date.toString());
                } catch (Exception e) {
                    e.printStackTrace();
                }

                TimeZone tz = TimeZone.getDefault();
                //System.out.println("TimeZone   "+tz.getDisplayName(false, TimeZone.SHORT)+" Timezon id :: " +tz.getID());

               hourTime = new SimpleDateFormat("M/d", Locale.ENGLISH);
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
            checkDisableTabs();
        }
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
        //mSensorTitleDate.setText("---");
    }

    private void NATextViews(){
        clearTable();
        mTextViewTemperature.setText("N/A");
        mTextViewHumidity.setText("N/A");
        mTextViewMaximumTemperature.setText("N/A");
        mTextViewMaximumHumidity.setText("N/A");
        mTextViewMinimumTemperature.setText("N/A");
        mTextViewMinimumwHumidity.setText("N/A");
        mTextViewSDTemperature.setText("N/A");
        mTextViewSDHumidity.setText("N/A");
        getActivity().setTitle("No data ");
        //mSensorTitleDate.setText("N/A");
        //getActivity().setTitle("Hatch: N/A");
    }

    private class GetSensorHistoryData extends AsyncTask<PeepHatch, Void, PeepHatch> {
        @Override
        protected PeepHatch doInBackground(PeepHatch... hatches) {


            PeepHatch current_hatch = new PeepHatch();

            if(current_hatch_uuid.equals("") && peep!=null)current_hatch_uuid = peep.getLastHatch().getUUID();

            Log.i("accessToken:","accessToken ");

            SharedPreferences preferences = getContext().getSharedPreferences("UserData", getContext().getApplicationContext().MODE_PRIVATE);
            String email = preferences.getString("email", null);
            String password = preferences.getString("password", null);

            String accessToken = RestApi.postUserAuth(
                    email,
                    password);

            //Log.i("TIMELINE BG:","accessToken: "+peep.getLastHatch().getUUID());

            JSONArray timelineJSON = RestApi.getPeepTimeline(
                    accessToken,
                    peep,current_tab_index,current_hatch_uuid);

            json = timelineJSON;
            if(timelineJSON!=null)Log.i("JSON - TABLE ",timelineJSON.toString());

            //peep.setMeasurement(peepMeasurement);

            //hatchUUIDList = RestApi.getHatchUUIDs(accessToken, peep);
            //Collections.reverse(hatchUUIDList);

            Log.i("current_hatch_uuid",current_hatch_uuid);
            current_hatch = RestApi.getHatch(accessToken, current_hatch_uuid);

            return current_hatch;
        }

        @Override
        protected void onPostExecute(PeepHatch current_hatch) {

            Log.i("onPostExecute","onPostExecute");
            try {
                //json = timelineJSON;
                mPeepHatch = current_hatch;
                if(mPeepHatch != null) {

                    LinearLayout layout = (LinearLayout) getView().findViewById(R.id.tableSensorDataHeader);
                    LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) layout.getLayoutParams();

                    ScrollView sv = (ScrollView)getView().findViewById(R.id.SensorScrollView);
                    RelativeLayout.LayoutParams scrollLayoutParams = (RelativeLayout.LayoutParams) sv.getLayoutParams();

                    if ((long) mPeepHatch.getEndUnixTimestamp() < (long) System.currentTimeMillis() / 1000) {
                        mTabLayoutSensorData.setVisibility(View.GONE);
                        current_tab_index = 999;
                        mTextViewTitleAverage.setText("Hatch Average");
                        mTextViewTitleMax.setText("Hatch Maxmium");
                        mTextViewTitleMin.setText("Hatch Minimum");
                        mTextViewTitleSD.setText("Avg. Deviation");
                        TabLayout.Tab tab = mTabLayoutSensorData.getTabAt(3);
                        tab.select();
                        layoutParams.topMargin = 100;
                        scrollLayoutParams.topMargin = 100;

                    }else{
                        mTabLayoutSensorData.setVisibility(View.VISIBLE);
                        layoutParams.topMargin = 156;
                        scrollLayoutParams.topMargin = 156;
                    }

                    sv.setLayoutParams(scrollLayoutParams);
                    layout.setLayoutParams(layoutParams);

                    // Set Hatch Name in Header
                    String mHatchName = mPeepHatch.getHatchName();
                    if(mHatchName.equals("null"))mHatchName = "Untitled Hatch";
                    mTextViewTitleHeader.setText(mHatchName);

                    // Set Egg count + Species Type
                    String speciesName = "Chicken";
                    if(mPeepHatch.getSpeciesUUID().equals("c0999080-7749-4c9b-ada1-947ec383a845"))speciesName = "Duck";
                    mTextViewEggCountSpeciesHeader.setText(mPeepHatch.getEggCount()+" "+speciesName+" Eggs");

                    // Set Peep Name and hatch notes
                    String mHatchNotes = new String();
                    if(!mPeepHatch.getHatchNotes().equals("null"))mHatchNotes = mPeepHatch.getHatchNotes()+"\n ";
                    String peepNotes = new String();
                    if(peep!=null)peepNotes = "Peep: "+peep.getName()+"\n\n";
                    mTextViewPeepNameHeader.setText(peepNotes + mHatchNotes);

                    // Set Hatched or in progress
                    float mPercentHatched;
                    int int_egg_count = mPeepHatch.getEggCount();
                    int int_hatch_count = mPeepHatch.getHatchedCount();

                    Log.i("int_hatch_count",String.valueOf(int_hatch_count));
                    Log.i("int_egg_count",String.valueOf(int_egg_count));

                    String hatchString = "";
                    if(int_egg_count + int_hatch_count > 0){
                        Log.i("int_egg_count","int_egg_count + int_hatch_count > 0");
                        mPercentHatched = (float)int_hatch_count/(float)int_egg_count;
                        mPercentHatched = mPercentHatched * 100;
                        hatchString = int_hatch_count + " Hatched ("+ Math.round(mPercentHatched) + "%)";
                        mTrHatchedCount.setVisibility(View.VISIBLE);
                    }
                    if(int_hatch_count == 0){
                        hatchString = "";
                        mTrHatchedCount.setVisibility(View.GONE);
                    }

                    mTextViewTimeframeHeader.setText(hatchString);


                }
                if(json!= null) {
                    /*
                    Log.i("onPostExecute","onPostExecute HAS DATA");
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
                    */
                    reloadSensorDataTable();
                }else{
                    if(current_tab_index != 999) {
                        NATextViews();
                    }
                }

            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    public void buildHatchHistoryList(JSONObject peepHatchAllData){
        /*
        mHatchStartEndArray = new String[hatchUUIDList.size()];
        JSONArray json = new JSONArray();
        try {
            json = peepHatchAllData.getJSONArray("hatches_all_data");
            //Log.i("json", json.toString());
        }catch(Exception e){
            e.printStackTrace();
        }
        for(int i=0;i<json.length();i++){

            JSONObject current = new JSONObject();
            Date dateStart = new java.util.Date();
            Date dateEnd = new java.util.Date();
            Date dateCurr = new java.util.Date();
            try{
                current = json.getJSONObject(i);
                Log.i("current",current.toString());
                dateStart = new java.util.Date(current.getInt("start_unix_timestamp")*1000L);
                dateEnd = new java.util.Date(current.getInt("end_unix_timestamp")*1000L);
            }catch(Exception e){
                e.printStackTrace();
            }
            SimpleDateFormat hourTime = new SimpleDateFormat("M/d/Y", Locale.ENGLISH);
            String localTimeStart = hourTime.format(dateStart);
            String localTimeEnd = hourTime.format(dateEnd);
            if(dateEnd.compareTo(dateCurr)>0)localTimeEnd = "In Progress";
            String startEnd = localTimeStart + " - " + localTimeEnd;
            mHatchStartEndArray[i] = startEnd;
        }

        //first create a list from String array
        List<String> list = Arrays.asList(mHatchStartEndArray);

        //next, reverse the list using Collections.reverse method
        Collections.reverse(list);

        //next, convert the list back to String array

        final String[] action = (String[])list.toArray();
        //peepHatches

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Hatches");
        builder.setItems(action, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.i("BUILDER", String.valueOf(which));

                current_hatch_uuid = hatchUUIDList.get(which);
                //if(String.valueOf(which).equals("0"))current_hatch_uuid = "";
                Log.i("current_hatch_uuid",current_hatch_uuid);
                resetAvgTextViews();
                //startRepeatingTask();
                //checkDisableTabs();
            }
        });
        builder.show();
        */
    }

    public void checkDisableTabs(){
        /*
        TabLayout tabLayout = getView().findViewById(R.id.sensor_data_tabs);

        if(current_hatch_uuid.equals("")){
            enableTabs();
            TabLayout.Tab tab = tabLayout.getTabAt(0);
            tab.select();
        }else{
            TabLayout.Tab tab = tabLayout.getTabAt(3);
            tab.select();
            disabledTabs();
        }
        */
    }

    public void getHatchHistoryData(){
        mHatchUUIDArray = new String[hatchUUIDList.size()];
        mHatchUUIDArray = hatchUUIDList.toArray(mHatchUUIDArray);
        getHatchDataEnum();
    }

    public void getData(){

        Log.i("TIMELINE Runnable:","mHandlerTask");

        PeepSensorDataFragment.GetSensorHistoryData GetSensorHistoryData = new PeepSensorDataFragment.GetSensorHistoryData();
        GetSensorHistoryData.execute();

    }

    public void disabledTabs(){
        //TabLayout tabLayout = getView().findViewById(R.id.mTabLayoutSensorData);

        LinearLayout tabStrip = ((LinearLayout)mTabLayoutSensorData.getChildAt(0));
        tabStrip.setEnabled(false);
        for(int i = 0; i < tabStrip.getChildCount(); i++) {
            if(i<3) {
                tabStrip.getChildAt(i).setClickable(false);
            }
        }
    }

    public void enableTabs(){
        //TabLayout tabLayout = getView().findViewById(R.id.sensor_data_tabs);
        LinearLayout tabStrip = ((LinearLayout)mTabLayoutSensorData.getChildAt(0));
        tabStrip.setEnabled(true);
        for(int i = 0; i < tabStrip.getChildCount(); i++) {
            tabStrip.getChildAt(i).setClickable(true);
        }
    }

    public void getHatchDataEnum(){
        Log.i("HATCH_DATA", "getHatchDataEnum");
        PeepSensorDataFragment.enumFunction job = new PeepSensorDataFragment.enumFunction();
        job.execute();
    }
    private class enumFunction extends AsyncTask<PeepHatch, Void, JSONObject > {

        JSONObject peepHatchAllData = new JSONObject();
        @Override
        protected JSONObject doInBackground(PeepHatch... pairs) {
            Log.i("HATCH_DATA", "doInBackground");
            String accessToken = RestApi.postUserAuth( peep.getUserEmail(), peep.getUserPassword());
            peepHatchAllData = RestApi.getAllHatchData(accessToken, peep.getUUID());
            //Log.i("HATCH_DATA", peepHatchAllData.toString());
            return peepHatchAllData;
        }

        @Override
        protected void onPostExecute(JSONObject returnedData) {
            buildHatchHistoryList(returnedData);
        }
    }

}
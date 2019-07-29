package com.example.hatchtracksensor;

import android.app.FragmentTransaction;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Fragment;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.util.Log;
import android.view.View.OnClickListener;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class SettingsFragment extends Fragment {

    private Spinner mSpinnerTemperature;
    private TextView mTextViewAccountEmail;
    private TextView mTextViewVersion;

    private PeepUnitManager mPeepUnitManager;

    private AccountManager mAccountManager;
    private SettingsManager mSettingsManager;

    private Switch mSwitchTempTooHot; //DBOI
    private Switch mSwitchTempTooCold; //DBOI
    private Switch mSwitchHumidityUnder; //DBOI
    private Switch mSwitchHumidityOver; //DBOI

    private Boolean onoff_SwitchTempTooHot;
    private Boolean onoff_SwitchTempTooCold;
    private Boolean onoff_SwitchHumidityUnder;
    private Boolean onoff_SwitchHumidityOver;

    // Poll the database for new data at the provided time interval.
    private final static int mDbPollInterval = 1000 * 60 * 1; // 1 minute

    private Handler mHandler = new Handler();
    private Runnable mHandlerTask = new Runnable() {
        @Override
        public void run() {
            mPeepUnitManager = new PeepUnitManager();
            PeepUnit peep = mPeepUnitManager.getPeepUnitActive();
            Log.i("TIMELINE Runnable:","mHandlerTask");

            SettingsFragment.SensorUpdateJob sensorUpdateJob = new SettingsFragment.SensorUpdateJob();
            sensorUpdateJob.execute(peep);

            mHandler.postDelayed(mHandlerTask, mDbPollInterval);
        }
    };


    public SettingsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_settings, container, false);

        //Switch mSwitchTempTooHot = (Switch) v.findViewById(R.id.SwitchTempTooHot);
        //Switch mSwitchTempTooHot = getView().findViewById(R.id.spinnerTemperature);
        //mSwitchTempTooHot.setOnClickListener(this);



        mSwitchTempTooHot = v.findViewById(R.id.SwitchTempTooHot);
        mSwitchTempTooHot.setOnClickListener(
                new Switch.OnClickListener() {
                    public void onClick(View v) {
                        startRepeatingTask();
                    }
                }
        );

        mSwitchTempTooCold = v.findViewById(R.id.SwitchTempTooCold);
        mSwitchTempTooCold.setOnClickListener(
                new Switch.OnClickListener() {
                    public void onClick(View v) {
                        startRepeatingTask();
                    }
                }
        );

        mSwitchHumidityUnder = v.findViewById(R.id.SwitchHumidityUnder);
        mSwitchHumidityUnder.setOnClickListener(
                new Switch.OnClickListener() {
                    public void onClick(View v) {
                        startRepeatingTask();
                    }
                }
        );

        mSwitchHumidityOver = v.findViewById(R.id.SwitchHumidityOver);
        mSwitchHumidityOver.setOnClickListener(
                new Switch.OnClickListener() {
                    public void onClick(View v) {
                        startRepeatingTask();
                    }
                }
        );

        // Inflate the layout for this fragment
        //return inflater.inflate(R.layout.fragment_settings, container, false);
        return v;
    }


    public void onCheckedChanged(Switch sw, boolean isChecked) {

        if(isChecked) {
            //do stuff when Switch is ON
        } else {
            //do stuff when Switch if OFF
        }
    }

    @Override
    public void onActivityCreated(Bundle bundle) {
        super.onActivityCreated(bundle);




        mAccountManager = new AccountManager(getContext());
        mSettingsManager = new SettingsManager();
        mTextViewAccountEmail = getView().findViewById(R.id.textViewAccountEmail);
        mTextViewVersion = getView().findViewById(R.id.textViewVersion);

        mTextViewAccountEmail.setText(mAccountManager.getEmail());

        try {
            Context c = getContext();
            PackageInfo info = c.getPackageManager().getPackageInfo(c.getPackageName(), 0);
            String version = info.versionName;
            mTextViewVersion.setText(version);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        mSpinnerTemperature = getView().findViewById(R.id.spinnerTemperature);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                getActivity(),
                R.array.temperature_units,
                R.layout.spinner_layout);

        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(R.layout.spinner_layout_dropdown);
        mSpinnerTemperature.setAdapter(adapter);
        // Apply the adapter to the spinner

        String find;
        String value;
        SettingsManager.TemperatureUnits units = mSettingsManager.getTemperatureUnits();
        find = (SettingsManager.TemperatureUnits.CELSIUS == units) ? "Celsius" : "Fahrenheit";
        for (int i = 0; i < adapter.getCount(); i++) {
            value = mSpinnerTemperature.getItemAtPosition(i).toString();
            if (value.contains(find)) {
                mSpinnerTemperature.setSelection(i);
            }
        }

        mSpinnerTemperature.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                String selected = parent.getItemAtPosition(position).toString();

                if (selected.contains("Celsius")) {
                    mSettingsManager.setTemperatureUnits(
                            SettingsManager.TemperatureUnits.CELSIUS);
                }
                else if (selected.contains("Fahrenheit")) {
                    mSettingsManager.setTemperatureUnits(
                            SettingsManager.TemperatureUnits.FAHRENHEIT);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Nothing to do right now...
            }
        });

        mPeepUnitManager = new PeepUnitManager();
        PeepUnit peep = mPeepUnitManager.getPeepUnitActive();

        SettingsFragment.GetNotificationSettings job = new SettingsFragment.GetNotificationSettings();
        job.execute(peep);

    }



    private void startRepeatingTask() {
        mHandlerTask.run();
    }

    private void stopRepeatingTask() {
        mHandler.removeCallbacks(mHandlerTask);
    }

    private class SensorUpdateJob extends AsyncTask<PeepUnit, Void, PeepUnit> {
        @Override
        protected PeepUnit doInBackground(PeepUnit... peeps) {

            PeepUnit peep = peeps[0];

            String accessToken = RestApi.postUserAuth(mAccountManager.getEmail(), mAccountManager.getPassword());
            RestApi.postToggleSwitch(accessToken,mSwitchTempTooHot.isChecked(),mSwitchTempTooCold.isChecked(),mSwitchHumidityOver.isChecked(),mSwitchHumidityUnder.isChecked());



            return peep;
        }

        @Override
        protected void onPostExecute(PeepUnit peep) {

            try {
                stopRepeatingTask();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private class GetNotificationSettings extends AsyncTask<PeepUnit, Void, PeepUnit[]> {

        @Override
        protected PeepUnit[] doInBackground(PeepUnit... peepUnits) {

            String accessToken = RestApi.postUserAuth(mAccountManager.getEmail(), mAccountManager.getPassword());

            JSONObject push_notification_settings = RestApi.PushNotificationSettings(accessToken);

            try{
                onoff_SwitchTempTooHot = push_notification_settings.getBoolean("SwitchTempTooHotState");
                onoff_SwitchTempTooCold = push_notification_settings.getBoolean("SwitchTempTooColdState");
                onoff_SwitchHumidityUnder = push_notification_settings.getBoolean("SwitchHumidityUnderState");
                onoff_SwitchHumidityOver = push_notification_settings.getBoolean("SwitchHumidityOverState");
            } catch (Exception e) {
                e.printStackTrace();
            }

            return peepUnits;
        }

        @Override
        protected void onPostExecute(PeepUnit[] peepUnits) {
            try {
                //json = timelineJSON;

                if(onoff_SwitchTempTooHot == true)mSwitchTempTooHot.setChecked(true);
                if(onoff_SwitchTempTooCold == true)mSwitchTempTooCold.setChecked(true);
                if(onoff_SwitchHumidityOver == true)mSwitchHumidityOver.setChecked(true);
                if(onoff_SwitchHumidityUnder == true)mSwitchHumidityUnder.setChecked(true);

                stopRepeatingTask();

            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
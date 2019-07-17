package com.example.hatchtracksensor;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.app.Fragment;
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

public class SettingsFragment extends Fragment {

    private Spinner mSpinnerTemperature;
    private TextView mTextViewAccountEmail;
    private TextView mTextViewVersion;

    private AccountManager mAccountManager;
    private SettingsManager mSettingsManager;

    private Switch mSwitchTempTooHot; //DBOI
    private Switch mSwitchTempTooCold; //DBOI
    private Switch mSwitchHumidityUnder; //DBOI
    private Switch mSwitchHumidityOver; //DBOI

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
                        String accessToken = RestApi.postUserAuth(mAccountManager.getEmail(), mAccountManager.getPassword());
                        if(mSwitchTempTooHot.isChecked()) {
                            Log.v("NOTIFICATIONS_SWITCH", "ON - SwitchTempTooHot");
                        }else{
                            Log.v("NOTIFICATIONS_SWITCH", "OFF - SwitchTempTooHot");
                        }
                        //API Call to update JSON obj to Postgres
                        // format: {"SwitchTempTooHot":1,"SwitchTempTooCold":1,"SwitchHumidityUnder":0,"SwitchHumidityOver":0}
                        //RestApi.postToggleSwitch(accessToken,mSwitchTempTooHot.isChecked(),mSwitchTempTooCold.isChecked(),mSwitchHumidityOver.isChecked(),mSwitchHumidityUnder.isChecked());
                        //END API Call
                    }
                }
        );

        mSwitchTempTooCold = v.findViewById(R.id.SwitchTempTooCold);
        mSwitchTempTooCold.setOnClickListener(
                new Switch.OnClickListener() {
                    public void onClick(View v) {
                        if(mSwitchTempTooCold.isChecked()) {
                            Log.v("NOTIFICATIONS_SWITCH", "ON - SwitchTempTooCold");
                        }else{
                            Log.v("NOTIFICATIONS_SWITCH", "OFF - SwitchTempTooCold");
                        }
                    }
                }
        );

        mSwitchHumidityUnder = v.findViewById(R.id.SwitchHumidityUnder);
        mSwitchHumidityUnder.setOnClickListener(
                new Switch.OnClickListener() {
                    public void onClick(View v) {
                        if(mSwitchHumidityUnder.isChecked()) {
                            Log.v("NOTIFICATIONS_SWITCH", "ON - SwitchHumidityUnder");
                        }else{
                            Log.v("NOTIFICATIONS_SWITCH", "OFF - SwitchHumidityUnder");
                        }
                    }
                }
        );

        mSwitchHumidityOver = v.findViewById(R.id.SwitchHumidityOver);
        mSwitchHumidityOver.setOnClickListener(
                new Switch.OnClickListener() {
                    public void onClick(View v) {
                        if(mSwitchHumidityOver.isChecked()) {
                            Log.v("NOTIFICATIONS_SWITCH", "ON - SwitchHumidityOver");
                        }else{
                            Log.v("NOTIFICATIONS_SWITCH", "OFF - SwitchHumidityOver");
                        }
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
    }
//    @Override
    public void onToggleSwitchTempTooHot(View v){
        //Log.v("Switch State=");
    }

}
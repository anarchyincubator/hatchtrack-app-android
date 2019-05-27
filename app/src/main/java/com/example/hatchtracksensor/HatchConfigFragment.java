package com.example.hatchtracksensor;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.TextView;

public class HatchConfigFragment extends Fragment {

    private Button mButton;
    private ProgressBar mSpinner;
    private TextView mEditTextPeepName;
    private TextView mEditTextMeasureIntervalMin;
    private TextView mEditTextMeasureTempOffset;
    private RadioButton mRadioButtonMinutes;
    private RadioButton mRadioButtonHours;
    private RadioButton mRadioButtonFahrenheit;
    private RadioButton mRadioButtonCelsius;

    private PeepUnitManager mPeepUnitManager;
    private AccountManager mAccountManager;
    private DbSyncJob mJob;

    private String mPeepName;
    private int mMeasureIntervalMin;
    private float mTemperatureOffset;

    public HatchConfigFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_hatch_config, container, false);
    }

    @Override
    public void onActivityCreated(Bundle bundle) {
        super.onActivityCreated(bundle);

        Activity activity = getActivity();
        Context context = getContext();

        mAccountManager = new AccountManager(context);
        mPeepUnitManager = new PeepUnitManager();

        SettingsManager settingsManager= new SettingsManager();
        final SettingsManager.TemperatureUnits CELSIUS =
                SettingsManager.TemperatureUnits.CELSIUS;
        final SettingsManager.TemperatureUnits FAHRENHEIT =
                SettingsManager.TemperatureUnits.FAHRENHEIT;

        mSpinner = activity.findViewById(R.id.progressBarHatchConfig);
        mSpinner.setVisibility(View.GONE);
        mEditTextPeepName = activity.findViewById(R.id.editTextHatchConfigName);
        mEditTextMeasureIntervalMin = activity.findViewById(R.id.editTextHatchConfigInterval);
        mEditTextMeasureTempOffset = activity.findViewById(R.id.editTextHatchConigTempOffset);
        mRadioButtonHours = activity.findViewById(R.id.radioButtonMeasureHours);
        mRadioButtonMinutes = activity.findViewById(R.id.radioButtonMeasureMinutes);
        mRadioButtonFahrenheit = activity.findViewById(R.id.radioButtonTemperatureFahrenheit);
        mRadioButtonCelsius = activity.findViewById(R.id.radioButtonTemperatureCelsius);

        mRadioButtonMinutes.setChecked(true);
        SettingsManager.TemperatureUnits units = settingsManager.getTemperatureUnits();
        if (CELSIUS == units) {
            mRadioButtonCelsius.setChecked(true);
        }
        else {
            mRadioButtonFahrenheit.setChecked(true);
        }

        mButton = activity.findViewById(R.id.buttonHatchConfigure);
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean isCelsius = (mRadioButtonCelsius.isChecked()) ? true : false;
                final boolean isHours = (mRadioButtonHours.isChecked()) ? true : false;

                mPeepName = mEditTextPeepName.getText().toString();

                try {
                    mMeasureIntervalMin = Integer.parseInt(
                            mEditTextMeasureIntervalMin.getText().toString());
                    if (true == isHours) {
                        mMeasureIntervalMin  *= 60;
                    }
                } catch (Exception e) {
                    mMeasureIntervalMin  = 15;
                }

                try {
                    mTemperatureOffset = Float.parseFloat(
                            mEditTextMeasureTempOffset.getText().toString());
                    if (false == isCelsius) {
                        mTemperatureOffset = mTemperatureOffset * 0.5556f;
                    }
                } catch (Exception e) {
                    mTemperatureOffset = 0;
                }

                mButton.setEnabled(false);
                mEditTextPeepName.setEnabled(false);
                mEditTextMeasureIntervalMin.setEnabled(false);
                mEditTextMeasureTempOffset.setEnabled(false);
                mRadioButtonHours.setEnabled(false);
                mRadioButtonMinutes.setEnabled(false);
                mRadioButtonCelsius.setEnabled(false);
                mRadioButtonFahrenheit.setEnabled(false);
                mSpinner.setVisibility(View.VISIBLE);

                PeepUnit peepUnit = mPeepUnitManager.getPeepUnit(0);
                peepUnit.setName(mPeepName);
                mJob = new DbSyncJob();
                mJob.execute(peepUnit);
            }
        });


    }

    private class DbSyncJob extends AsyncTask<PeepUnit, Void, PeepUnit[]> {

        @Override
        protected PeepUnit[] doInBackground(PeepUnit... peepUnits) {
            String email = peepUnits[0].getUserEmail();
            String password = peepUnits[0].getUserPassword();
            PeepUnit peepUnit = peepUnits[0];

            PeepHatch peepHatch = new PeepHatch();
            peepHatch.setTemperatureOffsetCelsius(0);
            peepHatch.setMeasureIntervalMin(mMeasureIntervalMin);
            peepHatch.setTemperatureOffsetCelsius(mTemperatureOffset);
            peepHatch.setEndUnixTimestamp(2147483647);

            String accessToken = RestApi.postUserAuth(email, password);
            RestApi.postUserNewPeep(accessToken, peepUnit);
            RestApi.postPeepName(accessToken, peepUnit);
            RestApi.postNewPeepHatch(accessToken, peepUnit, peepHatch);

            return peepUnits;
        }

        @Override
        protected void onPostExecute(PeepUnit[] peepUnits) {
            Log.i("MREUTMAN", "HatchConfigFragment DONE!");
            //mPeepUnitManager.setPeepUnitActive(mPeepUnitIndex);
            Fragment fragment = new PeepDatabaseSyncFragment();
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.replace(R.id.content_view, fragment);
            ft.addToBackStack(null);
            ft.commit();
        }
    }

}

package com.example.hatchtracksensor;

import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Fragment;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class SensorFragment extends Fragment {

    private final SettingsManager.TemperatureUnits CELSIUS =
            SettingsManager.TemperatureUnits.CELSIUS;
    private final SettingsManager.TemperatureUnits FAHRENHEIT =
            SettingsManager.TemperatureUnits.FAHRENHEIT;

    private TextView mTextViewTimeUpdate;
    private TextView mTextViewTemperature;
    private TextView mTextViewHumidity;
    private TextView mTextViewTemperatureOffset;
    private TextView mTextViewMeasurementInterval;
    private Button mButtonPeepSelect;

    private PeepUnitManager mPeepUnitManager;
    private SettingsManager mSettingsManager;

    // Poll the database for new data at the provided time interval.
    private final static int mDbPollInterval = 1000 * 60 * 1; // 1 minute
    float mTemperatureOffsetCelcius = 0.0f;

    private Handler mHandler = new Handler();
    private Runnable mHandlerTask = new Runnable() {
        @Override
        public void run() {
            PeepUnit peep = mPeepUnitManager.getPeepUnitActive();

            mButtonPeepSelect.setText(peep.getName());
            SensorUpdateJob sensorUpdateJob = new SensorUpdateJob();
            sensorUpdateJob.execute(peep);

            mHandler.postDelayed(mHandlerTask, mDbPollInterval);
        }
    };

    public SensorFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_sensor, container, false);
    }

    @Override
    public void onActivityCreated(Bundle bundle) {
        super.onActivityCreated(bundle);

        mPeepUnitManager = new PeepUnitManager();
        mSettingsManager = new SettingsManager();
        PeepUnit peep = mPeepUnitManager.getPeepUnitActive();

        mTextViewTimeUpdate = getView().findViewById(R.id.textViewTimeUpdate);
        mTextViewTemperature = getView().findViewById(R.id.textViewTemperature);
        mTextViewHumidity = getView().findViewById(R.id.textViewHumidity);
        mTextViewMeasurementInterval = getView().findViewById(R.id.textViewMeasurementInterval);
        mTextViewTemperatureOffset = getView().findViewById(R.id.textViewTemperatureOffset);
        mButtonPeepSelect = getView().findViewById(R.id.buttonPeepSelect);

        SettingsManager.TemperatureUnits units = mSettingsManager.getTemperatureUnits();
        float offset = peep.getLastHatch().getTemperatureOffsetCelsius();
        mTemperatureOffsetCelcius = offset;
        if (CELSIUS == units) {
            String s = "Temperature Offset: " + String.format("%.2f", offset) + " ℃";
            mTextViewTemperatureOffset.setText(s);
        }
        else if (FAHRENHEIT == units) {
            offset *= 1.8;
            String s = "Temperature Offset: " + String.format("%.2f", offset) + " ℉";
            mTextViewTemperatureOffset.setText(s);
        }

        int interval = peep.getLastHatch().getMeasureIntervalMin();
        if (0 == (interval % 60)) {
            interval /= 60;
            String s = "Measurement Interval: " + interval + " hours";
            mTextViewMeasurementInterval.setText(s);
        }
        else {
            String s = "Measurement Interval: " + interval + " minutes";
            mTextViewMeasurementInterval.setText(s);
        }

        mButtonPeepSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopRepeatingTask();

                Fragment fragment = new PeepSelectFragment();
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.replace(R.id.content_view, fragment);
                ft.addToBackStack(null);
                ft.commit();
            }
        });

        startRepeatingTask();
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

    private class SensorUpdateJob extends AsyncTask<PeepUnit, Void, PeepMeasurement> {

        @Override
        protected PeepMeasurement doInBackground(PeepUnit... peeps) {
            PeepUnit peepUnit = peeps[0];

            String accessToken = RestApi.postUserAuth(
                    peepUnit.getUserEmail(),
                    peepUnit.getUserPassword());

            PeepMeasurement peepMeasurement = RestApi.getPeepLastMeasure(
                    accessToken,
                    peepUnit);

            return peepMeasurement;
        }

        @Override
        protected void onPostExecute(PeepMeasurement peepMeasurement) {
            try {
                String fmt = "";
                SettingsManager.TemperatureUnits units = mSettingsManager.getTemperatureUnits();

                // Display Temperature value in the user specified units.
                float t = peepMeasurement.getTemperature() + mTemperatureOffsetCelcius;
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
                        peepMeasurement.getmHumidity() * 1.0) + " %";
                mTextViewHumidity.setText(fmt);

                // User readable time representation.
                DateFormat userTime = new SimpleDateFormat(
                        "MMM dd, yyyy HH:mm a",
                        Locale.ENGLISH);

                userTime.setTimeZone(TimeZone.getDefault());
                Date date = new Date(peepMeasurement.getUnixTimestamp() * 1000);
                String localTime = userTime.format(date);
                String text = "Last Updated: " + localTime;
                mTextViewTimeUpdate.setText(text);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
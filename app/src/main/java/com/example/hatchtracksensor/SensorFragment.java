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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class SensorFragment extends Fragment {

    public static final String PEEP_MANAGER_KEY = "PEEP_MANAGER";

    private TextView mTextViewTimeUpdate;
    private TextView mTextViewTemperature;
    private TextView mTextViewHumidity;
    private Button mButtonPeepSelect;

    private PeepManager mPeepManager;

    private static final String mDbURL = "https://db.hatchtrack.com:8086";
    private static final String mDbUser = "reader";
    private static final String mDbPassword = "B5FX6jIhXz0kbBxE";
    private static final String mDbName = "peep0";
    private final static int mDbPollInterval = 1000 * 60 * 1; // 1 minute
    private InfluxClient mInfluxClient;

    private Handler mHandler = new Handler();
    private Runnable mHandlerTask = new Runnable() {
        @Override
        public void run() {
            PeepManager.PeepUnit peep = mPeepManager.getPeepUnitActive();

            mButtonPeepSelect.setText(peep.getName());
            SensorUpdateJob sensorUpdateJob = new SensorUpdateJob();
            sensorUpdateJob.execute(peep.getUUID());

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

        mPeepManager = new PeepManager();
        PeepManager.PeepUnit peep = mPeepManager.getPeepUnitActive();

        mTextViewTimeUpdate = getView().findViewById(R.id.textViewTimeUpdate);
        mTextViewTemperature = getView().findViewById(R.id.textViewTemperature);
        mTextViewHumidity = getView().findViewById(R.id.textViewHumidity);
        mButtonPeepSelect = getView().findViewById(R.id.buttonPeepSelect);
        mButtonPeepSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Fragment fragment = new PeepSelectFragment();
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.replace(R.id.content_view, fragment);
                ft.commit();
            }
        });

        mInfluxClient = new InfluxClient(mDbURL, mDbUser, mDbPassword, mDbName);
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

    private class SensorUpdateJob extends AsyncTask<String, Void, InfluxClient.InfluxMeasurement> {

        @Override
        protected InfluxClient.InfluxMeasurement doInBackground(String... uuids) {
            InfluxClient.InfluxMeasurement influxMeasurement = null;

            for (int i = 0; i < uuids.length; i++) {
                influxMeasurement = mInfluxClient.getMeasurement(uuids[i]);
            }

            return influxMeasurement;
        }

        @Override
        protected void onPostExecute(InfluxClient.InfluxMeasurement influxMeasurement) {
            if (null != influxMeasurement) {
                try {
                    String fmt;

                    fmt = String.format(
                            Locale.US,
                            "%.1f", influxMeasurement.temperature) + " ℃";
                    mTextViewTemperature.setText(fmt);

                    fmt = String.format(
                            Locale.US, "%.1f",
                            influxMeasurement.humidity) + " %";
                    mTextViewHumidity.setText(fmt);

                    DateFormat isoTimestamp = new SimpleDateFormat(
                            "yyyy-MM-dd'T'HH:mm:ss'Z'",
                            Locale.ENGLISH);
                    DateFormat userTimestamp = new SimpleDateFormat(
                            "MMM dd, yyyy HH:mm a",
                            Locale.ENGLISH);

                    isoTimestamp.setTimeZone(TimeZone.getTimeZone("UTC"));
                    userTimestamp.setTimeZone(TimeZone.getDefault());
                    Date date = isoTimestamp.parse(influxMeasurement.timestamp);
                    String localTime = userTimestamp.format(date);

                    mTextViewTimeUpdate.setText("Last Updated: " + localTime);
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
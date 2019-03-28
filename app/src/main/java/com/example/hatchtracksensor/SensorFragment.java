package com.example.hatchtracksensor;

import android.content.Intent;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class SensorFragment extends Fragment {

    final String databaseURL = "https://db.hatchtrack.com:8086";

    public SensorFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_sensor, container, false);


    }

    private void getSensorData() {
        try {
            String u = databaseURL;
            URL url = new URL(u);
            HttpURLConnection urlConnection  = (HttpURLConnection) url.openConnection();
            InputStream in = new BufferedInputStream(urlConnection.getInputStream());
            readStream(in);
            urlConnection.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void readStream(InputStream in) {
    }

    @Override
    public void onActivityCreated(Bundle bundle) {
        super.onActivityCreated(bundle);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Do something?
    }
}
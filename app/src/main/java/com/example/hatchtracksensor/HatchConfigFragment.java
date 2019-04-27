package com.example.hatchtracksensor;

import android.app.Activity;
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
import android.widget.TextView;

import com.amazonaws.util.IOUtils;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class HatchConfigFragment extends Fragment {

    private Button mButton;
    private ProgressBar mSpinner;
    private TextView mEditTextPeepName;
    private TextView mEditTextMeasureIntervalMin;

    private PeepUnitManager mPeepUnitManager;
    private AccountManager mAccountManager;
    private DbSyncJob mJob;

    private String mPeepName;
    private int mMeasureIntervalMin;

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

        mSpinner = activity.findViewById(R.id.progressBarHatchConfig);
        mSpinner.setVisibility(View.GONE);
        mEditTextPeepName = activity.findViewById(R.id.editTextHatchConfigName);
        mEditTextMeasureIntervalMin = activity.findViewById(R.id.editTextHatchConfigInterval);

        mButton = activity.findViewById(R.id.buttonHatchConfigure);
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mPeepName = mEditTextPeepName.getText().toString();
                mMeasureIntervalMin =
                        Integer.parseInt(mEditTextMeasureIntervalMin.getText().toString());

                mButton.setEnabled(false);
                mEditTextPeepName.setEnabled(false);
                mEditTextMeasureIntervalMin.setEnabled(false);
                mSpinner.setVisibility(View.VISIBLE);

                PeepUnit peepUnit = mPeepUnitManager.getPeepUnit(0);
                mJob = new DbSyncJob();
                mJob.execute(peepUnit);
            }
        });


    }

    private class DbSyncJob extends AsyncTask<PeepUnit, Void, PeepUnit[]> {
        private final String HEADER_ACCESS_TOKEN = "access-token";

        protected  String  getAccessToken(String email, String password) {
            String accessToken = "";
            try {
                JSONObject json = new JSONObject();
                json.put("email", email);
                json.put("password", password);

                String body = json.toString();
                String requestURL = "https://db.hatchtrack.com:18888/auth";
                URL url = new URL(requestURL);

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(15000);
                conn.setConnectTimeout(15000);
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.setDoOutput(true);

                OutputStream out = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out, "UTF-8"));
                writer.write(body);

                writer.flush();
                writer.close();
                out.close();

                InputStream in = new BufferedInputStream(conn.getInputStream());
                String data = IOUtils.toString(in);
                json = new JSONObject(data);
                accessToken = json.getString("access-token");
            } catch (Exception e) {
                e.printStackTrace();
            }

            return accessToken;
        }

        protected boolean postNewPeepUnit(PeepUnit peepUnit, String accessToken) {
            boolean status = true;

            try {
                String peepUUID = peepUnit.getUUID();
                String peepName = peepUnit.getName();
                String email = peepUnit.getUserEmail();
                int endUnixTimestamp = 0;
                int measureIntervalMin = mMeasureIntervalMin;

                JSONObject json = new JSONObject();
                json.put("email", email);
                json.put("peepUUID", peepUUID);
                json.put("peepName", peepName);
                json.put("endUnixTimestamp", endUnixTimestamp);
                json.put("measureIntervalMin", measureIntervalMin);
                String body = json.toString();

                String requestURL = "https://db.hatchtrack.com:18888/api/v1/hatch";
                URL url = new URL(requestURL);

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(15000);
                conn.setConnectTimeout(15000);
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty(HEADER_ACCESS_TOKEN, accessToken);
                conn.setDoInput(true);
                conn.setDoOutput(true);

                OutputStream out = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out, "UTF-8"));
                writer.write(body);

                writer.flush();
                writer.close();
                out.close();

                int code = conn.getResponseCode();
                if (200 == code) {
                    status = true;
                } else {
                    status = false;
                }
            } catch (Exception e) {
                e.printStackTrace();
                status = false;
            }

            return status;
        }

        @Override
        protected PeepUnit[] doInBackground(PeepUnit... peepUnits) {
            String email = peepUnits[0].getUserEmail();
            String password = peepUnits[0].getUserPassword();

            String accessToken = getAccessToken(email, password);
            for (int i = 0; i < peepUnits.length; i++) {
                postNewPeepUnit(peepUnits[i], accessToken);
            }

            return peepUnits;
        }

        @Override
        protected void onPostExecute(PeepUnit[] peepUnits) {
            Log.i("MREUTMAN", "HatchConfigFragment DONE!");
        }
    }

}

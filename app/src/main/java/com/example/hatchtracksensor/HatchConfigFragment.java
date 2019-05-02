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

        @Override
        protected PeepUnit[] doInBackground(PeepUnit... peepUnits) {
            String email = peepUnits[0].getUserEmail();
            String password = peepUnits[0].getUserPassword();

            String accessToken = RestApi.postUserAuth(email, password);
            for (int i = 0; i < peepUnits.length; i++) {
                RestApi.postPeepHatchInfo(accessToken, peepUnits[i]);
            }

            return peepUnits;
        }

        @Override
        protected void onPostExecute(PeepUnit[] peepUnits) {
            Log.i("MREUTMAN", "HatchConfigFragment DONE!");
        }
    }

}

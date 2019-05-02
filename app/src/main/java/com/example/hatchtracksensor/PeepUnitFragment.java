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
import android.widget.EditText;
import android.widget.RadioButton;

import static com.amazonaws.mobile.auth.core.internal.util.ThreadUtils.runOnUiThread;

public class PeepUnitFragment extends Fragment {

    private EditText mEditTextPeepName;
    private EditText mEditTextPeepMeasurementInterval;
    private EditText mEditTextPeepTemperatureOffset;
    private RadioButton mRadioButtonMinutes;
    private RadioButton mRadioButtonHours;
    private RadioButton mRadioButtonFahrenheit;
    private RadioButton mRadioButtonCelsius;
    private Button mButtonConfigure;
    private Button mButtonMonitor;

    private PeepUnitManager mPeepUnitManager;
    private PeepUnit mPeepUnit;
    private int mPeepUnitIndex;

    public PeepUnitFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Bundle args = getArguments();

        try {
            mPeepUnitIndex = args.getInt("index");
        } catch (Exception e) {
            mPeepUnitIndex = 0;
        }

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_peep_unit, container, false);
    }

    @Override
    public void onActivityCreated(Bundle bundle) {
        super.onActivityCreated(bundle);

        Activity activity = getActivity();
        Context context = getContext();

        mPeepUnitManager = new PeepUnitManager();
        mPeepUnit = mPeepUnitManager.getPeepUnit(mPeepUnitIndex);

        mButtonConfigure = activity.findViewById(R.id.buttonPeepConfigure);
        mButtonMonitor = activity.findViewById(R.id.buttonPeepMonitor);
        mEditTextPeepName = activity.findViewById(R.id.editTextPeepName);
        mEditTextPeepMeasurementInterval = activity.findViewById(R.id.editTextPeepMeasureInterval);
        mEditTextPeepTemperatureOffset = activity.findViewById(R.id.editTextPeepTemperatureOffset);
        mRadioButtonHours = activity.findViewById(R.id.radioButtonMeasureHours);
        mRadioButtonMinutes = activity.findViewById(R.id.radioButtonMeasureMinutes);
        mRadioButtonFahrenheit = activity.findViewById(R.id.radioButtonTemperatureFahrenheit);
        mRadioButtonCelsius = activity.findViewById(R.id.radioButtonTemperatureCelsius);

        mEditTextPeepName.setText(mPeepUnit.getName());
        int tmp = mPeepUnit.getMeasureIntervalMin();
        if (0 == (tmp % 60)) {
            tmp = tmp / 60;
            mRadioButtonHours.setChecked(true);
        } else {
            mRadioButtonMinutes.setChecked(true);
        }
        mEditTextPeepMeasurementInterval.setText(String.valueOf(tmp));
        mEditTextPeepTemperatureOffset.setText(String.valueOf(mPeepUnit.getTemperatureOffset()));
        if (PeepUnit.PEEP_UNIT_TEMPERATURE_CELSIUS == mPeepUnit.getTemperatureUnits()) {
            mRadioButtonCelsius.setChecked(true);
        } else {
            mRadioButtonFahrenheit.setChecked(true);
        }

        mButtonConfigure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean isCelsius = (mRadioButtonCelsius.isChecked()) ? true : false;
                final boolean isHours = (mRadioButtonHours.isChecked()) ? true : false;

                String name = mEditTextPeepName.getText().toString();
                if (!name.matches("[A-Za-z0-9 ]+")) {
                    Log.e("MREUTMAN", "String not alphanumeric!");
                    return;
                }

                int measureInterval = 0;
                try {
                    measureInterval = Integer.parseInt(
                            mEditTextPeepMeasurementInterval.getText().toString());
                    if (isHours) {
                        measureInterval *= 60;
                    }
                } catch (Exception e) {
                    measureInterval = mPeepUnit.getMeasureIntervalMin();
                }

                // TODO: Throw error/warning if invalid?
                mPeepUnit.setName(name);
                mPeepUnit.setMeasureIntervalMin(measureInterval);

                AsyncTask.execute(new Runnable() {
                    @Override
                    public void run() {
                        String email = mPeepUnit.getUserEmail();
                        String password = mPeepUnit.getUserPassword();

                        UiUpdate(true);
                        String accessToken = RestApi.postUserAuth(email, password);
                        RestApi.postPeepName(accessToken, mPeepUnit);
                        RestApi.postPeepHatchInfo(accessToken, mPeepUnit);
                        UiUpdate(false);
                    }
                });
            }
        });

        mButtonMonitor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mPeepUnitManager.setPeepUnitActive(mPeepUnitIndex);
                Fragment fragment = new SensorFragment();
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.replace(R.id.content_view, fragment);
                ft.addToBackStack(null);
                ft.commit();
            }
        });
    }

    private void UiUpdate(final boolean isDbSync) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (isDbSync) {
                    mButtonMonitor.setEnabled(false);
                    mButtonConfigure.setEnabled(false);
                    mRadioButtonHours.setEnabled(false);
                    mRadioButtonMinutes.setEnabled(false);
                    mRadioButtonCelsius.setEnabled(false);
                    mRadioButtonFahrenheit.setEnabled(false);
                } else {
                    mButtonMonitor.setEnabled(true);
                    mButtonConfigure.setEnabled(true);
                    mRadioButtonHours.setEnabled(true);
                    mRadioButtonMinutes.setEnabled(true);
                    mRadioButtonCelsius.setEnabled(true);
                    mRadioButtonFahrenheit.setEnabled(true);
                }
            }
        });
    }
}

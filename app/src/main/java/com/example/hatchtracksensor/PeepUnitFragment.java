package com.example.hatchtracksensor;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RadioButton;

public class PeepUnitFragment extends Fragment {

    private EditText mEditTextPeepName;
    private EditText mEditTextPeepMeasurementInterval;
    private EditText mEditTextPeepTemperatureOffset;
    private RadioButton mRadioButtonMinutes;
    private RadioButton mRadioButtonHours;
    private RadioButton mRadioButtonFahrenheit;
    private RadioButton mRadioButtonCelsius;

    private PeepUnitManager mPeepUnitManager;
    private PeepUnit mPeepUnit;
    private int mPeepUnitIndex;

    public PeepUnitFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_peep_unit, container, false);
    }

    @Override
    public void onActivityCreated(Bundle bundle) {
        super.onActivityCreated(bundle);

        Activity activity = getActivity();
        Context context = getContext();

        //try {
        //    mPeepUnitIndex = this.getArguments().getInt("index");
        //} catch (Exception e) {
        //    mPeepUnitIndex = 0;
        //}

        mPeepUnitManager = new PeepUnitManager();
        mPeepUnit = mPeepUnitManager.getPeepUnit(mPeepUnitIndex);

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
    }
}

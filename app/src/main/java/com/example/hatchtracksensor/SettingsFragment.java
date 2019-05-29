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
import android.widget.TextView;

public class SettingsFragment extends Fragment {

    private Spinner mSpinnerTemperature;
    private TextView mTextViewAccountEmail;
    private TextView mTextViewVersion;

    private AccountManager mAccountManager;
    private SettingsManager mSettingsManager;

    public SettingsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_settings, container, false);
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
}
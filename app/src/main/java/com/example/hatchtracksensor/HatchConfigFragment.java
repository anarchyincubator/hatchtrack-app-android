package com.example.hatchtracksensor;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.AdapterView;

import java.util.ArrayList;
import java.util.List;

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

    private Spinner mSpinnerPeeps;

    private PeepUnitManager mPeepUnitManager;
    private AccountManager mAccountManager;
    private DbSyncJob mJob;

    private String mPeepName;
    private int mMeasureIntervalMin;
    private float mTemperatureOffset;

    private Spinner mSpinnerSpecies;
    private TextView mEditTextHatchConfigEggCount;
    private List<String> speciesUUIDArray;

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

        getActivity().setTitle("New Hatch");




        // Placeholder species UUID array
        speciesUUIDArray = new ArrayList<String>();
        speciesUUIDArray.add("90df88e3-5ed5-4a1f-a689-97dfc097ebf7"); // Chicken
        speciesUUIDArray.add("c0999080-7749-4c9b-ada1-947ec383a845"); // Duck
        // END Placeholder species UUID array

        mPeepUnitManager = new PeepUnitManager();
        PeepUnit peepUnit = mPeepUnitManager.getPeepUnitActive();

        Activity activity = getActivity();
        Context context = getContext();

        mAccountManager = new AccountManager(context);
        mPeepUnitManager = new PeepUnitManager();

        SettingsManager settingsManager= new SettingsManager();
        final SettingsManager.TemperatureUnits CELSIUS =
                SettingsManager.TemperatureUnits.CELSIUS;
        final SettingsManager.TemperatureUnits FAHRENHEIT =
                SettingsManager.TemperatureUnits.FAHRENHEIT;

        mSpinnerPeeps = activity.findViewById(R.id.spinnerPeeps);

        mSpinner = activity.findViewById(R.id.progressBarHatchConfig);
        mSpinner.setVisibility(View.GONE);

        mEditTextHatchConfigEggCount = activity.findViewById(R.id.editTextHatchConfigEggCount);

        mEditTextPeepName = activity.findViewById(R.id.editTextHatchConfigName);
        //mEditTextMeasureIntervalMin = activity.findViewById(R.id.editTextHatchConfigInterval);
        mEditTextMeasureTempOffset = activity.findViewById(R.id.editTextHatchConigTempOffset);
        //mRadioButtonHours = activity.findViewById(R.id.radioButtonMeasureHours);
        //mRadioButtonMinutes = activity.findViewById(R.id.radioButtonMeasureMinutes);
        mRadioButtonFahrenheit = activity.findViewById(R.id.radioButtonTemperatureFahrenheit);
        mRadioButtonCelsius = activity.findViewById(R.id.radioButtonTemperatureCelsius);

        //mRadioButtonMinutes.setChecked(true);
        SettingsManager.TemperatureUnits units = settingsManager.getTemperatureUnits();
        if (CELSIUS == units) {
            mRadioButtonCelsius.setChecked(true);
        }
        else {
            mRadioButtonFahrenheit.setChecked(true);
        }

        mButton = activity.findViewById(R.id.buttonHatchConfigure);
        mButton.setText("Create");
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean isCelsius = (mRadioButtonCelsius.isChecked()) ? true : false;
                //final boolean isHours = (mRadioButtonHours.isChecked()) ? true : false;

                mPeepName = mEditTextPeepName.getText().toString();

                /*
                try {
                    mMeasureIntervalMin = Integer.parseInt(
                            mEditTextMeasureIntervalMin.getText().toString());
                    if (true == isHours) {
                        mMeasureIntervalMin  *= 60;
                    }
                } catch (Exception e) {
                    mMeasureIntervalMin  = 15;
                }
                */
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
                //mEditTextMeasureIntervalMin.setEnabled(false);
                mEditTextMeasureTempOffset.setEnabled(false);
                //mRadioButtonHours.setEnabled(false);
                //mRadioButtonMinutes.setEnabled(false);
                mRadioButtonCelsius.setEnabled(false);
                mRadioButtonFahrenheit.setEnabled(false);
                mSpinner.setVisibility(View.VISIBLE);

                //PeepUnit peepUnit = mPeepUnitManager.getPeepUnit(0);
                PeepUnit peepUnit = mPeepUnitManager.getPeepUnitActive();
                peepUnit.setName(mPeepName);
                mJob = new DbSyncJob();
                mJob.execute(peepUnit);
            }
        });

        mSpinnerSpecies = getView().findViewById(R.id.spinnerSpecies);
        // Spinner Species
        List<String> speciesArray = new ArrayList<String>();
        speciesArray.add("Chicken");
        speciesArray.add("Duck");

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(getActivity(), R.layout.spinner_layout, speciesArray);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinnerSpecies.setAdapter(dataAdapter);

        //String speciesUUID = peepUnit.getLastHatch().getSpeciesUUID();
        int selected_species_position = mSpinnerSpecies.getSelectedItemPosition();
        String mSpeciesUUID = speciesUUIDArray.get(selected_species_position);





        // Spinner - Peeps
        List<PeepUnit> peepArray = mPeepUnitManager.getPeepUnits();
        List<String> peepUUIDArray = new ArrayList<String>();
        for(int i = 0;i<peepArray.size();i++){
            peepUUIDArray.add(peepArray.get(i).getName());
        }

        ArrayAdapter<String> dataAdapterPeeps = new ArrayAdapter<String>(getActivity(), R.layout.spinner_layout, peepUUIDArray);
        dataAdapterPeeps.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinnerPeeps.setAdapter(dataAdapterPeeps);

        String peepUUID = peepUnit.getUUID();

        int spinnedIndex = 0;
        for(int i=0;i<peepArray.size();i++){
            if(peepUUID.equals(peepArray.get(i).getUUID()))spinnedIndex = i;
        }
        mSpinnerPeeps.setSelection(spinnedIndex);

        mSpinnerPeeps.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                mPeepUnitManager.setPeepUnitActive(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
            }

        });
    }

    //int selected_species_position = mSpinnerSpecies.getSelectedItemPosition();


    private class DbSyncJob extends AsyncTask<PeepUnit, Void, PeepUnit[]> {

        @Override
        protected PeepUnit[] doInBackground(PeepUnit... peepUnits) {
            //String email = peepUnits[0].getUserEmail();
            //String password = peepUnits[0].getUserPassword();
            SharedPreferences preferences = getContext().getSharedPreferences("UserData", getContext().getApplicationContext().MODE_PRIVATE);
            String email = preferences.getString("email", null);
            String password = preferences.getString("password", null);
            PeepUnit peepUnit = peepUnits[0];

            String tvValue = mEditTextHatchConfigEggCount.getText().toString();
            int eggs = 0;
            if(Integer.parseInt(tvValue)!=0)eggs = Integer.parseInt(tvValue);

            int selected_species_position = mSpinnerSpecies.getSelectedItemPosition();
            String mSpeciesUUID = speciesUUIDArray.get(selected_species_position);

            String mHatchName = mEditTextPeepName.getText().toString();

           // PeepHatch peepHatch = peepUnit.getLastHatch();
            PeepHatch peepHatch = new PeepHatch();
            peepHatch.setMeasureIntervalMin(mMeasureIntervalMin);
            peepHatch.setTemperatureOffsetCelsius(mTemperatureOffset);
            peepHatch.setEndUnixTimestamp(2147483647);
            peepHatch.setEggCount(eggs);
            peepHatch.setSpeciesUUID(mSpeciesUUID);
            peepHatch.setHatchName(mHatchName);

            String accessToken = RestApi.postUserAuth(email, password);
            //RestApi.postUserNewPeep(accessToken, peepUnit);
            //RestApi.postPeepName(accessToken, peepUnit);
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
            ft.commit();
        }
    }

}

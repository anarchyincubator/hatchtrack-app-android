package com.example.hatchtracksensor;

import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Fragment;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.EditText;
import android.text.InputType;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;

public class PeepSelectFragment extends Fragment {

    private PeepUnitManager mPeepUnitManager;

    private ProgressBar mProgressBar;
    private FloatingActionButton mAddPeep;
    private RecyclerView mRecyclerView;
    private MyRecyclerViewAdapter mAdapter;
    private PeepUnit mPeepUnit;

    private ArrayList<PeepUnit> mPeepList;
    private static SharedPreferences mSharedPreferences;

    private String mPeepUnitName;
    private PeepHatch mPeepHatch;

    private int mNumberHatched;

    private String[] mHatchStartEndArray;
    public ArrayList<PeepHatch> peepHatches;
    public ArrayList<String> hatchUUIDList;

    public JSONArray peepHatchJson;

    public String current_hatch_uuid;

    public PeepSelectFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_peep_select, container, false);
    }

    @Override
    public void onActivityCreated(Bundle bundle) {
        super.onActivityCreated(bundle);

        mAddPeep = getView().findViewById(R.id.floatingActionButtonAddPeep);
        mAddPeep.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Fragment fragment = new BluetoothFragment();
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.replace(R.id.content_view, fragment);
                ft.addToBackStack(null);
                ft.commit();
            }
        });

        mSharedPreferences = getActivity().getApplicationContext().getSharedPreferences("UserData",getActivity().getApplicationContext().MODE_PRIVATE);

        mPeepUnitManager = new PeepUnitManager();

        mPeepList = new ArrayList<PeepUnit>();
        mPeepList = mPeepUnitManager.mPeepList;

        // data to populate the RecyclerView with
        ArrayList<String> peepNames = new ArrayList<>(Arrays.asList(mPeepUnitManager.getPeepNames()));
        ArrayList<String> peepHatches = new ArrayList<>(Arrays.asList(mPeepUnitManager.getPeepHatches()));
        mProgressBar = getView().findViewById(R.id.progressBarPeepSelect);
        mProgressBar.setVisibility(View.GONE);
        // set up the RecyclerView
        mRecyclerView = getView().findViewById(R.id.recyclerViewPeepSelect);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        buildRecycleView();
    }

    void buildRecycleView(){
        Log.i("buildRecycleView","buildRecycleView");
        // data to populate the RecyclerView with
        ArrayList<String> peepNames = new ArrayList<>(Arrays.asList(mPeepUnitManager.getPeepNames()));
        ArrayList<String> peepHatches = new ArrayList<>(Arrays.asList(mPeepUnitManager.getPeepHatches()));
        mProgressBar = getView().findViewById(R.id.progressBarPeepSelect);
        mProgressBar.setVisibility(View.GONE);

        mAdapter = new MyRecyclerViewAdapter(getActivity(), peepNames, peepHatches);
        //final String uuid = mPeepUnit.getUUID();
        mAdapter.setClickListener(
                new MyRecyclerViewAdapter.ItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        mPeepUnit = mPeepUnitManager.getPeepUnit(position);
                        final String uuid = mPeepUnit.getUUID();
                        final String[] action = {
                                "View Current Hatch",
                                "All Hatches",
                                "Rename Peep",
                                "Temperature Offset",
                                "Remove Peep"};

                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                        builder.setTitle("Options");

                        final TextView mTextViewPeepID = new TextView(getContext());
                        mTextViewPeepID.setText("Peep ID: "+uuid);
                        mTextViewPeepID.setGravity(android.view.Gravity.CENTER_VERTICAL | android.view.Gravity.CENTER_HORIZONTAL);

                        builder.setView(mTextViewPeepID);

                        builder.setItems(action, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (0 == which) {
                                    getActivity().setTitle("Getting hatch data...");
                                    //Set current Hatch UUID
                                    SharedPreferences.Editor editor = mSharedPreferences.edit();
                                    editor.putString("selected_hatch_uuid", mPeepUnit.getLastHatch().getUUID());
                                    editor.commit();
                                    Fragment fragment = new PeepSensorDataFragment();
                                    FragmentTransaction ft = getFragmentManager().beginTransaction();
                                    ft.replace(R.id.content_view, fragment);
                                    ft.addToBackStack(null);
                                    ft.commit();
                                }
                                else if (1 == which) {
                                    Log.i("PeepSelectFragment","getHatchesByPeepUUID");
                                    PeepSelectFragment.getHatchesByPeepUUID job = new PeepSelectFragment.getHatchesByPeepUUID();
                                    job.execute();
                                }
                                else if (2 == which) {
                                    mPeepUnitName = mPeepUnit.getName();
                                    setRenamePeep();
                                }
                                else if (3 == which) {
                                    setTemperatureOffset();
                                }
                                else if (4 == which) {
                                    confirmDeleteDialogue();
                                }
                            }
                        });
                        builder.show();
                    }
                }
        );
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setAdapter(null);
        mRecyclerView.setLayoutManager(null);
        mRecyclerView.getRecycledViewPool().clear();
        mRecyclerView.swapAdapter(mAdapter, false);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mAdapter.notifyDataSetChanged();
    }



    private void setRenamePeep() {

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        builder.setTitle("Rename Peep: ");

        final EditText input = new EditText(getContext());
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);
        input.setText(mPeepUnitName);
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mPeepUnitName = input.getText().toString();
                //Log.i("mNumberHatched",String.valueOf(mNumberHatched));
                if(!mPeepUnitName.equals("")){
                    mProgressBar.setVisibility(View.VISIBLE);
                    mRecyclerView.setVisibility(View.GONE);
                    mAddPeep.hide();
                    PeepSelectFragment.asyncRenamePeep asyncRenamePeep = new PeepSelectFragment.asyncRenamePeep();
                    mPeepUnit.setName(mPeepUnitName);
                    asyncRenamePeep.execute(mPeepUnit);
                }else{
                    dialog.cancel();
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    private void setTemperatureOffset() {



        SettingsManager settingsManager= new SettingsManager();
        SettingsManager.TemperatureUnits units = settingsManager.getTemperatureUnits();

        final SettingsManager.TemperatureUnits CELSIUS =
                SettingsManager.TemperatureUnits.CELSIUS;
        final SettingsManager.TemperatureUnits FAHRENHEIT =
                SettingsManager.TemperatureUnits.FAHRENHEIT;

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        String fc = "F°";
        if (CELSIUS == units)fc = "C°";
        builder.setTitle("Temperature offset("+fc+"): ");

        float offset = mPeepUnit.getLastHatch().getTemperatureOffsetCelsius();

        final EditText input = new EditText(getContext());
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        builder.setView(input);

        if (CELSIUS == units) {
            input.setText(Float.toString(offset));
        }
        else {
            offset *= 1.8;
            input.setText(Float.toString(offset));
        }

        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mPeepUnitName = input.getText().toString();
                //Log.i("mNumberHatched",String.valueOf(mNumberHatched));
                if(!mPeepUnitName.equals("")){
                    mProgressBar.setVisibility(View.VISIBLE);
                    mRecyclerView.setVisibility(View.GONE);
                    mAddPeep.hide();
                    PeepSelectFragment.asyncSetTempOffset asyncSetTempOffset = new PeepSelectFragment.asyncSetTempOffset();
                    //mPeepUnit.off(mPeepUnitName);
                    asyncSetTempOffset.execute(mPeepUnit);
                }else{
                    dialog.cancel();
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }



    private void confirmDeleteDialogue() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        final String[] action = {"Yes", "No"};

        builder.setTitle("Confirm remove?");
        builder.setItems(action, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (0 == which) {
                    mProgressBar.setVisibility(View.VISIBLE);
                    mRecyclerView.setVisibility(View.GONE);
                    mAddPeep.hide();

                    RemovePeepJob job = new RemovePeepJob();
                    job.execute(mPeepUnit);
                }
                else if (1 == which) {
                    // do nothing
                }
            }
        });
        builder.show();
    }

    private class RemovePeepJob extends AsyncTask<PeepUnit, Void, PeepUnit[]> {

        @Override
        protected PeepUnit[] doInBackground(PeepUnit... peepUnits) {
            //String email = peepUnits[0].getUserEmail();
            //String password = peepUnits[0].getUserPassword();
            SharedPreferences preferences = getContext().getSharedPreferences("UserData", getContext().getApplicationContext().MODE_PRIVATE);
            String email = preferences.getString("email", null);
            String password = preferences.getString("password", null);
            PeepUnit peepUnit = peepUnits[0];
            PeepHatch peepHatch = peepUnit.getLastHatch();

            String accessToken = RestApi.postUserAuth(email, password);
            if (null != peepHatch) {
                RestApi.postHatchEnd(accessToken, peepHatch);
            }
            RestApi.deleteUserPeep(accessToken, peepUnit);

            return peepUnits;
        }

        @Override
        protected void onPostExecute(PeepUnit[] peepUnits) {
            Fragment fragment = new PeepDatabaseSyncFragment();
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.replace(R.id.content_view, fragment);
            ft.addToBackStack(null);
            ft.commit();
        }
    }

    private class asyncRenamePeep extends AsyncTask<PeepUnit, Void, PeepUnit[]> {

        @Override
        protected PeepUnit[] doInBackground(PeepUnit... peepUnits) {
            String email = peepUnits[0].getUserEmail();
            String password = peepUnits[0].getUserPassword();
            PeepUnit peepUnit = peepUnits[0];

            String accessToken = RestApi.postUserAuth(email, password);
            RestApi.postPeepName(accessToken, peepUnit);

            return peepUnits;
        }

        @Override
        protected void onPostExecute(PeepUnit[] peepUnits) {
            Log.i("MREUTMAN", "asyncRenamePeep DONE!");
            buildRecycleView();

        }
    }

    private class asyncSetTempOffset extends AsyncTask<PeepUnit, Void, PeepUnit[]> {

        @Override
        protected PeepUnit[] doInBackground(PeepUnit... peepUnits) {
            String email = peepUnits[0].getUserEmail();
            String password = peepUnits[0].getUserPassword();
            PeepUnit peepUnit = peepUnits[0];

            String accessToken = RestApi.postUserAuth(email, password);
            //RestApi.postPeepName(accessToken, peepUnit);

            return peepUnits;
        }

        @Override
        protected void onPostExecute(PeepUnit[] peepUnits) {
            Log.i("MREUTMAN", "asyncRenamePeep DONE!");
            buildRecycleView();

        }
    }




    public void buildHatchHistoryList(JSONObject peepHatchAllData){


        //JSONArray json = new JSONArray();
        try {
            peepHatchJson = peepHatchAllData.getJSONArray("hatches_all_data");
            //Log.i("json", json.toString());
        }catch(Exception e){
            //e.printStackTrace();
            Log.i("json", e.toString() + " ------" + peepHatchAllData.toString());
        }
        mHatchStartEndArray = new String[peepHatchJson.length()];
        int untitledCount = 0;
        for(int i=0;i<peepHatchJson.length();i++){

            JSONObject current = new JSONObject();
            Date dateStart = new java.util.Date();
            Date dateEnd = new java.util.Date();
            Date dateCurr = new java.util.Date();
            String hatchName = new String();
            try{
                current = peepHatchJson.getJSONObject(i);
                Log.i("buildHatchHistoryList",current.toString());
                //dateStart = new java.util.Date(current.getInt("start_unix_timestamp")*1000L);
                //dateEnd = new java.util.Date(current.getInt("end_unix_timestamp")*1000L);
                hatchName = current.getString("hatch_name");

            }catch(Exception e){
                //e.printStackTrace();
                Log.i("e",e.toString());
            }
            /*
            SimpleDateFormat hourTime = new SimpleDateFormat("M/d/Y", Locale.ENGLISH);
            String localTimeStart = hourTime.format(dateStart);
            String localTimeEnd = hourTime.format(dateEnd);
            if(dateEnd.compareTo(dateCurr)>0)localTimeEnd = "In Progress";
            String startEnd = localTimeStart + " - " + localTimeEnd;
            */
            if(hatchName.equals("null")){
                untitledCount++;
                hatchName = "Untitled Hatch "+untitledCount;
            }
            mHatchStartEndArray[i] = hatchName;
        }

        Log.i("mHatchStartEndArray",mHatchStartEndArray.toString());
        //first create a list from String array
        List<String> list = Arrays.asList(mHatchStartEndArray);

        //next, reverse the list using Collections.reverse method
        //Collections.reverse(list);

        //next, convert the list back to String array

        final String[] action = (String[])list.toArray();
        //peepHatches

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Hatches");
        builder.setItems(action, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.i("BUILDER", String.valueOf(which));

                //current_hatch_uuid = hatchUUIDList.get(which);
                //if(String.valueOf(which).equals("0"))current_hatch_uuid = "";


                int mPeepUnitIndex = 0;
                String peepUUID = "";
                try{
                    current_hatch_uuid = peepHatchJson.getJSONObject(which).getString("uuid");
                    peepUUID = peepHatchJson.getJSONObject(which).getString("peep_uuid");
                    Log.i("current_hatch_uuid",current_hatch_uuid);
                    Log.i("current_peep_uuid",peepUUID);
                }catch(Exception e){

                }
                for (int i = 0; i < mPeepList.size(); i++) {
                    Log.i("TAG", peepUUID + " " + mPeepList.get(i).getUUID());
                    if (peepUUID.equals(mPeepList.get(i).getUUID()))mPeepUnitIndex = i;
                }

                // Set Active Peep Unit
                mPeepUnitManager.setPeepUnitActive(mPeepUnitIndex);

                //Set current Hatch UUID
                SharedPreferences.Editor editor = mSharedPreferences.edit();
                editor.putString("selected_hatch_uuid", current_hatch_uuid);
                editor.commit();

                String buttonTitle = mHatchStartEndArray[which];

                if(buttonTitle.indexOf("In Progress")!=-1){
                    getActivity().setTitle("Getting sensor data...");
                    Fragment fragment = new SensorFragment();
                    FragmentTransaction ft = getFragmentManager().beginTransaction();
                    ft.replace(R.id.content_view, fragment);
                    ft.addToBackStack(null);
                    ft.commit();
                }else {
                    getActivity().setTitle("Getting hatch data...");
                    Fragment fragment = new PeepSensorDataFragment();
                    FragmentTransaction ft = getFragmentManager().beginTransaction();
                    ft.replace(R.id.content_view, fragment);
                    ft.addToBackStack(null);
                    ft.commit();
                }
            }
        });
        builder.show();

    }



    private class getHatchesByPeepUUID extends AsyncTask<PeepHatch, Void, JSONObject > {

        JSONObject peepHatchAllData = new JSONObject();
        @Override
        protected JSONObject doInBackground(PeepHatch... pairs) {
            Log.i("HATCH_DATA", "doInBackground");
            String accessToken = RestApi.postUserAuth( mPeepUnit.getUserEmail(), mPeepUnit.getUserPassword());
            peepHatchAllData = RestApi.getAllHatchData(accessToken, mPeepUnit.getUUID());
            Log.i("HATCH_DATA", peepHatchAllData.toString());
            return peepHatchAllData;
        }

        @Override
        protected void onPostExecute(JSONObject returnedData) {
            buildHatchHistoryList(returnedData);
        }
    }


}
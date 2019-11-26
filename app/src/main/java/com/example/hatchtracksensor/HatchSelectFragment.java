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
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;
import android.view.inputmethod.EditorInfo;
import android.text.method.ScrollingMovementMethod;

public class HatchSelectFragment extends Fragment {

    private PeepUnitManager mPeepUnitManager;

    private ProgressBar mProgressBar;
    private FloatingActionButton mAddHatch;
    private RecyclerView mRecyclerView;
    private MyRecyclerViewAdapterHatches mAdapter;
    private PeepUnit mPeepUnit;
    private PeepHatch mPeepHatch;
    private String current_hatch_uuid;
    private int hatchOptionsMode;

    private int mNumberHatched;

    private ArrayList<PeepUnit> mPeepList;

    private static SharedPreferences mSharedPreferences;

    public HatchSelectFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_hatch_select, container, false);
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (hidden) {
            Log.d("TAG", ((Object) this).getClass().getSimpleName() + " is NOT on screen");
        }
        else
        {
            Log.d("TAG", ((Object) this).getClass().getSimpleName() + " is on screen");
        }
    }

    @Override
    public void onActivityCreated(Bundle bundle) {
        super.onActivityCreated(bundle);

        mAddHatch = getView().findViewById(R.id.floatingActionButtonAddHatch);
        mAddHatch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Fragment fragment = new HatchConfigFragment();
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.replace(R.id.content_view, fragment);
                ft.addToBackStack(null);
                ft.commit();
            }
        });

        mSharedPreferences = getActivity().getApplicationContext().getSharedPreferences("UserData",getActivity().getApplicationContext().MODE_PRIVATE);

        getActivity().setTitle("Hatches ");

        mProgressBar = getView().findViewById(R.id.progressBarPeepSelect);
        mProgressBar.setVisibility(View.GONE);

        mPeepList = new ArrayList<PeepUnit>();
        mPeepList = mPeepUnitManager.mPeepList;

        if(mPeepList.size()>0) {
            mPeepUnitManager = new PeepUnitManager();
            mPeepUnit = mPeepUnitManager.getPeepUnitActive();
        }



        Log.i("HATCH_DATA", "getHatchesList");

        HatchSelectFragment.getHatchesList job = new HatchSelectFragment.getHatchesList();
        job.execute();

    }

    private class getHatchByUUID extends AsyncTask<PeepHatch, Void, PeepHatch> {
        @Override
        protected PeepHatch doInBackground(PeepHatch... hatches) {

           // PeepUnit peepUnit = mPeepUnitManager.getPeepUnitActive();
            PeepHatch current_hatch = new PeepHatch();
            //if(peepUnit.getUUID()!=null) {


                //if (current_hatch_uuid.equals(""))current_hatch_uuid = peepUnit.getLastHatch().getUUID();

                Log.i("accessToken:", "accessToken ");

                SharedPreferences preferences = getContext().getSharedPreferences("UserData", getContext().getApplicationContext().MODE_PRIVATE);
                String email = preferences.getString("email", null);
                String password = preferences.getString("password", null);

                String accessToken = RestApi.postUserAuth(
                        email,
                        password);

                Log.i("current_hatch_uuid", current_hatch_uuid);
                current_hatch = RestApi.getHatch(accessToken, current_hatch_uuid);
            //}
            return current_hatch;
        }

        @Override
        protected void onPostExecute(PeepHatch current_hatch) {

            Log.i("onPostExecute","onPostExecute");
            mPeepHatch = current_hatch;
            if(hatchOptionsMode == 1){
                Fragment fragment = new HatchReconfigFragment();
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.replace(R.id.content_view, fragment);
                ft.addToBackStack(null);
                ft.commit();
            }else if(hatchOptionsMode == 2){
                hatchNotesDialog();
            }else if(hatchOptionsMode == 3){
                confirmStopHatchDialogue();
            }
        }
    }

    private void confirmResumeHatchDialogue() {

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        builder.setTitle("Resume Hatch ?");

        final EditText input = new EditText(getContext());
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        builder.setView(input);

        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.i("mNumberHatched",String.valueOf(mNumberHatched));
                    mProgressBar.setVisibility(View.VISIBLE);
                    mRecyclerView.setVisibility(View.GONE);
                    mAddHatch.hide();

                    //mPeepHatch.setHatchedCount(mNumberHatched);
                    HatchSelectFragment.ResumeHatchJob job = new HatchSelectFragment.ResumeHatchJob();
                    job.execute(mPeepUnit);

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

    private void confirmStopHatchDialogue() {

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        Date dateStart = new java.util.Date();

        SimpleDateFormat hourTime = new SimpleDateFormat("M/d/Y", Locale.ENGLISH);
        String todayDate = hourTime.format(dateStart);

        //builder.setTitle("Confirm Stop Current Hatch on: "+todayDate+"\n ");
        builder.setTitle("# of Eggs Hatched (of "+String.valueOf(mPeepHatch.getEggCount())+"):");

        final EditText input = new EditText(getContext());
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        builder.setView(input);

        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mNumberHatched = Integer.valueOf(input.getText().toString());
                int mNumberOfEggs = mPeepHatch.getEggCount();
                Log.i("mNumberHatched",String.valueOf(mNumberHatched));
                if(mNumberHatched <= mNumberOfEggs){
                    mProgressBar.setVisibility(View.VISIBLE);
                    mRecyclerView.setVisibility(View.GONE);
                    mAddHatch.hide();
                    //PeepHatch peepHatch = mPeepUnit.getLastHatch();
                    mPeepHatch.setHatchedCount(mNumberHatched);
                    HatchSelectFragment.StopHatchJob job = new HatchSelectFragment.StopHatchJob();
                    job.execute(mPeepUnit);
                }else{
                    //dialog.cancel();
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setTitle("# of Eggs Hatched Can't be greater than # of Eggs total.\n\n");
                    builder.setNegativeButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
                    builder.show();
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

    private void hatchNotesDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        //builder.setTitle("Confirm Stop Current Hatch on: "+todayDate+"\n ");
        builder.setTitle("Hatch Notes: ");

        final EditText input = new EditText(getContext());
        input.setHeight(100);
        String hatchNotes = new String();
        if(!mPeepHatch.getHatchNotes().equals("null"))hatchNotes = mPeepHatch.getHatchNotes();
        input.setText(hatchNotes);

        input.setSingleLine(false);
        input.setImeOptions(EditorInfo.IME_FLAG_NO_ENTER_ACTION);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        input.setLines(5);
        input.setMaxLines(10);
        input.setVerticalScrollBarEnabled(true);
        input.setMovementMethod(ScrollingMovementMethod.getInstance());
        input.setScrollBarStyle(View.SCROLLBARS_INSIDE_INSET);

        builder.setView(input);

        builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String notesString = input.getText().toString();
                Log.i("notesString",notesString);
                if(!notesString.equals("")){
                    mProgressBar.setVisibility(View.VISIBLE);
                    mRecyclerView.setVisibility(View.GONE);
                    mAddHatch.hide();
                    mPeepHatch.setHatchNotes(notesString);
                    HatchSelectFragment.HatchAddNotes job = new HatchSelectFragment.HatchAddNotes();
                    job.execute(mPeepUnit);
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

    private class HatchAddNotes extends AsyncTask<PeepUnit, Void, PeepUnit[]> {

        @Override
        protected PeepUnit[] doInBackground(PeepUnit... peepUnits) {
            //String email = peepUnits[0].getUserEmail();
            //String password = peepUnits[0].getUserPassword();
            //PeepUnit peepUnit = peepUnits[0];
            //PeepHatch peepHatch = peepUnit.getLastHatch();

            SharedPreferences preferences = getContext().getSharedPreferences("UserData", getContext().getApplicationContext().MODE_PRIVATE);
            String email = preferences.getString("email", null);
            String password = preferences.getString("password", null);

            String accessToken = RestApi.postUserAuth(email, password);
            if (null != mPeepHatch) {
                Log.i("mPeepHatch",mPeepHatch.toString());
                RestApi.postHatchNotes(accessToken, mPeepHatch);
            }

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

    private class StopHatchJob extends AsyncTask<PeepUnit, Void, PeepUnit[]> {

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

    private class ResumeHatchJob extends AsyncTask<PeepUnit, Void, PeepUnit[]> {

        @Override
        protected PeepUnit[] doInBackground(PeepUnit... peepUnits) {
            SharedPreferences preferences = getContext().getSharedPreferences("UserData", getContext().getApplicationContext().MODE_PRIVATE);
            String email = preferences.getString("email", null);
            String password = preferences.getString("password", null);
            //String email = peepUnits[0].getUserEmail();
            //String password = peepUnits[0].getUserPassword();
            String accessToken = RestApi.postUserAuth(email, password);

            if (null != mPeepHatch) {
                RestApi.resumeHatch(accessToken, mPeepHatch);
            }

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

    private class getHatchesList extends AsyncTask<PeepHatch, Void, JSONArray> {

        JSONObject peepHatchAllData = new JSONObject();
        @Override
        protected JSONArray doInBackground(PeepHatch... pairs) {
            Log.i("HATCH_DATA", "doInBackground");
            SharedPreferences preferences = getContext().getSharedPreferences("UserData", getContext().getApplicationContext().MODE_PRIVATE);
            String email = preferences.getString("email", null);
            String password = preferences.getString("password", null);
            String accessToken = RestApi.postUserAuth( email, password);
            peepHatchAllData = RestApi.getAllHatchDataEmail(accessToken, email);
            JSONArray allEmailDataJSON = new JSONArray();
            try{
                allEmailDataJSON = peepHatchAllData.getJSONArray("hatches_all_data_email");
            }catch(Exception e){

            }
            Log.i("HATCH_DATA",allEmailDataJSON.toString());
            return allEmailDataJSON;
        }

        @Override
        protected void onPostExecute(final JSONArray returnedData) {

            Log.i("returnedData -",returnedData.toString());

            // data to populate the RecyclerView with
            //ArrayList<String> peepNames = new ArrayList<>(Arrays.asList(mPeepUnitManager.getPeepNames()));
            //ArrayList<String> peepHatches = new ArrayList<>(Arrays.asList(mPeepUnitManager.getPeepHatches()));

            final long unixTime = System.currentTimeMillis() / 1000L;

            // set up the RecyclerView
            mRecyclerView = getView().findViewById(R.id.recyclerViewHatchSelect);
            mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
            mAdapter = new MyRecyclerViewAdapterHatches(getActivity(), returnedData);

            mAdapter.setClickListener(
                    new MyRecyclerViewAdapterHatches.ItemClickListener() {
                        @Override
                        public void onItemClick(View view, int position) {
                            final int hatchSelect = position;
                            String[] action = {
                                    "View Hatch",
                                    "Hatch Notes",
                                    "Reconfigure Hatch",
                                    "Stop Hatch"};
                            try {
                                Log.i("onItemClick", String.valueOf(returnedData.getJSONObject(hatchSelect).getInt("end_unix_timestamp")));
                            }catch(Exception e){

                            }
                            try {

                                if ((long)returnedData.getJSONObject(hatchSelect).getInt("end_unix_timestamp")<System.currentTimeMillis() / 1000L) {
                                    action = new String[]{
                                            "View Hatch",
                                            "Hatch Notes"};
                                }
                            }catch(Exception e){
                                Log.i("Exception",e.toString());
                            }

                            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                            builder.setTitle("Options");
                            builder.setItems(action, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                    String hatchUUID = "";
                                    String peepUUID = "";
                                    int mPeepUnitIndex = 0;
                                    long hatchEndUnix = 0;
                                    try {
                                        hatchUUID = returnedData.getJSONObject(hatchSelect).getString("uuid");
                                        current_hatch_uuid = hatchUUID;
                                        peepUUID = returnedData.getJSONObject(hatchSelect).getString("peep_uuid");
                                        hatchEndUnix = returnedData.getJSONObject(hatchSelect).getLong("end_unix_timestamp");
                                        for (int i = 0; i < mPeepList.size(); i++) {
                                            Log.i("TAG", peepUUID + " " + mPeepList.get(i).getUUID());
                                            if (peepUUID.equals(mPeepList.get(i).getUUID())){
                                                mPeepUnitIndex = i;
                                                mPeepUnitManager.setPeepUnitActive(mPeepUnitIndex);
                                            }
                                        }
                                    }catch(Exception e){

                                    }

                                    // Set Active Peep Unit
                                    //if(!peepUUID.equals(""))mPeepUnitManager.setPeepUnitActive(mPeepUnitIndex);



                                    // Set current Hatch UUID
                                    SharedPreferences.Editor editor = mSharedPreferences.edit();
                                    editor.putString("selected_hatch_uuid", hatchUUID);
                                    editor.commit();

                                    hatchOptionsMode = 0;
                                    if (0 == which) {
                                        if(hatchEndUnix>unixTime){
                                            getActivity().setTitle("Getting sensor data...");
                                            Fragment fragment = new SensorFragment();
                                            FragmentTransaction ft = getFragmentManager().beginTransaction();
                                            ft.replace(R.id.content_view, fragment);
                                            ft.addToBackStack(null);
                                            ft.commit();
                                            Log.i("hatchUUID", hatchUUID);
                                            Log.i("peepUUID", peepUUID);
                                        }else {
                                            getActivity().setTitle("Getting hatch data...");
                                            Fragment fragment = new PeepSensorDataFragment();
                                            FragmentTransaction ft = getFragmentManager().beginTransaction();
                                            ft.replace(R.id.content_view, fragment);
                                            ft.addToBackStack(null);
                                            ft.commit();
                                            Log.i("hatchUUID", hatchUUID);
                                            Log.i("peepUUID", peepUUID);
                                        }
                                    }
                                    else if (1 == which) {
                                        hatchOptionsMode = 2;
                                    }
                                    else if (2 == which) {
                                        hatchOptionsMode = 1;
                                        /*
                                        Fragment fragment = new HatchReconfigFragment();
                                        FragmentTransaction ft = getFragmentManager().beginTransaction();
                                        ft.replace(R.id.content_view, fragment);
                                        ft.addToBackStack(null);
                                        ft.commit();
                                        */

                                    }
                                    else if (3 == which) {
                                        hatchOptionsMode = 3;
                                        mPeepUnit = mPeepUnitManager.getPeepUnit(mPeepUnitIndex);

                                    }
                                    HatchSelectFragment.getHatchByUUID job = new HatchSelectFragment.getHatchByUUID();
                                    job.execute();
                                }
                            });
                            builder.show();
                        }
                    }
            );
            //mRecyclerView.setAdapter(mAdapter);
            /*
            mAdapter.setClickListener(
                    new MyRecyclerViewAdapterHatches.ItemClickListener() {
                        @Override
                        public void onItemClick(View view, int position) {
                            String hatchUUID = "";
                            String peepUUID = "";
                            try{
                                hatchUUID = returnedData.getJSONObject(position).getString("uuid");
                                peepUUID = returnedData.getJSONObject(position).getString("peep_uuid");

                                int mPeepUnitIndex = 0;
                                for(int i = 0;i<mPeepList.size();i++){
                                    Log.i("TAG",peepUUID + " " +mPeepList.get(i).getUUID());
                                    if(peepUUID.equals(mPeepList.get(i).getUUID()))mPeepUnitIndex = i;
                                }

                                mPeepUnitManager.setPeepUnitActive(mPeepUnitIndex);

                                SharedPreferences.Editor editor = mSharedPreferences.edit();
                                editor.putString("selected_hatch_uuid", hatchUUID);
                                editor.commit();
                                getActivity().setTitle("Getting hatch data...");
                                Fragment fragment = new PeepSensorDataFragment();
                                FragmentTransaction ft = getFragmentManager().beginTransaction();
                                ft.replace(R.id.content_view, fragment);
                                ft.addToBackStack(null);

                                ft.commit();

                            }catch(Exception e){

                            }

                            Log.i("hatchUUID",hatchUUID);
                            Log.i("peepUUID",peepUUID);
                        }
                    }
            );
            */
            mRecyclerView.setAdapter(mAdapter);


        }
    }


}
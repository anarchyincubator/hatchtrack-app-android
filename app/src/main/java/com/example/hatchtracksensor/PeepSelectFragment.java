package com.example.hatchtracksensor;

import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Fragment;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import java.util.ArrayList;
import java.util.Arrays;

public class PeepSelectFragment extends Fragment {

    private PeepUnitManager mPeepUnitManager;

    private ProgressBar mProgressBar;
    private FloatingActionButton mAddPeep;
    private RecyclerView mRecyclerView;
    private MyRecyclerViewAdapter mAdapter;
    private PeepUnit mPeepUnit;

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


        mPeepUnitManager = new PeepUnitManager();
        // data to populate the RecyclerView with
        ArrayList<String> peepNames = new ArrayList<>(Arrays.asList(mPeepUnitManager.getPeepNames()));

        mProgressBar = getView().findViewById(R.id.progressBarPeepSelect);
        mProgressBar.setVisibility(View.GONE);
        // set up the RecyclerView
        mRecyclerView = getView().findViewById(R.id.recyclerViewPeepSelect);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mAdapter = new MyRecyclerViewAdapter(getActivity(), peepNames);
        mAdapter.setClickListener(
                new MyRecyclerViewAdapter.ItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        final String[] action = {
                                "Monitor Peep",
                                "Reconfigure Hatch",
                                "New Hatch",
                                "Stop Hatch",
                                "Remove Peep"};
                        final int peepSelect = position;

                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                        builder.setTitle("Options");
                        builder.setItems(action, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (0 == which) {
                                    mPeepUnitManager.setPeepUnitActive(peepSelect);
                                    Fragment fragment = new SensorFragment();
                                    FragmentTransaction ft = getFragmentManager().beginTransaction();
                                    ft.replace(R.id.content_view, fragment);
                                    ft.addToBackStack(null);
                                    ft.commit();
                                }
                                else if (1 == which) {
                                    mPeepUnitManager.setPeepUnitActive(peepSelect);
                                    Fragment fragment = new HatchReconfigFragment();
                                    FragmentTransaction ft = getFragmentManager().beginTransaction();
                                    ft.replace(R.id.content_view, fragment);
                                    ft.addToBackStack(null);
                                    ft.commit();
                                }
                                else if (2 == which) {
                                    mProgressBar.setVisibility(View.VISIBLE);
                                    mRecyclerView.setVisibility(View.GONE);
                                    mAddPeep.hide();

                                    mPeepUnitManager.setPeepUnitActive(peepSelect);
                                    mPeepUnit = mPeepUnitManager.getPeepUnit(peepSelect);

                                    NewHatchJob job = new NewHatchJob();
                                    job.execute(mPeepUnit);
                                }
                                else if (3 == which) {
                                    mProgressBar.setVisibility(View.VISIBLE);
                                    mRecyclerView.setVisibility(View.GONE);
                                    mAddPeep.hide();

                                    mPeepUnitManager.setPeepUnitActive(peepSelect);
                                    mPeepUnit = mPeepUnitManager.getPeepUnit(peepSelect);

                                    StopHatchJob job = new StopHatchJob();
                                    job.execute(mPeepUnit);
                                }
                                else if (4 == which) {
                                    mPeepUnit = mPeepUnitManager.getPeepUnit(peepSelect);
                                    confirmDeleteDialogue();
                                }
                            }
                        });
                        builder.show();
                    }
                }
        );
        mRecyclerView.setAdapter(mAdapter);
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
            String email = peepUnits[0].getUserEmail();
            String password = peepUnits[0].getUserPassword();
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

    private class StopHatchJob extends AsyncTask<PeepUnit, Void, PeepUnit[]> {

        @Override
        protected PeepUnit[] doInBackground(PeepUnit... peepUnits) {
            String email = peepUnits[0].getUserEmail();
            String password = peepUnits[0].getUserPassword();
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

    private class NewHatchJob extends AsyncTask<PeepUnit, Void, PeepUnit[]> {

        @Override
        protected PeepUnit[] doInBackground(PeepUnit... peepUnits) {
            String email = peepUnits[0].getUserEmail();
            String password = peepUnits[0].getUserPassword();
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
            Fragment fragment = new HatchConfigFragment();
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.replace(R.id.content_view, fragment);
            ft.addToBackStack(null);
            ft.commit();
        }
    }

}
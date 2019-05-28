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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import java.util.ArrayList;
import java.util.Arrays;

public class PeepSelectFragment extends Fragment {

    private PeepUnitManager mPeepUnitManager;
    private RemovePeepJob mJob;

    private ProgressBar mProgressBar;
    private FloatingActionButton mAddPeep;
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private LinearLayoutManager layoutManager;
    private MyRecyclerViewAdapter adapter;

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
        adapter = new MyRecyclerViewAdapter(getActivity(), peepNames);
        adapter.setClickListener(
                new MyRecyclerViewAdapter.ItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        final String[] action = {"Monitor", "Configure", "Delete"};
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
                                    mRecyclerView.setVisibility(View.GONE);
                                    mAddPeep.hide();

                                    PeepUnit peepUnit = mPeepUnitManager.getPeepUnit(peepSelect);
                                    mProgressBar.setVisibility(View.VISIBLE);
                                    mJob = new RemovePeepJob();
                                    mJob.execute(peepUnit);
                                }
                            }
                        });
                        builder.show();
                    }
                }
        );
        mRecyclerView.setAdapter(adapter);
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

}
package com.example.hatchtracksensor;

import android.app.FragmentTransaction;
import android.os.Bundle;
import android.app.Fragment;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Arrays;

public class PeepSelectFragment extends Fragment {

    private PeepUnitManager mPeepUnitManager;

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

        // set up the RecyclerView
        mRecyclerView = getView().findViewById(R.id.recyclerViewPeepSelect);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        adapter = new MyRecyclerViewAdapter(getActivity(), peepNames);
        adapter.setClickListener(
                new MyRecyclerViewAdapter.ItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        //mPeepUnitManager.setPeepUnitActive(position);

                        //Fragment fragment = new SensorFragment();
                        Fragment fragment = new PeepUnitFragment();
                        Bundle args = new Bundle();
                        args.putInt("index", position);
                        fragment.setArguments(args);
                        FragmentTransaction ft = getFragmentManager().beginTransaction();
                        ft.replace(R.id.content_view, fragment);
                        ft.addToBackStack(null);
                        ft.commit();
                    }
                }
        );
        mRecyclerView.setAdapter(adapter);
    }
}
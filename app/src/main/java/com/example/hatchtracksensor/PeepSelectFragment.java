package com.example.hatchtracksensor;

import android.app.FragmentTransaction;
import android.os.Bundle;
import android.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;

public class PeepSelectFragment extends Fragment {

    private PeepManager mPeepManager;

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

        mPeepManager = new PeepManager("test@widgt.ninja");

        // data to populate the RecyclerView with
        ArrayList<String> peepNames = new ArrayList<>(Arrays.asList(mPeepManager.getPeepNames()));
        //peepNames.add("Peep 1");
        //peepNames.add("Peep 2");

        // set up the RecyclerView
        mRecyclerView = getView().findViewById(R.id.recyclerViewPeepSelect);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        adapter = new MyRecyclerViewAdapter(getActivity(), peepNames);
        adapter.setClickListener(
                new MyRecyclerViewAdapter.ItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        mPeepManager.setPeepUnitActive(position);

                        Fragment fragment = new SensorFragment();
                        FragmentTransaction ft = getFragmentManager().beginTransaction();
                        ft.replace(R.id.content_view, fragment);
                        ft.commit();
                    }
                }
        );
        mRecyclerView.setAdapter(adapter);
    }
}
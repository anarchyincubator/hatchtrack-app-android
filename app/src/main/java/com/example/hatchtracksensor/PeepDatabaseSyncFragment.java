package com.example.hatchtracksensor;

import android.app.FragmentTransaction;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import org.json.JSONObject;
import java.util.ArrayList;

public class PeepDatabaseSyncFragment extends Fragment {

    static public final int DATABASE_TO_APP_SYNC = 0;
    static public final int APP_TO_DATABASE_SYNC = 1;
    private  int mCommand = 0;
    private  DbSyncJob mJob;

    public PeepDatabaseSyncFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        try {
            mCommand = this.getArguments().getInt("command");
        }
        catch (Exception e) {
            mCommand = DATABASE_TO_APP_SYNC;
        }

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_peep_database_sync, container, false);
    }

    @Override
    public void onActivityCreated(Bundle bundle) {
        super.onActivityCreated(bundle);

        if (DATABASE_TO_APP_SYNC == mCommand) {
            AccountManager accountManager = new AccountManager(getContext());

            mJob = new DbSyncJob();
            String email = accountManager.getEmail();
            String password = accountManager.getPassword();
            Pair<String, String> pair = new Pair<>(email, password);
            mJob.execute(pair);
        }
        else if (APP_TO_DATABASE_SYNC == mCommand) {
            Log.e("MREUTMAN", "ooops");
        }
        else {
            Log.e("MREUTMAN", "you died");
        }
    }

    private class DbSyncJob extends AsyncTask<Pair<String, String>, Void, ArrayList<PeepUnit> > {
        private final String HEADER_ACCESS_TOKEN = "access-token";

        @Override
        protected ArrayList<PeepUnit>  doInBackground(Pair<String, String>... pairs) {
            ArrayList<PeepUnit> peepUnits = new ArrayList<PeepUnit>();
            String email = pairs[0].first;
            String password = pairs[0].second;

            String accessToken = RestApi.postUserAuth(email, password);
            ArrayList<String> uuids = RestApi.getPeepUUIDs(accessToken);
            JSONObject json;
            String uuid;
            for (int j = 0; j < uuids.size(); j++) {
                uuid = uuids.get(j);
                PeepUnit peepUnit = new PeepUnit(email, password, uuid);

                String name = RestApi.getPeepName(accessToken, peepUnit);
                peepUnit.setName(name);

                ArrayList<String> hatchUUIDs = RestApi.getHatchUUIDs(accessToken, peepUnit);
                if (0 < hatchUUIDs.size()) {
                    int last = hatchUUIDs.size() - 1;
                    PeepHatch peepHatch = RestApi.getHatch(accessToken, hatchUUIDs.get(last));
                    peepUnit.setHatch(peepHatch);
                }

                peepUnits.add(peepUnit);
            }

            return peepUnits;
        }

        @Override
        protected void onPostExecute(ArrayList<PeepUnit>  peepUnits) {
            if (peepUnits.size() != 0) {

                PeepUnitManager peepUnitManager = new PeepUnitManager();
                peepUnitManager.setPeepUnits(peepUnits);

                Fragment fragment = new SensorFragment();
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.replace(R.id.content_view, fragment);
                ft.commit();
            }
            else {
                Fragment fragment = new BluetoothFragment();
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.replace(R.id.content_view, fragment);
                ft.commit();
            }
        }
    }
}

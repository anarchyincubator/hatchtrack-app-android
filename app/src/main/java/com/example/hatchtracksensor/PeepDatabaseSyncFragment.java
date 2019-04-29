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

import com.amazonaws.util.IOUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
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

        protected  String  getAccessToken(String email, String password) {
            String accessToken = "";
            try {
                JSONObject json = new JSONObject();
                json.put("email", email);
                json.put("password", password);

                String body = json.toString();
                String requestURL = "https://db.hatchtrack.com:18888/auth";
                URL url = new URL(requestURL);

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(15000);
                conn.setConnectTimeout(15000);
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoInput(true);
                conn.setDoOutput(true);

                OutputStream out = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out, "UTF-8"));
                writer.write(body);

                writer.flush();
                writer.close();
                out.close();

                InputStream in = new BufferedInputStream(conn.getInputStream());
                String data = IOUtils.toString(in);
                json = new JSONObject(data);
                accessToken = json.getString("access-token");
            } catch (Exception e) {
                e.printStackTrace();
            }

            return accessToken;
        }

        protected ArrayList<String>  getPeepUUIDs(String accessToken, String email) {
            ArrayList<String> list = new ArrayList<String>();
            try {
                String requestURL = "https://db.hatchtrack.com:18888/api/v1/email2uuids?email=" + email;
                URL url = new URL(requestURL);

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(15000);
                conn.setConnectTimeout(15000);
                conn.setRequestMethod("GET");
                conn.setRequestProperty(HEADER_ACCESS_TOKEN, accessToken);
                conn.setDoInput(true);

                InputStream in = new BufferedInputStream(conn.getInputStream());
                String  data= IOUtils.toString(in);
                JSONObject json = new JSONObject(data);
                JSONArray array = json.getJSONArray("peep_uuids");
                for (int i = 0; i < array.length(); i++) {
                    list.add(array.getString(i));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return  list;
        }

        protected String getPeepName(String accessToken, String uuid) {
            String name = "";

            try {
                String requestURL = "https://db.hatchtrack.com:18888/api/v1/uuid2info?uuid=" + uuid;
                URL url = new URL(requestURL);

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(15000);
                conn.setConnectTimeout(15000);
                conn.setRequestMethod("GET");
                conn.setRequestProperty(HEADER_ACCESS_TOKEN, accessToken);
                conn.setDoInput(true);

                InputStream in = new BufferedInputStream(conn.getInputStream());
                String  data= IOUtils.toString(in);
                JSONObject json = new JSONObject(data);
                name = json.getString("name");
            } catch (Exception e) {
                e.printStackTrace();
            }

            return name;
        }

        @Override
        protected ArrayList<PeepUnit>  doInBackground(Pair<String, String>... pairs) {
            ArrayList<PeepUnit> peepUnits = new ArrayList<PeepUnit>();
            String email = pairs[0].first;
            String password = pairs[0].second;

            String accessToken = getAccessToken(email, password);
            ArrayList<String> uuids = getPeepUUIDs(accessToken, email);
            String name  = "";
            for (int j = 0; j < uuids.size(); j++) {
                name = getPeepName(accessToken, uuids.get(j));
                peepUnits.add(new PeepUnit(email, password, uuids.get(j), name));
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
                Log.d("MREUTMAN", "TODO: fill this in");
            }
        }
    }
}

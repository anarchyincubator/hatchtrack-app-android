package com.example.hatchtracksensor;

import android.os.AsyncTask;
import android.util.Log;

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

public class PeepManager {

    private static ArrayList<PeepUnit> mPeepList = new ArrayList<PeepUnit>();
    private static int mActivePeepIndex = 0;
    private static String mUserEmail;
    private static DatabaseJob mJob;

    public class PeepUnit {
        private String mName;
        private String mUUID;

        public PeepUnit(String uuid, String name) {
            mUUID = uuid;
            mName = name;
        }

        public String getName() {
            return mName;
        }

        public String getUUID() {
            return mUUID;
        }

        public void setName(String name) {
            mName = name;
        }

        public void setUUID(String uuid) {
            mUUID = uuid;
        }
    }

    public PeepManager(String userEmail) {
        if (mPeepList.isEmpty()) {
            /*
             * TODO: Create database the holds user's Peeps. For now, we just grab the two Peeps
             * TODO: that are configured for our demo purposes.
             */
            mActivePeepIndex = 0;

            mPeepList.add(
                   new PeepUnit("425e11b3-5844-4626-b05a-219d9751e5ca", "Peep 1"));
            mPeepList.add(
                  new PeepUnit("86559e4a-c115-4412-a8b3-b0f54486a18c", "Peep 2"));

            mJob = new DatabaseJob();
            mJob.execute(DatabaseJobTask.QUERY_PEEPS);
        }
    }

    public String[] getPeepNames() {
        String names[] = new String[mPeepList.size()];
        PeepUnit unit;

        for (int i = 0; i < mPeepList.size(); i++) {
            unit = mPeepList.get(i);
            names[i] = unit.getName();
        }

        return names;
    }

    public PeepUnit[] getPeepUnits() {
        PeepUnit units[] = new PeepUnit[mPeepList.size()];
        PeepUnit unit;

        for (int i = 0; i < mPeepList.size(); i++) {
            unit = mPeepList.get(i);
            units[i].setName(unit.getName());
            units[i].setUUID(unit.getUUID());
        }

        return units;
    }

    public int getPeepUnitCount() {
        return mPeepList.size();
    }

    public void setPeepUnitActive(int i) {
        if ((i >= 0) && (i < mPeepList.size())) {
            mActivePeepIndex = i;
        }
    }

    public PeepUnit getPeepUnitActive() {
        PeepUnit unit = mPeepList.get(mActivePeepIndex);
        return unit;
    }


    public enum DatabaseJobTask {
        QUERY_PEEPS,
    }

    private class DatabaseJob extends AsyncTask<DatabaseJobTask, Void,ArrayList<PeepUnit>> {

        private final String HEADER_ACCESS_TOKEN = "access-token";

        protected  String  getAccessToken() {
            String accessToken = "";
            try {
                String body =  "{\"email\": \"test@widgt.ninja\", \"password\": \"blaggg\" }";
                String requestURL = "https://db.hatchtrack.com:18888/auth";
                URL url = new URL(requestURL);

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(15000);
                conn.setConnectTimeout(15000);
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.setDoOutput(true);

                OutputStream out = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(out, "UTF-8"));
                writer.write(body);

                writer.flush();
                writer.close();
                out.close();

                InputStream in = new BufferedInputStream(conn.getInputStream());
                String data = IOUtils.toString(in);
                JSONObject json = new JSONObject(data);
                accessToken = json.getString("access-token");
            } catch (Exception e) {
                e.printStackTrace();
            }

            return accessToken;
        }

        protected ArrayList<String>  getPeepUUIDs(String accessToken) {
            ArrayList<String> list = new ArrayList<String>();
            try {
                String requestURL = "https://db.hatchtrack.com:18888/api/v1/email2uuids?email=test@widgt.ninja";
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
        protected  ArrayList<PeepUnit> doInBackground(DatabaseJobTask... tasks) {
            ArrayList<PeepUnit> peepUnits = new ArrayList<PeepUnit>();
            ArrayList<String> uuids;
            String name;
            String accessToken = getAccessToken();

            for (int i = 0; i < tasks.length; i++) {
                switch(tasks[i]) {
                    case QUERY_PEEPS:
                        uuids = getPeepUUIDs(accessToken);
                        for (int j = 0; j < uuids.size(); j++) {
                            name = getPeepName(accessToken, uuids.get(j));
                            peepUnits.add(new PeepUnit(uuids.get(j), name));
                        }
                        break;
                }
            }

            return peepUnits;
        }

        @Override
        protected void onPostExecute(ArrayList<PeepUnit> peepUnits) {
            Log.d("MREUTMAN", "done!");
        }
    }
}

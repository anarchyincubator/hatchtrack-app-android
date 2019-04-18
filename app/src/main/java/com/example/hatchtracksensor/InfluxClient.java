package com.example.hatchtracksensor;

import com.amazonaws.util.IOUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class InfluxClient {

    private String mUser;
    private String mPassword;
    private String mdbURL;
    private String mdbName;

    public class InfluxMeasurement {
        public String timestamp;
        public double temperature;
        public double humidity;

        public InfluxMeasurement() {
            timestamp = "";
            temperature = 0;
            humidity = 0;
        }
    }

    public InfluxClient(String url, String user, String password, String name) {
        mUser = user;
        mPassword = password;
        mdbURL = url;
        mdbName = name;
    }

    public InfluxMeasurement getMeasurement(String uuid) {
        InfluxMeasurement influxMeasurement = null;

        try {
            /*
             * Reference the InfluxQL documentation page for info regarding how to make queries.
             * For this routine, we just grab the last recorded value for a given Peep UUID.
             */
            String influxQuery = "";
            String uriQuery = "";

            influxQuery += "SELECT * FROM peep ";
            influxQuery += "WHERE peep_uuid='" + uuid + "' ";
            influxQuery += "GROUP BY * ORDER BY DESC LIMIT 1";

            /*
             * Note: We're doing this request over SSL, so no worries about credentials leaking.
             */
            uriQuery += "?u=" + mUser;
            uriQuery += "&p=" + mPassword;
            uriQuery += "&db=" + mdbName;
            uriQuery += "&q=" + URLEncoder.encode(influxQuery, "UTF-8");

            String u = mdbURL + "/query" + uriQuery;
            URL url = new URL(u);
            HttpURLConnection conn  = (HttpURLConnection) url.openConnection();
            InputStream in = new BufferedInputStream(conn.getInputStream());
            influxMeasurement = parseJSONToMeasurement(in);
            conn.disconnect();
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return influxMeasurement;
    }

    private InfluxMeasurement parseJSONToMeasurement(InputStream in) {
        /*
         * Parses something that looks like the following...
         * {
         *   "results": [
         *     {
         *       "statement_id": 0,
         *       "series": [
         *         {
         *           "name": "peep",
         *           "tags": {
         *             "hatch_uuid": "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx",
         *             "peep_uuid": "425e11b3-5844-4626-b05a-219d9751e5ca"
         *           },
         *           "columns": [
         *             "time",
         *             "humidity",
         *             "temperature"
         *           ],
         *           "values": [
         *             [
         *               "2019-03-21T03:21:22Z",
         *               28.61,
         *               22.57
         *             ]
         *           ]
         *         }
         *       ]
         *     }
         *   ]
         * }
         */

        InfluxMeasurement influxMeasurement = new InfluxMeasurement();
        try {
            String data = IOUtils.toString(in);
            JSONObject reader = new JSONObject(data);
            JSONArray results = reader.getJSONArray("results");
            JSONObject object;

            for (int i = 0; i < results.length(); i++) {
                object = results.getJSONObject(i);
                JSONArray series = object.getJSONArray("series");

                for (int j = 0; j < series.length(); j++) {
                    object = series.getJSONObject(j);
                    JSONArray columns = object.getJSONArray("columns");
                    JSONArray values = object.getJSONArray("values");

                    for (int k = 0; k < values.length(); k++) {
                        JSONArray value = values.getJSONArray(k);

                        for (int l = 0; l < columns.length(); l++) {
                            if (columns.getString(l).equals("time")) {
                                influxMeasurement.timestamp = value.getString(l);
                            }
                            else if (columns.getString(l).equals("temperature")) {
                                influxMeasurement.temperature = value.getDouble(l);
                            }
                            else if (columns.getString(l).equals("humidity")){
                                influxMeasurement.humidity = value.getDouble(l);
                            }
                        }
                    }
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return influxMeasurement;
    }
}

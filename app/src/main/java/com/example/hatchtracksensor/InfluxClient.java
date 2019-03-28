package com.example.hatchtracksensor;

import android.util.Log;
import com.amazonaws.util.IOUtils;
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
        public long unixTimestamp;
        public float temperature;
        public float humidity;

        public InfluxMeasurement() {
            unixTimestamp = 0;
            temperature = 0;
            humidity = 0;
        }
    }

    public InfluxClient(String user, String password, String url, String name) {
        mUser = user;
        mPassword = password;
        mdbURL = url;
        mdbName = name;
    }

    public InfluxMeasurement getMeasurement(String uuid) {
        InfluxMeasurement influxMeasurement = new InfluxMeasurement();

        try {
            String influxQuery = "";
            String uriQuery = "";

            influxQuery += "SELECT * FROM peep ";
            influxQuery += "WHERE peep_uuid='" + uuid + "' ";
            influxQuery += "GROUP BY * ORDER BY ASC LIMIT 1";

            uriQuery += "?u=" + mUser;
            uriQuery += "&p=" + mPassword;
            uriQuery += "&db=" + mdbName;
            uriQuery += "&q=" + URLEncoder.encode(influxQuery, "UTF-8");

            String u = mdbURL + "/query" + uriQuery;
            URL url = new URL(u);
            HttpURLConnection urlConnection  = (HttpURLConnection) url.openConnection();
            InputStream in = new BufferedInputStream(urlConnection.getInputStream());
            parseMeasurement(in);
            urlConnection.disconnect();
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return influxMeasurement;
    }

    private void parseMeasurement(InputStream in) {
        try {
            String data = IOUtils.toString(in);
            Log.i("MREUTMAN!!!!!!!!!!!!!!!", data);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}

package com.example.hatchtracksensor;

import android.util.JsonReader;

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

public class RestApi {
    private static final String HEADER_ACCESS_TOKEN = "Access-Token";

    public static String postUserAuth(String email, String password) {
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
            accessToken = json.getString("accessToken");
        } catch (Exception e) {
            e.printStackTrace();
        }

        return accessToken;
    }

    public static boolean postUserNewPeep(String accessToken, PeepUnit peepUnit) {
        String peepUUID = peepUnit.getUUID();
        boolean status = true;

        try {
            JSONObject json = new JSONObject();
            json.put("peepUUID", peepUUID);
            String body = json.toString();
            String requestURL = "https://db.hatchtrack.com:18888/api/v1/user/peep";
            URL url = new URL(requestURL);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(15000);
            conn.setConnectTimeout(15000);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty(HEADER_ACCESS_TOKEN, accessToken);
            conn.setDoOutput(true);

            OutputStream out = conn.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out, "UTF-8"));
            writer.write(body);

            writer.flush();
            writer.close();
            out.close();

            int code = conn.getResponseCode();
            if (200 == code) {
                status = true;
            } else {
                status = false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return status;
    }

    public static String getPeepName(String accessToken, PeepUnit peepUnit) {
        String peepUUID = peepUnit.getUUID();
        String peepName = "";

        try {
            String requestURL = "https://db.hatchtrack.com:18888/api/v1/peep/name?peepUUID=" + peepUUID;
            URL url = new URL(requestURL);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(15000);
            conn.setConnectTimeout(15000);
            conn.setRequestMethod("GET");
            conn.setRequestProperty(HEADER_ACCESS_TOKEN, accessToken);
            conn.setDoInput(true);

            InputStream in = new BufferedInputStream(conn.getInputStream());
            String data = IOUtils.toString(in);
            JSONObject json = new JSONObject(data);
            try {
                peepName = json.getString("peepName");
            } catch (Exception e) {
                peepName = "N/A";
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return peepName;
    }

    public static boolean postPeepName(String accessToken, PeepUnit peepUnit) {
        boolean status = true;

        try {
            JSONObject json = new JSONObject();
            json.put("peepUUID", peepUnit.getUUID());
            json.put("peepName", peepUnit.getName());
            String body = json.toString();
            String requestURL = "https://db.hatchtrack.com:18888/api/v1/peep/name";
            URL url = new URL(requestURL);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(15000);
            conn.setConnectTimeout(15000);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty(HEADER_ACCESS_TOKEN, accessToken);
            conn.setDoOutput(true);

            OutputStream out = conn.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out, "UTF-8"));
            writer.write(body);

            writer.flush();
            writer.close();
            out.close();

            int code = conn.getResponseCode();
            if (200 == code) {
                status = true;
            } else {
                status = false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return status;
    }

    public static ArrayList<String> getPeepUUIDs(String accessToken) {
        ArrayList<String> list = new ArrayList<String>();
        try {
            String reqURL = "https://db.hatchtrack.com:18888/api/v1/user/peeps";
            URL url = new URL(reqURL);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(15000);
            conn.setConnectTimeout(15000);
            conn.setRequestMethod("GET");
            conn.setRequestProperty(HEADER_ACCESS_TOKEN, accessToken);
            conn.setDoInput(true);

            InputStream in = new BufferedInputStream(conn.getInputStream());
            String  data= IOUtils.toString(in);
            JSONObject json = new JSONObject(data);
            JSONArray array = json.getJSONArray("peepUUIDs");
            for (int i = 0; i < array.length(); i++) {
                list.add(array.getString(i));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return  list;
    }

    public static ArrayList<String> getHatchUUIDs(String accessToken, PeepUnit peepUnit) {
        ArrayList<String> list = new ArrayList<String>();
        String peepUUID = peepUnit.getUUID();

        try {
            String reqURL = "https://db.hatchtrack.com:18888/api/v1/peep/hatches?peepUUID=" + peepUUID;
            URL url = new URL(reqURL);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(15000);
            conn.setConnectTimeout(15000);
            conn.setRequestMethod("GET");
            conn.setRequestProperty(HEADER_ACCESS_TOKEN, accessToken);
            conn.setDoInput(true);

            InputStream in = new BufferedInputStream(conn.getInputStream());
            String  data= IOUtils.toString(in);
            JSONObject json = new JSONObject(data);
            JSONArray array = json.getJSONArray("hatchUUIDs");
            for (int i = 0; i < array.length(); i++) {
                list.add(array.getString(i));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    public static PeepHatch getHatch(String accessToken, String hatchUUID) {
        PeepHatch peepHatch = new PeepHatch();

        try {
            String reqURL = "https://db.hatchtrack.com:18888/api/v1/hatch?hatchUUID=" + hatchUUID;
            URL url = new URL(reqURL);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(15000);
            conn.setConnectTimeout(15000);
            conn.setRequestMethod("GET");
            conn.setRequestProperty(HEADER_ACCESS_TOKEN, accessToken);
            conn.setDoInput(true);

            InputStream in = new BufferedInputStream(conn.getInputStream());
            String data= IOUtils.toString(in);
            JSONObject json = new JSONObject(data);
            peepHatch.setUUID(hatchUUID);
            peepHatch.setStartUnixTimestamp(json.getLong("startUnixTimestamp"));
            peepHatch.setEndUnixTimestamp(json.getLong("endUnixTimestamp"));
            peepHatch.setMeasureIntervalMin(json.getInt("measureIntervalMin"));
            peepHatch.setTemperatureOffsetCelsius(json.getInt("temperatureOffsetCelsius"));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return peepHatch;
    }

    public static boolean postNewPeepHatch(String accessToken, PeepUnit peepUnit, PeepHatch peepHatch) {
        boolean status = true;

        try {
            String peepUUID = peepUnit.getUUID();
            long endUnixTimestamp = peepHatch.getEndUnixTimestamp();
            int measureIntervalMin = peepHatch.getMeasureIntervalMin();
            int temperatureOffsetCelsius = peepHatch.getTemperatureOffsetCelsius();

            JSONObject json = new JSONObject();
            json.put("peepUUID", peepUUID);
            json.put("endUnixTimestamp", endUnixTimestamp);
            json.put("measureIntervalMin", measureIntervalMin);
            json.put("temperatureOffsetCelsius", temperatureOffsetCelsius);
            String body = json.toString();

            String requestURL = "https://db.hatchtrack.com:18888/api/v1/peep/hatch";
            URL url = new URL(requestURL);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(15000);
            conn.setConnectTimeout(15000);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty(HEADER_ACCESS_TOKEN, accessToken);
            conn.setDoInput(true);
            conn.setDoOutput(true);

            OutputStream out = conn.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out, "UTF-8"));
            writer.write(body);

            writer.flush();
            writer.close();
            out.close();

            int code = conn.getResponseCode();
            if (200 == code) {
                status = true;
            } else {
                status = false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            status = false;
        }

        return status;
    }
}

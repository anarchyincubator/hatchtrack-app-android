package com.example.hatchtracksensor;

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
    private static final String HEADER_ACCESS_TOKEN = "access-token";

    public static JSONObject getPeepName(String accessToken, String peepUUID) {
        JSONObject json = null;

        try {
            String requestURL = "https://db.hatchtrack.com:18888/api/v1/uuid2info?uuid=" + peepUUID;
            URL url = new URL(requestURL);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(15000);
            conn.setConnectTimeout(15000);
            conn.setRequestMethod("GET");
            conn.setRequestProperty(HEADER_ACCESS_TOKEN, accessToken);
            conn.setDoInput(true);

            InputStream in = new BufferedInputStream(conn.getInputStream());
            String  data= IOUtils.toString(in);
            json = new JSONObject(data);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return json;
    }

    public static ArrayList<String> getPeepUUIDs(String accessToken, String userEmail) {
        ArrayList<String> list = new ArrayList<String>();
        try {
            String reqURL = "https://db.hatchtrack.com:18888/api/v1/email2uuids?email=" +
                    userEmail;
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
            JSONArray array = json.getJSONArray("peep_uuids");
            for (int i = 0; i < array.length(); i++) {
                list.add(array.getString(i));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return  list;
    }

    public static JSONObject getPeepHatchInfo(String accessToken, String peepUUID) {
        JSONObject json = null;

        try {
            String reqURL = "https://db.hatchtrack.com:18888/api/v1/uuid2hatch?uuid=" + peepUUID;
            URL url = new URL(reqURL);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(15000);
            conn.setConnectTimeout(15000);
            conn.setRequestMethod("GET");
            conn.setRequestProperty(HEADER_ACCESS_TOKEN, accessToken);
            conn.setDoInput(true);

            InputStream in = new BufferedInputStream(conn.getInputStream());
            String  data= IOUtils.toString(in);
            json = new JSONObject(data);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return json;
    }

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

    public static boolean postNewPeepHatch(String accessToken, PeepUnit peepUnit) {
        boolean status = true;

        try {
            String peepUUID = peepUnit.getUUID();
            String peepName = peepUnit.getName();
            String email = peepUnit.getUserEmail();
            long endUnixTimestamp = peepUnit.getEndUnixTimestamp();
            int measureIntervalMin = peepUnit.getMeasureIntervalMin();

            JSONObject json = new JSONObject();
            json.put("email", email);
            json.put("peepUUID", peepUUID);
            json.put("peepName", peepName);
            json.put("endUnixTimestamp", endUnixTimestamp);
            json.put("measureIntervalMin", measureIntervalMin);
            String body = json.toString();

            String requestURL = "https://db.hatchtrack.com:18888/api/v1/hatch";
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

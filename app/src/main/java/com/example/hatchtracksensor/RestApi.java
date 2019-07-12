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
import android.util.Log;

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
            json.put("peepUUID", peepUUID.replace("\n", ""));
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

    public static boolean deleteUserPeep(String accessToken, PeepUnit peepUnit) {
        String peepUUID = peepUnit.getUUID();
        boolean status = true;

        try {
            String requestURL = "https://db.hatchtrack.com:18888/api/v1/user/peep?peepUUID=" + peepUUID;
            URL url = new URL(requestURL);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(15000);
            conn.setConnectTimeout(15000);
            conn.setRequestMethod("DELETE");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty(HEADER_ACCESS_TOKEN, accessToken);

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
            json.put("peepUUID", peepUnit.getUUID().replace("\n", ""));
            json.put("peepName", peepUnit.getName().replace("\n", ""));
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
            String data = IOUtils.toString(in);
            JSONObject json = new JSONObject(data);
            peepHatch.setUUID(hatchUUID);
            peepHatch.setStartUnixTimestamp(json.getLong("startUnixTimestamp"));
            peepHatch.setEndUnixTimestamp(json.getLong("endUnixTimestamp"));
            peepHatch.setMeasureIntervalMin(json.getInt("measureIntervalMin"));
            peepHatch.setTemperatureOffsetCelsius((float) json.getDouble("temperatureOffsetCelsius"));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return peepHatch;
    }

    public static boolean postHatchEnd(String accessToken, PeepHatch peepHatch) {
        boolean status = false;

        try {
            JSONObject json = new JSONObject();
            json.put("hatchUUID", peepHatch.getUUID().replace("\n", ""));
            String body = json.toString();

            String requestURL = "https://db.hatchtrack.com:18888/api/v1/hatch/end";
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

    public static boolean postNewPeepHatch(String accessToken, PeepUnit peepUnit, PeepHatch peepHatch) {
        boolean status = true;

        try {
            String peepUUID = peepUnit.getUUID();
            long endUnixTimestamp = peepHatch.getEndUnixTimestamp();
            int measureIntervalMin = peepHatch.getMeasureIntervalMin();
            float temperatureOffsetCelsius = peepHatch.getTemperatureOffsetCelsius();

            JSONObject json = new JSONObject();
            json.put("peepUUID", peepUUID.replace("\n", ""));
            json.put("endUnixTimestamp", endUnixTimestamp);
            json.put("measureIntervalMin", measureIntervalMin);
            json.put("temperatureOffsetCelsius", temperatureOffsetCelsius);
            String body = json.toString();

            Log.i("postNewPeepHatch JSON: ", body);


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

    public static boolean postReconfigHatch(String accessToken, PeepHatch peepHatch) {
        boolean status = true;

        try {
            String hatchUUID = peepHatch.getUUID();
            long endUnixTimestamp = peepHatch.getEndUnixTimestamp();
            int measureIntervalMin = peepHatch.getMeasureIntervalMin();
            float temperatureOffsetCelsius = peepHatch.getTemperatureOffsetCelsius();

            JSONObject json = new JSONObject();
            json.put("hatchUUID", hatchUUID);
            json.put("endUnixTimestamp", endUnixTimestamp);
            json.put("measureIntervalMin", measureIntervalMin);
            json.put("temperatureOffsetCelsius", temperatureOffsetCelsius);
            String body = json.toString();

            String requestURL = "https://db.hatchtrack.com:18888/api/v1/hatch/reconfig";
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

    public static PeepMeasurement getPeepLastMeasure(String accessToken, PeepUnit peepUnit) {
        PeepMeasurement peepMeasurement;
        String peepUUID = peepUnit.getUUID();

        try {
            String reqURL = "https://db.hatchtrack.com:18888/api/v1/peep/measure/last?peepUUID=" + peepUUID;
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
            peepMeasurement = new PeepMeasurement(
                    json.getString("hatchUUID"),
                    json.getLong("unixTimestamp"),
                    json.getDouble("humidity"),
                    json.getDouble("temperature")
            );
        } catch (Exception e) {
            e.printStackTrace();
            peepMeasurement = new PeepMeasurement(
                    "n/a",
                    0,
                    0,
                    0);
        }

        return peepMeasurement;
    }

    public static boolean postToggleSwitch(String accessToken, boolean SwitchTempTooHotState, boolean SwitchTempTooColdState, boolean SwitchHumidityOverState, boolean SwitchHumidityUnderState) {
        boolean status = true;

        try {


            JSONObject json = new JSONObject();
            json.put("SwitchTempTooHotState", SwitchTempTooHotState);
            json.put("SwitchTempTooColdState", SwitchTempTooColdState);
            json.put("SwitchHumidityOverState", SwitchHumidityOverState);
            json.put("SwitchHumidityUnderState", SwitchHumidityUnderState);
            String body = json.toString();

            String requestURL = "https://db.hatchtrack.com:18888/api/v1/user/notification_settings";
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

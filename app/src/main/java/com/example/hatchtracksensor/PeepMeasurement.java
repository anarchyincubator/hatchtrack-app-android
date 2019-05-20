package com.example.hatchtracksensor;

public class PeepMeasurement {

    private String mHatchUUID;
    private long mUnixTimestamp;
    private int mHumidity;
    private int mTemperature;

    public PeepMeasurement(String hatchUUID, long unixTimestamp, int humidity, int temperature) {
        mHatchUUID = hatchUUID;
        mUnixTimestamp = unixTimestamp;
        mHumidity = humidity;
        mTemperature = temperature;
    }

    public String getHatchUUID() { return mHatchUUID; }

    public long getUnixTimestamp() { return mUnixTimestamp; }

    public int getmHumidity() { return mHumidity; }

    public int getTemperature() { return mTemperature; }
}

package com.example.hatchtracksensor;

public class PeepMeasurement {

    private String mHatchUUID;
    private long mUnixTimestamp;
    private double mHumidity;
    private double mTemperature;

    public PeepMeasurement(String hatchUUID, long unixTimestamp, double humidity, double temperature) {
        mHatchUUID = hatchUUID;
        mUnixTimestamp = unixTimestamp;
        mHumidity = humidity;
        mTemperature = temperature;
    }

    public String getHatchUUID() { return mHatchUUID; }

    public long getUnixTimestamp() { return mUnixTimestamp; }

    public double getmHumidity() { return mHumidity; }

    public double getTemperature() { return mTemperature; }
}

package com.example.hatchtracksensor;

public class PeepHatch {
    private String mUUID;
    private long mStartUnixTimestamp;
    private long mEndUnixTimestamp;
    private int mMeasureIntervalMin;
    private float mTemperatureOffsetCelsius;

    public String getUUID() { return mUUID; }

    public long getStartUnixTimestamp() { return mStartUnixTimestamp; }

    public long getEndUnixTimestamp() { return mEndUnixTimestamp; }

    public int getMeasureIntervalMin() { return mMeasureIntervalMin; }

    public float getTemperatureOffsetCelsius() { return mTemperatureOffsetCelsius; }

    public void setUUID(String uuid) { mUUID = uuid; }

    public void setStartUnixTimestamp(long unixTimestamp) { mStartUnixTimestamp = unixTimestamp; }

    public void setEndUnixTimestamp(long unixTimestamp) { mEndUnixTimestamp = unixTimestamp; }

    public void setMeasureIntervalMin(int intervalMin) { mMeasureIntervalMin = intervalMin; }

    public void setTemperatureOffsetCelsius(float offset) { mTemperatureOffsetCelsius = offset; }
}

package com.example.hatchtracksensor;

public class PeepHatch {
    private String mUUID;
    private long mStartUnixTimestamp;
    private long mEndUnixTimestamp;
    private int mMeasureIntervalMin;
    private float mTemperatureOffsetCelsius;
    private int mEggCount;
    private String mSpeciesUUID;
    private String mHatchName;
    private int mHatchedCount;
    private String mHatchNotes;

    public String getUUID() { return mUUID; }

    public long getStartUnixTimestamp() { return mStartUnixTimestamp; }

    public long getEndUnixTimestamp() { return mEndUnixTimestamp; }

    public int getMeasureIntervalMin() { return mMeasureIntervalMin; }

    public float getTemperatureOffsetCelsius() { return mTemperatureOffsetCelsius; }

    public int getEggCount(){ return mEggCount; }

    public String getHatchNotes(){ return mHatchNotes; }

    public int getHatchedCount(){ return mHatchedCount; }

    public String getSpeciesUUID(){ return mSpeciesUUID; }

    public String getHatchName(){ return mHatchName; }

    public void setUUID(String uuid) { mUUID = uuid; }

    public void setStartUnixTimestamp(long unixTimestamp) { mStartUnixTimestamp = unixTimestamp; }

    public void setEndUnixTimestamp(long unixTimestamp) { mEndUnixTimestamp = unixTimestamp; }

    public void setMeasureIntervalMin(int intervalMin) { mMeasureIntervalMin = intervalMin; }

    public void setTemperatureOffsetCelsius(float offset) { mTemperatureOffsetCelsius = offset; }

    public void setEggCount(int eggCount) { mEggCount = eggCount; }

    public void setSpeciesUUID(String speciesUUID) { mSpeciesUUID = speciesUUID; }

    public void setHatchName(String hatchName) { mHatchName = hatchName; }

    public void setHatchedCount(int hatchedCount) { mHatchedCount = hatchedCount; }

    public void setHatchNotes(String hatchNotes) { mHatchNotes = hatchNotes; }
}

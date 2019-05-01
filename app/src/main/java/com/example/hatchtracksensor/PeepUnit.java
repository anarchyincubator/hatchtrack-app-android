package com.example.hatchtracksensor;

public class PeepUnit {
    private String mName;
    private String mUUID;
    private String mHatchUUID;
    private String mUserEmail;
    private String mUserPassword;
    private long mEndUnixTimestamp;
    private int mMeasureIntervalMin;
    private int mTemperatureOffset;
    private int mTemperatureUnits;

    public static final int PEEP_UNIT_TEMPERATURE_FAHRENHEIT = 1;
    public static final int PEEP_UNIT_TEMPERATURE_CELSIUS = 2;

    public PeepUnit() {
        mEndUnixTimestamp = 0;
        mMeasureIntervalMin = 15;
        mTemperatureUnits = PEEP_UNIT_TEMPERATURE_FAHRENHEIT;
    }

    public PeepUnit(String userEmail, String userPassword, String uuid, String name) {
        mUserEmail = userEmail;
        mUserPassword = userPassword;
        mUUID = uuid;
        mName = name;
        mEndUnixTimestamp = 0;
        mMeasureIntervalMin = 15;
        mTemperatureOffset = 0;
        mTemperatureUnits = PEEP_UNIT_TEMPERATURE_FAHRENHEIT;
    }

    public String getName() { return mName; }

    public String getUUID() { return mUUID; }

    public String getHatchUUID() { return mHatchUUID; }

    public String getUserEmail() { return mUserEmail; }

    public String getUserPassword() { return  mUserPassword; }

    public long getEndUnixTimestamp() { return mEndUnixTimestamp; }

    public int getMeasureIntervalMin() { return mMeasureIntervalMin; }

    public int getTemperatureOffset() { return mTemperatureOffset; }

    public int getTemperatureUnits() { return mTemperatureUnits; }

    public void setName(String name) { mName = name; }

    public void setUUID(String uuid) { mUUID = uuid; }

    public void setHatchUUID(String uuid) { mHatchUUID = uuid; }

    public void setUserEmail(String email) { mUserEmail = email; }

    public void setUserPassword(String password) { mUserPassword = password; }

    public void setEndUnixTimestamp(long unixTimestamp) { mEndUnixTimestamp = unixTimestamp; }

    public void setMeasureIntervalMin(int intervalMin) { mMeasureIntervalMin = intervalMin; }

    public void setTemperatureOffset(int offset) { mTemperatureOffset = offset; }

    public void setTemperatureUnits(int temperatureUnits) {
        if (temperatureUnits == PEEP_UNIT_TEMPERATURE_CELSIUS) {
            mTemperatureUnits = PEEP_UNIT_TEMPERATURE_CELSIUS;
        } else {
            mTemperatureUnits = PEEP_UNIT_TEMPERATURE_FAHRENHEIT;
        }
    }
}

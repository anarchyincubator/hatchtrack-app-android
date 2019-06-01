package com.example.hatchtracksensor;

public class PeepUnit {
    private String mName;
    private String mUUID;
    private String mUserEmail;
    private String mUserPassword;
    private PeepHatch mHatch;
    private PeepMeasurement mMeasurement;

    public PeepUnit() {
        mName = "none";
        mHatch = null;
    }

    public PeepUnit(String userEmail, String userPassword, String uuid) {
        mUserEmail = userEmail;
        mUserPassword = userPassword;
        mUUID = uuid;
        mName = "none";
        mHatch = null;
        mMeasurement = null;
    }

    public PeepUnit(String userEmail, String userPassword, String uuid, String name) {
        mUserEmail = userEmail;
        mUserPassword = userPassword;
        mUUID = uuid;
        mName = name;
        mHatch = null;
        mMeasurement = null;
    }

    public String getName() { return mName; }

    public String getUUID() { return mUUID; }

    public PeepHatch getLastHatch() { return mHatch; }

    public String getUserEmail() { return mUserEmail; }

    public String getUserPassword() { return  mUserPassword; }

    public PeepMeasurement getMeasurement() { return mMeasurement; }

    public void setName(String name) { mName = name; }

    public void setUUID(String uuid) { mUUID = uuid; }

    public void setHatch(PeepHatch hatch) { mHatch = hatch; }

    public void setMeasurement(PeepMeasurement measurement) { mMeasurement = measurement; }

    public void setUserEmail(String email) { mUserEmail = email; }

    public void setUserPassword(String password) { mUserPassword = password; }
}

package com.example.hatchtracksensor;

import java.util.ArrayList;

public class PeepUnit {
    private String mName;
    private String mUUID;
    private String mUserEmail;
    private String mUserPassword;
    private PeepHatch mHatch;

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
    }

    public PeepUnit(String userEmail, String userPassword, String uuid, String name) {
        mUserEmail = userEmail;
        mUserPassword = userPassword;
        mUUID = uuid;
        mName = name;
        mHatch = null;
    }

    public String getName() { return mName; }

    public String getUUID() { return mUUID; }

    public PeepHatch getLastHatch() { return mHatch; }

    public String getUserEmail() { return mUserEmail; }

    public String getUserPassword() { return  mUserPassword; }

    public void setName(String name) { mName = name; }

    public void setUUID(String uuid) { mUUID = uuid; }

    public void setHatch(PeepHatch hatch) { mHatch = hatch; }

    public void setUserEmail(String email) { mUserEmail = email; }

    public void setUserPassword(String password) { mUserPassword = password; }
}

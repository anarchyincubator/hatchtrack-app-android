package com.example.hatchtracksensor;

public class PeepUnit {
    private String mName;
    private String mUUID;
    private String mUserEmail;
    private String mUserPassword;

    public PeepUnit(String userEmail, String userPassword, String uuid, String name) {
        mUserEmail = userEmail;
        mUserPassword = userPassword;
        mUUID = uuid;
        mName = name;
    }

    public String getName() {
        return mName;
    }

    public String getUUID() {
        return mUUID;
    }

    public String getUserEmail() { return mUserEmail; }

    public String getUserPassword() { return  mUserPassword; }

    public void setName(String name) {
        mName = name;
    }

    public void setUUID(String uuid) {
        mUUID = uuid;
    }

    public void setUserEmail(String email) { mUserEmail = email; }

    public void setUserPassword(String password) { mUserPassword = password; }
}

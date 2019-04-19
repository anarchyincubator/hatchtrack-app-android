package com.example.hatchtracksensor;

public class PeepUnit {
    private String mName;
    private String mUUID;

    public PeepUnit(String uuid, String name) {
        mUUID = uuid;
        mName = name;
    }

    public String getName() {
        return mName;
    }

    public String getUUID() {
        return mUUID;
    }

    public void setName(String name) {
        mName = name;
    }

    public void setUUID(String uuid) {
        mUUID = uuid;
    }
}

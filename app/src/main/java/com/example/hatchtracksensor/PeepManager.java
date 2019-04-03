package com.example.hatchtracksensor;

import java.util.ArrayList;

public class PeepManager {

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

    private static ArrayList<PeepUnit> mPeepList = new ArrayList<PeepUnit>();
    private static int mActivePeepIndex = 0;

    public PeepManager() {
        if (mPeepList.isEmpty()) {
            /*
             * TODO: Create database the holds user's Peeps. For now, we just grab the two Peeps
             * TODO: that are configured for our demo purposes.
             */
            mActivePeepIndex = 0;

            mPeepList.add(
                    new PeepUnit("425e11b3-5844-4626-b05a-219d9751e5ca", "Peep 1"));
            mPeepList.add(
                    new PeepUnit("86559e4a-c115-4412-a8b3-b0f54486a18c", "Peep 2"));
        }
    }

    public String[] getPeepNames() {
        String names[] = new String[mPeepList.size()];
        PeepUnit unit;

        for (int i = 0; i < mPeepList.size(); i++) {
            unit = mPeepList.get(i);
            names[i] = unit.getName();
        }

        return names;
    }

    public PeepUnit[] getPeepUnits() {
        PeepUnit units[] = new PeepUnit[mPeepList.size()];
        PeepUnit unit;

        for (int i = 0; i < mPeepList.size(); i++) {
            unit = mPeepList.get(i);
            units[i].setName(unit.getName());
            units[i].setUUID(unit.getUUID());
        }

        return units;
    }

    public int getPeepUnitCount() {
        return mPeepList.size();
    }

    public void setPeepUnitActive(int i) {
        if ((i >= 0) && (i < mPeepList.size())) {
            mActivePeepIndex = i;
        }
    }

    public PeepUnit getPeepUnitActive() {
        PeepUnit unit = mPeepList.get(mActivePeepIndex);
        return unit;
    }
}

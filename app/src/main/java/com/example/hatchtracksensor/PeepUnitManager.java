package com.example.hatchtracksensor;

import java.util.ArrayList;

public class PeepUnitManager {

    private static ArrayList<PeepUnit> mPeepList = new ArrayList<PeepUnit>();
    private static int mActivePeepIndex = 0;

    public PeepUnitManager() {
        if (mPeepList.isEmpty()) {
            mActivePeepIndex = 0;
        }
    }

    public void setPeepUnits(ArrayList<PeepUnit> peepUnits) {
        mActivePeepIndex = 0;
        mPeepList = new ArrayList<PeepUnit>();
        for (int i = 0; i < peepUnits.size(); i++) {
            mPeepList.add(peepUnits.get(i));
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

    public void addPeepUnit(PeepUnit peepUnit) {
        mPeepList.add(0, peepUnit);
    }

    public PeepUnit getPeepUnit(int index) {
        if (index < mPeepList.size()) {
            return mPeepList.get(index);
        }
        else {
            return null;
        }
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

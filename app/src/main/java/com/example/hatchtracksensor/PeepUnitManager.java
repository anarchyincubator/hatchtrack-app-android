package com.example.hatchtracksensor;

import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class PeepUnitManager {

    public static ArrayList<PeepUnit> mPeepList = new ArrayList<PeepUnit>();
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

    public String[] getPeepHatches() {
        String hatches[] = new String[mPeepList.size()];
        PeepUnit unit;

        for (int i = 0; i < mPeepList.size(); i++) {
            unit = mPeepList.get(i);

            Date dateStart = new java.util.Date();
            Date dateEnd = new java.util.Date();
            Date dateCurr = new java.util.Date();
            try{
                //current = json.getJSONObject(i);
                //Log.i("current",current.toString());
                dateStart = new java.util.Date(unit.getLastHatch().getStartUnixTimestamp()*1000L);
                dateEnd = new java.util.Date(unit.getLastHatch().getEndUnixTimestamp()*1000L);
            }catch(Exception e){

            }
            SimpleDateFormat hourTime = new SimpleDateFormat("M/d", Locale.ENGLISH);
            String localTimeStart = hourTime.format(dateStart);
            String localTimeEnd = hourTime.format(dateEnd);
            if(dateEnd.compareTo(dateCurr)>0)localTimeEnd = "In Progress";
            String startEnd = localTimeStart + " - " + localTimeEnd;

            hatches[i] = startEnd;
        }

        return hatches;
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
//PeepUnit[]
    public ArrayList<PeepUnit> getPeepUnits() {
        /*
        PeepUnit units[] = new PeepUnit[mPeepList.size()];
        PeepUnit unit;

        for (int i = 0; i < mPeepList.size(); i++) {
            unit = mPeepList.get(i);
            units[i].setName(unit.getName());
            units[i].setUUID(unit.getUUID());
        }

        return units;
        */
        return mPeepList;
    }

    public int getPeepUnitCount() {
        return mPeepList.size();
    }

    public void setPeepUnitActive(int i) {
        Log.i("setPeepUnitActive","setPeepUnitActive "+i);
        if ((i >= 0) && (i < mPeepList.size())) {
            mActivePeepIndex = i;
        }
    }

    public PeepUnit getPeepUnitActive() {
        PeepUnit unit = mPeepList.get(mActivePeepIndex);
        Log.i("GETPeepUnitActive","GETPeepUnitActive "+unit.getUUID());
        return unit;
    }
}

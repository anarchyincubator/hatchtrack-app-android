package com.example.hatchtracksensor;

public class SettingsManager {

    enum TemperatureUnits {
        FAHRENHEIT, CELSIUS
    }

    private static TemperatureUnits mTemperatureUnits = TemperatureUnits.FAHRENHEIT;

    public SettingsManager() {

    }

    public void setTemperatureUnits(TemperatureUnits units) {
        mTemperatureUnits = units;
    }

    public TemperatureUnits getTemperatureUnits() {
        return mTemperatureUnits;
    }
}

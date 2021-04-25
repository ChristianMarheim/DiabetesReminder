package com.example.diabetesreminder;

import android.content.SharedPreferences;

import java.text.SimpleDateFormat;
import java.util.Date;

import static com.example.diabetesreminder.Constants.SENSOR_TIMESTAMP_KEY;
import static com.example.diabetesreminder.Constants.SPROYTE_TIMESTAMP_KEY;
import static com.example.diabetesreminder.Formatter.format;

public class Storage {

    private static boolean DEBUG;

    private static SharedPreferences preferences;

    public static void initialize(SharedPreferences preferences, boolean DEBUG){
        new Storage(preferences, DEBUG);
    }

    private Storage(){}

    private Storage(SharedPreferences preferences, boolean DEBUG) {
        this.preferences = preferences;
        Storage.DEBUG = DEBUG;
    }

    public static synchronized long getSensorTimestamp() {
        return preferences.getLong(SENSOR_TIMESTAMP_KEY, 0);
    }

    public static synchronized long getSproyteTimestamp() {
        return preferences.getLong(SPROYTE_TIMESTAMP_KEY, 0);
    }

    public static synchronized long getTimePassedSensor() {
        long longTimestampSensor = getSensorTimestamp();
        return new Date().getTime() - longTimestampSensor;
    }

    public static synchronized long getTimePassedSproyte() {
        long longTimestampSproyte = getSproyteTimestamp();
        return new Date().getTime() - longTimestampSproyte;
    }

    public static void saveInternal(String key, long value) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putLong(key, value);
        editor.commit();

        if(Long.MAX_VALUE == value){
            throw new RuntimeException("SHIT IS FACKED");
        }
    }

    public static String getDebugData() {
        if (!DEBUG)
            return "";
        return "\n\nDEBUG:\nsensor/raw=" + preferences.getLong(SENSOR_TIMESTAMP_KEY, 0) + "\n" +
                "sproyte/raw=" + preferences.getLong(SPROYTE_TIMESTAMP_KEY, 0) + "\n" +
                "sensor/date=" + new SimpleDateFormat().format(new Date(preferences.getLong(SENSOR_TIMESTAMP_KEY, 0))) + "\n" +
                "sproyte/date=" + format(new Date(preferences.getLong(SPROYTE_TIMESTAMP_KEY, 0))) + "\n" +
                "new Date()" + new SimpleDateFormat().format(new Date().getTime()) + "\n";
    }
}

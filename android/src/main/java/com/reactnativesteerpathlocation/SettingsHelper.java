package com.reactnativesteerpathlocation;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

import com.steerpath.sdk.location.LocationServices;

/**
 *
 * This is a helper class for getting and setting values from/to SharedPreferences.
 *
 */
public class SettingsHelper {

    private static SharedPreferences defaultPreferences;

    public SettingsHelper() {}

    public static void init(Context context) {
        defaultPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static void initGuideOptions(Context context) {
        LocationServices.getGuideOptions().accelerometer(defaultPreferences.getBoolean("accelerometer", true));
        LocationServices.getGuideOptions().compass(defaultPreferences.getBoolean("compass", false));
        LocationServices.getGuideOptions().gyroscope(defaultPreferences.getBoolean("gyroscope", false));
    }

    public static boolean useAccelerometer() {
        return defaultPreferences.getBoolean("accelerometer", true);
    }

    public static boolean useCompass() {
        return defaultPreferences.getBoolean("compass", false);
    }

    public static boolean useGyroscope() {
        return defaultPreferences.getBoolean("gyroscope", false);
    }

    public static boolean useMonitor() {
        return defaultPreferences.getBoolean("monitor", true);
    }

    public static boolean useAssetGateway() {
        return defaultPreferences.getBoolean("asset_gateway", false);
    }

    public static boolean useHighAccuracyPositioning() {
        return defaultPreferences.getBoolean("location_request", false);
    }
}

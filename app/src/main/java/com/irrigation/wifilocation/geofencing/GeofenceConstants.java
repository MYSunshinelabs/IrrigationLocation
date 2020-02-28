package com.irrigation.wifilocation.geofencing;

import com.google.android.gms.maps.model.LatLng;

import java.util.HashMap;

/**
 * GeofenceConstants used in this sample.
 */

public class GeofenceConstants {
    private GeofenceConstants() {
    }

    private static final String PACKAGE_NAME = "com.google.android.gms.location.Geofence";
    static final String GEOFENCES_ADDED_KEY = PACKAGE_NAME + ".GEOFENCES_ADDED_KEY";
    /**
     * Used to set an expiration time for a geofence. After this amount of time Location Services
     * stops tracking the geofence.
     */
    private static final long GEOFENCE_EXPIRATION_IN_HOURS = 24*30;
    /**
     * For this sample, geofences expire after twelve hours.
     */
    static final long GEOFENCE_EXPIRATION_IN_MILLISECONDS =GEOFENCE_EXPIRATION_IN_HOURS * 60 * 60 * 1000;
    public static final float GEOFENCE_RADIUS_IN_METERS = 20; // 1 mile, 1.6 km

    /**
     * Map for storing information about airports in the San Francisco bay area.
     */
    public static final HashMap<String, LatLng> wifiLandmarks = new HashMap<>();

//    static {
//        // San Francisco International Airport.
//        BAY_AREA_LANDMARKS.put("Virtual Employee I 15", new LatLng(28.543846, 77.402609));
//        // Googleplex.
//        BAY_AREA_LANDMARKS.put("location 2", new LatLng(28.542500, 77.402287));
//    }
}
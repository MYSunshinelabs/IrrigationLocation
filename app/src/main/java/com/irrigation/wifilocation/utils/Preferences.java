package com.irrigation.wifilocation.utils;

import android.content.Context;
import android.content.SharedPreferences;
import static android.content.Context.MODE_PRIVATE;

/**
 * Created by dalvendrakumar on 23/10/18.
 */

public class Preferences {
    private static final String PREFERENCE_NAME="Irrigation Preference";


    public static void setCurrentNetworkName(Context context, String currentNetWorkName){
        SharedPreferences mSharedPreferences = context.getSharedPreferences(PREFERENCE_NAME, MODE_PRIVATE);
        mSharedPreferences.edit().putString("current_network_name",currentNetWorkName).commit();
    }

    public static String getCurrentNetworkName(Context context){
        SharedPreferences mSharedPreferences = context.getSharedPreferences(PREFERENCE_NAME, MODE_PRIVATE);
        return mSharedPreferences.getString("current_network_name", "");
    }


    public static void setAutoConnectEnable(Context context, boolean isEnable){
        SharedPreferences mSharedPreferences = context.getSharedPreferences(PREFERENCE_NAME, MODE_PRIVATE);
        mSharedPreferences.edit().putBoolean("auto_connect_enable",isEnable).commit();
    }

    public static boolean getAutoConnectEnable(Context context){
        SharedPreferences mSharedPreferences = context.getSharedPreferences(PREFERENCE_NAME, MODE_PRIVATE);
        return mSharedPreferences.getBoolean("auto_connect_enable",false);
    }


    public static void setHotSpotRoomEnable(Context context, boolean isEnable){
        SharedPreferences mSharedPreferences = context.getSharedPreferences(PREFERENCE_NAME, MODE_PRIVATE);
        mSharedPreferences.edit().putBoolean("hotspot_room_enable",isEnable).commit();
    }

    public static boolean getHotSpotRoomEnable(Context context){
        SharedPreferences mSharedPreferences = context.getSharedPreferences(PREFERENCE_NAME, MODE_PRIVATE);
        return mSharedPreferences.getBoolean("hotspot_room_enable",false);
    }

    public static void setTrackingMode(Context context, boolean trackingMode){
        SharedPreferences mSharedPreferences = context.getSharedPreferences(PREFERENCE_NAME, MODE_PRIVATE);
        mSharedPreferences.edit().putBoolean("tracking_mode",trackingMode).commit();
    }

    public static boolean getTrackingMode(Context context){
        SharedPreferences mSharedPreferences = context.getSharedPreferences(PREFERENCE_NAME, MODE_PRIVATE);
        return mSharedPreferences.getBoolean("tracking_mode",false);
    }


    public static void setTrackingUrl(Context context, String trackingUrl){
        SharedPreferences mSharedPreferences = context.getSharedPreferences(PREFERENCE_NAME, MODE_PRIVATE);
        mSharedPreferences.edit().putString("tracking_url",trackingUrl).commit();
    }

    public static String getTrackingUrl(Context context){
        SharedPreferences mSharedPreferences = context.getSharedPreferences(PREFERENCE_NAME, MODE_PRIVATE);
        return mSharedPreferences.getString("tracking_url", "");
    }

    public static void setTrackingPort(Context context, String port){
        SharedPreferences mSharedPreferences = context.getSharedPreferences(PREFERENCE_NAME, MODE_PRIVATE);
        mSharedPreferences.edit().putString("tracking_port",port).commit();
    }

    public static String getTrackingUrlPort(Context context){
        SharedPreferences mSharedPreferences = context.getSharedPreferences(PREFERENCE_NAME, MODE_PRIVATE);
        return mSharedPreferences.getString("tracking_port", "");
    }


    public static void setTrackingPeriod(Context context, String trackingPeriod){
        SharedPreferences mSharedPreferences = context.getSharedPreferences(PREFERENCE_NAME, MODE_PRIVATE);
        mSharedPreferences.edit().putString("tracking_period",trackingPeriod).commit();
    }

    public static String getTrackingUrlPeriod(Context context){
        SharedPreferences mSharedPreferences = context.getSharedPreferences(PREFERENCE_NAME, MODE_PRIVATE);
        return mSharedPreferences.getString("tracking_period", "120");
    }

    public static void setTimeOut(Context context, String timeOut){
        SharedPreferences mSharedPreferences = context.getSharedPreferences(PREFERENCE_NAME, MODE_PRIVATE);
        mSharedPreferences.edit().putString("TimeOut",timeOut).commit();
    }

    public static String getTimeOut(Context context){
        SharedPreferences mSharedPreferences = context.getSharedPreferences(PREFERENCE_NAME, MODE_PRIVATE);
        return mSharedPreferences.getString("TimeOut", "30");
    }

    public static void setLatitude(Context context, String latitude){
        SharedPreferences mSharedPreferences = context.getSharedPreferences(PREFERENCE_NAME, MODE_PRIVATE);
        mSharedPreferences.edit().putString("latitude",latitude).commit();
    }

    public static String getLatitude(Context context){
        SharedPreferences mSharedPreferences = context.getSharedPreferences(PREFERENCE_NAME, MODE_PRIVATE);
        return mSharedPreferences.getString("latitude", "");
    }

    public static void setLongtitude(Context context, String longtitude){
        SharedPreferences mSharedPreferences = context.getSharedPreferences(PREFERENCE_NAME, MODE_PRIVATE);
        mSharedPreferences.edit().putString("longtitude",longtitude).commit();
    }

    public static String getLongtitude(Context context){
        SharedPreferences mSharedPreferences = context.getSharedPreferences(PREFERENCE_NAME, MODE_PRIVATE);
        return mSharedPreferences.getString("longtitude", "");
    }


    public static void setLocationString(Context context, String location){
        SharedPreferences mSharedPreferences = context.getSharedPreferences(PREFERENCE_NAME, MODE_PRIVATE);
        mSharedPreferences.edit().putString("string_location",location).commit();
    }

    public static String getLocationString(Context context){
        SharedPreferences mSharedPreferences = context.getSharedPreferences(PREFERENCE_NAME, MODE_PRIVATE);
        return mSharedPreferences.getString("string_location", "");
    }
    public static void setSatelliteCount(Context context, String longtitude){
        SharedPreferences mSharedPreferences = context.getSharedPreferences(PREFERENCE_NAME, MODE_PRIVATE);
        mSharedPreferences.edit().putString("Satellite_Count",longtitude).commit();
    }

    public static String getSatelliteCount(Context context){
        SharedPreferences mSharedPreferences = context.getSharedPreferences(PREFERENCE_NAME, MODE_PRIVATE);
        return mSharedPreferences.getString("Satellite_Count", "");
    }

    public static void setSSID(Context context, String longtitude){
        SharedPreferences mSharedPreferences = context.getSharedPreferences(PREFERENCE_NAME, MODE_PRIVATE);
        mSharedPreferences.edit().putString("_ssid",longtitude).commit();
    }

    public static String getSSID(Context context){
        SharedPreferences mSharedPreferences = context.getSharedPreferences(PREFERENCE_NAME, MODE_PRIVATE);
        return mSharedPreferences.getString("_ssid", "");
    }


    public static void setInGeofencePeriod(Context context, String inGeofencePeriod){
        SharedPreferences mSharedPreferences = context.getSharedPreferences(PREFERENCE_NAME, MODE_PRIVATE);
        mSharedPreferences.edit().putString("geofence_update_period",inGeofencePeriod).commit();
    }

    public static String getInGeofencePeriod(Context context){
        SharedPreferences mSharedPreferences = context.getSharedPreferences(PREFERENCE_NAME, MODE_PRIVATE);
        return mSharedPreferences.getString("geofence_update_period", "120");
    }

    public static void setOutsideGeofencePeriod(Context context, String outsideGeofencePeriod){
        SharedPreferences mSharedPreferences = context.getSharedPreferences(PREFERENCE_NAME, MODE_PRIVATE);
        mSharedPreferences.edit().putString("outside_geofence_update_period",outsideGeofencePeriod).commit();
    }

    public static String getOutsideGeofencePeriod(Context context){
        SharedPreferences mSharedPreferences = context.getSharedPreferences(PREFERENCE_NAME, MODE_PRIVATE);
        return mSharedPreferences.getString("outside_geofence_update_period", "20");
    }

    public static void setGeofenceRadius(Context context, String outsideGeofencePeriod){
        SharedPreferences mSharedPreferences = context.getSharedPreferences(PREFERENCE_NAME, MODE_PRIVATE);
        mSharedPreferences.edit().putString("geofence_radius",outsideGeofencePeriod).commit();
    }

    public static String getGeofenceRadius(Context context){
        SharedPreferences mSharedPreferences = context.getSharedPreferences(PREFERENCE_NAME, MODE_PRIVATE);
        return mSharedPreferences.getString("geofence_radius", "50");
    }

    public static void setGpsTracking(Context context, boolean isGpsTracking){
        SharedPreferences mSharedPreferences = context.getSharedPreferences(PREFERENCE_NAME, MODE_PRIVATE);
        mSharedPreferences.edit().putBoolean("isGpsTracking",isGpsTracking).commit();
    }

    public static Boolean isGpsTracking(Context context){
        SharedPreferences mSharedPreferences = context.getSharedPreferences(PREFERENCE_NAME, MODE_PRIVATE);
        return mSharedPreferences.getBoolean("isGpsTracking", false);
    }
}

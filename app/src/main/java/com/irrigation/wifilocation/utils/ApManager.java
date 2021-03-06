package com.irrigation.wifilocation.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;

import com.irrigation.wifilocation.widget.NetworkWidgetProviderVersion2;

import java.lang.reflect.Method;

/*App manger class*/
public class ApManager {
    public static final String TAG = "ApManager";
    private final WifiManager mWifiManager;
    private static ApManager apManager;
    private Context context;
    private WifiManager.LocalOnlyHotspotReservation mReservation;
    private final int REQUEST_ENABLE_LOCATION_SYSTEM_SETTINGS = 101;

    private ApManager(Context context) {
        this.context = context;
        mWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
    }

    public static ApManager newInstance(Context context){
        if(apManager==null)
            apManager= new ApManager(context);
        return apManager;
    }

    public void showWritePermissionSettings(boolean force) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (force || !Settings.System.canWrite(this.context)) {
                Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_WRITE_SETTINGS);
                intent.setData(Uri.parse("package:" + this.context.getPackageName()));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                this.context.startActivity(intent);
            }
        }
    }

    //  check whether wifi hotspot on or off
    public boolean isApOn() {
        try {
            Method method = mWifiManager.getClass().getDeclaredMethod("isWifiApEnabled");
            method.setAccessible(true);
            return (Boolean) method.invoke(mWifiManager);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // Turn wifiAp hotspot on
    public boolean turnWifiApOn() {
        WifiConfiguration wificonfiguration = null;
        try {
            Method method = mWifiManager.getClass().getMethod("setWifiApEnabled", WifiConfiguration.class, boolean.class);
            method.invoke(mWifiManager, wificonfiguration, true);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // Turn wifiAp hotspot off
    public boolean turnWifiApOff() {
        WifiConfiguration wificonfiguration = null;
        try {
            Method method = mWifiManager.getClass().getMethod("setWifiApEnabled", WifiConfiguration.class, boolean.class);
            method.invoke(mWifiManager, null, false);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean createNewNetwork(String ssid, String password) {
        mWifiManager.setWifiEnabled(false); // turn off Wifi
        WifiConfiguration myConfig = new WifiConfiguration();
        myConfig.SSID = ssid; // SSID name of netwok
        myConfig.preSharedKey = password; // password for network
        myConfig.allowedKeyManagement.set(4); // 4 is for KeyMgmt.WPA2_PSK which is not exposed by android KeyMgmt class
        myConfig.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN); // Set Auth Algorithms to open
        try {
            Method method = mWifiManager.getClass().getMethod("setWifiApEnabled", WifiConfiguration.class, boolean.class);
            Preferences.setSSID(context,ssid+"\n key=:"+password);
            return (Boolean) method.invoke(mWifiManager, myConfig, true);  // setting and turing on android wifiap with new configrations
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean isLocationPermissionEnable() {
        try {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 2);
                return false;
            }
            return true;
        }catch (Exception e){
            Utils.printLog("isLocationPermissionEnable",e.getMessage());
        }
        return false;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void turnOnHotspot() {
        if (!isLocationPermissionEnable()) {
            return;
        }
        WifiManager manager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        try{
            if (manager != null) {
                // Don't start when it started (existed)
                manager.startLocalOnlyHotspot(new WifiManager.LocalOnlyHotspotCallback() {
                    @Override
                    public void onStarted(WifiManager.LocalOnlyHotspotReservation reservation) {
                        super.onStarted(reservation);
                        //Log.d(TAG, "Wifi Hotspot is on now");
                        mReservation = reservation;
                        String key = mReservation.getWifiConfiguration().preSharedKey;
                        String ussid = mReservation.getWifiConfiguration().SSID;
                        Preferences.setSSID(context,ussid+"\n key=:"+key);
                        NetworkWidgetProviderVersion2.CONNECTED_TO=0;
                        NetworkWidgetProviderVersion2.updateWidget(context);
                        Utils.printLog("","Key="+key+"  "+ussid);
                    }

                    @Override
                    public void onStopped() {
                        super.onStopped();
                        //Log.d(TAG, "onStopped: ");
                    }

                    @Override
                    public void onFailed(int reason) {
                        super.onFailed(reason);
                        //Log.d(TAG, "onFailed: ");
                    }
                }, new Handler());
            }
        }catch (Exception e){
            e.printStackTrace();
        }



    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void turnOffHotspot() {
        if (!isLocationPermissionEnable()) {
            return;
        }
        if (mReservation != null) {
            mReservation.close();
        }
    }


}
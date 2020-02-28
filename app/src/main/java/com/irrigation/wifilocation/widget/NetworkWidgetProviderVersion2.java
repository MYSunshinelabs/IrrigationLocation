package com.irrigation.wifilocation.widget;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.widget.RemoteViews;

import com.google.android.gms.maps.model.LatLng;
import com.irrigation.wifilocation.R;
import com.irrigation.wifilocation.activities.ConfigActivity;
import com.irrigation.wifilocation.activities.EditWifiListActivity;
import com.irrigation.wifilocation.activities.SplashActivity;
import com.irrigation.wifilocation.db.AppDataBase;
import com.irrigation.wifilocation.db.schema.Wifi;
import com.irrigation.wifilocation.geofencing.GeofenceConstants;
import com.irrigation.wifilocation.utils.ApManager;
import com.irrigation.wifilocation.utils.Preferences;
import com.irrigation.wifilocation.utils.TimeOutManager;
import com.irrigation.wifilocation.utils.Utils;
import com.irrigation.wifilocation.utils.WifiHelper;

import java.util.ArrayList;
import java.util.List;
import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

public class NetworkWidgetProviderVersion2 extends AppWidgetProvider {
    private static final String CLICK_AUTO_CONNECT = "action_auto_connect_click";
    private static final String CLICK_HOTSPOT_ROAM = "action_hotspot_roam_click";
    private static final String CLICK_CRUD = "action_crud_click";
    private static final String CLICK_HOTSPOT= "action_config_click";
    private static final String CLICK_HOME = "action_home_click";
    private static final String CLICK_OFFICE1 = "action_office_1_click";
    private static final String CLICK_OFFICE2 = "action_office_2_click";
    public static final String ACTION_TIMEOUT="action_time_out";
    public static int CONNECTED_TO = -0;
    public static final int HOME = 1;
    public static final int OFFICE1 = 2;
    public static final int OFFICE2 = 3;
    public static final int SECONDARY_WIFI_LOCATIONS= 4;
    public static final int HOTSPOT= 5;
    public static List<Wifi> wifis = new ArrayList<>();

    protected PendingIntent getPendingSelfIntent(Context context, String action) {
        Intent intent = new Intent(context, getClass());
        intent.setAction(action);
        return PendingIntent.getBroadcast(context, 0, intent, 0);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        boolean isUiUpdateNeeded = false;
        if(!Preferences.getAutoConnectEnable(context) && !(intent.getAction().equals(CLICK_AUTO_CONNECT)))
            return;

        switch (intent.getAction()) {
            case CLICK_AUTO_CONNECT:
                startAutoConnect(context);
                isUiUpdateNeeded = true;
                break;

            case ACTION_TIMEOUT:
                startAutoConnect(context);
                isUiUpdateNeeded=true;
                TimeOutManager.clearTimeOut(context);
                break;

            case CLICK_HOTSPOT_ROAM:
                try {
                    CONNECTED_TO=0;
                    boolean isHotspotRoomEnable = !Preferences.getHotSpotRoomEnable(context);
                    Preferences.setHotSpotRoomEnable(context, isHotspotRoomEnable);
                    isUiUpdateNeeded = true;
                } catch (Exception e) {
                    Utils.printLog("CLICK_HOTSPOT_ROAM", e.toString());
                }
                break;
            case CLICK_CRUD:
                try {
                    Intent wifiListIntent = new Intent(context, SplashActivity.class);
                    intent.setFlags(FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(wifiListIntent);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;

//            case CLICK_HOTSPOT:
//                try {
//                    disableAutoConnect(context);
//                    openHotSpot(context, remoteViews, false);
//                    isUiUpdateNeeded=true;
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//                break;

            case CLICK_HOME:
                isUiUpdateNeeded = true;
                if (wifis.size() >= 1)
                    WifiHelper.connectTo(context, wifis.get(0));
                break;

            case CLICK_OFFICE1:
                isUiUpdateNeeded = true;
                if (wifis.size() >= 2)
                    WifiHelper.connectTo(context, wifis.get(1));
                break;

            case CLICK_OFFICE2:
                isUiUpdateNeeded = true;
                if (wifis.size() >= 3)
                    WifiHelper.connectTo(context, wifis.get(2));
                break;
        }

        if (isUiUpdateNeeded)
            onUpdate(context, AppWidgetManager.getInstance(context), AppWidgetManager.getInstance(context).getAppWidgetIds(new ComponentName(context, NetworkWidgetProviderVersion2.class)));
    }

    private void startAutoConnect(Context context){
        boolean isAutoConnectEnable = !Preferences.getAutoConnectEnable(context);
        CONNECTED_TO = 0;
        if (isAutoConnectEnable) {
            Preferences.setHotSpotRoomEnable(context,false);
            turnOffHotSpot(context);
            populateWifiList(context);
//            GeoFenceHelper.newInstance(context).addGeofences();
            Utils.startLocationService(context);
        }else {
            Utils.stopLocationService(context);
//            GeoFenceHelper.newInstance(context).removeGeofences();
        }

        Preferences.setAutoConnectEnable(context, isAutoConnectEnable);
    }
    @SuppressLint("MissingPermission")
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {

        super.onUpdate(context, appWidgetManager, appWidgetIds);

        populateWifiList(context);
        // Get all ids
        ComponentName thisWidget = new ComponentName(context, NetworkWidgetProviderVersion2.class);
        int[] allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.layout_widget_new);

        remoteViews.setOnClickPendingIntent(R.id.btnAuto, getPendingSelfIntent(context, CLICK_AUTO_CONNECT));
        remoteViews.setOnClickPendingIntent(R.id.btnHotSpotRoom, getPendingSelfIntent(context, CLICK_HOTSPOT_ROAM));
        remoteViews.setOnClickPendingIntent(R.id.btnHome, getPendingSelfIntent(context, CLICK_HOME));
        remoteViews.setOnClickPendingIntent(R.id.btnOffice1, getPendingSelfIntent(context, CLICK_OFFICE1));
        remoteViews.setOnClickPendingIntent(R.id.btnOffice2, getPendingSelfIntent(context, CLICK_OFFICE2));
        remoteViews.setOnClickPendingIntent(R.id.btnHotSpot, getPendingSelfIntent(context, CLICK_HOTSPOT));

        Intent intent = new Intent(context, EditWifiListActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
        remoteViews.setOnClickPendingIntent(R.id.btnCrud, pendingIntent);

        Intent intentConfig = new Intent(context, ConfigActivity.class);
        PendingIntent pendingIntentConfig = PendingIntent.getActivity(context, 0, intentConfig, 0);
        remoteViews.setOnClickPendingIntent(R.id.btnConfig, pendingIntentConfig);

        int iconAuto = context.getResources().getIdentifier("background_auto_mode", "drawable", context.getPackageName());
        int iconGray = context.getResources().getIdentifier("background_manual_gray", "drawable", context.getPackageName());
        int iconYellow = context.getResources().getIdentifier("background_manual_yellow", "drawable", context.getPackageName());
        int radioOn = context.getResources().getIdentifier("bg_radio_on", "drawable", context.getPackageName());
        int radioOff = context.getResources().getIdentifier("bg_radio_off", "drawable", context.getPackageName());


        if (Preferences.getHotSpotRoomEnable(context))
            remoteViews.setInt(R.id.btnHotSpotRoom, "setBackgroundResource", iconAuto);
        else
            remoteViews.setInt(R.id.btnHotSpotRoom, "setBackgroundResource", iconYellow);


        if (Preferences.getAutoConnectEnable(context)) {
            remoteViews.setTextViewText(R.id.btnAuto, "ON");
            remoteViews.setInt(R.id.btnAuto, "setBackgroundResource", iconAuto);
            remoteViews.setInt(R.id.radioGps, "setBackgroundResource", radioOn);
        } else {
            remoteViews.setInt(R.id.btnAuto, "setBackgroundResource", iconYellow);
            remoteViews.setTextViewText(R.id.btnAuto, "OFF");
        }

        if(Preferences.isGpsTracking(context))
            remoteViews.setInt(R.id.radioGps, "setBackgroundResource", radioOn);
        else
            remoteViews.setInt(R.id.radioGps, "setBackgroundResource", radioOff);


        remoteViews.setInt(R.id.btnHome, "setBackgroundResource", iconGray);
        remoteViews.setInt(R.id.btnOffice1, "setBackgroundResource", iconGray);
        remoteViews.setInt(R.id.btnOffice2, "setBackgroundResource", iconGray);
        remoteViews.setInt(R.id.btnHotSpot, "setBackgroundResource", iconGray);
        remoteViews.setInt(R.id.radioWifi, "setBackgroundResource", radioOff);
        remoteViews.setInt(R.id.radioHotspot, "setBackgroundResource", radioOff);

        remoteViews.setTextViewText(R.id.txtSSID, "");

        switch (CONNECTED_TO) {
            case HOME:
                try {
                    remoteViews.setInt(R.id.btnHome, "setBackgroundResource", iconAuto);
                    remoteViews.setInt(R.id.radioWifi, "setBackgroundResource", radioOn);
                    Preferences.setSSID(context, wifis.get(0).getSsid());
                    TimeOutManager.scheduleTimeOut(context);
                }catch (Exception e){}
                break;

            case OFFICE1:
                try {
                    remoteViews.setInt(R.id.btnOffice1, "setBackgroundResource", iconAuto);
                    remoteViews.setInt(R.id.radioWifi, "setBackgroundResource", radioOn);
                    Preferences.setSSID(context,wifis.get(1).getSsid());
                    TimeOutManager.scheduleTimeOut(context);
                }catch (Exception e){}
                break;

            case OFFICE2:
                try {
                    remoteViews.setInt(R.id.btnOffice2, "setBackgroundResource", iconAuto);
                    remoteViews.setInt(R.id.radioWifi, "setBackgroundResource", radioOn);
                    Preferences.setSSID(context,wifis.get(2).getSsid());
                    TimeOutManager.scheduleTimeOut(context);
                }catch (Exception e){}
                break;
            case HOTSPOT:
                remoteViews.setInt(R.id.radioHotspot, "setBackgroundResource", radioOn);
                break;

            case 0:
                if (Preferences.getHotSpotRoomEnable(context))
                    openHotSpot(context,remoteViews,true);
                else {
                    Preferences.setSSID(context,"");
                    turnOffHotSpot(context);
                }
                break;
        }

        remoteViews.setTextViewText(R.id.txtSSID, Preferences.getSSID(context));
        remoteViews.setTextViewText(R.id.txtLatLong, Preferences.getLocationString(context));
        remoteViews.setTextViewText(R.id.txtNoSatellites, Preferences.getSatelliteCount(context));
        appWidgetManager.updateAppWidget(allWidgetIds, remoteViews);
    }

    private void populateWifiList(final Context context){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    wifis.clear();
                    wifis= AppDataBase.getAppDataBase(context).wifiDAO().getAllWifi();
                    for (Wifi wifi: wifis) {
                        LatLng latLng=getLatLong(wifi.getLatitude(),wifi.getLongtitude());
                        if (latLng!=null)
                            GeofenceConstants.wifiLandmarks.put(wifi.getSsid(),getLatLong(wifi.getLatitude(),wifi.getLongtitude()));
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }).start();
    }


    private LatLng getLatLong(String latitude, String longtitude) {
        try{
            double lat= Double.parseDouble(latitude);
            double lon= Double.parseDouble(longtitude);
            return new LatLng(lat,lon);
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }


    private void turnOffHotSpot(Context context) {
        ApManager ap = ApManager.newInstance(context);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ap.turnOffHotspot();
        }else {
            ap.turnWifiApOff();
        }
    }

    private void disableAutoConnect(Context context){
        if(Preferences.getAutoConnectEnable(context)) {
            Preferences.setAutoConnectEnable(context, false);
        }
    }

    private void openHotSpot(Context context, RemoteViews remoteViews, boolean isRoamingCheck) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Build.VERSION.SDK_INT <= Build.VERSION_CODES.N_MR1) {
            if (Settings.System.canWrite(context)) {
                CreateNewWifiApNetwork(context,remoteViews);
            } else {
                Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
                intent.setData(Uri.parse("package:" + context.getPackageName()));
                intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }
        }
        CreateNewWifiApNetwork(context,remoteViews);
    }

    public void CreateNewWifiApNetwork(Context context,RemoteViews remoteViews) {
        int radioOn = context.getResources().getIdentifier("bg_radio_on", "drawable", context.getPackageName());
        ApManager ap = ApManager.newInstance(context);
        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.O)
            ap.turnOnHotspot();
        else
            ap.createNewNetwork("sun00","sun01234");
        remoteViews.setInt(R.id.radioHotspot, "setBackgroundResource", radioOn);
    }

    public static void updateWidget(Context context){
        Intent intent = new Intent(context, NetworkWidgetProviderVersion2.class);
        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        // Use an array and EXTRA_APPWIDGET_IDS instead of AppWidgetManager.EXTRA_APPWIDGET_ID,
        // since it seems the onUpdate() is only fired on that:
        int[] ids = AppWidgetManager.getInstance(context.getApplicationContext()).getAppWidgetIds(new ComponentName(context,NetworkWidgetProviderVersion2.class));
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
        context.sendBroadcast(intent);
    }

}

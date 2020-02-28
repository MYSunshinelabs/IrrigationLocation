package com.irrigation.wifilocation.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.irrigation.wifilocation.R;
import com.irrigation.wifilocation.activities.SplashActivity;
import com.irrigation.wifilocation.db.AppDataBase;
import com.irrigation.wifilocation.db.schema.Wifi;
import com.irrigation.wifilocation.geofencing.GeofenceConstants;
import com.irrigation.wifilocation.utils.ApManager;
import com.irrigation.wifilocation.utils.Constants;
import com.irrigation.wifilocation.utils.Preferences;
import com.irrigation.wifilocation.utils.Utils;
import com.irrigation.wifilocation.utils.WifiHelper;

import java.util.ArrayList;
import java.util.List;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

public class NetworkWidgetProvider extends AppWidgetProvider {
    private static final String CLICK_AUTO_CONNECT="action_auto_connect_click";
    private static final String CLICK_HOTSPOT_ROAM="action_hotspot_roam_click";
    private static final String CLICK_CRUD="action_crud_click";
    private static final String CLICK_HOT_SPOT="action_hot_spot_click";
    private static final String CLICK_SSID1="action_ssid_1_click";
    private static final String CLICK_SSID2="action_ssid_2_click";
    private static final String CLICK_SSID3="action_ssid_3_click";
    private static final String CLICK_SSID4="action_ssid_4_click";
    private static final String CLICK_SSID5="action_ssid_5_click";

    private static List<Wifi> wifis=new ArrayList<>();

    protected PendingIntent getPendingSelfIntent(Context context, String action) {
        Intent intent = new Intent(context, getClass());
        intent.setAction(action);
        return PendingIntent.getBroadcast(context, 0, intent, 0);
    }
    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);


        switch (intent.getAction()){
            case CLICK_AUTO_CONNECT:
                boolean isAutoConnectEnable=!Preferences.getAutoConnectEnable(context);
                if(isAutoConnectEnable) {
                    Utils.startLocationService(context);
                }else {
                    Utils.stopLocationService(context);
                }
                Preferences.setAutoConnectEnable(context,isAutoConnectEnable);
                onUpdate(context,AppWidgetManager.getInstance(context),AppWidgetManager.getInstance(context).getAppWidgetIds(new ComponentName(context,NetworkWidgetProvider.class)));
                break;
            case CLICK_HOTSPOT_ROAM:
                try{
                    boolean isHotspotRoomEnable=!Preferences.getHotSpotRoomEnable(context);
                    TelephonyManager telephonyManager=(TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

                    if(!isHotspotRoomEnable && telephonyManager.isNetworkRoaming())
                        turnOffHotSpot(context);

                    Preferences.setHotSpotRoomEnable(context,isHotspotRoomEnable);
                    onUpdate(context,AppWidgetManager.getInstance(context),AppWidgetManager.getInstance(context).getAppWidgetIds(new ComponentName(context,NetworkWidgetProvider.class)));

                }catch (Exception e){
                    Utils.printLog("CLICK_HOTSPOT_ROAM",e.toString());
                }
                break;
            case CLICK_CRUD:
                Intent wifiListIntent=new Intent(context, SplashActivity.class);
                intent.setFlags(FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(wifiListIntent);
                break;
            case CLICK_HOT_SPOT:
                openHotSpot(context);
                break;
            case CLICK_SSID1:
                if(wifis.size()>=1)
                    WifiHelper.connectTo(context,wifis.get(0));
                break;
            case CLICK_SSID2:
                if(wifis.size()>=2)
                    WifiHelper.connectTo(context,wifis.get(1));
                break;
            case CLICK_SSID3:
                if(wifis.size()>=3)
                    WifiHelper.connectTo(context,wifis.get(2));
                break;
            case CLICK_SSID4:
                if(wifis.size()>=4)
                    WifiHelper.connectTo(context,wifis.get(3));
                break;

            case CLICK_SSID5:
                if(wifis.size()>=5)
                    WifiHelper.connectTo(context,wifis.get(4));
                break;
            default:
                Utils.startLocationService(context);
        }
    }


    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);

        populateWifiList(context);
        // Get all ids
        ComponentName thisWidget = new ComponentName(context,NetworkWidgetProvider.class);
        int[] allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);

        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.layout_widget);

        remoteViews.setOnClickPendingIntent(R.id.btnAuto, getPendingSelfIntent(context, CLICK_AUTO_CONNECT));
        remoteViews.setOnClickPendingIntent(R.id.btnHotSpotRoom, getPendingSelfIntent(context, CLICK_HOTSPOT_ROAM));
        remoteViews.setOnClickPendingIntent(R.id.btnCrud, getPendingSelfIntent(context, CLICK_CRUD));
        remoteViews.setOnClickPendingIntent(R.id.btnHotSpot, getPendingSelfIntent(context, CLICK_HOT_SPOT));
        remoteViews.setOnClickPendingIntent(R.id.btnSSID1, getPendingSelfIntent(context, CLICK_SSID1));
        remoteViews.setOnClickPendingIntent(R.id.btnSSID2, getPendingSelfIntent(context, CLICK_SSID2));
        remoteViews.setOnClickPendingIntent(R.id.btnSSID3, getPendingSelfIntent(context, CLICK_SSID3));
        remoteViews.setOnClickPendingIntent(R.id.btnSSID4, getPendingSelfIntent(context, CLICK_SSID4));
        remoteViews.setOnClickPendingIntent(R.id.btnSSID5, getPendingSelfIntent(context, CLICK_SSID5));

        int listSize=wifis.size();
        for(int i=0;i<5;i++){
            if(i<listSize) {
                switch (i){
                    case 0:
                        remoteViews.setTextViewText(R.id.btnSSID1,wifis.get(i).getSsid());
                        break;
                    case 1:
                        remoteViews.setTextViewText(R.id.btnSSID2,wifis.get(i).getSsid());
                        break;
                    case 2:
                        remoteViews.setTextViewText(R.id.btnSSID3,wifis.get(i).getSsid());
                        break;
                    case 3:
                        remoteViews.setTextViewText(R.id.btnSSID4,wifis.get(i).getSsid());
                        break;
                    case 4:
                        remoteViews.setTextViewText(R.id.btnSSID5,wifis.get(i).getSsid());
                        break;
                }
            }else switch (i){
                case 0:
                    remoteViews.setTextViewText(R.id.btnSSID1,"LOC1 SSID");
                    break;
                case 1:
                    remoteViews.setTextViewText(R.id.btnSSID2,"LOC2 SSID");
                    break;
                case 2:
                    remoteViews.setTextViewText(R.id.btnSSID3,"LOC3 SSID");
                    break;
                case 3:
                    remoteViews.setTextViewText(R.id.btnSSID4,"LOC4 SSID");
                    break;
                case 4:
                    remoteViews.setTextViewText(R.id.btnSSID5,"LOC5 SSID");
                    break;
            }
        }

        int iconAuto = context.getResources().getIdentifier("background_auto_mode", "drawable", context.getPackageName());
        int iconManual= context.getResources().getIdentifier("background_manual_mode", "drawable", context.getPackageName());

        if(Preferences.getAutoConnectEnable(context))
            remoteViews.setInt(R.id.btnAuto,"setBackgroundResource",iconAuto);
        else
            remoteViews.setInt(R.id.btnAuto,"setBackgroundResource",iconManual);


        if(Preferences.getHotSpotRoomEnable(context))
            remoteViews.setInt(R.id.btnHotSpotRoom,"setBackgroundResource",iconAuto);
        else
            remoteViews.setInt(R.id.btnHotSpotRoom,"setBackgroundResource",iconManual);


        appWidgetManager.updateAppWidget(allWidgetIds, remoteViews);

        if(false) {
            // Create an Intent to launch ExampleActivity
            Intent intent = new Intent(context, SplashActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
        }
    }

    private void populateWifiList(final Context context){
        new Thread(new Runnable() {
            @Override
            public void run() {
                wifis.clear();
                wifis= AppDataBase.getAppDataBase(context).wifiDAO().getAllWifi();
                for (Wifi wifi: wifis) {
                    GeofenceConstants.wifiLandmarks.put(wifi.getSsid(),getLatLong(wifi.getLatitude(),wifi.getLongtitude()));
                }
            }
        }).start();
    }

    private LatLng getLatLong(String latitude, String longtitude) {
        double lat= Double.parseDouble(latitude);
        double lon= Double.parseDouble(longtitude);
        return new LatLng(lat,lon);
    }

    private void turnOffHotSpot(Context context) {
        ApManager ap = ApManager.newInstance(context);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ap.turnOffHotspot();
        }else {
            ap.turnWifiApOff();
        }

    }

    private void openHotSpot(Context context) {

        TelephonyManager telephonyManager=(TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        if(!Preferences.getHotSpotRoomEnable(context)&& telephonyManager.isNetworkRoaming()){
            Toast.makeText(context,"You are in Roaming ", Toast.LENGTH_SHORT).show();
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Build.VERSION.SDK_INT <= Build.VERSION_CODES.N_MR1) {
            if (Settings.System.canWrite(context)) {
                CreateNewWifiApNetwork(context);
            } else {
                Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_WRITE_SETTINGS);
                intent.setData(Uri.parse("package:" + context.getPackageName()));
                intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }
        }
        CreateNewWifiApNetwork(context);
    }

    public void CreateNewWifiApNetwork(Context context) {
        ApManager ap = ApManager.newInstance(context);
        ap.createNewNetwork("sun00","sun01234");

        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.O)
            ap.turnOnHotspot();
    }
}

package com.irrigation.wifilocation.utils;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Build;
import com.irrigation.wifilocation.db.schema.Wifi;
import com.irrigation.wifilocation.widget.NetworkWidgetProviderVersion2;

import java.util.List;

import static android.content.Context.WIFI_SERVICE;

public class WifiHelper {

    private static final String TAG=WifiHelper.class.getSimpleName();

    public static void connectTo(Context context, Wifi wifi){

        if(wifi.getSsid().equals(Preferences.getCurrentNetworkName(context)))
            return;

        turnOffHotSpot(context);

        WifiConfiguration wifiConfig = new WifiConfiguration();
        wifiConfig.SSID = String.format("\"%s\"", wifi.getSsid());
        wifiConfig.preSharedKey = String.format("\"%s\"", wifi.getPassword());
        //      For WEP connection
        wifiConfig.wepKeys[0] = String.format("\"%s\"", wifi.getPassword());
        wifiConfig.wepTxKeyIndex = 0;
        wifiConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
        wifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);

        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(WIFI_SERVICE);
        wifiConfig.priority = getHighestPriority(wifiManager)+1;

        int netId = wifiManager.addNetwork(wifiConfig);

        if(!wifiManager.isWifiEnabled())// Turn on wifi if not avilivable
            wifiManager.setWifiEnabled(true);

        disconnectCurrentNetwork(wifiManager);

        wifiManager.enableNetwork(netId, true);
        wifiManager.reconnect();
        Preferences.setLocationString(context,wifi.getLatitude()+", "+wifi.getLongtitude());
        Preferences.setSSID(context,wifi.getSsid());

        if(wifi.getTag().equals("Home")) {
            NetworkWidgetProviderVersion2.CONNECTED_TO = NetworkWidgetProviderVersion2.HOME;
        }else if(wifi.getTag().equals("Office1")){
            NetworkWidgetProviderVersion2.CONNECTED_TO = NetworkWidgetProviderVersion2.OFFICE1;
        }else if(wifi.getTag().equals("Office2")){
            NetworkWidgetProviderVersion2.CONNECTED_TO = NetworkWidgetProviderVersion2.OFFICE2;
        } else{
            NetworkWidgetProviderVersion2.CONNECTED_TO = NetworkWidgetProviderVersion2.SECONDARY_WIFI_LOCATIONS;
        }

        Preferences.setHotSpotRoomEnable(context,false);
        NetworkWidgetProviderVersion2.updateWidget(context);
        Utils.printLog(TAG, "Tring to connect with "+wifi.getSsid());

    }

    private static int getHighestPriority(WifiManager wifiManager) {
        int priority=0;
        List<WifiConfiguration> wifiConfigurations= wifiManager.getConfiguredNetworks();
        for (WifiConfiguration configuration: wifiConfigurations) {
            if(configuration.priority>priority)
                priority=configuration.priority;
        }
        return priority;
    }

    public static boolean disconnectCurrentNetwork(WifiManager wifiManager){
        if(wifiManager != null && wifiManager.isWifiEnabled()){
            int netId = wifiManager.getConnectionInfo().getNetworkId();
            wifiManager.disableNetwork(netId);
            return wifiManager.disconnect();
        }
        return false;
    }

    private static void turnOffHotSpot(Context context) {
        try{
            ApManager ap = ApManager.newInstance(context);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                ap.turnOffHotspot();
            }else {
                ap.turnWifiApOff();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static String getWifiSSID(Context context){
        WifiManager wifiManager= (WifiManager) context.getSystemService(WIFI_SERVICE);
        String ssid=wifiManager.getConnectionInfo().getSSID();
        try {
            ssid=ssid.replaceAll("\"|\"", "");
        }catch (Exception e){
            Utils.printLog(TAG+" getWifiSSID",e.toString());
        }
        return ssid;
    }

}

package com.irrigation.wifilocation.recivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.irrigation.wifilocation.utils.Preferences;
import com.irrigation.wifilocation.utils.Utils;


public class NetworkConnectivityReceiver extends BroadcastReceiver {
    private static final String ACTION_CONNECTIVITY_CHANGE="android.net.conn.CONNECTIVITY_CHANGE";
    private static final String TAG = NetworkConnectivityReceiver.class.getSimpleName();
    private final ConnectivityChangeObserver connectivityChangeObserver;

    public NetworkConnectivityReceiver(ConnectivityChangeObserver connectivityChangeObserver) {
        this.connectivityChangeObserver = connectivityChangeObserver;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        boolean isConnected = false;
        if (intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
            isConnected = (!intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false));
        }
        connectivityChangeObserver.onConnectivityChange(isConnected);
        NetworkInfo networkInfo= (NetworkInfo) intent.getExtras().get("networkInfo");
        String name=intent.getExtras().getString("extraInfo");
        try {
            name=name.replaceAll("\"|\"", "");
        }catch (Exception e){
            Utils.printLog(TAG,e.toString());
        }
        Preferences.setCurrentNetworkName(context,name);
        Utils.printLog(TAG,"onReceive=====>>> Connected "+isConnected+" "+name);

    }


    public interface ConnectivityChangeObserver {
        void onConnectivityChange(boolean isConnected);
    }

    public static NetworkConnectivityReceiver registerNetworkReciver(NetworkConnectivityReceiver.ConnectivityChangeObserver observer, Context context) {
        NetworkConnectivityReceiver cartChangeReceiver = new NetworkConnectivityReceiver(observer);
        context.registerReceiver(cartChangeReceiver, new IntentFilter(ACTION_CONNECTIVITY_CHANGE));
        return cartChangeReceiver;
    }

    public static void unregisterReceiver(NetworkConnectivityReceiver receiver, Context context){
        try{
            context.unregisterReceiver(receiver);
        }catch (Exception e){
            Utils.printLog(TAG,e.getMessage());
        }
    }
}
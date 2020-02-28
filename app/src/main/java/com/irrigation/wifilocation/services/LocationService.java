package com.irrigation.wifilocation.services;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Toast;

import com.irrigation.wifilocation.R;
import com.irrigation.wifilocation.activities.EditWifiListActivity;
import com.irrigation.wifilocation.db.schema.Wifi;
import com.irrigation.wifilocation.recivers.CurrentLocationReceiver;
import com.irrigation.wifilocation.recivers.NetworkConnectivityReceiver;
import com.irrigation.wifilocation.utils.LocationHelper;
import com.irrigation.wifilocation.utils.Preferences;
import com.irrigation.wifilocation.utils.Utils;
import com.irrigation.wifilocation.utils.WifiHelper;
import com.irrigation.wifilocation.widget.NetworkWidgetProviderVersion2;

import java.util.Iterator;

import static com.irrigation.wifilocation.services.LocationMonitoringJobService.isSendLocation;
import static com.irrigation.wifilocation.services.LocationMonitoringJobService.sendLocaiton;

/**
 * Created by Dalvendra on 15-03-2019.
 */

public class LocationService extends Service implements GpsStatus.Listener, LocationListener, NetworkConnectivityReceiver.ConnectivityChangeObserver {

    private static final String TAG = LocationService.class.getSimpleName();
    private boolean isCheckForWifi=true;
    public static boolean isStopService=false;
    private Intent intent;

    private LocationManager locationManager;
    private NetworkConnectivityReceiver receiver;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        this.intent=intent;
        //Make it stick to the notification panel so it is less prone to get cancelled by the Operating System.
        receiver=NetworkConnectivityReceiver.registerNetworkReciver(this,getBaseContext());
        return START_STICKY;
    }

    private void requestLocationUPdate(){
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            return ;
        }
        Long minTime=Long.parseLong(Preferences.getOutsideGeofencePeriod(getBaseContext()))*1000;
        Float minDis=1f;
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,minTime, 0, this);
        locationManager.addGpsStatusListener(this);

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
        isStopService=false;
        NetworkConnectivityReceiver.unregisterReceiver(receiver,getBaseContext());
        Utils.startLocationService(this);
    }

    @Override
    public void onGpsStatusChanged(int event) {
        Log.d(TAG, "onGpsStatusChanged event "+event);
        int size = 0;
        if (locationManager != null){
            @SuppressLint("MissingPermission") GpsStatus gpsStatus = locationManager.getGpsStatus(null);
            Iterator<GpsSatellite> iterator =gpsStatus.getSatellites().iterator();
            while (iterator.hasNext()) {
                iterator.next();
                size++;
            }
        }
        Preferences.setGpsTracking(getBaseContext(),true);
        if(!Preferences.getSatelliteCount(this).equals(size+"")){
            Preferences.setSatelliteCount(this,size+"");
            NetworkWidgetProviderVersion2.updateWidget(this);
        }
        Log.d(TAG, "onGpsStatusChanged Satellites Count : "+size);
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG,"onLocationChanged Latitude : "+location.getLatitude()+"  Longtitude : "+location.getLongitude());
        processLocation(location,getBaseContext());
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        Log.d(TAG,"onStatusChanged : "+provider+" status : "+status);
        Log.d(TAG,"satellites : "+extras.get("satellites")+" MaxCn0 : "+extras.get("maxCn0")+" MeanCn0 : "+extras.get("meanCn0"));
    }

    @Override
    public void onProviderEnabled(String provider) {
        Log.d(TAG,"onProviderEnabled : "+provider);
        requestLocationUPdate();
    }

    @Override
    public void onProviderDisabled(String provider) {
        Log.d(TAG,"onProviderDisabled : "+provider);
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(),getString(R.string.msg_disabled_provider_alert),Toast.LENGTH_SHORT).show();
            }
        });

    }


    private void processLocation(Location location,Context context){
        if (location != null) {
            CurrentLocationReceiver.sendBroadcast(context,location);
            EditWifiListActivity.latitude=location.getLatitude()+"";
            EditWifiListActivity.longtitude=location.getLongitude()+"";

            if(Preferences.getTrackingMode(context) && isSendLocation){
                sendLocaiton(context,location);
            }
            long delayTime= Long.parseLong(Preferences.getOutsideGeofencePeriod(context));

            Log.d(TAG, "location= "+location.getLatitude()+", "+location.getLongitude());
            //  Commented due to unused because of geofenceing
            if(isCheckForWifi && Preferences.getAutoConnectEnable(context)){
                isCheckForWifi=false;
                Wifi nearByWifi= LocationHelper.getNearByWifi(location,context);
                if (nearByWifi!=null) {
                    Utils.printLog(TAG, nearByWifi.getSsid());
                    WifiHelper.connectTo(context,nearByWifi);
                    delayTime= Long.parseLong(Preferences.getInGeofencePeriod(context));
                }
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        isCheckForWifi=true;
                    }
                },delayTime*1000);
            }
        }
    }

    @Override
    public void onConnectivityChange(boolean isConnected) {}
}
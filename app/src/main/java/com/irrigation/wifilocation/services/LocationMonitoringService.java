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
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.irrigation.wifilocation.activities.EditWifiListActivity;
import com.irrigation.wifilocation.db.schema.Wifi;
import com.irrigation.wifilocation.recivers.CurrentLocationReceiver;
import com.irrigation.wifilocation.utils.LocationHelper;
import com.irrigation.wifilocation.utils.Preferences;
import com.irrigation.wifilocation.utils.Utils;
import com.irrigation.wifilocation.utils.WifiHelper;
import com.irrigation.wifilocation.widget.NetworkWidgetProviderVersion2;

import java.util.Iterator;


/**
 * Created by Dalvendra on 15-03-2019.
 */

public class LocationMonitoringService extends Service implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener, GpsStatus.Listener {

    private static final String TAG = LocationMonitoringService.class.getSimpleName();
    private boolean isCheckForWifi=true;
    public static boolean isStopService=false;
    private GoogleApiClient mLocationClient;
    private LocationRequest mLocationRequest = new LocationRequest();


    Intent intent;

    private LocationManager locManager;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        this.intent=intent;

        mLocationClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mLocationRequest.setInterval(20000);
        mLocationRequest.setFastestInterval(10000);

        int priority = LocationRequest.PRIORITY_HIGH_ACCURACY; //by default
        //PRIORITY_BALANCED_POWER_ACCURACY, PRIORITY_LOW_POWER, PRIORITY_NO_POWER are the other priority modes
        mLocationRequest.setPriority(priority);
        mLocationClient.connect();

        //Make it stick to the notification panel so it is less prone to get cancelled by the Operating System.
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /*
     * LOCATION CALLBACKS
     */
    @Override
    public void onConnected(Bundle dataBundle) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            Log.d(TAG, "== Error On onConnected() Permission not granted");
            //Permission not granted by user so cancel the further execution.
            return;
        }
        try {
            LocationServices.FusedLocationApi.requestLocationUpdates(mLocationClient, mLocationRequest, this);
            locManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
            locManager.addGpsStatusListener(this);
//            locManager.requestLocationUpdates(" ",2000*10,5, (android.location.LocationListener) this);
        }catch (Exception e){
            e.printStackTrace();
        }

        Log.d(TAG, "Connected to Google API");
    }

    /*
     * Called by Location Services if the connection to the
     * location client drops because of an error.
     */
    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "Connection suspended");
    }

    //to get the location change
    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG, "Location changed");

        if(isStopService)
            stopService(intent);

        if (location != null) {
            CurrentLocationReceiver.sendBroadcast(getBaseContext(),location);

            EditWifiListActivity.latitude=location.getLatitude()+"";
            EditWifiListActivity.longtitude=location.getLongitude()+"";

            if(Preferences.getTrackingMode(getBaseContext()) && LocationMonitoringJobService.isSendLocation){
                LocationMonitoringJobService.sendLocaiton(this,location);
            }

            Log.d(TAG, "location= "+location.getLatitude()+", "+location.getLongitude());

            long delayTime= Long.parseLong(Preferences.getOutsideGeofencePeriod(getBaseContext()));

            if(isCheckForWifi && Preferences.getAutoConnectEnable(getBaseContext())){
                isCheckForWifi=false;
                Wifi nearByWifi= LocationHelper.getNearByWifi(location,getBaseContext());
                if (nearByWifi!=null) {
                    Utils.printLog(TAG, nearByWifi.getSsid());
                    WifiHelper.connectTo(getBaseContext(),nearByWifi);
                    delayTime= Long.parseLong(Preferences.getInGeofencePeriod(getBaseContext()));
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
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(TAG, "Failed to connect to Google API");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
//        if (mLocationClient.isConnected()) {
//            mLocationClient.disconnect();
//
//        }
        isStopService=false;
        Utils.startLocationService(this);
    }

    @Override
    public void onGpsStatusChanged(int event) {
        Log.d(TAG, "onGpsStatusChanged event "+event);
        int size = 0;
        if (locManager != null){
            @SuppressLint("MissingPermission") GpsStatus gpsStatus = locManager.getGpsStatus(null);
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
}
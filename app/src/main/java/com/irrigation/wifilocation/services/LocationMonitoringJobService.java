package com.irrigation.wifilocation.services;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.irrigation.wifilocation.R;
import com.irrigation.wifilocation.activities.EditWifiListActivity;
import com.irrigation.wifilocation.activities.SplashActivity;
import com.irrigation.wifilocation.db.schema.Wifi;
import com.irrigation.wifilocation.recivers.CurrentLocationReceiver;
import com.irrigation.wifilocation.utils.Constants;
import com.irrigation.wifilocation.utils.HttpUrlConnection;
import com.irrigation.wifilocation.utils.LocationHelper;
import com.irrigation.wifilocation.utils.Preferences;
import com.irrigation.wifilocation.utils.Utils;
import com.irrigation.wifilocation.utils.WifiHelper;
import com.irrigation.wifilocation.widget.NetworkWidgetProviderVersion2;

import org.json.JSONObject;

import java.util.Iterator;


/**
 * Created by Dalvendra on 15-03-2019.
 */

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class LocationMonitoringJobService extends JobService implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener, GpsStatus.Listener {

    private static final String TAG = LocationMonitoringJobService.class.getSimpleName();
    private static final String NOTIFICATION_CHANNEL_ID = "notificaton_channel_id";
    private static final String NOTIFICATION_CHANNEL_NAME = "Notification_channel_name";
    private static final String NOTIFICATION_CHANNEL_DESC= "Irrigation widget tracking you GPS Satellites Counts";
    private static final int NOTIFICATION_ID = 200;
    public static boolean isServiceStarted = false;
    private boolean isCheckForWifi = true;
    private GoogleApiClient mLocationClient;
    private LocationRequest mLocationRequest = new LocationRequest();
    private Location locationPrev;


    public static final String ACTION_LOCATION_BROADCAST = LocationMonitoringJobService.class.getName() + "LocationBroadcast";
    public static final String EXTRA_LATITUDE = "extra_latitude";
    public static final String EXTRA_LONGITUDE = "extra_longitude";
    private LocationManager locManager;
    public static boolean isSendLocation;

    @Override
    public boolean onStartJob(JobParameters params) {
        isServiceStarted = true;
        startInForeground();

        mLocationClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);

        int priority = LocationRequest.PRIORITY_HIGH_ACCURACY; //by default
        //PRIORITY_BALANCED_POWER_ACCURACY, PRIORITY_LOW_POWER, PRIORITY_NO_POWER are the other priority modes
        mLocationRequest.setPriority(priority);
        mLocationClient.connect();

        return false;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        isServiceStarted=false;
        Utils.startLocationService(this);
        return false;
    }

    /*
     * LOCATION CALLBACKS
     */
    @Override
    public void onConnected(Bundle dataBundle) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
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
        if (location != null) {
            CurrentLocationReceiver.sendBroadcast(getBaseContext(),location);
            EditWifiListActivity.latitude=location.getLatitude()+"";
            EditWifiListActivity.longtitude=location.getLongitude()+"";

            if(Preferences.getTrackingMode(getBaseContext()) && isSendLocation){
                sendLocaiton(this,location);
            }
            long delayTime= Long.parseLong(Preferences.getOutsideGeofencePeriod(getBaseContext()));

            Log.d(TAG, "location= "+location.getLatitude()+", "+location.getLongitude());
            //  Commented due to unused because of geofenceing
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

    public static void sendLocaiton(Context context,Location location) {
        isSendLocation=false;
        JSONObject objectLocation=new JSONObject();
        try {
            objectLocation.put("longitude",location.getLongitude());
            objectLocation.put("latitude",location.getLatitude());
            new HttpUrlConnection(new HttpUrlConnection.GetJSONListener() {
                @Override
                public void onRemoteCallComplete(String jsonFromNet) {
                    Log.d(TAG, "onRemoteCallComplete "+jsonFromNet);
                }
                @Override
                public void onSocketTimeOut() {
                    Log.d(TAG, "onSocketTimeOut");
                }
            },objectLocation).execute(Preferences.getTrackingUrl(context)+":"+Preferences.getTrackingUrlPort(context));

        }catch (Exception e){

        }
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                isSendLocation=true;
            }
        }, Long.parseLong(Preferences.getTrackingUrlPeriod(context))*1000);

    }

    private void sendMessageToUI(String lat, String lng) {
        Log.d(TAG, "Sending info...");
        Intent intent = new Intent(ACTION_LOCATION_BROADCAST);
        intent.putExtra(EXTRA_LATITUDE, lat);
        intent.putExtra(EXTRA_LONGITUDE, lng);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(TAG, "Failed to connect to Google API");
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



    private void startInForeground() {
        Intent notificationIntent = new Intent(this, SplashActivity.class);
        PendingIntent pendingIntent=PendingIntent.getActivity(this,0,notificationIntent,0);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this,NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(getString(R.string.app_name))
                .setContentText("Tracking location Satellites")
                .setTicker("TICKER")
                .setContentIntent(pendingIntent);
        Notification notification=builder.build();
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, NOTIFICATION_CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription(NOTIFICATION_CHANNEL_DESC);
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(channel);
        }
        startForeground(NOTIFICATION_ID, notification);
    }
}
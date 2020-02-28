package com.irrigation.wifilocation.services;

import android.Manifest;
import android.annotation.SuppressLint;
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
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.irrigation.wifilocation.R;
import com.irrigation.wifilocation.activities.EditWifiListActivity;
import com.irrigation.wifilocation.activities.SplashActivity;
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

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class LocationJobService extends JobService implements LocationListener, GpsStatus.Listener, NetworkConnectivityReceiver.ConnectivityChangeObserver {
    private final static  String TAG=LocationJobService.class.getSimpleName();
    private static final String NOTIFICATION_CHANNEL_ID = "notificaton_channel_id";
    private static final String NOTIFICATION_CHANNEL_NAME = "Notification_channel_name";
    private static final String NOTIFICATION_CHANNEL_DESC= "Irrigation widget tracking you GPS Satellites Counts";
    private static final int NOTIFICATION_ID = 200;
    private static boolean isCheckForWifi=true;
    private LocationManager locationManager;
    private NetworkConnectivityReceiver receiver;
    @Override
    public boolean onStartJob(JobParameters params) {
        requestLocationUPdate();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startInForeground();
        }
        receiver=NetworkConnectivityReceiver.registerNetworkReciver(this,getBaseContext());
        return false;
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
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, minTime, 0, this);
        locationManager.addGpsStatusListener(this);

    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return false;
    }

    @Override
    public void onDestroy() {
        NetworkConnectivityReceiver.unregisterReceiver(receiver,getBaseContext());
        super.onDestroy();
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG,"onLocationChanged Latitude : "+location.getLatitude()+"  Longtitude : "+location.getLongitude());
        processLocation(location,getBaseContext());
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

    @Override
    public void onGpsStatusChanged(int event) {
        Log.d(TAG, "onGpsStatusChanged event "+event);
        int size = 0;
        if (locationManager != null){
            @SuppressLint("MissingPermission")
            GpsStatus gpsStatus = locationManager.getGpsStatus(null);
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

    @Override
    public void onConnectivityChange(boolean isConnected) {

    }
}

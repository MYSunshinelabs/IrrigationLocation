package com.irrigation.wifilocation.recivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.util.Log;

import com.irrigation.wifilocation.utils.Utils;

/**
 * Created by dalvendrakumar on 3/12/18.
 */

public class CurrentLocationReceiver extends BroadcastReceiver {
    public static final String TAG= CurrentLocationReceiver.class.getSimpleName();
    public static final String ACTION_CURRENT_LOCATION="action_on_current_location_change";
    private LocationObserver locationObserver;

    public CurrentLocationReceiver(LocationObserver locationObserver) {
        this.locationObserver = locationObserver;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG+" onReceive"," location ");
        Location location=intent.getParcelableExtra("location");
        locationObserver.onNewLocation(location);
    }

    public interface LocationObserver {
        void onNewLocation(Location location);
    }

    public static CurrentLocationReceiver registerReceiver(LocationObserver locationObserver, Context context){
        Log.d(TAG+" registerReceiver",context.toString());
        CurrentLocationReceiver receiver=new CurrentLocationReceiver(locationObserver);
        context.registerReceiver(receiver,new IntentFilter(ACTION_CURRENT_LOCATION));
        return receiver;
    }

    public static void unregisterReceiver(CurrentLocationReceiver receiver, Context context){
        try{
            Log.d(TAG," unregisterReceiver");
            context.unregisterReceiver(receiver);
        }catch (Exception e){
            Utils.printLog(TAG,e.getMessage());
        }
    }

    public static void sendBroadcast(Context context, Location location){
        Intent intent=new Intent(ACTION_CURRENT_LOCATION);
        intent.putExtra("location",location);
        context.sendBroadcast(intent);
    }
}

package com.irrigation.wifilocation.utils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;

import com.irrigation.wifilocation.widget.NetworkWidgetProviderVersion2;

public class TimeOutManager {
    public static void scheduleTimeOut(Context context) {
        String interval =Preferences.getTimeOut(context);
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        long intervalMillis = Integer.parseInt(interval)*60*1000;
        PendingIntent pendingIntent = getAlarmIntent(context);
        pendingIntent.cancel();
        am.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,SystemClock.elapsedRealtime() +intervalMillis, pendingIntent);
    }
     
    private static PendingIntent getAlarmIntent(Context context) {
        Intent intent = new Intent(context, NetworkWidgetProviderVersion2.class);
        intent.setAction(NetworkWidgetProviderVersion2.ACTION_TIMEOUT);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
        return pendingIntent;
    }
 
    public static void clearTimeOut(Context context) {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        am.cancel(getAlarmIntent(context));
    }   
     
}
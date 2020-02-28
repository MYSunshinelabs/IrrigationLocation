package com.irrigation.wifilocation.utils;

import android.app.Activity;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.irrigation.wifilocation.BuildConfig;
import com.irrigation.wifilocation.services.LocationJobService;
import com.irrigation.wifilocation.services.LocationMonitoringJobService;
import com.irrigation.wifilocation.services.LocationMonitoringService;

/**
 * Created by dalvendrakumar on 27/03/19.
 */

public class Utils {

    public static void initDevieInfo(Activity activity){
        DisplayMetrics displayMetrics=new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getRealMetrics(displayMetrics);
        Constants.DEVICE_HEIGHT= displayMetrics.heightPixels;
        Constants.DEVICE_WIDTH = displayMetrics.widthPixels;
    }

    public static void startLocationService(Context context) {
        if (isDeviceConnected(context) || hasSimCard(context)) {
            if (LocationHelper.getInstance(context).hasPermission() && LocationHelper.getInstance(context).checkPlayServices()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    JobScheduler jobScheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
                    jobScheduler.schedule(new JobInfo.Builder(Constants.JobId.JOB_LOCATION,
                            new ComponentName(context, LocationJobService.class))
                            .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                            .build());
                    Intent intent = new Intent(context, LocationJobService.class);
                    context.startForegroundService(intent);
                } else {
                    LocationMonitoringService.isStopService = false;
                    Intent intent = new Intent(context, LocationMonitoringService.class);
                    context.startService(intent);
                }
            }
        } else {
            Toast.makeText(context,"Your must have connectivity",Toast.LENGTH_SHORT).show();
        }
    }

    private static boolean hasSimCard(Context context) {
        TelephonyManager telMgr = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        int simState = telMgr.getSimState();
        switch (simState) {
            case TelephonyManager.SIM_STATE_ABSENT:
                // do something
                return false;
            case TelephonyManager.SIM_STATE_NETWORK_LOCKED:
                // do something
                return true;
            case TelephonyManager.SIM_STATE_PIN_REQUIRED:
                // do something
                return true;
            case TelephonyManager.SIM_STATE_PUK_REQUIRED:
                // do something
                return true;
            case TelephonyManager.SIM_STATE_READY:
                // do something
                return true;
            case TelephonyManager.SIM_STATE_UNKNOWN:
                // do something
                return false;
        }
        return false;
    }

    public static void stopLocationService(Context context){
        if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.O){
            JobScheduler jobScheduler = (JobScheduler)context.getSystemService(Context.JOB_SCHEDULER_SERVICE );
            jobScheduler.cancel(Constants.JobId.JOB_LOCATION);
        }else {
            LocationMonitoringService.isStopService=true;
            Intent intent = new Intent(context, LocationMonitoringService.class);
            context.stopService(intent);
        }
    }

    //  To check device have internet conectivity or not
    public static boolean isDeviceConnected(Context context){
        ConnectivityManager cm =(ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null &&activeNetwork.isConnectedOrConnecting();
    }
    //  Get previous response from the Shared Preferences and parse it to genrate the Connection Source Data List
    //  Return black List if Shared Preference does not have previous response.


    //  To Print the log only when apk is in Debug mode.
    public static void printLog(String tag, String msg){
        if(BuildConfig.DEBUG && msg!=null)
            Log.d(tag+" ====>>>",msg);
    }

    public static void showSnackbar(View view, String msg){
        showSnackbar(view,msg,Snackbar.LENGTH_LONG);
    }

    public static void showSnackbar(View view, String msg, Integer length){
        Snackbar snackbar = Snackbar.make(view, msg, length);
        snackbar.show();
    }

    public static void setStatusBarColor(Activity activity, int colorId){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = activity.getWindow();
            // clear FLAG_TRANSLUCENT_STATUS flag:
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            // add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            // finally change the color
            window.setStatusBarColor(ContextCompat.getColor(activity,colorId));
        }
    }

    public static void sendLog(String log, Context context)
    {
        try{
            Utils.printLog("sendLog","sendLog ");
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType ("plain/text");
            intent.putExtra (Intent.EXTRA_EMAIL, new String[] {"dalvendrakumar@virtualemployee.com"});
            intent.putExtra (Intent.EXTRA_SUBJECT, "Irrigation app log");
            intent.putExtra (Intent.EXTRA_TEXT, log); // do this so some email clients don't complain about empty body.
            context.startActivity (intent);

        }catch (Exception e){

        }
    }



}

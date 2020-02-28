package com.irrigation.wifilocation.recivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import com.irrigation.wifilocation.utils.Utils;


public class UpdateWidgetReceiver extends BroadcastReceiver {
    private static final String ACTION_UPDATE="Widget Update needed";
    private static final String TAG = UpdateWidgetReceiver.class.getSimpleName();
    private final WidgetUpdateObserver  widgetUpdateObserver;

    public UpdateWidgetReceiver(WidgetUpdateObserver  widgetUpdateObserver) {
        this.widgetUpdateObserver = widgetUpdateObserver;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        widgetUpdateObserver.onUpdateRequired(context);
    }


    public interface WidgetUpdateObserver {
        void onUpdateRequired(Context context);
    }

    public static UpdateWidgetReceiver registerWidgetUpdateReciver(UpdateWidgetReceiver.WidgetUpdateObserver  observer, Context context) {
        UpdateWidgetReceiver cartChangeReceiver = new UpdateWidgetReceiver(observer);
        context.registerReceiver(cartChangeReceiver, new IntentFilter(ACTION_UPDATE));
        return cartChangeReceiver;
    }

    public static void unregisterReceiver(UpdateWidgetReceiver receiver, Context context){
        try{
            context.unregisterReceiver(receiver);
        }catch (Exception e){
            Utils.printLog(TAG,e.getMessage());
        }
    }

}
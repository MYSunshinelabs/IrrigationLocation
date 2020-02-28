package com.irrigation.wifilocation.activities;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;

import com.irrigation.wifilocation.R;
import com.irrigation.wifilocation.databinding.ActivityWifiListBinding;
import com.irrigation.wifilocation.db.AppDataBase;
import com.irrigation.wifilocation.recivers.NetworkConnectivityReceiver;
import com.irrigation.wifilocation.utils.ApManager;
import com.irrigation.wifilocation.utils.LocationHelper;
import com.irrigation.wifilocation.utils.Preferences;
import com.irrigation.wifilocation.utils.Utils;
import com.irrigation.wifilocation.utils.WifiHelper;


public class WifiListActivity extends BaseActivity implements View.OnClickListener, NetworkConnectivityReceiver.ConnectivityChangeObserver {
    private static final String TAG = WifiListActivity.class.getSimpleName();
    private ActivityWifiListBinding binding;
    private Button[] ssidBtn= new Button[5];
    private boolean isAutoConnectEnable,isHotspotRoomEnable;
    private NetworkConnectivityReceiver connectivityReceiver;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding= DataBindingUtil.setContentView(this, R.layout.activity_wifi_list);

        init();

        initClickListener();

        populateWifiList();

        LocationHelper.buildGoogleApiClient(this,this);

    }

    private void toggleSSIDBtn() {
        try{
            int listSize= LocationHelper.wifis.size();

            if(listSize<=0) {
                for(int i=0;i<5;i++)
                    ssidBtn[i].setVisibility(View.INVISIBLE);
                return;
            }

            for(int i=0;i<5;i++){
                if(i<listSize) {
                    ssidBtn[i].setText(LocationHelper.wifis.get(i).getSsid());
                    ssidBtn[i].setVisibility(View.VISIBLE);
                }else
                    ssidBtn[i].setVisibility(View.INVISIBLE);
            }
        }catch (Exception e){
            Utils.printLog(TAG,e.getMessage());
        }
    }

    private void populateWifiList(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                LocationHelper.wifis.clear();
                LocationHelper.wifis= AppDataBase.getAppDataBase(getApplicationContext()).wifiDAO().getAllWifi();
            }
        }).start();
    }

    private void init() {

        ssidBtn[0]=binding.btnSSID1;
        ssidBtn[1]=binding.btnSSID2;
        ssidBtn[2]=binding.btnSSID3;
        ssidBtn[3]=binding.btnSSID4;
        ssidBtn[4]=binding.btnSSID5;

        isAutoConnectEnable= Preferences.getAutoConnectEnable(this);
        isHotspotRoomEnable= Preferences.getHotSpotRoomEnable(this);


        if(isAutoConnectEnable)
            binding.btnAuto.setBackgroundResource(R.drawable.background_auto_mode);
        else
            binding.btnAuto.setBackgroundResource(R.drawable.background_manual_mode);

        if(isHotspotRoomEnable)
            binding.btnHotSpotRoom.setBackgroundResource(R.drawable.background_auto_mode);
        else
            binding.btnHotSpotRoom.setBackgroundResource(R.drawable.background_manual_mode);

        connectivityReceiver= NetworkConnectivityReceiver.registerNetworkReciver(this,this);
    }

    private void initClickListener() {
        binding.btnAuto.setOnClickListener(this);
        binding.btnHotSpotRoom.setOnClickListener(this);
        binding.btnCrud.setOnClickListener(this);
        binding.btnHotSpot.setOnClickListener(this);

        binding.btnSSID1.setOnClickListener(this);
        binding.btnSSID2.setOnClickListener(this);
        binding.btnSSID3.setOnClickListener(this);
        binding.btnSSID4.setOnClickListener(this);
        binding.btnSSID5.setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        toggleSSIDBtn();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btnAuto:
                isAutoConnectEnable=!isAutoConnectEnable;
                if(isAutoConnectEnable) {
                    binding.btnAuto.setBackgroundResource(R.drawable.background_auto_mode);
                    Utils.startLocationService(this);
                }else {
                    binding.btnAuto.setBackgroundResource(R.drawable.background_manual_mode);
                    Utils.stopLocationService(this);
                }
                Preferences.setAutoConnectEnable(this,isAutoConnectEnable);

                break;
            case R.id.btnHotSpotRoom:
                isHotspotRoomEnable=!isHotspotRoomEnable;
                if(isHotspotRoomEnable) {
                    binding.btnHotSpotRoom.setBackgroundResource(R.drawable.background_auto_mode);
                }else {
                    binding.btnHotSpotRoom.setBackgroundResource(R.drawable.background_manual_mode);
                    turnOffHotSpot();
                }
                Preferences.setHotSpotRoomEnable(this,isHotspotRoomEnable);

                break;
            case R.id.btnCrud:
                startActivity(new Intent(this,EditWifiListActivity.class));
                break;
            case R.id.btnHotSpot:
                if(isHotspotRoomEnable) {
                    openHotSpot();
                }else
                    Utils.showSnackbar(binding.getRoot(),getString(R.string.msg_enable_hotspot_room_first));
                break;
            case R.id.btnSSID1:
                WifiHelper.connectTo(this,LocationHelper.wifis.get(0));
                isAutoConnectEnable=false;
                Preferences.setAutoConnectEnable(this,isAutoConnectEnable);
                binding.btnAuto.setBackgroundResource(R.drawable.background_manual_mode);
                break;
            case R.id.btnSSID2:
                WifiHelper.connectTo(this,LocationHelper.wifis.get(1));
                isAutoConnectEnable=false;
                Preferences.setAutoConnectEnable(this,isAutoConnectEnable);
                binding.btnAuto.setBackgroundResource(R.drawable.background_manual_mode);
                break;
            case R.id.btnSSID3:
                WifiHelper.connectTo(this,LocationHelper.wifis.get(2));
                isAutoConnectEnable=false;
                Preferences.setAutoConnectEnable(this,isAutoConnectEnable);
                binding.btnAuto.setBackgroundResource(R.drawable.background_manual_mode);
                break;
            case R.id.btnSSID4:
                WifiHelper.connectTo(this,LocationHelper.wifis.get(3));
                isAutoConnectEnable=false;
                Preferences.setAutoConnectEnable(this,isAutoConnectEnable);
                binding.btnAuto.setBackgroundResource(R.drawable.background_manual_mode);
                break;
            case R.id.btnSSID5:
                WifiHelper.connectTo(this,LocationHelper.wifis.get(4));
                isAutoConnectEnable=false;
                Preferences.setAutoConnectEnable(this,isAutoConnectEnable);
                binding.btnAuto.setBackgroundResource(R.drawable.background_manual_mode);
                break;
        }
    }

    private void turnOffHotSpot() {
        ApManager ap = ApManager.newInstance(this.getApplicationContext());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ap.turnOffHotspot();
        }else {
            ap.turnWifiApOff();
        }

    }

    private void openHotSpot() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Build.VERSION.SDK_INT <= Build.VERSION_CODES.N_MR1) {
            if (Settings.System.canWrite(this.getApplicationContext())) {
                CreateNewWifiApNetwork();
            } else {
                Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_WRITE_SETTINGS);
                intent.setData(Uri.parse("package:" + this.getPackageName()));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        }
        CreateNewWifiApNetwork();
    }

    public void CreateNewWifiApNetwork() {
        ApManager ap = ApManager.newInstance(this.getApplicationContext());
        ap.createNewNetwork("sun00","sun01234");

        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.O)
            ap.turnOnHotspot();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        NetworkConnectivityReceiver.unregisterReceiver(connectivityReceiver,this);
    }


    @Override
    public void onConnectivityChange(boolean isConnected) {

    }


}

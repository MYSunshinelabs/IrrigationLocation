package com.irrigation.wifilocation.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;


import com.irrigation.wifilocation.R;
import com.irrigation.wifilocation.adapter.WifiLocationAdapter;
import com.irrigation.wifilocation.db.AppDataBase;
import com.irrigation.wifilocation.db.schema.Wifi;
import com.irrigation.wifilocation.recivers.CurrentLocationReceiver;
import com.irrigation.wifilocation.utils.DialogUtils;
import com.irrigation.wifilocation.utils.LocationHelper;
import com.irrigation.wifilocation.utils.Utils;

import java.util.ArrayList;
import java.util.List;

public class EditWifiListActivity extends AppCompatActivity implements View.OnClickListener, CurrentLocationReceiver.LocationObserver, WifiLocationAdapter.WifiLocationListner {
    private static final String TAG = EditWifiListActivity.class.getSimpleName();
    private List<Wifi> wifis=new ArrayList<>();
    private WifiLocationAdapter mAdapter;
    private RecyclerView recyclerView;
    private CurrentLocationReceiver receiver;
    public static String longtitude="",latitude="";

    @Override
    protected void onStart() {
        super.onStart();
        new Thread(new Runnable() {
            @Override
            public void run() {
                LocationHelper.wifis.clear();
                LocationHelper.wifis= AppDataBase.getAppDataBase(EditWifiListActivity.this).wifiDAO().getAllWifi();
            }
        }).start();

        Utils.initDevieInfo(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_edit_wifi_list);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            startActivity(new Intent(this,SplashActivity.class));
            finish();
        }


        Utils.initDevieInfo(this);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                init();
                findViewById(R.id.lytBar).setVisibility(View.GONE);
            }
        },2000);

        LocationHelper.buildGoogleApiClient(this,this);
    }

    private void init() {
        receiver=CurrentLocationReceiver.registerReceiver(this,EditWifiListActivity.this);
        wifis=LocationHelper.wifis;
        manageFixConnections();
        recyclerView=findViewById(R.id.recyclerView);
        mAdapter= new WifiLocationAdapter(wifis,this);
        LinearLayoutManager manager=new LinearLayoutManager(this);
        recyclerView.setLayoutManager(manager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);

        findViewById(R.id.btnSave).setOnClickListener(this);
        findViewById(R.id.btnAddNew).setOnClickListener(this);

    }
    private void manageFixConnections() {
        int size=wifis.size();
        if(size>5)
            findViewById(R.id.btnAddNew).setVisibility(View.INVISIBLE);
        else if(size<3){
            switch (size){
                case 0:
                    wifis.add(new Wifi("","","",""));
                    wifis.add(new Wifi("","","",""));
                    wifis.add(new Wifi("","","",""));
                    break;
                case 1:
                    wifis.add(new Wifi("","","",""));
                    wifis.add(new Wifi("","","",""));
                    break;
                case 2:
                    wifis.add(new Wifi("","","",""));
                    break;
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btnSave:
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        saveChanges();
                    }
                }).start();

                onBackPressed();
                break;
            case R.id.btnAddNew:

                if(wifis.size()>=5)
                    findViewById(R.id.btnAddNew).setVisibility(View.INVISIBLE);
                else
                    findViewById(R.id.btnAddNew).setVisibility(View.VISIBLE);

                wifis.add(new Wifi("","","",""));
                mAdapter.notifyDataSetChanged();
                recyclerView.scrollToPosition(wifis.size() - 1);
                break;
        }
    }

    private void saveChanges() {
        for (Wifi wifi:wifis){
            if(wifi.getId()<=0) {
                AppDataBase.getAppDataBase(getApplicationContext()).wifiDAO().insert(wifi);
                addWifi(wifi);
            }else
                AppDataBase.getAppDataBase(getApplicationContext()).wifiDAO().updatewifi(wifi);
        }
    }

    private void addWifi(Wifi wifi) {

    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        CurrentLocationReceiver.unregisterReceiver(receiver,this);
    }

    @Override
    public void onNewLocation(Location location) {
        latitude=location.getLatitude()+"";
        longtitude=location.getLongitude()+"";
        Utils.printLog(TAG,"onNewLocation()"+latitude+" "+longtitude);
    }

    @Override
    public void setCurrentLocation(Wifi wifi) {
        if(longtitude.length()>0 && latitude.length()>0){
            wifi.setLatitude(latitude);
            wifi.setLongtitude(longtitude);
            mAdapter.notifyDataSetChanged();
        }else
            Utils.showSnackbar(recyclerView,getString(R.string.error_msg_location));

    }

    @Override
    public void removeWifiLocation(final Wifi wifi) {
        DialogUtils.showConfirmationDialog(this, getString(R.string.msg_confirmation_delete), new Runnable() {
            @Override
            public void run() {
                removeWifi(wifi);
            }
        }).show();
    }

    private void removeWifi(final Wifi wifi){
        wifis.remove(wifi);

        if(wifis.size()<5)
            findViewById(R.id.btnAddNew).setVisibility(View.VISIBLE);
        mAdapter.notifyDataSetChanged();

        if(wifi.getId()>0)
            new Thread(new Runnable() {
                @Override
                public void run() {
                    AppDataBase.getAppDataBase(getApplicationContext()).wifiDAO().deleteWifi(wifi);
                }
            }).start();
    }


//    private static void getPreferences(){
//        val keyGenParameterSpec = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC);
//        val masterKeyAlias = MasterKeys.getOrCreate(keyGenParameterSpec)
//        val sharedPreferences = EncryptedSharedPreferences.create(
//                fileName,
//                        masterKeyAlias,
//                        context,
//                        EncryptedSharedPreferencesKeysets.PrefKeyEncryptionScheme.AES256_SIV,
//                        EncryptedSharedPreferencesKeysets.PrefValueEncryptionScheme.AES256_GCM
//                )
//
//        val sharedPrefsEditor = sharedPreferences.edit()
//    }

}

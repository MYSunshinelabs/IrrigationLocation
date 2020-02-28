package com.irrigation.wifilocation.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.os.Bundle;

import com.irrigation.wifilocation.R;
import com.irrigation.wifilocation.utils.Utils;

public class SplashActivity extends BaseActivity {

    private static final String TAG = SplashActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
//        CustomTextViewLight txtVersion=findViewById(R.id.txtVersion);
//        PackageInfo pInfo = null;
//        try {
//            pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
//            txtVersion.setText(getString(R.string.app_name)+"  : "+pInfo.versionName);
//        } catch (Exception e) {
//            Utils.printLog(TAG,e.getMessage());
//        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            startActivityForResult(new Intent(this,PermissionsActivity.class),100);
        else
            launchDashboard();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==100 && resultCode== 200)
            launchDashboard();
        else {
            finish();
            System.exit(0);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    private void launchDashboard() {
        Utils.startLocationService(this);
//        new Handler().postDelayed(new Runnable(){
//            @Override
//            public void run() {
        startActivity(new Intent(SplashActivity.this, EditWifiListActivity.class));
        SplashActivity.this.finish();
//            }
//        },500);
    }

}

package com.irrigation.wifilocation.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.EditText;

import com.irrigation.wifilocation.BuildConfig;
import com.irrigation.wifilocation.R;
import com.irrigation.wifilocation.customview.CustomTextViewLight;
import com.irrigation.wifilocation.utils.Preferences;

public class ConfigActivity extends AppCompatActivity {
    private EditText edtInGeofence,edtOutGeofence,edtTrackingUrl,edtTrackingPort,edtTrackingPeriod ,edtTimeOut,edtGeofenceRadius;
    private CheckBox cbHotSpot,cbTrackingMode;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config);

        init();

        updateValues();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            startActivityForResult(new Intent(this, PermissionsActivity.class),100);

    }

    private void init() {

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        getWindow().setAttributes(lp);

        edtInGeofence=findViewById(R.id.edtInGeofence);
        edtOutGeofence=findViewById(R.id.edtOutGeofence);
        edtGeofenceRadius=findViewById(R.id.edtGeofenceRadius);
        edtTrackingUrl=findViewById(R.id.edtTrackingUrl);
        edtTrackingPort=findViewById(R.id.edtTrackingPort);
        edtTrackingPeriod=findViewById(R.id.edtTrackingPeriod);
        edtTimeOut=findViewById(R.id.edtTimeOut);
        cbHotSpot=findViewById(R.id.cbAutoHotSpot);
        cbTrackingMode=findViewById(R.id.cbTrackingMode);
        CustomTextViewLight txtVersion=findViewById(R.id.txtVersion);

        txtVersion.setText("Version : "+ BuildConfig.VERSION_NAME);

    }

    private void updateValues() {
        edtInGeofence.setText(Preferences.getInGeofencePeriod(this));
        edtOutGeofence.setText(Preferences.getOutsideGeofencePeriod(this));
        edtGeofenceRadius.setText(Preferences.getGeofenceRadius(this));
        edtTrackingUrl.setText(Preferences.getTrackingUrl(this));
        edtTrackingPort.setText(Preferences.getTrackingUrlPort(this));
        edtTrackingPeriod.setText(Preferences.getTrackingUrlPeriod(this));
        edtTimeOut.setText(Preferences.getTimeOut(this));
        cbHotSpot.setChecked(Preferences.getHotSpotRoomEnable(this));
        cbTrackingMode.setChecked(Preferences.getTrackingMode(this));
    }

    @Override
    public void onBackPressed() {
        saveData();
        super.onBackPressed();
    }

    private void saveData() {
        Preferences.setInGeofencePeriod(this,edtInGeofence.getText().toString().trim());
        Preferences.setOutsideGeofencePeriod(this,edtOutGeofence.getText().toString().trim());
        Preferences.setTrackingUrl(this,edtTrackingUrl.getText().toString().trim());
        Preferences.setTrackingPort(this,edtTrackingPort.getText().toString().trim());
        Preferences.setTrackingPeriod(this,edtTrackingPeriod.getText().toString().trim());
        Preferences.setTimeOut(this,edtTimeOut.getText().toString().trim());
        Preferences.setHotSpotRoomEnable(this,cbHotSpot.isChecked());
        Preferences.setTrackingMode(this,cbTrackingMode.isChecked());
        Preferences.setGeofenceRadius(this,edtGeofenceRadius.getText().toString().trim());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==100 && resultCode== 200) {
        }else {
            finish();
            System.exit(0);
        }
    }
}

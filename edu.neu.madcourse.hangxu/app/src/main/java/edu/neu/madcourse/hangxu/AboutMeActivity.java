package edu.neu.madcourse.hangxu;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.widget.TextView;

public class AboutMeActivity extends AppCompatActivity {
    private static final int PERMISSIONS_REQUEST_READ_PHONE_STATE = 999;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_me);

        // set name of the title of this Activity
        getSupportActionBar().setTitle("About Me");

        // get device's unique ID
        if (checkSelfPermission(Manifest.permission.READ_PHONE_STATE)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_PHONE_STATE},
                    PERMISSIONS_REQUEST_READ_PHONE_STATE);
        } else {
            String deviceId = getDeviceImei();
            TextView textView8 = (TextView) findViewById(R.id.textView6);
            textView8.setText(deviceId);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST_READ_PHONE_STATE
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            String deviceId = getDeviceImei();
            TextView textView8 = (TextView) findViewById(R.id.textView6);
            textView8.setText(deviceId);
        }
    }

    private String getDeviceImei() {
        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        String deviceId = telephonyManager.getDeviceId();
        if (deviceId == null) {
            deviceId = "This phone's deviceId is null";
        }
        return deviceId;
    }
}

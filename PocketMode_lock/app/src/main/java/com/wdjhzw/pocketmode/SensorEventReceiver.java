package com.wdjhzw.pocketmode;

import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

/**
 * Created by houzhiwei on 16/5/23.
 */
public class SensorEventReceiver extends BroadcastReceiver implements SensorEventListener {
    private static final String TAG = "SensorEventReceiver";

    private SensorManager mSM;
    private Sensor mProximitySensor;
    private DevicePolicyManager mDPM;
    private ComponentName mDeviceAdmin;

    public SensorEventReceiver(SensorManager sm, Sensor s, DevicePolicyManager dpm, ComponentName
            deviceAdmin) {
        mSM = sm;
        mProximitySensor = s;
        mDPM = dpm;
        mDeviceAdmin = deviceAdmin;

        Log.e(TAG, mProximitySensor.toString());
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        switch (intent.getAction()) {
            case Intent.ACTION_USER_PRESENT:
                Log.e(TAG, "ACTION_USER_PRESENT");
                break;
            case Intent.ACTION_SCREEN_OFF:
                Log.e(TAG, "ACTION_SCREEN_OFF");
                break;
            case Intent.ACTION_SCREEN_ON:
                Log.e(TAG, "ACTION_SCREEN_ON");
                mSM.registerListener(this, mProximitySensor, SensorManager.SENSOR_DELAY_NORMAL);
                Log.e(TAG, "Sensor ON");

                break;
            default:
                break;
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        Log.e(TAG, String.valueOf(event.values[0]));

        if (event.values[0] == 0.0f) {
            if (mDPM.isAdminActive(mDeviceAdmin)) {
                mDPM.lockNow();
                Log.e(TAG, "Screen lock");
            }
        }

        mSM.unregisterListener(this);
        Log.e(TAG, "Sensor OFF");
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}

package com.wdjhzw.pocketmode;

import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

/**
 * Created by houzhiwei on 16/5/18.
 */
public class MyReceiver extends BroadcastReceiver implements SensorEventListener {
    private static final String TAG = "MyReceiver";

    private Context mContext;
    private SensorManager mSensorManager;
    private Sensor mProximitySensor;

    public MyReceiver(Context context, SensorManager sm, Sensor s) {
        mContext = context;
        mSensorManager = sm;
        mProximitySensor = s;

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
                mSensorManager.unregisterListener(this);
                break;
            case Intent.ACTION_SCREEN_ON:
                Log.e(TAG, "ACTION_SCREEN_ON");
                mSensorManager.registerListener(this, mProximitySensor, SensorManager
                        .SENSOR_DELAY_NORMAL);

                break;
            default:
                break;
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        Log.e(TAG, "sensor value:" + String.valueOf(event.values[0]));

        if (event.values[0] == 0.0f) {
            mContext.startActivity(new Intent().setClass(mContext, LockedScreenActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}

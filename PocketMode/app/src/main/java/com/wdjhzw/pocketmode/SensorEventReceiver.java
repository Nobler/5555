package com.wdjhzw.pocketmode;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by houzhiwei on 16/5/23.
 */
public class SensorEventReceiver extends BroadcastReceiver implements SensorEventListener {
    private static final String TAG = "SensorEventReceiver";

    private Context mContext;
    private SensorManager mSM;
    private Sensor mProximitySensor;
    private boolean mIsBlockedViewShown = false;

    public SensorEventReceiver(Context context, SensorManager sm, Sensor s) {
        mContext = context;
        mSM = sm;
        mProximitySensor = s;

        Log.e(TAG, mProximitySensor.toString());
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        switch (intent.getAction()) {
            case Intent.ACTION_USER_PRESENT:
                Log.e(TAG, "ACTION_USER_PRESENT");

            case Intent.ACTION_SCREEN_OFF:
                Log.e(TAG, "ACTION_SCREEN_OFF");
                if (mIsBlockedViewShown) {
                    hideBlockedView();
                    mSM.unregisterListener(this);
                    Log.e(TAG, "Sensor OFF");
                    mIsBlockedViewShown = false;
                }
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
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays
                    (mContext)) {
                mSM.unregisterListener(this);
                Log.e(TAG, "Sensor OFF");

                Toast.makeText(mContext, "Please give my app this permission!", Toast
                        .LENGTH_LONG).show();
                return;
            }

            showBlockedView();
            mIsBlockedViewShown = true;
        } else {
            if (mIsBlockedViewShown) {
                hideBlockedView();
                mIsBlockedViewShown = false;
            }
            mSM.unregisterListener(this);
            Log.e(TAG, "Sensor OFF");
        }
    }

    private void showBlockedView() {
        Log.e(TAG, "showBlockedView");
        mContext.startService(new Intent(MainService.ACTION_SHOW_BLOCKED_VIEW).setClass(mContext,
                MainService.class));
    }

    private void hideBlockedView() {
        Log.e(TAG, "hideBlockedView");
        mContext.startService(new Intent(MainService.ACTION_HIDE_BLOCKED_VIEW).setClass(mContext,
                MainService.class));
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}

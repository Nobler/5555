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
    private boolean mIsBlockedActivityInBack = true;

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
                mSM.unregisterListener(this);

                break;
            case Intent.ACTION_SCREEN_OFF:
                Log.e(TAG, "ACTION_SCREEN_OFF");
                if (!mIsBlockedActivityInBack) {
                    hideBlockedActivity();
                    Log.e(TAG, "hide activity");
                    mSM.unregisterListener(this);
                    Log.e(TAG, "Sensor OFF");
                    mIsBlockedActivityInBack = true;
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
                        .LENGTH_SHORT).show();
                return;
            }

            Log.e(TAG, "show activity");
            mContext.startActivity(new Intent().setClass(mContext, BlockedActivity.class).addFlags
                    (Intent.FLAG_ACTIVITY_NEW_TASK));
            mIsBlockedActivityInBack = false;
        } else {
            if (!mIsBlockedActivityInBack) {
                hideBlockedActivity();
                Log.e(TAG, "hide activity");
                mIsBlockedActivityInBack = true;
            }
            mSM.unregisterListener(this);
            Log.e(TAG, "Sensor OFF");
        }
    }

    private void hideBlockedActivity() {
        BlockedActivity activity = BlockedActivity.getInstance();
        if (activity != null) {
            activity.moveTaskToBack(false);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}

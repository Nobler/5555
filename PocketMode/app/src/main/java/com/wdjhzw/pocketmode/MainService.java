package com.wdjhzw.pocketmode;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PixelFormat;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ProgressBar;

/**
 * Created by houzhiwei on 16/5/23.
 */
public class MainService extends Service {
    public static final String ACTION_SHOW_BLOCKED_VIEW = "com.wdjhzw.pocketmode.SHOW_BLOCKED_VIEW";
    public static final String ACTION_HIDE_BLOCKED_VIEW = "com.wdjhzw.pocketmode.HIDE_BLOCKED_VIEW";
    public static final String TAG = "MainService";
    private SensorEventReceiver mReceiver;
    private BlockedView mBlockedView;
    private WindowManager.LayoutParams mLayoutParams;
    private WindowManager mWindowManager;

    private boolean mIsBlockedViewShown;
    private ProgressBar mProgressBar;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        Sensor proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);

        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_USER_PRESENT);

        mReceiver = new SensorEventReceiver(this, sensorManager, proximitySensor);
        registerReceiver(mReceiver, filter);

        mWindowManager = ((WindowManager) getSystemService(Context.WINDOW_SERVICE));
        initBlockedViewLayoutParams();
        inflateBlockedView();
        mIsBlockedViewShown = false;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent.getAction();
        Log.e(TAG, "onStartCommand:" + action + ":" + toString());

        if (action != null) {
            if (action.equals(ACTION_SHOW_BLOCKED_VIEW)) {
                showBlockedView();
            } else if (action.equals(ACTION_HIDE_BLOCKED_VIEW)) {
                hideBlockedView();
            }
        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        unregisterReceiver(mReceiver);
    }

    private void initBlockedViewLayoutParams() {
        mLayoutParams = new WindowManager.LayoutParams(WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams
                .TYPE_SYSTEM_ERROR, WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER, PixelFormat.TRANSLUCENT);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mLayoutParams.flags |= WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS;
        } else {
            mLayoutParams.flags |= WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
        }
    }

    private void inflateBlockedView() {
        mBlockedView = (BlockedView) ((LayoutInflater) getSystemService(Context
                .LAYOUT_INFLATER_SERVICE)).inflate(R.layout.activity_locked_screen, null);

        mProgressBar = (ProgressBar) mBlockedView.findViewById(R.id.progressBar);
        mBlockedView.setOnKeyStateChangeListener(new BlockedView.OnKeyStateChangeListener() {

            @Override
            public void onDoubleVolumeKeyStateChange(boolean downInSameTime, int repeatCount) {
                if (!downInSameTime) {
                    mProgressBar.setVisibility(View.INVISIBLE);
                    return;
                }

                mProgressBar.setVisibility(View.VISIBLE);
                mProgressBar.setProgress(mProgressBar.getMax() * repeatCount / 21);

                if (repeatCount > 20) {
                    hideBlockedView();
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            sendBroadcast(new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));
                            Log.e(TAG, "clean");
                        }
                    }, 500);
                }
            }
        });

    }

    private void showBlockedView() {
        if (!mIsBlockedViewShown) {
            mWindowManager.addView(mBlockedView, mLayoutParams);
            mIsBlockedViewShown = true;
        }
    }

    private void hideBlockedView() {
        mProgressBar.setVisibility(View.INVISIBLE);

        if (mIsBlockedViewShown) {
            mWindowManager.removeView(mBlockedView);
            mIsBlockedViewShown = false;
        }
    }

}

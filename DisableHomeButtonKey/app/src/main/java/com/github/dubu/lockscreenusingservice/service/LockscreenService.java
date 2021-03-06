package com.github.dubu.lockscreenusingservice.service;

import android.app.KeyguardManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.github.dubu.lockscreenusingservice.LockscreenActivity;
import com.github.dubu.lockscreenusingservice.LockscreenUtil;


/**
 * Created by mugku on 15. 5. 20..
 */
public class LockscreenService extends Service {
    private final String TAG = "LockscreenService";
    private Context mContext = null;

    private KeyguardManager mKeyManager = null;
    private KeyguardManager.KeyguardLock mKeyLock = null;

    private BroadcastReceiver mLockscreenReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (null != context) {
                if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
                    Intent startLockscreenIntent = new Intent(mContext, LockscreenViewService
                            .class);
                    stopService(startLockscreenIntent);
                    TelephonyManager tManager = (TelephonyManager) context.getSystemService
                            (Context.TELEPHONY_SERVICE);
                    boolean isPhoneIdle = tManager.getCallState() == TelephonyManager
                            .CALL_STATE_IDLE;
                    if (isPhoneIdle) {
                        Log.e(TAG, "SCREEN OFF: startLockscreenActivity");
                        startLockscreenActivity();
                    }
                }
            }
        }
    };

    private void stateRecever(boolean isStartRecever) {
        Log.e(TAG, (isStartRecever ? "reg" : "unreg") + " SCREEN_OFF receiver");
        if (isStartRecever) {
            IntentFilter filter = new IntentFilter();
            filter.addAction(Intent.ACTION_SCREEN_OFF);
            registerReceiver(mLockscreenReceiver, filter);
        } else {
            if (null != mLockscreenReceiver) {
                unregisterReceiver(mLockscreenReceiver);
            }
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e(TAG, "onStartCommand");

        stateRecever(true);
        Intent bundleIntet = intent;
        if (null != bundleIntet) {
            startLockscreenActivity();
        } else {
            Log.d(TAG, TAG + " onStartCommand intent NOT existed");
        }
        setLockGuard();
        return LockscreenService.START_STICKY;
    }

    private void setLockGuard() {
        Log.e(TAG, "set LockGuard");
        initKeyguardService();
        setStandardKeyguardState(LockscreenUtil.getInstance(mContext).isStandardKeyguardState());
    }

    private void initKeyguardService() {
        if (null != mKeyManager) {
            mKeyManager = null;
        }
        mKeyManager = (KeyguardManager) getSystemService(mContext.KEYGUARD_SERVICE);
        if (null != mKeyManager) {
            if (null != mKeyLock) {
                mKeyLock = null;
            }
            mKeyLock = mKeyManager.newKeyguardLock(mContext.KEYGUARD_SERVICE);
        }
    }

    private void setStandardKeyguardState(boolean isStart) {
        if (isStart) {
            if (null != mKeyLock) {
                mKeyLock.reenableKeyguard();
            }
        } else {
            if (null != mKeyManager) {
                mKeyLock.disableKeyguard();
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public void onDestroy() {
        stateRecever(false);
        setStandardKeyguardState(true);
    }

    private void startLockscreenActivity() {
        Log.e(TAG, "start LockscreenActivity");
        Intent startLockscreenActIntent = new Intent(mContext, LockscreenActivity.class);
        startLockscreenActIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(startLockscreenActIntent);
    }

}

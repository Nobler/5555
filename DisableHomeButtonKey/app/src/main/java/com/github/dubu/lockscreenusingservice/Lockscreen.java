package com.github.dubu.lockscreenusingservice;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.github.dubu.lockscreenusingservice.service.LockscreenService;
import com.github.dubu.lockscreenusingservice.service.LockscreenViewService;

import java.awt.font.TextAttribute;

/**
 * Created by mugku on 15. 5. 20..
 */
public class Lockscreen {
    private static final String TAG = "LockScreen";
    private Context mContext = null;
    public static final String ISSOFTKEY = "ISSOFTKEY";
    public static final String ISLOCK = "ISLOCK";
    private static Lockscreen mLockscreenInstance;
    public static Lockscreen getInstance(Context context) {
        if (mLockscreenInstance == null) {
            if (null != context) {
                mLockscreenInstance = new Lockscreen(context);
            }
            else {
                mLockscreenInstance = new Lockscreen();
            }
        }
        return mLockscreenInstance;
    }

    private Lockscreen() {
        mContext = null;
    }

    private Lockscreen(Context context) {
        mContext = context;
    }

    public void startLockscreenService() {
        Log.e(TAG, "startLockscreenService");
        SharedPreferencesUtil.init(mContext);
        Intent startLockscreenIntent =  new Intent(mContext, LockscreenService.class);
//        startLockscreenIntent.putExtra(LockscreenService.LOCKSCREENSERVICE_FIRST_START, true);
        mContext.startService(startLockscreenIntent);
    }
    public void stopLockscreenService() {
        Log.e(TAG, "stopLockscreenService");

        Intent stopLockscreenViewIntent =  new Intent(mContext, LockscreenViewService.class);
        mContext.stopService(stopLockscreenViewIntent);
        Intent stopLockscreenIntent =  new Intent(mContext, LockscreenService.class);
        mContext.stopService(stopLockscreenIntent);
    }
}

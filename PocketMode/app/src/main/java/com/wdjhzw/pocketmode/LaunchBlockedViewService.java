package com.wdjhzw.pocketmode;

import android.app.KeyguardManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;

import java.awt.font.TextAttribute;

public class LaunchBlockedViewService extends Service {
    private static final String TAG = "LaunchBlockedView";

    public LaunchBlockedViewService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.e(TAG, "onCreate");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e(TAG, "onCreate");

        WindowManager.LayoutParams params = new WindowManager.LayoutParams(WindowManager
                .LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_SYSTEM_ERROR, WindowManager.LayoutParams
                .FLAG_SHOW_WHEN_LOCKED | WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER,
                PixelFormat.TRANSLUCENT);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            params.flags |= WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS;
        } else {
            params.flags |= WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
        }

        View blockedView = ((LayoutInflater) getBaseContext().getSystemService(Context
                .LAYOUT_INFLATER_SERVICE)).inflate(R.layout.activity_locked_screen, null);

        WindowManager wm = ((WindowManager) getSystemService(WINDOW_SERVICE));
        wm.addView(blockedView, params);

        return super.onStartCommand(intent, flags, startId);


    }


}

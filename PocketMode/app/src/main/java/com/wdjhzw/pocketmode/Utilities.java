package com.wdjhzw.pocketmode;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;

/**
 * Utilities for common features
 */
public class Utilities {
    private static final String KEY_SCREEN_HEIGHT = "screen_height";
    private static int mScreenHeight;

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @SuppressWarnings("deprecation")
    public static void removeOnGlobalLayoutListener(View v, ViewTreeObserver
            .OnGlobalLayoutListener listener) {
        if (Build.VERSION.SDK_INT < 16)
            v.getViewTreeObserver().removeGlobalOnLayoutListener(listener);
        else {
            v.getViewTreeObserver().removeOnGlobalLayoutListener(listener);
        }
    }

    public static int getScreenHeight(Context context) {
        if (mScreenHeight == 0) {
            mScreenHeight = PreferenceManager.getDefaultSharedPreferences(context).getInt(KEY_SCREEN_HEIGHT, 0);

            if (mScreenHeight == 0) {
                DisplayMetrics dm = new DisplayMetrics();
                ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getMetrics(dm);
                mScreenHeight = dm.heightPixels;

                // We need an Editor object to make preference changes.
                // All objects are from android.context.Context
                SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
                SharedPreferences.Editor editor = settings.edit();
                editor.putInt(KEY_SCREEN_HEIGHT, mScreenHeight);

                // Commit the edits!
                editor.apply();
            }
        }
        return mScreenHeight;
    }

    public static void log(String tag, String msg) {
        if (BuildConfig.DEBUG) {
            Log.e(tag, msg);
        }
    }
}

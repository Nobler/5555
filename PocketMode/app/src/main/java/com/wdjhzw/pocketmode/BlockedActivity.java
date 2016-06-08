package com.wdjhzw.pocketmode;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

import java.awt.font.TextAttribute;

/**
 * Created by houzhiwei on 16/5/20.
 */
public class BlockedActivity extends Activity {
    public static final String TAG = "BlockedActivity";

    private static BlockedActivity mInstance;

    private WindowManager mWM = null;
    private StatusBarBlockedView mBlockedView = null;
    private View mDecorView = null;
    private WindowManager.LayoutParams mBlockedViewParams;

    private boolean mIsVolumeDownKeyDown;
    private boolean mIsVolumeUpKeyDown;
    private boolean mIsDoubleKeyDownInSameTime;
    private int mLastRepeatCount;
    private int mLastKeyCode;

    private ProgressBar mProcessBar;

    public static BlockedActivity getInstance() {
        return mInstance;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.e(TAG, "onCreate");
        if (mInstance == null) {
            mInstance = this;
        }

        mLastRepeatCount = -1;

        mWM = (WindowManager) getSystemService(Context.WINDOW_SERVICE);

        mDecorView = getWindow().getDecorView();
        mDecorView.setOnSystemUiVisibilityChangeListener(new View
                .OnSystemUiVisibilityChangeListener() {
            @Override
            public void onSystemUiVisibilityChange(int visibility) {
                // Note that system bars will only be "visible" if none of the
                // LOW_PROFILE, HIDE_NAVIGATION, or FULLSCREEN flags are set.
                if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
                    // The system bars are visible
                    hideSystemUI();
                }
            }
        });

        mBlockedView = new StatusBarBlockedView(this);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | WindowManager
                .LayoutParams.FLAG_SHOW_WALLPAPER);

        setContentView(R.layout.activity_locked_screen);
        mProcessBar = (ProgressBar) findViewById(R.id.progressBar);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.e(TAG, "onStart");

        disableStatusBar();
        hideSystemUI();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.e(TAG, "onNewIntent");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.e(TAG, "onResume");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.e(TAG, "onPause");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.e(TAG, "onStop");

        if (mBlockedView != null) {
            mWM.removeView(mBlockedView);
        }
    }

    @Override
    public void onBackPressed() {
        //do nothing to block back key
    }

    // This snippet hides the system bars.
    private void hideSystemUI() {
        // Set the IMMERSIVE flag.
        // Set the content to appear under the system bars so that the content
        // doesn't resize when the system bars hide and show.
        mDecorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View
                .SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                | View.SYSTEM_UI_FLAG_IMMERSIVE);
    }

    private void disableStatusBar() {
        mWM.addView(mBlockedView, getBlockedViewLayoutParams());
    }

    @NonNull
    private WindowManager.LayoutParams getBlockedViewLayoutParams() {
        if (mBlockedViewParams != null) {
            return mBlockedViewParams;
        }

        mBlockedViewParams = new WindowManager.LayoutParams();
        mBlockedViewParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ERROR;
        mBlockedViewParams.gravity = Gravity.TOP;
        mBlockedViewParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                // this is to enable the notification to receive touch events
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL |
                // Draws over status bar
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;

        mBlockedViewParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        mBlockedViewParams.height = getStatusBarHeight();
//        mBlockedViewParams.format = PixelFormat.TRANSPARENT;

        return mBlockedViewParams;
    }

    private int getStatusBarHeight() {
        // status bar height
        int statusBarHeight = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            statusBarHeight = getResources().getDimensionPixelSize(resourceId);
            Log.e(TAG, "status bar height:" + statusBarHeight);
        }

        return statusBarHeight;
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        Log.e(TAG, "key:" + event.getKeyCode() + ", action:" + event.getAction() + ", repeat " +
                "count:" + event.getRepeatCount());

        int keyCode = event.getKeyCode();
        int action = event.getAction();
        int repeatCount = event.getRepeatCount();

        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            mIsVolumeDownKeyDown = (action == KeyEvent.ACTION_DOWN);
        } else if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            mIsVolumeUpKeyDown = (action == KeyEvent.ACTION_DOWN);
        } else {
            return true;
        }


        if (mIsVolumeDownKeyDown && mIsVolumeUpKeyDown) {
            if (mLastRepeatCount == 0 && repeatCount == 0 && keyCode != mLastKeyCode) {
                mIsDoubleKeyDownInSameTime = true;
            }
        } else {
            mIsDoubleKeyDownInSameTime = false;
        }

        if (mIsDoubleKeyDownInSameTime) {
            mProcessBar.setVisibility(View.VISIBLE);
            mProcessBar.setProgress(mProcessBar.getMax() * repeatCount / 21);

            if (repeatCount > 20) {
                // dispatchKeyEvent will be also called one more time after this, between onPause
                // and onStop. mIsDoubleKeyDownInSameTime & mIsVolumeDownKeyDown &
                // mIsVolumeUpKeyDown will be set automatically.
                moveTaskToBack(false);
            }
        } else {
            mProcessBar.setVisibility(View.INVISIBLE);
        }

        mLastRepeatCount = repeatCount;
        mLastKeyCode = keyCode;

        return true;
    }

    public class StatusBarBlockedView extends ViewGroup {

        public StatusBarBlockedView(Context context) {
            super(context);
        }

        @Override
        protected void onLayout(boolean changed, int l, int t, int r, int b) {
        }

        @Override
        public boolean onInterceptTouchEvent(MotionEvent ev) {
            Log.e("StatusBarBlockedView", "Intercepted");
            return true;
        }
    }
}
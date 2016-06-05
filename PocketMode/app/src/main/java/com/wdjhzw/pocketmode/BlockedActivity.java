package com.wdjhzw.pocketmode;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

/**
 * Created by houzhiwei on 16/5/20.
 */
public class BlockedActivity extends Activity {
    private static final String TAG = "BlockedActivity";

    private static BlockedActivity mInstance;

    private WindowManager mWM = null;
    private StatusBarBlockedView mBlockView = null;
    private View mDecorView = null;

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

//        registerReceiver(new BootReceiver() {
//            @Override
//            public void onReceive(Context context, Intent intent) {
//                String reason = intent.getStringExtra("reason");
//                Log.e(TAG, "reason:" + reason);
//            }
//        }, new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));

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

        hideSystemUI();

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | WindowManager
                .LayoutParams.FLAG_SHOW_WALLPAPER);

//        setContentView(R.layout.activity_locked_screen);
//        LinearLayout l = (LinearLayout) findViewById(R.id.background);
//        l.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//            }
//        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.e(TAG, "onStart");
        disableStatusBar();
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
        Log.e(TAG, getStatusBarHeight() + ":" + getNavigationBarHeight() + ":" + getResources()
                .getDisplayMetrics().scaledDensity);
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.e(TAG, "onStop");

        if (mBlockView != null) {
            mWM.removeView(mBlockView);
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
        WindowManager.LayoutParams params = new WindowManager.LayoutParams();
        params.type = WindowManager.LayoutParams.TYPE_SYSTEM_ERROR;
        params.gravity = Gravity.BOTTOM;
        params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                // this is to enable the notification to receive touch events
//                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL |
                // Draws over status bar
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;

        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = getStatusBarHeight();
//        params.format = PixelFormat.TRANSPARENT;

        mBlockView = new StatusBarBlockedView(this);
        mWM.addView(mBlockView, params);
    }

    private int getStatusBarHeight() {
        // status bar height
        int statusBarHeight = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            statusBarHeight = getResources().getDimensionPixelSize(resourceId);
        }

        return statusBarHeight;
    }

    private int getNavigationBarHeight() {
        // navigation bar height
        int navigationBarHeight = 0;
        int resourceId = getResources().getIdentifier("navigation_bar_height", "dimen", "android");
        if (resourceId > 0) {
            navigationBarHeight = getResources().getDimensionPixelSize(resourceId);
        }

        return navigationBarHeight;
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

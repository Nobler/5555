package com.wdjhzw.pocketmode;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.LinearLayout;

/**
 * TODO: document your custom view class.
 */
public class BlockedView extends LinearLayout {
    public static final String TAG = "BlockedView";

    private boolean mIsVolumeDownKeyDown;
    private boolean mIsVolumeUpKeyDown;
    private boolean mIsDoubleKeyDownInSameTime;
    private int mLastRepeatCount;
    private int mLastKeyCode;

    private OnKeyStateChangeListener mListener;

    public BlockedView(Context context) {
        this(context, null);
    }

    public BlockedView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BlockedView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
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

        mListener.onDoubleVolumeKeyStateChange(mIsDoubleKeyDownInSameTime, repeatCount);

        mLastRepeatCount = repeatCount;
        mLastKeyCode = keyCode;

        return true;
    }

    public void setOnKeyStateChangeListener(OnKeyStateChangeListener l) {
        mListener = l;
    }

    /**
     * Interface definition for a callback to be invoked when double volumn key are down in same
     * time.
     */
    public interface OnKeyStateChangeListener {
        /**
         * Called when double volumn key are down in same.
         *
         * @param downInSameTime Whether the double volume key are down in same time.
         * @param repeatCount    The repeat count double volume key are down in same.
         */
        void onDoubleVolumeKeyStateChange(boolean downInSameTime, int repeatCount);
    }
}

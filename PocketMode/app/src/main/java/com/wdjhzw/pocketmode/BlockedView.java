package com.wdjhzw.pocketmode;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.RelativeLayout;

/**
 * View to block all touch events.
 */
public class BlockedView extends RelativeLayout {
    public static final String TAG = "BlockedView";

    private boolean mIsVolumeDownKeyDown;
    private boolean mIsVolumeUpKeyDown;
    private int mKeyState;
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
            Log.e(TAG, "key down");
            if (mLastRepeatCount == 0 && repeatCount == 0 && keyCode != mLastKeyCode) {
                mKeyState = KeyState.STATE_DOUBLE_KEY_DOWN_SIMULTANEOUSLY;
                Log.e(TAG, "STATE_DOUBLE_KEY_DOWN_SIMULTANEOUSLY");
            }
        } else if (!mIsVolumeDownKeyDown && !mIsVolumeUpKeyDown && keyCode != mLastKeyCode) {
            mKeyState = KeyState.STATE_DOUBLE_KEY_UP;
            Log.e(TAG, "STATE_DOUBLE_KEY_UP");
        } else {
            mKeyState = KeyState.STATE_OTHERS;
        }

        mListener.onKeyStateChange(mKeyState, repeatCount);

        mLastRepeatCount = repeatCount;
        mLastKeyCode = keyCode;

        return true;
    }

    public void setOnKeyStateChangeListener(OnKeyStateChangeListener l) {
        mListener = l;
    }

    /**
     * Interface definition for a callback to be invoked when key state change
     * time.
     */
    public interface OnKeyStateChangeListener {
        /**
         * Called when double volume key state change
         *
         * @param keyState    Whether the double volume key are down in same time.
         * @param repeatCount The repeat count double volume key are down in same.
         */
        void onKeyStateChange(int keyState, int repeatCount);
    }

    public static class KeyState {
        public static final int STATE_DOUBLE_KEY_DOWN_SIMULTANEOUSLY = 0;
        public static final int STATE_DOUBLE_KEY_UP = 1;
        public static final int STATE_OTHERS = 2;
    }
}

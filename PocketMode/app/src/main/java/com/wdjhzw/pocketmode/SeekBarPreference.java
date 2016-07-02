package com.wdjhzw.pocketmode;

import android.content.Context;
import android.content.res.TypedArray;
import android.preference.Preference;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.wdjhzw.pocketmode.widget.SeekBarWithHint;

/**
 * A {@link Preference} that provides a SeekBar to pick value.
 *
 */
public class SeekBarPreference extends Preference {
    public static final String TAG = "SeekBarPreference";
    public static final int DEFAULT_VALUE = 20;
    private int mCurrentValue;
    private OnSeekBarTrackingStateChangedListener mListener;

    public SeekBarPreference(Context context) {
        this(context, null);
    }

    public SeekBarPreference(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SeekBarPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
        Log.e(TAG, "onSetInitialValue:" + restorePersistedValue + ":" + defaultValue);
        if (restorePersistedValue) {
            // Restore existing state
            mCurrentValue = this.getPersistedInt(DEFAULT_VALUE);
        } else {
            // Set default state from the XML attribute
            mCurrentValue = (Integer) defaultValue;
            persistInt(mCurrentValue);
        }
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getInteger(index, DEFAULT_VALUE);
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);
        Log.e(TAG, "onBindView");

        final SeekBarWithHint seekBar = (SeekBarWithHint) view.findViewById(R.id
                .seek_bar_with_hint);
        seekBar.setProgress(mCurrentValue);
        seekBar.setOnSeekBarChangeListener(new SeekBarWithHint.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(int progress) {
                callChangeListener(progress);
            }

            @Override
            public void onStartTrackingTouch() {
                if (mListener != null) {
                    mListener.onStartTrackingTouch();
                }
            }

            @Override
            public void onStopTrackingTouch(int progress) {
                mCurrentValue = progress;
                persistInt(mCurrentValue);
                if (mListener != null) {
                    mListener.onStopTrackingTouch(progress);
                }
            }
        });
    }

    public void setOnSeekBarTrackingStateChangedListener (OnSeekBarTrackingStateChangedListener l) {
        mListener = l;
    }

    /**
     * Listener related to SeekBarWithHint.OnSeekBarChangeListener
     */
    public interface OnSeekBarTrackingStateChangedListener {
        void onStartTrackingTouch();
        void onStopTrackingTouch(int progress);
    }
}

package com.wdjhzw.pocketmode.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.wdjhzw.pocketmode.R;

/**
 * SeekBar with a hint view to show its current value.
 */
public class SeekBarWithHint extends LinearLayout {
    private OnSeekBarChangeListener mListener;
    private SeekBar mSeekBar;

    public SeekBarWithHint(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        View view = inflate(context, R.layout.seek_bar_with_hint, this);

        final TextView tv = (TextView) view.findViewById(R.id.hint);
        mSeekBar = (SeekBar) view.findViewById(R.id.seek_bar);
        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            private int progress;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tv.setText(String.format("%d%%", progress));
                this.progress = progress;
                if (mListener != null) {
                    mListener.onProgressChanged(this.progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                if (mListener != null) {
                    mListener.onStartTrackingTouch();
                }
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (mListener != null) {
                    mListener.onStopTrackingTouch(this.progress);
                }
            }
        });
    }

    public SeekBarWithHint(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SeekBarWithHint(Context context) {
        this(context, null);
    }

    public void setProgress(int progress) {
        mSeekBar.setProgress(progress);
    }

    public void setOnSeekBarChangeListener(OnSeekBarChangeListener listener) {
        mListener = listener;
    }

    /**
     * Listener related to SeekBar.OnSeekBarChangeListener
     */
    public interface OnSeekBarChangeListener {
        void onProgressChanged(int progress);
        void onStartTrackingTouch();
        void onStopTrackingTouch(int progress);
    }

}

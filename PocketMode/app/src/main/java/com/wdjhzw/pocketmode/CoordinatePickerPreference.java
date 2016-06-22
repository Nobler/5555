package com.wdjhzw.pocketmode;

import android.content.Context;
import android.preference.Preference;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import java.awt.font.TextAttribute;

/**
 * Created by houzhiwei on 16/6/22.
 */
public class CoordinatePickerPreference extends Preference {
    public static final String TAG = "CoordinatePicker";

    private SeekBar mSeekBar;
    private TextView mCoordinaten;

    public CoordinatePickerPreference(Context context) {
        this(context, null);
    }

    public CoordinatePickerPreference(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CoordinatePickerPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        Log.e(TAG, "Construction");
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);

        mSeekBar = (SeekBar) view.findViewById(R.id.seek_bar);
        mCoordinaten = (TextView) view.findViewById(R.id.coordinate);
        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                Log.e(TAG, "" + progress + (mCoordinaten == null ? "true" : "false"));
                mCoordinaten.setText(progress + "%");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    @Override
    protected void onClick() {
        super.onClick();
    }
}

package com.wdjhzw.pocketmode;

import android.content.Context;
import android.content.res.ColorStateList;
import android.support.design.widget.FloatingActionButton;
import android.util.AttributeSet;

/**
 * Implementation of App Widget functionality.
 */
public class CheckableFab extends FloatingActionButton {
    private boolean mIsChecked;
    private boolean mIsEnabled;

    public CheckableFab(Context context) {
        super(context);
    }

    public CheckableFab(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CheckableFab(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setChecked(boolean checked) {
        mIsChecked = checked;
        setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(checked ? android.R
                .color.holo_green_light : R.color.colorAccent)));
    }

    @Override
    public boolean isEnabled() {
//        return super.isEnabled();
        return mIsEnabled;
    }

    @Override
    public void setEnabled(boolean enabled) {
//        super.setEnabled(enabled);
        mIsEnabled = enabled;

        int colorId;

        if (enabled) {
            if (mIsChecked) {
                colorId = android.R.color.holo_green_light;
            } else {
                colorId = R.color.colorAccent;
            }
        } else {
            colorId = android.R.color.darker_gray;
        }

        setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(colorId)));
    }
}


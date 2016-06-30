package com.wdjhzw.pocketmode;

import android.content.Context;
import android.content.res.ColorStateList;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.animation.AnimationUtils;

/**
 * Implementation of App Widget functionality.
 */
public class CheckableFab extends FloatingActionButton {
    private boolean mIsChecked;
    private boolean mIsEnabled;
    private Context mContext;

    public CheckableFab(Context context) {
        this(context, null);
    }

    public CheckableFab(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CheckableFab(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
    }

    public void setChecked(boolean checked) {
        mIsChecked = checked;
        setImageResource(mIsChecked ? android.R.drawable.ic_media_pause : android.R.drawable
                .ic_media_play);

        if (mIsChecked) {
            startAnimation(AnimationUtils.loadAnimation(mContext, R.anim.breath));
        } else {
            clearAnimation();
        }
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

        int colorId = enabled ? R.color.colorAccent : android.R.color.darker_gray;
        setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(mContext, colorId)));

        if (!enabled && mIsChecked) {
            setImageResource(android.R.drawable.ic_media_play);
            clearAnimation();
        }
    }
}


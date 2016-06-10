package com.github.dubu.lockscreenusingservice;

import android.annotation.TargetApi;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;

/**
 * Created by mugku on 15. 5. 20..
 */
public class LockScreenView extends RelativeLayout {
    public LockScreenView(Context context) {
        super(context);
    }

    public LockScreenView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public LockScreenView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(21)
    public LockScreenView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }
}

package com.github.dubu.lockscreensample;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.SwitchCompat;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CompoundButton;

import com.github.dubu.lockscreenusingservice.Lockscreen;
import com.github.dubu.lockscreenusingservice.SharedPreferencesUtil;

/**
 * Created by DUBU on 15. 5. 20..
 */
public class MainActivity extends ActionBarActivity {
    private SwitchCompat mSwitchd = null;
    private Context mContext = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        setContentView(R.layout.activity_main);
        SharedPreferencesUtil.init(mContext);

        mSwitchd = (SwitchCompat) this.findViewById(R.id.switch_locksetting);
        mSwitchd.setTextOn("yes");
        mSwitchd.setTextOff("no");
        mSwitchd.setChecked(SharedPreferencesUtil.get(Lockscreen.ISLOCK));

        mSwitchd.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferencesUtil.setBoolean(Lockscreen.ISLOCK, isChecked);
                if (isChecked) {
                    Lockscreen.getInstance(mContext).startLockscreenService();
                } else {
                    Lockscreen.getInstance(mContext).stopLockscreenService();
                }

            }
        });
    }
}

package com.wdjhzw.pocketmode;


import android.app.ActivityManager;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.SwitchPreference;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.util.Log;
import android.widget.Toast;

import java.util.List;

public class MainSettingsFragment extends PreferenceFragment implements Preference
        .OnPreferenceChangeListener {
    private static final String TAG = "MainSettingsFragment";

    private static final String KEY_CAN_DRAW_OVERLAYS = "can_draw_overlays";
    private static final String KEY_AUTO_START = "auto_start";

    private ComponentName mBootReceiver;

    private MainActivity mContext;
    private boolean mCanDrawOverlays;
    private SwitchPreference mDrawOverlays;
    private SwitchPreference mBootStart;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        mContext = (MainActivity) context;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.pref_main);

        mBootStart = (SwitchPreference) findPreference(KEY_AUTO_START);
        mBootStart.setOnPreferenceChangeListener(this);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            // Permission "Draw over other apps" is added in Android M.
            getPreferenceScreen().removePreference(findPreference("permission"));
        } else {
            mDrawOverlays = (SwitchPreference) findPreference(KEY_CAN_DRAW_OVERLAYS);
            mDrawOverlays.setOnPreferenceChangeListener(this);

            mBootStart.setDependency(KEY_CAN_DRAW_OVERLAYS);
        }

        mBootReceiver = new ComponentName(mContext, MainService.BootReceiver.class);
    }

    @Override
    public void onResume() {
        super.onResume();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mCanDrawOverlays = Settings.canDrawOverlays(mContext);
            mDrawOverlays.setChecked(mCanDrawOverlays);

            if (!mCanDrawOverlays) {
                mContext.setFabEnabled(false);

                // auto-start should be disabled too
                if (mBootStart.isChecked()) {
                    setPreference(mBootStart, false);
                }

                return;
            }
        }

        mContext.setFabEnabled(true);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        Log.e(TAG, "onPreferenceChange:" + preference.toString() + ":" + newValue.toString());

        boolean value = (Boolean) newValue;

        if (preference == mDrawOverlays) {
            if (value != mCanDrawOverlays) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse
                        ("package:" + mContext.getPackageName()));
                startActivity(intent);
                // return false - don't update checkbox until we're really active
                return false;
            }
        } else if (preference == mBootStart) {
            mContext.getPackageManager().setComponentEnabledSetting(mBootReceiver, value ? PackageManager
                    .COMPONENT_ENABLED_STATE_ENABLED : PackageManager
                    .COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
            Log.e(TAG, "" + mContext.getPackageManager().getComponentEnabledSetting(mBootReceiver));
        }
        return true;
    }

    private void setPreference(SwitchPreference preference, boolean newValue) {
        onPreferenceChange(preference, newValue);
        // onPreferenceChanage() is manually called, the check state should be
        // also set manually.
        preference.setChecked(newValue);
    }
}

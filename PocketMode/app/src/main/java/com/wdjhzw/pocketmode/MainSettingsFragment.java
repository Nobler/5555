package com.wdjhzw.pocketmode;


import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.SwitchPreference;
import android.provider.Settings;
import android.util.Log;

public class MainSettingsFragment extends PreferenceFragment implements Preference
        .OnPreferenceChangeListener {
    public static final String KEY_CAN_DRAW_OVERLAYS = "can_draw_overlays";
    public static final String KEY_START_AT_BOOT = "start_at_boot";
    public static final String KEY_SHOW_BLOCKED_INFO = "show_blocked_info";
    private static final String TAG = "MainSettingsFragment";
    private ComponentName mBootReceiver;

    private MainActivity mContext;
    private boolean mCanDrawOverlays;
    private SwitchPreference mDrawOverlays;
    private SwitchPreference mBootStart;
    private SwitchPreference mShowBlockedInfo;

    @SuppressWarnings("deprecation")
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            mContext = (MainActivity) activity;
        }
    }

    /*
     * onAttach(Context) is not called on pre API 23 versions of Android and onAttach(Activity) is
     * deprecated
     */
    @TargetApi(23)
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        mContext = (MainActivity) context;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        addPreferencesFromResource(R.xml.pref_main);

        mBootStart = (SwitchPreference) findPreference(KEY_START_AT_BOOT);
        mBootStart.setOnPreferenceChangeListener(this);
        mShowBlockedInfo = (SwitchPreference) findPreference(KEY_SHOW_BLOCKED_INFO);
        mShowBlockedInfo.setOnPreferenceChangeListener(this);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            // Permission "Draw over other apps" is added in Android M.
            getPreferenceScreen().removePreference(findPreference("permission"));
        } else {
            mDrawOverlays = (SwitchPreference) findPreference(KEY_CAN_DRAW_OVERLAYS);
            mDrawOverlays.setOnPreferenceChangeListener(this);

            findPreference("general").setDependency(KEY_CAN_DRAW_OVERLAYS);
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

    @SuppressWarnings("all")
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
            mContext.getPackageManager().setComponentEnabledSetting(mBootReceiver, value ?
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED : PackageManager
                    .COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
            Log.e(TAG, "" + mContext.getPackageManager().getComponentEnabledSetting(mBootReceiver));
        } else if (preference == mShowBlockedInfo) {
            mContext.startService(new Intent(mContext, MainService.class).setAction(MainService
                    .ACTION_UPDATE_BLOCKED_VIEW).putExtra(MainService
                    .EXTRA_IS_BLOCKED_INFO_VISIBLE, value));
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

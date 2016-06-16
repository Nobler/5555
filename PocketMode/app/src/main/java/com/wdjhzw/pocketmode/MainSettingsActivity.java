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
import android.preference.SwitchPreference;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import java.util.List;

/**
 * A {@link PreferenceActivity} that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 * <p/>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
public class MainSettingsActivity extends AppCompatPreferenceActivity implements Preference
        .OnPreferenceChangeListener {
    private static final String TAG = "MainSettingActivity";

    private static final String KEY_CAN_DRAW_OVERLAYS = "can_draw_overlays";
    private static final String KEY_START_SERVICE = "start_service";
    private static final String KEY_AUTO_START = "auto_start";

    private static final String MAIN_SERVICE = "com.wdjhzw.pocketmode.MainService";

    private boolean mCanDrawOverlays;
    private boolean mIsServiceStart;

    private ComponentName mBootReceiver;

    private SwitchPreference mDrawOverlays;
    private SwitchPreference mStartService;
    private SwitchPreference mBootStart;

    /**
     * Helper method to determine if the device has an extra-large screen. For
     * example, 10" tablets are extra-large.
     */
    private static boolean isXLargeTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout & Configuration
                .SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.pref_main);

        mStartService = (SwitchPreference) findPreference(KEY_START_SERVICE);
        mStartService.setOnPreferenceChangeListener(this);
        mBootStart = (SwitchPreference) findPreference(KEY_AUTO_START);
        mBootStart.setOnPreferenceChangeListener(this);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            // Permission "Draw over other apps" is added in Android M.
            getPreferenceScreen().removePreference(findPreference("permission"));
        } else {
            mDrawOverlays = (SwitchPreference) findPreference(KEY_CAN_DRAW_OVERLAYS);
            mDrawOverlays.setOnPreferenceChangeListener(this);

            mStartService.setDependency(KEY_CAN_DRAW_OVERLAYS);
            mBootStart.setDependency(KEY_CAN_DRAW_OVERLAYS);
        }

        mBootReceiver = new ComponentName(this, MainService.BootReceiver.class);
    }

    @Override
    protected void onResume() {
        super.onResume();

        mIsServiceStart = isServiceRunning(MAIN_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mCanDrawOverlays = Settings.canDrawOverlays(this);
            mDrawOverlays.setChecked(mCanDrawOverlays);

            if (mCanDrawOverlays == false) {
                // If permission is disabled, MainService should be stopped as well.
                if (mIsServiceStart) {
                    setPreference(mStartService, false);
                }

                // auto-start should be disabled too
                if (mBootStart.isChecked()) {
                    setPreference(mBootStart, false);
                }
            }
        }

        mStartService.setChecked(mIsServiceStart);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onIsMultiPane() {
        return isXLargeTablet(this);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        Log.e(TAG, "onPreferenceChange:" + preference.toString() + ":" + newValue.toString());

        boolean value = (Boolean) newValue;

        if (preference == mDrawOverlays) {
            if (value != mCanDrawOverlays) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse
                        ("package:" + getPackageName()));
                startActivity(intent);
                // return false - don't update checkbox until we're really active
                return false;
            }
        } else if (preference == mStartService) {
            if (value != mIsServiceStart) {
                mIsServiceStart = value;

                Intent intent = new Intent(this, MainService.class);

                boolean pass;
                if (mIsServiceStart) {
                    pass = startService(intent) != null;
                } else {
                    pass = stopService(intent);
                }

                // auto enable auto start switch when service is started
                if (pass && mIsServiceStart && !mBootStart.isChecked()) {
                    setPreference(mBootStart, true);
                }

                Toast.makeText(MainSettingsActivity.this, getString(pass ? (mIsServiceStart ? R
                        .string.toast_service_start : R.string.toast_service_stop) : R.string
                        .toast_occur_error), Toast.LENGTH_SHORT).show();
            }
        } else if (preference == mBootStart) {
            getPackageManager().setComponentEnabledSetting(mBootReceiver, value ? PackageManager
                    .COMPONENT_ENABLED_STATE_ENABLED : PackageManager
                    .COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
            Log.e(TAG, "" + getPackageManager().getComponentEnabledSetting(mBootReceiver));
        }
        return true;
    }

    private void setPreference(SwitchPreference preference, boolean newValue) {
        onPreferenceChange(preference, newValue);
        // onPreferenceChanage() is manually called, the check state should be
        // also set manually.
        preference.setChecked(newValue);
    }

    /**
     * Check whether the serviceName is running or not.
     *
     * @param serviceName
     * @return
     */
    public boolean isServiceRunning(String serviceName) {
        ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> runningServices = am.getRunningServices(Integer
                .MAX_VALUE);

        if (runningServices.size() <= 0) {
            return false;
        }

        for (int i = 0; i < runningServices.size(); i++) {
            String service = runningServices.get(i).service.getClassName().toString();

            if (service.equals(serviceName)) {
                Log.e(TAG, serviceName + " is running");
                return true;
            }
        }

        return false;
    }
}

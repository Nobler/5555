package com.wdjhzw.pocketmode;


import android.app.ActivityManager;
import android.app.admin.DeviceAdminReceiver;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.SwitchPreference;
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

    private static final int REQUEST_CODE_ENABLE_ADMIN = 1;
    private static final String KEY_ENABLE_ADMIN = "enable_admin";
    private static final String KEY_START_SERVICE = "start_service";
    private static final String KEY_AUTO_START = "auto_start";

    private static final String MAIN_SERVICE = "com.wdjhzw.pocketmode.MainService";

    private DevicePolicyManager mDPM;
    private ComponentName mDeviceAdmin;

    private boolean mIsAdminActive;
    private boolean mIsServiceStart;

    private ComponentName mBootReceiver;

    private SwitchPreference mEnableAdmin;
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

        // Prepare to work with the DPM
        mDPM = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
        mDeviceAdmin = new ComponentName(this, AdminReceiver.class);

        addPreferencesFromResource(R.xml.pref_main);

        mEnableAdmin = (SwitchPreference) findPreference(KEY_ENABLE_ADMIN);
        mEnableAdmin.setOnPreferenceChangeListener(this);
        mStartService = (SwitchPreference) findPreference(KEY_START_SERVICE);
        mStartService.setOnPreferenceChangeListener(this);
        mBootStart = (SwitchPreference) findPreference(KEY_AUTO_START);
        mBootStart.setOnPreferenceChangeListener(this);


        mBootReceiver = new ComponentName(this, BootReceiver.class);
    }

    @Override
    protected void onResume() {
        super.onResume();

        mIsAdminActive = mDPM.isAdminActive(mDeviceAdmin);
        mEnableAdmin.setChecked(mIsAdminActive);

        mIsServiceStart = isServiceRunning(MAIN_SERVICE);
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

        if (preference == mEnableAdmin) {
            if (value != mIsAdminActive) {
                if (value) {
                    // Launch the activity to have the user enable our admin.
                    Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
                    intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, mDeviceAdmin);
//                    intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, mActivity
//                            .getString(R.string.add_admin_extra_app_text));
                    startActivityForResult(intent, REQUEST_CODE_ENABLE_ADMIN);
                    // return false - don't update checkbox until we're really active
                    return false;
                } else {
                    mDPM.removeActiveAdmin(mDeviceAdmin);
                    mIsAdminActive = false;

                    // If device admin is disabled, MainService should be stopped as well.
                    if (mIsServiceStart) {
                        setPreference(mStartService, false);

                    }

                    // auto-start should be disabled too
                    if (mBootStart.isChecked()) {
                        setPreference(mBootStart, false);
                    }
                }
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

    /**
     * Created by houzhiwei on 16/5/21.
     * <p/>
     * Base class for implementing a device administration component. This class provides a
     * convenience for interpreting the raw intent actions that are sent by the system.
     */
    public static class AdminReceiver extends DeviceAdminReceiver {

        @Override
        public void onDisabled(Context context, Intent intent) {
        }

        @Override
        public void onEnabled(Context context, Intent intent) {
        }
    }
}

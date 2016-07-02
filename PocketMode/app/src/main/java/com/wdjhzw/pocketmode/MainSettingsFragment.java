package com.wdjhzw.pocketmode;


import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.SwitchPreference;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;

public class MainSettingsFragment extends PreferenceFragment implements Preference
        .OnPreferenceChangeListener, SeekBarPreference.OnSeekBarTrackingStateChangedListener {
    public static final String KEY_CAN_DRAW_OVERLAYS = "can_draw_overlays";
    public static final String KEY_START_AT_BOOT = "start_at_boot";
    public static final String KEY_SHOW_BLOCKED_INFO = "show_blocked_info";
    public static final String KEY_BLOCKED_INFO_COORDINATE = "blocked_info_coordinate";
    private static final String TAG = "MainSettingsFragment";
    private ComponentName mBootReceiver;

    private MainActivity mContext;
    private boolean mCanDrawOverlays;
    private SwitchPreference mDrawOverlaysPre;
    private SwitchPreference mBootStartPre;
    private SwitchPreference mShowBlockedInfoPre;
    private SeekBarPreference mBlockedInfoCoordinatePre;
    private View mBlockedInfoPreview;
    private View mBlockedInfo;
    private int mBlockedInfoHeight;
    private int mScreenHeight;
    private int mBlockedInfoCoordinate;

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
    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        mContext = (MainActivity) context;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        addPreferencesFromResource(R.xml.pref_main);

        mBootStartPre = (SwitchPreference) findPreference(KEY_START_AT_BOOT);
        mBootStartPre.setOnPreferenceChangeListener(this);
        mShowBlockedInfoPre = (SwitchPreference) findPreference(KEY_SHOW_BLOCKED_INFO);
        mShowBlockedInfoPre.setOnPreferenceChangeListener(this);
        mBlockedInfoCoordinatePre = (SeekBarPreference) findPreference(KEY_BLOCKED_INFO_COORDINATE);
        mBlockedInfoCoordinatePre.setOnPreferenceChangeListener(this);
        mBlockedInfoCoordinatePre.setOnSeekBarTrackingStateChangedListener(this);

        // Permission "Draw over other apps" is added in Android M.
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            getPreferenceScreen().removePreference(findPreference("permission"));
        } else {
            mDrawOverlaysPre = (SwitchPreference) findPreference(KEY_CAN_DRAW_OVERLAYS);
            mDrawOverlaysPre.setOnPreferenceChangeListener(this);

            findPreference("general").setDependency(KEY_CAN_DRAW_OVERLAYS);
        }

        mBootReceiver = new ComponentName(mContext, MainService.BootReceiver.class);

        mScreenHeight = Utilities.getScreenHeight(mContext);
    }

    @Override
    public void onResume() {
        super.onResume();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mCanDrawOverlays = Settings.canDrawOverlays(mContext);
            mDrawOverlaysPre.setChecked(mCanDrawOverlays);

            if (!mCanDrawOverlays) {
                mContext.setFabEnabled(false);

                // auto-start should be disabled too
                if (mBootStartPre.isChecked()) {
                    setPreference(mBootStartPre, false);
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

        if (preference == mDrawOverlaysPre) {
            if ((Boolean) newValue != mCanDrawOverlays) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse
                        ("package:" + mContext.getPackageName()));
                startActivity(intent);
                // return false - don't update checkbox until we're really active
                return false;
            }
        } else if (preference == mBootStartPre) {
            mContext.getPackageManager().setComponentEnabledSetting(mBootReceiver, (Boolean)
                    newValue ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED : PackageManager
                    .COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
        } else if (preference == mShowBlockedInfoPre) {
            // Only when service is running, send request to service to set blocked info's visibility.
            if (mContext.isServiceRunning()) {
                mContext.startService(new Intent(mContext, MainService.class).setAction
                        (MainService.ACTION_UPDATE_BLOCKED_VIEW).putExtra(MainService
                        .EXTRA_IS_BLOCKED_INFO_VISIBLE, (Boolean) newValue));
            }
        } else if (preference == mBlockedInfoCoordinatePre) {
            mBlockedInfoCoordinate = (int) newValue;
            mBlockedInfo.setY((mScreenHeight - mBlockedInfoHeight / 2) / 100 *
                    mBlockedInfoCoordinate);

            // Whether service is running or not, don't send request to service to set blocked
            // info's coordinate, while value of SeekBarPreference is still changing, or service
            // will be started unintentionally.
        }

        return true;
    }

    private void setPreference(SwitchPreference preference, boolean newValue) {
        onPreferenceChange(preference, newValue);
        // onPreferenceChanage() is manually called, the check state should be also set manually.
        preference.setChecked(newValue);
    }

    @Override
    public void onStartTrackingTouch() {
        Log.e(TAG, "onStartTrackingTouch");

        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams(WindowManager
                .LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_PHONE, 0, PixelFormat.TRANSLUCENT);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            layoutParams.flags |= WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS;
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            layoutParams.flags |= WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
        }

        mBlockedInfoPreview = ((LayoutInflater) mContext.getSystemService(Context
                .LAYOUT_INFLATER_SERVICE)).inflate(R.layout.blocked_info_preview, null);
        mBlockedInfo = mBlockedInfoPreview.findViewById(R.id.blocked_info);

        ViewTreeObserver vto = mBlockedInfo.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                Utilities.removeOnGlobalLayoutListener(mBlockedInfo, this);
                mBlockedInfoHeight = mBlockedInfo.getHeight();
            }
        });

        mContext.getWindowManager().addView(mBlockedInfoPreview, layoutParams);
    }

    @Override
    public void onStopTrackingTouch(int progress) {
        Log.e(TAG, "onStopTrackingTouch");

        mContext.getWindowManager().removeView(mBlockedInfoPreview);

        // After SeekBar tracking stop, if service is running, send request to service to set
        // blocked info's coordinate.
        if (mContext.isServiceRunning()) {
            mContext.startService(new Intent(mContext, MainService.class).setAction(MainService
                    .ACTION_SET_BLOCKED_INFO_COORDINATE).putExtra(MainService
                    .EXTRA_BLOCKED_INFO_COORDINAT, mBlockedInfoCoordinate));
        }
    }
}

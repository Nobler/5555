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
        .OnPreferenceChangeListener, CoordinatePickerPreference
        .OnSeekBarTrackingStateChangedListener {
    public static final String KEY_CAN_DRAW_OVERLAYS = "can_draw_overlays";
    public static final String KEY_START_AT_BOOT = "start_at_boot";
    public static final String KEY_SHOW_BLOCKED_INFO = "show_blocked_info";
    public static final String KEY_BLOCKED_INFO_POS = "blocked_info_pos";
    private static final String TAG = "MainSettingsFragment";
    private ComponentName mBootReceiver;

    private MainActivity mContext;
    private boolean mCanDrawOverlays;
    private SwitchPreference mDrawOverlays;
    private SwitchPreference mBootStart;
    private SwitchPreference mShowBlockedInfo;
    private CoordinatePickerPreference mBlockedInfoPos;
    private WindowManager.LayoutParams mLayoutParams;
    private View mBlockedInfoPreview;
    private View mBlockedInfo;
    private int mBlockedInfoHeight;
    private int mScreenHeight;

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
        mBlockedInfoPos = (CoordinatePickerPreference) findPreference(KEY_BLOCKED_INFO_POS);
        mBlockedInfoPos.setOnPreferenceChangeListener(this);
        mBlockedInfoPos.setOnSeekBarTrackingStateChangedListener(this);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            // Permission "Draw over other apps" is added in Android M.
            getPreferenceScreen().removePreference(findPreference("permission"));
        } else {
            mDrawOverlays = (SwitchPreference) findPreference(KEY_CAN_DRAW_OVERLAYS);
            mDrawOverlays.setOnPreferenceChangeListener(this);

            findPreference("general").setDependency(KEY_CAN_DRAW_OVERLAYS);
        }

        mBootReceiver = new ComponentName(mContext, MainService.BootReceiver.class);

        mScreenHeight = ((WindowManager) mContext.getSystemService(Context
                .WINDOW_SERVICE)).getDefaultDisplay().getHeight();
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

        if (preference == mDrawOverlays) {
            if ((Boolean) newValue != mCanDrawOverlays) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse
                        ("package:" + mContext.getPackageName()));
                startActivity(intent);
                // return false - don't update checkbox until we're really active
                return false;
            }
        } else if (preference == mBootStart) {
            mContext.getPackageManager().setComponentEnabledSetting(mBootReceiver, (Boolean)
                    newValue ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED : PackageManager
                    .COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
            Log.e(TAG, "" + mContext.getPackageManager().getComponentEnabledSetting(mBootReceiver));
        } else if (preference == mShowBlockedInfo) {
            if (mContext.isServiceStarted()) {
                mContext.startService(new Intent(mContext, MainService.class).setAction
                        (MainService.ACTION_UPDATE_BLOCKED_VIEW).putExtra(MainService
                        .EXTRA_IS_BLOCKED_INFO_VISIBLE, (Boolean) newValue));
            }
        } else if (preference == mBlockedInfoPos) {
            if (mContext.isServiceStarted()) {
                mContext.startService(new Intent(mContext, MainService.class).setAction
                        (MainService.ACTION_SET_BLOCKED_INFO_COORDINATE).putExtra(MainService
                        .EXTRA_BLOCKED_INFO_COORDINAT, (Integer) newValue));
            }

            mBlockedInfo.setY((mScreenHeight - mBlockedInfoHeight / 2) / 100 * (int) newValue);
        }

        return true;
    }

    private void setPreference(SwitchPreference preference, boolean newValue) {
        onPreferenceChange(preference, newValue);
        // onPreferenceChanage() is manually called, the check state should be
        // also set manually.
        preference.setChecked(newValue);
    }

    @Override
    public void onStartTrackingTouch() {
        Log.e(TAG, "onStartTrackingTouch");
        mLayoutParams = new WindowManager.LayoutParams(WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.TYPE_PHONE,
                0, PixelFormat.TRANSLUCENT);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mLayoutParams.flags |= WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS;
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            mLayoutParams.flags |= WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
        }
        mBlockedInfoPreview = ((LayoutInflater) mContext.getSystemService(Context
                .LAYOUT_INFLATER_SERVICE)).inflate(R.layout.blocked_info_preview, null);
        mBlockedInfo = mBlockedInfoPreview.findViewById(R.id.blocked_info);
        ViewTreeObserver vto = mBlockedInfo.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mBlockedInfo.getViewTreeObserver().removeGlobalOnLayoutListener(this);

                mBlockedInfoHeight = mBlockedInfo.getHeight();

            }
        });
        mContext.getWindowManager().addView(mBlockedInfoPreview, mLayoutParams);
    }

    @Override
    public void onStopTrackingTouch(int progress) {
        Log.e(TAG, "onStopTrackingTouch");
        mContext.getWindowManager().removeView(mBlockedInfoPreview);
    }
}

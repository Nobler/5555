package com.wdjhzw.pocketmode;

import android.app.ActivityManager;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final String MAIN_SERVICE = "com.wdjhzw.pocketmode.MainService";

    private CheckableFab mFab;
    private boolean mIsServiceStart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        loadFragment();

        mFab = (CheckableFab) findViewById(R.id.fab);
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (view.getId() == R.id.fab) {
                    if (!mFab.isEnabled()) {
                        Snackbar.make(view, R.string.permission_hint, Snackbar.LENGTH_LONG)
                                .setAction(R.string.jump_to, new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        Intent intent = new Intent(Settings
                                                .ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse
                                                ("package:" + MainActivity.this.getPackageName()));
                                        startActivity(intent);
                                    }
                                }).show();

                        return;
                    }

                    Intent intent = new Intent(MainActivity.this, MainService.class);

                    boolean pass;
                    if (mIsServiceStart) {
                        pass = MainActivity.this.stopService(intent);
                    } else {
                        pass = MainActivity.this.startService(intent) != null;
                    }

                    if (pass) {
                        mIsServiceStart = !mIsServiceStart;
                        mFab.setChecked(mIsServiceStart);
                    }

                    Toast.makeText(MainActivity.this, getString(pass ? (mIsServiceStart ? R
                            .string.toast_service_start : R.string.toast_service_stop) : R.string
                            .toast_occur_error), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void loadFragment() {
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.main, new MainSettingsFragment());
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mIsServiceStart = isServiceRunning(MAIN_SERVICE);
        mFab.setChecked(mIsServiceStart);
    }

    public void setFabEnabled(boolean enabled) {
        if (!enabled && mIsServiceStart) {
            stopService(new Intent(this, MainService.class));
        }
        mFab.setEnabled(enabled);
    }

    /**
     * Check whether the serviceName is running or not.
     */
    public boolean isServiceRunning(String serviceName) {
        ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> runningServices = am.getRunningServices(Integer
                .MAX_VALUE);

        if (runningServices.size() <= 0) {
            return false;
        }

        for (int i = 0; i < runningServices.size(); i++) {
            String service = runningServices.get(i).service.getClassName();

            if (service.equals(serviceName)) {
                Log.e(TAG, serviceName + " is running");
                return true;
            }
        }

        return false;
    }
}

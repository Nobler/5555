package com.wdjhzw.pocketmode;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.wdjhzw.pocketmode.widget.CheckableFab;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final String MAIN_SERVICE = "com.wdjhzw.pocketmode.MainService";

    private CheckableFab mFab;
    private boolean mIsServiceRunning;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        loadFragment();

        mFab = (CheckableFab) findViewById(R.id.fab);
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (view.getId() == R.id.fab) {
                    if (!mFab.isEnabled()) {
                        Snackbar.make(view, R.string.permission_hint, Snackbar.LENGTH_LONG)
                                .setAction(R.string.jump_to, new View.OnClickListener() {
                                    @TargetApi(Build.VERSION_CODES.M)
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
                    if (mIsServiceRunning) {
                        pass = MainActivity.this.stopService(intent);
                    } else {
                        pass = MainActivity.this.startService(intent) != null;
                    }

                    if (pass) {
                        mIsServiceRunning = !mIsServiceRunning;
                        mFab.setChecked(mIsServiceRunning);
                    }

                    Toast.makeText(MainActivity.this, getString(pass ? (mIsServiceRunning ? R
                            .string.toast_service_start : R.string.toast_service_stop) : R.string
                            .toast_occur_error), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void loadFragment() {
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.settings, new MainSettingsFragment());
//        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mIsServiceRunning = isServiceRunning(MAIN_SERVICE);
        mFab.setChecked(mIsServiceRunning);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        Utilities.log(TAG, "onDestory");
    }

    public void setFabEnabled(boolean enabled) {
        if (!enabled && mIsServiceRunning) {
            stopService(new Intent(this, MainService.class));
        }
        mFab.setEnabled(enabled);
    }

    public boolean isServiceRunning() {
        return mIsServiceRunning;
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
                Utilities.log(TAG, serviceName + " is running");
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_scrolling, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_about) {
            startActivity(new Intent(MainActivity.this, AboutActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}

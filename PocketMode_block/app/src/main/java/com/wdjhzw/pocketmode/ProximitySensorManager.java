/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License
 */
package com.wdjhzw.pocketmode;

import android.content.Context;
import android.os.PowerManager;
import android.util.Log;

/**
 * This class manages the proximity sensor and allows callers to turn it on and off.
 */
public class ProximitySensorManager {
    private static final String TAG = ProximitySensorManager.class.getSimpleName();

    private final PowerManager.WakeLock mProximityWakeLock;

    public ProximitySensorManager(Context context) {
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);

//        if (pm.isWakeLockLevelSupported(PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK)) {
        mProximityWakeLock = pm.newWakeLock(PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK, TAG);
//        } else {
//            mProximityWakeLock = null;
//        }
        Log.d(TAG, "onCreate: mProximityWakeLock: " + mProximityWakeLock);
    }

    /**
     * Turn the proximity sensor on.
     */
    void turnOn() {
        if (mProximityWakeLock == null) {
            return;
        }
        if (!mProximityWakeLock.isHeld()) {
            Log.e(TAG, "Acquiring proximity wake lock");
            mProximityWakeLock.acquire();
        } else {
            Log.e(TAG, "Proximity wake lock already acquired");
        }
    }

    /**
     * Turn the proximity sensor off.
     */
    void turnOff(/*boolean screenOnImmediately*/) {
        if (mProximityWakeLock == null) {
            return;
        }
        if (mProximityWakeLock.isHeld()) {
            Log.e(TAG, "Releasing proximity wake lock");
//            int flags =
//                (screenOnImmediately ? 0 : PowerManager.RELEASE_FLAG_WAIT_FOR_NO_PROXIMITY);
            mProximityWakeLock.release(/*flags*/);
        } else {
            Log.e(TAG, "Proximity wake lock already released");
        }
    }
}

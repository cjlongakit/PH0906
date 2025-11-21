package com.example.ph906_spalshscreen.notifications;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import java.util.concurrent.TimeUnit;

/**
 * Receives BOOT_COMPLETED broadcast to restart background notification checks
 * after device reboot or app update.
 */
public class BootReceiver extends BroadcastReceiver {
    private static final String TAG = "BootReceiver";
    private static final String UNIQUE_WORK = "ph906_sync_work";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.d(TAG, "Received broadcast: " + action);

        if (Intent.ACTION_BOOT_COMPLETED.equals(action) ||
            Intent.ACTION_MY_PACKAGE_REPLACED.equals(action) ||
            "android.intent.action.QUICKBOOT_POWERON".equals(action)) {

            Log.d(TAG, "Restarting background notification checks");

            // Ensure notification channel exists
            NotificationUtils.ensureChannel(context);

            // Reschedule the periodic work
            Constraints constraints = new Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build();

            PeriodicWorkRequest request = new PeriodicWorkRequest.Builder(
                    SyncWorker.class,
                    15,
                    TimeUnit.MINUTES
            )
                    .setConstraints(constraints)
                    .build();

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                    UNIQUE_WORK,
                    ExistingPeriodicWorkPolicy.KEEP, // Keep existing if already scheduled
                    request
            );

            Log.d(TAG, "Background work rescheduled successfully");
        }
    }
}


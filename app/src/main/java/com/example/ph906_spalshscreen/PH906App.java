package com.example.ph906_spalshscreen;

import android.app.Application;
import android.util.Log;

import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.example.ph906_spalshscreen.notifications.NotificationUtils;
import com.example.ph906_spalshscreen.notifications.SyncWorker;

import java.util.concurrent.TimeUnit;

public class PH906App extends Application {
    private static final String TAG = "PH906App";
    private static final String UNIQUE_WORK = "ph906_sync_work";

    @Override
    public void onCreate() {
        super.onCreate();

        Log.d(TAG, "PH906App onCreate - Setting up background notifications");

        // Ensure notification channel exists
        NotificationUtils.ensureChannel(this);

        // Schedule periodic background sync (15 min minimum interval for Android)
        // This will run even when the app is closed
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .setRequiresBatteryNotLow(false) // Run even on low battery
                .build();

        PeriodicWorkRequest request = new PeriodicWorkRequest.Builder(
                SyncWorker.class,
                15,
                TimeUnit.MINUTES
        )
                .setConstraints(constraints)
                .setInitialDelay(1, TimeUnit.MINUTES) // Start checking after 1 minute
                .build();

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                UNIQUE_WORK,
                ExistingPeriodicWorkPolicy.KEEP, // Keep existing work if already scheduled
                request
        );

        Log.d(TAG, "Background notification work scheduled - will run every 15 minutes");
    }
}

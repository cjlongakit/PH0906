package com.example.ph906_spalshscreen;

import android.app.Application;

import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.example.ph906_spalshscreen.notifications.NotificationUtils;
import com.example.ph906_spalshscreen.notifications.SyncWorker;

import java.util.concurrent.TimeUnit;

public class PH906App extends Application {
    private static final String UNIQUE_WORK = "ph906_sync_work";

    @Override
    public void onCreate() {
        super.onCreate();

        // Ensure notification channel exists
        NotificationUtils.ensureChannel(this);

        // Schedule periodic background sync (15 min min interval)
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();
        PeriodicWorkRequest request = new PeriodicWorkRequest.Builder(SyncWorker.class, 15, TimeUnit.MINUTES)
                .setConstraints(constraints)
                .build();
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                UNIQUE_WORK,
                ExistingPeriodicWorkPolicy.UPDATE,
                request
        );
    }
}


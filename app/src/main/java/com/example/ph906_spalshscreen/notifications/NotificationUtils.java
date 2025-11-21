package com.example.ph906_spalshscreen.notifications;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.example.ph906_spalshscreen.R;

public class NotificationUtils {
    private static final String TAG = "NotificationUtils";
    public static final String CHANNEL_ID = "ph906_updates";
    public static final String CHANNEL_NAME = "PH906 Updates";

    public static void ensureChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH); // Changed to HIGH for better visibility
            channel.setDescription("Notifications for new letters and events");
            channel.enableLights(true);
            channel.setLightColor(Color.BLUE);
            channel.enableVibration(true);
            channel.setShowBadge(true); // Show badge on app icon
            NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (nm != null) {
                nm.createNotificationChannel(channel);
                Log.d(TAG, "Notification channel created: " + CHANNEL_ID);
            } else {
                Log.e(TAG, "NotificationManager is null, cannot create channel");
            }
        }
    }

    public static void notify(Context context, int id, String title, String message, PendingIntent contentIntent) {
        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (nm == null) {
            Log.e(TAG, "NotificationManager is null, cannot post notification");
            return;
        }

        // Check if notifications are enabled
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && !nm.areNotificationsEnabled()) {
            Log.w(TAG, "Notifications are disabled by user");
            return;
        }

        try {
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_notification)
                    .setContentTitle(title)
                    .setContentText(message)
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                    .setAutoCancel(true)
                    .setContentIntent(contentIntent)
                    .setPriority(NotificationCompat.PRIORITY_HIGH) // Changed to HIGH
                    .setDefaults(NotificationCompat.DEFAULT_ALL)
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC) // Show on lock screen
                    .setCategory(NotificationCompat.CATEGORY_MESSAGE); // Categorize as message

            nm.notify(id, builder.build());
            Log.d(TAG, "Notification posted: id=" + id + ", title=" + title);
        } catch (Exception e) {
            Log.e(TAG, "Failed to post notification: " + e.getMessage(), e);
        }
    }
}

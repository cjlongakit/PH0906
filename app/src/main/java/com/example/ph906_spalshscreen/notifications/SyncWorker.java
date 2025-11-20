package com.example.ph906_spalshscreen.notifications;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Constraints;
import androidx.work.NetworkType;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.ph906_spalshscreen.PrefsHelper;
import com.example.ph906_spalshscreen.api.ApiClient;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class SyncWorker extends Worker {
    private static final String TAG = "SyncWorker";
    private static final String BASE = "https://hjcdc.swuitapp.com/api";
    private final OkHttpClient http = new OkHttpClient();

    public SyncWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.d(TAG, "SyncWorker started");
        Context ctx = getApplicationContext();
        PrefsHelper prefs = new PrefsHelper(ctx);
        if (!prefs.isLoggedIn()) {
            Log.d(TAG, "User not logged in, skipping sync");
            return Result.success();
        }

        // Ensure channel exists
        NotificationUtils.ensureChannel(ctx);

        try {
            // Letters: only for current user
            String lettersSig = fetchLettersSignature(prefs);
            if (lettersSig != null) {
                String last = prefs.getLastLettersSig();
                Log.d(TAG, "Letters signature - Current: " + lettersSig + ", Last: " + last);
                if (last != null && !last.equals(lettersSig)) {
                    Log.d(TAG, "Letters changed, posting notification");
                    postLettersNotification(ctx);
                }
                prefs.saveLastLettersSig(lettersSig);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error checking letters: " + e.getMessage(), e);
        }

        try {
            String eventsSig = fetchEventsSignature();
            if (eventsSig != null) {
                String last = new PrefsHelper(ctx).getLastEventsSig();
                Log.d(TAG, "Events signature - Current: " + eventsSig + ", Last: " + last);
                if (last != null && !last.equals(eventsSig)) {
                    Log.d(TAG, "Events changed, posting notification");
                    postEventsNotification(ctx);
                }
                new PrefsHelper(ctx).saveLastEventsSig(eventsSig);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error checking events: " + e.getMessage(), e);
        }

        Log.d(TAG, "SyncWorker completed");
        return Result.success();
    }

    private String fetchLettersSignature(PrefsHelper prefs) throws IOException {
        String rawId = prefs.getPh906();
        if (rawId == null || rawId.isEmpty()) return null;
        String digits = rawId.replaceAll("[^0-9]", "");
        String url = BASE + "/get_students.php?_ts=" + System.currentTimeMillis();
        Request req = new Request.Builder().url(url).get().addHeader("Accept","application/json").build();
        try (Response res = http.newCall(req).execute()) {
            if (!res.isSuccessful()) return null;
            String body = res.body() != null ? res.body().string() : "";
            JSONObject jo = new JSONObject(body);
            JSONArray data = jo.optJSONArray("data");
            if (data == null) return null;
            List<String> parts = new ArrayList<>();
            for (int i = 0; i < data.length(); i++) {
                JSONObject o = data.getJSONObject(i);
                String ph = o.optString("ph906", "");
                String phDigits = ph.replaceAll("[^0-9]", "");
                if (!phDigits.equals(digits)) continue; // keep only current user
                String type = o.optString("type", "");
                String deadline = o.optString("deadline", "");
                String status = o.optString("status", "");
                parts.add(type + "|" + deadline + "|" + status);
            }
            if (parts.isEmpty()) return "none"; // no letters for user yet
            Collections.sort(parts);
            return String.join(",", parts);
        } catch (Exception e) {
            return null;
        }
    }

    private String fetchEventsSignature() throws IOException {
        String url = BASE + "/events.php?upcoming=1&_ts=" + System.currentTimeMillis();
        Request req = new Request.Builder().url(url).get().addHeader("Accept","application/json").build();
        try (Response res = http.newCall(req).execute()) {
            if (!res.isSuccessful()) return null;
            String body = res.body() != null ? res.body().string() : "";
            JSONArray arr;
            String t = body.trim();
            if (t.startsWith("[")) {
                arr = new JSONArray(t);
            } else if (t.startsWith("{")) {
                JSONObject jo = new JSONObject(t);
                arr = jo.optJSONArray("data");
                if (arr == null) arr = new JSONArray();
            } else {
                arr = new JSONArray();
            }
            List<String> parts = new ArrayList<>();
            int n = Math.min(3, arr.length());
            for (int i = 0; i < n; i++) {
                JSONObject o = arr.getJSONObject(i);
                String date = o.optString("date", o.optString("event_date", ""));
                String title = o.optString("title", o.optString("event_title", ""));
                parts.add(date + "|" + title);
            }
            if (parts.isEmpty()) return "none";
            Collections.sort(parts);
            return String.join(",", parts);
        } catch (Exception e) {
            return null;
        }
    }

    private void postLettersNotification(Context ctx) {
        Intent intent = ctx.getPackageManager().getLaunchIntentForPackage(ctx.getPackageName());
        int flags = PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE;
        PendingIntent pi = PendingIntent.getActivity(ctx, 1001, intent, flags);
        NotificationUtils.notify(ctx, 1001, "New Letter", "You have a new letter update.", pi);
    }

    private void postEventsNotification(Context ctx) {
        Intent intent = ctx.getPackageManager().getLaunchIntentForPackage(ctx.getPackageName());
        int flags = PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE;
        PendingIntent pi = PendingIntent.getActivity(ctx, 1002, intent, flags);
        NotificationUtils.notify(ctx, 1002, "New Event", "A new event has been posted.", pi);
    }

    // Helper to schedule/refresh from Application if desired
    public static void schedule(Context ctx) {
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();
        // Keep using existing unique work if set by PH906App; here just placeholder
        WorkManager.getInstance(ctx); // ensure initialized
    }
}

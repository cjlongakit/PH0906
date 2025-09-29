package com.example.ph906_spalshscreen;

import android.app.Application;
import android.content.SharedPreferences;
import android.util.Log;

public class App extends Application {
    private static final String PREF = "app_crash_capture";
    private static final String KEY_LAST_CRASH = "last_crash";

    @Override
    public void onCreate() {
        super.onCreate();
        Thread.UncaughtExceptionHandler defaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
            try {
                String stack = Log.getStackTraceString(e);
                SharedPreferences sp = getSharedPreferences(PREF, MODE_PRIVATE);
                sp.edit().putString(KEY_LAST_CRASH, stack).apply();
            } catch (Throwable ignored) {}
            if (defaultHandler != null) defaultHandler.uncaughtException(t, e);
        });
    }

    public static String consumeLastCrash(Application app) {
        try {
            SharedPreferences sp = app.getSharedPreferences(PREF, MODE_PRIVATE);
            String s = sp.getString(KEY_LAST_CRASH, null);
            if (s != null) sp.edit().remove(KEY_LAST_CRASH).apply();
            return s;
        } catch (Throwable e) { return null; }
    }
}


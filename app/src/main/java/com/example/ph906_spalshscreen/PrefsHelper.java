package com.example.ph906_spalshscreen;

import android.content.Context;
import android.content.SharedPreferences;

public class PrefsHelper {
    private static final String PREF_NAME = "student_prefs"; // unified name

    private static final String KEY_TOKEN = "token";
    private static final String KEY_PH906 = "ph906";
    private static final String KEY_FULL_NAME = "full_name";
    private static final String KEY_BIRTHDAY = "birthday";
    private static final String KEY_VERSION = "version"; // minor/adult
    private static final String KEY_IS_DEFAULT_PASSWORD = "is_default_password";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    // Legacy single key
    private static final String KEY_PROFILE_PHOTO_URI = "profile_photo_uri";
    // New explicit keys
    private static final String KEY_PROFILE_PHOTO_SERVER_URL = "profile_photo_server_url";
    private static final String KEY_PROFILE_PHOTO_LOCAL_URI = "profile_photo_local_uri";

    private final SharedPreferences prefs;
    private final SharedPreferences.Editor editor;

    public PrefsHelper(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    // ===== Save login info =====
    public void saveLoginInfo(String ph906, String token, String fullName, boolean isDefaultPassword) {
        editor.putString(KEY_PH906, ph906);
        editor.putString(KEY_TOKEN, token);
        editor.putString(KEY_FULL_NAME, fullName);
        editor.putBoolean(KEY_IS_DEFAULT_PASSWORD, isDefaultPassword);
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.apply();
    }

    public void saveBirthday(String birthday) {
        editor.putString(KEY_BIRTHDAY, birthday);
        editor.apply();
    }

    public void saveVersion(String version) {
        editor.putString(KEY_VERSION, version);
        editor.apply();
    }

    // Legacy method: keep working by preferring server URL
    public void saveProfilePhotoUri(String uri) {
        // Assume server URL for better persistence (older code paths)
        editor.putString(KEY_PROFILE_PHOTO_SERVER_URL, uri);
        editor.putString(KEY_PROFILE_PHOTO_URI, uri);
        editor.apply();
    }

    public String getProfilePhotoUri() {
        String server = getServerPhotoUrl();
        if (server != null && !server.isEmpty()) return server;
        String local = getLocalPhotoUri();
        if (local != null && !local.isEmpty()) return local;
        return prefs.getString(KEY_PROFILE_PHOTO_URI, null);
    }

    // New explicit API
    public void saveServerPhotoUrl(String url) {
        editor.putString(KEY_PROFILE_PHOTO_SERVER_URL, url);
        // keep legacy in sync
        editor.putString(KEY_PROFILE_PHOTO_URI, url);
        editor.apply();
    }

    public String getServerPhotoUrl() {
        return prefs.getString(KEY_PROFILE_PHOTO_SERVER_URL, null);
    }

    public void saveLocalPhotoUri(String uri) {
        editor.putString(KEY_PROFILE_PHOTO_LOCAL_URI, uri);
        editor.apply();
    }

    public String getLocalPhotoUri() {
        return prefs.getString(KEY_PROFILE_PHOTO_LOCAL_URI, null);
    }

    // ===== Getters =====
    public String getPh906() { return prefs.getString(KEY_PH906, null); }
    public String getToken() { return prefs.getString(KEY_TOKEN, null); }
    public String getFullName() { return prefs.getString(KEY_FULL_NAME, ""); }
    public boolean isDefaultPassword() { return prefs.getBoolean(KEY_IS_DEFAULT_PASSWORD, true); }
    public String getBirthday() { return prefs.getString(KEY_BIRTHDAY, null); }
    public String getVersion() { return prefs.getString(KEY_VERSION, "minor"); }

    // ===== Session check =====
    public boolean isLoggedIn() { return prefs.getBoolean(KEY_IS_LOGGED_IN, false); }

    public void clearAll() { editor.clear().apply(); }
}

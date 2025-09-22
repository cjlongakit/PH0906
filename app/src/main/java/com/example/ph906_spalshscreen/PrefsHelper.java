package com.example.ph906_spalshscreen;

import android.content.Context;
import android.content.SharedPreferences;

public class PrefsHelper {
    private static final String PREF_NAME = "UserPrefs";

    private static final String KEY_TOKEN = "token";
    private static final String KEY_PH906 = "ph906";
    private static final String KEY_FULL_NAME = "full_name";
    private static final String KEY_BIRTHDAY = "birthday";
    private static final String KEY_VERSION = "version"; // minor/adult
    private static final String KEY_IS_DEFAULT_PASSWORD = "is_default_password";

    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;

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

    // ===== Getters =====
    public String getPh906() {
        return prefs.getString(KEY_PH906, null);
    }

    public String getToken() {
        return prefs.getString(KEY_TOKEN, null);
    }

    public String getFullName() {
        return prefs.getString(KEY_FULL_NAME, null);
    }

    public boolean isDefaultPassword() {
        return prefs.getBoolean(KEY_IS_DEFAULT_PASSWORD, true);
    }

    public String getBirthday() {
        return prefs.getString(KEY_BIRTHDAY, null);
    }

    public String getVersion() {
        return prefs.getString(KEY_VERSION, "minor");
    }

    // ===== Session check =====
    public boolean isLoggedIn() {
        return getToken() != null && getPh906() != null;
    }

    public void clearAll() {
        editor.clear().apply();
    }
}

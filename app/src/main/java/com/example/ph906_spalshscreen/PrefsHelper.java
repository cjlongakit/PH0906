package com.example.ph906_spalshscreen;

import android.content.Context;
import android.content.SharedPreferences;

public class PrefsHelper {
    private static final String PREF_NAME = "UserPrefs";
    private static final String KEY_TOKEN = "token";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_AGE = "age";

    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;

    public PrefsHelper(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    public void saveToken(String token) {
        editor.putString(KEY_TOKEN, token);
        editor.apply();
    }

    public String getToken() {
        return prefs.getString(KEY_TOKEN, null);
    }

    public void saveUsername(String username) {
        editor.putString(KEY_USERNAME, username);
        editor.apply();
    }

    public String getUsername() {
        return prefs.getString(KEY_USERNAME, null);
    }

    public void saveAge(int age) {
        editor.putInt(KEY_AGE, age);
        editor.apply();
    }

    public int getAge() {
        return prefs.getInt(KEY_AGE, 0);
    }

    public void clearAll() {
        editor.clear().apply();
    }
}

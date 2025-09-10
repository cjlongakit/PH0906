package com.example.ph906_spalshscreen;

import android.content.Context;
import android.content.SharedPreferences;

public class PrefsHelper {
    private static final String PREF_NAME = "UserPrefs";
    private static final String KEY_PASSWORD = "password";

    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;

    public PrefsHelper(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    public void savePassword(String password) {
        editor.putString(KEY_PASSWORD, password);
        editor.apply();
    }

    public String getPassword() {
        return prefs.getString(KEY_PASSWORD, "PH0906"); // default password
    }
}

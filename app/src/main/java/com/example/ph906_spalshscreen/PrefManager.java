package com.example.ph906_spalshscreen;

import android.content.Context;
import android.content.SharedPreferences;

public class PrefManager {
    private static final String PREF_NAME = "terms_privacy_prefs";
    private static final String KEY_TERMS_PREFIX = "terms_accepted_";
    private static final String KEY_PRIVACY_PREFIX = "privacy_accepted_";

    private final SharedPreferences prefs;

    public PrefManager(Context ctx) {
        prefs = ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    private String normVersion(String v) {
        if (v == null || v.trim().isEmpty()) return "minor"; // safe default
        String n = v.trim().toLowerCase();
        if (!n.equals("minor") && !n.equals("adult")) return "minor";
        return n;
    }

    public boolean isTermsAccepted(String version) {
        return prefs.getBoolean(KEY_TERMS_PREFIX + normVersion(version), false);
    }

    public void acceptTerms(String version, boolean accepted) {
        prefs.edit().putBoolean(KEY_TERMS_PREFIX + normVersion(version), accepted).apply();
    }

    public boolean isPrivacyAccepted(String version) {
        return prefs.getBoolean(KEY_PRIVACY_PREFIX + normVersion(version), false);
    }

    public void acceptPrivacy(String version, boolean accepted) {
        prefs.edit().putBoolean(KEY_PRIVACY_PREFIX + normVersion(version), accepted).apply();
    }

    public void clearAll() { prefs.edit().clear().apply(); }
}
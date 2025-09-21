package com.example.ph906_spalshscreen;

import android.content.Context;
import android.content.SharedPreferences;

public class PrefManager {

    private static final String PREF_NAME = "user_session";
    private SharedPreferences prefs;

    public PrefManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void setTermsAccepted(String version) {
        prefs.edit().putBoolean("accepted_terms_" + version, true).apply();
    }

    public void setPrivacyAccepted(String version) {
        prefs.edit().putBoolean("accepted_privacy_" + version, true).apply();
    }

    public boolean isTermsAccepted(String version) {
        return prefs.getBoolean("accepted_terms_" + version, false);
    }

    public boolean isPrivacyAccepted(String version) {
        return prefs.getBoolean("accepted_privacy_" + version, false);
    }

    public boolean isAllAccepted(String version) {
        return isTermsAccepted(version) && isPrivacyAccepted(version);
    }
}

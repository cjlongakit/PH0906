package com.example.ph906_spalshscreen;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ph906_spalshscreen.api.ApiClient;

public class SplashActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler().postDelayed(() -> {
            ApiClient apiClient = new ApiClient(SplashActivity.this);
            PrefManager pref = new PrefManager(SplashActivity.this);

            boolean isLoggedIn = apiClient.isLoggedIn();
            Log.d("DEBUG", "SplashActivity: isLoggedIn = " + isLoggedIn);

            if (!isLoggedIn) {
                Log.d("DEBUG", "Going to LoginActivity");
                Intent loginIntent = new Intent(SplashActivity.this, LoginActivity.class);
                loginIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(loginIntent);
                finish();
                return;
            }

            // Get saved version: "minor" or "adult"
            String version = apiClient.getSavedVersion();

            Intent nextIntent;
            if (!pref.isTermsAccepted(version)) {
                Log.d("DEBUG", "Going to TermsActivity");
                nextIntent = new Intent(SplashActivity.this, TermsActivity.class)
                        .putExtra("version", version);
            } else if (!pref.isPrivacyAccepted(version)) {
                Log.d("DEBUG", "Going to PrivacyActivity");
                nextIntent = new Intent(SplashActivity.this, PrivacyActivity.class)
                        .putExtra("version", version);
            } else {
                Log.d("DEBUG", "Going to MainActivity");
                nextIntent = new Intent(SplashActivity.this, MainActivity.class)
                        .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            }

            nextIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(nextIntent);
            finish();

        }, 2000); // 2-second splash delay
    }
}

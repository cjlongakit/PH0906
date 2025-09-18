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
            ApiClient apiClient = new ApiClient(this);
            boolean isLoggedIn = apiClient.isLoggedIn();

            Log.d("DEBUG", "SplashActivity: isLoggedIn = " + isLoggedIn);

            if (isLoggedIn) {
                Log.d("DEBUG", "Going to MainActivity");
                startActivity(new Intent(this, MainActivity.class));
            } else {
                Log.d("DEBUG", "Going to LoginActivity");
                startActivity(new Intent(this, LoginActivity.class));
            }
            finish();
        }, 2000);
    }
}
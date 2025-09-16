package com.example.ph906_spalshscreen;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.ph906_spalshscreen.ui.privacy.DataPrivacyAdultFragment;
import com.example.ph906_spalshscreen.ui.privacy.DataPrivacyMinorFragment;

public class PrivacyActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Make sure the layout exists at res/layout/activity_privacy.xml
        setContentView(R.layout.activity_privacy);

        String version = getIntent().getStringExtra("version");
        Fragment fragment;

        if ("minor".equals(version)) {
            fragment = new DataPrivacyMinorFragment();
        } else {
            fragment = new DataPrivacyAdultFragment();
        }

        // Replace the container with the selected fragment
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.privacy_container, fragment)
                .commit();
    }

    // Called by fragment when "Agree" is pressed
    public void onAgreed() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}

package com.example.ph906_spalshscreen;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.ph906_spalshscreen.ui.privacy.DataPrivacyAdultFragment;
import com.example.ph906_spalshscreen.ui.privacy.DataPrivacyMinorFragment;

public class PrivacyActivity extends AppCompatActivity {

    private String version;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_privacy);

        version = getIntent().getStringExtra("version");

        Fragment fragment = "minor".equals(version)
                ? new DataPrivacyMinorFragment()
                : new DataPrivacyAdultFragment();

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.privacy_container, fragment)
                .commit();
    }

    // Called by your fragment when user taps "Agree"
    public void onAgreed() {
        new PrefManager(this).setPrivacyAccepted(version);

        startActivity(new Intent(this, MainActivity.class)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
        finish();
    }
}

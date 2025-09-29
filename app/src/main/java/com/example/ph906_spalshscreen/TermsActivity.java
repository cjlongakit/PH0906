package com.example.ph906_spalshscreen;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.ph906_spalshscreen.ui.privacy.TermsAndAgreementsFragment;
import com.example.ph906_spalshscreen.ui.privacy.TermsAndAgreementsOlderFragment;

public class TermsActivity extends AppCompatActivity {

    private String version;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_terms);

        version = getIntent().getStringExtra("version");

        Fragment fragment = "minor".equals(version)
                ? new TermsAndAgreementsFragment()
                : new TermsAndAgreementsOlderFragment();

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.terms_container, fragment)
                .commit();
    }

    // Called by your fragment when user taps "Agree"
    public void onAgreed() {
        new PrefManager(this).acceptTerms(version, true);

        startActivity(new Intent(this, PrivacyActivity.class)
                .putExtra("version", version));
        finish();
    }
}

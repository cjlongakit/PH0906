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

    // Called by fragment when "Agree" is pressed
    public void onAgreed() {
        Intent intent = new Intent(this, PrivacyActivity.class);
        intent.putExtra("version", version);
        startActivity(intent);
        finish();
    }
}
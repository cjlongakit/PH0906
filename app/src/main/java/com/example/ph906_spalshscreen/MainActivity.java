package com.example.ph906_spalshscreen;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.example.ph906_spalshscreen.ui.home.HomeFragment;
import com.example.ph906_spalshscreen.ui.settings.SettingsFragment;
import com.example.ph906_spalshscreen.ui.about.AboutFragment;
import com.example.ph906_spalshscreen.ui.privacy.DataPrivacyFragment;
import com.example.ph906_spalshscreen.ui.privacy.DataPrivacyAdultFragment;
import com.example.ph906_spalshscreen.ui.privacy.TermsAndAgreementsFragment;
import com.example.ph906_spalshscreen.ui.privacy.TermsAndAgreementsOlderFragment;
import com.example.ph906_spalshscreen.ui.profile.ProfileFragment; // ✅ make sure you have this
import com.google.android.material.navigation.NavigationView;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // ✅ Remove the default title completely
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        // Drawer layout and navigation view
        drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close
        );
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        // Load default fragment
        if (savedInstanceState == null) {
            replaceFragment(new HomeFragment());
            navigationView.setCheckedItem(R.id.nav_home);
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            replaceFragment(new HomeFragment());

        } else if (id == R.id.nav_profile) {
            replaceFragment(new ProfileFragment()); // ✅ Profile now works

        } else if (id == R.id.nav_settings) {
            replaceFragment(new SettingsFragment());

        } else if (id == R.id.nav_about) {
            replaceFragment(new AboutFragment());

        } else if (id == R.id.nav_data_privacy_minor) {
            replaceFragment(new DataPrivacyFragment());

        } else if (id == R.id.nav_data_privacy_adult) {
            replaceFragment(new DataPrivacyAdultFragment());

        } else if (id == R.id.nav_terms_minor) {
            replaceFragment(new TermsAndAgreementsFragment());

        } else if (id == R.id.nav_terms_adult) {
            replaceFragment(new TermsAndAgreementsOlderFragment());

        } else if (id == R.id.nav_logout) {
            // Logout → back to LoginActivity
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }

        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void replaceFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}

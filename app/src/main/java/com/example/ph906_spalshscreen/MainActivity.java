package com.example.ph906_spalshscreen;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.ph906_spalshscreen.api.ApiClient;
import com.example.ph906_spalshscreen.ui.about.AboutFragment;
import com.example.ph906_spalshscreen.ui.letters.LettersFragment;
import com.example.ph906_spalshscreen.ui.profile.ProfileFragment;
import com.example.ph906_spalshscreen.ui.home.HomeFragment;
import com.google.android.material.navigation.NavigationView;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "MainActivity";

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;
    private ApiClient apiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // make sure you have activity_main.xml

        // Init ApiClient
        apiClient = new ApiClient(this);

        // Check login status
        if (!apiClient.isLoggedIn()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        // Setup Toolbar
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Setup Drawer
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close
        );
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);

        // Update header with logged-in user info
        updateNavigationHeader();

        // --- HANDLE DASHBOARD BUTTONS ---
        Button btnLetters = findViewById(R.id.btn_letters);
        if (btnLetters != null) {
            btnLetters.setOnClickListener(v -> {
                Log.d(TAG, "Letters button clicked");
                // Replace fragment instead of starting activity
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, new LettersFragment())
                        .addToBackStack(null)
                        .commit();
            });
        }

        // Ensure HomeFragment is loaded as the default fragment
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, new HomeFragment())
                .commit();
        }
    }

    private void updateNavigationHeader() {
        if (navigationView != null) {
            View header = null;
            try { header = navigationView.getHeaderView(0); } catch (Exception ignored) {}
            if (header == null) {
                try { header = navigationView.inflateHeaderView(R.layout.nav_header); } catch (Exception ignored) {}
            }
            if (header != null) {
                TextView tvName = header.findViewById(R.id.nav_username);
                TextView tvId = header.findViewById(R.id.nav_student_id);

                String fullName = apiClient.getFullName();
                String studentId = apiClient.getLoggedInStudentId();

                if (tvName != null) tvName.setText(fullName != null ? fullName : "Unknown User");
                if (tvId != null) tvId.setText(studentId != null ? "ID: " + studentId : "ID: N/A");
            }
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, new HomeFragment())
                .commit();
        } else if (id == R.id.nav_profile) {
            getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, new ProfileFragment())
                .addToBackStack(null)
                .commit();
        } else if (id == R.id.nav_change_password) {
            startActivity(new Intent(this, ChangePasswordActivity.class));
        } else if (id == R.id.nav_about) {
            getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, new AboutFragment())
                .addToBackStack(null)
                .commit();
        } else if (id == R.id.nav_logout) {
            logoutUser();
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void logoutUser() {
        apiClient.logout();
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateNavigationHeader();
    }
}

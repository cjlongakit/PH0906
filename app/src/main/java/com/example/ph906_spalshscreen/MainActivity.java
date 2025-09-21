package com.example.ph906_spalshscreen;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.ph906_spalshscreen.api.ApiClient;
import com.example.ph906_spalshscreen.ui.about.AboutFragment;
import com.example.ph906_spalshscreen.ui.home.HomeFragment;
import com.example.ph906_spalshscreen.ui.profile.ProfileFragment;
import com.google.android.material.navigation.NavigationView;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ApiClient apiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        apiClient = new ApiClient(this);

        // Check login status first
        if (!apiClient.isLoggedIn()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_main);

        // Hide the default ActionBar completely
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // Setup Drawer Layout & NavigationView
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // Setup manual hamburger button click
        ImageView hamburgerMenu = findViewById(R.id.hamburger_menu);
        hamburgerMenu.setOnClickListener(v -> {
            if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawer(GravityCompat.START);
            } else {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });

        // Update navigation header with user info
        updateNavigationHeader();

        // Default fragment when app starts
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new HomeFragment())
                    .commit();
            navigationView.setCheckedItem(R.id.nav_home);
        }
    }

    private void updateNavigationHeader() {
        // Get the values from ApiClient
        boolean isLoggedIn = apiClient.isLoggedIn();
        String fullName = apiClient.getFullName();
        String studentId = apiClient.getLoggedInStudentId();

        android.util.Log.d("DEBUG", "=== Navigation Header Debug ===");
        android.util.Log.d("DEBUG", "Is logged in: " + isLoggedIn);
        android.util.Log.d("DEBUG", "Full name: '" + fullName + "'");
        android.util.Log.d("DEBUG", "Student ID: '" + studentId + "'");

        View headerView = navigationView.getHeaderView(0);
        TextView tvName = headerView.findViewById(R.id.nav_username);
        TextView tvStudentId = headerView.findViewById(R.id.nav_student_id);

        android.util.Log.d("DEBUG", "tvName found: " + (tvName != null));
        android.util.Log.d("DEBUG", "tvStudentId found: " + (tvStudentId != null));

        if (tvName != null) {
            tvName.setText(fullName.isEmpty() ? "No Name" : fullName);
            android.util.Log.d("DEBUG", "Set name to: " + fullName);
        }
        if (tvStudentId != null) {
            tvStudentId.setText(studentId == null ? "No ID" : "ID: " + studentId);
            android.util.Log.d("DEBUG", "Set ID to: " + studentId);
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new HomeFragment())
                    .commit();
        } else if (id == R.id.nav_profile) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new ProfileFragment())
                    .commit();
        } else if (id == R.id.nav_change_password) {
            startActivity(new Intent(this, ChangePasswordActivity.class));
        } else if (id == R.id.nav_about) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new AboutFragment())
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
}
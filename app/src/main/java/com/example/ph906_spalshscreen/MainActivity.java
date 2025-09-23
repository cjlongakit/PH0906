package com.example.ph906_spalshscreen;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.ph906_spalshscreen.api.ApiClient;
import com.example.ph906_spalshscreen.ui.about.AboutFragment;
import com.example.ph906_spalshscreen.ui.profile.ProfileFragment;
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
                startActivity(new Intent(MainActivity.this, LettersFragment.class));
            });
        }
    }

    private void updateNavigationHeader() {
        if (navigationView != null) {
            TextView tvName = navigationView.getHeaderView(0).findViewById(R.id.nav_username);
            TextView tvId = navigationView.getHeaderView(0).findViewById(R.id.nav_student_id);

            String fullName = apiClient.getFullName();
            String studentId = apiClient.getLoggedInStudentId();

            if (tvName != null) tvName.setText(fullName != null ? fullName : "Unknown User");
            if (tvId != null) tvId.setText(studentId != null ? "ID: " + studentId : "ID: N/A");
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            // Already here
        } else if (id == R.id.nav_profile) {
            startActivity(new Intent(this, ProfileFragment.class));
        } else if (id == R.id.nav_change_password) {
            startActivity(new Intent(this, ChangePasswordActivity.class));
        } else if (id == R.id.nav_about) {
            startActivity(new Intent(this, AboutFragment.class));
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

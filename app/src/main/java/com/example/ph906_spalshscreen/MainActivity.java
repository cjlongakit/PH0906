package com.example.ph906_spalshscreen;
import android.content.Intent;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import com.example.ph906_spalshscreen.api.ApiClient;
import com.example.ph906_spalshscreen.ui.profile.ProfileFragment;
import com.google.android.material.navigation.NavigationView;

public class MainActivity extends AppCompatActivity {
    private ApiClient apiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        apiClient = new ApiClient(this);

        // 🔹 Setup Drawer + Navigation
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);

        // 🔹 Update Header Info (Student Name + ID)
        View headerView = navigationView.getHeaderView(0);
        TextView tvName = headerView.findViewById(R.id.nav_header_name);
        TextView tvEmail = headerView.findViewById(R.id.nav_header_email);

        tvName.setText(apiClient.getFullName());  // student’s full name from login
        tvEmail.setText("Student ID: " + apiClient.getLoggedInStudentId());

        // 🔹 Handle Navigation Drawer Clicks
        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_profile) {
                startActivity(new Intent(this, ProfileFragment.class));
            } else if (id == R.id.nav_change_password) {
                startActivity(new Intent(this, ChangePasswordActivity.class));
            } else if (id == R.id.nav_events) {
                startActivity(new Intent(this, EventsActivity.class));
            } else if (id == R.id.nav_logout) {
                apiClient.logout();
                startActivity(new Intent(this, LoginActivity.class));
                finish();
            }

            drawer.closeDrawers();
            return true;
        });
    }
}

package com.example.ph906_spalshscreen;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.widget.Button;
import android.widget.TextView;
import android.view.View;
import android.widget.Toast;

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
    private boolean isTermsExpanded = false;
    private boolean isPrivacyExpanded = false;
    private static final int GROUP_TERMS = 1001;
    private static final int GROUP_PRIVACY = 1002;

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

        // Defer menu reload/manipulation to ensure the view is ready
        navigationView.post(() -> {
            rebuildDrawerMenu();
            setTermsChildrenVisible(false);
            setPrivacyChildrenVisible(false);
        });

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

    private void setTermsChildrenVisible(boolean visible) {
        if (navigationView == null) return;
        Menu menu = navigationView.getMenu();
        menu.setGroupVisible(GROUP_TERMS, visible);
        isTermsExpanded = visible;
        Log.d(TAG, "Terms submenu set visible=" + visible);
        navigationView.invalidate();
        navigationView.requestLayout();
    }

    private void setPrivacyChildrenVisible(boolean visible) {
        if (navigationView == null) return;
        Menu menu = navigationView.getMenu();
        menu.setGroupVisible(GROUP_PRIVACY, visible);
        isPrivacyExpanded = visible;
        Log.d(TAG, "Privacy submenu set visible=" + visible);
        navigationView.invalidate();
        navigationView.requestLayout();
    }

    private void ensureMenuParentsPresent() {
        if (navigationView == null) return;
        Menu menu = navigationView.getMenu();

        // Normalize About: remove any old submenu-version and add as simple leaf
        final int ORDER_ABOUT = 55; // between Terms (50) and Privacy (60)
        if (menu.findItem(R.id.nav_about) != null) {
            menu.removeItem(R.id.nav_about);
            menu.add(Menu.NONE, R.id.nav_about, ORDER_ABOUT, "About");
        }

        // Try to place before About if present by controlling order
        final int ORDER_TERMS = 50;
        final int ORDER_PRIVACY = 60;

        if (menu.findItem(R.id.nav_terms_menu) == null) {
            menu.add(Menu.NONE, R.id.nav_terms_menu, ORDER_TERMS, "Terms & Agreements").setCheckable(false);
            menu.add(Menu.NONE, R.id.nav_terms_minor, ORDER_TERMS + 1, "Terms & Agreements (Minor)");
            menu.add(Menu.NONE, R.id.nav_terms_adult, ORDER_TERMS + 2, "Terms & Agreements (Adult)");
        }
        if (menu.findItem(R.id.nav_privacy_menu) == null) {
            menu.add(Menu.NONE, R.id.nav_privacy_menu, ORDER_PRIVACY, "Data Privacy").setCheckable(false);
            menu.add(Menu.NONE, R.id.nav_privacy_minor, ORDER_PRIVACY + 1, "Data Privacy (Minor)");
            menu.add(Menu.NONE, R.id.nav_privacy_adult, ORDER_PRIVACY + 2, "Data Privacy (Adult)");
        }
    }

    private void rebuildDrawerMenu() {
        if (navigationView == null) return;
        Log.d(TAG, "rebuildDrawerMenu(): building top-level Terms & Privacy outside About");
        Menu menu = navigationView.getMenu();
        menu.clear();

        // Orders to control placement
        final int O_HOME = 10, O_PROFILE = 20, O_TERMS = 30, O_PRIVACY = 40, O_ABOUT = 50, O_SETTINGS_HEADER = 60, O_CHANGE_PWD = 61, O_LOGOUT = 90;

        // Home & Profile
        menu.add(Menu.NONE, R.id.nav_home, O_HOME, "Home").setIcon(android.R.drawable.ic_menu_view);
        menu.add(Menu.NONE, R.id.nav_profile, O_PROFILE, "Profile").setIcon(android.R.drawable.ic_menu_edit);

        // Terms & Agreements (parent)
        MenuItem termsParent = menu.add(Menu.NONE, R.id.nav_terms_menu, O_TERMS, "Terms & Agreements");
        termsParent.setIcon(android.R.drawable.ic_menu_info_details);
        termsParent.setCheckable(true);
        termsParent.setEnabled(true);
        termsParent.setOnMenuItemClickListener(mi -> {
            boolean next = !isTermsExpanded;
            setTermsChildrenVisible(next);
            if (next) setPrivacyChildrenVisible(false);
            navigationView.setCheckedItem(R.id.nav_terms_menu);
            Toast.makeText(this, next ? "Terms expanded" : "Terms collapsed", Toast.LENGTH_SHORT).show();
            return true;
        });
        // Terms children in a group
        menu.add(GROUP_TERMS, R.id.nav_terms_minor, O_TERMS + 1, "Terms & Agreements (Minor)");
        menu.add(GROUP_TERMS, R.id.nav_terms_adult, O_TERMS + 2, "Terms & Agreements (Adult)");

        // Data Privacy (parent)
        MenuItem privacyParent = menu.add(Menu.NONE, R.id.nav_privacy_menu, O_PRIVACY, "Data Privacy");
        privacyParent.setIcon(android.R.drawable.ic_menu_info_details);
        privacyParent.setCheckable(true);
        privacyParent.setEnabled(true);
        privacyParent.setOnMenuItemClickListener(mi -> {
            boolean next = !isPrivacyExpanded;
            setPrivacyChildrenVisible(next);
            if (next) setTermsChildrenVisible(false);
            navigationView.setCheckedItem(R.id.nav_privacy_menu);
            Toast.makeText(this, next ? "Privacy expanded" : "Privacy collapsed", Toast.LENGTH_SHORT).show();
            return true;
        });
        // Privacy children in a group
        menu.add(GROUP_PRIVACY, R.id.nav_privacy_minor, O_PRIVACY + 1, "Data Privacy (Minor)");
        menu.add(GROUP_PRIVACY, R.id.nav_privacy_adult, O_PRIVACY + 2, "Data Privacy (Adult)");

        // About
        menu.add(Menu.NONE, R.id.nav_about, O_ABOUT, "About").setIcon(android.R.drawable.ic_menu_info_details);

        // Settings subheader + Change Password
        SubMenu settings = menu.addSubMenu(Menu.NONE, Menu.NONE, O_SETTINGS_HEADER, "Settings");
        settings.add(Menu.NONE, R.id.nav_change_password, O_CHANGE_PWD, "Change Password").setIcon(android.R.drawable.ic_menu_manage);

        // Logout
        menu.add(Menu.NONE, R.id.nav_logout, O_LOGOUT, "Logout").setIcon(android.R.drawable.ic_lock_power_off);

        // Hide groups by default
        menu.setGroupVisible(GROUP_TERMS, false);
        menu.setGroupVisible(GROUP_PRIVACY, false);

        navigationView.invalidate();
        navigationView.requestLayout();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        Log.d(TAG, "onNavigationItemSelected id=" + getResources().getResourceEntryName(id));

        if (id == R.id.nav_home) {
            getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, new HomeFragment())
                .commit();
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        } else if (id == R.id.nav_profile) {
            getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, new ProfileFragment())
                .addToBackStack(null)
                .commit();
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        } else if (id == R.id.nav_terms_menu) {
            boolean next = !isTermsExpanded;
            setTermsChildrenVisible(next);
            if (next) setPrivacyChildrenVisible(false);
            navigationView.setCheckedItem(id);
            Toast.makeText(this, next ? "Terms expanded" : "Terms collapsed", Toast.LENGTH_SHORT).show();
            return true;
        } else if (id == R.id.nav_privacy_menu) {
            boolean next = !isPrivacyExpanded;
            setPrivacyChildrenVisible(next);
            if (next) setTermsChildrenVisible(false);
            navigationView.setCheckedItem(id);
            Toast.makeText(this, next ? "Privacy expanded" : "Privacy collapsed", Toast.LENGTH_SHORT).show();
            return true;
        } else if (id == R.id.nav_terms_minor) {
            startActivity(new Intent(this, TermsActivity.class).putExtra("version", "minor"));
        } else if (id == R.id.nav_terms_adult) {
            startActivity(new Intent(this, TermsActivity.class).putExtra("version", "adult"));
        } else if (id == R.id.nav_privacy_minor) {
            startActivity(new Intent(this, PrivacyActivity.class).putExtra("version", "minor"));
        } else if (id == R.id.nav_privacy_adult) {
            startActivity(new Intent(this, PrivacyActivity.class).putExtra("version", "adult"));
        } else if (id == R.id.nav_about) {
            getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, new AboutFragment())
                .addToBackStack(null)
                .commit();
        } else if (id == R.id.nav_change_password) {
            startActivity(new Intent(this, ChangePasswordActivity.class));
        } else if (id == R.id.nav_logout) {
            logoutUser();
        }

        // Close drawer for leaf actions
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
        // Ensure our custom parents exist even after configuration/activity changes
        if (navigationView != null) {
            Menu m = navigationView.getMenu();
            if (m == null || m.findItem(R.id.nav_terms_menu) == null || m.findItem(R.id.nav_privacy_menu) == null) {
                Log.d(TAG, "Rebuilding drawer menu in onResume");
                rebuildDrawerMenu();
                setTermsChildrenVisible(false);
                setPrivacyChildrenVisible(false);
            }
        }
    }
}

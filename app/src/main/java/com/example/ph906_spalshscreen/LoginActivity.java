package com.example.ph906_spalshscreen;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ph906_spalshscreen.api.ApiCallback;
import com.example.ph906_spalshscreen.api.ApiClient;

import org.json.JSONObject;

import java.time.LocalDate;
import java.time.Period;

public class LoginActivity extends AppCompatActivity {

    private EditText etPh906, etBirthday;
    private Button btnLogin;
    private TextView tvForgotPassword;
    private ApiClient apiClient;
    private PrefsHelper prefs;

    private static final String TAG = "LOGIN_DEBUG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etPh906 = findViewById(R.id.etPh906);
        etBirthday = findViewById(R.id.etBirthday);
        btnLogin = findViewById(R.id.btnLogin);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);

        apiClient = new ApiClient(this);
        prefs = new PrefsHelper(this);

        // DEBUG: Log current saved session to see what's stored
        Log.d(TAG, "=== Checking saved session ===");
        Log.d(TAG, "isLoggedIn: " + prefs.isLoggedIn());
        Log.d(TAG, "PH906: " + prefs.getPh906());
        Log.d(TAG, "Token: " + (prefs.getToken() != null ? "exists" : "null"));
        Log.d(TAG, "Full Name: " + prefs.getFullName());

        // Check if user has a valid saved session
        // If corrupted session, clear it
        if (prefs.isLoggedIn() && (prefs.getPh906() == null || prefs.getToken() == null)) {
            Log.w(TAG, "Corrupted session detected. Clearing.");
            prefs.clearAll();
        }

        // Auto-login if user has valid session (stay logged in until logout)
        if (prefs.isLoggedIn() && prefs.getPh906() != null && prefs.getToken() != null) {
            String savedVersion = prefs.getVersion();
            Log.d(TAG, "Auto-login with saved session. ph906=" + prefs.getPh906() + " version=" + savedVersion);
            goNext(savedVersion);
            return;
        }

        Log.d(TAG, "No valid session found - showing login screen");

        // Setup login button
        btnLogin.setOnClickListener(v -> {
            String usernameRaw = etPh906.getText().toString().trim();
            String birthdayRaw = etBirthday.getText().toString().trim();

            if (usernameRaw.isEmpty() || birthdayRaw.isEmpty()) {
                Toast.makeText(this, "Please enter both student ID and birthday", Toast.LENGTH_SHORT).show();
                return;
            }

            String username = usernameRaw.replaceAll("[^0-9]", ""); // digits-only
            String birthday = normalizeBirthdayToYmd(birthdayRaw);

            btnLogin.setEnabled(false);

            apiClient.studentLogin(username, birthday, new ApiCallback() {
                @Override
                public void onSuccess(JSONObject response) {
                    runOnUiThread(() -> {
                        try {
                            Log.d(TAG, "=== Login Success === " + response);

                            // Get version from server if present; else derive from birthday
                            String version = response.optString("version", "").trim();
                            if (version.isEmpty()) {
                                version = deriveVersionFromBirthday(response.optString("birthday", birthday));
                            }
                            prefs.saveVersion(version); // ensure goNext sees it

                            // Double-check essentials are saved by ApiClient
                            if (prefs.getPh906() == null || prefs.getToken() == null) {
                                // Defensive: if ApiClient didn't save, do minimal save from response
                                String ph = response.optString("ph906", username);
                                String token = response.optString("token", null);
                                String fullName = (response.optString("first_name", "") + " " +
                                        response.optString("last_name", "")).trim();
                                prefs.saveLoginInfo(ph, token, fullName, response.optBoolean("is_default_password", true));
                                prefs.saveBirthday(response.optString("birthday", birthday));
                            }

                            btnLogin.setEnabled(true);
                            Toast.makeText(LoginActivity.this, "Login successful", Toast.LENGTH_SHORT).show();
                            goNext(version);
                        } catch (Exception e) {
                            btnLogin.setEnabled(true);
                            Log.e(TAG, "Post-login processing error", e);
                            Toast.makeText(LoginActivity.this, "Post-login error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
                }

                @Override
                public void onError(String errorMessage) {
                    runOnUiThread(() -> {
                        btnLogin.setEnabled(true);
                        Log.e(TAG, "=== Login Failed === " + errorMessage);
                        Toast.makeText(LoginActivity.this, "Login failed: " + errorMessage, Toast.LENGTH_LONG).show();
                    });
                }
            });
        });

        tvForgotPassword.setOnClickListener(v ->
                startActivity(new Intent(LoginActivity.this, ForgotPasswordActivity.class))
        );
    }

    private void goNext(String version) {
        try {
            // Defensive: ensure we have essentials before leaving the Login screen
            if (prefs.getToken() == null || prefs.getPh906() == null) {
                Log.w(TAG, "Missing session data; staying on Login.");
                Toast.makeText(this, "Session not saved. Please try again.", Toast.LENGTH_LONG).show();
                return;
            }

            String safeVersion = (version == null || version.trim().isEmpty()) ? "minor" : version.trim().toLowerCase();
            PrefManager pref = new PrefManager(this);

            if (!pref.isTermsAccepted(safeVersion)) {
                startActivity(new Intent(this, TermsActivity.class)
                        .putExtra("version", safeVersion));
            } else if (!pref.isPrivacyAccepted(safeVersion)) {
                startActivity(new Intent(this, PrivacyActivity.class)
                        .putExtra("version", safeVersion));
            } else {
                startActivity(new Intent(this, MainActivity.class)
                        .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
            }
            finish();
        } catch (Exception e) {
            Log.e(TAG, "Navigation error after login", e);
            Toast.makeText(this, "Navigation error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private String normalizeBirthdayToYmd(String s) {
        try {
            String t = s.trim();
            if (t.matches("\\d{4}-\\d{2}-\\d{2}")) return t; // YYYY-MM-DD
            if (t.matches("\\d{2}/\\d{2}/\\d{4}")) {         // MM/DD/YYYY
                String[] p = t.split("/");
                return p[2] + "-" + p[0] + "-" + p[1];
            }
            if (t.matches("\\d{2}-\\d{2}-\\d{4}")) {         // DD-MM-YYYY (ambiguous)
                return t;
            }
        } catch (Exception ignored) {}
        return s;
    }

    private String deriveVersionFromBirthday(String ymd) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                LocalDate b = LocalDate.parse(ymd);
                LocalDate now = LocalDate.now();
                int age = Period.between(b, now).getYears();
                return age < 18 ? "minor" : "adult";
            }
        } catch (Exception ignored) {}
        // Fallback if parsing fails or pre-O
        return "minor";
    }
}
package com.example.ph906_spalshscreen;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ph906_spalshscreen.api.ApiCallback;
import com.example.ph906_spalshscreen.api.ApiClient;

import org.json.JSONObject;

/* ============================================================
 * ChangePasswordActivity.java
 * ------------------------------------------------------------
 * Activity to allow logged-in student to change password
 * ------------------------------------------------------------
 * Uses: ApiClient for API calls, ApiCallback for handling responses
 * ============================================================ */

public class ChangePasswordActivity extends AppCompatActivity {

    // ==============================
    // UI Elements
    // ==============================
    private EditText etCurrentPassword, etNewPassword, etConfirmPassword;
    private Button btnChangePassword;

    // ==============================
    // API Client
    // ==============================
    private ApiClient apiClient;

    // ==============================
    // onCreate
    // ==============================
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        // Initialize UI
        etCurrentPassword = findViewById(R.id.etCurrentPassword);
        etNewPassword = findViewById(R.id.etNewPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnChangePassword = findViewById(R.id.btnChangePassword);

        // Initialize API client
        apiClient = new ApiClient(this);

        // Set click listener
        btnChangePassword.setOnClickListener(v -> changePassword());
    }

    // ==============================
    // Change Password Logic
    // ==============================
    private void changePassword() {
        // Read input
        String currentPassword = etCurrentPassword.getText().toString().trim();
        String newPassword = etNewPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        // Validate input
        if (currentPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!newPassword.equals(confirmPassword)) {
            Toast.makeText(this, "New passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }
        if (newPassword.length() < 6) {
            Toast.makeText(this, "New password must be at least 6 characters", Toast.LENGTH_SHORT).show();
            return;
        }

        // Disable button while processing
        btnChangePassword.setEnabled(false);
        btnChangePassword.setText("Changing...");

        // ==============================
        // API Call: Change Password
        // ==============================
        apiClient.changePassword(currentPassword, newPassword, new ApiCallback() {
            @Override
            public void onSuccess(JSONObject response) {
                runOnUiThread(() -> {
                    Toast.makeText(ChangePasswordActivity.this, "Password changed successfully!", Toast.LENGTH_SHORT).show();
                    finish(); // Close activity
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    Toast.makeText(ChangePasswordActivity.this, "Failed: " + error, Toast.LENGTH_LONG).show();
                    btnChangePassword.setEnabled(true);
                    btnChangePassword.setText("Change Password");
                });
            }
        });
    }
}

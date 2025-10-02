package com.example.ph906_spalshscreen;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ph906_spalshscreen.api.ApiCallback;
import com.example.ph906_spalshscreen.api.ApiClient;

import org.json.JSONObject;

public class ResetPasswordActivity extends AppCompatActivity {

    EditText etNewPassword, etConfirmPassword;
    Button btnSave;
    private ApiClient apiClient;

    private String email;
    private String resetToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        etNewPassword = findViewById(R.id.etNewPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnSave = findViewById(R.id.btnSavePassword);
        apiClient = new ApiClient(this);

        // From Forgot or OTP screens
        email = getIntent().getStringExtra("email");
        resetToken = getIntent().getStringExtra("reset_token");

        btnSave.setOnClickListener(v -> {
            String newPass = etNewPassword.getText().toString().trim();
            String confirmPass = etConfirmPassword.getText().toString().trim();

            if (newPass.isEmpty() || confirmPass.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!newPass.equals(confirmPass)) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                return;
            }
            if (newPass.length() < 4) {
                Toast.makeText(this, "Password must be at least 4 characters", Toast.LENGTH_SHORT).show();
                return;
            }

            btnSave.setEnabled(false);
            // Prefer token if available; else fallback to email-based reset
            apiClient.resetPassword(resetToken, email, newPass, new ApiCallback() {
                @Override public void onSuccess(JSONObject response) {
                    runOnUiThread(() -> {
                        Toast.makeText(ResetPasswordActivity.this, "Password reset successful!", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(ResetPasswordActivity.this, LoginActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    });
                }
                @Override public void onError(String message) {
                    runOnUiThread(() -> {
                        btnSave.setEnabled(true);
                        Toast.makeText(ResetPasswordActivity.this, "Reset failed: " + message, Toast.LENGTH_LONG).show();
                    });
                }
            });
        });
    }
}

package com.example.ph906_spalshscreen;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ph906_spalshscreen.api.ApiClient;
import com.example.ph906_spalshscreen.api.ApiCallback;

import org.json.JSONObject;

public class ChangePasswordActivity extends AppCompatActivity {

    private EditText etCurrentPassword, etNewPassword, etConfirmPassword;
    private Button btnChangePassword;
    private ApiClient apiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        // Init UI
        etCurrentPassword = findViewById(R.id.etCurrentPassword);
        etNewPassword = findViewById(R.id.etNewPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnChangePassword = findViewById(R.id.btnChangePassword);

        // Init API client
        apiClient = new ApiClient(this);

        btnChangePassword.setOnClickListener(v -> {
            String currentPassword = etCurrentPassword.getText().toString().trim();
            String newPassword = etNewPassword.getText().toString().trim();
            String confirm = etConfirmPassword.getText().toString().trim();

            if (currentPassword.isEmpty() || newPassword.isEmpty() || confirm.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!newPassword.equals(confirm)) {
                Toast.makeText(this, "New password and confirm do not match", Toast.LENGTH_SHORT).show();
                return;
            }
            if (newPassword.length() < 4) {
                Toast.makeText(this, "Password must be at least 4 characters", Toast.LENGTH_SHORT).show();
                return;
            }

            btnChangePassword.setEnabled(false);
            btnChangePassword.setText("Changing...");

            // API Call: Change Password (server hashes securely)
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
        });
    }
}

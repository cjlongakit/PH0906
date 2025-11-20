package com.example.ph906_spalshscreen;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.InputType;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.ph906_spalshscreen.api.ApiCallback;
import com.example.ph906_spalshscreen.api.ApiClient;

import org.json.JSONException;
import org.json.JSONObject;

public class ForgotPasswordActivity extends AppCompatActivity {
    EditText etUsername, etBirthday;
    Button btnResetPassword;
    private ApiClient apiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        etUsername = findViewById(R.id.etUsername);
        etBirthday = findViewById(R.id.etBirthday);
        btnResetPassword = findViewById(R.id.btnResetPassword);
        apiClient = new ApiClient(this);

        etBirthday.setInputType(InputType.TYPE_NULL);
        etBirthday.setOnClickListener(v -> showDatePicker());

        btnResetPassword.setOnClickListener(v -> {
            String username = etUsername.getText().toString().trim();
            String birthday = etBirthday.getText().toString().trim();

            if (username.isEmpty() || birthday.isEmpty()) {
                Toast.makeText(this, "Please enter both username and birthday", Toast.LENGTH_SHORT).show();
                return;
            }
            btnResetPassword.setEnabled(false);
            JSONObject payload = new JSONObject();
            try {
                payload.put("username", username);
                payload.put("birthday", birthday);
            } catch (JSONException e) {
                Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                btnResetPassword.setEnabled(true);
                return;
            }
            // Step 1: Request reset token with both username and birthday
            apiClient.requestPasswordResetWithEmail(payload, new ApiCallback() {
                @Override
                public void onSuccess(JSONObject response) {
                    runOnUiThread(() -> {
                        String resetToken = response.optString("reset_token", "");
                        if (resetToken.isEmpty()) {
                            Toast.makeText(ForgotPasswordActivity.this, "No reset token received", Toast.LENGTH_LONG).show();
                            btnResetPassword.setEnabled(true);
                            return;
                        }
                        showNewPasswordDialog(username, resetToken);
                    });
                }
                @Override
                public void onError(String message) {
                    runOnUiThread(() -> {
                        Toast.makeText(ForgotPasswordActivity.this, "Error: " + message, Toast.LENGTH_LONG).show();
                        btnResetPassword.setEnabled(true);
                    });
                }
            });
        });

    }

    private void showNewPasswordDialog(String username, String resetToken) {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        final EditText etNew = new EditText(this);
        etNew.setHint("New password");
        etNew.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        final EditText etConfirm = new EditText(this);
        etConfirm.setHint("Confirm password");
        etConfirm.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        int pad = (int)(16 * getResources().getDisplayMetrics().density);
        layout.setPadding(pad,pad,pad,pad);
        layout.addView(etNew);
        layout.addView(etConfirm);

        new AlertDialog.Builder(this)
            .setTitle("Enter new password")
            .setView(layout)
            .setPositiveButton("Change", (dialog, which) -> {
                String p1 = etNew.getText().toString();
                String p2 = etConfirm.getText().toString();
                if (p1.length() < 6) {
                    Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!p1.equals(p2)) {
                    Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                    return;
                }
                sendFinalResetRequest(username, resetToken, p1);
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void sendFinalResetRequest(String username, String resetToken, String newPassword) {
        apiClient.resetPassword(resetToken, username, newPassword, new ApiCallback() {
            @Override
            public void onSuccess(JSONObject response) {
                runOnUiThread(() -> {
                    Toast.makeText(ForgotPasswordActivity.this, "Password reset successful", Toast.LENGTH_LONG).show();
                    finish();
                });
            }
            @Override
            public void onError(String message) {
                runOnUiThread(() -> {
                    Toast.makeText(ForgotPasswordActivity.this, "Error: " + message, Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void showDatePicker() {
        java.util.Calendar c = java.util.Calendar.getInstance();
        int y = c.get(java.util.Calendar.YEAR);
        int m = c.get(java.util.Calendar.MONTH);
        int d = c.get(java.util.Calendar.DAY_OF_MONTH);
        DatePickerDialog dpd = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            String mm = String.format("%02d", month + 1);
            String dd = String.format("%02d", dayOfMonth);
            etBirthday.setText(year + "-" + mm + "-" + dd); // ISO format yyyy-MM-dd
        }, y, m, d);
        dpd.show();
    }
}

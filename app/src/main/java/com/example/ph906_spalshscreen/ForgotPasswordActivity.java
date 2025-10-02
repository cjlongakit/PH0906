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

import java.util.Random;

public class ForgotPasswordActivity extends AppCompatActivity {

    EditText etEmail;
    Button btnSend;
    private ApiClient apiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        etEmail = findViewById(R.id.etEmail);
        btnSend = findViewById(R.id.btnSendOTP);
        apiClient = new ApiClient(this);

        btnSend.setOnClickListener(v -> {
            String identifier = etEmail.getText().toString().trim();

            if (identifier.isEmpty()) {
                Toast.makeText(this, "Please enter your email or PH906", Toast.LENGTH_SHORT).show();
                return;
            }

            btnSend.setEnabled(false);
            // First try server-backed request
            apiClient.requestPasswordReset(identifier, new ApiCallback() {
                @Override public void onSuccess(JSONObject response) {
                    runOnUiThread(() -> {
                        btnSend.setEnabled(true);
                        // Expect reset_token or otp (support both)
                        String token = response.optString("reset_token", "");
                        String serverOtp = response.optString("otp", "");
                        if (!token.isEmpty()) {
                            // Go directly to reset screen with token
                            Intent intent = new Intent(ForgotPasswordActivity.this, ResetPasswordActivity.class);
                            intent.putExtra("email", identifier);
                            intent.putExtra("reset_token", token);
                            startActivity(intent);
                            finish();
                        } else if (!serverOtp.isEmpty()) {
                            // Go to OTP verify then reset
                            Intent intent = new Intent(ForgotPasswordActivity.this, OtpVerificationActivity.class);
                            intent.putExtra("email", identifier);
                            intent.putExtra("otp", serverOtp);
                            startActivity(intent);
                        } else {
                            Toast.makeText(ForgotPasswordActivity.this, "Email sent if account exists", Toast.LENGTH_LONG).show();
                        }
                    });
                }
                @Override public void onError(String message) {
                    // Fallback to local OTP flow to avoid blocking user if server route not present
                    runOnUiThread(() -> {
                        btnSend.setEnabled(true);
                        String otp = String.valueOf(1000 + new Random().nextInt(9000));
                        Toast.makeText(ForgotPasswordActivity.this, "Using local OTP: " + otp, Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(ForgotPasswordActivity.this, OtpVerificationActivity.class);
                        intent.putExtra("email", identifier);
                        intent.putExtra("otp", otp);
                        startActivity(intent);
                    });
                }
            });
        });
    }
}

package com.example.ph906_spalshscreen;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ph906_spalshscreen.api.ApiClient;
import com.example.ph906_spalshscreen.api.ApiCallback;

import org.json.JSONObject;

public class LoginActivity extends AppCompatActivity {

    private EditText etPh906, etBirthday;
    private Button btnLogin;
    private TextView tvForgotPassword;
    private ApiClient apiClient;

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

        // Already logged in? -> skip to next screen
        if (apiClient.isLoggedIn()) {
            String savedVersion = apiClient.getSavedVersion();
            goNext(savedVersion);
            return;
        }

        btnLogin.setOnClickListener(v -> {
            String username = etPh906.getText().toString().trim();
            String birthday = etBirthday.getText().toString().trim();

            if (username.isEmpty() || birthday.isEmpty()) {
                Toast.makeText(this, "Please enter both student ID and birthday", Toast.LENGTH_SHORT).show();
                return;
            }

            btnLogin.setEnabled(false);

            apiClient.studentLogin(username, birthday, new ApiCallback() {
                @Override
                public void onSuccess(JSONObject response) {
                    runOnUiThread(() -> {
                        Log.d(TAG, "=== Login Success ===");
                        Log.d(TAG, "Full Response: " + response.toString());
                        Log.d(TAG, "Stored Name: " + apiClient.getFullName());
                        Log.d(TAG, "Stored ID: " + apiClient.getLoggedInStudentId());
                        Log.d(TAG, "Stored Version: " + apiClient.getSavedVersion());

                        btnLogin.setEnabled(true);
                        Toast.makeText(LoginActivity.this, "Login successful", Toast.LENGTH_SHORT).show();

                        String version = apiClient.getSavedVersion();
                        goNext(version);
                    });
                }

                @Override
                public void onError(String errorMessage) {
                    runOnUiThread(() -> {
                        btnLogin.setEnabled(true);
                        Log.e(TAG, "=== Login Failed ===");
                        Log.e(TAG, "Error Message: " + errorMessage);
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
        PrefManager pref = new PrefManager(this);

        if (!pref.isTermsAccepted(version)) {
            startActivity(new Intent(this, TermsActivity.class)
                    .putExtra("version", version));
        } else if (!pref.isPrivacyAccepted(version)) {
            startActivity(new Intent(this, PrivacyActivity.class)
                    .putExtra("version", version));
        } else {
            startActivity(new Intent(this, MainActivity.class)
                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
        }
        finish();
    }
}

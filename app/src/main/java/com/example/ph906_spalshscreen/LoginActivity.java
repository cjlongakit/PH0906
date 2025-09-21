package com.example.ph906_spalshscreen;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ph906_spalshscreen.api.ApiCallback;
import com.example.ph906_spalshscreen.api.ApiClient;

import org.json.JSONObject;

public class LoginActivity extends AppCompatActivity {

    private EditText etPh906, etBirthday;
    private Button btnLogin;
    private TextView tvForgotPassword;
    private ApiClient apiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etPh906 = findViewById(R.id.etPh906);
        etBirthday = findViewById(R.id.etBirthday);
        btnLogin = findViewById(R.id.btnLogin);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);

        apiClient = new ApiClient(this);

        // If already logged in, skip login → goNext handles Terms/Privacy check
        if (apiClient.isLoggedIn()) {
            // Retrieve saved version if already logged in
            String savedVersion = apiClient.getSavedVersion(); // implement in ApiClient // "minor" or "adult"
            goNext(savedVersion);
            return;
        }

        btnLogin.setOnClickListener(v -> {
            String username = etPh906.getText().toString().trim();
            String birthday = etBirthday.getText().toString().trim();

            if (username.isEmpty() || birthday.isEmpty()) {
                Toast.makeText(this, "Please enter both username and birthday", Toast.LENGTH_SHORT).show();
                return;
            }

            btnLogin.setEnabled(false);

            apiClient.studentLogin(username, birthday, new ApiCallback() {
                @Override
                public void onSuccess(JSONObject response) {
                    runOnUiThread(() -> {
                        android.util.Log.d("DEBUG", "=== Login Success Debug ===");
                        android.util.Log.d("DEBUG", "Response: " + response.toString());
                        android.util.Log.d("DEBUG", "Stored name: " + apiClient.getFullName());
                        android.util.Log.d("DEBUG", "Stored ID: " + apiClient.getLoggedInStudentId());

                        btnLogin.setEnabled(true);
                        Toast.makeText(LoginActivity.this, "Login successful", Toast.LENGTH_SHORT).show();

                        // Extract version from API response
                        String version = "minor"; // default
                        if (response.has("student")) {
                            version = response.optJSONObject("student").optString("version", "minor");
                        }

                        // Save version locally in ApiClient for future logins
                        apiClient.saveVersion(version);

                        goNext(version);
                    });
                }

                @Override
                public void onError(String errorMessage) {
                    runOnUiThread(() -> {
                        btnLogin.setEnabled(true);
                        Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
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

package com.example.ph906_spalshscreen;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.ph906_spalshscreen.api.ApiClient;
import com.example.ph906_spalshscreen.api.ApiCallback;

import org.json.JSONObject;

public class LoginActivity extends AppCompatActivity {
    private EditText etPh906, etBirthday;
    private Button btnLogin;
    private ApiClient apiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login); // make sure this matches your actual XML file name

        etPh906 = findViewById(R.id.etPh906);       // Username (PH0906 ID)
        etBirthday = findViewById(R.id.etBirthday); // Birthday (manual input: YYYY-MM-DD)
        btnLogin = findViewById(R.id.btnLogin);     // Continue button

        apiClient = new ApiClient(this);

        // ✅ If already logged in, skip to Main
        if (apiClient.isLoggedIn()) {
            navigateToMain();
            return;
        }

        btnLogin.setOnClickListener(v -> attemptLogin());
    }

    private void attemptLogin() {
        String ph906 = etPh906.getText().toString().trim();
        String birthday = etBirthday.getText().toString().trim();

        if (ph906.isEmpty() || birthday.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // ✅ Regex: must match YYYY-MM-DD (e.g. 2005-08-21)
        if (!birthday.matches("\\d{4}-\\d{2}-\\d{2}")) {
            Toast.makeText(this, "Birthday must be in format YYYY-MM-DD", Toast.LENGTH_LONG).show();
            return;
        }

        btnLogin.setEnabled(false);
        btnLogin.setText("Logging in...");

        apiClient.studentLogin(ph906, birthday, new ApiCallback() {
            @Override public void onSuccess(JSONObject response) {
                runOnUiThread(() -> {
                    Toast.makeText(LoginActivity.this, "Login successful!", Toast.LENGTH_SHORT).show();
                    navigateToMain();
                });
            }
            @Override public void onError(String error) {
                runOnUiThread(() -> {
                    Toast.makeText(LoginActivity.this, "Login failed: " + error, Toast.LENGTH_LONG).show();
                    btnLogin.setEnabled(true);
                    btnLogin.setText("Continue");
                });
            }
        });
    }

    private void navigateToMain() {
        startActivity(new Intent(this, MainActivity.class));
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        finish();
    }
}

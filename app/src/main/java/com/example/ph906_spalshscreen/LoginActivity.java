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
        setContentView(R.layout.activity_login);

        etPh906 = findViewById(R.id.etPh906);
        etBirthday = findViewById(R.id.etBirthday);
        btnLogin = findViewById(R.id.btnLogin);

        apiClient = new ApiClient(this);

        btnLogin.setOnClickListener(v -> attemptLogin());
    }

    private void attemptLogin() {
        String ph906 = etPh906.getText().toString().trim();
        String birthday = etBirthday.getText().toString().trim();

        if (ph906.isEmpty() || birthday.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate date format YYYY-MM-DD
        if (!birthday.matches("\\d{4}-\\d{2}-\\d{2}")) {
            Toast.makeText(this, "Birthday must be in format YYYY-MM-DD", Toast.LENGTH_LONG).show();
            return;
        }

        btnLogin.setEnabled(false);
        btnLogin.setText("Logging in...");

        apiClient.studentLogin(ph906, birthday, new ApiCallback() {
            @Override
            public void onSuccess(JSONObject response) {
                runOnUiThread(() -> {
                    Toast.makeText(LoginActivity.this, "Login successful!", Toast.LENGTH_SHORT).show();

                    // Save birthday for age check
                    apiClient.saveBirthday(birthday);

                    // Determine adult/minor
                    boolean isAdult = apiClient.isAdult();
                    String version = isAdult ? "adult" : "minor";

                    // Go to TermsActivity
                    Intent intent = new Intent(LoginActivity.this, TermsActivity.class);
                    intent.putExtra("version", version);
                    startActivity(intent);
                    finish();
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    Toast.makeText(LoginActivity.this, "Login failed: " + error, Toast.LENGTH_LONG).show();
                    btnLogin.setEnabled(true);
                    btnLogin.setText("Continue");
                });
            }
        });
    }
}
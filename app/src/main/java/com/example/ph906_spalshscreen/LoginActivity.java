package com.example.ph906_spalshscreen;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    private EditText etUsername, etPassword;
    private Button btnLogin;
    private TextView tvForgot;

    private PrefsHelper prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Init SharedPreferences helper
        prefs = new PrefsHelper(this);

        // Bind views
        etUsername = findViewById(R.id.editTextText);
        etPassword = findViewById(R.id.editTextTextPassword);
        btnLogin = findViewById(R.id.button2);
        tvForgot = findViewById(R.id.textView3);

        // Login button click
        btnLogin.setOnClickListener(v -> {
            String enteredUsername = etUsername.getText().toString().trim();
            String enteredPassword = etPassword.getText().toString().trim();

            // For now, username is optional (can add later)
            String savedPassword = prefs.getPassword();

            if (enteredPassword.equals(savedPassword)) {
                Toast.makeText(LoginActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();

                // TODO: Navigate to Home screen or MainActivity
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(intent);
                finish();

            } else {
                Toast.makeText(LoginActivity.this, "Incorrect Password", Toast.LENGTH_SHORT).show();
            }
        });

        // Forgot Password click
        tvForgot.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
            startActivity(intent);
        });
    }
}

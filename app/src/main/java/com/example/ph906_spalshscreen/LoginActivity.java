package com.example.ph906_spalshscreen;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    EditText usernameInput, passwordInput;
    Button continueButton;

    private final String CORRECT_USERNAME = "PH0906";
    private final String CORRECT_PASSWORD = "Password";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login); // keeps your full XML intact

        // Reference views from your XML
        usernameInput = findViewById(R.id.editTextText);
        passwordInput = findViewById(R.id.editTextTextPassword);
        continueButton = findViewById(R.id.button2);

        continueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String enteredUsername = usernameInput.getText().toString().trim();
                String enteredPassword = passwordInput.getText().toString().trim();

                if (enteredUsername.equals(CORRECT_USERNAME) &&
                        enteredPassword.equals(CORRECT_PASSWORD)) {

                    // Go to MainActivity safely
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                } else {
                    Toast.makeText(LoginActivity.this,
                            "Invalid username or password", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}

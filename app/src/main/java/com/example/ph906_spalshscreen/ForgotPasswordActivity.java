package com.example.ph906_spalshscreen;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Random;

public class ForgotPasswordActivity extends AppCompatActivity {

    EditText etEmail;
    Button btnSend;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        etEmail = findViewById(R.id.etEmail);
        btnSend = findViewById(R.id.btnSendOTP);

        btnSend.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();

            if (email.isEmpty()) {
                Toast.makeText(this, "Please enter your email", Toast.LENGTH_SHORT).show();
            } else {
                // Generate a random 4-digit OTP
                String otp = String.valueOf(1000 + new Random().nextInt(9000));

                Toast.makeText(this, "OTP Generated: " + otp, Toast.LENGTH_SHORT).show();

                // Pass OTP and email to the next activity
                Intent intent = new Intent(this, OtpVerificationActivity.class);
                intent.putExtra("email", email);
                intent.putExtra("otp", otp);
                startActivity(intent);
            }
        });
    }
}

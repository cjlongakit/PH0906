package com.example.ph906_spalshscreen;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class OtpVerificationActivity extends AppCompatActivity {

    private EditText etOtp1, etOtp2, etOtp3, etOtp4;
    private Button btnVerify;
    private String correctOtp, email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp_verification);

        etOtp1 = findViewById(R.id.etOtp1);
        etOtp2 = findViewById(R.id.etOtp2);
        etOtp3 = findViewById(R.id.etOtp3);
        etOtp4 = findViewById(R.id.etOtp4);
        btnVerify = findViewById(R.id.btnVerifyOTP);

        // ✅ Always clear OTP fields when screen opens
        clearOtpFields();

        // Get OTP + email passed from ForgotPasswordActivity
        correctOtp = getIntent().getStringExtra("otp");
        email = getIntent().getStringExtra("email");

        // Setup OTP inputs with forward + backward navigation
        setupOtpInput(etOtp1, etOtp2, null);
        setupOtpInput(etOtp2, etOtp3, etOtp1);
        setupOtpInput(etOtp3, etOtp4, etOtp2);
        setupOtpInput(etOtp4, null, etOtp3);

        btnVerify.setOnClickListener(v -> verifyOtp());
    }

    private void verifyOtp() {
        String enteredOtp = etOtp1.getText().toString().trim() +
                etOtp2.getText().toString().trim() +
                etOtp3.getText().toString().trim() +
                etOtp4.getText().toString().trim();

        if (enteredOtp.length() < 4) {
            Toast.makeText(this, "Please enter the full OTP", Toast.LENGTH_SHORT).show();
            return;
        }

        if (enteredOtp.equals(correctOtp)) {
            Toast.makeText(this, "OTP Verified", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, ResetPasswordActivity.class);
            intent.putExtra("email", email);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "Invalid OTP", Toast.LENGTH_SHORT).show();
            clearOtpFields(); // ✅ Clear fields after wrong OTP
            etOtp1.requestFocus();
        }
    }

    // Forward + Backward OTP movement
    private void setupOtpInput(EditText current, EditText next, EditText previous) {
        // Forward typing
        current.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() == 1) {
                    if (next != null) {
                        next.requestFocus();
                    } else {
                        // ✅ Auto-submit when last box is filled
                        verifyOtp();
                    }
                }
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        // Backward delete
        current.setOnKeyListener((v, keyCode, event) -> {
            if (event.getAction() == KeyEvent.ACTION_DOWN &&
                    keyCode == KeyEvent.KEYCODE_DEL &&
                    current.getText().toString().isEmpty() &&
                    previous != null) {
                previous.requestFocus();
                previous.setText(""); // clear previous box
                return true;
            }
            return false;
        });
    }

    // ✅ Utility: Clear all OTP boxes
    private void clearOtpFields() {
        etOtp1.setText("");
        etOtp2.setText("");
        etOtp3.setText("");
        etOtp4.setText("");
    }
}
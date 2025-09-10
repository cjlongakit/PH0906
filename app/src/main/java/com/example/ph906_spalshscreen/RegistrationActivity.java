package com.example.ph906_spalshscreen;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class RegistrationActivity extends AppCompatActivity {

    EditText etNickname, etCurrentPlace, etTeacher, etMobile, etAltMobile;
    CheckBox cbAgree;
    Button btnContinue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_registration);

        // Bind UI
        etNickname = findViewById(R.id.etNickname);
        etCurrentPlace = findViewById(R.id.etPlace);
        etTeacher = findViewById(R.id.etTeacher);
        etMobile = findViewById(R.id.etMobile);
        etAltMobile = findViewById(R.id.etAltMobile);
        cbAgree = findViewById(R.id.cbAgree);
        btnContinue = findViewById(R.id.btnContinue);

        btnContinue.setOnClickListener(v -> registerUser());
    }

    private void registerUser() {
        String nickname = etNickname.getText().toString().trim();
        String currentPlace = etCurrentPlace.getText().toString().trim();
        String teacher = etTeacher.getText().toString().trim();
        String mobile = etMobile.getText().toString().trim();
        String altMobile = etAltMobile.getText().toString().trim();

        if (nickname.isEmpty() || currentPlace.isEmpty() || teacher.isEmpty() || mobile.isEmpty()) {
            Toast.makeText(this, "Please fill out all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!cbAgree.isChecked()) {
            Toast.makeText(this, "You must agree to the Terms and Conditions", Toast.LENGTH_SHORT).show();
            return;
        }

        // Save to SharedPreferences
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("nickname", nickname);
        editor.putString("currentPlace", currentPlace);
        editor.putString("teacher", teacher);
        editor.putString("mobile", mobile);
        editor.putString("altMobile", altMobile);
        editor.apply();

        Toast.makeText(this, "Registration successful!", Toast.LENGTH_SHORT).show();

        // Redirect to MainActivity
        Intent intent = new Intent(RegistrationActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}

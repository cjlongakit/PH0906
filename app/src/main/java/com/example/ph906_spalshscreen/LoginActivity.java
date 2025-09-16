package com.example.ph906_spalshscreen;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ph906_spalshscreen.models.LoginRequest;
import com.example.ph906_spalshscreen.models.LoginResponse;
import com.example.ph906_spalshscreen.network.ApiService;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class LoginActivity extends AppCompatActivity {

    private EditText etUsername, etPassword;
    private Button btnLogin;
    private TextView tvForgot;

    private PrefsHelper prefs;

    private static final String BASE_URL = "https://hjcdc.swuitapp.com/"; // ✅ Your server

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        prefs = new PrefsHelper(this);

        etUsername = findViewById(R.id.editTextText);
        etPassword = findViewById(R.id.editTextTextPassword);
        btnLogin = findViewById(R.id.button2);
        tvForgot = findViewById(R.id.textView3);

        btnLogin.setOnClickListener(v -> attemptLogin());

        tvForgot.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, ForgotPasswordActivity.class));
        });
    }

    private void attemptLogin() {
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter both username and password", Toast.LENGTH_SHORT).show();
            return;
        }

        // Logging for debugging
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient client = new OkHttpClient.Builder().addInterceptor(logging).build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ApiService api = retrofit.create(ApiService.class);

        LoginRequest body = new LoginRequest(username, password);
        api.login(body).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    LoginResponse lr = response.body();
                    if ("success".equalsIgnoreCase(lr.status)) {
                        // Save login info
                        prefs.saveToken(lr.token);
                        prefs.saveUsername(lr.username);
                        prefs.saveAge(lr.age);

                        // Route based on age
                        Intent intent = new Intent(LoginActivity.this, PrivacyActivity.class);
                        if (lr.age < 18) {
                            intent.putExtra("version", "minor");
                        } else {
                            intent.putExtra("version", "adult");
                        }
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(LoginActivity.this, lr.message, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(LoginActivity.this, "Invalid response: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                Toast.makeText(LoginActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}

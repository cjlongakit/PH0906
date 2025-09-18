package com.example.ph906_spalshscreen.api;

import okhttp3.*;
import org.json.JSONObject;
import org.json.JSONException;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import android.content.Context;
import android.content.SharedPreferences;

public class ApiClient {
    private static final String BASE_URL = "https://hjcdc.swuitapp.com/api/api.php/login";
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private OkHttpClient client;
    private SharedPreferences prefs;

    public ApiClient(Context context) {
        client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();
        prefs = context.getSharedPreferences("student_prefs", Context.MODE_PRIVATE);
    }

    // ---- STUDENT LOGIN ----
    public void studentLogin(String ph906, String birthday, ApiCallback callback) {
        try {
            JSONObject json = new JSONObject();
            json.put("username", ph906);
            json.put("password", birthday);

            RequestBody body = RequestBody.create(JSON, json.toString());
            Request request = new Request.Builder()
                    .url(BASE_URL + "/login.php")
                    .post(body)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override public void onFailure(Call call, IOException e) {
                    callback.onError("Network error: " + e.getMessage());
                }
                @Override public void onResponse(Call call, Response response) throws IOException {
                    String responseBody = response.body().string();
                    try {
                        JSONObject jsonResponse = new JSONObject(responseBody);
                        if ("success".equals(jsonResponse.optString("status"))) {
                            saveLoginInfo(
                                    jsonResponse.optString("ph906"),
                                    jsonResponse.optString("token"),
                                    jsonResponse.optString("name"),
                                    jsonResponse.optBoolean("is_default_password", true)
                            );
                            // Save birthday from login input or API response
                            saveBirthday(birthday);
                            callback.onSuccess(jsonResponse);
                        } else {
                            callback.onError(jsonResponse.optString("message", "Login failed"));
                        }
                    } catch (JSONException e) {
                        callback.onError("JSON error: " + e.getMessage());
                    }
                }
            });
        } catch (JSONException e) {
            callback.onError("JSON creation error: " + e.getMessage());
        }
    }

    // ---- CHANGE PASSWORD ----
    public void changePassword(String currentPassword, String newPassword, ApiCallback callback) {
        String token = getToken();
        if (token == null) { callback.onError("Not logged in"); return; }

        try {
            JSONObject json = new JSONObject();
            json.put("current_password", currentPassword);
            json.put("new_password", newPassword);

            RequestBody body = RequestBody.create(JSON, json.toString());
            Request request = new Request.Builder()
                    .url(BASE_URL + "/change_password")
                    .addHeader("Authorization", token)
                    .post(body)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override public void onFailure(Call call, IOException e) {
                    callback.onError("Network error: " + e.getMessage());
                }
                @Override public void onResponse(Call call, Response response) throws IOException {
                    String responseBody = response.body().string();
                    try {
                        JSONObject jsonResponse = new JSONObject(responseBody);
                        if ("success".equals(jsonResponse.optString("status"))) {
                            prefs.edit().putBoolean("is_default_password", false).apply();
                            callback.onSuccess(jsonResponse);
                        } else {
                            callback.onError(jsonResponse.optString("message", "Failed to change password"));
                        }
                    } catch (JSONException e) {
                        callback.onError("JSON parsing error: " + e.getMessage());
                    }
                }
            });
        } catch (JSONException e) {
            callback.onError("JSON creation error: " + e.getMessage());
        }
    }

    // ---- PROFILE ----
    public void getMyProfile(ApiCallback callback) { requestWithToken("/my_profile", "GET", null, callback); }
    public void updateMyProfile(JSONObject payload, ApiCallback callback) { requestWithToken("/my_profile", "PUT", payload, callback); }

    // ---- EVENTS ----
    public void getUpcomingEvents(ApiCallback callback) { requestWithToken("/my_events", "GET", null, callback); }

    // ---- HISTORY ----
    public void getMyHistory(ApiCallback callback) { requestWithToken("/my_history", "GET", null, callback); }

    // ---- HELPERS ----
    private void requestWithToken(String endpoint, String method, JSONObject jsonBody, ApiCallback callback) {
        String token = getToken();
        if (token == null) { callback.onError("Not logged in"); return; }

        Request.Builder builder = new Request.Builder()
                .url(BASE_URL + endpoint)
                .addHeader("Authorization", token);

        if ("POST".equalsIgnoreCase(method) || "PUT".equalsIgnoreCase(method)) {
            builder.method(method, RequestBody.create(JSON, jsonBody.toString()));
        } else {
            builder.get();
        }

        client.newCall(builder.build()).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) { callback.onError("Network error: " + e.getMessage()); }
            @Override public void onResponse(Call call, Response response) throws IOException {
                String res = response.body().string();
                try {
                    JSONObject jsonResponse = new JSONObject(res);
                    if ("success".equals(jsonResponse.optString("status"))) callback.onSuccess(jsonResponse);
                    else callback.onError(jsonResponse.optString("message", "Request failed"));
                } catch (JSONException e) { callback.onError("JSON parse error: " + e.getMessage()); }
            }
        });
    }

    // ---- LOCAL STORAGE ----
    private void saveLoginInfo(String ph906, String token, String fullName, boolean isDefaultPassword) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("ph906", ph906);
        editor.putString("token", token);
        editor.putString("full_name", fullName);
        editor.putBoolean("is_default_password", isDefaultPassword);
        editor.putBoolean("is_logged_in", true);
        editor.apply();
    }

    public String getToken() { return prefs.getString("token", null); }
    public String getLoggedInStudentId() { return prefs.getString("ph906", null); }
    public String getFullName() { return prefs.getString("full_name", ""); }
    public boolean isDefaultPassword() { return prefs.getBoolean("is_default_password", true); }
    public boolean isLoggedIn() { return prefs.getBoolean("is_logged_in", false); }
    public void logout() { prefs.edit().clear().apply(); }

    // ---- AGE & BIRTHDAY HELPERS ----
    public void saveBirthday(String birthday) {
        prefs.edit().putString("birthday", birthday).apply();
    }

    public String getBirthday() {
        return prefs.getString("birthday", null);
    }

    public boolean isAdult() {
        String birthday = getBirthday();
        if (birthday == null) return false;

        try {
            String[] parts = birthday.split("-");
            int year = Integer.parseInt(parts[0]);
            int month = Integer.parseInt(parts[1]);
            int day = Integer.parseInt(parts[2]);

            java.util.Calendar dob = java.util.Calendar.getInstance();
            dob.set(year, month - 1, day);

            java.util.Calendar today = java.util.Calendar.getInstance();
            int age = today.get(java.util.Calendar.YEAR) - dob.get(java.util.Calendar.YEAR);

            if (today.get(java.util.Calendar.DAY_OF_YEAR) < dob.get(java.util.Calendar.DAY_OF_YEAR)) {
                age--;
            }

            return age >= 18;
        } catch (Exception e) {
            return false;
        }
    }
}
package com.example.ph906_spalshscreen.api;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONObject;
import org.json.JSONException;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.*;

/* ============================================================
 * ApiClient.java
 * ------------------------------------------------------------
 * Handles all API calls for PH906 app, including:
 * 1. Student Login
 * 2. Change Password
 * 3. Profile retrieval and update
 * 4. Events retrieval
 * 5. Local session storage (token, birthday, version)
 * ------------------------------------------------------------
 * Uses: OkHttp3 for network calls, SharedPreferences for local storage
 * ============================================================ */

public class ApiClient {

    // ==============================
    // Constants
    // ==============================
    private static final String BASE_URL = "https://hjcdc.swuitapp.com/api/api.php"; // Base API URL
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8"); // Media type for JSON requests

    // ==============================
    // Members
    // ==============================
    private OkHttpClient client;             // OkHttp client
    private SharedPreferences prefs;         // SharedPreferences for storing session info

    // ==============================
    // Constructor
    // ==============================
    public ApiClient(Context context) {
        client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();

        prefs = context.getSharedPreferences("student_prefs", Context.MODE_PRIVATE);
    }

    // ==============================
    // STUDENT LOGIN
    // ==============================
    /**
     * Logs in a student with username (ph906) and birthday
     */
    public void studentLogin(String ph906, String birthday, ApiCallback callback) {
        try {
            JSONObject json = new JSONObject();
            json.put("username", ph906);
            json.put("password", birthday);

            RequestBody body = RequestBody.create(JSON, json.toString());
            Request request = new Request.Builder()
                    .url(BASE_URL + "/student_login")  // Fixed endpoint
                    .post(body)
                    .build();

            client.newCall(request).enqueue(new okhttp3.Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    callback.onError("Network error: " + e.getMessage());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String responseBody = response.body().string();
                    try {
                        JSONObject jsonResponse = new JSONObject(responseBody);
                        if ("success".equals(jsonResponse.optString("status"))) {

                            // Determine version based on age
                            boolean adult = isAdultFromBirthday(jsonResponse.optString("birthday", birthday));
                            String version = adult ? "adult" : "minor";

                            // Save info
                            saveLoginInfo(
                                    jsonResponse.optString("ph906"),
                                    jsonResponse.optString("token"),
                                    jsonResponse.optString("full_name"),  // Fixed field name
                                    jsonResponse.optBoolean("is_default_password", true)
                            );
                            saveBirthday(jsonResponse.optString("birthday", birthday));
                            saveVersion(version); // <-- Save version

                            callback.onSuccess(jsonResponse);
                        } else {
                            callback.onError(jsonResponse.optString("message", "Login failed"));
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

    // ==============================
    // CHANGE PASSWORD
    // ==============================
    /**
     * Changes the logged-in student's password
     */
    public void changePassword(String currentPassword, String newPassword, ApiCallback callback) {
        String token = getToken();
        if (token == null) {
            callback.onError("Not logged in");
            return;
        }

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

            client.newCall(request).enqueue(new okhttp3.Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    callback.onError("Network error: " + e.getMessage());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
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
                        callback.onError("JSON parse error: " + e.getMessage());
                    }
                }
            });
        } catch (JSONException e) {
            callback.onError("JSON creation error: " + e.getMessage());
        }
    }

    // ==============================
    // PROFILE
    // ==============================
    public void getMyProfile(ApiCallback callback) {
        requestWithToken("/my_profile", "GET", null, callback);
    }

    public void updateMyProfile(JSONObject payload, ApiCallback callback) {
        requestWithToken("/my_profile", "PUT", payload, callback);
    }

    // ==============================
    // EVENTS
    // ==============================
    public void getUpcomingEvents(ApiCallback callback) {
        requestWithToken("/my_events", "GET", null, callback);
    }

    // ==============================
    // STUDENTS/LETTERS DATA
    // ==============================
    /**
     * Gets students data for Letters section - fetches from 'students' table
     */
    public void getStudentsData(ApiCallback callback) {
        String token = getToken();
        if (token == null) {
            callback.onError("Not logged in");
            return;
        }

        Request request = new Request.Builder()
                .url(BASE_URL + "/students")
                .addHeader("Authorization", token)
                .get()
                .build();

        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onError("Network error: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseBody = response.body().string();

                android.util.Log.d("API_DEBUG", "Students response: " + responseBody);

                try {
                    JSONObject jsonResponse = new JSONObject(responseBody);
                    // Handle both array response and object response with data field
                    if (jsonResponse.has("data") || jsonResponse instanceof JSONObject) {
                        callback.onSuccess(jsonResponse);
                    } else {
                        callback.onError("No data found");
                    }
                } catch (JSONException e) {
                    callback.onError("JSON parsing error: " + e.getMessage());
                }
            }
        });
    }

    // ==============================
    // INTERNAL HELPER: REQUEST WITH TOKEN
    // ==============================
    private void requestWithToken(String endpoint, String method, JSONObject jsonBody, ApiCallback callback) {
        String token = getToken();
        if (token == null) {
            callback.onError("Not logged in");
            return;
        }

        Request.Builder builder = new Request.Builder()
                .url(BASE_URL + endpoint)
                .addHeader("Authorization", token);

        if ("POST".equalsIgnoreCase(method) || "PUT".equalsIgnoreCase(method)) {
            RequestBody body = jsonBody != null ?
                    RequestBody.create(JSON, jsonBody.toString()) :
                    RequestBody.create(JSON, "{}");
            builder.method(method, body);
        } else {
            builder.get();
        }

        client.newCall(builder.build()).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onError("Network error: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    String res = response.body().string();
                    JSONObject jsonResponse = new JSONObject(res);
                    if ("success".equals(jsonResponse.optString("status"))) {
                        callback.onSuccess(jsonResponse);
                    } else {
                        callback.onError(jsonResponse.optString("message", "Request failed"));
                    }
                } catch (Exception e) {
                    callback.onError("JSON parse error: " + e.getMessage());
                }
            }
        });
    }

    // ==============================
    // LOCAL STORAGE METHODS
    // ==============================
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

    public void saveBirthday(String birthday) { prefs.edit().putString("birthday", birthday).apply(); }
    public String getBirthday() { return prefs.getString("birthday", null); }

    // ==============================
    // VERSION STORAGE
    // ==============================
    public void saveVersion(String version) { prefs.edit().putString("version", version).apply(); }
    public String getSavedVersion() { return prefs.getString("version", null); }

    // ==============================
    // AGE HELPERS
    // ==============================
    public boolean isAdult() {
        String birthday = getBirthday();
        if (birthday == null) return false;
        return isAdultFromBirthday(birthday);
    }

    private boolean isAdultFromBirthday(String birthday) {
        try {
            String[] parts = birthday.split("-");
            int year = Integer.parseInt(parts[0]);
            int month = Integer.parseInt(parts[1]);
            int day = Integer.parseInt(parts[2]);

            java.util.Calendar dob = java.util.Calendar.getInstance();
            dob.set(year, month - 1, day);

            java.util.Calendar today = java.util.Calendar.getInstance();
            int age = today.get(java.util.Calendar.YEAR) - dob.get(java.util.Calendar.YEAR);
            if (today.get(java.util.Calendar.DAY_OF_YEAR) < dob.get(java.util.Calendar.DAY_OF_YEAR)) age--;

            return age >= 18;
        } catch (Exception e) {
            return false;
        }
    }
}